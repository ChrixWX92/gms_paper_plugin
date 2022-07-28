package com.gms.paper.events;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryClickEvent;
import com.gms.paper.Main;
import com.gms.paper.level.gslevels.GSLevel;
import com.gms.paper.level.gslevels.rules.GSGameRule;
import org.bukkit.event.Listener;

public class MoveRH implements Listener {

    @EventHandler
    public void onItemMove(InventoryClickEvent e) {

        if (e.getSourceItem().getName().equals("Return Home")) {
            //for (Player player : e.getInventory().getViewers()) {
                GSLevel gsLevel = Main.getGsLevelManager().getGSLevel(e.getPlayer());
                if (!gsLevel.getGSGameRules().getBoolean(GSGameRule.MOVE_RH)) {
                    e.setCancelled();
                }
            //}
        }
    }
}

