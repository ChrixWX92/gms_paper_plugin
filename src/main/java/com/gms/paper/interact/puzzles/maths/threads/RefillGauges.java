package com.gms.paper.interact.puzzles.maths.threads;

import com.gms.mc.interact.puzzles.maths.Gauges;

import static cn.nukkit.level.Sound.CONDUIT_ACTIVATE;

public class RefillGauges extends Thread{

    Gauges puzzle;

    public RefillGauges(Gauges puzzle) {
        this.puzzle = puzzle;
    }

    public void run() {
        try{

            // Filling columns

            for (Gauges.Gauge gauge : puzzle.getGauges()) {
                puzzle.getLevel().addSound(gauge.getLocation(), CONDUIT_ACTIVATE, 1F, 1F);
                for (int i = 0 ; i < gauge.getMaxBlocks() ; i++) {
                    gauge.addBlock();
                    sleep(250);
                }
            }

        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}

