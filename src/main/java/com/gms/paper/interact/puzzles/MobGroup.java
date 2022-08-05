package com.gms.paper.interact.puzzles;

import cn.nukkit.Player;
import cn.nukkit.block.*;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemShears;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.*;
import cn.nukkit.utils.TextFormat;
import com.gms.mc.custom.items.ItemGSDye;
import com.gms.mc.custom.items.listeners.NoDrop;
import com.gms.mc.data.User;
import com.gms.mc.error.InvalidFrameWriteException;
import com.gms.mc.interact.puzzles.handlers.anchors.AnchorHandler;
import com.gms.mc.interact.puzzles.maths.Arithmetic;
import com.gms.mc.interact.puzzles.maths.Pen;
import com.gms.mc.util.Log;
import io.netty.util.internal.ThreadLocalRandom;
import nukkitcoders.mobplugin.entities.animal.walking.Sheep;
import org.bukkit.entity.Player;

import javax.lang.model.element.Element;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.gms.mc.interact.puzzles.PuzzleType.MOBGROUP;

public class MobGroup {

    public MobGroup() {
    }

    private static HashMap<Object, Object> answerMap;
    private static HashMap<Integer, Item> items = new HashMap<>();

    /**
     * /// MOBGROUP Structure ///
     * <p>
     * Questions' (Sheep) tags: (String) Name: <QuestionSetID>, Value: Word (also in names)
     * Answers: Checked against the questions' Integer keys in answerMap
     * <p>
     * When a player right-clicks on sheep with custom item of type GSDye, that sheep's damage value changes based on
     * the colour of the dye. When the player clicks submit, the sheep's dye colours are cross-referenced with their
     * question nameTag values. If the dye values equals the question nameTag's value's key in answerMap then that sheep
     * is marked as correct.
     *
     * @param p         The Player object
     * @param buttonLoc The Location of the button used to generate the puzzle
     * @param facing    The cardinal direction of the puzzle (single character), specified by the player
     * @param size      Specifies the size of the apothem of the pen
     * @param pen       Should a pen (fences and gate) be generated?
     * @param data      One entry per question. Standard incremental Integer keys. Values in the following format: [0] = Prompt, [1] = Correct Answer, [2] = Correct Item, [3] = Mob Type
     * @throws InterruptedException
     */
    public static void generateMobGroup(Player p, Location buttonLoc, String facing, int size, boolean pen,
                                        HashMap<Integer, String[]> data) throws InvalidFrameWriteException {
        Level level = p.getLevel();
        Location base;
        BlockFence block = new BlockFence();
        int distance = size;
        size = size * 2;
        Location place;
        HashMap<Location, BlockFence> penFence = new HashMap<>();
        items = new HashMap<>();
        NoDrop.setNoDrop(true);
        buttonLoc.level = level;

        Arithmetic.makeSubmit(buttonLoc, facing);

        Log.logGeneric(p, TextFormat.LIGHT_PURPLE + "Generating " + TextFormat.AQUA + "MOBGROUP " + TextFormat.LIGHT_PURPLE + "puzzle...");

        // Establishing base & starting coordinates for placing fences
        switch (facing.toUpperCase()) {
            case "N" -> {
                base = buttonLoc.add(0, -1, -(Math.floor(size / 2F)));
                place = base.add(-distance);
            }
            case "E" -> {
                base = buttonLoc.add((Math.floor(size / 2F)), -1);
                place = base.add(0, 0, -distance);
            }
            case "S" -> {
                base = buttonLoc.add(0, -1, (Math.floor(size / 2F)));
                place = base.add(distance);
            }
            case "W" -> {
                base = buttonLoc.add(-(Math.floor(size / 2F)), -1);
                place = base.add(0, 0, distance);
            }
            default -> {
                Log.error(TextFormat.RED + "Invalid direction specified. Please choose " + "§f'N'§c, §f'E'§c, §f'S'§c, or §f'W'§c.");
                throw new IllegalStateException("Unexpected value: " + facing);
            }
        }

        //Building pen
        Location firstPlace;
        int lowX = (int) place.x;
        int lowZ = (int) place.z;
        int highX = 0;
        int highZ = 0;
        int x;
        int z;

        for (int i = 0; i < 4; i++) {
            firstPlace = place;
            x = 0;
            z = 0;
            if (i == 2) {
                highX = (int) place.x;
                highZ = (int) place.z;
            }
            switch (facing.toUpperCase()) {
                case "N" -> {
                    switch (i) {
                        case 0 -> x = 1;
                        case 1 -> z = 1;
                        case 2 -> x = -1;
                        case 3 -> z = -1;
                    }
                }
                case "E" -> {
                    switch (i) {
                        case 0 -> z = 1;
                        case 1 -> x = -1;
                        case 2 -> z = -1;
                        case 3 -> x = 1;
                    }
                }
                case "S" -> {
                    switch (i) {
                        case 0 -> x = -1;
                        case 1 -> z = -1;
                        case 2 -> x = 1;
                        case 3 -> z = 1;
                    }
                }
                case "W" -> {
                    switch (i) {
                        case 0 -> z = -1;
                        case 1 -> x = 1;
                        case 2 -> z = 1;
                        case 3 -> x = -1;
                    }
                }
            }
            for (int j = 1; j <= size; j++) {
                place = firstPlace.add((x * j), 0, (z * j));
                penFence.put(place, block);
            }
        }
        if (pen) {
            for (Map.Entry<Location, BlockFence> e : penFence.entrySet()) {
                level.setBlock(e.getKey(), e.getValue());
            }

            // Replacing the base block with the gate
            BlockFenceGate gate = new BlockFenceGate();
            // Setting gate facing
            switch (facing.toUpperCase()) {
                case "N" -> gate.setDamage(2);
                case "E" -> gate.setDamage(1);
                case "S" -> gate.setDamage(0);
                case "W" -> gate.setDamage(3);
                default -> throw new IllegalStateException("Unexpected value: " + facing);
            }
            level.setBlock(base, gate, true, true);
        }

        // Spawning mobs
        int counter = 1;
        for (Map.Entry<Object, Object> e : answerMap.entrySet()) {
            String value = (String) e.getKey();
            int minX = Math.min(lowX, highX);
            int maxX = Math.max(lowX, highX);
            int minZ = Math.min(lowZ, highZ);
            int maxZ = Math.max(lowZ, highZ);
            Vector3 mobPlace = new Vector3(ThreadLocalRandom.current().nextInt(minX + 2, maxX - 2), base.y, ThreadLocalRandom.current().nextInt(minZ + 2, maxZ - 2));
            while (level.getBlock(mobPlace).isSolid()) {
                minX = Math.min(lowX, highX);
                maxX = Math.max(lowX, highX);
                minZ = Math.min(lowZ, highZ);
                maxZ = Math.max(lowZ, highZ);
                mobPlace = new Vector3(ThreadLocalRandom.current().nextInt(minX + 2, maxX - 2), base.y, ThreadLocalRandom.current().nextInt(minZ + 2, maxZ - 2));
            }
            if (data.isEmpty()) {
                Sheep sheep = (Sheep) Pen.setMob(p, "PenDye1", mobNBT(p, mobPlace, "PenDye1", value), false);
                sheepProcessing(sheep, value);
            }
            else {
                Entity entity = Pen.setMobByType(p, data.get(counter)[3], mobNBT(p, mobPlace, BackendUtils.getQuestionSetID(), value));
                if (entity instanceof Sheep sheepEnt) sheepProcessing(sheepEnt, value);
                else {
                    //entity.setBaby(false);
                    entity.setNameTag(value);
                    entity.setNameTagVisible(true);
                    entity.setNameTagAlwaysVisible(true);
                    entity.spawnToAll();
                }
                counter++;
            }
        }

        // Finding each unique item type used and counting each iteration to divvy out items
        if (!data.isEmpty()) {
            HashMap<String, Integer[]> itemMap = new HashMap<>();
            Integer[] countAndColour;
            for (String[] strings : data.values()) {
                if (!itemMap.containsKey(strings[1])) {
                    countAndColour = new Integer[]{1, Arithmetic.getKeyColorDamageValue(strings[2])};
                    itemMap.put(strings[1], countAndColour);
                }
                else {
                    countAndColour = itemMap.get(strings[1]);
                    countAndColour[0] = countAndColour[0] + 1;
                    itemMap.replace(strings[1], countAndColour);
                }
            }
            for (Map.Entry<String, Integer[]> e : itemMap.entrySet()) {
                Item dye = User.getCurrent().addItemToInventory(p, new ItemGSDye(e.getValue()[0], e.getKey(), BackendUtils.getQuestionSetID(), e.getValue()[1]));
                items.put(e.getValue()[0], dye);
            }

            ItemShears shears = new ItemShears();
            shears.setDamage(-1);
            shears.setDamage(-1);
            User.getCurrent().addItemToInventory(p, shears);
        }
        else {
            //The below is a temporary solution to the above, for examples
            Item red = User.getCurrent().addItemToInventory(p, new ItemGSDye(7, "Noun", "PenDye1", 14));
            Item blue = User.getCurrent().addItemToInventory(p, new ItemGSDye(8, "Adjective", "PenDye1", 11));
            User.getCurrent().addItemToInventory(p, new ItemShears());

            p.getInventory().addItem(new ItemShears());
            items.put(7, red);
            items.put(8, blue);//newly discovered categories relating to specified colours
        }

        AnchorHandler.placeAnchor(p, level, buttonLoc, (int) Math.ceil(size / 2D), "");

        BackendUtils.setPuzzleType(MOBGROUP);
        Log.logGeneric(p, TextFormat.AQUA + "MOBGROUP " + TextFormat.GREEN + "puzzle successfully generated.");
    }

