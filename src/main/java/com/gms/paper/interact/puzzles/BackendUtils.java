package com.gms.paper.interact.puzzles;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import com.gms.mc.PlayerInstance;
import com.gms.mc.custom.sound.Chord;
import com.gms.mc.custom.sound.ChordType;
import com.gms.mc.custom.sound.MusicMaker;
import com.gms.mc.custom.sound.Note;
import com.gms.mc.data.*;
import com.gms.mc.error.InvalidBackendQueryException;
import com.gms.mc.interact.puzzles.maths.Arithmetic;
import com.gms.mc.util.Helper;
import com.gms.mc.util.Log;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.nukkit.level.Sound.*;
import static com.gms.mc.interact.puzzles.PuzzleType.PAIRS;

public class BackendUtils {

    private static final String printResult = "%s: Result = [%s] | Attempt Time = [%s] | Question = [%s] | Answer = [%s] | Course = [%s] | Lesson = [%s] | Question Set = [%s]";
    private static String questionSetID = "";
    private static PuzzleType puzzleType;

    private static TreeMap<Integer, Question> questions = new TreeMap<>();

    private static HashMap<String, String> checksData = new HashMap<>();
    private static HashMap<Integer, String[]> mobGroupData = new HashMap<>();
    private static HashMap<Integer, String[]> pairsData = new HashMap<>();
    private static HashMap<Integer, List<HashMap<Integer, String>>> assembleData = new HashMap<>();


    public static void setPuzzleData(Player player, String puzzleType, String questionSetID, int difficulty) throws InvalidBackendQueryException {
        setPuzzleData(player, puzzleType, questionSetID, null, difficulty);
    }

    /**
     * Returns puzzle data from the backend in the following forms:
     * CHECKS   - HashMap<K = Prompt/Question (String), V = Correct Answer (String)>
     * MOBGROUP - HashMap<K = Question Number (Integer), V = String[0] = Prompt, [1] = Correct Answer, [2] = Correct Item, [3] = Mob Type>
     * PAIRS - HashMap<K = Question Number (Integer), V = String[0] = Prompt, [1] = Correct Answer>
     * ASSEMBLE - HashMap<K = Question Number (Integer), V = List<HashMap<K = Word Position, V = String[0] = Correct Answer Word, [1] = Prompt Word, [2] = Answer1 Word>>>
     *
     * @param player        The Player object
     * @param puzzleType    The type of Puzzle to be parsed
     * @param questionSetID The respective Question Set ID, as a String
     * @param qs2           A specific QuestionSet object. If null is passed, the method will get the object using the questionSetID argument
     * @param difficulty    The QuestionSet's difficulty level
     * @throws InvalidBackendQueryException If data for the specified Question Set ID can't be found.
     */
    public static void setPuzzleData(Player player, String puzzleType, String questionSetID, QuestionSet qs2, int difficulty) throws InvalidBackendQueryException {
        Log.info(TextFormat.LIGHT_PURPLE + "Retrieving backend data for " + TextFormat.AQUA + questionSetID + TextFormat.LIGHT_PURPLE + "...");

        questions = new TreeMap<>();

        QuestionIdInfo idInfo = new QuestionIdInfo(questionSetID);
        QuestionIdInfo questionIdInfo;
        Course c = Course.get(idInfo.courseId);
        QuestionSet qs;
        if (qs2 == null) qs = c.getQuestionSet(player, idInfo);
        else qs = qs2;
        for (int i = 1; i <= qs.numQuestions(difficulty); i++) {
            questionIdInfo = new QuestionIdInfo(questionSetID + "." + i);
            questions.put(i, qs.getQuestion(player, questionIdInfo, difficulty));
        }

        setQuestionSetID(questionSetID);
        switch (puzzleType) {
            case "Checks" -> parseChecksData();
            case "MobGroup" -> parseMobGroupData();
            case "Pairs" -> {
                parsePairsData();
                setPuzzleType(PAIRS);
            }
            case "Assemble" -> parseAssembleData();
            default -> {
                Log.error(TextFormat.RED + "Invalid puzzle type.");
                return;
            }
        }
        Log.info(TextFormat.GREEN + "Data successfully loaded.");
    }

