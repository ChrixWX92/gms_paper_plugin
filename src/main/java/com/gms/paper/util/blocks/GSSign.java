package com.gms.paper.util.blocks;

import com.gms.paper.util.world.GSWorld;
import lombok.Getter;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.world.level.block.entity.TileEntitySign;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock;

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

    public GSSign(Sign bukkitSign) {
        this.bukkitSign = bukkitSign;
        this.location = bukkitSign.getLocation();
        this.gsWorld = new GSWorld(this.location.getWorld());

        //TODO: GET NMS SIGN FROM BUKKIT ONE

        this.text = extractText();
    }

    public GSSign(TileEntitySign nmsSign) {
        this.nmsSign = nmsSign;
        this.bukkitSign = (Sign) CraftBlock.at(nmsSign.k(), nmsSign.p()).getLocation();
        this.location = this.bukkitSign.getLocation();
        this.text = extractText();
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

}
