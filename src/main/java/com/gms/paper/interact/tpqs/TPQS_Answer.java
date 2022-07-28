package com.gms.paper.interact.tpqs;

import com.gms.paper.data.GamePosition;
import com.gms.paper.data.IAnswer;
import com.gms.paper.util.Helper;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Comparator;

public class TPQS_Answer implements IAnswer {
    static int s_maxAnswers = 5;

    public class Item {
        public String answer;
        public String feedbackPhrase;
        public GamePosition coords;

        Feedback feedback() {
            return new Feedback(feedbackPhrase, coords);
        }
    }

    protected ArrayList<Item> _originalAnswerOptions = new ArrayList<>();
    public ArrayList<Item> answerOptions = new ArrayList<>();
    public String wrongAnswerFeedback;
    public String correctAnswerFeedback;
    public String correctAnswer;
    public int orgCorrectAnswerIndex;
    public int correctAnswerIndex;

    public TPQS_Answer(JsonObject meta) {
        wrongAnswerFeedback = Helper.getKeyDefault(meta, "wrongAnswerFeedback", "");
        correctAnswerFeedback = Helper.getKeyDefault(meta, "correctAnswerFeedback", "");
        correctAnswerIndex = Integer.parseInt(meta.get("correctAnswerIndex").getAsString()) - 1;
        orgCorrectAnswerIndex = correctAnswerIndex;

        for (int i = 0; i < s_maxAnswers; i++) {
            String answerKey = String.format("answer%d", i + 1);

            if (meta.has(answerKey)) {
                Item item = new Item();
                item.answer = meta.get(answerKey).getAsString();
                item.feedbackPhrase = meta.get(String.format("%sFeedbackPhrase", answerKey)).getAsString();
                String coords = meta.get(String.format("%sCoords", answerKey)).getAsString();

                if (!coords.isEmpty())
                    item.coords = Helper.parseLocation(coords);

                _originalAnswerOptions.add(item);
            }
        }

        //correctAnswer = _originalAnswerOptions.get(correctAnswerIndex).answer;

        randomizeAnswers();
        sortRandomizedAnswers();

    }

    private void randomizeAnswers(){
        answerOptions = new ArrayList<>(_originalAnswerOptions.size());
        int i = 0;
        while (i++ < _originalAnswerOptions.size())
            answerOptions.add(null);

        ArrayList<Integer> rindices = Helper.randomiseIndices(0, _originalAnswerOptions.size());
        for (int ri = 0; ri < _originalAnswerOptions.size(); ri++) {
            int orgIndex = ri;
            int newIndex = rindices.get(ri).intValue();
            answerOptions.set(newIndex, _originalAnswerOptions.get(orgIndex));

            /// Correct answer indices are 1 based
            if (orgIndex == orgCorrectAnswerIndex)
                correctAnswerIndex = newIndex;
        }
        correctAnswer = answerOptions.get(correctAnswerIndex).answer;
    }

    private void sortRandomizedAnswers() {
        //debug
       /* for(var ans : answerOptions) {
            Log.debug(String.format("Before Sort Answers : %s", ans.answer));
        }*/



        // keep answers randomized but move the empty answers to the end
        Comparator<Item> cm = (o1, o2) -> {
            if (o1.answer.isEmpty()) {
                return 1;
            }
            return -1;
        };

        answerOptions.sort(cm);

        // make sure to update the correctAnswerIndex using the correctAnswer
        Item correctAnswerItem = answerOptions.stream()
                .filter(e -> e.answer.equals(correctAnswer))
                .findFirst()
                .orElse(null);
        correctAnswerIndex = answerOptions.indexOf(correctAnswerItem);



        //debug
        /*for(var ans : answerOptions) {
            Log.debug(String.format("After Sort Answers : %s", ans.answer));
        }*/
    }


//    public Question getRandomised() {
//        try {
//            Question q = (Question) this.clone();
//        }
//        catch (CloneNotSupportedException e) {
//            e.printStackTrace();
//            return this;
//        }
//    }

    @Override
    public String getAnswerText() {
        return correctAnswer;
    }

    public Feedback getFeedbackAt(int index) {
        if (index >= 0 && index < answerOptions.size()) {
            Item opt = answerOptions.get(index);
            return opt.feedback();
        }

        return null;
    }

    public Feedback getIncorrectFeedback(Info info) {
        var fb = getFeedbackAt(info.index - 1); //Answer index in world are 1 based
        if (fb != null && fb.msg != null && !fb.msg.isEmpty())
            return fb;

        if (wrongAnswerFeedback != null && !wrongAnswerFeedback.isEmpty())
            return new Feedback(wrongAnswerFeedback);

        return fb;
    }

    @Override
    public Feedback getCorrectFeedback() {
        Feedback fb = getFeedbackAt(correctAnswerIndex);
        if (fb != null && fb.msg != null && !fb.msg.isEmpty())
            return fb;

        if (correctAnswerFeedback != null && !correctAnswerFeedback.isEmpty())
            return new Feedback(correctAnswerFeedback);

        return fb;
    }

    @Override
    public boolean isCorrect(Info info) {
        /// Indices are 1 based in the game but 0 based otherwise
        return (info.index - 1) == correctAnswerIndex;
    }


    public int getOriginalIndex(int answerIndex){
        // world indexes for answers are 1 based
        Item answerItem = answerOptions.get(answerIndex - 1);
        int originalIndex = _originalAnswerOptions.indexOf(answerItem) + 1;
        return originalIndex;
    }
}
