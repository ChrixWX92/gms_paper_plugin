package com.gms.paper.events;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageEvent;
import com.gms.paper.interact.puzzles.maths.Arithmetic;
import org.bukkit.event.Listener;

public class NegateFallDamage implements Listener {

    @EventHandler (ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent e){
        //Player player = (Player) e.getEntity();
        if(e.getCause() == EntityDamageEvent.DamageCause.FALL){
            if (Arithmetic.puzzleName != null) {
                if (Arithmetic.puzzleName.startsWith("Freefall")) {
                    e.setCancelled(true);
                }
            }
        }

    }
}

