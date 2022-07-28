package com.gms.paper.data;

import com.gms.paper.MCServer;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ContentProgress extends GenericContent {
    public String type;
    public int numAttempts = 0;
    public int numSuccessful = 0;
    public int numSuccessfulUnique = 0;
    public int level = 1;
    public JsonObject metadata = new JsonObject();
    public boolean isFinished = false;

    private static <T extends ContentProgress> T loadDev(QuestionIdInfo idInfo, JsonObject metadata, Class<T> cls) throws Exception {
        var progress = (T)cls.getConstructors()[0].newInstance();

        if (idInfo.isQuestionSet()) {
            progress.type = "questionSet";
            progress.contentId = idInfo.questionSetContentId;
        }
        else if (idInfo.isLesson()) {
            progress.type = "lesson";
            progress.contentId = idInfo.lessonContentId;
        }
        else {
            progress.type = "course";
            progress.contentId = idInfo.courseId;
        }

        progress.numAttempts = 0;
        progress.numSuccessful = 0;

        progress.metadata = metadata;

        return progress;
    }

    public static <T extends ContentProgress> T loadOrCreate(QuestionIdInfo idInfo, String contentId, JsonObject metadata, Class<T> cls) throws Exception {
        if (Helper.isLocalDev()) {
            return loadDev(idInfo, metadata, cls);
        }

        String endPoint = String.format("content-progresses/%s", User.getCurrent()._id);
        T progress = MCServer.getAPI().getProgress(endPoint, contentId, cls);

        if (progress == null) {
            Log.debug(String.format("Unable to find lesson progress for contentId: %s, Creating new progress ...", contentId));

            progress = loadDev(idInfo, metadata, cls);
            progress.contentId = contentId;

            JsonObject progressJson = new JsonObject();

            progressJson.addProperty("contentId", contentId);
            progressJson.add("metadata", metadata);

            String contentJson = MCServer.getAPI().post("content-progresses", progressJson);

            if (contentJson == null || contentJson.isEmpty())
                return null;

            progress = (new Gson()).fromJson(contentJson, cls);
        }

        return progress;
    }

    public JsonObject toJson() {
        return (JsonObject)(new Gson()).toJsonTree(this);
    }
}
