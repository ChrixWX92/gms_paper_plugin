package com.gms.paper.interact.puzzles.maths;

import com.gms.paper.custom.sound.Chord;
import com.gms.paper.custom.sound.ChordType;
import com.gms.paper.custom.sound.MusicMaker;
import com.gms.paper.custom.sound.Note;
import com.gms.paper.error.InvalidFrameWriteException;
import com.gms.paper.interact.puzzles.MathsPuzzle;
import com.gms.paper.interact.puzzles.MathsTopic;
import com.gms.paper.interact.puzzles.Resettable;
import com.gms.paper.interact.puzzles.maths.threads.FindFrames;
import com.gms.paper.util.Log;
import com.gms.paper.util.Vector3D;
import io.netty.util.internal.ThreadLocalRandom;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.IntStream;

import static com.gms.paper.interact.puzzles.PuzzleType.*;

public class Pillars extends MathsPuzzle implements Resettable {

    public Pillars(Player player, String id, MathsTopic topic){
        super(player, PILLARS, id, topic);


    }

    public class Pillar {
        int id;
        String puzzleName;
        Level level;
        Vector3D location; // Coordinates of base block
        int facing;
        Vector3D apex;
        int colour;
        BlockFallable block;
        List<BlockFallable> blocks;
        int maxBlocks;
        int target;

        Pillar(String gaugesID, int id, BlockFallable block, int maxBlocks) {
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

    }

    private List<Pillar> pillars;
    private BlockEntitySign sign;
    private int stage;
    private int[] answers;
    private static int combinedTotal = 0;

    public static synchronized void doPillars(Player p, String pillars, boolean mark) throws InterruptedException {

        Map<Integer, Block> blocks = new HashMap<>();
        Map<Integer, Vector3> positions = new HashMap<>();
        int defaultFace;
        this.stage = 1;
        this.answers(new int[2]); // Size == # of questions
        setPillarsColour(0);

        this.sign = Arithmetic.findSignByTag(p,pillars);

        /** ### Face Directions (I don't make the rules): ###
         * 0 = East (+X)
         * 1 = West (-X)
         * 2 = North (-Z)
         * 3 = South (+Z)
         */

        /** ### .setDamage Color IDs: ###
         * 0 = Plain
         * 1 = Orange
         * 2 = Magenta
         * 3 = Light Blue
         * 4 = Yellow
         * 5 = Lime
         * 6 = Pink
         * 7 = Dark Grey
         * 8 = Light Grey
         * 9 = Cyan
         * 10 = Purple
         * 11 = Blue
         * 12 = Brown
         * 13 = Green
         * 14 = Red
         * 15 = Black
         * 16 = White
         * Anything higher turns it to Concrete Powder (in appearance only)
         */

        switch (pillars) {
            case "Cakes1" -> {


                BlockConcrete pillarBlock = new BlockConcrete();
                pillarBlock.setDamage(14);
                this.pillars.add(i, new Pillar(this.id, i, pillarBlock, 8)); //TODO: Height should be specified on a gauge-by-gauge basis in the backend

                BlockConcrete pillarBlock = new BlockConcrete();
                pillarBlock.setDamage(3);
                this.pillars.add(i, new Pillar(this.id, i, pillarBlock, 8));

                BlockConcrete pillarBlock = new BlockConcrete();
                pillarBlock.setDamage(4);
                this.pillars.add(i, new Pillar(this.id, i, pillarBlock, 8));

                pillarsColourRand(blocks.keySet());
                getPillarsSolutions()[0] = getPillarsColour();
                getPillarsSolutions()[1] = getPillarsColour();
                while (getPillarsSolutions()[0] == getPillarsSolutions()[1]) {
                    pillarsColourRand(blocks.keySet());
                    getPillarsSolutions()[1] = getPillarsColour();
                }

                //Updating titles and signs
                updatePillars(p, pillars, mark);

                level.addSound(p.getPosition(), BLOCK_STONECUTTER_USE, 1F, 0.7F);

                //Deleting any pre-existing item frames
                while (!Arithmetic.deleteFrameEntities(level,pillars)){
                    Arithmetic.class.wait(); //TODO: Delete this?
                }

                //Building our pillars
                while (!buildPillars(level,blocks,positions,3,10)){
                    Arithmetic.class.wait(); //TODO: Delete this?
                }

                defaultFace = 1;
                //Adding item frames to our pillars
                Arithmetic.applyItemFrames(level, positions, defaultFace, pillars,10, true);

            }
        }

    }