    public static void resetPuzzleData() {
        //General
        questions = new TreeMap<>();
        setQuestionSetID("");
        setPuzzleType(null);

        //Checks
        checksData = new HashMap<>();
        Checks.setChecksSolved(0);

        //MobGroup
        mobGroupData = new HashMap<>();

        //Pairs
        pairsData = new HashMap<>();
        Pairs.resetPairsData();

        //Assemble
        assembleData = new HashMap<>();
    }

    private static HashMap<String, String> parseChecksData() {
        checksData = new HashMap<>();
        for (Map.Entry<Integer, Question> e : questions.entrySet()) {
            Question q = e.getValue();
            String prompt = q.prompt;
            String answer = q.answer.get("correctAnswer").getAsString();
            checksData.put(prompt, answer);
        }
        return checksData;
    }

    private static HashMap<Integer, String[]> parseMobGroupData() {
        mobGroupData = new HashMap<>();
        //String[0] = Prompt, [1] = Correct Answer, [2] = Correct Item, [3] = Mob Type
        for (Map.Entry<Integer, Question> e : questions.entrySet()) {
            String[] strings = new String[4];
            Question q = e.getValue();
            strings[0] = q.prompt;
            strings[1] = q.answer.get("correctAnswer").getAsString();
            strings[2] = q.metadata.get("mobgroup_correctItem").getAsString();
            strings[3] = q.metadata.get("mobgroup_mobType").getAsString();
            mobGroupData.put(e.getKey(), strings);
        }
        return mobGroupData;
    }

    private static HashMap<Integer, String[]> parsePairsData() {
        pairsData = new HashMap<>();
        int counter = 1;
        for (Map.Entry<Integer, Question> e : questions.entrySet()) {
            Question q = e.getValue();
            String prompt = q.prompt;
            String answer = q.answer.get("correctAnswer").getAsString();
            pairsData.put(counter, new String[]{prompt, answer});
            counter++;
        }
        return pairsData;
    }

    private static HashMap<Integer, List<HashMap<Integer, String>>> parseAssembleData() {
        assembleData = new HashMap<>();
        int counter = 1;
        for (Map.Entry<Integer, Question> e : questions.entrySet()) {
            if (e.getValue() != null) {
                Question q = e.getValue();
                String[] configurationsArray = new String[3];
                configurationsArray[0] = q.answer.get("correctAnswer").getAsString();
                configurationsArray[1] = q.prompt;
                configurationsArray[2] = q.answer.get("answer1").getAsString();
                assembleData.put(counter, parseAssembleStrings(configurationsArray));
                counter++;
            }
        }
        return assembleData;
    }

    private static List<HashMap<Integer, String>> parseAssembleStrings(String[] strings) {
        List<HashMap<Integer, String>> configurations = new ArrayList<>();
        for (String string : strings) {
            HashMap<Integer, String> configuration = new HashMap<>();


            List<String> stringList = new ArrayList<>();
            Pattern p = Pattern.compile("\\[(.*?)\\]");
            Matcher m = p.matcher(string);
            while (m.find()) {
                stringList.add(m.group(1));
            }

            String[] words = new String[stringList.size()];
            stringList.toArray(words);

            //String[] words = string.split("(?<=\[)([^\]]+)(?=\])");
            //String[] words = string.split("\\[(.*?)\\]");
            int subCounter = 0;
            for (String word : words) {
                configuration.put(subCounter, word);
                subCounter++;
            }
            configurations.add(configuration);
        }
        return configurations;
    }


    public static HashMap<Integer, Integer> difficultyTally(QuestionSet qs, boolean broadcast) {
        HashMap<Integer, Integer> difficultyTally = new HashMap<>();
        for (int i = 0; i <= 10; i++) {
            int num = qs.numQuestions(i);
            if (broadcast) Log.debug(i + " = " + num);
            difficultyTally.put(i, num);
        }
        return difficultyTally;
    }

    public static void throwIQBE(Class clazz) throws InvalidBackendQueryException {
        throw new InvalidBackendQueryException(Log.errorMsg("Backend retrieval failure at " + clazz.getName().toUpperCase()));
    }

