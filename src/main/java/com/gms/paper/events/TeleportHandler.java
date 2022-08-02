package com.gms.paper.events;

import com.gms.paper.util.Vector3D;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import com.gms.paper.Main;
import com.gms.paper.data.User;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TeleportHandler implements Listener {
    static World s_current;

    public TeleportHandler() {
    }

    private Main getPlugin() {
        return Main.s_plugin;
    }

    public static void unloadCurrentLevel() {
        if (s_current != null) {
            try {
                Log.debug(String.format("Unloading level: %s [Dir: %s]", s_current.getName(), s_current.getWorldFolder().getName()));
                Bukkit.getServer().unloadWorld(s_current, false);
            }
            catch (Exception e) {
                Log.exception(e, "Unable to unload current level. Ignoring ...");
            }
            finally {
                s_current = null;
            }
        }
    }

    // @EventHandler TODO: Why is this annotation here? Invalid method signature to be using this
    public void tpWorld(Player player, String worldName, Vector3D spawnCoordinates, float headYaw, String worldId) {
        try {

            if (!this.getPlugin().getServer().isLevelLoaded(worldName)) {
                boolean didLoad = this.getPlugin().getServer().loadLevel(worldName);
                if (!didLoad)
                    Log.error(String.format("Unable to load world: %s", worldName));
            }

            Log.debug(String.format("Loading world: %s", worldName));
            World world = this.getPlugin().getServer().getWorldByName(worldName);

            if (world == null)
                return;

            Location spawn = new Location(world, spawnCoordinates.x, spawnCoordinates.y, spawnCoordinates.z);
            World prevLevel = s_current;

            s_current = world;

            boolean didLoad = false;

            player.teleport(world.getSafeSpawn());

            if (world.getFolderName().equals(Helper.s_mainWorld)) {
                Vector3D v = Helper.s_mainLobbySpawnPos;
                spawn = new Location(world, v.x, v.y, v.z);
            }

            Log.debug(String.format("Teleport to world: %s @ (%d, %d, %d)", worldName, (int)spawn.getX(), (int)spawn.getY(), (int)spawn.getZ()));
            player.teleport(spawn);

            //player.headYaw = headYaw;
            //TODO: Unsure why this headyaw value is being change, but accomplishing the desired effect with packet send:
            EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
            PlayerConnection connection = entityPlayer.b;
            connection.a(new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) (headYaw)));
            // Hoping the above method is the correct one

            didLoad = true;

            if (User.getCurrent() != null && User.getCurrent().getState() != null) {
                User.getCurrent().getState().updateWorld(worldId);
                User.getCurrent().getState().updatePos(new Vector3D(spawn.getX(), spawn.getY(), spawn.getZ()), 0);
            }

            if (!didLoad) {
                Log.logAndSend(player, String.format("Unable to load world: %s", worldName));
            }
            else {
                /// TODO: later
//                if (prevLevel != null) {
//                    try {
//                        Log.debug(String.format("Unloading world: %s [Dir: %s]", prevLevel.getName(), prevLevel.getFolderName()));
//                        this.getPlugin().getServer().unloadLevel(prevLevel);
//                    }
//                    catch (Exception e) {
//                        Log.exception(e, String.format("Error unloading world: %s [Dir: %s]. Ignoring ...", prevLevel.getName(), prevLevel.getFolderName()));
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