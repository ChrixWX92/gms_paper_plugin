package com.gms.paper.interact.tpqs;

import com.gms.paper.data.*;
import com.gms.paper.error.InvalidBackendQueryException;
import com.gms.paper.interact.InteractionHandler;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import org.bukkit.World;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.IOException;

public class TPQS_InteractionHandler extends InteractionHandler {
    public static GamePosition[] findSignLocations(World world, GamePosition questionLocation) {
        GamePosition[] signPositions = new GamePosition [s_answerOffsets.length + 1];

        signPositions[0] = questionLocation.add(s_questionOffset);

        for (int i = 0; i < s_answerOffsets.length; i++)
            signPositions[i + 1] = questionLocation.add(s_answerOffsets[i]);

        return signPositions;
    }

    @Override
    public void setupQuestion(GamePosition qpos) throws InvalidBackendQueryException, IOException {
        super.setupQuestion(qpos);

        LessonProgress progress = profile.getCurrentProgress();

        idInfo.setQuestionId(progress.currentQuestionSet(), progress.currentQuestion());

        questionSet = course.getQuestionSet(player, idInfo);
        question = course.getQuestion(player, idInfo, profile.getCurrentProgress().level);

        if (question == null)
            throw new RuntimeException(String.format("Unable to find question: %s [Level Progress: %d]", idInfo.questionContentId, profile.getCurrentProgress().level));

        TPQS_Answer ans = question.getTPQSAnswer();
        answer = ans;

        GamePosition[] signLocations = findSignLocations(world, qpos);

//        GamePosition additionalInfoLoc = signLocations[0].add(s_questionAdditionalInfoOffset);
//        populateSign(level, "this is a test", additionalInfoLoc);

        populateSign(world, Helper.formatQuestionSign(question.prompt), signLocations[0]);

        for (int i = 1; i < signLocations.length && i < ans.answerOptions.size() + 1; i++) {
            GamePosition signLocation = signLocations[i];
            TPQS_Answer.Item ansOption = ans.answerOptions.get(i - 1);
            if (!populateSign(world,Helper.formatAnswerSign(ansOption.answer), signLocation))
                Log.error(String.format("Error populating sign index: %d", i + 1));
        }

        //if extra prompt available
        if(question.getExtraPrompt() != null){
            GamePosition promptLocation = qpos.add(s_extraPromptOffset);
            if(!populateSign(world, Helper.formatQuestionSign(question.getExtraPrompt()), promptLocation, true)){
                Log.error(String.format("Error populating extra prompt: %s", promptLocation));
            }
        }

        Log.debug(String.format("Finished setting up questionId: %s [Correct Index: %d => %d]", question.contentId, ans.orgCorrectAnswerIndex, ans.correctAnswerIndex));
    }

    @Override
    public void handle(PlayerInteractEvent event) throws IOException, InvalidBackendQueryException {
        super.handle(event);

        idInfo = new QuestionIdInfo(signText[2].trim());

        //get question location based on player
        int questionSetId = idInfo.questionSetId;
        course = Course.get(idInfo.courseId);

        if (course == null) {
            Log.logAndSend(player, String.format("Unable to find courseId from question ID: %s", idInfo.contentId));
            return;
        }

        lesson = course.getLesson(player, idInfo);
        questionSet = course.getQuestionSet(player, idInfo);

        GamePosition spawnPosWorld = getWorldSpawnPosition(signText[1]);

        GamePosition targetSignPos = spawnPosWorld.add(s_blockSignOffset);
        String[] moreInfo = getSignInfo(world, targetSignPos);

        if (moreInfo == null || moreInfo.length == 0)
            throw new RuntimeException(String.format("Unable to find additional information underneath spawn position for question: %s", idInfo.questionContentId));

        LessonProgress progress = profile.getProgress(idInfo, spawnPosWorld);

        /// Start from the last question
//        idInfo.setQuestionId(progress.currentQuestionSet, progress.currentQuestion);

        question = course.getQuestion(player, idInfo, profile.getCurrentProgress().level);

        InteractionHandler prev = this;

//        if (question == null) {
//            /// Nothing more in this question set
//            progress = progress.finishQuestionSet();
//
//            /// If the lesson is finished then don't bother setting the question up
//            if (progress.isLessonFinshed(lesson)) {
//                progress = progress.finishLesson();
//            }
//        }

        questionPos = spawnPosWorld; ///progress.location;

        GamePosition targetPosOffset = Helper.parseLocation(moreInfo[0]);
        targetPos = questionPos.add(targetPosOffset);

        Log.debug(String.format("TPQS Lesson Id: %s [Teleport: %s, Target: %s]", progress.contentId, questionPos, targetPos));

        teleportPlayerLocal(player, questionPos, prev);
    }
}
