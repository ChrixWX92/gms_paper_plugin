package com.gms.paper.interact;

import cn.nukkit.event.player.PlayerInteractEvent;
import com.gms.paper.util.TextFormat;
import com.gms.paper.error.InvalidBackendQueryException;
import com.gms.paper.items.ItemsToSell;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;

import java.io.IOException;

public class RANDOM_InteractionHandler extends InteractionHandler {
    @Override
    public void handle(PlayerInteractEvent event) throws IOException, InvalidBackendQueryException {
        super.handle(event);

        int cost = Integer.parseInt(signText[1]);
        if (profile.tickets >= cost) {
            profile.spendTickets(cost);

            String[] IDs = signText[2].split(",");
            int random = Helper.generateRandomIntIntRange(0, (IDs.length - 1));
            int itemID = Integer.parseInt(IDs[random]);
            ItemsToSell.getItem(itemID, player);
        }
        else {
            Log.logGeneric(player, "This lucky dip costs " + cost + "tickets and you only have " + profile.tickets);
            Log.logGeneric(player, "Head through to the lessons to earn more!");
            Helper.setPlayerTitle(player, TextFormat.AQUA + "You need more tickets!\nGo through a lesson to\n earn more!");
        }
    }
}
