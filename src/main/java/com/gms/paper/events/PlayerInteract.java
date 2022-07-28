package com.gms.paper.events;


import org.bukkit.entity.Player;
import cn.nukkit.block.*;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import com.gms.paper.data.GamePosition;
import com.gms.paper.interact.InteractionHandler;
import com.gms.paper.util.Log;
import org.bukkit.event.Listener;

public class PlayerInteract implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Log.debug("PlayerInteract::onInteract! handler");
        try {
            PlayerInteractEvent.Action action = event.getAction();

            if (action == null) {
                Log.debug("No action associated with PlayerInteractEvent!");
                return;
            }

            Player player = event.getPlayer();
            Block buttonBlock = event.getBlock();
            GamePosition b_loc = new GamePosition(null, buttonBlock.getLocation());
            GamePosition signLoc = b_loc.add(new Vector3(0, -2, 0)); //info sign
            Level level = buttonBlock.getLocation().level;

            BlockEntity signBlockEntity = level.getBlockEntity(signLoc);

            int[] xz_range = { 0, -1, 1 };

            for (int xi = 0; xi < 3 && !(signBlockEntity instanceof BlockEntitySign); xi++) {
                int dx = xz_range[xi];
                for (int zi = 0; zi < 3 && !(signBlockEntity instanceof BlockEntitySign); zi++) {
                    int dz = xz_range[zi];
                    for (int dy = -3; dy >= -5 && !(signBlockEntity instanceof BlockEntitySign); dy--) {
                        Log.warn(String.format("No signBlockEntity for sign loc: %s [dx: %d, dy = %d, dz = %d]", signLoc.toString(), dx, dy, dz));
                        signLoc = b_loc.add(new Vector3(dx, dy, dz)); //info sign
                        signBlockEntity = level.getBlockEntity(signLoc);
                    }
                }
            }

            if (signBlockEntity == null) {
                Log.error(String.format("No signBlockEntity found at all for b_loc: %s. Changing game mode to creative for dev servers ...", b_loc.toString()));
//                Helper.setDebugDevMode(player);
//                return;
            }

            String buttonType = "";

            if ((action.equals(PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)) &&
                    ((buttonBlock.getName().equals(InteractionHandler.s_stoneButtonName)) || (buttonBlock.getName().equals(InteractionHandler.s_woodenButtonName))) &&
                    (signBlockEntity.getBlock().getId() == 68 || signBlockEntity.getBlock().getId() == 63)) {

                //Checks if the button has a sign underneath it (where activity info is stored)
                String[] signText = InteractionHandler.getSignInfo(level, signLoc);
                if (signText != null && signText.length > 0) {
                    buttonType = signText[0].split(",")[0]; //Where the button/activity type is stored
                    InteractionHandler.handleInteraction(level, event, buttonType);
                }
//                else {
//                    Log.error("Invalid sign text found!");
//                    Helper.setDebugDevMode(player);
//                }
            }
        }
        catch (Exception e) {
            Log.exception(e, "PlayerInteract: Unhandled exception!");
        }
    }
}