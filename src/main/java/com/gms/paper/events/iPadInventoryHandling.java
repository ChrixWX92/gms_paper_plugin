package com.gms.paper.events;

import org.bukkit.entity.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerItemHeldEvent;
import com.gms.paper.custom.items.ItemReturnHome;
import org.bukkit.event.Listener;

public class iPadInventoryHandling implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerItemHeldEvent event) {
        if (event.getItem() instanceof ItemReturnHome) {
            Player player = event.getPlayer();
            if (player.getLoginChainData().getDeviceOS() == 2) {
                event.setCancelled();
                new RHListener().showDlg(event.getPlayer());
                player.getInventory().setHeldItemSlot(0);
            }
        }
    }


}