    /**
     * Dedicated method for solving MOBGROUP puzzles.
     *
     * @param p           - The Player object
     * @param questionSet - The question set ID to be used for this MOBGROUP puzzle
     */
    public static boolean solveMobGroup(Player p, String questionSet) {

        if (BackendUtils.getPuzzleType() == MOBGROUP) {

            Level l = p.getLevel();
            Map<Long, ? extends FullChunk> chunksMap = l.getChunks();
            Map<Long, Entity> entMap;
            int correct = 0;
            int total = 0;
            boolean pass = true;
            for (Map.Entry<Long, ? extends FullChunk> entry : chunksMap.entrySet()) {
                entMap = entry.getValue().getEntities();
                for (Map.Entry<Long, Entity> chunkEntry : entMap.entrySet()) {
                    if (chunkEntry.getValue() instanceof EntityCreature entity) {
                        if (entity.namedTag.contains(questionSet)) {
                            total++;
                            String answerKey = entity.namedTag.getString(questionSet);
                            Integer answerValue = (Integer) answerMap.get(answerKey);
                            if (entity instanceof Sheep sheep) {
                                if (sheep.getColor() != answerValue) pass = false;
                                else correct++;
                            }
                            else {
                                Log.debug("NOT SHEEP?!");
                                //TODO Other mobs
                            }
                        }
                    }
                }
            }
            if (pass) {
                BackendUtils.markAnswers(p,PuzzleType.MOBGROUP, true, null);
                Arithmetic.removePuzzleInventoryItems(p, MOBGROUP);
                Arithmetic.puzzleTeleport(p, questionSet);
                return true;
            }
            else {
                BackendUtils.markAnswers(p,PuzzleType.MOBGROUP, false, null);
                return false;
            }
        }
        return false;
    }

