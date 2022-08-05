package com.gms.paper.util;

import com.gms.paper.util.blocks.GSSign;
import org.bukkit.block.BlockFace;

public class EntityDirectionHelper {

    public static BlockFace getSignFacingDirection(GSSign signEntity) {

        return signEntity.getFacing();

    }

    public static BlockFace getItemFrameFacingDirection(BlockEntityItemFrame itemFrameEntity) {

        var damageValue = itemFrameEntity.getBlock().getDamage();
        switch (damageValue) {
            case 3:
                return BlockFace.NORTH;
            case 2:
                return BlockFace.SOUTH;
            case 0:
                return BlockFace.EAST;
            case 1:
                return BlockFace.WEST;
        }

        Log.error(String.format("Item frame direction for this damage value does not exist %d", damageValue));
        return null;
    }

}

