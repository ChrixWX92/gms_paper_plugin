package com.gms.paper.events;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerToggleGlideEvent;
import com.gms.paper.Main;
import com.gms.paper.level.gslevels.GSLevel;
import com.gms.paper.level.gslevels.rules.GSGameRule;
import org.bukkit.event.Listener;

public class ElytraEnabled implements Listener {

    @EventHandler
    public void onElytraUse(PlayerToggleGlideEvent e) {

        GSLevel gsLevel = Main.getGsLevelManager().getGSLevel(e.getPlayer());

        if (!gsLevel.getGSGameRules().getBoolean(GSGameRule.ELYTRA_ENABLED)) {
            e.setCancelled();
        }
    }
}

