package com.gms.paper.util;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.*;
import gt.creeperface.holograms.Hologram;
import gt.creeperface.holograms.Holograms;
import gt.creeperface.holograms.entity.HologramEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HologramHelper {

    public static Map<HologramEntity, Hologram> spawnBasicHologram(Player player, String name, String text, CompoundTag nbt) {

        Map<HologramEntity, Hologram> wholeHolo = new HashMap<>();

        List<List<String>> listList = new ArrayList<>();
        List<String> list = new ArrayList<>();
        list.add(text);
        listList.add(list);

        Hologram hologram = new Hologram(name, listList, new Hologram.GridSettings());
        HologramEntity entity = new HologramEntity(player.getChunk(), nbt, hologram);
        Holograms.getInstance().getInternalHolograms().putIfAbsent(entity.getHologramId(), hologram);
        entity.setNameTag(name);
        entity.namedTag.putString("holoText", text);
        hologram.spawnEntity(entity);
        //Holograms.getInstance().reloadHolograms();
        wholeHolo.put(entity, hologram);
        Holograms.getInstance().getInternalHolograms().putIfAbsent(entity.getHologramId(), hologram);
        entity.spawnToAll();
        Holograms.getInstance().editors.put(player.getId(), entity);

        return wholeHolo;

    }

    /**
     * Returns a single HologramEntity object with a given name, if one exists.
     * @param level - The level to search.
     * @param name - The name of the HologramEntity to be found.
     * @return - A HologramEntity object with a name equal to the value passed in the name parameter, if one exists.
     */
    public static HologramEntity getHologramEntity(Level level, String name) {

        Map<Long, ? extends FullChunk> chunksMap = level.getChunks();

        Map<Long, Entity> entMap;

        for (var entry : chunksMap.entrySet()) {
            entMap = entry.getValue().getEntities();
            for (var chunkEntry : entMap.entrySet()) {
                if (chunkEntry.getValue() instanceof HologramEntity he) {
                    if (he.getName().equals(name)) return he;
                }
            }
        }

        return null;
    }

    /**
     * Returns a Map<Long, HologramEntity>, containing all HologramEntity objects in the level.
     * @param level - The level to search.
     */
    public static Map<Long, HologramEntity> getHologramEntities(Level level) {

        Map<Long, ? extends FullChunk> chunksMap = level.getChunks();

        Map<Long, Entity> entMap;

        Map<Long, HologramEntity> hologramMap = new HashMap<>();

        for (var entry : chunksMap.entrySet()) {
            entMap = entry.getValue().getEntities();
            for (var chunkEntry : entMap.entrySet()) {
                if (chunkEntry.getValue() instanceof HologramEntity he) {
                    hologramMap.put(chunkEntry.getKey(), he);
                }
            }
        }

        return hologramMap;
    }

    public static CompoundTag hologramNBT(Player p, Vector3 location) {
        return new CompoundTag()
                .putList(new ListTag<>("Pos")
                        .add(new DoubleTag("0", location.x))
                        .add(new DoubleTag("1", location.y))
                        .add(new DoubleTag("2", location.z)))
                .putList(new ListTag<DoubleTag>("Motion")
                        .add(new DoubleTag("0", 0))
                        .add(new DoubleTag("1", 0))
                        .add(new DoubleTag("2", 0)))
                .putList(new ListTag<FloatTag>("Rotation")
                        .add(new FloatTag("0", (float) p.getYaw()))
                        .add(new FloatTag("1", (float) p.getPitch())));

    }

    //update text by creating a new hologram
    public static void updateHologramText(Player player, HologramEntity hologramEntity, String text){
        Hologram hologram = hologramEntity.getHologram();
        hologramEntity.closeHologram();

        HologramHelper.spawnBasicHologram(player, hologram.getName(), text, hologramEntity.namedTag);

    }

    public static Vector3 getHologramLocation(HologramEntity hologramEntity){
        if(hologramEntity == null){
            return null;
        }

        ListTag position = hologramEntity.namedTag.getList("Pos");
        DoubleTag x = (DoubleTag) position.get(0);
        DoubleTag y = (DoubleTag) position.get(1);
        DoubleTag z = (DoubleTag) position.get(2);
        Vector3 holoLocation =  new Vector3(x.getData(), y.getData(), z.getData());
        return holoLocation;
    }
}
