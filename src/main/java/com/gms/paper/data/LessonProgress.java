package com.gms.paper.data;

import com.gms.paper.MCServer;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import com.google.gson.JsonObject;

import java.util.HashMap;

public class LessonProgress extends ContentProgress {
    private Activity lessonEnter;
    private Activity subjectEnter;
    private QuestionSetProgress qsProgress;

    private static HashMap<String, LessonProgress> s_progressMapDev = new HashMap<>();

    public void setCurrentQuestionSet(int currentQuestionSet) {
        metadata.remove("currentQuestionSet");
        metadata.addProperty("currentQuestionSet", currentQuestionSet);

        if (qsProgress == null) {
            QuestionIdInfo idInfo = new QuestionIdInfo(String.format("%s.%d", contentId, currentQuestionSet));
            qsProgress = QuestionSetProgress.loadOrCreate(idInfo);
            assert qsProgress != null;
        }
    }

    public int currentQuestionSet() {
        if (metadata.has("currentQuestionSet"))
            return metadata.get("currentQuestionSet").getAsInt();
        return 0;
    }

    public void setCurrentQuestion(int currentQuestion) {
        assert qsProgress != null;
        qsProgress.setCurrentQuestion(currentQuestion);
    }

    public int currentQuestion() {
        return qsProgress.currentQuestion();
    }

    public GamePosition location() {
        if (metadata.has("location"))
            return GamePosition.fromJson(metadata.getAsJsonObject("location").toString());
        return new GamePosition();
    }

    private static LessonProgress loadDev(QuestionIdInfo idInfo, GamePosition pos) {
        LessonProgress progress = s_progressMapDev.get(idInfo.lessonContentId);

        if (progress != null)
            return progress;

        progress = new LessonProgress();
        progress.contentId = idInfo.contentId;

        progress.qsProgress = QuestionSetProgress.loadDev(idInfo);
        progress.metadata.addProperty("currentQuestionSet", idInfo.questionSetId);
        progress.metadata.add("location", pos.toJson());

        return progress;
    }

    public static LessonProgress loadOrCreate(QuestionIdInfo idInfo, GamePosition pos) {
        JsonObject metadata = new JsonObject();

        metadata.addProperty("currentQuestionSet", idInfo.questionSetId);
        metadata.add("location", pos.toJson());

        try {
            LessonProgress progress = ContentProgress.loadOrCreate(idInfo, idInfo.lessonContentId, metadata, LessonProgress.class);
            Course course = Course.get(idInfo.courseId);
            int questionSetId = progress.currentQuestionSet();

            progress.qsProgress = QuestionSetProgress.loadOrCreate(idInfo);
            assert progress.qsProgress != null;

            progress.start(course.getSubject());
            progress.setCurrent(idInfo.questionSetId, idInfo.questionId);

            return progress;
        }
        catch (Exception e) {
            Log.exception(e, String.format("Unable to create lesson progress: %s", idInfo.contentId));
        }

        return null;
    }

    public void start(Subject subject) {
        assert qsProgress != null;
        qsProgress.start();

        /// Save this information in user state
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
        qsProgress.end();

        if (Helper.isLocalDev() || lessonEnter == null)
            return;

        lessonEnter.end();
        lessonEnter = null;
    }

    public void setCurrent(int questionSetId, int questionId) {
        int actualCurrentQuestionSet = currentQuestionSet();
        int actualCurrentQuestion = currentQuestion();

        setCurrentQuestionSet(questionSetId);
        setCurrentQuestion(questionId);

        User.getCurrent().getState().setProgressId(_id);

        Log.debug(String.format("Changing lesson progress from [%s]: %d.%d => %d.%d", contentId, actualCurrentQuestionSet, actualCurrentQuestion, currentQuestionSet(), currentQuestion()));
    }

    public LessonProgress finishQuestionSet(Lesson lesson) {
        /// Finish and then move on
        qsProgress.finish();
        qsProgress = null;

        int currentQuestionSet = this.currentQuestionSet();

        if (isLastQuestionSet(lesson)) {
            return this;
        }

        setCurrentQuestionSet(currentQuestionSet + 1);

        return this;
    }

    public boolean isLessonFinshed(Lesson lesson) {
        return currentQuestionSet() > lesson.getNumQuestionSets();
    }

    public boolean isLastQuestionSet(Lesson lesson) {
        return currentQuestionSet() == lesson.getNumQuestionSets();
    }

    public LessonProgress finish() {
        if (Helper.isLocalDev())
            return this;

        String lessonProgressJson = MCServer.getAPI().put(String.format("finish-lesson/%s", _id), null);

        if (lessonEnter != null)
            lessonEnter.end();
        if (subjectEnter != null)
            subjectEnter.end();

        LessonProgress progress = MCServer.getAPI().getContentFromJson(lessonProgressJson, LessonProgress.class, false);

        progress.lessonEnter = null;
        progress.subjectEnter = null;

        User.getCurrent().getProfile().setCurrentProgress(progress);

        return progress;
    }

    public void moveToQuestion(QuestionIdInfo idInfo, GamePosition pos) {
    }
}
