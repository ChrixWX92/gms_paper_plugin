package com.gms.paper.util.blocks;

import com.gms.paper.util.world.GSWorld;
import lombok.Getter;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.world.level.block.entity.TileEntitySign;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock;
import org.jetbrains.annotations.NotNull;

public class GSSign {

    @Getter
    public Sign bukkitSign;
    @Getter
    public TileEntitySign nmsSign;
    @Getter
    public String[] text;
    @Getter
    public Location location;
    @Getter
    public GSWorld gsWorld;
    @Getter
    public @NotNull BlockFace facing;

    public GSSign(Sign bukkitSign) {
        this.bukkitSign = bukkitSign;
        this.populateFields();

        //TODO: GET NMS SIGN FROM BUKKIT ONE

    }

    public GSSign(TileEntitySign nmsSign) {
        this.nmsSign = nmsSign;
        this.bukkitSign = (Sign) CraftBlock.at(nmsSign.k(), nmsSign.p()).getLocation();
        this.populateFields();
    }

    private String[] extractText() {
        String[] text = new String[4];
        int index = 0;
        for (net.kyori.adventure.text.Component line : this.bukkitSign.lines()) {
            text[index] = ((TextComponent) line).content();
            index++;
        }
        return text;
    }

    private void populateFields() {
        this.location = this.bukkitSign.getLocation();
        this.gsWorld = new GSWorld(this.location.getWorld());
        this.text = extractText();
        if (this.bukkitSign.getBlockData() instanceof Directional directional) {
            this.facing = directional.getFacing();
        }
    }


}
