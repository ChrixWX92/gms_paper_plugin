package com.gms.paper.interact.treasureHunt;

import cn.nukkit.event.player.PlayerInteractEvent;
import com.gms.paper.error.InvalidBackendQueryException;
import com.gms.paper.interact.StaticWordList_InteractionHandler;
import com.gms.paper.util.Helper;

import java.io.IOException;

public class THA_InteractionHandler extends StaticWordList_InteractionHandler {
    @Override
    public void handle(PlayerInteractEvent event) throws IOException, InvalidBackendQueryException {
        super.handle(event);

        String answer = signText[1].trim().toUpperCase();
        THTP_InteractionHandler questionSetup = (THTP_InteractionHandler) s_questionSetup;

        this.copyFrom(questionSetup);

        if (answer.equals(chosenWord.trim().toUpperCase())) {
            Helper.setPlayerTitle(player, "Well done!");

            if (questionSetup.wordList.size() >= 1) {
                /// Fixed reward for THA questions
                profile.earnTickets(10);

                questionSetup.setupQuestion(null);
                Helper.setPlayerTitle(player, "Well done!", "Next, find "  + questionSetup.chosenWord, 10, 100, 20);
            }
            else {
                questionSetup.setupQuestion(null);
            }
        }
        else {
            player.sendMessage("Not quite, you're looking for " + questionSetup.chosenWord);
            player.sendMessage("and this is " + answer);
        }
    }
}
