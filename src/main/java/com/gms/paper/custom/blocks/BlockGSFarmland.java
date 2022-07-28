package com.gms.paper.custom.blocks;

import cn.nukkit.block.*;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.BlockColor;

public class BlockGSFarmland extends BlockFarmland {

    private static boolean delete;

    public static boolean getDelete() {
        return delete;
    }

    public static boolean setDelete(Boolean bool) {
        delete = bool;
        return delete;
    }

    public BlockGSFarmland() {
        this(0);
    }

    public BlockGSFarmland(int meta) {
        super(meta);
    }

    public String getName() {
        return "Farmland";
    }

    public int getId() {
        return 60;
    }

    public double getResistance() {
        return 3.0D;
    }

    public double getHardness() {
        return 0.6D;
    }

    public int getToolType() {
        return 2;
    }

    public double getMaxY() {
        return this.y + 1.0D;
    }

    public int onUpdate(int type) {
        if (type != 2) {
            return 0;
        } else {
            Vector3D v = new Vector3();
            if (this.level.getBlock(v.setComponents(this.x, this.y + 1.0D, this.z)) instanceof BlockCrops) {
                return 0;
            } else if (this.level.getBlock(v.setComponents(this.x, this.y + 1.0D, this.z)).isSolid()) {
                this.level.setBlock(this, Block.get(3), false, true);
                return 2;
            } else {
                boolean found = false;
                if (this.level.isRaining()) {
                    found = true;
                } else {
                    for(int x = (int)this.x - 4; (double)x <= this.x + 4.0D; ++x) {
                        for(int z = (int)this.z - 4; (double)z <= this.z + 4.0D; ++z) {
                            for(int y = (int)this.y; (double)y <= this.y + 1.0D; ++y) {
                                if ((double)z != this.z || (double)x != this.x || (double)y != this.y) {
                                    v.setComponents((double)x, (double)y, (double)z);
                                    int block = this.level.getBlockIdAt(v.getFloorX(), v.getFloorY(), v.getFloorZ());
                                    if (block == 8 || block == 9) {
                                        found = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                Block block = this.level.getBlock(v.setComponents(this.x, this.y - 1.0D, this.z));
                if (!found && !(block instanceof BlockWater)) {
                    if (this.getDamage() > 0) {
                        this.setDamage(this.getDamage() - 1);
                        this.level.setBlock(this, this, false, false);
                    } else {
                        this.level.setBlock(this, Block.get(3), false, true);
                    }

                    return 2;
                } else {
                    if (this.getDamage() < 7) {
                        this.setDamage(7);
                        this.level.setBlock(this, this, false, false);
                    }

                    return 2;
                }
            }
        }
    }

    public Item toItem() {
        return new ItemBlock(Block.get(3));
    }

    public BlockColor getColor() {
        return BlockColor.DIRT_BLOCK_COLOR;
    }
}

