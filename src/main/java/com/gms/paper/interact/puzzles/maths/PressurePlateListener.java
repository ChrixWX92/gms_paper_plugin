package com.gms.paper.interact.puzzles.maths;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockPressurePlateStone;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockRedstoneEvent;
import cn.nukkit.level.Location;
import com.gms.mc.PlayerInstance;

import java.util.Map;

import static com.gms.mc.interact.puzzles.maths.Arithmetic.puzzleName;


public class PressurePlateListener implements Listener {

    @EventHandler
    public void onPress(BlockRedstoneEvent event) throws Exception {

        if (event.getNewPower() > 0) {

            Block plate = event.getBlock();
            Map<Long, Player> players = event.getBlock().getLevel().getPlayers();
            Player p = null;

            // Assigns a player object to the event, based a potential player's proximity to the sign
            for (Player player : players.values()) {
                double x = player.x - plate.x;
                double y = player.y - plate.y;
                double z = player.z - plate.z;
                double radius = 1.5;
                if (x>-radius && x<radius && y>-radius && y<radius && z>-radius && z<radius) {
                    p = player;
                }
            }

            if (plate instanceof BlockPressurePlateStone && puzzleName.startsWith("Islands")) {

                Location signLoc = plate.getLocation().add(0, -2);
                BlockEntitySign blockEntitySign = (BlockEntitySign) plate.getLevel().getBlockEntity(signLoc.round());
                String[] signText = blockEntitySign.getText();

                for (int i = 0; i < signText.length; i++) {
                    signText[i] = signText[i].replaceAll("§0", "");
                }

                if (p != null) {
                    MATHS_InteractionHandler.submit(p, signText, plate, PlayerInstance.getPlayer(p.getName()).getProfile());
                }

            }
        }
    }
}
