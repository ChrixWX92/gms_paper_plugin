package com.gms.paper.interact.puzzles.handlers.anchors;

import cn.nukkit.Player;
import cn.nukkit.block.BlockAir;
import cn.nukkit.block.BlockWallSign;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.utils.TextFormat;
import com.gms.mc.Main;
import com.gms.mc.commands.Pets;
import com.gms.mc.interact.puzzles.BackendUtils;
import com.gms.mc.interact.puzzles.PuzzleType;
import com.gms.mc.interact.puzzles.handlers.anchors.unique.DenyAnchor;
import com.gms.mc.interact.puzzles.maths.Arithmetic;
import com.gms.mc.util.Log;

import java.util.*;

public class AnchorHandler implements Listener {

    /**
     * - ANCHOR Sign Structure -
     *
     * ANCHOR
     * QuestionSetID
     * Apothem
     *
     */

    private static boolean anchorScan = false;
    private static HashMap<Vector3, String[]> levelAnchors = new HashMap<>();
    private static HashMap<Vector3, Integer> apothems = new HashMap<>();
    private static BlockEntitySign endAnchor = null;

    @EventHandler
    public void onMove(PlayerMoveEvent event) {

        if (!anchorScan) return;

        Player p = event.getPlayer();
        Location pLoc = p.getLocation();
        List<Vector3> spentSigns =  new ArrayList<>();

        for (Map.Entry<Vector3, Integer> e : apothems.entrySet()) {
            if (Arithmetic.inApothem(e.getValue(), e.getKey(), p.getLocation())) {
                //Log.debug(levelAnchors.get(e.getKey()).getText()[1]);
                try {
                    PuzzleType.initializePuzzleType(p, levelAnchors.get(e.getKey()), e.getKey());
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                levelAnchors.remove(e.getKey());
                spentSigns.add(e.getKey());
                if (levelAnchors.isEmpty()) {
                    Log.info("ANCHORS: All Anchors in the levels have been handled");
                    setAnchorScan(false);
                }
            }
        }
        for (Vector3 loc : spentSigns) apothems.remove(loc);
    }

    public static void setLevelAnchors(HashMap<Vector3, String[]> levelAnchors) {
        AnchorHandler.levelAnchors = levelAnchors;
    }
  
    public static void setEndAnchor(BlockEntitySign endAnchor) {
        AnchorHandler.endAnchor = endAnchor;
    }

    public static BlockEntitySign getEndAnchor() {
        return endAnchor;
    }

    public static void addLevelAnchors(HashMap<Vector3, BlockEntitySign> newAnchors) {
        DenyAnchor.setDeny(false);
        DenyAnchor.anchorLocs = new HashMap<>();
        apothems = new HashMap<>();
        for (Map.Entry<Vector3, String[]> e : levelAnchors.entrySet()) {
            try {
                if (!uniqueAnchors(e.getValue(), e.getKey())) {
                    if (!Objects.equals(e.getValue()[2], "END"))
                        apothems.put(e.getKey(), Integer.valueOf(e.getValue()[2]));
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                Log.error(Log.errorMsg("Unable to find apothem int tag for ANCHOR sign at " + e.getKey() + "."));
            }
        }
    }

    public static HashMap<Vector3, String[]> getLevelAnchors() {
        return levelAnchors;
    }

    public static void putLevelAnchor(Vector3 key, String[] levelAnchor) {
        if (!levelAnchor[2].equals("END")) {
            Log.info("ANCHORS: Anchor found at " + key.x + ", " + key.y + ", " + key.z + " (" + levelAnchor[1] + ")");
        } else {
            Log.info("ANCHORS: Anchor found at " + key.x + ", " + key.y + ", " + key.z + " (" + levelAnchor[1] + " - END)");
        }
        AnchorHandler.levelAnchors.put(key, levelAnchor);
        try {
            if (!uniqueAnchors(levelAnchor, key)) {
                if (!levelAnchor[2].equals("END"))
                    apothems.put(key, Integer.valueOf(levelAnchor[2]));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Log.error(Log.errorMsg("Unable to find apothem int tag for ANCHOR sign at " + key + "."));
            //Log.error(Log.errorMsg("CONTENT: " + e.getKey() + " " + Arrays.toString(e.getValue())));
        }
    }

    public static void placeAnchor(Player p, Level level, Location buttonLoc, int apothem, String data) {
        boolean facing = true;
        BlockWallSign anchorBlock = new BlockWallSign();
        Vector3 anchorBlockLoc = buttonLoc.add(0,-3);
        level.setBlock(anchorBlockLoc, new BlockAir(), true, true);
        switch (data.toUpperCase()) {
            case "N" -> anchorBlock.setDamage(2);
            case "E" -> anchorBlock.setDamage(5);
            case "S" -> anchorBlock.setDamage(0);
            case "W" -> anchorBlock.setDamage(4);
            default -> facing = false;
        }
        level.setBlock(anchorBlockLoc, anchorBlock, true, true);
        BlockEntitySign anchor = new BlockEntitySign(p.getChunk(), BlockEntityItemFrame.getDefaultCompound(level.getBlock(anchorBlockLoc),"Sign"));
        anchor.setText("ANCHOR", BackendUtils.getQuestionSetID().toLowerCase(), String.valueOf(apothem), facing ? data.toUpperCase() : data);
        level.addBlockEntity(anchor);
        level.scheduleBlockEntityUpdate(anchor);
    }

    public static void handleEndAnchor(Player p, String questionSetID) {

        Log.info("ANCHORS: Looking for END Anchor for puzzle " + questionSetID + ".");

        Vector3 teleportLoc = null;

        if (Arithmetic.findSignByText(p, "ANCHOR", questionSetID, "END") != null) {
            Log.info("ANCHORS: END Anchor acquired for puzzle " + questionSetID + " from scanning loaded chunks.");
            teleportLoc = Arithmetic.findGroundAbove(Arithmetic.findSignByText(p, "ANCHOR", questionSetID, "END").getLocation());
        } else {
            Log.info("ANCHORS: No END Anchor found for puzzle " + questionSetID + " in loaded chunks.");
            boolean found = false;
            HashMap<Vector3, String[]> levelAnchors = AnchorHandler.getLevelAnchors();
            for (Map.Entry<Vector3, String[]> levelAnchor : levelAnchors.entrySet()) {
                String[] anchorText = levelAnchor.getValue();
                if (anchorText[0].equalsIgnoreCase("ANCHOR") && anchorText[1].equalsIgnoreCase(questionSetID) && anchorText[2].equalsIgnoreCase("END")) {
                    teleportLoc = Arithmetic.findGroundAbove(levelAnchor.getKey(), p.getLevel());
                    found = true;
                    Log.warn("ANCHORS: END Anchor acquired for puzzle " + questionSetID + " from region data.");
                    break;
                }
            }
            if (!found) {
                Log.warn("ANCHORS: No END Anchor found for puzzle " + questionSetID + ".");
                return;
            }
        }
        if (teleportLoc != null) {
            Vector3 finalTeleportLoc = teleportLoc;
            new NukkitRunnable() {
                @Override
                public void run() {
                    Log.info("ANCHORS: Teleporting player to nearest ground above END anchor for " + questionSetID + ".");
                    p.teleport(finalTeleportLoc);
                }
            }.runTaskLater(Main.s_plugin, 80);
        } else {
            Log.error("ANCHORS: Unexpected error acquiring teleport location for " + questionSetID + "'s END Anchor.");
        }

    }

    public static boolean uniqueAnchors(String[] anchor, Vector3 location) {
        switch (anchor[1].toUpperCase()) {
            case "DENY" -> {
                DenyAnchor.setDeny(true);
                try {
                    DenyAnchor.anchorLocs.put(location, Integer.valueOf(anchor[2]));
                    Log.info(TextFormat.GOLD + "DENY ANCHOR: " + TextFormat.WHITE + "Located at " + location + ".");
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Log.error(Log.errorMsg("Unable to find apothem int tag for ANCHOR DENY sign at " + location + "."));
                    return false;
                }
                return true;
            }
            case "DRAGON" -> {
                Pets.setDragonAnchorLoc(location);
                Log.info(TextFormat.GOLD + "DRAGON ANCHOR: " + TextFormat.WHITE + "Located at " + location + ".");
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    public static boolean isAnchorScan() {
        return anchorScan;
    }

    public static void resetAnchorScan() {
        Log.debug("Resetting anchor scan");
        levelAnchors = new HashMap<>();
        apothems = new HashMap<>();
        endAnchor = null;
        anchorScan = false;
    }

    public static void setAnchorScan(boolean anchorScan) {
        if (anchorScan = true) {
            Log.info("ANCHORS: Anchor Scanning enabled");
        }
        else {
            Log.info("ANCHORS: Anchor Scanning disabled");
        }
        AnchorHandler.anchorScan = anchorScan;
    }
}
