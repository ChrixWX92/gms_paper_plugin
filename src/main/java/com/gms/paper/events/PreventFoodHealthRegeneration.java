package com.gms.paper.events;

import org.bukkit.entity.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityRegainHealthEvent;
import com.gms.paper.Main;
import com.gms.paper.level.gslevels.GSLevel;
import com.gms.paper.level.gslevels.rules.GSGameRule;
import org.bukkit.event.Listener;

public class PreventFoodHealthRegeneration implements Listener {

    @EventHandler
    public void onPlayerGainHealth(EntityRegainHealthEvent e) {

        if (e.getEntity() instanceof Player player && e.getRegainReason() == 1) {

            GSLevel gsLevel = Main.getGsLevelManager().getGSLevel(player);

            if (!gsLevel.getGSGameRules().getBoolean(GSGameRule.BECOME_HUNGRY)) {
                e.setCancelled();
            }
        }
    }
}

