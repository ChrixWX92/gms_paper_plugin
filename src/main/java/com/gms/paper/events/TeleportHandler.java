package com.gms.paper.events;

import com.gms.paper.util.Vector3D;
import com.gms.paper.util.world.GSWorld;
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

    public void tpWorld(Player player, String worldName, Vector3D spawnCoordinates, float headYaw, String worldId) {
        try {

            Log.debug(String.format("Loading world: %s", worldName));
            GSWorld world = GSWorld.loadWorld(worldName);

            if (world == null || this.getPlugin().getServer().getWorld(worldName) == null || !world.isLoaded()) {
                Log.error(String.format("Unable to load world: %s", worldName));
            }

            if (world == null)
                return;

            Location spawn = new Location(world.bukkitWorld, spawnCoordinates.x, spawnCoordinates.y, spawnCoordinates.z);

            World prevLevel = s_current;

            s_current = world.bukkitWorld;

            GSWorld gsWorld = new GSWorld(s_current);

            Vector3D safeSpawn = gsWorld.getSafeSpawn();

            player.teleport(new Location(s_current, safeSpawn.getX(), safeSpawn.getY(), safeSpawn.getZ()));

            if (gsWorld.getFolder().getName().equals(Helper.s_mainWorld)) {
                Vector3D v = Helper.s_mainLobbySpawnPos;
                spawn = new Location(world.bukkitWorld, v.x, v.y, v.z);
            }

            Log.debug(String.format("Teleport to world: %s @ (%d, %d, %d)", worldName, (int)spawn.getX(), (int)spawn.getY(), (int)spawn.getZ()));
            player.teleport(spawn);

            //player.headYaw = headYaw;
            //Unsure why this headYaw value is being changed, but accomplishing the desired effect with packet send:
            EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
            PlayerConnection connection = entityPlayer.b;
            connection.a(new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) (headYaw)));

            if (User.getCurrent() != null && User.getCurrent().getState() != null) {
                User.getCurrent().getState().updateWorld(worldId);
                User.getCurrent().getState().updatePos(new Vector3D(spawn.getX(), spawn.getY(), spawn.getZ()), 0);
            }

        }
        catch (Exception e) {
            Log.exception(e, String.format("Error loading world: %s", worldName));
        }
    }

}