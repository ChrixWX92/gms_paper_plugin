package com.gms.paper.events;

import org.bukkit.entity.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerMoveEvent;
import com.gms.paper.PlayerInstance;
import com.gms.paper.data.ChildProfile;
import org.bukkit.event.Listener;

public class TicketsHUD implements Listener {
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerInstance p = PlayerInstance.getPlayer(player.getName());

        if (p != null) {
            ChildProfile profile = p.getProfile();

            if (profile != null) {
                String msg = ("§eT§5I§dC§cK§bE§aT§9S §7= §f" + profile.tickets);
                player.sendActionBar(msg);
            }
        }
    }
}
