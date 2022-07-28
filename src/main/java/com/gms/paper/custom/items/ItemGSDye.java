package com.gms.paper.custom.items;

import org.bukkit.entity.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockWool;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

public class ItemGSDye extends Item {
    public ItemGSDye() {
        this(1, "GSDye", "", 0);
    }

    public ItemGSDye(int count, String name, String puzzle, int damage) {
        super(35, damage, count, "§l+§9" + name);
        String formatting;
        this.setDamage(damage); //blue = 11, red = 14
        switch (damage) {
            case 11 -> formatting = "§9";
            case 14 -> formatting = "§c";
            default -> formatting = "§f";
        }
        this.name = "§l" + formatting + name;
        this.block = new BlockWool(damage);
        this.setCount(count);
        this.setCustomName(this.name);
        CompoundTag customItemNBT = this.getNamedTag();
        if (customItemNBT == null) {
            customItemNBT = new CompoundTag();
            this.setNamedTag(customItemNBT);
        }
        customItemNBT.putString(puzzle, name);
        customItemNBT.putInt("damage", damage);
        customItemNBT.putBoolean("Undroppable", true);
        //this.setLore("Placeholder");
    }

    public boolean onUse(Player player, int ticksUsed) {
        return false;
    }

    public boolean canBeActivated() {
        return false;
    }

    public boolean isUnbreakable() {
        return true;
    }

    public boolean onActivate(Level level, Player player, Block block, Block target, BlockFace face, double fx, double fy, double fz) {
        return false;
    }

    public boolean onClickAir(Player player, Vector3D directionVector) {
        return false;
    }

    public int getMaxStackSize() {
        return 64;
    }

    public boolean isUndroppable() {
        return true;
    }
}