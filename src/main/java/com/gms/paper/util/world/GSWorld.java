package com.gms.paper.util.world;

import com.destroystokyo.paper.exception.ServerException;
import com.gms.paper.Main;
import com.gms.paper.data.GamePosition;
import com.gms.paper.interact.puzzles.maths.Arithmetic;
import com.gms.paper.util.Log;
import com.gms.paper.util.Vector3D;
import com.gms.paper.util.blocks.GSSign;
import lombok.Getter;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntitySign;
import org.bukkit.*;
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
    @Getter
    public @NotNull File folder;


    public GSWorld(String name) throws ServerException {
        this(Objects.requireNonNull(loadWorld(name, true)).bukkitWorld);
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
        this.folder = this.bukkitWorld.getWorldFolder();
    }

    public TileEntity getBlockEntity(Location location) {
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        return this.nmsWorld.getBlockEntity(blockPosition, true);
    }

    public TileEntity getBlockEntity(GamePosition gamePosition) {
        BlockPosition blockPosition = new BlockPosition(gamePosition.x, gamePosition.y, gamePosition.z);
        return this.nmsWorld.getBlockEntity(blockPosition, true);
    }

    public TileEntity getBlockEntity(Vector3D vector3D) {
        BlockPosition blockPosition = new BlockPosition(vector3D.x, vector3D.y, vector3D.z);
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

    public static GSWorld loadWorld(String name) throws ServerException {
        return loadWorld(name, false);
    }

    public static GSWorld loadWorld(String name, boolean initialize) throws ServerException {
        WorldCreator wc = new WorldCreator(name);
        GSWorld newWorld = new GSWorld(Bukkit.createWorld(wc));
        return newWorld.load(initialize);
    }

    public GSWorld load() throws ServerException {
        return this.load(false);
    }

    public GSWorld load(boolean initialize) throws ServerException {
        return this.load(initialize, false);
    }

    public GSWorld load(boolean initialize, boolean force) throws ServerException {
        if (Objects.equals(this.name.trim(), "")) {
            throw new ServerException("Invalid empty level name");
        } else if (this.isLoaded() && !force) {
            Log.info("GSWorld " + this.name + " already loaded");
            return null;
        } else if (!this.isGenerated()) {
            Log.warn("GSWorld not found for name " + this.name);
            return null;
        } else {
            WorldCreator wc = new WorldCreator(this.name);
            GSWorld newWorld = new GSWorld(Main.s_plugin.getServer().createWorld(wc));
            if (newWorld.bukkitWorld == null) {
                Log.error("GSWorld not found for name " + this.name + " - unknown provider");
                return null;
            } else {
                if (initialize) newWorld.initialize();
                return newWorld;
            }
        }
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

    public Vector3D getSafeSpawn() {
        return this.getSafeSpawn(null);
    }

    public Vector3D getSafeSpawn(Vector3D spawn) {

        if (spawn == null || spawn.y < 1.0D) {
            spawn = new Vector3D(this.bukkitWorld.getSpawnLocation().getX(), this.bukkitWorld.getSpawnLocation().getY(), this.bukkitWorld.getSpawnLocation().getZ());
        }

        Vector3D v = spawn.floor();
        @NotNull Chunk chunk = this.bukkitWorld.getChunkAt((int)v.x >> 4, (int)v.z >> 4);
        int x = (int)v.x & 15;
        int z = (int)v.z & 15;
        if (chunk.isLoaded()) {
            int y = (int) Arithmetic.clamp(v.y, 1.0D, 254.0D);

            org.bukkit.block.Block block;
            for(boolean wasAir = chunk.getBlock(x, y - 1, z).getType() == Material.AIR; y > 0 ; --y) {
                block = chunk.getBlock(x, y, z);
                if (block.isSolid()) {
                    if (wasAir) {
                        ++y;
                        break;
                    }
                } else {
                    wasAir = true;
                }
            }

            for(; y >= 0 && y < 255; ++y) {
                block = chunk.getBlock(x, y + 1, z);
                if (block.getType() == Material.AIR) {
                    block = chunk.getBlock(x, y, z);
                    if (block.getType() == Material.AIR) {
                        return new Vector3D(spawn.x, y == (int)spawn.y ? spawn.y : (double)y, spawn.z);
                    }
                } else {
                    ++y;
                }
            }

            v.y = y;
        }

        return new Vector3D(spawn.x, v.y, spawn.z);
    }



}
