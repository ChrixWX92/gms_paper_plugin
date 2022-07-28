package com.gms.paper.interact;

import cn.nukkit.event.player.PlayerInteractEvent;
import com.gms.paper.util.TextFormat;
import com.gms.paper.error.InvalidBackendQueryException;
import com.gms.paper.items.ItemsToSell;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;

import java.io.IOException;

public class SHOP_InteractionHandler extends InteractionHandler {
    @Override
    public void handle(PlayerInteractEvent event) throws IOException, InvalidBackendQueryException {
        super.handle(event);

        int cost = Integer.parseInt(signText[1]);

        /// check if player has tickets
        if (profile.tickets >= cost) {
            profile.spendTickets(cost);
            int itemID = Integer.parseInt(signText[2]);
            ItemsToSell.getItem(itemID, player);

            Helper.setPlayerTitle(player, TextFormat.GREEN + "Thanks for shopping,\ncome again soon!");
            profile.showTicketsStatus(player, String.format("You now have %d tickets left.", profile.tickets));
        }
        else {
            Log.logGeneric(player, "This item costs " + cost + "tickets and you only have " + profile.tickets);
            Log.logGeneric(player, "Head through to the lessons to earn more!");
            Helper.setPlayerTitle(player, TextFormat.AQUA + "You need more tickets!\nGo through a lesson to\n earn more!");
        }
    }
}
