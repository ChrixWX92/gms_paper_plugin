package com.gms.paper.interact.puzzles.maths.threads;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import com.gms.mc.interact.puzzles.maths.Arithmetic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddPillarObjects extends Thread{

    private final Level l;
    private final Vector3 c;
    private final Block b;
    private final int f;
    private final int i;
    private final Boolean e;
    CompoundTag cachedTag;
    private static List<Vector3> blockLocs;
    private static Set<Vector3> allBlockLocs = new HashSet<>();

    public AddPillarObjects(Level level, Vector3 column, Block block, int face, int iteration, Boolean empty) {
        l = level;
        c = column;
        b = block;
        f = face;
        i = iteration;
        e = empty;
        blockLocs = new ArrayList<>();
    }

    public void run() {
        if(e){
            BlockAir a = new BlockAir();
            switch (f) {
                case 0 -> l.setBlock(c.add(1, i), a, true, true);
                case 1 -> l.setBlock(c.add(-1, i), a, true, true);
                case 2 -> l.setBlock(c.add(0, i, 1), a, true, true);
                case 3 -> l.setBlock(c.add(0, i, -1), a, true, true);
            }
        } else {
            switch (f) {
                case 0 -> l.setBlock(c.add(1, i), b, true, true);
                case 1 -> l.setBlock(c.add(-1, i), b, true, true);
                case 2 -> l.setBlock(c.add(0, i, 1), b, true, true);
                case 3 -> l.setBlock(c.add(0, i, -1), b, true, true);
            }
            Vector3 blockVector = new Vector3(b.x, b.y, b.z);
            blockLocs.add(blockVector);
            if (Arithmetic.getCachedTag() != null) {
                cachedTag = Arithmetic.getCachedTag();
                cachedTag.putInt("x", (int) b.x).putInt("y", (int) b.y).putInt("z", (int) b.z);
                Arithmetic.setCachedTag(cachedTag);
            }
        }
    }

    public static void setBlockLocs(List<Vector3> blockLocs) {
        AddPillarObjects.blockLocs = blockLocs;
    }

    public static List<Vector3> getBlockLocs() {
        return blockLocs;
    }

    public static void setAllBlockLocs(Set<Vector3> allBlockLocs) {
        AddPillarObjects.allBlockLocs = allBlockLocs;
    }

    public static Set<Vector3> getAllBlockLocs() {
        return allBlockLocs;
    }

    public static void resetBlockLoc(){
        blockLocs = new ArrayList<>();
        allBlockLocs = new HashSet<>();
    }
}
