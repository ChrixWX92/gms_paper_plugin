package com.gms.paper.interact;

import cn.nukkit.event.player.PlayerInteractEvent;
import com.gms.paper.error.InvalidBackendQueryException;
import com.gms.paper.util.Helper;

import java.io.IOException;

public class TITLE_InteractionHandler extends InteractionHandler {
    @Override
    public void handle(PlayerInteractEvent event) throws IOException, InvalidBackendQueryException {
        super.handle(event);

        String word = signText[1];

        if (word.charAt(0) == '¦') {
            word = word.substring(1) + signText[2] + signText[3];
            Helper.setPlayerTitle(player, word);
        }
        else {
            Helper.setPlayerTitle(player, "The word is...\n" + word);
        }

    }

}
