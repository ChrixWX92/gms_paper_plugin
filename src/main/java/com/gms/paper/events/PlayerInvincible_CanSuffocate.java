package com.gms.paper.events;

import org.bukkit.entity.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageEvent;
import com.gms.paper.Main;
import com.gms.paper.level.gslevels.GSLevel;
import com.gms.paper.level.gslevels.rules.GSGameRule;
import org.bukkit.event.Listener;

public class PlayerInvincible_CanSuffocate implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent e) {

        if (e.getEntity() instanceof Player player) {

            GSLevel gsLevel = Main.getGsLevelManager().getGSLevel(player);

            if (gsLevel.getGSGameRules().getBoolean(GSGameRule.PLAYER_INVINCIBLE)) {
                e.setCancelled();
            } else if (!gsLevel.getGSGameRules().getBoolean(GSGameRule.CAN_SUFFOCATE) && e.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
                e.setCancelled();
            }
        }
    }
}

