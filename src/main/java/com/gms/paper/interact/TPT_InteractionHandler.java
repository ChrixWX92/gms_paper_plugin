package com.gms.paper.interact;

import com.gms.paper.util.TextFormat;
import com.gms.paper.data.GamePosition;
import com.gms.paper.error.InvalidBackendQueryException;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.IOException;

public class TPT_InteractionHandler extends InteractionHandler {
    @Override
    public void handle(PlayerInteractEvent event) throws IOException, InvalidBackendQueryException {
        super.handle(event);

        int cost = Integer.parseInt(signText[1]);
        //check if player has tickets
        if (profile.tickets >= cost) {
            profile.spendTickets(cost);

            GamePosition spawnPos = Helper.parseLocation(signText[2]);
            GamePosition spawnPosWorld = signLoc.add(spawnPos);
            teleportPlayer(player, spawnPosWorld);

            Helper.setPlayerTitle(player, TextFormat.GREEN + "You have been\nteleported!");
            Log.logGeneric(player, "You now have " + profile.tickets + " tickets");
        }
        else {
            Log.logGeneric(player, TextFormat.AQUA + "Using this teleport costs " + cost + " tickets and you only have " + profile.tickets);
            Log.logGeneric(player, TextFormat.AQUA + "Head through to the lessons to earn more!");
            Helper.setPlayerTitle(player, TextFormat.AQUA + "You need more tickets!\nGo through a lesson to\n earn more!");
        }
    }
}
