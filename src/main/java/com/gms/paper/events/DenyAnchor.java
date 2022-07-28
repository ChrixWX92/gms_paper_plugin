package com.gms.paper.events;

import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.math.Vector3;
import com.gms.paper.util.TextFormat;
import com.gms.paper.interact.puzzles.maths.Arithmetic;
import com.gms.paper.util.Log;
import com.gms.paper.util.Vector3D;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;

public class DenyAnchor implements Listener {

    private static boolean deny = false;
    public static HashMap<Vector3D, Integer> anchorLocs = new HashMap<>();

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
