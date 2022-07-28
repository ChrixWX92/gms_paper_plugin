package com.gms.paper.interact.puzzles.handlers;

import cn.nukkit.Player;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.window.CustomWindowForm;
import com.gms.mc.data.Course;
import com.gms.mc.data.GamePosition;
import com.gms.mc.data.LessonProgress;
import com.gms.mc.data.QuestionIdInfo;
import com.gms.mc.error.InvalidBackendQueryException;
import com.gms.mc.error.InvalidFrameWriteException;
import com.gms.mc.interact.puzzles.BackendUtils;
import com.gms.mc.interact.puzzles.Pairs;
import com.gms.mc.interact.puzzles.maths.Arithmetic;
import com.gms.mc.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class PAIRS_InteractionHandler extends PUZZLE_InteractionHandler {

    @Override
    public void handle(PlayerInteractEvent event) throws IOException {
        try {
            super.handle(event);
        } catch (IOException | InvalidBackendQueryException ioException) {
            ioException.printStackTrace();
        }

        AtomicReference<String> id = new AtomicReference<>();
        AtomicReference<String> facing = new AtomicReference<>();
        AtomicReference<Integer> apothem = new AtomicReference<>();

        CustomWindowForm window = FormAPI.customWindowForm("pairs", "§lGenerate PAIRS Puzzle")
                .addLabel("§lQuestion Set ID")
                .addInput("ID", "(e.g. \"#tlp.1.1\")")
                .addLabel("§lFacing")
                .addStepSlider("Facing", "The cardinal direction towards which the puzzle will face", Arithmetic.getFacings())
                .addLabel("§lApothem")
                .addSlider("Apothem", "In blocks, the size of this puzzle's Anchor's apothem", 2, 50, 1, 8);

        window.sendTo(player);

        CustomWindowForm handler = window.addHandler((e) -> {
            if (!this.handled) {
                if (e.isClosed()) return;
                id.set(e.getForm().getResponse().getInputResponse(1));
                facing.set(e.getForm().getResponse().getStepSliderResponse(3).getElementContent());
                apothem.set((int) e.getForm().getResponse().getSliderResponse(5));
                HashMap<Integer, String[]> answers = new HashMap<>();
                switch (id.get()) {
                    case "0" -> {
                        answers.put(0, new String[]{"Letter", "A"});
                        answers.put(1, new String[]{"Letter", "B"});
                        answers.put(2, new String[]{"Letter", "C"});
                        answers.put(3, new String[]{"Number", "1"});
                        answers.put(4, new String[]{"Number", "2"});
                        answers.put(5, new String[]{"Number", "3"});
                    }
                    case "1" -> {
                        answers.put(0, new String[]{"Food", "Bread"});
                        answers.put(1, new String[]{"Food", "Cake"});
                        answers.put(2, new String[]{"Food", "Soup"});
                        answers.put(3, new String[]{"Food", "Biscuits"});
                        answers.put(4, new String[]{"Food", "Burger"});
                        answers.put(5, new String[]{"Poison", "Arsenic"});
                        answers.put(6, new String[]{"Poison", "Cyanide"});
                        answers.put(7, new String[]{"Poison", "Strychnine"});
                        answers.put(8, new String[]{"Poison", "Nightshade"});
                        answers.put(9, new String[]{"Poison", "Hemlock"});
                    }
                    case "2" -> {
                        answers.put(0, new String[]{"Bird", "Pigeon"});
                        answers.put(1, new String[]{"Bird", "Seagull"});
                        answers.put(2, new String[]{"Bird", "Owl"});
                        answers.put(3, new String[]{"Bird", "Ostrich"});
                        answers.put(4, new String[]{"Plane", "Enola Gay"});
                        answers.put(5, new String[]{"Plane", "The Wright Flyer"});
                        answers.put(6, new String[]{"Plane", "Concorde"});
                        answers.put(7, new String[]{"Plane", "Air Force One"});
                        answers.put(8, new String[]{"Superman", "Henry Cavill"});
                        answers.put(9, new String[]{"Superman", "Stevie Wonder"});
                        answers.put(10, new String[]{"Superman", "Chuck Norris"});
                        answers.put(11, new String[]{"Superman", "Eamonn Holmes"});
                    }
                    default -> {
                        try {
                            QuestionIdInfo idInfo = new QuestionIdInfo(id.get().toLowerCase().trim());
                            subHandle(idInfo);
                            BackendUtils.setPuzzleData(player, "Pairs", id.get().toLowerCase(), questionSet, 1);
                        } catch (InvalidBackendQueryException | IOException invalidBackendQueryException) {
                            invalidBackendQueryException.printStackTrace();
                        }
                        answers = BackendUtils.getPairsData();
                    }
                }

                try {
                    Pairs.generatePairs(event.getPlayer(), event.getBlock().getLocation(), facing.get(), answers, apothem.get());
                } catch (InvalidFrameWriteException | InterruptedException exception) {
                    exception.printStackTrace();
                }
                this.handled = true;
            }
        });
    }

    private void subHandle(QuestionIdInfo idInfo) throws InvalidBackendQueryException, IOException {

        super.setupQuestion(new GamePosition(null, buttonBlock));
        course = Course.get(idInfo.courseId);
        if (course != null) lesson = course.getLesson(player, idInfo);
        GamePosition gamePosition = new GamePosition(null, buttonBlock); //Must be the same as the world in which the lesson is taking place
        LessonProgress progress = profile.getProgress(idInfo, gamePosition);
        profile.setCurrentProgress(progress);
        idInfo.setQuestionId(progress.currentQuestionSet(), progress.currentQuestion());
        course = Course.get(idInfo.courseId);
        lesson = course.getLesson(player, idInfo);
        questionSet = course.getQuestionSet(player, idInfo);
        question = course.getQuestion(player, idInfo, profile.getCurrentProgress().level); //TODO May need changing for assemble parts

    }

    public static void fetchPairs(Player player, String questionSetID) {

        try {
            BackendUtils.setPuzzleData(player, "Pairs", questionSetID.toLowerCase(), 1);
        } catch (InvalidBackendQueryException invalidBackendQueryException) {
            BackendUtils.resetPuzzleData();
            Log.error(TextFormat.RED + "Unable to retrieve data from backend. Ensure that questions have been uploaded and a valid question set ID is specified.");
            invalidBackendQueryException.printStackTrace();
            return;
        }

        Pairs.initializePairs(BackendUtils.getPairsData().entrySet().size());

    }

}
