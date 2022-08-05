package com.gms.paper.events;

import com.gms.paper.interact.puzzles.ResetPuzzles;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerBehaviourHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
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