    public static void initializeMobGroup(Player p, HashMap<Integer, String[]> data) {
        items = new HashMap<>();
        NoDrop.setNoDrop(true);

        if (!data.isEmpty()) {
            HashMap<String, Integer[]> itemMap = new HashMap<>();
            Integer[] countAndColour;
            for (String[] strings : data.values()) {
                if (!itemMap.containsKey(strings[1])) {
                    countAndColour = new Integer[]{1, Arithmetic.getKeyColorDamageValue(strings[2])};
                    itemMap.put(strings[1], countAndColour);
                }
                else {
                    countAndColour = itemMap.get(strings[1]);
                    countAndColour[0] = countAndColour[0] + 1;
                    itemMap.replace(strings[1], countAndColour);
                }
            }
            for (Map.Entry<String, Integer[]> e : itemMap.entrySet()) {
                Item dye = User.getCurrent().addItemToInventory_ReplaceExisting(p, new ItemGSDye(e.getValue()[0], e.getKey(), BackendUtils.getQuestionSetID(), e.getValue()[1]));
                items.put(e.getValue()[1], dye);
            }

            User.getCurrent().addItemToInventory_ReplaceExisting(p, new ItemShears());
        }
        BackendUtils.setPuzzleType(MOBGROUP);
    }

    private static CompoundTag mobNBT(Player p, Vector3 location, String puzzle, String value) {

        CompoundTag nbt = Sheep.getDefaultNBT(location);

        return nbt
                .putList(new ListTag<>("Pos")
                        .add(new DoubleTag("0", location.x))
                        .add(new DoubleTag("1", location.y))
                        .add(new DoubleTag("2", location.z)))
                .putList(new ListTag<DoubleTag>("Motion")
                        .add(new DoubleTag("0", 0))
                        .add(new DoubleTag("1", 0))
                        .add(new DoubleTag("2", 0)))
                .putList(new ListTag<FloatTag>("Rotation")
                        .add(new FloatTag("0", (float) p.getYaw()))
                        .add(new FloatTag("1", (float) p.getPitch())))
                .putString(puzzle, value)
                .putBoolean("Invulnerable", true)
                .putBoolean("isRotation", false)
                .putBoolean("npc", false)
                .putFloat("scale", 1.0F);

    }

