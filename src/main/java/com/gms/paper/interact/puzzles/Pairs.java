package com.gms.paper.interact.puzzles;

import cn.nukkit.Player;
import cn.nukkit.block.*;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.item.ItemMap;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.*;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.utils.TextFormat;
import com.gms.mc.Main;
import com.gms.mc.error.InvalidFrameWriteException;
import com.gms.mc.interact.puzzles.handlers.anchors.AnchorHandler;
import com.gms.mc.interact.puzzles.listeners.FrameClickHandling;
import com.gms.mc.interact.puzzles.maths.Arithmetic;
import com.gms.mc.interact.puzzles.maths.threads.AddPillarObjects;
import com.gms.mc.util.Helper;
import com.gms.mc.util.Log;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.gms.mc.interact.puzzles.PuzzleType.*;
import static com.gms.mc.interact.puzzles.PuzzleType.PAIRS;

public class Pairs {

    private static int toSolve = 0;
    private static int solved = 0;

    public Pairs(){}

    /**
     * /// PAIRS Structure ///
     *
     * Matching answers are stored in a String array in an <Integer, String[]> HashMap. Their text is stored as a String
     * tag, named after the puzzle, in the BlockEntityItemFrame. Their keys are stored, similarly, as an Integer tag
     * named "Pair". Each BlockEntityItemFrame also contains a "Flipped" boolean tag to determine its facing. The String
     * task mostly exists to inform the handler on the text the entity represents when generating its image.
     *
     * While the "Flipped" tag is true FrameClickHandling will allow for one more flip before turning all Pairs over.
     * The two flipped tiles' Pair tags are checked. If they are equal then the question has been answered correctly and
     * the pair tints and solidifies. Otherwise, they return to being face-down.
     *
     */


