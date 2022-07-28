package com.gms.paper.events;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.Listener;

import java.util.UUID;

public class ResourcePackHandling implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        //byte[] bytes = HexFormat.of().parseHex("9e7c275034cd49dbb1d067d118e88c16");
        byte[] bytes = hexStringToByteArray("9e7c275034cd49dbb1d067d118e88c16");
        UUID uuid = UUID.nameUUIDFromBytes(bytes);
        event.getPlayer().getServer().getResourcePackManager().getPackById(uuid);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        int dataLen = (len - 1) / 2;

        byte[] data = new byte[dataLen];
        for (int i = 0; i < dataLen; i++) {
            int j = i * 2 + 1;
            char c = s.charAt(j);
            data[i] = (byte) ((Character.digit(c, 16) << 4) + Character.digit(c, 16));
        }
        return data;
    }

}