    public static void cleanUpEntitieswithTag(Player p, String tag) {
        var allFrames = Arithmetic.getFrames(p, tag);
        while (allFrames.size() > 0) {
            for (var frame : allFrames) {
                frame.close();
                allFrames = Arithmetic.getFrames(p, BackendUtils.getQuestionSetID());
            }
        }
    }

    public static HashMap<String, String> getChecksData() {
        return checksData;
    }

    public static HashMap<Integer, String[]> getMobGroupData() {
        return mobGroupData;
    }

    public static HashMap<Integer, String[]> getPairsData() {
        return pairsData;
    }

    public static HashMap<Integer, List<HashMap<Integer, String>>> getAssembleData() {
        return assembleData;
    }

    private static void setQuestionSetID(String id) {
        questionSetID = id;
    }

    public static String getQuestionSetID() {
        return questionSetID;
    }

    public static PuzzleType getPuzzleType() {
        return puzzleType;
    }

    public static void setPuzzleType(PuzzleType puzzleType) {
        BackendUtils.puzzleType = puzzleType;
    }

    private static ChildProfile profile;
    private static Course course;
    private static Lesson lesson;
    private static QuestionSet questionSet;
    private static int tickets;


    public static void markAnswers(Player p, PuzzleType puzzleType, boolean result, HashMap<Integer, String> data) {

        PlayerInstance playerInstance = PlayerInstance.getPlayer(p.getName());
        assert playerInstance != null;
        profile = playerInstance.getProfile();

        QuestionIdInfo idInfo = new QuestionIdInfo(questionSetID);
        course = Course.get(idInfo.courseId);
        lesson = course.getLesson(p, idInfo);
        questionSet = course.getQuestionSet(p, idInfo);

        LessonProgress progress = profile.getProgress(idInfo, new GamePosition(null, p.getPosition()));
        profile.setCurrentProgress(progress);

        Question q = new Question();
        // Mark and send data according to puzzle type
        switch (puzzleType) {
            case PAIRS -> markPairs(result, data);
            case ASSEMBLE -> markAssemble(result, data);
            case MOBGROUP -> markMobGroup(result);
            case CHECKS -> markChecks(result, data);
        }
        // Feedback + Tickets
        BackendUtils.giveAnswerFeedback(p, result);
        BackendUtils.giveMusicFeedback(p, result);
        BackendUtils.updateTickets(p, result, profile);
    }

    private static void markPairs(boolean result, HashMap<Integer, String> data) {
        Question q = new Question();
        boolean idFound = false;
        if (result) {
            // Finding the question that corresponds to the pair //NOTE: Item frames do not know which question number they belong to so find in list from metadata.
            Map.Entry<Integer, Question>[] entryArray = questions.entrySet().toArray(new Map.Entry[questions.entrySet().size()]);
            for (int i = 0; i < entryArray.length; i++) {
                q = entryArray[i].getValue();
                for (int j = 0; j < data.size(); j++) {
                    String frameString = data.get(j);
                    if (frameString.equals(q.prompt)) {
                        q = entryArray[i].getValue();
                        idFound = true;
                    }
                    if (idFound) break;
                }
                if (idFound) break;
            }

            //mark
            q.mark(profile, result, 0, q.answer.get("correctAnswer").getAsString(), q.prompt, course, lesson, questionSet);

            tickets = q.ticketsSuccess;
            Log.info(printResult(result, q));
        }
        else {
            q.mark(profile, result, 0, "Incorrect Pair", "Incorrect Pair", course, lesson, questionSet);

            tickets = q.ticketsFailure;
            Log.info(String.format(printResult, puzzleType, "Incorrect", 0, data.get(1), data.get(0), course.title, lesson.title, questionSetID));
        }

    }

