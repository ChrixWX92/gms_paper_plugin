package com.gms.paper.events;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockFenceGate;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import com.gms.paper.custom.items.ItemGSDye;
import com.gms.paper.util.Log;
import org.bukkit.event.Listener;

public class GSDyeListener implements Listener {

    public GSDyeListener() {
    }

    @EventHandler (ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {

        try {
            Item clickedItem = event.getItem();
            Block block = event.getBlock();
            if (clickedItem instanceof ItemGSDye  && !(block instanceof BlockFenceGate)) {event.setCancelled();}
        }
        catch (Exception e) {
            Log.exception(e, "Exception occurred in GSDyeListener!");
        }
    }
}


