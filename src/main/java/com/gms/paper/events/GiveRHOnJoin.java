package com.gms.paper.events;

import org.bukkit.entity.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import com.gms.paper.custom.items.ItemReturnHome;
import org.bukkit.event.Listener;

public class GiveRHOnJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {

        Player p = e.getPlayer();
        ItemReturnHome rh = new ItemReturnHome();

        if (!p.hasPlayedBefore() || !p.getInventory().contains(rh)) {
            p.getInventory().setItem(8, rh);
        }
    }

}