    public static synchronized boolean buildPillars(Level l, Map<Integer, Block> b, Map<Integer, Vector3> p, int min, int max) {

        Set<Integer> colors = b.keySet();
        Block air = new BlockAir();
        int[] solutionSizes = new int[getPillarsSolutions().length];
        while (IntStream.of(solutionSizes).sum() > 9 || IntStream.of(solutionSizes).sum() < 1) { //TODO: Somehow, this method doesn't seem to produce any integers with a sum greater than 5
            for (int i = 0; i < getPillarsSolutions().length; i++) {
                solutionSizes[i] = ThreadLocalRandom.current().nextInt(min, (max + 1));
            }
        }
        HashMap<Integer, Integer> pairedValues = new HashMap<>();
        for (int i = 0; i < getPillarsSolutions().length; i++) {
            pairedValues.put(getPillarsSolutions()[i], solutionSizes[i]);
        }
        
        for (var entry : p.entrySet()) {
            Vector3 pe = entry.getValue();
            for (int i = 0; i < 10; i++){
                l.setBlock(pe.add(0,i),air, true, true);
                l.setBlock(pe.add(0, i, 1), air,true,true);
                l.setBlock(pe.add(0, i, -1), air,true,true);
                l.setBlock(pe.add(-1, i), air,true,true);
                l.setBlock(pe.add(1, i), air,true,true);
            }
        }

        for (Integer color : colors) {
            int size;
            if (Arrays.stream(getPillarsSolutions()).anyMatch(color::equals)) {
                size = pairedValues.get(color);
            } else {
                size = ThreadLocalRandom.current().nextInt(min, (max+1));
            }
            Block block = b.get(color);
            Vector3 position = p.get(color);
            for (int i = 0; i < size; i++){
                l.setBlock(position.add(0,i),block);
            }
        }
        return true;
    }



    public static void updatePillars(Player p, String pillars, boolean mark) {
        Level level = p.level;


        if (this.stage > 1 && this.stage <= this.answers.length) {
            Set<Integer> oldPillarsColour = new HashSet<>();
            oldPillarsColour.add(getPillarsColour());
            while (oldPillarsColour.contains(getPillarsColour())) {pillarsColourRand(getPillarBlocks().keySet());}
        } else  {
            pillarsColourRand(getPillarBlocks().keySet());
        }
        if (this.stage <= this.answers.length) {setPillarsColour(getPillarsSolutions()[this.stage-1]);}

        level.addParticleEffect(new Vector3(Objects.requireNonNull(this.sign).add(0.5).x, Objects.requireNonNull(this.sign).add(0,0.5).y, Objects.requireNonNull(this.sign).add(0,0,0.5).z),LLAMA_SPIT);

        String task;

        if (getPillarsSolutionStep() < 3) {

            switch (getPillarsColour()) {

                case 3 -> {
                    this.sign.setText("§fHow many", "§fcakes are on", "§fthe §b§lBLUE", "§r§fpillar?");
                    task = ("§fHow many §fcakes are" + "\n" + "§f on the §b§lBLUE §r§fpillar?");
                    if (mark) {
                        Arithmetic.mark(p, true, task, false);
                        pillarsMarkNoise(p);
                        Arithmetic.puzzleName = pillars;
                    } else {
                        p.sendTitle(task, "", 20, 33, 20);
                    }

                }
                case 4 -> {
                    this.sign.setText("§fHow many", "§fcakes are on", "§fthe §e§lYELLOW", "§r§fpillar?");
                    task = ("§fHow many §fcakes are" + "\n" + "§f on the §e§lYELLOW §r§fpillar?");
                    if (mark) {
                        Arithmetic.mark(p, true, task, false);
                        pillarsMarkNoise(p);
                        Arithmetic.puzzleName = pillars;
                    } else {
                        p.sendTitle(task, "", 20, 33, 20);
                    }
                }
                case 14 -> {
                    this.sign.setText("§fHow many", "§fcakes are on", "§fthe §c§lRED", "§r§fpillar?");
                    task = ("§fHow many §fcakes are" + "\n" + "§f on the §c§lRED §r§fpillar?");
                    if (mark) {
                        Arithmetic.mark(p, true, task, false);
                        pillarsMarkNoise(p);
                        Arithmetic.puzzleName = pillars;
                    } else {
                        p.sendTitle(task, "", 20, 33, 20);
                    }
                }
                default -> throw new IllegalStateException();

            }
        } else {
            String pillar1;
            String pillar2;
            switch (getPillarsSolutions()[0]) {
                case 3 -> pillar1 = "§b§lBLUE";
                case 4 -> pillar1 = "§e§lYELLOW";
                case 14 -> pillar1 = "§c§lRED";
                default -> throw new IllegalStateException("Unexpected value: " + getPillarsSolutions()[0]);
            }
            switch (getPillarsSolutions()[1]) {
                case 3 -> pillar2 = "§b§lBLUE";
                case 4 -> pillar2 = "§e§lYELLOW";
                case 14 -> pillar2 = "§c§lRED";
                default -> throw new IllegalStateException("Unexpected value: " + getPillarsSolutions()[1]);
            }
            this.sign.setText("§fHow many", "§fcakes are on", pillar1 + "§r§f and", pillar2 + "§r§f?");
            task = ("§fHow many §fcakes are" + "\n" + "§f on the " + pillar1 + "§r§f and " + pillar2 + "\n§r§fpillars combined?");
            if (mark) {
                pillarsMarkNoise(p);
                Arithmetic.mark(p, true, task, false);
                Arithmetic.puzzleName = pillars;
            } else {
                p.sendTitle(task, "", 20, 33, 20);
            }
        }
    }

