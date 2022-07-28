package com.gms.paper.interact.puzzles.handlers.anchors.unique;

import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import com.gms.mc.interact.puzzles.maths.Arithmetic;
import com.gms.mc.util.Log;

import java.util.HashMap;
import java.util.Map;

public class DenyAnchor implements Listener {

    private static boolean deny = false;
    public static HashMap<Vector3, Integer> anchorLocs = new HashMap<>();

    @EventHandler
    public void onInteract(BlockBreakEvent event) {

        if (!deny) return;

        Block block = event.getBlock();
        for (Map.Entry<Vector3, Integer> e : anchorLocs.entrySet()) {
            if (Arithmetic.inApothem(e.getValue(), e.getKey(), block.getLocation(), true)) {
                event.setCancelled();
                Log.info(TextFormat.GOLD + "DENY ANCHOR: " + TextFormat.WHITE + "Block break denied.");
                return;
            }
        }

    }

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) {

        if (!deny) return;

        Block block = event.getBlock();
        for (Map.Entry<Vector3, Integer> e : anchorLocs.entrySet()) {
            if (Arithmetic.inApothem(e.getValue(), e.getKey(), block.getLocation(), true)) {
                event.setCancelled();
                Log.info(TextFormat.GOLD + "DENY ANCHOR: " + TextFormat.WHITE + "Block placement denied.");
                return;
            }
        }

    }

    public static boolean isDeny() {
        return deny;
    }

    public static void setDeny(boolean deny) {
        DenyAnchor.deny = deny;
    }

}
