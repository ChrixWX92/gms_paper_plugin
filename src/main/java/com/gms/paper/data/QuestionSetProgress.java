package com.gms.paper.data;

import com.gms.paper.MCServer;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import com.google.gson.JsonObject;

import java.util.HashMap;

public class QuestionSetProgress extends ContentProgress {
    private Activity questionSetEnter;

    private static HashMap<String, QuestionSetProgress> s_progressMapDev = new HashMap<>();

    public void setCurrentQuestion(int currentQuestion) {
        metadata.remove("currentQuestion");
        metadata.addProperty("currentQuestion", currentQuestion);
    }

    public int currentQuestion() {
        if (metadata.has("currentQuestion"))
            return metadata.get("currentQuestion").getAsInt();
        return 0;
    }

    public static QuestionSetProgress loadDev(QuestionIdInfo idInfo) {
        QuestionSetProgress progress = s_progressMapDev.get(idInfo.lessonContentId);

        if (progress != null)
            return progress;

        progress = new QuestionSetProgress();
        progress.metadata.addProperty("currentQuestion", idInfo.questionId);

        return progress;
    }

    public static QuestionSetProgress loadOrCreate(QuestionIdInfo idInfo) {
        JsonObject metadata = new JsonObject();

        metadata.addProperty("currentQuestion", idInfo.questionId);

        try {
            QuestionSetProgress progress = ContentProgress.loadOrCreate(idInfo, idInfo.questionSetContentId, metadata, QuestionSetProgress.class);

            progress.start();
            progress.setCurrentQuestion(idInfo.questionId);

            return progress;
        }
        catch (Exception e) {
            Log.exception(e, String.format("Unable to create lesson progress: %s", idInfo.contentId));
        }

        return null;
    }

    public void start() {
        /// Save this information in user state`
        if (Helper.isLocalDev())
            return;

//        if (lessonEnter == null) {
//            lessonEnter = Activity.begin(Activity.Type.s_lesson, new String[][] {
//                    { "contentId", lessonId }
//            });
//        }

//        if (subjectEnter == null) {
//            subjectEnter = Activity.begin(Activity.Type.s_subject, new String[][] {
//                    { "contentId",  subject.name }
//            });
//        }
    }

    public void end() {
        if (Helper.isLocalDev() || questionSetEnter == null)
            return;

        questionSetEnter.end();
        questionSetEnter = null;
    }

    public QuestionSetProgress finish() {
        if (Helper.isLocalDev()) {
            isFinished = true;
            return this;
        }

        if (questionSetEnter != null) {
            questionSetEnter.end();
            questionSetEnter = null;
        }

        MCServer.getAPI().put(String.format("finish-progress/%s", _id), null);

        return this;
    }

    public boolean isFinished(QuestionSet qset) {
        return currentQuestion() > qset.numQuestions(level);
    }
}
