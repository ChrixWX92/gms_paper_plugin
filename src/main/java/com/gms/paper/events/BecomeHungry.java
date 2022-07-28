package com.gms.paper.events;

import org.bukkit.entity.Player;
import com.gms.paper.Main;
import com.gms.paper.level.gslevels.GSLevel;
import com.gms.paper.level.gslevels.rules.GSGameRule;
import com.gms.paper.util.Log;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class BecomeHungry implements Listener {

    @EventHandler
    public void onPlayerBecomeHungry(PlayerFoodLevelChangeEvent e) {

        Player player = e.getPlayer();

        if (e.getFoodLevel() < player.getFoodData().getMaxLevel()) {

            GSLevel gsLevel = Main.getGsLevelManager().getGSLevel(player);

            if (gsLevel != null) {
                if (!gsLevel.getGSGameRules().getBoolean(GSGameRule.BECOME_HUNGRY)) {
                    player.getFoodData().setLevel(player.getFoodData().getMaxLevel());
                    player.getFoodData().setFoodSaturationLevel(player.getFoodData().getMaxLevel());
                    e.setCancelled();
                }
            }
            else {
                Log.error("Unable to get current GS level for player [BecomeHungry listener]");
            }
        }
    }
}

