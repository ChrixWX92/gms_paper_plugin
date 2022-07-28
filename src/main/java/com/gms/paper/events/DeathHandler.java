package com.gms.paper.events;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerDeathEvent;
import com.gms.paper.Main;
import com.gms.paper.level.gslevels.GSLevel;
import com.gms.paper.level.gslevels.rules.GSGameRule;
import com.gms.paper.util.Helper;
import org.bukkit.event.Listener;

public class DeathHandler implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        e.setCancelled();

        GSLevel gsLevel = Main.getGsLevelManager().getGSLevel(e.getEntity());

        if (gsLevel.getGSGameRules().getBoolean(GSGameRule.HANDLE_DEATH)) {
            Helper.teleportToLobby(e.getEntity());
        }
    }
}

