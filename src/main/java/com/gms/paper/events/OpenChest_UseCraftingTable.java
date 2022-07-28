package com.gms.paper.events;

import org.bukkit.entity.Player;
import cn.nukkit.block.BlockAnvil;
import cn.nukkit.block.BlockChest;
import cn.nukkit.block.BlockCraftingTable;
import cn.nukkit.block.BlockEnderChest;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import com.gms.paper.Main;
import com.gms.paper.level.gslevels.GSLevel;
import com.gms.paper.level.gslevels.rules.GSGameRule;
import org.bukkit.event.Listener;

public class OpenChest_UseCraftingTable implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {

        Player player = e.getPlayer();

        if (e.getBlock() instanceof BlockChest || e.getBlock() instanceof BlockEnderChest) {//TODO: Entity?

            GSLevel gsLevel = Main.getGsLevelManager().getGSLevel(player);

            if (!gsLevel.getGSGameRules().getBoolean(GSGameRule.OPEN_CHEST)) {
                e.setCancelled();
            }
        } else if (e.getBlock() instanceof BlockCraftingTable) {//TODO: Entity?

            GSLevel gsLevel = Main.getGsLevelManager().getGSLevel(player);;

            if (!gsLevel.getGSGameRules().getBoolean(GSGameRule.USE_CRAFTING_TABLE)) {
                e.setCancelled();
            }
        } else if (e.getBlock() instanceof BlockAnvil) {//TODO: Entity?

            GSLevel gsLevel = Main.getGsLevelManager().getGSLevel(player);;

            if (!gsLevel.getGSGameRules().getBoolean(GSGameRule.USE_ANVIL)) {
                e.setCancelled();
            }
        }
    }
}

