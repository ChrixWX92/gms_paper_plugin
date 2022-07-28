package com.gms.paper.data;

import com.gms.paper.interact.InteractionHandler;

public class InteractionState {

    protected LessonProgress lessonProgress;
    protected String buttonType;
    protected GamePosition questionPosition;
    protected GamePosition targetPosition;
    protected String worldId;

    InteractionState() {
        init(null);
    }

    InteractionState(InteractionHandler handler) {
        init(handler);
    }

    private void init(InteractionHandler currentHandler) {
        if (currentHandler == null)
            currentHandler = InteractionHandler.getCurrent();

        if (currentHandler != null) {
            this.lessonProgress = currentHandler.getLessonProgress();
            this.buttonType = currentHandler.getQuestion().type;
            this.questionPosition = currentHandler.getQuestionPos();
            this.targetPosition = currentHandler.getTargetPos();

            Lesson lesson = User.getCurrent().getCurrentLesson();
            if (lesson != null)
                this.worldId = lesson.cleanContentId();
        }
    }

    //getters
    public String getButtonType() {
        return buttonType;
    }

    public LessonProgress getLessonProgress() {
        return lessonProgress;
    }

    public GamePosition getQuestionPosition() {
        return questionPosition;
    }

    public GamePosition getTargetPosition() {
        return targetPosition;
    }

    public String getWorldId() {
        return worldId;
    }
}
