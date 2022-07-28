package com.gms.paper.events;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerEatFoodEvent;
import com.gms.paper.interact.puzzles.maths.Arithmetic;
import org.bukkit.event.Listener;

public class NoScran implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerEat(PlayerEatFoodEvent e){
        if (Arithmetic.puzzleName != null) {
            e.setCancelled();
        }
    }

}
