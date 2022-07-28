package com.gms.paper.events;

import cn.nukkit.block.BlockAir;
import cn.nukkit.block.BlockNetherPortal;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockUpdateEvent;
import com.gms.paper.Main;
import com.gms.paper.level.gslevels.GSLevel;
import com.gms.paper.level.gslevels.rules.GSGameRule;
import org.bukkit.event.Listener;

public class BuildNetherPortal implements Listener {

    @EventHandler
    public void onPortalBuild(BlockUpdateEvent e) {

        if (e.getBlock()instanceof BlockNetherPortal) {
            GSLevel gsLevel = Main.getGsLevelManager().getGSLevel(e.getBlock().level);

            if (!gsLevel.getGSGameRules().getBoolean(GSGameRule.BUILD_NETHER_PORTAL)) {
                e.getBlock().getWorld().setBlock(e.getBlock(),new BlockAir());
                e.setCancelled();
            }
        }
    }
}

