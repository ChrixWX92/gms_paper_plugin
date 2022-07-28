package com.gms.paper.interact.puzzles.maths.threads;

import cn.nukkit.block.BlockAir;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import com.gms.mc.custom.blocks.BlockGSAnvil;
import com.gms.mc.interact.puzzles.maths.Arithmetic;

import static cn.nukkit.level.ParticleEffect.EXPLOSION_CAULDRON;

public class ClearAnvils extends Thread{

    private final Level l;
    private final Vector3 c;
    private final int f;

    public ClearAnvils(Level level, Vector3 column, int floor) {
        l = level;
        c = column;
        f = floor;
    }

    public void run() {
        try{
            boolean update = false;
            BlockAir air = new BlockAir();
            for (int i = (int) c.y; i > f; i--) {
                if (l.getBlock((int) c.x, i, (int) c.z) instanceof BlockGSAnvil) {
                    if (i == (f+1)) {update = true;}
                    while(!l.setBlock(new Vector3(c.x, i, c.z), air,true,update)){
                        Arithmetic.class.wait();
                    }
                    l.addParticleEffect(new Vector3(c.x, i, c.z),EXPLOSION_CAULDRON);
                }
            }
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }
}


