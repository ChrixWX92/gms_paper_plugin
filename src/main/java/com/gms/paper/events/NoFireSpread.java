package com.gms.paper.events;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.Listener;

import static cn.nukkit.event.block.BlockIgniteEvent.BlockIgniteCause.SPREAD;

public class NoFireSpread implements Listener {

    @EventHandler
    public void onFireSpread(BlockIgniteEvent e) {
        if (e.getCause() == SPREAD) {
            e.setCancelled();
        }
    }
}


