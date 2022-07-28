package com.gms.paper.events;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerDropItemEvent;
import com.gms.paper.custom.items.ItemGSDye;
import com.gms.paper.custom.items.ItemReturnHome;
import org.bukkit.event.Listener;


public class NoDrop implements Listener {

    Class[] items = new Class[]{ItemReturnHome.class, ItemGSDye.class};
    private static boolean noDrop = false;

    @EventHandler(ignoreCancelled=true)
    public void onItemDrop(PlayerDropItemEvent e) {

        if (noDrop) {
            e.setCancelled(true);
            return;
        }

        for (Class item : items) {
            if (item.isInstance(e.getItem())){
                e.setCancelled(true);
                return;
            }
        }

        if(e.getItem().getName().equals("Return Home") || e.getItem().getNamedTag().getBoolean("Undroppable")) {
            e.setCancelled(true);
        }
    }

    public static  boolean isNoDrop() {
        return NoDrop.noDrop;
    }

    public static void setNoDrop(boolean noDrop) {
        NoDrop.noDrop = noDrop;
    }

}

