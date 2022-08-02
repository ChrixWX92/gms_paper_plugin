package com.gms.paper.events;

import cn.nukkit.Player;
import cn.nukkit.block.*;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.utils.TextFormat;
import com.gms.mc.error.InvalidFrameWriteException;
import com.gms.mc.interact.puzzles.maths.Arithmetic;
import com.gms.mc.interact.puzzles.maths.Farm;

import java.lang.reflect.InvocationTargetException;

import static cn.nukkit.event.player.PlayerInteractEvent.Action.*;
import static com.gms.mc.interact.puzzles.PuzzleType.FARM;

public class FarmlandHandling implements Listener {

    //TODO: Left click events don't fire in adventure mode - if we want to improve player interaction here we'll have
    // to change the player's gamemode to survival during the puzzle, or implement ray tracing a la the build plugin's
    // fill mechanic

    private long lastPressed = System.currentTimeMillis();

    @EventHandler
    public void farmlandListener(PlayerInteractEvent event) throws InvalidFrameWriteException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (Arithmetic.currentPuzzle == null) {
            return;
        }
        Block block = event.getBlock();

        Player player = event.getPlayer();

        if (player.getGamemode() == 2) {
            event.setCancelled(true);
            if (Arithmetic.currentPuzzle.getPuzzleType() == FARM) {

                if ((System.currentTimeMillis() - lastPressed) >= 350) {
                    lastPressed = System.currentTimeMillis();
                } else return;

                Farm farm = ((Farm) Arithmetic.currentPuzzle);
                if (farm.isFinalQuestion()) {
                    event.setCancelled(true);
                    return;
                }
                if (block instanceof BlockFarmland) {
                        for (Farm.CropType cropType : Farm.CropType.values()) {
                            if (cropType.getItemID() == player.getInventory().getItemInHand().getId()) {
                                if (block.down().down().getId() != cropType.getPlotBlockID()) {
                                    player.sendMessage(TextFormat.LIGHT_PURPLE + "This is the wrong plot for this crop! Try planting this in the " + TextFormat.AQUA + cropType.getName() + TextFormat.LIGHT_PURPLE + " plot.");
                                    return;
                                } else {
                                    farm.plant(cropType, block.up().getLocation());
                                }
                            }
                        }
                    } else if (block instanceof BlockCrops crop) {
                    farm.uproot(crop);
                }
            }
        }
    }

}
