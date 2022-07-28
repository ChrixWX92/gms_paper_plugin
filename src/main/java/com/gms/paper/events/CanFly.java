package com.gms.paper.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class CanFly implements Listener {

    @EventHandler
    public void onLevelChange(PlayerTeleportEvent e){

//        if (e.getTo().getWorld() != e.getFrom().getWorld()) {
//
//            Player player = e.getPlayer();
//
//            GSLevel gsLevel = Main.getGsLevelManager().getGSLevel(player);
//
//            if (gsLevel == null)
//                return;
//
//            player.getAdventureSettings().set(AdventureSettings.Type.FLYING, gsLevel.getGSGameRules().getBoolean(GSGameRule.CAN_FLY));
//        }
    }
}
