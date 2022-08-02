package com.gms.paper.interact.treasureHunt;

import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.level.Location;
import com.gms.paper.data.Course;
import com.gms.paper.data.GamePosition;
import com.gms.paper.data.QuestionIdInfo;
import com.gms.paper.error.InvalidBackendQueryException;
import com.gms.paper.interact.StaticWordList_InteractionHandler;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;

import java.io.IOException;
import java.util.ArrayList;

public class THTP_InteractionHandler extends StaticWordList_InteractionHandler {
    @Override
    public void handle(PlayerInteractEvent event) throws IOException, InvalidBackendQueryException {
        super.handle(event);

        GamePosition spawnPos = Helper.parseLocation(signText[1]);
        GamePosition spawnPosWorld = signLoc.add(spawnPos);

        int ycoord = -3;
        String[] info = null;
        String[] prevInfo;

        wordList.add(signText[2]);
        wordList.add(signText[3]);
        ArrayList<String[]> wordInfos = new ArrayList<>();

        do {
            var nextSignLoc = new GamePosition(blockLoc, new Location(0, ycoord, 0), false); /// buttonBlock.getLocation().add(new Location(0, -2, 0)); //info sign
            var nextSignBlock = world.getBlockEntity(nextSignLoc.round());

            if (nextSignBlock instanceof BlockEntitySign) {
                prevInfo = info;
                info = getSignInfo(world, nextSignLoc);

                if (prevInfo != null)
                    wordInfos.add(prevInfo);
            }
            else {
                prevInfo = info;
                info = null;
            }

            ycoord -= 1;
        } while (info != null);

        targetPos = null;

        for (var wordInfo : wordInfos) {
            for (int i = 0; i < wordInfo.length; i++) {
                if (!wordInfo[i].isEmpty())
                    wordList.add(wordInfo[i]);
            }
        }

        if (prevInfo != null) {
            idInfo = new QuestionIdInfo(prevInfo[1] + ".1");

            course = Course.get(idInfo.courseId);
            if (course == null) {
                Log.logAndSend(player, String.format("Unable to find courseId from question ID: %s", idInfo.contentId));
                return;
            }

            lesson = course.getLesson(player, idInfo);
            questionSet = course.getQuestionSet(player, idInfo);

            /// THTP question positions are always absolute
            targetPos = Helper.parseLocation(prevInfo[0]).asAbsolute();
        }

        isRandom = true;

        Log.debug(String.format("THTP Question %s Loaded. Num words: %d. TargetPos: %s", idInfo.questionSetId, wordList.size(), targetPos.toString()));

        /// start the question
        startQuestion();

        teleportPlayer(player, spawnPosWorld);

        this.setupQuestion(signLoc);
    }

    @Override
    public void endQuestion() {
        super.endQuestion();
    }

    @Override
    public void setupQuestion(GamePosition questionLocation) throws InvalidBackendQueryException, IOException {
        if (questionLocation == null)
            questionLocation = signLoc;

        if (wordList.size() >= 1)
            super.setupQuestion(questionLocation);
        else {
            /// mark the time here
            endQuestion();
            Helper.setPlayerTitle(player, "§2You've found all\nthe words!\nWell done");

            /// Teleport the player to the target
            teleportPlayer(player, targetPos, null);
        }
    }
}
