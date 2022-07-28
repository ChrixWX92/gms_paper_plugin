package com.gms.paper.interact.puzzles.maths.threads;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.Tag;

import java.util.HashMap;
import java.util.Map;

public class FindFrames implements Runnable{

    private final Player p;
    private final String t;
    private final boolean sw;

    private static volatile Map<Long, BlockEntityItemFrame> frameMap;

    public FindFrames(Player player, String tag){
        this(player, tag, false);
    }

    public FindFrames(Player player, String tag, boolean startsWith){
        p = player;
        t = tag;
        frameMap = new HashMap<>();
        sw = startsWith;
    }

    /***
     * Returns a list of frames that contain matching nbt tags in the player's level in a hashmap
     * getMap() returns a hashmap of BlockEntityItemFrame objects in the player's level that contain an nbt tag key equal to tag
     */

    @Override
    public void run() {
        Level level = p.getLevel();

        Map<Long, ? extends FullChunk> chunksMap = level.getChunks();

        Map<Long, BlockEntity> entMap;

        frameMap = new HashMap<>();

        for (var entry : chunksMap.entrySet()) {
            entMap = entry.getValue().getBlockEntities();
            for (var chunkEntry : entMap.entrySet()) {
                if (chunkEntry.getValue() instanceof BlockEntityItemFrame beif) {
                    if (sw) {
                        for (Tag tag : beif.namedTag.getAllTags()) {
                            if (tag.getName().startsWith(t)) {
                                frameMap.put(chunkEntry.getKey(), beif);
                            }
                        }
                    } else {
                        if (beif.namedTag.contains(t)) {
                            frameMap.put(chunkEntry.getKey(), beif);
                        }
                    }
                }
            }
        }
    }

    public static Map<Long, BlockEntityItemFrame> getMap() {
        return frameMap;
    }

    public boolean isSw() {
        return sw;
    }
}