    private static void sheepProcessing(Sheep sheep, String value) {
        sheep.setColor(0);
        sheep.setBaby(false);
        sheep.setNameTag(value);
        sheep.spawnToAll();
    }

    public static <E> E getOnlyElement(Iterable<E> iterable) {

        Iterator<E> iterator = iterable.iterator();

        if (!iterator.hasNext()) {
            throw new RuntimeException("Collection is empty");
        }

        Element element = (Element) iterator.next();

        if (iterator.hasNext()) {
            throw new RuntimeException("Collection contains more than one item");
        }

        return (E) element;
    }

    public static <T, E> T getAnswerKey(Map<T, E> map, E value) {
        return getOnlyElement(map.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), value))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet()));
    }

   /* public static <E> E getOnlyElement(Iterable<E> iterable) {

        Iterator<E> iterator = iterable.iterator();

        if (!iterator.hasNext()) {
            throw new RuntimeException("Collection is empty");
        }

        Element element = (Element) iterator.next();

        if (iterator.hasNext()) {
            throw new RuntimeException("Collection contains more than one item");
        }

        return (E) element;
    }

    public static <T, E> Set<T> getAnswerKeys(Map<T, E> map, E value) {
        return map.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), value))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }*/


    public static HashMap<Object, Object> getAnswerMap() {
        return answerMap;
    }

    public static void setAnswerMap(HashMap<Object, Object> answers) {
        answerMap = answers;
    }

    public static HashMap<Integer, Item> getItems() {
        return items;
    }

    public static void setItems(HashMap<Integer, Item> items) {
        MobGroup.items = items;
    }
}
