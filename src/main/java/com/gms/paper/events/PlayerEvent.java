package com.gms.paper.events;

import com.gms.paper.util.Vector3D;
import org.bukkit.entity.Player;
import com.gms.paper.Main;
import com.gms.paper.PlayerInstance;
import com.gms.paper.data.ChildProfile;
import com.gms.paper.data.User;
import com.gms.paper.data.UserState;
import com.gms.paper.interact.InteractionHandler;
import com.gms.paper.level.GameMode;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class PlayerEvent implements Listener {
    @EventHandler
    public void onDataPk(DataPacketReceiveEvent event) throws Exception {
        try {
            Player player = event.getPlayer();

            /// check the user matches
            if (event.getPacket() instanceof LoginPacket) {
                /// NO-OP
            }
            else if (event.getPacket() instanceof SetLocalPlayerAsInitializedPacket) {
                handlePlayerConnected(event, player);
                handlePlayerJoined(event, player);
            }
            else if (event.getPacket() instanceof MovePlayerPacket)
                handlePlayerMove(event, (MovePlayerPacket)event.getPacket(), player);

        }
        catch (Exception e) {
            Log.exception(e, "PlayerEvent: Unhandled exception!");
        }
    }

    void handleLevelLoad() {
    }

    private void handleDisconnect(boolean kicked) {
        if (Main.s_mcServer != null)
            Main.s_mcServer.playerDisconnected(kicked);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Log.debug("PlayerQuitEvent!");
        handleDisconnect(false);
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        Log.debug("PlayerKickEvent!");
        handleDisconnect(true);
    }

    void handlePlayerMove(DataPacketReceiveEvent event, MovePlayerPacket packet, Player player) {
        Main.s_mcServer.updateMove(packet);

        if (InteractionHandler.isPendingTeleport() && packet.onGround) {
            InteractionHandler.getPendingTeleport().postTeleport();
        }
    }

    void clearCache() {
        /// Clear the GS cache
        Main.getGsLevelManager().clearCache();
        InteractionHandler.reset();

//        if (!Helper.isContentDev()) {
//            String worldDir = Helper.getNukkitWorldBaseDir().toString();
//            Log.warn(String.format("Deleting: %s", worldDir));
//            Helper.deleteDirectory(new File(worldDir));
//        }
    }

    void handlePlayerJoined(DataPacketReceiveEvent event, Player player) {
        /// If there is no active player, then don't bother doing anything
        /// and exit early
        if (User.getCurrent() == null)
            return;

        long playerId = player.getId();
        Log.debug(String.format("Player ID: %d", playerId));
        Log.debug(String.format("Player joined: %s [Tag = %s]", player.getDisplayName(), player.getNameTag()));

        PlayerInstance pis = new PlayerInstance(player, User.getCurrent());
        PlayerInstance.addPlayer(pis, true);

        Log.debug("Setting game mode to adventure mode!");

        if (Helper.isContentDev())
            player.setGameMode(GameMode.S_DEV.bukkitGameMode);
        else
            player.setGameMode(GameMode.S_ADVENTURE.bukkitGameMode);

        if (User.getCurrent() != null && User.getCurrent().getState() != null) {
            UserState state = User.getCurrent().getState();
            Vector3D pos = state.getPos();
            float yaw = state.headYaw;

            Helper.teleportToWorld(player, state.getWorldId(), pos, yaw);

            state.updatePos(pos, yaw);
        }
    }

    void handlePlayerLogin(DataPacketReceiveEvent event, Player player) {
    }

    void handlePlayerConnected(DataPacketReceiveEvent event, Player player) {
        /// If it's a prod server and someone is already logged in
        if (Helper.isProd() && User.getCurrent() != null) {
            Log.logAndSend(player, "Someone is already on this server!");
            player.kick(PlayerKickEvent.Reason.NOT_WHITELISTED);
            event.setCancelled();

            return;
        }

        /// check the user matches
        String minecraftId = player.getName();

        clearCache();

        try {
            Main.s_mcServer.playerConnected(minecraftId);

            /// By this time, we should have a current user
            if (User.getCurrent() == null) {
                Log.logAndSend(player, String.format("Invalid join token for player: %s. Kicking ...", player.getName()));
                player.kick(PlayerKickEvent.Reason.INVALID_PVE);
                event.setCancelled();

                return;
            }

            if (!Helper.isDev()) {
                ChildProfile profile = User.getCurrent().getProfile();

//                if (!profile.isMinecraftPlayer(minecraftId)) {
//                    Log.logAndSend(player, String.format("You're not allowed on this server as you're not the owner [Owner = %s, Joiner: %s]", profile.minecraftId, player.getName()));
//                    event.setCancelled();
//                    return;
//                }

                Log.debug(String.format("Allowing user to join in: %s [Owner: %s]", player.getName(), profile.minecraftId));
            }
            else {
                Log.debug(String.format("DEV server allowing any user to join in: %s", player.getName()));
            }

//            if (!Main.s_mcServer.canJoin(minecraftId)) {
//                Log.logAndSend(player, String.format("You're not allowed on this server as you're not the owner [Owner = %s, Joiner: %s]",
//                        Main.s_mcServer.getServerInfo().minecraftId_Target, minecraftId));
//                player.kick(PlayerKickEvent.Reason.NOT_WHITELISTED);
//                event.setCancelled();
//            }
        }
        catch (Exception e) {
            Log.exception(e, "Unable to finalise the connection to the Minecraft server!");
            player.kick(PlayerKickEvent.Reason.NOT_WHITELISTED);
            event.setCancelled();
            handleDisconnect(false);
        }
    }
}