    private static void markAssemble(boolean result, HashMap<Integer, String> data) {
        Question q = questions.get(Assemble.getQuestionNumber());

        if (result) {
            //mark
            q.mark(profile, result, 0, q.answer.get("correctAnswer").getAsString(), q.prompt, course, lesson, questionSet);

            tickets = q.ticketsSuccess;
            Log.info(printResult(result, q));
        }
        else {

            String entries = "";
            int i = 0;
            while (i < data.size()) {
                entries = entries.concat("[%s]");
                entries = String.format(entries, data.get(i));
                i++;
            }

            //mark
            q.mark(profile, result, 0, entries, q.prompt, course, lesson, questionSet);

            tickets = q.ticketsFailure;
            Log.info(String.format(printResult, puzzleType, "Incorrect", 0, q.prompt, entries, course.title, lesson.title, questionSetID));
        }

    }

    private static void markMobGroup(boolean result) {
        //NOTE: MobGroup needs to add all tickets from all questions together
        Question q = new Question();
        tickets = 0;

        if (result) {
            for (Map.Entry<Integer, Question> integerQuestionEntry : questions.entrySet()) {

                q = integerQuestionEntry.getValue();
                //mark
                q.mark(profile, result, 0, q.answer.get("correctAnswer").getAsString(), q.prompt, course, lesson, questionSet);
                tickets++;

                Log.info(printResult(result, q));
                ;
            }
        }
        else {
            q.mark(profile, result, 0, "Incorrect", questionSetID, course, lesson, questionSet);

            Log.info(String.format(printResult, puzzleType, "Incorrect", 0, "Incorrect", questionSetID, course.title, lesson.title, questionSetID));
        }

    }

    private static void markChecks(boolean result, HashMap<Integer, String> data) {

        Question q = new Question();

        Set<Map.Entry<Integer, Question>> entrySet = questions.entrySet();//.toArray(new Map.Entry[questions.entrySet().size()]);
        for (Map.Entry<Integer, Question> integerQuestionEntry : entrySet) {
            Question q2 = integerQuestionEntry.getValue();
            if (integerQuestionEntry.getValue().prompt.replaceAll(" ", "").equals(data.get(0).replaceAll(" ", ""))) {
                q = q2;
                break;
            }
        }

        if (result) {
            //mark
            q.mark(profile, result, 0, q.answer.get("correctAnswer").getAsString(), q.prompt, course, lesson, questionSet);
            tickets = q.ticketsSuccess;

            Log.info(printResult(result, q));
        }
        else {
            q.mark(profile, result, 0, "Incorrect Row", "Incorrect Row", course, lesson, questionSet);
            tickets = q.ticketsFailure;

            Log.info(String.format(printResult, puzzleType, "Incorrect", 0, data.get(0), data.get(1), course.title, lesson.title, questionSetID));
        }

    }

    private static void giveAnswerFeedback(Player player, boolean correct) {
        var feedback = "Not quite, \nbut try again!";
        var color = TextFormat.GOLD;

        if (correct) {
            feedback = "§2Correct!";
            color = TextFormat.GREEN;
        }

        Helper.setPlayerTitle(player, feedback);
        player.sendMessage(color + feedback);
    }

    private static void giveMusicFeedback(Player p, boolean result) {
        if (result) {
            Chord correct = new Chord(Note.C5, ChordType.MAJ, 1, false);
            MusicMaker.playArpeggio(p, correct, 105, NOTE_HARP);
            Arithmetic.puzzleName = null;
        }
        else {
            MusicMaker.playNote(p, Note.C3, NOTE_XYLOPHONE);
        }
    }

    private static void updateTickets(Player player, boolean result, ChildProfile profile) {
        if (result) {
            profile.showTicketsStatus(player, String.format("You've earned %d ticket(s).", tickets));
        }
        else {
            profile.showTicketsStatus(player, String.format("A wrong answer loses %d ticket(s).", tickets));
        }
    }

    private static String printResult(boolean result, Question q) {
        String mark = result ? "Correct" : "Incorrect";
        return
                String.format(printResult,
                        puzzleType.toString().toUpperCase(),          // PuzzleType
                        mark,                                         // Correct or incorrect
                        0,                                            // Time
                        q.prompt,                                     // Question (prompt)
                        q.answer.get("correctAnswer").getAsString(),  // Answer
                        course.title,                                 // Course
                        lesson.title,                                 // Lesson
                        questionSetID                               // Question Set ID
                );
    }

}
