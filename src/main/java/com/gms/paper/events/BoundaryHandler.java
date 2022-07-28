package com.gms.paper.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import com.gms.paper.error.InvalidFrameWriteException;
import com.gms.paper.interact.puzzles.maths.Arithmetic;
import com.gms.paper.interact.puzzles.maths.Freefall;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

public class BoundaryHandler implements Listener {

    float vol;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) throws InvalidFrameWriteException, InterruptedException {
        if (Arithmetic.puzzleName == null) return;

        Location playerLoc = event.getPlayer().getLocation();

        if (Arithmetic.puzzleName.equals("Freefall1")){
            Location fixYaw;
            if (playerLoc.getY() < 17) {
                vol = 0;
                HashSet<Location> numberPosition = Freefall.getNumberPosition();
                double minX = Collections.min(numberPosition, Comparator.comparing(Location::getX)).getX();
                double maxX = Collections.max(numberPosition, Comparator.comparing(Location::getX)).getX();
                double minZ = Collections.min(numberPosition, Comparator.comparing(Location::getZ)).getX();
                double maxZ = Collections.max(numberPosition, Comparator.comparing(Location::getZ)).getX();
                if ((playerLoc.getX() >= minX-1 && playerLoc.getX() <= maxX+1) && (playerLoc.getZ() >= minZ-1 && playerLoc.getZ() <= maxZ+1)) {
                    Freefall.doFreefall(event.getPlayer(), Arithmetic.puzzleName, Freefall.getFreefallFacing(), Freefall.getFreefallCentre(), true);
                } else {
                    Arithmetic.mark(playerLoc,false, Freefall.getFreefallSum(), true);
                    int tpYaw;
                    switch (Freefall.getFreefallFacing()) {
                        case "N" -> tpYaw = -180;
                        case "E" -> tpYaw = -90;
                        case "S" -> tpYaw = 0;
                        case "W" -> tpYaw = 90;
                        default -> {return;}
                    }
                    fixYaw = new Location(Freefall.getFreefallCentre().getWorld(), Freefall.getFreefallCentre().getX(), Freefall.getFreefallHeight(), Freefall.getFreefallCentre().getZ(), (float) tpYaw, 90F);
                    event.getPlayer().teleport(fixYaw);
                }
            } else if (playerLoc.getY() <= (Freefall.getFreefallCentre().getY() + Freefall.getFreefallHeight())-2) {
                vol = vol + 0.005F;
                playerLoc.getWorld().addSound(new Location(playerLoc.getX(), playerLoc.getY()-10, playerLoc.getZ()), Sound.ELYTRA_LOOP, vol,1+vol);
            }
        }
    }
}

