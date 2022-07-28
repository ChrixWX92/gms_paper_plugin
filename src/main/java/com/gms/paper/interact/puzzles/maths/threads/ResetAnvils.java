package com.gms.paper.interact.puzzles.maths.threads;

import cn.nukkit.math.Vector3;
import com.gms.mc.interact.puzzles.maths.Anvils;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import static cn.nukkit.level.Sound.CONDUIT_ACTIVATE;

public class ResetAnvils extends Thread{

    Anvils puzzle;

    public ResetAnvils(Anvils puzzle) {
        this.puzzle = puzzle;
    }

    public void run() {
        try{

            puzzle.getLevel().addSound(puzzle.getColumns()[0], CONDUIT_ACTIVATE, 1F, 1F);

            // Filling columns
            int index = 0;
            int[] counts = new int[0];
            switch (puzzle.topic) {
                case ADDITION -> {
                    while (counts.length < 1 || Arrays.stream(counts).sum() > 8) counts = IntStream.generate(() -> new Random().nextInt(8)).limit(puzzle.getColumns().length).toArray();
                }
                case SUBTRACTION -> { //TODO: This method doesn't currently work for >2 columns
                    while (counts.length < 1 || Arrays.stream(counts).sum() > 8 || counts[0] < counts[1]) counts = IntStream.generate(() -> new Random().nextInt(8)).limit(puzzle.getColumns().length).toArray();
                }
            }

            for (Vector3 column : puzzle.getColumns()) {
                for (int i = 0 ; i < counts[index] ; i++) {
                    puzzle.getLevel().setBlock(column, puzzle.getBlock(), true, true);
                    sleep(250);
                }
                index++;
            }

        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}

