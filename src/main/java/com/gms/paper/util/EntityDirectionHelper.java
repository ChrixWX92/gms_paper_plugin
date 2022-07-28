package com.gms.paper.util;

import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.math.BlockFace;

public class EntityDirectionHelper {

    public static BlockFace getSignFacingDirection(BlockEntitySign signEntity) {

        var damageValue = signEntity.getBlock().getDamage();
        switch (damageValue) {
            case 2:
                return BlockFace.NORTH;
            case 3:
                return BlockFace.SOUTH;
            case 5:
                return BlockFace.EAST;
            case 4:
                return BlockFace.WEST;
        }

        Log.error(String.format("Sign direction for this damage value does not exist %d", damageValue));
        return null;
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

