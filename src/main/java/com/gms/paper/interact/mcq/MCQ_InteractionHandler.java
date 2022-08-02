package com.gms.paper.interact.mcq;

import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import com.gms.paper.data.Course;
import com.gms.paper.data.GamePosition;
import com.gms.paper.data.LessonProgress;
import com.gms.paper.data.QuestionIdInfo;
import com.gms.paper.error.InvalidBackendQueryException;
import com.gms.paper.interact.InteractionHandler;
import com.gms.paper.interact.tpqs.TPQS_Answer;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import com.gms.paper.util.Scanner;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MCQ_InteractionHandler  extends InteractionHandler{

    private Scanner scanner;
    private MCQ_PresentationHandler presentationHandler;

    @Override
    public void setupQuestion(GamePosition qpos) throws InvalidBackendQueryException, IOException {
        super.setupQuestion(qpos);

        LessonProgress progress = profile.getCurrentProgress();
        idInfo.setQuestionId(progress.currentQuestionSet(), progress.currentQuestion());
        questionSet = course.getQuestionSet(player, idInfo);
        question = course.getQuestion(player, idInfo, profile.getCurrentProgress().level);

        if (question == null)
            throw new RuntimeException(String.format("Unable to find question: %s [Level Progress: %d]", idInfo.questionContentId, profile.getCurrentProgress().level));

        TPQS_Answer sortedAnswer = question.getTPQSAnswer();
        answer = sortedAnswer;

        //Log.debug(String.format("MCQ Setup Question called"));
        scanner = new Scanner();
        List<Location> signLocations = scanner.Scan(player, BlockEntitySign.class, -20, 20, -4,10,-20, 20); //these bounds are set for the test level. May need tweaking.
        List<Location> lessonSignLocations =  filterLessonSigns(idInfo.questionSetContentId, signLocations);
        
        presentationHandler.setupQuestion(MCQ_PresentationHandler.PresentationType.SCATTER, lessonSignLocations, question);

    }

    //only return signs with the question set id
    public List<Location> filterLessonSigns(String lessonId, List<Location> signLocations) {
        List<Location> lessonSignLocations = new ArrayList<Location>();

        for(Location signLocation : signLocations){
            BlockEntitySign sign = (BlockEntitySign) world.getBlockEntity(signLocation);
            String[] signText = getSignInfo(sign);
            if(signText != null && signText.length > 1 && signText[1].length() > 1) {
                String signQuestionSetId = signText[1].substring(1);
                if (signQuestionSetId.equals(lessonId)) {
                    lessonSignLocations.add(signLocation);
                    //Log.debug(String.format("Lesson Signs: %s [Location: %s]", signQuestionSetId, signLocation));
                }
            }
        }

        return lessonSignLocations;
    }

    @Override
    public void handle(PlayerInteractEvent event) throws IOException, InvalidBackendQueryException {
        super.handle(event);

        presentationHandler = new MCQ_PresentationHandler(player,this);
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

        question = course.getQuestion(player, idInfo, profile.getCurrentProgress().level);

        InteractionHandler prev = this;

        questionPos = spawnPosWorld; ///progress.location;

        GamePosition targetPosOffset = Helper.parseLocation(moreInfo[0]);
        targetPos = questionPos.add(targetPosOffset);

        Log.debug(String.format("TPQS Lesson Id: %s [Teleport: %s, Target: %s]", progress.contentId, questionPos, targetPos));

        teleportPlayer(player, questionPos, prev);
    }

    @Override
    public void resetHandlerState(World world) {
        if(presentationHandler != null)
            presentationHandler.resetPresentationState(world);
    }
}