    /**
     * Dedicated method for solving PILLARS puzzles.
     * Searches for objects and a count value, then compares them.
     * @param p - The Player object
     * @param pillars - The name of the PILLARS puzzle (e.g. "Pillars1")
     */
    public static boolean solvePillars(Player p, String pillars, String value) throws InterruptedException {

        int valInt = Integer.parseInt(value);
        int lbPillar = 0; //ID = 3
        int yPillar = 0; //ID = 4
        int rPillar = 0; //ID = 14

        if (Arithmetic.puzzleName.equals(pillars)) {
            switch (pillars) {
                case ("Cakes1") -> {

                    FindFrames ff = new FindFrames(p,pillars);
                    Thread t = new Thread(ff);
                    t.start();
                    t.join();
                    Map<Long, BlockEntityItemFrame> cakes = FindFrames.getMap();

                    for (Map.Entry<Long, BlockEntityItemFrame> frame : cakes.entrySet()) {
                        switch (frame.getValue().namedTag.getInt(pillars)) {
                            case 3 -> lbPillar++;
                            case 4 -> yPillar++;
                            case 14 -> rPillar++;
                        }
                    }

                    int chosenPillar;
                    Pillar chosenPillar = 

                    switch (getPillarsColour()) {
                        case 3 -> chosenPillar = lbPillar;
                        case 4 -> chosenPillar = yPillar;
                        case 14 -> chosenPillar = rPillar;
                        default -> throw new IllegalStateException("Unexpected value: " + getPillarsColour());
                    }

                    switch (this.stage) {
                        case 1, 2 -> {
                            if (chosenPillar == valInt) {
                                this.stage = this.stage+1;
                                updatePillars(p,pillars,true);
                            } else {
                                Arithmetic.mark(p, false);
                                return false;
                            }
                        }
                        case 3 -> {
                            int total = 0;
                            for (int i = 0 ; i < getPillarsSolutions().length ; i++){
                                switch (getPillarsSolutions()[i]) {
                                    case 3 -> total += lbPillar;
                                    case 4 -> total += yPillar;
                                    case 14 -> total += rPillar;
                                    default -> throw new IllegalStateException("Unexpected value: " + getPillarsSolutions()[i]);
                                }
                            }
                            if (total == valInt) {
                                doPillars(p,pillars,true);
                                return true;
                            } else {
                                Arithmetic.mark(p, false);
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void pillarsColourRand(Set<Integer> blockIDs){
        int item = new Random().nextInt(blockIDs.size());
        int i = 0;
        for (int colour : blockIDs) {
            if (i == item) {
                setPillarsColour(colour);
            }
            i++;
        }
    }

    private void pillarsMarkNoise(){
        switch (this.stage){
            case 1: {
                Chord correct = new Chord(Note.C5, ChordType.MAJ, 1, false);
                MusicMaker.playArpeggio(this.player, correct, 105, NOTE_HARP);
                break;
            }
            case 3: MusicMaker.playNote(this.player, Note.G5, NOTE_HARP, 0.75F);
            case 2: MusicMaker.playNote(this.player, Note.E5, NOTE_HARP, 0.75F);
        }
    }

    @Override
    public void reset() throws InvalidFrameWriteException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

    }

    @Override
    public boolean solve() throws InvalidFrameWriteException, InterruptedException, CloneNotSupportedException {
        return false;
    }
}
