package com.gms.paper.custom.blocks;

import org.bukkit.entity.Player;
import cn.nukkit.block.*;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityFallingBlock;
import cn.nukkit.event.block.BlockFallEvent;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.utils.Faceable;
import com.gms.paper.Main;
import com.gms.paper.interact.puzzles.maths.Arithmetic;
import com.gms.paper.level.gslevels.GSLevel;
import com.gms.paper.level.gslevels.rules.GSGameRule;
import org.bukkit.block.Block;

public class BlockGSAnvil implements Block {

    private static boolean delete;

    public static boolean getDelete() {
        return delete;
    }

    public static boolean setDelete(Boolean bool) {
        delete = bool;
        return delete;
    }

    @Override
    public int getId() {
        return 145;
    }

    @Override
    public String getName() {
       // if (Arithmetic.world != 0) {
            return "Anvil";
        //} else {
        //    String[] gsaNames = new String[]{"Anvil", "Anvil", "Anvil", "Anvil", "Slightly Damaged Anvil", "Slightly Damaged Anvil", "Slightly Damaged Anvil", "Slightly Damaged Anvil", "Very Damaged Anvil", "Very Damaged Anvil", "Very Damaged Anvil", "Very Damaged Anvil"}; // Nukkit features a typo here ("Slighty")
        //    return gsaNames[this.getDamage() > 11 ? 0 : this.getDamage()];
       // }

    }

    @Override
    public boolean canBeActivated() {
        return Arithmetic.world == 0;
    }

    @Override
    public Item toItem() { //TODO: Will return non-fatal null pointer - can we use an empty object instead?
       // if (Arithmetic.world != 0) {
            return null;
       // } else {
       //     return super.toItem();
       // }
    }

    @Override
    public Item[] getDrops(Item item) {
        if (Arithmetic.world != 0) {
            return null;
        } else {
            return super.getDrops(item);
        }
    }

    @Override
    public boolean onActivate(Item item, Player player) {
        GSLevel gsLevel = Main.getGsLevelManager().getGSLevel(player);
        if (!gsLevel.getGSGameRules().getBoolean(GSGameRule.USE_ANVIL)) {
            return false;
        } else {
            return super.onActivate(item, player);
        }
    }

    @Override
    public int onUpdate(int type) {
        //if (Arithmetic.world != 0) {
            if (delete) {
                return type;
            }
            if (type == 1) {
                Block down = this.down();
                if (down.getId() == 0 || down instanceof BlockLiquid || down instanceof BlockFire) {
                    BlockFallEvent event = new BlockFallEvent(this);
                    this.level.getServer().getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return type;
                    }

                    this.level.setBlock(this, Block.get(0), true, true);
                    CompoundTag nbt = (new CompoundTag()).putList((new ListTag("Pos")).add(new DoubleTag("", this.x + 0.5D)).add(new DoubleTag("", this.y)).add(new DoubleTag("", this.z + 0.5D))).putList((new ListTag("Motion")).add(new DoubleTag("", 0.0D)).add(new DoubleTag("", 0.0D)).add(new DoubleTag("", 0.0D))).putList((new ListTag("Rotation")).add(new FloatTag("", 0.0F)).add(new FloatTag("", 0.0F))).putInt("TileID", this.getId()).putByte("Data", this.getDamage());
                    EntityFallingBlock fall = (EntityFallingBlock) Entity.createEntity("FallingSand", this.getWorld().getChunk((int) this.x >> 4, (int) this.z >> 4), nbt, new Object[0]);
                    if (fall != null) {
                        fall.spawnToAll();
                    }
                }
            }
            return type;
        //} else {
       //     return super.onUpdate(type);
       // }
    }
}

