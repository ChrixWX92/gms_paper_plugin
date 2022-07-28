package com.gms.paper.data;

public interface IAnswer {
    public class Feedback {
        public String msg;
        public GamePosition pos;

        public Feedback() {}
        public Feedback(String msg, GamePosition pos) {
            this.msg = msg;
            this.pos = pos;
        }
        public Feedback(String msg) {
            this.msg = msg;
        }
    }

    public class Info {
        public int index;
        public String questionText;
        public String answerText;

        public Info() {}
        public Info(int index) {
            this.index = index;
        }
    }

    public String getAnswerText();
    public Feedback getIncorrectFeedback(Info info);
    public Feedback getCorrectFeedback();
    public boolean isCorrect(Info info);
}
