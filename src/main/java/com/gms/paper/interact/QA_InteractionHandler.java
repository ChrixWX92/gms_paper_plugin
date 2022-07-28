package com.gms.paper.interact;

import cn.nukkit.event.player.PlayerInteractEvent;
import com.gms.paper.util.TextFormat;
import com.gms.paper.data.*;
import com.gms.paper.error.InvalidBackendQueryException;
import com.gms.paper.interact.tpqs.TPQS_Answer;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import com.gms.paper.util.MessageUtil;

import java.io.IOException;

public class QA_InteractionHandler extends InteractionHandler {

    private LessonProgress progress;
    private QuestionSet qs;
    private InteractionHandler questionHandler;
    private Question q;
    private int numTickets;

    private long attemptTime;
    private int answerIndex;

    private IAnswer.Info ansInfo;

    private int followUpQuestionId;

    private void init() {
        //get question location based on player
        Course course = Course.get(idInfo.courseId);

        if (course == null) {
            Log.logAndSend(player, String.format("Unable to find courseId from question ID: %s", idInfo.contentId));
            return;
        }

        progress = profile.getCurrentProgress();
        qs = course.getQuestionSet(player, idInfo);
        questionHandler = s_questionSetup;
        q = questionHandler.question;
        numTickets = profile.tickets;

        attemptTime = questionHandler.getQuestionAttemptTime();
        answerIndex = Integer.parseInt(signText[2]);

        /// Reset the question start time
        questionHandler.resetStartTime();

        ansInfo = new IAnswer.Info(answerIndex);

        // set follow-up question info
        TPQS_Answer answer = q.getTPQSAnswer();
        var originalAnswerIndex = answer.getOriginalIndex(answerIndex); //get original answer index
        followUpQuestionId = progress.currentQuestion() * 100 + originalAnswerIndex;
    }

    @Override
    public void handle(PlayerInteractEvent event) throws IOException, InvalidBackendQueryException {
        super.handle(event);

        init();

        if (questionHandler.answer.isCorrect(ansInfo)) {
            try {
                correctAnswerHandler();
            } catch (InvalidBackendQueryException e) {
                e.printStackTrace();
            }
        }
        //if incorrect and has follow-up question, set that up
        else if (qs.hasFollowUpQuestion(idInfo, followUpQuestionId)) {
            setUpFollowUpQuestion();
        }

        //if incorrect and no followup question
        else {
            this.givePlayerAnswerFeedback(false);
            progress = q.markIncorrect(profile, attemptTime, questionHandler.answer.getAnswerText(), getQuestionText(q), course, lesson, qs);
            updateTicketsFeedback(false);
        }
    }

    private void correctAnswerHandler() throws InvalidBackendQueryException, IOException {
        /// Mark the question as ended
        questionHandler.endQuestion();

        progress = q.markCorrect(profile, attemptTime, questionHandler.answer.getAnswerText(), getQuestionText(q), course, lesson, qs);

        //if question set or lesson ended
        if (!isFollowUpQuestion()) {
            if (progress.currentQuestion() > qs.numQuestions(progress.level)) {
                if (!progress.isLastQuestionSet(questionHandler.lesson)) {
                    /// Finish the question set
                    progress = progress.finishQuestionSet(questionHandler.lesson);
                }
                else {
                    /// Finish the lesson
                    progress = progress.finish();
                    Helper.setPlayerTitle(player, MessageUtil.s_lessonFinished);
                }

                updateTicketsFeedback(true);
                this.givePlayerAnswerFeedback(true);
                questionHandler.resetHandlerState(null);
                teleportPlayerPostQuestionSet(player, questionHandler.targetPos);

                return;
            }
        }

        //if follow-up question marked correct
        else {
            // return to original question
            markCorrectFollowUpQuestion();
        }

        player.sendMessage(String.format("Question Number: %d", progress.currentQuestion()));

        updateTicketsFeedback(true);
        this.givePlayerAnswerFeedback(true);
        questionHandler.setupQuestion(questionHandler.questionPos);
    }

    private void givePlayerAnswerFeedback(boolean correct) {
        var feedback = questionHandler.getIncorrectFeedback(ansInfo);
        var color = TextFormat.RED;

        if(correct){
            feedback = questionHandler.getCorrectFeedback();
            color = TextFormat.GREEN;
        }

        Helper.setPlayerSubtitle(player, feedback.msg);
        //player.sendMessage(color + feedback.msg);

    }

    private void updateTicketsFeedback(boolean wonTickets) {
        if (wonTickets) {
            int won = profile.tickets - numTickets;
            //player.sendMessage(String.format("You've earned %d ticket(s).", won));
            profile.showTicketsStatus(player);
        }
        else {
            int loss = numTickets - profile.tickets;
            //player.sendMessage(String.format("A wrong answer loses %d ticket(s).", loss));
            profile.showTicketsStatus(player);
        }
    }

    //region FollowUp

    private void setUpFollowUpQuestion() {
        this.givePlayerAnswerFeedback(false);
        progress.setCurrentQuestion(followUpQuestionId);
        progress = q.markIncorrect(profile, attemptTime, questionHandler.answer.getAnswerText(), getQuestionText(q), course, lesson, qs);
        try {
            questionHandler.setupQuestion(questionHandler.questionPos);
        }
        catch (InvalidBackendQueryException | IOException e) {
            e.printStackTrace();
        }
    }

    private void markCorrectFollowUpQuestion() {
        //this.givePlayerAnswerFeedback(true);
        progress = q.markCorrect(profile, attemptTime, questionHandler.answer.getAnswerText(), getQuestionText(q), course, lesson, qs);
        progress.setCurrentQuestion(progress.currentQuestion() / 100);
    }

    private boolean isFollowUpQuestion() {
        return progress.currentQuestion() > 100;
    }

    // endregion
}
