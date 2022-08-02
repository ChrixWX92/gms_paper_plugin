package com.gms.paper.util.world;

import com.gms.paper.util.blocks.GSSign;
import lombok.Getter;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntitySign;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.List;

public class GSWorld {

    public net.minecraft.world.level.World nmsWorld;
    public org.bukkit.World bukkitWorld;

    @Getter
    public @NotNull List<Entity> entities;
    @Getter
    public Map<BlockPosition, TileEntity> blockEntities;
    @Getter
    public Set<GSSign> signs;

    public GSWorld(net.minecraft.world.level.World nmsWorld) {
        this.nmsWorld = nmsWorld;
        this.bukkitWorld = nmsWorld.getWorld();
        this.populateFields();

    }

    public GSWorld(org.bukkit.World bukkitWorld) {
        this.bukkitWorld = bukkitWorld;
        this.nmsWorld = ((CraftWorld) bukkitWorld).getHandle();
        this.populateFields();
    }

    private void populateFields() {
        this.entities = this.bukkitWorld.getEntities();
        this.blockEntities = this.nmsWorld.capturedTileEntities;
        this.signs = this.fetchSigns();
    }

    public TileEntity getBlockEntity(BlockPosition blockPosition) {
        return this.nmsWorld.getBlockEntity(blockPosition, true); //TODO: No idea what this second argument does
    }

    public Map<Location, TileEntity> getBlockEntitiesByType(Class<? extends TileEntity> type) {
        Map<Location, TileEntity> blockEntities = new HashMap<>();
        for (Entity entity : this.entities) {
            if (entity.getClass().isAssignableFrom(type)) {
                blockEntities.put(entity.getLocation(), type.cast(entity));
            }
        }
        return blockEntities;
    }

    public Set<GSSign> fetchSigns() {
        Set<GSSign> signs = new HashSet<>();
        for (Map.Entry<BlockPosition, TileEntity> blockEntity : this.blockEntities.entrySet()) {
            if (blockEntity.getValue() instanceof TileEntitySign tileEntitySign) {
                GSSign sign = new GSSign(tileEntitySign);
                signs.add(sign);
            }
        }
        return signs;
    }

    public GSSign getSignFromText(String... lines) {

        for (GSSign sign : this.signs) {
                if (sign.getText()[0].equals(lines[0])) {
                    String[] text = sign.getText();
                    switch (lines.length) {
                        case 4 : if (!text[3].equals(lines[3])) continue;
                        case 3 : if (!text[2].equals(lines[2])) continue;
                        case 2 : if (!text[1].equals(lines[1])) continue;
                        case 1 : return sign;
                    }
                }
            }

        return null;

    }

    /*
    public static ItemFrame spawnItemFrame(Location loc, BlockFace bf) {
        EnumDirection side = null;
        if(bf == BlockFace.SOUTH) {
            side = EnumDirection.d;
        } else if(bf == BlockFace.WEST) {
            side = EnumDirection.e;
        } else if(bf == BlockFace.NORTH) {
            side = EnumDirection.c;
        } else if(bf == BlockFace.EAST) {
            side = EnumDirection.f;;
        }
        // GET NMS WORLD
        WorldServer w = ((CraftWorld)loc.getWorld()).getHandle();
        // CREATE A NEW ITEMFRAME
        EntityItemFrame entity = new EntityItemFrame(((CraftWorld)loc.getWorld()).getHandle(), new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), side);
        // ADDS THE ITEMFRAME TO THE WORLD
        w.addFreshEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        // FINALLY, RETURNS THE BUKKIT ITEMFRAME
        return (ItemFrame)entity.getBukkitEntity();
    }

        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
*/
}
