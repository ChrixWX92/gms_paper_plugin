package com.gms.paper.interact.puzzles.maths;

import com.gms.paper.custom.items.ItemGSDye;
import com.gms.paper.custom.sound.*;
import com.gms.paper.error.InvalidFrameWriteException;
import com.gms.paper.interact.puzzles.Puzzle;
import com.gms.paper.interact.puzzles.PuzzleType;
import com.gms.paper.interact.puzzles.ResetPuzzles;
import com.gms.paper.interact.puzzles.handlers.anchors.AnchorHandler;
import com.gms.paper.interact.puzzles.maths.threads.AddPillarObjects;
import com.gms.paper.interact.puzzles.utils.TextToImage;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import com.gms.paper.util.world.GSWorld;
import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntitySign;
import net.minecraft.world.level.chunk.Chunk;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class Arithmetic {

    public static boolean solveForX = false;
    public static String puzzleName = null;
    public static int sumX;
    public static int world = 0;

    private static NBTTagCompound cachedTag;
    private static final ArrayList<String> facings = new ArrayList<>(Arrays.asList("N", "E", "S", "W"));
    public static Puzzle currentPuzzle;

    /** WORLD int:
     * 0 = Lobby? (So far - nothing)
     * 1 = Addition Zone
     * 2 = Subtraction Zone
     * 3 = Multiplication Zone
     * 4 = Division Zone
     */

    public static TileEntitySign findSignByText(Player player, String... lines) {

        World world = player.getWorld();
        return findSignByText(world, lines);

    }



    public static Set<TileEntitySign> getBlocksWithTextForChunk(Chunk chunk, String... lines) {
        var entMap = chunk.getBlockEntities();
        Set<TileEntitySign> signSet = new HashSet<>();

        if (entMap.size() > 0) {
            for (var chunkEntry : entMap.entrySet()) {
                if (chunkEntry.getValue() instanceof TileEntitySign sign) {
                    String[] signText = sign.getText();

                    if (signText != null && signText[0] != null) {
                        if (signText[0].equals(lines[0])) {
                            String[] text = sign.getText();
                            switch (lines.length) {
                                case 4 : if (!text[3].equals(lines[3])) continue;
                                case 3 : if (!text[2].equals(lines[2])) continue;
                                case 2 : if (!text[1].equals(lines[1])) continue;
                                case 1 : signSet.add((TileEntitySign) chunkEntry.getValue());
                            }
                        }
                    }
                }
            }
        }

        return signSet;
    }

    public static Set<TileEntitySign> findSignsByTextInChunks(List<Chunk> chunks, List<Chunk> unfinished, String... lines) {
        Set<TileEntitySign> signSet = new HashSet<>();

        for (var chunk : chunks) {
            boolean isUnfinished = true;

            if (chunk.isLoaded()) {
                BlockState[] entMap = chunk.getTileEntities();
                if (entMap != null) {
                    isUnfinished = false;

                    for (var chunkEntry : entMap.entrySet()) {
                        if (chunkEntry.getValue() instanceof TileEntitySign sign) {
                            if (sign.getText() != null && sign.getText()[0] != null && sign.getText()[0].equals(lines[0])) {
                                String[] text = sign.getText();
                                switch (lines.length) {
                                    case 4 : if (!text[3].equals(lines[3])) continue;
                                    case 3 : if (!text[2].equals(lines[2])) continue;
                                    case 2 : if (!text[1].equals(lines[1])) continue;
                                    case 1 : signSet.add((TileEntitySign) chunkEntry.getValue());
                                }
                            }
                        }
                    }
                }
            }

            if (isUnfinished)
                unfinished.add(chunk);
        }

        return signSet;
    }

    /***
     * For returning all signs in the level based on their text data
     * @param world the Level to be scanned for signs
     * @param lines The lines of text by which the signs will be evaluated
     * @return A set of TileEntitySigns with the level parameter, the text content of which matches the lines parameter
     */
    public static Set<TileEntitySign> findSignsByText(World world, String... lines) {

        Map<Long, ? extends Chunk> chunksMap = world.getLoadedChunks();

        Map<Long, TileEntity> entMap;
        Set<TileEntitySign> signSet = new HashSet<>();

        for (var entry : chunksMap.entrySet()) {
            entMap = entry.getValue().getBlockEntities();
            for (var chunkEntry : entMap.entrySet()) {
                if (chunkEntry.getValue() instanceof TileEntitySign sign) {

                    if (sign.getText() != null && sign.getText()[0] != null && sign.getText()[0].equals(lines[0])) {
                        String[] text = sign.getText();
                        switch (lines.length) {
                            case 4 : if (!text[3].equals(lines[3])) continue;
                            case 3 : if (!text[2].equals(lines[2])) continue;
                            case 2 : if (!text[1].equals(lines[1])) continue;
                            case 1 : signSet.add((TileEntitySign) chunkEntry.getValue());
                        }
                    }
                }
            }
        }
        return signSet;

    }

    /***
     * Returns a TileEntitySign based on its NBT data. Intended for individual signs, as it returns when conditions are met.
     * @param player The Player object
     * @param tag The nbt tag key that differentiates the TileEntitySign object
     * @return The TileEntitySign object in the player's level with an nbt tag key equal to tag
     */
    public static TileEntitySign findSignByTag(Player player, String tag) {

        World world = player.getWorld();

        Map<Long, ? extends Chunk> chunksMap = world.getLoadedChunks();

        Map<Long, BlockEntity> entMap;

        for (var entry : chunksMap.entrySet()) {
            entMap = entry.getValue().getBlockEntities();
            for (var chunkEntry : entMap.entrySet()) {
                if (chunkEntry.getValue().namedTag.contains(tag)) {
                    if (chunkEntry.getValue() instanceof TileEntitySign) {
                        return (TileEntitySign) chunkEntry.getValue();
                    }
                }

            }
        }
        return null;
    }

    /**
     *
     * @param player
     * @param sum
     * @param i
     * @param v
     * @param type
     * @throws InvalidFrameWriteException
     */
    public static void findFrame(Player player, String sum, int i, int v, int type) throws InvalidFrameWriteException {

        World world = player.getWorld();

        Map<Long, ? extends Chunk> chunksMap = world.getLoadedChunks();

        Map<Long, BlockEntity> entMap;

        for (var entry : chunksMap.entrySet()) {
            entMap = entry.getValue().getBlockEntities();
            for (var chunkEntry : entMap.entrySet()) {
                if (chunkEntry.getValue().namedTag.contains(sum)) {
                    if (chunkEntry.getValue() instanceof BlockEntityItemFrame) {
                        CompoundTag nbt = chunkEntry.getValue().namedTag;
                        if (nbt.getInt(sum) == i) {
                            writeFrame((BlockEntityItemFrame) chunkEntry.getValue(), v, type);
                        }
                    }
                }
            }
        }
    }

    public static void findFrame(Player player, String puzzle, int type) throws InvalidFrameWriteException {

        World world = player.getWorld();

        Map<Long, ? extends Chunk> chunksMap = world.getLoadedChunks();

        Map<Long, BlockEntity> entMap;

        for (var entry : chunksMap.entrySet()) {
            entMap = entry.getValue().getBlockEntities();
            for (var chunkEntry : entMap.entrySet()) {
                if (chunkEntry.getValue().namedTag.contains(puzzle)) {
                    if (chunkEntry.getValue() instanceof BlockEntityItemFrame) {
                        writeFrame((BlockEntityItemFrame) chunkEntry.getValue(), player, type);
                    }
                }

            }
        }
    }

    public static BlockEntityItemFrame getFrame(Player player, String tag, int i){

        World world = player.getWorld();

        Map<Long, ? extends Chunk> chunksMap = world.getLoadedChunks();

        Map<Long, BlockEntity> entMap;

        for (var entry : chunksMap.entrySet()) {
            entMap = entry.getValue().getBlockEntities();
            for (var chunkEntry : entMap.entrySet()) {
                if (chunkEntry.getValue().namedTag.contains(tag)) {
                    if (chunkEntry.getValue() instanceof BlockEntityItemFrame) {
                        if (chunkEntry.getValue().namedTag.getInt(tag) == i) {
                            return (BlockEntityItemFrame) chunkEntry.getValue();
                        }
                    }
                }

            }
        }
        return null;
    }

    /***
     * This one is intended for individual frames, as it returns when conditions are met
     * @param player The Player object
     * @param tag The nbt tag key that differentiates the BlockEntityItemFrame object
     * @return The BlockEntityItemFrame object in the player's level with an nbt tag key equal to tag
     */
    public static BlockEntityItemFrame getFrame(Player player, String tag) {

        World world = player.getWorld();

        Map<Long, ? extends Chunk> chunksMap = world.getLoadedChunks();

        Map<Long, BlockEntity> entMap;

        for (var entry : chunksMap.entrySet()) {
            entMap = entry.getValue().getBlockEntities();
            for (var chunkEntry : entMap.entrySet()) {
                if (chunkEntry.getValue().namedTag.contains(tag)) {
                    if (chunkEntry.getValue() instanceof BlockEntityItemFrame) {
                        return (BlockEntityItemFrame) chunkEntry.getValue();
                    }
                }

            }
        }
        return null;
    }

    /***
     * For returning all item frames in the level that contain the specified tag.
     * @param player The Player object
     * @param tag The nbt tag key that differentiates the BlockEntityItemFrame object
     * @return The BlockEntityItemFrame object in the player's level with an nbt tag key equal to tag
     */
    public static Set<BlockEntityItemFrame> getFrames(Player player, String tag) {

        World world = player.getWorld();

        Map<Long, ? extends Chunk> chunksMap = world.getLoadedChunks();

        Map<Long, BlockEntity> entMap;
        Set<BlockEntityItemFrame> frameMap = new HashSet<>();

        for (var entry : chunksMap.entrySet()) {
            entMap = entry.getValue().getBlockEntities();
            for (var chunkEntry : entMap.entrySet()) {
                if (chunkEntry.getValue().namedTag.contains(tag)) {
                    if (chunkEntry.getValue() instanceof BlockEntityItemFrame) {
                        frameMap.add((BlockEntityItemFrame) chunkEntry.getValue());
                    }
                }

            }
        }
        return frameMap;
    }

    /***
     * This one is intended for individual signs, as it returns when conditions are met
     * @param player The Player object
     * @param tag The nbt tag key that differentiates the TileEntitySign object
     * @return The TileEntitySign object in the player's level with an nbt tag key equal to tag
     */
    public static TileEntitySign getSign(Player player, String tag) {

        World world = player.getWorld();

        Map<Long, ? extends Chunk> chunksMap = world.getLoadedChunks();

        Map<Long, BlockEntity> entMap;

        for (var entry : chunksMap.entrySet()) {
            entMap = entry.getValue().getBlockEntities();
            for (var chunkEntry : entMap.entrySet()) {
                if (chunkEntry.getValue().namedTag.contains(tag)) {
                    if (chunkEntry.getValue() instanceof TileEntitySign) {
                        return (TileEntitySign) chunkEntry.getValue();
                    }
                }
            }
        }
        return null;
    }

    /***
     * Deletes all frames in the player's level that contain a matching nbt tag
     * @param player The Player object
     * @param tag The nbt tag key that differentiates the BlockEntityItemFrame object
     */
    public static synchronized void deleteFrameEntities(Player player, String tag) {

        World world = player.getWorld();

        Map<Long, ? extends Chunk> chunksMap = world.getLoadedChunks();

        Map<Long, BlockEntity> entMap;

        for (var entry : chunksMap.entrySet()) {
            entMap = entry.getValue().getBlockEntities();
            for (var chunkEntry : entMap.entrySet()) {
                if (chunkEntry.getValue().namedTag.contains(tag)) {
                    if (chunkEntry.getValue() instanceof BlockEntityItemFrame) {
                        chunkEntry.getValue().close();
                    }
                }

            }
        }
    }

    /***
     * Deletes all frames in a specified level that contain a matching nbt tag
     * @param level The Level object
     * @param tag The nbt tag key that differentiates the BlockEntityItemFrame object
     */
    public static synchronized boolean deleteFrameEntities(World world, String tag) {

        Map<Long, ? extends Chunk> chunksMap = world.getLoadedChunks();

        Map<Long, BlockEntity> entMap;

        for (var entry : chunksMap.entrySet()) {
            entMap = entry.getValue().getBlockEntities();
            if (!entMap.values().isEmpty()) {
                for (var chunkEntry : entMap.entrySet()) {
                    if (chunkEntry.getValue().namedTag.contains(tag)) {
                        if (chunkEntry.getValue() instanceof BlockEntityItemFrame) {
                            chunkEntry.getValue().close();
                        }
                    }

                }
            }
        }
        return true;
    }

    public static synchronized <E extends Entity> Entity getMob(World world, String tag, Class<? extends Entity> mob) {

        Map<Long, ? extends Chunk> chunksMap = world.getLoadedChunks();

        Map<Long, Entity> entMap;

        for (var entry : chunksMap.entrySet()) {
            entMap = entry.getValue().getEntities();
            for (var chunkEntry : entMap.entrySet()) {
                if (mob.isAssignableFrom(chunkEntry.getValue().getClass())) {
                    if (chunkEntry.getValue().namedTag.contains(tag)) {
                        return chunkEntry.getValue();
                    }
                }
            }
        }

        return null;
    }

    /***
     * Deletes all mobs in a specified level that contain a matching nbt tag.
     * @param level The Level object
     * @param tag The nbt tag key that differentiates the mob
     * @param mob The entity subclass object type to be checked (what kind of mob)
     */
    public static synchronized <E extends Entity> boolean deleteMobs(World world, String tag, Class<? extends Entity> mob, Boolean fx) {

        Map<Long, ? extends Chunk> chunksMap = world.getLoadedChunks();

        Map<Long, Entity> entMap;

        for (var entry : chunksMap.entrySet()) {
            entMap = entry.getValue().getEntities();
            for (var chunkEntry : entMap.entrySet()) {
                if (chunkEntry.getValue().getClass() == mob) {
                    if (chunkEntry.getValue().namedTag.contains(tag)) {
                        if (fx) {world.addParticleEffect(new Vector3(chunkEntry.getValue().x, chunkEntry.getValue().y, chunkEntry.getValue().z),DRAGON_DESTROY_BLOCK);}
                        chunkEntry.getValue().close();
                    }
                }

            }
        }
        return true;
    }

    /***
     * Counts frames that contain a specific nbt tag. Overloaded for greater specificity.
     * @param player The Player object
     * @param tag The nbt tag key that differentiates the BlockEntityItemFrame object
     * @return int equal to the number of BlockEntityItemFrame objects in the player's level with an nbt tag key equal to tag
     */
    public static int countFrames(Player player, String tag) {

        World world = player.getWorld();

        Map<Long, ? extends Chunk> chunksMap = world.getLoadedChunks();

        Map<Long, BlockEntity> entMap;

        int frameCount = 0;

        for (var entry : chunksMap.entrySet()) {
            entMap = entry.getValue().getBlockEntities();
            for (var chunkEntry : entMap.entrySet()) {
                if (chunkEntry.getValue().namedTag.contains(tag)) {
                    if (chunkEntry.getValue() instanceof BlockEntityItemFrame) {
                        frameCount++;
                    }
                }

            }
        }
        return frameCount;
    }
    /***
     * Counts frames that contain a specific nbt tag. Overloaded for greater specificity.
     * @param player The Player object
     * @param tag The nbt tag key that differentiates the BlockEntityItemFrame object
     * @param i The value of tag (expected as an Integer tag) to further differentiate the BlockEntityItemFrame object
     * @return int equal to the number of BlockEntityItemFrame objects in the player's level with an nbt tag key equal to tag
     */
    public static int findFrames(Player player, String tag, int i){

        World world = player.getWorld();

        Map<Long, ? extends Chunk> chunksMap = world.getLoadedChunks();

        Map<Long, BlockEntity> entMap;

        int frameCount = 0;

        for (var entry : chunksMap.entrySet()) {
            entMap = entry.getValue().getBlockEntities();
            for (var chunkEntry : entMap.entrySet()) {
                if (chunkEntry.getValue().namedTag.contains(tag)) {
                    if (chunkEntry.getValue() instanceof BlockEntityItemFrame) {
                        if (chunkEntry.getValue().namedTag.getInt(tag) == i) {
                            frameCount++;
                        }
                    }
                }

            }
        }
        return frameCount;
    }

    public static int[] doSum(Player player, String sum, boolean reset) throws InvalidFrameWriteException {

        int elementsAmount = 0; // Must include elements that don't change (e.g. =)
        int[] elements = new int[0];
        boolean override = false;

        switch (sum) { //TODO: Pull these sums in from elsewhere (database?) and make array assignment modular
            case "Sum1" -> { // n + n = n
                elementsAmount = 5;
                elements = new int[elementsAmount];
                elements[0] = ThreadLocalRandom.current().nextInt(0, 9 + 1);
                /**
                 * 0 = +
                 * 1 = -
                 * 2 = ×
                 * 3 = ÷
                 * 4 = =
                 */
                elements[1] = ThreadLocalRandom.current().nextInt(0, 3 + 1);
                elements[2] = ThreadLocalRandom.current().nextInt(0, 9 + 1);
                elements[3] = 4;
                switch (elements[1]) {
                    case 0 -> elements[4] = (elements[0] + elements[2]);
                    case 1 -> {
                        elements[4] = (elements[0] - elements[2]);
                        do {
                            elements[0] = ThreadLocalRandom.current().nextInt(0, 9 + 1);
                            elements[2] = ThreadLocalRandom.current().nextInt(0, 9 + 1);
                            elements[4] = (elements[0] - elements[2]);
                        } while (elements[4] < 0);
                    }
                    case 2 -> elements[4] = (elements[0] * elements[2]);
                    case 3 -> {
                        float div;
                        do {
                            elements[0] = ThreadLocalRandom.current().nextInt(0, 9 + 1);
                            elements[2] = ThreadLocalRandom.current().nextInt(0, 9 + 1);
                            div = ((float) elements[0]) / ((float) elements[2]);
                        } while (div % 1 != 0);
                        elements[4] = (elements[0] / elements[2]);
                    }

                }
                elements[1] = (2147483647 - elements[1]); //TODO: Operator handling
                elements[3] = (2147483647 - elements[3]);
            }
            case "Sum2" -> { // n + n = n

                elementsAmount = 5;
                elements = new int[elementsAmount];
                elements[0] = ThreadLocalRandom.current().nextInt(0, 4 + 1);
                /**
                 * 0 = +
                 * 1 = -
                 * 2 = ×
                 * 3 = ÷
                 * 4 = =
                 * 5 = x
                 */
                elements[1] = 0;
                elements[2] = ThreadLocalRandom.current().nextInt(0, 5 + 1);
                elements[3] = 4;
                elements[4] = 5;
                elements[1] = (2147483647 - elements[1]);
                elements[3] = (2147483647 - elements[3]);
                elements[4] = (2147483647 - elements[4]);
                sumX = (elements[0] + elements[2]);
                solveForX = true;
            }
            case "NSR-1" -> { // n + n = n

                elementsAmount = 5;
                elements = new int[elementsAmount];
                elements[0] = ThreadLocalRandom.current().nextInt(0, 10 + 1);
                /**
                 * 0 = +
                 * 1 = -
                 * 2 = ×
                 * 3 = ÷
                 * 4 = =
                 * 5 = x
                 */
                elements[1] = 0;
                elements[2] = ThreadLocalRandom.current().nextInt(0, 10 + 1);
                elements[3] = 4;
                elements[4] = 5;
                elements[1] = (2147483647 - elements[1]);
                elements[3] = (2147483647 - elements[3]);
                elements[4] = (2147483647 - elements[4]);
                override = true;
            }

//            case "TWR-1" -> {
//                int level = Integer.parseInt(sum.replace("TWR-1",""));
//                elementsAmount = 5;
//                elements = new int[elementsAmount];
//                elements[0] = ThreadLocalRandom.current().nextInt(0, 5 + level); // Default difficulty - ground floor, both numbers <= 5, this goes up by one per floor
//                /**
//                 * 0 = +
//                 * 1 = -
//                 * 2 = ×
//                 * 3 = ÷
//                 * 4 = =
//                 * 5 = x
//                 */
//                elements[1] = 0;
//                elements[2] = ThreadLocalRandom.current().nextInt(0, 5 + level);
//                elements[3] = 4;
//                elements[4] = 5;
//                elements[1] = (2147483647 - elements[1]);
//                elements[3] = (2147483647 - elements[3]);
//                elements[4] = (2147483647 - elements[4]);
//            }

        }
        if (reset) {
            Arrays.fill(elements,2147483642);
        }
        if ((elements.length > 0) && !override) {
            for (int i = 1; i <= elementsAmount; i++) {
                findFrame(player, sum, i, elements[(i - 1)], 1);
            }
        }
        return elements;
    }

    static CompoundTag mathsNBTMaker(Player p, String puzzle, int value) {
        CompoundTag nbt = new CompoundTag();
        double x;
        double y;
        double z;
        switch(puzzle) {
            case "Pen1":
                x = 106;
                y = 20;
                z = -212;
                break;
            default:
                x = p.x;
                y = p.y;
                z = p.z;
        }

        nbt.putList((new ListTag("Pos"))
                .add(new DoubleTag("", x))
                .add(new DoubleTag("", y))
                .add(new DoubleTag("", z)))
                .putList((new ListTag("Motion"))
                        .add(new DoubleTag("", 0.0D))
                        .add(new DoubleTag("", 0.0D))
                        .add(new DoubleTag("", 0.0D)))
                .putList((new ListTag("Rotation"))
                        .add(new FloatTag("", (float)p.getYaw()))
                        .add(new FloatTag("", (float)p.getPitch())))
                .putBoolean("Invulnerable", true).putBoolean("isRotation", false)
                .putBoolean("npc", false)
                .putFloat("scale", 1.0F).putInt(puzzle,value);
        return nbt;
    }

    public static void writeSumAnswers(Collection<BlockEntityItemFrame> questionFrames, String puzzle, int solution, boolean submit, int submitVal) throws InvalidFrameWriteException {
            for (BlockEntityItemFrame iF : questionFrames) {
                switch (puzzle) {
                    case "Islands1", "Freefall1" -> {
                        switch (iF.namedTag.getInt(puzzle)) {
                            case 1 -> {
                                if (submit) {
                                    writeFrame(iF, submitVal, 1);
                                } else {
                                    writeFrame(iF, 2147483642, 1);
                                }
                            }
                            case 2 -> writeFrame(iF, 2147483647, 1);
                            case 3 -> writeFrame(iF, 2147483642, 1);
                            case 4 -> writeFrame(iF, 2147483643, 1);
                            case 5 -> writeFrame(iF, solution, 1);
                        }
                    }
                }
                iF.getWorld().addParticleEffect(new Vector3(iF.add(0.5).x, iF.add(0, 0.5).y, iF.add(0, 0, 0.5).z), CAMERA_SHOOT_EXPLOSION);
            }
    }

    public static CompoundTag getCachedTag() {
        return cachedTag;
    }

    public static void setCachedTag(CompoundTag template) {
        cachedTag = template;
    }

    public static synchronized void writeFrame(BlockEntityItemFrame iF, int value, int type) throws InvalidFrameWriteException {
        writeFrame(iF, value, type, false, "");
    }

    public static synchronized void writeFrame(BlockEntityItemFrame iF, int value, int type, boolean update, String puzzle) throws InvalidFrameWriteException {

        if (iF != null) {
            /**
             * Types:
             * 0 = Reset Frames
             * 1 = Standard Sum
             * 2 = Grid Puzzle
             * 3 = Multi-Stage Challenge Step
             * 4 = Checks Puzzle
             * 5 = Submit
             * 6 = Pairs Puzzle
             * 7 = Farm Puzzle
             */

            switch (type) {
                case 0 -> iF.setItem(null);
                case 1 -> {
                    ItemMap map = new ItemMap();
                    BufferedImage image = null;
                    String filename = switch (value) {
                        case 2147483647 -> "+.png";
                        case 2147483646 -> "-.png";
                        case 2147483645 -> "x.png";
                        case 2147483644 -> "div.png";
                        case 2147483643 -> "=.png";
                        case 2147483642 -> "purpleqm.png";
                        default -> (Integer.valueOf(value).toString() + ".png");
                    };
                    try {
                        File filePath = new File(Helper.getImagesDir().toFile(), filename);
                        image = ImageIO.read(filePath);
                    }
                    catch (IOException ioException) {
                        //ioException.printStackTrace();
                        String nameError = (TextFormat.RED + "No " + filename + "file found in " + Helper.getImagesDir());
                        Log.debug(nameError);
                    }
                    map.setImage(image);

                    //p.getInventory().addItem(new Item[]{map});

                    iF.setItem(map);

                    iF.getWorld().addParticleEffect(new Vector3(iF.add(0.5).x, iF.add(0,0.5).y, iF.add(0,0,0.5).z),CAMERA_SHOOT_EXPLOSION);
                }
                case 2 -> throw new InvalidFrameWriteException(type);
                case 3 -> {
                    ItemMap map = new ItemMap();
                    BufferedImage image = null;
                    String filename = switch (value) {
                        case 0 -> "notcomplete.png";
                        case 1 -> "complete.png";
                        default -> throw new InvalidFrameWriteException(type);
                    };
                    try {
                        File filePath = new File(Helper.getImagesDir().toFile(), filename);
                        image = ImageIO.read(filePath);
                    }
                    catch (IOException ioException) {
                        //ioException.printStackTrace();
                        String nameError = (TextFormat.RED + "No " + filename + "file found in " + Helper.getImagesDir());
                        Log.debug(nameError);
                    }
                    map.setImage(image);
                    iF.setItem(map);
                    iF.getWorld().addParticleEffect(new Vector3(iF.add(0.5).x, iF.add(0,0.5).y, iF.add(0,0,0.5).z),CAMERA_SHOOT_EXPLOSION);}
                case 4 -> {
                    ItemMap map = new ItemMap();
                    BufferedImage image = null;
                    String filename = switch (value) {
                        case 0 -> "cross.png";
                        case 1 -> "check.png";
                        case 2 -> "submit.png";
                        default -> throw new InvalidFrameWriteException(type);
                    };
                    try {
                        File filePath = new File(Helper.getImagesDir().toFile(), filename);
                        image = ImageIO.read(filePath);
                    }
                    catch (IOException ioException) {
                        //ioException.printStackTrace();
                        String nameError = (TextFormat.RED + "No " + filename + "file found in " + Helper.getImagesDir());
                        Log.debug(nameError);
                    }
                    map.setImage(image);
                    iF.setItem(map);
                    // Additional updating
                    if (update) {
                        iF.spawnToAll();
                    }
                    iF.getWorld().addParticleEffect(new Vector3(iF.add(0.5).x, iF.add(0,0.5).y, iF.add(0,0,0.5).z),CAMERA_SHOOT_EXPLOSION);}
                case 5 -> {
                    ItemMap map = new ItemMap();
                    BufferedImage image = null;
                    try {
                        File filePath = new File(Helper.getImagesDir().toFile(), "submit.png");
                        image = ImageIO.read(filePath);
                    }
                    catch (IOException ioException) {
                        //ioException.printStackTrace();
                        String nameError = (TextFormat.RED + "No submit file found in " + Helper.getImagesDir());
                        Log.debug(nameError);
                    }
                    map.setImage(image);
                    iF.setItem(map);
                    // Additional updating
                    if (update) {
                        iF.spawnToAll();
                    }
                    iF.getWorld().addParticleEffect(new Vector3(iF.add(0.5).x, iF.add(0,0.5).y, iF.add(0,0,0.5).z),CAMERA_SHOOT_EXPLOSION);}
                case 6 -> {
                    ItemMap map = new ItemMap();
                    BufferedImage image = null;
                    switch (value) {
                        case 0 -> {
                            String filename = "back.png";
                            try {
                                File filePath = new File(Helper.getImagesDir().toFile(), filename);
                                image = ImageIO.read(filePath);
                            }
                            catch (IOException ioException) {
                                //ioException.printStackTrace();
                                String nameError = (TextFormat.RED + "No Pairs tile back image file found in " + Helper.getImagesDir());
                                Log.debug(nameError);
                            }
                        }
                        case 1 -> {
                            try {
                                image = TextToImage.generate(iF.namedTag.getString(puzzle), false);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                                Log.error(TextToImage.fmError);
                            }
                        }
                        default -> throw new InvalidFrameWriteException(type);
                    }
                    map.setImage(image);
                    iF.setItem(map);
                    iF.getWorld().addParticleEffect(new Vector3(iF.add(0.5).x, iF.add(0,0.5).y, iF.add(0,0,0.5).z),CAMERA_SHOOT_EXPLOSION);
                }
                case 7 -> {
                    ItemMap map = new ItemMap();
                    BufferedImage image = null;
                    String filename = "";
                    File filePath;
                    if (value == Integer.MAX_VALUE) filename = "acc0.png";
                    else if (value == Integer.MAX_VALUE-1) filename = "acc1.png";
                    else if (value == Integer.MAX_VALUE-2) filename = "acc2.png";
                    else {
                        for (Farm.CropType cropType : Farm.CropType.values()) {
                            if (value == cropType.itemID)
                                filename = cropType.getName().toLowerCase() + ".png";
                        }
                        if (filename.length() < 1) {
                            String nameError = (TextFormat.RED + "No item data associated with ID " + value);
                            Log.error(nameError);
                        }
                    }
                    try {
                        filePath = new File(Helper.getImagesDir().toFile(), filename);
                        image = ImageIO.read(filePath);
                    } catch (IOException ioException) {
                        String nameError = (TextFormat.RED + "No " + filename + " file found in " + Helper.getImagesDir());
                        Log.error(nameError);
                    }
                    map.setImage(image);
                    iF.setItem(map);
                    iF.getWorld().addParticleEffect(new Vector3(iF.add(0.5).x, iF.add(0,0.5).y, iF.add(0,0,0.5).z),CAMERA_SHOOT_EXPLOSION);
                }
            }
        }
    }


    public static synchronized void writeFrame(BlockEntityItemFrame iF, Player p, int type) throws InvalidFrameWriteException {

        if (iF != null) {
            iF.namedTag.putFloat("ItemDropChance",0);
            iF.dropItem(p);

            switch (type) {
                case 0 -> iF.setItem(null);
                case 1, 3 -> throw new InvalidFrameWriteException(type);
            }
        }
    }

    public static HashMap<Integer, BlockEntityItemFrame> findAnswerItemFrames(Player player, String puzzle) {
        HashMap<Integer, BlockEntityItemFrame> answerFrames = new HashMap<>();
        for (int i = 1; ; i++) {
            String concatAF = puzzle + "A" + i;
            BlockEntityItemFrame newAnswer = getFrame(player, concatAF);
            if (newAnswer == null) {
                break;
            }
            answerFrames.put(i, newAnswer);
        }
        return answerFrames;
    }

    public static synchronized List<BlockEntityItemFrame> applyItemFrames(World world, Map<Integer, Vector3> columns, int defaultFace, int max, boolean margin) throws InterruptedException {
        return applyItemFrames(level, columns, defaultFace, "", max, margin, false, true);
    }
    public static synchronized List<BlockEntityItemFrame> applyItemFrames(World world, Map<Integer, Vector3> columns, int defaultFace, String puzzle, int max, boolean margin) throws InterruptedException {
        return applyItemFrames(level, columns, defaultFace, puzzle, max, margin, false, true);
    }
    public static synchronized List<BlockEntityItemFrame> applyItemFrames(World world, Vector3[] columns, int defaultFace, String puzzle, int max, boolean margin, boolean empty) throws InterruptedException {
        int counter = 0;
        HashMap<Integer, Vector3> columnsMap = new HashMap<>();
        for (Vector3 column : columns) {
            columnsMap.put(counter, column);
            counter++;
            }
        return applyItemFrames(level, columnsMap, defaultFace, puzzle, max, margin, empty, true);
    }
    public static synchronized List<BlockEntityItemFrame> applyItemFrames(World world, Vector3[] columns, int defaultFace, String puzzle, int max, boolean margin, boolean empty, boolean delete) throws InterruptedException {
        int counter = 0;
        HashMap<Integer, Vector3> columnsMap = new HashMap<>();
        for (Vector3 column : columns) {
            columnsMap.put(counter, column);
            counter++;
        }
        return applyItemFrames(level, columnsMap, defaultFace, puzzle, max, margin, empty, delete);
    }

        /**
         * Attaches item frames to a vertical column of blocks. Originally written for Pillars puzzles, but has since
         * been adapted for other applications. Will return a list of BlockEntityItemFrames or Vector3s (the locations
         * of the placed frames), if "empty" is set to false or true, respectively.
         * @param level - The Level object for the placing of blocks/block entities.
         * @param columns - Map<Integer, Vector3> - The Vector3 (of the base of the column) is used to determine where
         *                to place the item frames. The integer value is ultimately used to add an integer tag to the
         *                block entity created of that value. This was originally used in Pillars puzzles to distinguish
         *                different columns by their colours. The 'puzzle' parameter is used to named the tag.
         * @param defaultFace - To determine which way the item frame should face. Integer values are:
         * @param puzzle - The puzzle name, used to specify an item to fill the frame with and to create an integer tag
         *               on applied block entities with this parameter as a name and the 'columns' parameter's key as a
         *               value. A tag will not be generated if this.length() < 1;
         * @param max - The maximum possible height of the column to be populated with item frames.
         * @param margin - If set to true to top and bottom block of the column will not be populated with item frames
         *               (such as in Pillars puzzles).
         * @throws InterruptedException
         */
    public static synchronized List<BlockEntityItemFrame> applyItemFrames(World world, Map<Integer, Vector3> columns, int defaultFace, String puzzle, int max, boolean margin, boolean empty, boolean delete) throws InterruptedException {

        BlockItemFrame frame = new BlockItemFrame();
        frame.setDamage(defaultFace);
        frame.setLevel(level);
        Item item = null;
        boolean fill = false;
        boolean tag = !puzzle.equals("Checks") && puzzle.length() > 0;
        List<BlockEntityItemFrame> itemFrames = new ArrayList<>();

        // Customisation of frame contents below
        if (!empty) {
            switch (puzzle) {
                case ("Cakes1") -> item = new ItemCake();
                case ("Checks") -> {
                    ItemMap map = new ItemMap();
                    BufferedImage image = null;
                    try {
                        File filePath = new File(Helper.getImagesDir().toFile(), "cross.png");
                        image = ImageIO.read(filePath);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    map.setImage(image);
                    item = map;
                }
                case ("Submit") -> {
                    ItemMap map = new ItemMap();
                    BufferedImage image = null;
                    try {
                        File filePath = new File(Helper.getImagesDir().toFile(), "submit.png");
                        image = ImageIO.read(filePath);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    map.setImage(image);
                    item = map;
                }
                case ("Pairs1") -> {
                    ItemMap map = new ItemMap();
                    BufferedImage image = null;
                    try {
                        File filePath = new File(Helper.getImagesDir().toFile(), "back.png");
                        image = ImageIO.read(filePath);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    map.setImage(image);
                    item = map;
                }
                case ("Assemble") -> {
                    ItemMap map = new ItemMap();
                    BufferedImage image = null;
                    try {
                        File filePath = new File(Helper.getImagesDir().toFile(), "purpleqm.png");
                        image = ImageIO.read(filePath);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    map.setImage(image);
                    item = map;
                }

            }


            if (item != null) fill = true;
            if (item instanceof ItemMap) fill = false;

            if (delete) {
                while (!Arithmetic.deleteFrameEntities(level, puzzle)) {
                    Arithmetic.class.wait();
                }
            }
        }

        for (var entry : columns.entrySet()) {
            int limit = margin ? (max - 1) : max;
            int colour = entry.getKey();
            Vector3 pe = entry.getValue();
            CompoundTag contents = (new CompoundTag())
                    .putString("id", "ItemFrame")
                    .putByte("ItemRotation", 0)
                    .putFloat("ItemDropChance", 0F);
            for (int i = margin ? 1 : 0 ; i < limit ; i++) {

                if (!margin) {
                    AddPillarObjects apo = new AddPillarObjects(level, pe, frame, defaultFace, i, false);
                    apo.start();
                    apo.join();
                    AddPillarObjects.getAllBlockLocs().addAll(AddPillarObjects.getBlockLocs());
                }

                if (world.isFullBlock(pe.add(0, i))) {

                    if (world.isFullBlock(pe.add(0, (i + 1))) || !margin) {
                        Arithmetic.setCachedTag(contents);
                        AddPillarObjects apo2 = new AddPillarObjects(level, pe, frame, defaultFace, i, false);
                        apo2.start();
                        apo2.join();
                        AddPillarObjects.getAllBlockLocs().addAll(AddPillarObjects.getBlockLocs());

                        contents = Arithmetic.getCachedTag();
                        if (!empty) {
                            if (fill) contents.putCompound("Item", NBTIO.putItemHelper(Objects.requireNonNull(item)));
                            if (tag) contents.putInt(puzzle, colour);

                            if (fill) {
                                if (item.hasCustomBlockData()) {
                                    for (Tag aTag : item.getCustomBlockData().getAllTags()) {
                                        contents.put(aTag.getName(), aTag);
                                    }
                                }
                            }

                            frame.setX(contents.getInt("x"));
                            frame.setY(contents.getInt("y"));
                            frame.setZ(contents.getInt("z"));
                            BlockEntityItemFrame frameEnt = new BlockEntityItemFrame(frame.getChunk(), contents);
                            if (item instanceof ItemMap) frameEnt.setItem(item);
                            world.addBlockEntity(frameEnt);
                            itemFrames.add(frameEnt);

                            world.addParticleEffect(new Vector3(frame.add(0.5).x, frame.add(0, 0.5).y, frame.add(0, 0, 0.5).z), CAMERA_SHOOT_EXPLOSION);
                        }
                    }
                }
            }
        }
        return itemFrames;
    }

    public static BufferedImage imageTint(BufferedImage image, int r, int g, int b, int a){

            //Make the gray image, which is used as the overlay, translucent
            Graphics2D g2d = image.createGraphics();
            g2d.setColor(new Color(r, g, b, a));
            g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
            g2d.dispose();

            return image;

    }


    public static void makeSubmit(Location loc, String facing) {
        Level l = loc.getWorld();
        l.setBlock(loc.add(0, -1), new BlockWood());

        int facingInt;

        switch (facing.toUpperCase()) {
            case "N" -> facingInt = 3;
            case "E" -> facingInt = 0;
            case "S" -> facingInt = 2;
            case "W" -> facingInt = 1;
            default -> {
                for (Player p : l.getPlayers().values()){
                    Log.error(TextFormat.RED + "Invalid direction specified. Please choose " + "§f'N'§c, §f'E'§c, §f'S'§c, or §f'W'§c.");
                }
                throw new IllegalStateException("Unexpected value: " + facing);
            }
        }

        HashMap<Integer, Vector3> applyMap = new HashMap<>();
        applyMap.put(0, loc.add(0,-1));

        try {
            applyItemFrames(l, applyMap, facingInt, "Submit", 1, false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void replacePuzzleSign(BlockWallSign signBlock, String questionSetID){

        TileEntitySign sign = (TileEntitySign) signBlock.getWorld().getBlockEntity(signBlock);
        Location loc = signBlock.getLocation();
        String[] signText = new String[4];

        signText[0] = "SUBMIT";
        signText[1] = questionSetID;
        signText[2] = "";
        signText[3] = "";
        sign.setText(signText);

        sign.namedTag
                .putList(new ListTag<>("Pos")
                        .add(new DoubleTag("0", loc.x))
                        .add(new DoubleTag("1", loc.y))
                        .add(new DoubleTag("2", loc.z)));

        signBlock.getWorld().addBlockEntity(sign);
    }

    /**
     * Currently only works for blocks whose damage values are used to determine their facing.
     * If damage values are not mapped to direction the method will still return an adjacent block, but it will
     * essentially be arbitrary.
     * For placing blocks relatively, if you'd like to place a block relatively that does not store facing as a damage
     * value, get block locations from a chain of blocks with conventional facing information, placed using this method,
     * then replace them with the blocks you'd like.
     * @param block The block you'd like to get an adjacent block from.
     * @param direction Which direction from the source block you would like to check: BACK, FRONT, LEFT, RIGHT, UP or DOWN
     * @return The block adjacent to parameter block in parameter direction.
     */
    public static Block getAdjacent(Block block, String direction, int step) throws Exception {
        boolean notSupported = false;
        Block adjacent = null;
        BlockFace facing = BlockFace.fromHorizontalIndex(block.getDamage() & 7);
        switch (direction.toUpperCase(Locale.ROOT)) {
            case "BACK" -> {
                switch (facing) {
                    case NORTH -> adjacent = block.south(step);
                    case SOUTH -> adjacent = block.north(step);
                    case EAST -> adjacent = block.west(step);
                    case WEST -> adjacent = block.east(step);
                    case UP -> adjacent = block.down(step);
                    case DOWN -> adjacent = block.up(step);
                }
            }
            case "FRONT" -> {
                switch (facing) {
                    case NORTH -> adjacent = block.north(step);
                    case SOUTH -> adjacent = block.south(step);
                    case EAST -> adjacent = block.east(step);
                    case WEST -> adjacent = block.west(step);
                    case UP -> adjacent = block.up(step);
                    case DOWN -> adjacent = block.down(step);
                }
            }
            case "LEFT" -> {
                switch (facing) {
                    case NORTH -> adjacent = block.west(step);
                    case SOUTH -> adjacent = block.east(step);
                    case EAST -> adjacent = block.north(step);
                    case WEST -> adjacent = block.south(step);
                    case UP, DOWN -> notSupported = true;
                }
            }
            case "RIGHT" -> {
                switch (facing) {
                    case NORTH -> adjacent = block.east(step);
                    case SOUTH -> adjacent = block.west(step);
                    case EAST -> adjacent = block.south(step);
                    case WEST -> adjacent = block.north(step);
                    case UP, DOWN -> notSupported = true;
                }
            }
            case "UP" -> adjacent = block.up(step);
            case "DOWN" -> adjacent = block.down(step);
            default -> throw new IllegalStateException("Unexpected value: " + direction.toUpperCase(Locale.ROOT));
        }
        if (notSupported) {
            throw new Exception("This direction is not yet supported.");
        } else {
            return adjacent;
        }
    }

    public static Location highestGroundAt(Location loc) {
        for (int i = 320 ; i > 0 ; i--) {
            loc.setY(i);
            if (!(loc.getWorld().getBlock(loc) instanceof BlockAir)) return loc.add(0,1);
        }
        return null;
    }

    public static Vector3 findGroundAbove(Vector3 loc, World world) {
        loc.add(0,1);

        for (int i = (int)loc.y ; i < 320 ; i++) {
            loc.setY(i);
            Location spawnLoc = new Location(loc.x, loc.y + 1, loc.z);

            if (!(world.getBlock(loc) instanceof BlockAir) && (world.getBlock(spawnLoc) instanceof BlockAir)) { //check for a ground block and then for an air block above it
                return spawnLoc;
            }
        }
        return null;
    }

    public static Location findGroundAbove(Location loc) {
        loc.add(0,1); //start by checking above the given location

        for (int i = (int)loc.y ; i < 320 ; i++) {
            loc.setY(i);
            Location spawnLoc = new Location(loc.x, loc.y + 1, loc.z);

            if (!(loc.getWorld().getBlock(loc) instanceof BlockAir) && (loc.getWorld().getBlock(spawnLoc) instanceof BlockAir)) { //check for a ground block and then for an air block above it
                return spawnLoc;
            }
        }
        return null;
    }

    public static boolean inApothem(int apothem, Vector3 anchorLoc, Location pLoc) {
        return (inApothem(apothem, anchorLoc, pLoc, false));
    }

    public static boolean inApothem(int apothem, Vector3 anchorLoc, Location pLoc, boolean infiniteY) {
        double x = anchorLoc.x;
        double y = anchorLoc.y;
        double z = anchorLoc.z;
        return (inApothem(apothem, x, y, z, pLoc, infiniteY));
    }

    public static boolean inApothem(int apothem, double x, double y, double z, Location pLoc, boolean infiniteY) {
        if (!infiniteY) {
            return ((pLoc.x >= x - apothem && pLoc.x <= x + apothem) && (pLoc.z >= z - apothem && pLoc.z <= z + apothem) && (pLoc.y <= y + apothem));
        } else {
            return ((pLoc.x >= x - apothem && pLoc.x <= x + apothem) && (pLoc.z >= z - apothem && pLoc.z <= z + apothem) && (pLoc.y >= y));
        }
    }

    public static boolean isStringNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    public static int getKeyColorDamageValue(String color){
        int damage;
        switch (color.toLowerCase()) {
            case "white" -> damage = 0;
            case "orange" -> damage = 1;
            case "magenta" -> damage = 2;
            case "light blue" -> damage = 3;
            case "yellow" -> damage = 4;
            case "lime" -> damage = 5;
            case "pink" -> damage = 6;
            case "gray" -> damage = 7;
            case "light gray" -> damage = 8;
            case "cyan" -> damage = 9;
            case "purple" -> damage = 10;
            case "blue" -> damage = 11;
            case "brown" -> damage = 12;
            case "green" -> damage = 13;
            case "red" -> damage = 14;
            case "black" -> damage = 15;
            default -> {return 0;}
        }
        return damage;
    }

    public static void puzzleTeleport(Player p, String questionSetID) {
        // Remember to consider the order of the below
        ResetPuzzles.resetPuzzles(p);
        AnchorHandler.handleEndAnchor(p, questionSetID);
    }



    public static void removePuzzleInventoryItems(Player p){
        removePuzzleInventoryItems(p, PuzzleType.MOBGROUP);
        removePuzzleInventoryItems(p, PuzzleType.ASSEMBLE);
    }

    public static void removePuzzleInventoryItems(Player p, PuzzleType puzzleType){
        switch (puzzleType) {
            case MOBGROUP -> {
                for (Map.Entry<Integer, Item> entry : p.getInventory().getContents().entrySet()) {
                    Item v = entry.getValue();
                    if (v instanceof ItemGSDye || v instanceof ItemShears) {
                        p.getInventory().removeItem(v);
                    }
                }
            }
            case ASSEMBLE -> {
                for (Map.Entry<Integer, Item> entry : p.getInventory().getContents().entrySet()) {
                    Item v = entry.getValue();
                    if (v instanceof ItemMap) {
                        p.getInventory().removeItem(v);
                    }
                }
            }
        }
    }

    public static boolean mark(Player p, boolean result){
        if (result) {
            p.sendTitle("§2Correct!");
            Chord correct = new Chord(Note.C5, ChordType.MAJ, 1, false);
            MusicMaker.playArpeggio(p,correct,105,NOTE_HARP);
            puzzleName = null;
            return true;
        } else {
            p.sendTitle(TextFormat.GOLD + "Not quite, \nbut try again!");
            MusicMaker.playNote(p,Note.C3,NOTE_XYLOPHONE);
            return false;
        }
    }

    public static boolean mark(Player p, boolean result, String sub, boolean sound) {
        if (result) {
            p.sendTitle("§2Correct!",sub,5, 75, 5);
            if (sound) {
                Chord correct = new Chord(Note.C5, ChordType.MAJ, 1, false);
                MusicMaker.playArpeggio(p, correct, 105, NOTE_HARP);
            }
            puzzleName = null;
            return true;
        } else {
            p.sendTitle(TextFormat.GOLD + "Not quite, \nbut try again!", sub, 5, 75, 5);
            if (sound) {
            MusicMaker.playNote(p,Note.C3,NOTE_XYLOPHONE);
            }
            return false;
        }
    }

    public static void reset(){
        solveForX = false;
        puzzleName = null;
        world = 0;
        Pillars.setPillarsColour(-1);
    }

    public static double clamp(double value, double min, double max) {
        return value < min ? min : (Math.min(value, max));
    }

    public static ArrayList<String> getFacings() {
        return Arithmetic.facings;
    }

}
