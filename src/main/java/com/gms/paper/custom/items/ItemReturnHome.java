package com.gms.paper.custom.items;

import org.bukkit.entity.Player;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

public class ItemReturnHome extends Item {

    String name;

    public ItemReturnHome() {
        this(0, 1);
    }

    public ItemReturnHome(Integer meta, int count) {
        super(403, meta, count, "Return Home");
        this.name = "Return Home";
        this.setCount(1);
        this.setCustomName(this.name);
        CompoundTag customItemNBT = this.getNamedTag();

        if (customItemNBT == null) {
            customItemNBT = new CompoundTag();
            this.setNamedTag(customItemNBT);
        }
        customItemNBT.putBoolean("RH", true);
        this.setLore("Use this to go back to a previous lobby.");
    }

    public boolean onUse(Player player, int ticksUsed) {
        return false;//returnHome(player);
    }

    public boolean canBeActivated() {
        return false;
    }
    public boolean isUnbreakable() { return true; }


    public boolean onActivate(Level level, Player player, Block block, Block target, BlockFace face, double fx, double fy, double fz) {
        return false;//returnHome(player);
    }

    public boolean onClickAir(Player player, Vector3D directionVector) {
        return false;//returnHome(player);
    }


    public int getMaxStackSize() {
        return 1;
    }
}