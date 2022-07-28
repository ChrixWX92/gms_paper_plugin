package com.gms.paper.interact.puzzles;

import cn.nukkit.Player;
import cn.nukkit.block.*;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.item.ItemMap;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.particle.BoneMealParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.*;
import cn.nukkit.network.protocol.UpdateBlockPacket;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.utils.TextFormat;
import com.gms.mc.Main;
import com.gms.mc.custom.particles.ParticleFX;
import com.gms.mc.custom.particles.ParticleFXSequence;
import com.gms.mc.error.InvalidFrameWriteException;
import com.gms.mc.error.NoBlockEntityException;
import com.gms.mc.interact.puzzles.handlers.anchors.AnchorHandler;
import com.gms.mc.interact.puzzles.maths.Arithmetic;
import com.gms.mc.interact.puzzles.maths.threads.AddPillarObjects;
import com.gms.mc.interact.puzzles.maths.threads.FindFrames;
import com.gms.mc.util.Helper;
import com.gms.mc.util.HologramHelper;
import com.gms.mc.util.Log;
import gt.creeperface.holograms.Hologram;
import gt.creeperface.holograms.entity.HologramEntity;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.gms.mc.interact.puzzles.PuzzleType.CHECKS;

public class Checks {

    private static int checksSolved = 0;
    private static int checksSize = 0;
    private static Set<BlockEntitySign> signs = new HashSet<>();
    
    public Checks(){}

