package com.gms.paper.util;

import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.format.FullChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import cn.nukkit.Player;
import com.gms.paper.util.blockentity.BlockEntity;

public class Scanner {

    /***
     * Intended for getting locations of the type of entity within a given range of the player.
     * @param player The Player object
     * @param entityType The type of entity to search for e.e BlockEntitySign
     * @param minX Minimum X offset from the player
     * @param maxX Maximum X offset from the player
     * @param minX Minimum Y offset from the player
     * @param maxY Maximum Y offset from the player
     * @param minX Minimum Z offset from the player
     * @param maxZ Maximum Z offset from the player
     * @return The list of location for the entities found.
     */
    public List<Location> Scan(Player player, Class<?> entityType, int minX, int maxX, int minY, int maxY, int minZ, int maxZ){

        List<Location> locations = new ArrayList<Location>();

        Level level = player.getLevel();
        Map<Long, ? extends FullChunk> chunksMap = level.getChunks();
        Map<Long, BlockEntity> entMap;

        for (var entry : chunksMap.entrySet()) {
            entMap = entry.getValue().getBlockEntities();
            for (var chunkEntry : entMap.entrySet()) {
                if (entityType.isInstance(chunkEntry.getValue())) {
                    //String[] text = ((BlockEntitySign) chunkEntry.getValue()).getText();
                    if(inBoundingBox(minX, maxX, minY, maxY, minZ, maxZ, chunkEntry.getValue().getLocation(), player.getLocation())){
                        locations.add(chunkEntry.getValue().getLocation());
                    }
                }
            }
        }
        return locations;
    }

    private boolean inBoundingBox(int minX, int maxX, int minY, int maxY, int minZ, int maxZ, Location blockLocation, Location playerLocation) {
        if(blockLocation.x > playerLocation.x + minX && blockLocation.x < playerLocation.x + maxX){
            if(blockLocation.y > playerLocation.y + minY && blockLocation.y < playerLocation.y + maxY){
                if(blockLocation.z > playerLocation.z + minZ && blockLocation.z < playerLocation.z + maxZ){
                    return true;
                }
            }
        }
        return false;
    }

}

