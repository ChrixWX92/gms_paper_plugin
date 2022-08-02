package com.gms.paper.data;

import com.google.gson.annotations.SerializedName;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class QuestionSet extends GenericContent {
    @SerializedName("questions")
    Question[] _questions;

    private transient HashMap<Integer, ArrayList<Question>> _questionList = new HashMap<Integer, ArrayList<Question>>();
    private transient HashMap<Integer, Integer> _numQuestions = new HashMap<Integer, Integer>();
    private transient HashMap<String, Question> _questionLUT = new HashMap<>();

    public int numQuestions(int difficultlyLevel) {
        Integer dlevel = Integer.valueOf(difficultlyLevel);
        Integer numQuestions = _numQuestions.get(dlevel);
        if (numQuestions == null)
            return 0;

        return numQuestions.intValue();
    }

    public QuestionSet cache() {
        if (isCached())
            return this;

        /// Just cache all the questions
        for (Question question : _questions) {
            question.cache();

            Integer dlevel = Integer.valueOf(question.difficultyLevel);

            if (_questionList.get(dlevel) == null) {
                _questionList.put(dlevel, new ArrayList<>());
                _numQuestions.put(dlevel, 0);
            }

            Integer numQuestions = _numQuestions.get(dlevel);
            QuestionIdInfo idInfo = new QuestionIdInfo(question.contentId);

            /// Only follow-up questions can have an ID > 100
            if (idInfo.questionId < 100) {
                numQuestions = Math.max(numQuestions, idInfo.questionId);
                _numQuestions.put(dlevel, numQuestions);
            }

            _questionList.get(dlevel).add(question);

            _questionLUT.put(question.contentId, question);
            _questionLUT.put(question._id, question);
        }

        return setCached();
    }

    public Question getQuestion(Player player, QuestionIdInfo idInfo, int progressLevel) {
        cache();

        Question q = null;

        if (progressLevel != QuestionIdInfo.DifficultyLevel.undefined)
            idInfo.setDifficultyLevel(progressLevel);

        q = _questionLUT.get(idInfo.questionContentId);

        if (q == null) {
            int startIndex = QuestionIdInfo.DifficultyLevel.getIndexForLevel(progressLevel);

            /// Need to discuss the correct behaviour with Jack and Katherine for this
            if (startIndex <= 0 || startIndex >= QuestionIdInfo.DifficultyLevel.levels.length)
                startIndex = QuestionIdInfo.DifficultyLevel.levels.length - 1;

            /// Try to find the first question with any difficulty level, starting with the easiest (hardest?)
            for (int i = startIndex; i >= 0 && q == null; i--) {
                idInfo.setDifficultyLevel(QuestionIdInfo.DifficultyLevel.levels[i]);
                q = _questionLUT.get(idInfo.questionContentId);
            }

        }

        return q;
    }

    public static QuestionSet getId(String id) {
        return CmsApi.s_public.getContentFromId("question-sets", id, QuestionSet.class);
    }

    public static QuestionSet get(String contentId) {
        return CmsApi.s_public.getContent("question-sets", contentId, QuestionSet.class);
    }

    public boolean hasFollowUpQuestion(QuestionIdInfo idInfo, int followUpQuestionId)
    {
        String followUpQuestionContentId =  idInfo.questionSetContentId + "." + followUpQuestionId + "." + idInfo.difficultyLevel;
        QuestionIdInfo followupIdInfo = new QuestionIdInfo(followUpQuestionContentId);
        var question = this.getQuestion(null, followupIdInfo, QuestionIdInfo.DifficultyLevel.undefined);
        return question != null;
    }
}
