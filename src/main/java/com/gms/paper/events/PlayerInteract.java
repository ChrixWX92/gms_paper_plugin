package com.gms.paper.events;


import com.gms.paper.util.Vector3D;
import com.gms.paper.util.blocks.GSSign;
import com.gms.paper.util.world.GSWorld;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntitySign;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import com.gms.paper.data.GamePosition;
import com.gms.paper.interact.InteractionHandler;
import com.gms.paper.util.Log;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerInteract implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Log.debug("PlayerInteract::onInteract! handler");
        try {
            @NotNull Action action = event.getAction();

            Block buttonBlock = event.getClickedBlock();
            if (buttonBlock == null) return;
            GamePosition b_loc = new GamePosition(null, new Vector3D(buttonBlock.getX(), buttonBlock.getY(), buttonBlock.getZ()));
            GamePosition signLoc = b_loc.add(new Vector3D(0, -2, 0)); //info sign
            GSWorld gsWorld = new GSWorld(buttonBlock.getLocation().getWorld());

            TileEntity signBlockEntity = gsWorld.getBlockEntity(signLoc);

            int[] xz_range = { 0, -1, 1 };

            for (int xi = 0; xi < 3 && !(signBlockEntity instanceof TileEntitySign); xi++) {
                int dx = xz_range[xi];
                for (int zi = 0; zi < 3 && !(signBlockEntity instanceof TileEntitySign); zi++) {
                    int dz = xz_range[zi];
                    for (int dy = -3; dy >= -5 && !(signBlockEntity instanceof TileEntitySign); dy--) {
                        Log.warn(String.format("No signBlockEntity for sign loc: %s [dx: %d, dy = %d, dz = %d]", signLoc, dx, dy, dz));
                        signLoc = b_loc.add(new Vector3D(dx, dy, dz)); //info sign
                        signBlockEntity = gsWorld.getBlockEntity(signLoc);
                    }
                }
            }

            if (signBlockEntity == null) {
                Log.error(String.format("No signBlockEntity found at all for b_loc: %s. Changing game mode to creative for dev servers ...", b_loc));
            }

            String buttonType = "";

            if (signBlockEntity instanceof TileEntitySign sign) {
                GSSign gsSign = new GSSign(sign);

                if (action.equals(Action.RIGHT_CLICK_BLOCK) && Tag.BUTTONS.isTagged(buttonBlock.getType()) && Tag.SIGNS.isTagged(gsSign.bukkitSign.getBlock().getType())) {
                    //Checks if the button has a sign underneath it (where activity info is stored)
                    String[] signText = InteractionHandler.getSignInfo(gsWorld.bukkitWorld, signLoc);
                    if (signText != null && signText.length > 0) {
                        buttonType = signText[0].split(",")[0]; //Where the button/activity type is stored
                        InteractionHandler.handleInteraction(gsWorld.bukkitWorld, event, buttonType);
                    }
                }
            }
        }
        catch (Exception e) {
            Log.exception(e, "PlayerInteract: Unhandled exception!");
        }
    }
}