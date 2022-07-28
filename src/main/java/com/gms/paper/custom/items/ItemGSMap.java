package com.gms.paper.custom.items;

import org.bukkit.entity.Player;
import cn.nukkit.block.Block;
import cn.nukkit.item.ItemMap;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import com.gms.paper.util.Log;

import java.awt.image.BufferedImage;

public class ItemGSMap extends ItemMap {

    String name;

    public ItemGSMap() {
        this("GSMap", "", null);
    }

    public ItemGSMap(String name, String puzzle, BufferedImage image) {
        super();
        if (image != null) {
            this.setImage(image);
        }
        this.name = "§f" + name;
        this.setCustomName(this.name);
        CompoundTag customItemNBT = this.getNamedTag();
        customItemNBT.putString(puzzle, name);
        customItemNBT.putBoolean("Undroppable", true);
        //this.setLore("Placeholder");
    }

    public boolean onUse(Player player, int ticksUsed) {
        Log.debug("USING MAP");
        return true;}
    public boolean canBeActivated() {return false;}
    public boolean isUnbreakable() {return true;}
    public boolean onActivate(Level level, Player player, Block block, Block target, BlockFace face, double fx, double fy, double fz) {return false;}
    public boolean onClickAir(Player player, Vector3D directionVector) {return false;}
    public int getMaxStackSize() {return 1;}
    public boolean isUndroppable() {return true;}
}