package com.gms.paper.interact;

import cn.nukkit.block.BlockAir;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.math.Vector3;
import com.gms.paper.util.TextFormat;
import com.gms.paper.data.GamePosition;
import com.gms.paper.error.InvalidBackendQueryException;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;

import java.io.IOException;

public class OPEN_InteractionHandler extends InteractionHandler {
    @Override
    public void handle(PlayerInteractEvent event) throws IOException, InvalidBackendQueryException {
        super.handle(event);

        int cost = Integer.parseInt(signText[1]);
        if (profile.tickets >= cost) {
            profile.tickets -= cost;
            String[] startCoOrds = signText[2].split(",");
            String[] endCoOrds = signText[3].split(",");
            int startX = Integer.parseInt(startCoOrds[0]);
            int startY = Integer.parseInt(startCoOrds[1]);
            int startZ = Integer.parseInt(startCoOrds[2]);
            int endX = Integer.parseInt(endCoOrds[0]);
            int endY = Integer.parseInt(endCoOrds[1]);
            int endZ = Integer.parseInt(endCoOrds[2]);

            GamePosition startLoc = new GamePosition(signLoc, new Vector3(startX, startY, startZ));
            GamePosition endLoc = new GamePosition(signLoc, new Vector3(endX, endY, endZ));
            level.setBlock(startLoc, new BlockAir());
            level.setBlock(endLoc, new BlockAir());
        }
        else {
            Log.logGeneric(player, "This item costs " + cost + "profile.tickets and you only have " + profile.tickets);
            Log.logGeneric(player, "Head through to the lessons to earn more!");
            Helper.setPlayerTitle(player, TextFormat.AQUA + "You need more profile.tickets!\nGo through a lesson to\n earn more!");
        }
    }
}
