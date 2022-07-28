package com.gms.paper.interact;

import cn.nukkit.event.player.PlayerInteractEvent;
import com.gms.paper.data.GamePosition;
import com.gms.paper.error.InvalidBackendQueryException;
import com.gms.paper.util.Helper;

import java.io.IOException;

public class TP_InteractionHandler extends InteractionHandler {
    @Override
    public void handle(PlayerInteractEvent event) throws IOException, InvalidBackendQueryException {
        super.handle(event);

        GamePosition spawnPos = Helper.parseLocation(signText[1]);
        GamePosition spawnPosWorld = signLoc.add(spawnPos);
        teleportPlayer(player, spawnPosWorld);
    }
}
