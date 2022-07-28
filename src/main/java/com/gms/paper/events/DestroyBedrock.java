package com.gms.paper.events;

import cn.nukkit.block.BlockBedrock;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import com.gms.paper.Main;
import com.gms.paper.level.gslevels.GSLevel;
import com.gms.paper.level.gslevels.rules.GSGameRule;
import org.bukkit.event.Listener;

public class DestroyBedrock implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        if (e.getBlock() instanceof BlockBedrock) {

            GSLevel gsLevel = Main.getGsLevelManager().getGSLevel(e.getPlayer());

            if (!gsLevel.getGSGameRules().getBoolean(GSGameRule.DESTROY_BEDROCK)) {
                e.setCancelled();
            }
        }
    }
}

