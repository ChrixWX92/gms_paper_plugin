package com.gms.paper.events;

import org.bukkit.entity.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import com.gms.paper.Main;
import com.gms.paper.data.User;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;

public class TeleportHandler implements Listener {
    static Level s_current;

    public TeleportHandler() {
    }

    private Main getPlugin() {
        return Main.s_plugin;
    }

    public static void unloadCurrentLevel() {
        if (s_current != null) {
            try {
                Log.debug(String.format("Unloading level: %s [Dir: %s]", s_current.getName(), s_current.getFolderName()));
                s_current.unload();
            }
            catch (Exception e) {
                Log.exception(e, "Unable to unload current level. Ignoring ...");
            }
            finally {
                s_current = null;
            }
        }
    }

    @EventHandler
    public void tpWorld(Player p, String worldName, Location spawn, float headYaw, String worldId) {
        try {
            Player player = p;

            if (!this.getPlugin().getServer().isLevelLoaded(worldName)) {
                boolean didLoad = this.getPlugin().getServer().loadLevel(worldName);
                if (!didLoad)
                    Log.error(String.format("Unable to load world: %s", worldName));
            }

            Log.debug(String.format("Loading world: %s", worldName));
            Level level = this.getPlugin().getServer().getWorldByName(worldName);

            if (level == null)
                return;

            var prevLevel = s_current;

            s_current = level;

            boolean didLoad = false;

            player.teleport(level.getSafeSpawn());

            if (level.getFolderName().equals(Helper.s_mainWorld)) {
                Vector3D v = Helper.s_mainLobbySpawnPos;
                spawn = new Location(v.x, v.y, v.z);
            }

            Log.debug(String.format("Teleport to world: %s @ (%d, %d, %d)", worldName, (int)spawn.x, (int)spawn.y, (int)spawn.z));
            player.teleport(spawn);
            player.headYaw = headYaw;

            didLoad = true;

            if (User.getCurrent() != null && User.getCurrent().getState() != null) {
                User.getCurrent().getState().updateWorld(worldId);
                User.getCurrent().getState().updatePos(new Vector3(spawn.x, spawn.y, spawn.z), 0);
            }

            if (!didLoad) {
                Log.logAndSend(player, String.format("Unable to load world: %s", worldName));
            }
            else {
                /// TODO: later
//                if (prevLevel != null) {
//                    try {
//                        Log.debug(String.format("Unloading level: %s [Dir: %s]", prevLevel.getName(), prevLevel.getFolderName()));
//                        this.getPlugin().getServer().unloadLevel(prevLevel);
//                    }
//                    catch (Exception e) {
//                        Log.exception(e, String.format("Error unloading level: %s [Dir: %s]. Ignoring ...", prevLevel.getName(), prevLevel.getFolderName()));
//                    }
//                }
            }
        }
        catch (Exception e) {
            Log.exception(e, String.format("Error loading world: %s", worldName));
        }
    }

//    @EventHandler
//    public void tpWorld(Player p, String worldName, Location spawn) {
//        tpWorld(p, worldName, spawn, 0, worldName);
//    }
}