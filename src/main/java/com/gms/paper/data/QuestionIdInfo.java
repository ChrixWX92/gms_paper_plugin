package com.gms.paper.data;

public class QuestionIdInfo {
    public static class DifficultyLevel {
        public static int undefined = 0;
        public static int easy = 1;
        public static int medium = 5;
        public static int hard = 10;

        public static int[] levels = new int[] {
                DifficultyLevel.undefined,
                DifficultyLevel.easy,
                DifficultyLevel.medium,
                DifficultyLevel.hard
        };

        public static int getIndexForLevel(int level) {
            for (int i = 0; i < levels.length; i++) {
                if (levels[i] == level)
                    return i;
            }

            return -1;
        }
    }

    public String contentId;
    public String courseId;
    public int lessonId = -1;
    public int questionSetId = 1;
    public int questionId = 1;
    public int difficultyLevel = 0;    /// Default to unknown difficulty level

    public String lessonContentId;
    public String questionSetContentId;
    public String questionContentIdWithoutDifficulty;
    public String questionContentId;

    public QuestionIdInfo(String contentId_) {
        assert contentId_.charAt(0) == '#';

        this.contentId = contentId_.replace("#", "");
        assert !contentId.isEmpty();

        String[] parts = contentId.split("\\.");
        assert parts.length > 0;

        courseId = parts[0];
        if (parts.length > 1)
            lessonId = Integer.parseInt(parts[1]);

        if (parts.length > 2)
            questionSetId = Integer.parseInt(parts[2]);

        if (parts.length > 3)
            questionId = Integer.parseInt(parts[3]);

        if (parts.length > 4)
            difficultyLevel = Integer.parseInt(parts[4]);

        lessonContentId = String.format("%s.%d", courseId, lessonId);
        questionSetContentId = String.format("%s.%d", lessonContentId, questionSetId);
        updateQuestionId();
    }

    public boolean isLesson() {
        return lessonContentId != null && !lessonContentId.isEmpty();
    }

    public boolean isCourse() {
        return courseId != null && !courseId.isEmpty();
    }

    public boolean isQuestionSet() {
        return questionSetContentId != null && !questionSetContentId.isEmpty();
    }

    public boolean isUndefinedDifficultyLevel() {
        return difficultyLevel == DifficultyLevel.undefined;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
        updateQuestionId();
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
        updateQuestionId();
    }

    public void setQuestionId(int questionSetId, int questionId) {
        this.questionSetId = questionSetId;
        questionSetContentId = String.format("%s.%d", lessonContentId, questionSetId);
        setQuestionId(questionId);
    }

    private void updateQuestionSetId() {
        questionContentIdWithoutDifficulty = String.format("%s.%d", questionSetContentId, questionId);
        questionContentId = String.format("%s.%s", questionContentIdWithoutDifficulty, difficultyLevel);
    }

    private void updateQuestionId() {
        questionContentIdWithoutDifficulty = String.format("%s.%d", questionSetContentId, questionId);
        questionContentId = String.format("%s.%s", questionContentIdWithoutDifficulty, difficultyLevel);
    }

    public boolean isValidQuestion() {
        return questionId >= 1;
    }
}
