package com.gms.paper.interact.puzzles.maths.threads;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.Sound;
import com.gms.mc.custom.entities.ai.Motion;
import com.gms.mc.interact.puzzles.PuzzleType;
import com.gms.mc.interact.puzzles.maths.Arithmetic;
import com.gms.mc.interact.puzzles.maths.Farm;

import java.util.concurrent.ThreadLocalRandom;

public class EntityMovement extends Thread {

    private final Entity entity;

    public EntityMovement(Entity entity) {
        this.entity = entity;
        this.start();
    }

    public void run() { //TODO: Below only accounts for specific FARM puzzle jumping farmer
        if (Arithmetic.currentPuzzle.getPuzzleType() != PuzzleType.FARM) {
            return;
        }
        while (((Farm) Arithmetic.currentPuzzle).isFinalQuestion()) {
            Motion.jump(entity);
            entity.getLevel().addSound(entity.getLocation(), Sound.MOB_VILLAGER_YES);
            int wait = ThreadLocalRandom.current().nextInt(1500, 3250);
            synchronized (this) {
                try {
                    this.wait(wait);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


