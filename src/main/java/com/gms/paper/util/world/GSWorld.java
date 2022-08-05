package com.gms.paper.util.world;

import com.destroystokyo.paper.exception.ServerException;
import com.gms.paper.Main;
import com.gms.paper.data.GamePosition;
import com.gms.paper.level.LevelManager;
import com.gms.paper.util.Log;
import com.gms.paper.util.blocks.GSSign;
import lombok.Getter;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.WorldProviderNormal;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntitySign;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class GSWorld {

    @Getter
    public net.minecraft.world.level.World nmsWorld;
    @Getter
    public WorldServer worldServer;
    @Getter
    public org.bukkit.World bukkitWorld;
    @Getter
    public String name;
    @Getter
    public Location spawnLocation;
    @Getter
    public @NotNull List<Entity> entities;
    @Getter
    public Map<BlockPosition, TileEntity> blockEntities;
    @Getter
    public Set<GSSign> signs;
    @Getter
    public Path worldPath = Main.s_plugin.getServer().getWorldContainer().toPath();

    public GSWorld(String name) {

    }

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
        this.name = this.bukkitWorld.getName();
        this.spawnLocation = this.bukkitWorld.getSpawnLocation();
        this.worldServer = nmsWorld.getMinecraftWorld();
        this.entities = this.bukkitWorld.getEntities();
        this.blockEntities = this.nmsWorld.capturedTileEntities;
        this.signs = this.fetchSigns();
    }

    public TileEntity getBlockEntity(GamePosition gamePosition) {
        BlockPosition blockPosition = new BlockPosition(gamePosition.x, gamePosition.y, gamePosition.z);
        return this.nmsWorld.getBlockEntity(blockPosition, true);
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


    public GSWorld loadLevel(String name) throws ServerException {
        if (Objects.equals(name.trim(), "")) {
            throw new ServerException("Invalid empty level name");
        } else if (this.isLoaded()) {
            return null;
        } else if (!this.isGenerated()) {
            Log.warn("GSWorld not found for name " + name);
            return null;
        } else {
            WorldCreator wc = new WorldCreator(name);
            GSWorld newWorld = new GSWorld(Bukkit.createWorld(wc));
            if (newWorld.bukkitWorld == null) {
                Log.error("GSWorld not found for name " + name + " - unknown provider");
                return null;
            } else {
                newWorld.initialize();
                return newWorld;
            }
        }
    }

    public boolean load() {
        WorldCreator wc = new WorldCreator(this.name);
        Main.s_plugin.getServer().createWorld(wc);
        return this.isLoaded();
    }

    public boolean isLoaded() {
        return Main.s_plugin.getServer().getWorld(this.name) != null;
    }

    public boolean isGenerated() {
        if (Objects.equals(this.name.trim(), "")) {
            return false;
        } else if (Main.s_plugin.getServer().getWorld(this.name) != null) {
            return true;
        } else {
            String path;
            if (!this.name.contains("/") && !this.name.contains("\\")) {
                path = this.worldPath + this.name + "/";
            } else {
                path = this.name;
            }
            return new File(path).isFile();
        }
    }

    public void initialize() {
        Log.info("Preparing start region for GSWorld " + this.name);
        Location spawn = this.spawnLocation;
        this.bukkitWorld.getChunkAt(spawn).load();
    }


}
