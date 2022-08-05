package com.gms.paper.interact.puzzles;

import cn.nukkit.Player;
import cn.nukkit.block.BlockPlanks;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemMap;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.*;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.utils.TextFormat;
import com.gms.mc.Main;
import com.gms.mc.custom.items.ItemGSMap;
import com.gms.mc.error.InvalidFrameWriteException;
import com.gms.mc.interact.puzzles.handlers.anchors.AnchorHandler;
import com.gms.mc.interact.puzzles.maths.Arithmetic;
import com.gms.mc.interact.puzzles.utils.TextToImage;
import com.gms.mc.util.Log;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Player;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.gms.mc.interact.puzzles.PuzzleType.ASSEMBLE;

public class Assemble {

    private static HashMap<Integer, List<HashMap<Integer, String>>> questionSet = new HashMap<>();
    private static int questionNumber = 0;
    private static int questionSetSize = 0;

    public Assemble() {
    }

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
     * @param
     * @param
     * @param
     * @param
     * @param
     * @throws InvalidFrameWriteException
     * @throws InterruptedException
     */
    public static void generateAssemble(Player p, Location buttonLoc, String facing, HashMap<Integer, String> answers, HashMap<Integer, String> questions, HashMap<Integer, String> inventory, int apothem) throws InvalidFrameWriteException, InterruptedException {

        Level level = p.getLevel();
        Location base = new Location();
        BlockPlanks block = new BlockPlanks();

        Arithmetic.makeSubmit(buttonLoc, facing);

        // Establishing base
        switch (facing.toUpperCase()) {
            case "N" -> base = buttonLoc.add(0, -1, 7);
            case "E" -> base = buttonLoc.add(-7, -1);
            case "S" -> base = buttonLoc.add(0, -1, -7);
            case "W" -> base = buttonLoc.add(7, -1);
        }

        int[] dimensions;

        try {
             dimensions = sizeString(answers.values());
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.error(TextToImage.fmError);
            return;
        }

        //Creating question and inventory map items
        HashMap<Integer, ItemGSMap> questionMaps = new HashMap<>();
        for (Map.Entry<Integer, String> e : questions.entrySet()) {
            if (e.getValue().length() > 0)
                questionMaps.put(e.getKey(), new ItemGSMap(e.getValue(), BackendUtils.getQuestionSetID(), TextToImage.generate(e.getValue(), dimensions[0], 200, false, true))); //TextToImage.generate(e.getValue(), false)
            else questionMaps.put(e.getKey(), null);
        }
        Set<ItemGSMap> inventoryMaps = new HashSet<>();
        for (Map.Entry<Integer, String> e : inventory.entrySet()) {
            inventoryMaps.add(new ItemGSMap(e.getValue(), BackendUtils.getQuestionSetID(), TextToImage.generate(e.getValue(), dimensions[0], 200, false, true)));
        }

        //Creating canvas and Vector3s for Arithmetic.applyItemFrames()
        Vector3[] columnLocs = new Vector3[questions.size() + 2];
        for (int i = questions.size() + 2; i > 0; i--) {
            Location place;
            int offset = Math.round(questions.size() + 2 / 2F);
            for (int j = 2; j >= 0; j--) {
                switch (facing.toUpperCase()) {
                    case "N" -> place = base.add((offset - i), j);
                    case "E" -> place = base.add(0, j, (offset - i));
                    case "S" -> place = base.add((-offset + i), j);
                    case "W" -> place = base.add(0, j, (-offset + i));
                    default -> {
                        //p.sendMessage(TextFormat.RED + "Invalid direction specified. Please choose " + "§f'N'§c, §f'E'§c, §f'S'§c, or §f'W'§c.");
                        Log.error(TextFormat.RED + "Invalid direction specified. Please choose " + "§f'N'§c, §f'E'§c, §f'S'§c, or §f'W'§c.");
                        throw new IllegalStateException("Unexpected value: " + facing);
                    }
                }
                level.setBlock(place, block);
                if (place.y == base.y) {
                    columnLocs[i - 1] = place.add(0, 0);
                }
            }
        }
        columnLocs = ArrayUtils.remove(columnLocs, 0);
        Vector3[] finalColumnLocs = ArrayUtils.remove(columnLocs, columnLocs.length - 1);

        //Adding item frames
        int facingInt;
        switch (facing.toUpperCase()) {
            case "N" -> facingInt = 3;
            case "E" -> facingInt = 0;
            case "S" -> facingInt = 2;
            case "W" -> facingInt = 1;
            default -> throw new IllegalStateException("Unexpected value: " + facing);
        }

        AtomicReference<List<BlockEntityItemFrame>> newAssembleFrames = new AtomicReference<>();

        ExecutorService es = Executors.newCachedThreadPool();
        es.execute(() -> {
            try {
                newAssembleFrames.set(Arithmetic.applyItemFrames(level, finalColumnLocs, facingInt, "Assemble", 3, true, false, false));
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        es.shutdown();
        boolean finished = es.awaitTermination(5, TimeUnit.SECONDS);

        // Assigning maps to item frames
        Set<BlockEntityItemFrame> assembleFramesSet = new HashSet<>(newAssembleFrames.get());
        TreeMap<Integer, BlockEntityItemFrame> frameTreeMap;
        if (facing.equals("S") || facing.equals("W")) {frameTreeMap = new TreeMap<>();}
        else {frameTreeMap = new TreeMap<>(Collections.reverseOrder());}
        for (BlockEntityItemFrame ent : assembleFramesSet) {
            switch (facing.toUpperCase()) {
                case "N", "S" -> frameTreeMap.put((int) ent.x, ent);
                case "E", "W" -> frameTreeMap.put((int) ent.z, ent);
                default -> throw new IllegalStateException("Unexpected value: " + facing);
            }
        }

        //Setting question frames items
        int tmCounter = 0;
        for (Map.Entry<Integer, BlockEntityItemFrame> e : frameTreeMap.entrySet()) {
            if (questionMaps.get(tmCounter) != null) {
                e.getValue().setItem(questionMaps.get(tmCounter));
                e.getValue().namedTag.putBoolean("Assemble", true);
            }
            else e.getValue().namedTag.putBoolean("Assemble", false);
            e.getValue().namedTag.putInt("Question", questionNumber);
            e.getValue().namedTag.putInt("Order", tmCounter); //This needs placement based on sentence order
            tmCounter++;
        }

        //Setting player inventory
        new NukkitRunnable() {
            @Override
            public void run() {
                for (ItemGSMap map : inventoryMaps) {
                    BlockEntityItemFrame beif = new BlockEntityItemFrame(p.getChunk(), new CompoundTag());
                    beif.setItem(map);
                    beif.namedTag.putBoolean("Assemble", true);
                    Item cachedMap = beif.getItem();
                    p.getInventory().addItem(cachedMap);
                }
            }
        }.runTaskLater(Main.s_plugin, 1);

        AnchorHandler.placeAnchor(p, level, buttonLoc, apothem, "");

        BackendUtils.setPuzzleType(ASSEMBLE);
        Log.logGeneric(p, TextFormat.AQUA + "ASSEMBLE " + TextFormat.GREEN + "puzzle successfully generated.");
        //p.sendMessage(TextFormat.AQUA + "ASSEMBLE " + TextFormat.GREEN + "puzzle successfully generated.");

    }

    /**
     * Dedicated method for solving CHECKS puzzles.
     * Searches for objects and a count value, then compares them.
     *
     * @param p - The Player object
     * @param// grid - The name of the GRID puzzle (e.g. "Grid1")
     */
    public static boolean solveAssemble(Player p, String assemble) throws InvalidFrameWriteException, InterruptedException {

        //TODO: Need to load subsequent questions in solve

        if (BackendUtils.getPuzzleType() == ASSEMBLE) {

            TreeMap<Integer, String> sentence = new TreeMap<>();
            Map<Integer, BlockEntityItemFrame> questionFrames = new TreeMap<>();

            for (BlockEntityItemFrame frame : Arithmetic.getFrames(p, "Assemble")) {
                if (frame.getItem() != null) {
                    Item item = frame.getItem();
                    if (item.getNamedTag().exist(assemble)) {
                        sentence.put(frame.namedTag.getInt("Order"), item.getNamedTag().getString(assemble));
                        questionFrames.put(frame.namedTag.getInt("Order"), frame);
                    }
                }
            }

            HashMap<Integer, String> answerData =  new HashMap<>();
            String concatSentence = "";
                for (int i = 0; i < sentence.size(); i++) {
                    String chunk = sentence.get(i);
                    concatSentence = concatSentence.concat(chunk);
                    answerData.put(i, chunk);
                }

            HashMap<Integer, String> answers = BackendUtils.getAssembleData().get(questionNumber).get(0);
            String concatAnswers = "";
            for (String chunk : answers.values()) concatAnswers = concatAnswers.concat(chunk);

            if (concatSentence.equals(concatAnswers)) {
                BackendUtils.markAnswers(p, PuzzleType.ASSEMBLE, true, answerData);
                if (Assemble.questionNumber >= Assemble.questionSetSize) {
                    Arithmetic.puzzleTeleport(p, assemble);
                }
                else {
                    for (Item item : p.getInventory().getContents().values()) if (item instanceof ItemMap) p.getInventory().remove(item);

                    HashMap<Integer, String> questionMaps = Assemble.getQuestionSet().get(questionNumber + 1).get(1);
                    HashMap<Integer, String> inventory = Assemble.getQuestionSet().get(questionNumber + 1).get(2);

                    int[] max_questionMapString;
                    int[] max_inventoryString;

                    try {
                        max_questionMapString = sizeString(questionMaps.values());
                        max_inventoryString = sizeString(inventory.values());
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Log.error(TextToImage.fmError);
                        return false;
                    }
                    int[] dimensions = max_questionMapString[0] > max_inventoryString[0] ? max_questionMapString : max_inventoryString;

                    Set<ItemGSMap> inventoryMaps = new HashSet<>();
                    for (Map.Entry<Integer, String> e : inventory.entrySet()) {
                        ItemGSMap map = new ItemGSMap(e.getValue(), BackendUtils.getQuestionSetID(), TextToImage.generate(e.getValue(), dimensions[0], 200, false, true));
                        inventoryMaps.add(map);
                    }
                    new NukkitRunnable() {
                        @Override
                        public void run() {
                            for (ItemGSMap map : inventoryMaps) {
                                BlockEntityItemFrame beif = new BlockEntityItemFrame(p.getChunk(), new CompoundTag());
                                beif.setItem(map);
                                beif.namedTag.putBoolean("Assemble", true);
                                Item cachedMap = beif.getItem();
                                p.getInventory().addItem(cachedMap);
                            }
                        }
                    }.runTaskLater(Main.s_plugin, 1);

                    // Setting questions
                    for (Map.Entry<Integer, BlockEntityItemFrame> e : questionFrames.entrySet()) {
                        BlockEntityItemFrame beif = e.getValue();

                        beif.setItem(new ItemGSMap(questionMaps.get(e.getKey()), BackendUtils.getQuestionSetID(), TextToImage.generate(questionMaps.get(e.getKey()), dimensions[0], 200, false, true)));

                        beif.namedTag.putInt("Question", questionNumber + 1);
                    }
                    questionNumber = questionNumber + 1;
                }

            }
            else {
                BackendUtils.markAnswers(p, PuzzleType.ASSEMBLE,false, answerData);
            }
        }
        return false;
    }

    public static void initializeAssemble(Player p, HashMap<Integer, String> answers, HashMap<Integer, String> questions, HashMap<Integer, String> inventory) {

        int[] dimensions;

        try {
            dimensions = sizeString(answers.values());
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.error(TextToImage.fmError);
            return;
        }

        Set<ItemGSMap> inventoryMaps = new HashSet<>();
        for (Map.Entry<Integer, String> e : inventory.entrySet()) {
            if (dimensions != null) {
                inventoryMaps.add(new ItemGSMap(e.getValue(), BackendUtils.getQuestionSetID(), TextToImage.generate(e.getValue(), dimensions[0], 200, false, true)));
            }
        }

        //Setting player inventory
        new NukkitRunnable() {
            @Override
            public void run() {
                for (ItemGSMap map : inventoryMaps) {
                    BlockEntityItemFrame beif = new BlockEntityItemFrame(p.getChunk(), new CompoundTag());
                    beif.setItem(map);
                    beif.namedTag.putBoolean("Assemble", true);
                    Item cachedMap = beif.getItem();
                    p.getInventory().addItem(cachedMap);
                }
            }
        }.runTaskLater(Main.s_plugin, 1);

        BackendUtils.setPuzzleType(ASSEMBLE);

    }

    public static int[] sizeString(Collection<String> strings) {

        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        FontMetrics fm = TextToImage.getFontMetrics(g2d);
        if (fm == null) return null;
        g2d.setFont(fm.getFont());
        int width = 0;
        for (String string : strings) if (fm.stringWidth(string) > width) width = fm.stringWidth(string);

        int height = 200;

        return new int[]{width, height};

    }

    public static HashMap<Integer, List<HashMap<Integer, String>>> getQuestionSet() {
        return questionSet;
    }

    public static void setQuestionSet(HashMap<Integer, List<HashMap<Integer, String>>> questionSet) {
        Assemble.questionSet = questionSet;
    }

    public static int getQuestionNumber() {
        return questionNumber;
    }

    public static void setQuestionNumber(int questionNumber) {
        Assemble.questionNumber = questionNumber;
    }

    public static int getQuestionSetSize() {
        return questionSetSize;
    }

    public static void setQuestionSetSize(int questionSetSize) {
        Assemble.questionSetSize = questionSetSize;
    }
}
