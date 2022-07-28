package com.gms.paper.interact.puzzles.maths;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import com.gms.mc.error.InvalidFrameWriteException;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

public class BoundaryHandler implements Listener {

    float vol;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) throws InvalidFrameWriteException, InterruptedException {
        if (Arithmetic.puzzleName == null) return;

        Player p = event.getPlayer();

        if (Arithmetic.puzzleName.equals("Freefall1")){
            Location fixYaw;
            if (p.y < 17) {
                vol = 0;
                HashSet<Location> numberPosition = Freefall.getNumberPosition();
                double minX = Collections.min(numberPosition, Comparator.comparing(Location::getX)).x;
                double maxX = Collections.max(numberPosition, Comparator.comparing(Location::getX)).x;
                double minZ = Collections.min(numberPosition, Comparator.comparing(Location::getZ)).z;
                double maxZ = Collections.max(numberPosition, Comparator.comparing(Location::getZ)).z;
                if ((p.x >= minX-1 && p.x <= maxX+1) && (p.z >= minZ-1 && p.z <= maxZ+1)) {
                    Freefall.doFreefall(p, Arithmetic.puzzleName, Freefall.getFreefallFacing(), Freefall.getFreefallCentre(), true);
                } else {
                    Arithmetic.mark(p,false, Freefall.getFreefallSum(), true);
                    int tpYaw;
                    switch (Freefall.getFreefallFacing()) {
                        case "N" -> tpYaw = -180;
                        case "E" -> tpYaw = -90;
                        case "S" -> tpYaw = 0;
                        case "W" -> tpYaw = 90;
                        default -> {return;}
                    }
                    fixYaw = new Location(Freefall.getFreefallCentre().x, Freefall.getFreefallHeight(), Freefall.getFreefallCentre().z, (float) tpYaw, 90F);
                    p.teleport(fixYaw);
                }
            } else if (p.y <= (Freefall.getFreefallCentre().y + Freefall.getFreefallHeight())-2) {
                vol = vol + 0.005F;
                p.getLevel().addSound(new Location(p.x, p.y-10, p.z), Sound.ELYTRA_LOOP, vol,1+vol);
            }
        }
    }
}