    /**
     *
     *
     * @param
     * @param
     * @param
     * @param
     * @param
     * @throws InvalidFrameWriteException
     * @throws InterruptedException
     */
    public static void generatePairs(Player p, Location buttonLoc, String facing, HashMap<Integer, String[]> pairs, int apothem) throws InvalidFrameWriteException, InterruptedException {

        Level level = p.getLevel();
        Location base = new Location();
        BlockPlanks block = new BlockPlanks();
        int[] size = calculateGrid(pairs.size()*2);
        FrameClickHandling.setFlipped(false);
        toSolve = 0;
        solved = 0;

        AddPillarObjects.resetBlockLoc();

        Arithmetic.makeSubmit(buttonLoc, facing);

        // Establishing base
        switch (facing.toUpperCase()) {
            case "N" -> base = buttonLoc.add(0, -1,7);
            case "E" -> base = buttonLoc.add(-7,-1);
            case "S" -> base = buttonLoc.add(0, -1,-7);
            case "W" -> base = buttonLoc.add(7, -1);
        }

        //Building rows
        List<BlockEntityItemFrame> pairsFrames = new ArrayList<>();
        ItemMap back = new ItemMap();
        BufferedImage image = null;
        try {
            File filePath = new File(Helper.getImagesDir().toFile(), "back.png");
            image = ImageIO.read(filePath);
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
        back.setImage(image);
        int counter = 0;
        int subCounter = 0;
        Vector3[] columnLocs = new Vector3[size[0]];
        for (Map.Entry<Integer, String[]> pair : pairs.entrySet()) {
            toSolve++;
            Location place;
            if (counter < size[1]) {
                for (int width = size[0]; width > 0; width--) {
                    switch (facing.toUpperCase()) {
                        case "N" -> place = base.add((size[0] - width), counter);
                        case "E" -> place = base.add(0, counter, (size[0] - width));
                        case "S" -> place = base.add((-size[0] + width), counter);
                        case "W" -> place = base.add(0, counter, (-size[0] + width));
                        default -> {
                            Log.error(TextFormat.RED + "Invalid direction specified. Please choose " + "§f'N'§c, §f'E'§c, §f'S'§c, or §f'W'§c.");
                            throw new IllegalStateException("Unexpected value: " + facing);
                        }
                    }
                    level.setBlock(place, block);
                    Vector3 columnLoc = place.add(0, -1);
                    if (place.y == base.y + 1 && subCounter < columnLocs.length/*&& !alternator*/) {
                        columnLocs[subCounter] = columnLoc;
                        subCounter++;
                    }
                }
            }
            counter++;
        }

        //Adding item frames
        int facingInt;
        switch (facing.toUpperCase()) {
            case "N" -> facingInt = 3;
            case "E" -> facingInt = 0;
            case "S" -> facingInt = 2;
            case "W" -> facingInt = 1;
            default -> throw new IllegalStateException("Unexpected value: " + facing);
        }

        //clean up any old entities in the level. Can happen with generating puzzles again and again
        BackendUtils.cleanUpEntitieswithTag(p, BackendUtils.getQuestionSetID());

        AtomicReference<List<BlockEntityItemFrame>> newPairsFrames = new AtomicReference<>();

        ExecutorService es = Executors.newCachedThreadPool();
        es.execute(() -> {
            try {
                newPairsFrames.set(Arithmetic.applyItemFrames(level, columnLocs, facingInt, BackendUtils.getQuestionSetID(), size[1], false, false, false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        es.shutdown();
        boolean finished = es.awaitTermination(5, TimeUnit.SECONDS);

        new NukkitRunnable() {
            @Override
            public void run () {

                List<Vector3> frameLocs = new ArrayList<>(AddPillarObjects.getAllBlockLocs());
                int sizeGet = frameLocs.size();
                int counter = 0;
                if (!frameLocs.isEmpty()) {
                    Set<Integer> usedIndices = new HashSet<>();

                    //cycle through all pair values
                    for (Map.Entry<Integer, String[]> pair : pairs.entrySet()) {
                        for (String answer : pair.getValue()) {
                            int randomInd = ThreadLocalRandom.current().nextInt(0, sizeGet - 1);
                            while (usedIndices.contains(randomInd)) {
                                //Log.debug(usedIndices.toString() + randomInd);
                                randomInd = ThreadLocalRandom.current().nextInt(0, sizeGet - 1);  //TODO: Still causing problems
                            }

                            usedIndices.add(randomInd);

                            BlockItemFrame frameBlock = (BlockItemFrame) level.getBlock(frameLocs.get(randomInd));
                            CompoundTag contents = (new CompoundTag())
                                    .putString("id", "ItemFrame")
                                    .putByte("ItemRotation", 0)
                                    .putFloat("ItemDropChance", 0F)
                                    .putString(BackendUtils.getQuestionSetID(), answer)
                                    .putBoolean("Flipped", false)
                                    .putInt("Pair", counter)
                                    .putCompound("Item", NBTIO.putItemHelper(Objects.requireNonNull(back)))
                                    .putInt("x", (int) frameBlock.x).putInt("y", (int) frameBlock.y).putInt("z", (int) frameBlock.z);

                            BlockEntityItemFrame newFrame = newPairsFrames.get().get(randomInd);
                            newFrame.namedTag = contents;
                            newFrame.setItem(back);

                            Log.debug(String.format("onclick: [%s] [%s], Address [%s]", newFrame.namedTag.getBoolean("Flipped"), newFrame.getId(), newFrame));
                        }
                        counter++;
                    }

                    try {
                        FrameClickHandling.resetPairsFrames(p, BackendUtils.getQuestionSetID());
                    } catch (InvalidFrameWriteException e) {
                        e.printStackTrace();
                    }

                }

            }
        }.runTaskLater(Main.s_plugin, 20); //TODO ENTITY WILL NOT RELIABLY ALIGN WITH SIGN (WE THINK) FIX!

        AnchorHandler.placeAnchor(p, level, buttonLoc, apothem, "");

        BackendUtils.setPuzzleType(PAIRS);
        FrameClickHandling.setFlipped(false);
        FrameClickHandling.setInitialReset(true);
        FrameClickHandling.setCachedID(9223372036854775807L);
        Log.logGeneric(p, TextFormat.AQUA + "PAIRS " + TextFormat.GREEN + "puzzle successfully generated." + AddPillarObjects.getAllBlockLocs());
    }

    /**
     * Dedicated method for solving PAIRS puzzles.
     * Searches for objects and a count value, then compares them.
     * @param p - The Player object
     * @param questionSet - The question set ID to be used for this PAIRS puzzle
     */
    public static boolean solvePairs(Player p, String questionSet) {

        if (BackendUtils.getPuzzleType() == PAIRS) {
            Arithmetic.puzzleTeleport(p, questionSet);
            return true;
        }
        return false;
    }

    public static void initializePairs(int toSolve) {

        FrameClickHandling.setFlipped(false);
        Pairs.toSolve = toSolve;
        solved = 0;
        BackendUtils.setPuzzleType(PAIRS);
        FrameClickHandling.setFlipped(false);
        FrameClickHandling.setInitialReset(true);
        FrameClickHandling.setCachedID(9223372036854775807L);

    }

    public static void resetPairsData() {
        FrameClickHandling.setFlipped(false);
        Pairs.setToSolve(0);
        Pairs.setSolved(0);
        FrameClickHandling.setFlipped(false);
        FrameClickHandling.setInitialReset(true);
        FrameClickHandling.setCachedID(9223372036854775807L);
    }

    private static int[] calculateGrid(int size){
        double sqrt = Math.sqrt(size);
        int truncated = (int) Math.floor(sqrt);
        if ((sqrt - truncated) == 0) {
            return new int[]{(int)sqrt, (int)sqrt};
        }
        else {
            if (size % truncated == 0) {
                return new int[]{(size/truncated), truncated};
            } else {
                while (size % truncated != 0) {
                    truncated--;
                }
                return new int[]{(size/truncated), truncated};
            }
        }
    }

    public static int getToSolve() {
        return toSolve;
    }

    public static void setToSolve(int toSolve) {
        Pairs.toSolve = toSolve;
    }

    public static int getSolved() {
        return solved;
    }

    public static void setSolved(int solved) {
        Pairs.solved = solved;
    }
}
