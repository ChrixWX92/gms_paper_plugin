package com.gms.paper.data;

import com.gms.paper.MCServer;
import com.gms.paper.interact.tpqs.TPQS_Answer;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

public class Question extends GenericContent {
    public String prompt;
    public JsonObject metadata;

    @SerializedName("skills")
    public String[] skillIds;
    private transient Skill[] _skills;

    public String type;
    public String ageRange;
    public int difficultyLevel;
    public int ticketsSuccess = 0;
    public int ticketsFailure = 0;
    public JsonObject answer;
    public boolean dynamicQuestion = false;
    public boolean randomise = true;

    private transient TPQS_Answer _tpqsAnswer = null;

    private transient String _extraTPQSPrompt = null;

    public Skill getPrimarySkill() {
        return _skills.length > 0 ? _skills[0] : null;
    }

    public Skill getSecondarySill() {
        return _skills.length > 1 ? _skills[1] : null;
    }

    public Question cache() {
        if (isCached())
            return this;

        _skills = new Skill[skillIds.length];

        if (skillIds.length > 0) {
            /// The only thing that we need is to check the skill
            for (int si = 0; si < skillIds.length; si++)
                _skills[si] = Skill.find(skillIds[si]);
        }

        return setCached();
    }

    public static Question getId(String id) {
        return CmsApi.s_public.getContentFromId("questions", id, Question.class);
    }

    public static Question get(String contentId) {
        return CmsApi.s_public.getContent("questions", contentId, Question.class);
    }

    public LessonProgress markCorrect(ChildProfile profile, long attemptTimeMilliseconds, String answerText, String questionText,
                                      Course course, Lesson lesson, QuestionSet qset) {
        return mark(profile, true, attemptTimeMilliseconds, answerText, questionText, course, lesson, qset);
    }

    public LessonProgress markIncorrect(ChildProfile profile, long attemptTimeMilliseconds, String answerText, String questionText,
                                        Course course, Lesson lesson, QuestionSet qset) {
        return mark(profile, false, attemptTimeMilliseconds, answerText, questionText, course, lesson, qset);
    }

    public LessonProgress mark(ChildProfile profile, boolean correct, long attemptTimeMilliseconds, String answerText, String questionText,
                               Course course, Lesson lesson, QuestionSet qset) {

        if (Helper.isDev()) {

            if (correct) {
                Log.debug(String.format("Correct answer award: %d [User = %s]", ticketsSuccess, User.getCurrent().username));
                profile.tickets += ticketsSuccess;
            }
            else {
                Log.debug(String.format("Wrong answer penalty: %d [User = %s]", ticketsFailure, User.getCurrent().username));

                if (ticketsFailure > 0)
                    profile.tickets -= ticketsFailure;
                else
                    profile.tickets += ticketsFailure;
            }

            LessonProgress progress = profile.getCurrentProgress();

            if (progress.currentQuestion() < 100 && correct) {
                progress.setCurrentQuestion(progress.currentQuestion() + 1);
            }
            return progress;
        }

        JsonObject body = new JsonObject();

        if (dynamicQuestion) {
            /// Only send these if its a dynamic question
            body.addProperty("questionText", questionText);
            body.addProperty("answerText", answerText);
        }
        else {
            /// otherwise its not needed
            body.addProperty("questionText", "");
            body.addProperty("answerText", "");
        }

        body.addProperty("milliseconds", attemptTimeMilliseconds);
        body.addProperty("correct", correct);
        body.addProperty("contentId", contentId);
        body.addProperty("courseId", course._id);
        body.addProperty("lessonId", lesson._id);
        body.addProperty("questionSetId", qset._id);
        body.addProperty("questionId", _id);
        body.addProperty("partialContentId", partialContentId());

        String jsonResult = MCServer.getAPI().post("answers", body);

        try {
            JsonObject result = JsonParser.parseString(jsonResult).getAsJsonObject();
            LessonProgress lessonProgress = profile.getCurrentProgress(); ///(new Gson()).fromJson(result, LessonProgress.class);

            lessonProgress.setCurrentQuestion(result.get("currentQuestion").getAsInt());

            profile.tickets = result.get("tickets").getAsInt();
            return lessonProgress;
        }
        catch (Exception e) {
            Log.exception(e, String.format("Exception while handling answer. Json Request: %s", body));
            Log.exception(e, String.format("Exception while handling answer. Json Response: %s", jsonResult));
        }

        return null;
    }

    public TPQS_Answer getTPQSAnswer() {
        if (_tpqsAnswer != null)
            return _tpqsAnswer;
        _tpqsAnswer = new TPQS_Answer(answer);
        return _tpqsAnswer;
    }

    public String getExtraPrompt() {
        if (_extraTPQSPrompt != null)
            return _extraTPQSPrompt;

        _extraTPQSPrompt = Helper.getKeyDefault(metadata, "extraTPQSPrompt", "");

        if (_extraTPQSPrompt.isEmpty())
            Log.debug("Extra prompt not found");

        return _extraTPQSPrompt;
    }

    public String getAnswerKey(String key) {
        if (!answer.has(key))
            return "";

        return answer.get(key).getAsString();
    }
}