    /**
     * /// CHECKS Structure ///
     *
     * Questions' (Holograms) names: <Puzzlename> + row key; tags: (Integer) Name: <Puzzlename> + row key, Value: correct answer ID
     * Answers' (BlockEntitySigns) tags: (Integer) Name: <Puzzlename>"A", Value: column key
     * Checks' (BlockEntityItemFrames) tags: (Boolean) Name: <Puzzlename> + row key, Value: FALSE by default
     *
     * When the player presses "Submit Answer" the plugin will search for item frames containing boolean tags matching
     * the question holograms' names. If the plugin finds an item frame with a TRUE tag it will search upwards (+Y) until
     * it finds a sign (after 250 blocks it will throw an error). Finally, it will get the integer tag from that sign -
     * if the value matches the answer ID specified as correct for the Question being checked then that row will be
     * marked as correct in a table.
     *
     * @param p The Player object
     * @param buttonLoc The Location of the button used to generate the puzzle
     * @param facing The cardinal direction of the puzzle (single character), specified by the player
     * @param columns
     * @param rows
     * @throws InvalidFrameWriteException
     * @throws InterruptedException
     */
    public static void generateChecks(Player p, Location buttonLoc, String facing, HashMap<Integer, String> columns, HashMap<Integer, String> rows, int apothem) throws Exception {

        Level level = p.getLevel();
        Location base = new Location();
        boolean alternator = false;
        BlockPlanks block = new BlockPlanks();
        Set<HologramEntity> holograms = new HashSet<>();
        HashSet<String> uniqueColumns = new HashSet<>(columns.values());

        // Establishing base
        switch (facing.toUpperCase()) {
            case "N" -> base = buttonLoc.add(0, -1,7);
            case "E" -> base = buttonLoc.add(-7,-1);
            case "S" -> base = buttonLoc.add(0, -1,-7);
            case "W" -> base = buttonLoc.add(7, -1);
        }

        //Building rows
        int counter = 0;
        for (Map.Entry<Integer, String> row : rows.entrySet()) {

            int wood = (alternator ? 1 : 0);
            block.setDamage(wood);
            Location place;
            for (int width = 0 ; width < (uniqueColumns.size()+3) ; width++) {
                switch (facing.toUpperCase()) {
                    case "N" -> place = base.add((2 - width), counter);
                    case "E" -> place = base.add(0, counter, (2 - width));
                    case "S" -> place = base.add((-2 + width), counter);
                    case "W" -> place = base.add(0, counter, (-2 + width));
                    default -> {
                        Log.error(TextFormat.RED + "Invalid direction specified. Please choose " + "§f'N'§c, §f'E'§c, §f'S'§c, or §f'W'§c.");
                        throw new IllegalStateException("Unexpected value: " + facing);
                    }
                }
                level.setBlock(place,block);
            }

            // Locations for holograms
            Vector3 holoPlace;
            switch (facing.toUpperCase()) {
                case "N" -> holoPlace = base.add(1.5, counter);
                case "E" -> holoPlace = base.add(1, counter, 1.5);
                case "S" -> holoPlace = base.add(-0.5, counter, 1);
                case "W" -> holoPlace = base.add(0, counter, -0.5);
                default -> {
                    Log.error(TextFormat.RED + "Invalid direction specified. Please choose " + "§f'N'§c, §f'E'§c, §f'S'§c, or §f'W'§c.");
                    throw new IllegalStateException("Unexpected value: " + facing);
                }
            }

            // Creating our holograms
            String broken = row.getValue().replaceAll("(.{1,18})(?:$| )", "$1\n");

            checksSize = holograms.size();
            holograms.addAll(
                    HologramHelper.spawnBasicHologram(p, BackendUtils.getQuestionSetID() + "-" + (row.getKey()+1), broken, hologramNBT(p, holoPlace, BackendUtils.getQuestionSetID(), columns.get(row.getKey()), row.getKey()))
                    .keySet());

            alternator = !alternator;
            counter++;

        }

        //Adding column item frames and signs
        Vector3 columnTop = new Vector3();
        Map<Integer, Vector3> columnLocs = new HashMap<>();
        counter = 1;
        for (String column : uniqueColumns) {
            //Establishing column base for item frame application
            Vector3 columnBase;
            switch (facing.toUpperCase()) {
                case "N" -> columnBase = base.add(-counter, - 1);
                case "E" -> columnBase = base.add(0, -1, -counter);
                case "S" -> columnBase = base.add(counter, -1);
                case "W" -> columnBase = base.add(0, -1, counter);
                default -> throw new IllegalStateException("Unexpected value: " + facing);
            }
            //Placing column tops
            columnTop = columnBase.add(0,rows.size()+1);
            level.setBlock(columnTop,new BlockWood());
            // Setting column signs
            BlockWallSign sign = new BlockWallSign();
            Vector3 columnSignLoc;
            int facingInt;
            switch (facing.toUpperCase()) {
                case "N" -> {
                    columnSignLoc = columnTop.add(0, 0, -1);
                    sign.setDamage(2);
                    facingInt = 3;
                }
                case "E" -> {
                    columnSignLoc = columnTop.add(1);
                    sign.setDamage(5);
                    facingInt = 0;
                }
                case "S" -> {
                    columnSignLoc = columnTop.add(0, 0, 1);
                    sign.setDamage(0);
                    facingInt = 2;
                }
                case "W" -> {
                    columnSignLoc = columnTop.add(-1);
                    sign.setDamage(4);
                    facingInt = 1;
                }
                default -> throw new IllegalStateException("Unexpected value: " + facing);
            }

            level.setBlock(columnSignLoc, sign, true, true);

            Arithmetic.setCachedTag(setSign(new BlockEntitySign(p.getChunk(), BlockEntityItemFrame.getDefaultCompound(level.getBlock(columnSignLoc),"Sign")), level.getBlock(columnSignLoc), BackendUtils.getQuestionSetID(), column));
            for (int i = 0 ; i < 10 ; i++){
            AddPillarObjects apo = new AddPillarObjects(level, columnTop, sign, facingInt, 0, false);
            apo.start();
            apo.join();
            }
            
            spawnSigns(p, (Location) columnSignLoc, column);

            columnLocs.put(counter-1,columnBase.add(0,1));
            counter++;
        }
        int facingInt;
        switch (facing.toUpperCase()) {
            case "N" -> facingInt = 3;
            case "E" -> facingInt = 0;
            case "S" -> facingInt = 2;
            case "W" -> facingInt = 1;
            default -> throw new IllegalStateException("Unexpected value: " + facing);
        }
        List<BlockEntityItemFrame> checksFrames = Arithmetic.applyItemFrames(level, columnLocs, facingInt, "Checks", rows.size(), false);

        for (BlockEntityItemFrame frame : checksFrames)
        {setFrame(frame, holograms, BackendUtils.getQuestionSetID());}

        //Arithmetic.writeFrame(frame,0, 4, true); TODO: Will need to populate submit sign with this

        AnchorHandler.placeAnchor(p, level, buttonLoc, apothem, facing);

        BackendUtils.setPuzzleType(CHECKS);
        Log.logGeneric(p, TextFormat.AQUA + "CHECKS " + TextFormat.GREEN + "puzzle successfully generated.");

    }



