package com.gms.paper.custom.items;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryClickEvent;

public class StayInInventory implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getHeldItem().getName().equals("Return Home")) {
            e.setCancelled(true);
        }
    }

}




