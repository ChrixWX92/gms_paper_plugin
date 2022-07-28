package com.gms.paper.interact.puzzles.maths;


import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.block.BlockConcretePowder;
import cn.nukkit.block.BlockFallable;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.NukkitRunnable;
import com.gms.mc.Main;
import com.gms.mc.custom.particles.ParticleFX;
import com.gms.mc.custom.particles.ParticleFXSequence;
import com.gms.mc.custom.sound.Chord;
import com.gms.mc.custom.sound.ChordType;
import com.gms.mc.custom.sound.MusicMaker;
import com.gms.mc.custom.sound.Note;
import com.gms.mc.error.InvalidFrameWriteException;
import com.gms.mc.interact.puzzles.MathsPuzzle;
import com.gms.mc.interact.puzzles.MathsTopic;
import com.gms.mc.interact.puzzles.Resettable;
import com.gms.mc.interact.puzzles.maths.threads.RefillGauges;
import com.gms.mc.util.Log;
import io.netty.util.internal.ThreadLocalRandom;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static cn.nukkit.level.Sound.NOTE_HARP;
import static com.gms.mc.interact.puzzles.PuzzleType.GAUGES;

public class Gauges extends MathsPuzzle implements Resettable {


    public class Gauge {
        int id;
        String puzzleName;
        Vector3 location; // Coordinates of base block
        Level level;
        Vector3 apex;
        int colour;
        BlockFallable block;
        List<BlockFallable> blocks;
        int maxBlocks;
        int target;

        Gauge(String gaugesID, int id, BlockFallable block, int maxBlocks) {
            this.id = id;
            this.puzzleName = GAUGES.abbreviation + gaugesID;
            Position signLoc = Arithmetic.findSignByText(level, GAUGES.abbreviation, String.valueOf(gaugesID), String.valueOf(id));
            if (signLoc == null) {
                Log.error("No location sign found for Gauge " + id + "in puzzle " + puzzleName);
                return;
            }
            this.location = signLoc.add(0, +1);
            this.level = signLoc.getLevel();
            this.apex = this.location.add(0, this.maxBlocks);
            this.block = block;
            this.colour = this.block.getDamage();
            this.target = ThreadLocalRandom.current().nextInt(0, this.maxBlocks + 1);
        }

        public void addBlock() {
            this.level.setBlock(this.apex, this.block, true, true);
            //TODO: (S)FX
        }

        public void subtractBlock() {
            this.level.setBlock(this.getLocation().add(0,1), new BlockAir(), true, true);
            //TODO: (S)FX
        }

        int checkLevel() {
            for (int i = 1 ; i < this.maxBlocks ; i++) {
                Block block = this.level.getBlock(this.location.add(0, i));
                if (block.getId() == this.block.getId() && block instanceof BlockFallable validBlock) {
                    this.blocks.add(validBlock);
                }
            }
            return this.countLevel();
        }

        private int countLevel() {
            int level = 0;
            for (BlockFallable ignored : this.blocks){
                level++;
            }
            return level;
        }

        public int getMaxBlocks() {
            return maxBlocks;
        }

        public Vector3 getLocation() {
            return location;
        }

    }


    private int challengeAmount;
    private List<Gauge> gauges;
    //Colour order = yellow, green, red, blue...
    int[] colours = new int[]{4, 5, 14, 3};

    public Gauges(Player player, String id, MathsTopic topic) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, InterruptedException {
        super(player, GAUGES, id, topic);

        BlockFallable gaugeBlock;

        switch (this.name) {
            case "GGS1" -> {
                this.challengeAmount = 4;
                gaugeBlock = new BlockConcretePowder();
            }
            default -> gaugeBlock = new BlockConcretePowder();

        }
        for (int i = 0 ; i < this.challengeAmount ; i++) {
            gaugeBlock = gaugeBlock.getClass().getConstructor().newInstance();
            gaugeBlock.setDamage(colours[i]);
            this.gauges.add(i, new Gauge(this.id, i, gaugeBlock, 6)); //TODO: Height should be specified on a gauge-by-gauge basis in the backend
        }

        RefillGauges refill = new RefillGauges(this);
        refill.start();
        refill.join();

        for (Gauge gauge : this.gauges) gauge.checkLevel();

    }


    @Override
    public void reset() throws InvalidFrameWriteException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Gauges newGauges = new Gauges(this.player, this.id, this.topic);
        Arithmetic.currentPuzzle = newGauges;
        MATHS_InteractionHandler.reinitializePuzzle(newGauges);
    }

    @Override
    public boolean solve() throws InvalidFrameWriteException, InterruptedException, CloneNotSupportedException {
        for (Gauge gauge : this.gauges) {
            if (gauge.checkLevel() != gauge.target) {
                return false;
            }
        }
        return puzzleCorrect();

    }

    private boolean puzzleCorrect() {
        Arithmetic.mark(this.player, true, "", false);
        Chord correct1 = new Chord(Note.C4, ChordType.MAJ, 1, false);
        Chord correct2 = new Chord(Note.C5, ChordType.MAJ, 1, false);
        Chord[] chords = new Chord[]{correct1, correct2};
        MusicMaker.playArpeggio(this.player, chords, 105, NOTE_HARP);
        new NukkitRunnable() {
            @Override
            public void run() {
                try {
                    reset();
                } catch (InvalidFrameWriteException | InterruptedException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskLater(Main.s_plugin, 60);

        for (Gauge gauge : this.gauges) {
            for (BlockFallable bf : gauge.blocks) {
                ParticleFXSequence pFX = new ParticleFXSequence(ParticleFX.COMPLETE, bf.getLevel(), bf);
                synchronized (pFX) {
                    pFX.run();
                }
            }
        }

        return true;
    }

    public List<Gauge> getGauges() {
        return this.gauges;
    }

}