    /**
     * Dedicated method for solving CHECKS puzzles.
     * Searches for objects and a count value, then compares them.
     * @param p - The Player object
     * @param checks - The question set ID to be used for this CHECKS puzzle
     */
    public static boolean solveChecks(Player p, String checks) throws InterruptedException, NoBlockEntityException {

        if (BackendUtils.getPuzzleType() == CHECKS) {

            boolean titleTrigger = true;

            for (int i = 0; i < checksSize; i++) {

                boolean solved = false;
                FindFrames ff = new FindFrames(p, checks, true);
                Thread t = new Thread(ff);
                t.start();
                t.join();
                Map<Long, BlockEntityItemFrame> frames = FindFrames.getMap();

                for (Map.Entry<Long, BlockEntityItemFrame> entry : frames.entrySet()) {

                    BlockEntityItemFrame frame = entry.getValue();
                    if (frame.namedTag.contains("freeze") && frame.namedTag.getBoolean("freeze")) continue;
                    Collection<Tag> frameTags = frame.namedTag.getAllTags();

                    for (Tag tag : frameTags) {

                        if (tag.getName().startsWith(checks)) {
                            String tagName = tag.getName();

                            if (frame.namedTag.getBoolean(tagName)) {

                                Block searchBlock = frame.getBlock().up();
                                int counter = 0;

                                while (!(searchBlock instanceof BlockWallSign answerSign)) {
                                    if (counter > 250) {
                                        Log.error(TextFormat.RED + "No answer sign could be found for the item frame at " + TextFormat.WHITE + frame.getLocation() + TextFormat.RED + ".");
                                        return false;
                                    }
                                    counter++;
                                    searchBlock = searchBlock.up();
                                }

                                BlockEntitySign answerSignEntity = null;
                                String answer = "";

                                try {
                                    answerSignEntity = (BlockEntitySign) answerSign.getLevel().getBlockEntity(answerSign);
                                    if (answerSignEntity.namedTag.getString(checks).length() <= 0) {
                                        Log.error("Could not find the puzzle String tag for answer sign at " + answerSignEntity.getLocation() + ".");
                                        Log.warn("Tag format: K=<Question Set ID>, V=<Answer>");
                                    } else {
                                        answer = TextFormat.clean(answerSignEntity.namedTag.getString(checks));
                                    }
                                } catch (Exception e) {
                                    throw new NoBlockEntityException("", answerSignEntity);
                                }

                                HologramEntity checkHologram = HologramHelper.getHologramEntity(frame.getLevel(), tagName); // Getting the hologram whose name matches the full boolean tag name of the check item frame in question

                                //data for marking answer and giving tickets
                                HashMap<Integer, String> checksData = new HashMap<>();
                                checksData.put(0,  checkHologram.namedTag.getString("holoText").replaceAll("\n", ""));
                                checksData.put(1, answer);

                                if (checkHologram == null) {

                                    Log.error("No hologram found with name " + tagName + ".");
                                    return false;

                                } else if (checkHologram.namedTag.getString(tagName).equals(answer)) { // If the HologramEntity's integer tag (containing its correct answer ID) is equal to the corresponding integer tag obtained from the located answer sign above

                                    p.sendMessage(TextFormat.GREEN + checkHologram.namedTag.getString("holoText") + " - CORRECT!");
                                    checksSolved++;

                                    for (BlockEntityItemFrame beif : Arithmetic.getFrames(p, tagName)) {
                                        boolean check = beif.namedTag.getBoolean(tagName);
                                        ItemMap tint = (ItemMap) beif.getItem();
                                        tint.setImage(Arithmetic.imageTint(getAnswerImage(check), check ? 20 : 150, check ? 150 : 20, 0, 128));
                                        beif.setItem(tint);
                                        beif.namedTag.putBoolean("freeze", true);
                                        beif.level.addParticle(new BoneMealParticle(beif.getLocation()));
                                    }

                                    new NukkitRunnable() {
                                        @Override
                                        public void run() {
                                            Hologram hologram = checkHologram.getHologram();
                                            checkHologram.closeHologram();
                                            HologramHelper.spawnBasicHologram(p, hologram.getName(), TextFormat.GREEN + checkHologram.namedTag.getString("holoText").replaceAll("\n", "\n" + TextFormat.GREEN), checkHologram.namedTag);
                                        }
                                    }.runTaskLater(Main.s_plugin, checksSolved + 1);

                                    if (checksSolved == (checksSize + 1)) solved = true;

                                    //mark answers
                                    BackendUtils.markAnswers(p, CHECKS, true, checksData);
                                } else {

                                    if (titleTrigger) {
                                        //mark answers
                                        BackendUtils.markAnswers(p, CHECKS, false, checksData);
                                        titleTrigger = false;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
                if (solved) {
                    for (BlockEntityItemFrame beif : frames.values()) {
                        ParticleFXSequence pFX = new ParticleFXSequence(ParticleFX.COMPLETE, beif.getLevel(), beif.getLocation());
                        synchronized (pFX) {
                            pFX.run();
                        }
                    }
                    Arithmetic.puzzleTeleport(p, checks);
                    return true;
                }
            }
        }
        return false;
    }

    public static void initializeChecks(Player p, Vector3 buttonLoc, String facing, HashMap<Integer, String> columns, HashMap<Integer, String> rows) {

        Vector3 base = new Vector3();
        Set<HologramEntity> holograms = new HashSet<>();

        // Establishing base
        switch (facing.toUpperCase()) {
            case "N" -> base = buttonLoc.add(0, -1, 7);
            case "E" -> base = buttonLoc.add(-7, -1);
            case "S" -> base = buttonLoc.add(0, -1, -7);
            case "W" -> base = buttonLoc.add(7, -1);
        }

        // Locations for holograms
        int counter = 0;
        for (Map.Entry<Integer, String> row : rows.entrySet()) {
            Vector3 holoPlace;
            switch (facing.toUpperCase()) {
                case "N" -> holoPlace = base.add(1.5, counter);
                case "E" -> holoPlace = base.add(1, counter, 1.5);
                case "S" -> holoPlace = base.add(-0.5, counter, 1);
                case "W" -> holoPlace = base.add(0, counter, -0.5);
                default -> {
                    Log.error(TextFormat.RED + "Invalid direction specified. Please choose " + "§f'N'§c, §f'E'§c, §f'S'§c, or §f'W'§c.");
                    throw new IllegalStateException("Unexpected value: " + facing);
                }
            }

            // Creating our holograms
            String broken = row.getValue().replaceAll("(.{1,18})(?:$| )", "$1\n");

            checksSize = holograms.size();
            holograms.addAll(
                    HologramHelper.spawnBasicHologram(p, BackendUtils.getQuestionSetID() + "-" + (row.getKey() + 1), broken, hologramNBT(p, holoPlace, BackendUtils.getQuestionSetID(), columns.get(row.getKey()), row.getKey()))
                            .keySet());
            counter++;

        }

        BackendUtils.setPuzzleType(CHECKS);

    }

    
    private static void spawnSigns(Player p, Location columnSignLoc, String column){
        Level level = p.getLevel();
        new NukkitRunnable() {
            @Override
            public void run () {
                BlockEntitySign columnSign = new BlockEntitySign(p.getChunk(), BlockEntityItemFrame.getDefaultCompound(level.getBlock(columnSignLoc),"Sign"));
                columnSign.namedTag = setSign(columnSign, level.getBlock(columnSignLoc), BackendUtils.getQuestionSetID(), column);
                level.addBlockEntity(columnSign);
                UpdateBlockPacket ubp = new UpdateBlockPacket();
                ubp.putEntityUniqueId(columnSign.id);
                ubp.encode();
                p.handleDataPacket(ubp);

                signs.add(columnSign);

                level.scheduleBlockEntityUpdate(columnSign);
                //SetEntityDataPacket packet = new SetEntityDataPacket();
                //packet.metadata;
                //packet.encode();
            }
        }.runTaskLater(Main.s_plugin, 1); //TODO ENTITY WILL NOT RELIABLY ALIGN WITH SIGN (WE THINK) FIX!
    }

    private static void removeDuplicateColumns(final Map<Integer, String> map) {
        final Iterator<Map.Entry<Integer, String>> iter = map.entrySet().iterator();
        final HashSet<String> valueSet = new HashSet<String>();
        while (iter.hasNext()) {
            final Map.Entry<Integer, String> next = iter.next();
            if (!valueSet.add(next.getValue())) {
                iter.remove();
            }
        }
    }

    private static CompoundTag hologramNBT(Player p, Vector3 location, String questionSet, String answer, int rowKey){

        return new CompoundTag()
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
                .putString("hologramId", questionSet)
                .putString(questionSet + "-" + (rowKey+1), answer);

    }

    private static CompoundTag setSign(BlockEntitySign columnSign, Vector3 location, String puzzle, String line){

        String[] columnSignText = new String[4];

        columnSignText[0] = "";
        columnSignText[1] = line;
        columnSignText[2] = "";
        columnSignText[3] = "";
        columnSign.setText(columnSignText);

        return columnSign.namedTag
                .putList(new ListTag<>("Pos")
                        .add(new DoubleTag("0", location.x))
                        .add(new DoubleTag("1", location.y))
                        .add(new DoubleTag("2", location.z)))
                .putString("SignId", BackendUtils.getQuestionSetID())
                .putString(puzzle, line); //The critical line
    }

    private static void setFrame(BlockEntityItemFrame beif, Set<HologramEntity> holograms, String puzzleName) {

        CompoundTag contents = (new CompoundTag())
                .putString("id", "ItemFrame")
                .putByte("ItemRotation", 0)
                .putFloat("ItemDropChance", 0F);

        for (HologramEntity h : holograms) {
            if (h.getFloorY() == beif.getFloorY()) {
                for (Tag tag : h.namedTag.getAllTags()) {
                    if (tag.getName().startsWith(puzzleName)) {
                        contents.putBoolean(tag.getName(), false);}
                }
            }
        }

        beif.namedTag = contents;

        ItemMap map = new ItemMap();
        BufferedImage image = null;
        try {
            File filePath = new File(Helper.getImagesDir().toFile(), "cross.png");
            image = ImageIO.read(filePath);
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
        map.setImage(image);
        beif.setItem(map);

    }

    private static BufferedImage getAnswerImage(boolean answer){
        BufferedImage image = null;
        try {
            File filePath = new File(Helper.getImagesDir().toFile(), answer ? "check.png" : "cross.png");
            image = ImageIO.read(filePath);
        }
        catch (IOException ioException) {
            //ioException.printStackTrace();
            String nameError = (TextFormat.RED + "No answer image file found in " + Helper.getImagesDir());
            Log.error(nameError);
        }
        return image;
    }

    public static int getChecksSolved() {
        return checksSolved;
    }

    public static void setChecksSolved(int checksSolved) {
        Checks.checksSolved = checksSolved;
    }

}
