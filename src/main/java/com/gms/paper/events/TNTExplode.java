package com.gms.paper.events;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityExplosionPrimeEvent;
import cn.nukkit.level.GameRule;
import com.gms.paper.Main;
import com.gms.paper.level.gslevels.GSLevel;
import org.bukkit.event.Listener;

public class TNTExplode implements Listener {

    @EventHandler
    public void onIgniteTNT(EntityExplosionPrimeEvent e) {

        GSLevel gsLevel = Main.getGsLevelManager().getGSLevel(e.getEntity().getWorld());

        if (!gsLevel.getGameRules().getBoolean(GameRule.TNT_EXPLODES)) {
            e.setCancelled();
        }
    }
}


