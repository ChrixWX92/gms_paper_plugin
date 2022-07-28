package com.gms.paper.events;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.*;
import com.gms.paper.interact.puzzles.ResetPuzzles;
import org.bukkit.event.Listener;

public class PlayerBehaviourHandler implements Listener {

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerMoveEvent event) {
        //ResetPuzzles.resetPuzzles(event.getPlayer()); TODO: Logic to this, if it becomes needed
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) { ResetPuzzles.resetPuzzles(event.getPlayer()); }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        ResetPuzzles.resetPuzzles(event.getPlayer());
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onKick(PlayerKickEvent event) {
        ResetPuzzles.resetPuzzles(event.getPlayer());
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        ResetPuzzles.resetPuzzles(event.getEntity());
    }

}
