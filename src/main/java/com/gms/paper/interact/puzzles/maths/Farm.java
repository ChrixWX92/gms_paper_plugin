package com.gms.paper.interact.puzzles.maths;

import cn.nukkit.Player;
import cn.nukkit.block.*;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.*;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.BoneMealParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.utils.TextFormat;
import com.gms.mc.Main;
import com.gms.mc.custom.particles.ParticleFX;
import com.gms.mc.custom.particles.ParticleFXSequence;
import com.gms.mc.custom.sound.*;
import com.gms.mc.error.InvalidFrameWriteException;
import com.gms.mc.interact.puzzles.MathsPuzzle;
import com.gms.mc.interact.puzzles.MathsTopic;
import com.gms.mc.interact.puzzles.Resettable;
import com.gms.mc.interact.puzzles.maths.threads.EntityMovement;
import com.gms.mc.util.Log;
import io.netty.util.internal.ThreadLocalRandom;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static cn.nukkit.level.Sound.NOTE_HARP;
import static com.gms.mc.interact.puzzles.PuzzleType.FARM;
import static com.gms.mc.interact.puzzles.maths.Farm.CropType.*;
import static com.gms.paper.interact.puzzles.maths.Farm.CropType.*;

public class Farm extends MathsPuzzle implements Resettable {

    /**
     <pre>
     <h1>FARM</h1>
     The player is given seeds for various crops. They must plant/remove crops as appropriate to create the sum total
     required. Various combinations of crops are used, through several stages of equations. The last stage of the puzzle
     requires all crops to total a certain amount. <br>
     </pre> <br>
     <h2>Tag Formats:</h2>
     <b>• Crop Frames</b> <br> <code>INT</code> - identifierStub + [name of crop's block's name*]<br> Value: Current numeric value of the frame. <br>
     <b>• Question Frames</b> <br> <code>INT</code> - questionStub<br> Value: Index of the frame in the question. <br>
     <b>• NPC</b> <br> <code>INT</code> - questionStub<br> Value: Index of the frame in the question. <br>
     <br>
     <h2>Mechanics:</h2>
     <b>• Reset</b> <br>

     <b>• Input</b> <br>

     <b>• Solution</b> <br>

     </pre>
     <br> <br> <br>
     * *String is trimmed, with "Block" removed
     */

    public enum CropType {
        WHEAT(295, 59, 206),
        CARROT(391, 141, 214),
        POTATO(392, 142, 243),
        BEETROOT(458, 244, 45);

        final int itemID;
        final int blockID;
        final int plotBlockID;
        final Item seed;
        final BlockCrops block;
        final Block plotBlock;
        final String name;
        BlockEntityItemFrame countFrame;
        String frameTag;

        CropType(int id, int blockID, int plotBlockID) {
            this.itemID = id;
            this.blockID = blockID;
            this.plotBlockID = plotBlockID;
            this.block = (BlockCrops) Block.get(this.blockID);
            this.seed = Item.get(this.itemID);
            this.plotBlock = Block.get(this.plotBlockID);
            this.name = this.block.getName().replaceAll("Block", "").trim();
        }

        public int getItemID() {return itemID;}
        public int getBlockID() {return blockID;}
        public int getPlotBlockID() {return plotBlockID;}
        public Item getSeed() {return seed;}
        public BlockCrops getBlock() {return block;}
        public Block getPlotBlock() {return plotBlock;}
        public String getName() {return name;}
        void setFrameTag(String frameTag) {this.frameTag = frameTag;}
        public BlockEntityItemFrame getCountFrame() {return countFrame;}
        public void setCountFrame(BlockEntityItemFrame countFrame) {this.countFrame = countFrame;}
    }

    private final HashMap<CropType, List<Location>> plots;
    private final HashMap<Location, CropType> plantedCrops;
    private HashMap<String, Integer> cropCounts;
    private final List<List<Object>> challenges;
    private List<BlockEntityItemFrame> questionFrames;
    private int currentChallenge;
    private final EntityHuman farmer;
    private static boolean finalQuestion = false;

    private final int maxCropAmount;
    private int challengeCount;
    private int cropInputValue;
    private int finalQuestionAnswer;

    public Farm (Player player, String id, MathsTopic topic) throws InvalidFrameWriteException {
        super(player, FARM, id, topic);

        this.plots = new HashMap<>();
        this.plantedCrops  = new HashMap<>();
        this.cropCounts = new HashMap<>();
        this.challenges  = new ArrayList<>();
        this.questionFrames = new ArrayList<>();
        this.farmer = (EntityHuman) Arithmetic.getMob(this.level, this.answerStub, EntityHuman.class);
        this.currentChallenge = 1;
        finalQuestion = false;

        //TODO: Backend
        switch (this.id) {
            case "1" -> {
                this.maxCropAmount = 5; //TODO: Difficulty-dependent
                this.plots.put(BEETROOT, new ArrayList<>());
                this.plots.put(CARROT, new ArrayList<>());
                this.plots.put(POTATO, new ArrayList<>());
                this.plots.put(WHEAT, new ArrayList<>());
                this.challengeCount = 4;
                if (this.farmer != null) this.farmer.setNameTag("Farmer");
            }
            default -> this.maxCropAmount = 5;
        }

        // Searching for all valid crop spaces within a specified radius of the player
        this.findPlots();
        // Clearing crop space and setting a random number of new crops down
        this.generateRandomCrops();
        // Updating amounts of crops and printing them to their respective item frames
        this.updateCropNumbers(true);
        // Giving seeds to the player
        this.giveSeeds(10);
        // Computing our challenges
        this.computeChallenges();
        // Populating our question frames with our first question
        this.populateQuestionFrames(this.challenges.get(0));
        // FX
        this.fx();
    }

    public void updateCropNumbers(boolean init) throws InvalidFrameWriteException {

        this.cropCounts = new HashMap<>();
        if (init) this.assignCropFrames();

        // Tallying planted crop numbers
        for (Map.Entry<Location, CropType> plantedCrop : this.plantedCrops.entrySet()) {
            String cropName = plantedCrop.getValue().name;
            this.cropCounts.put(cropName, this.cropCounts.get(cropName) == null ? 1 : this.cropCounts.get(cropName) + 1);
        }
        for (CropType cropType : this.plots.keySet()) {
            if (!this.plantedCrops.containsValue(cropType)) {
                this.cropCounts.put(cropType.name, 0);
            }
        }

        // Printing crop numbers to the relevant signs
        for (Map.Entry<String, Integer> cropCount : cropCounts.entrySet()) {
            for (CropType cropType : this.plots.keySet()) {
                if (cropType.name.equals(cropCount.getKey())) {
                    if (cropType.countFrame.namedTag.getInt(cropType.frameTag) != cropCount.getValue() || init) {
                        cropType.countFrame.namedTag.putInt(cropType.frameTag, cropCount.getValue());
                        Arithmetic.writeFrame(cropType.countFrame, cropCount.getValue(), 1);
                    }
                }
            }
        }
    }

    private void assignCropFrames() {
        for (CropType cropType : this.plots.keySet()) {
            cropType.setFrameTag(this.addTagModifier(this.identifierStub, cropType.name));
            BlockEntityItemFrame cropFrame = Arithmetic.getFrame(this.player, cropType.frameTag);
            if (cropFrame == null) {
                Log.error("FARM: No crop frame found for " + cropType.frameTag);
            } else {
                cropType.countFrame = cropFrame;
            }
        }
    }

    private void computeChallenges() { //TODO: Ideally needs making more modular to accommodate equations of varying lengths
        this.fetchQuestionFrames();
        MathsEngine mathsEngine = new MathsEngine(this.topic);
        for (int i = 0; i < this.challengeCount - 1; i++) {
            List<Object> challenge = new ArrayList<>();
            CropType[] cachedCropTypes = CropType.values().clone();

            int pick = new Random().nextInt(cachedCropTypes.length);
            CropType chosenCrop = cachedCropTypes[pick];
            // Index 0
            challenge.add(chosenCrop);
            cachedCropTypes = ArrayUtils.removeElement(cachedCropTypes, chosenCrop);

            // Index 1
            challenge.add(this.topic.getAssociatedOperator().value);

            pick = new Random().nextInt(cachedCropTypes.length);
            chosenCrop = cachedCropTypes[pick];
            // Index 2
            challenge.add(chosenCrop);

            // Index 3
            challenge.add(MathsEngine.Operator.EQUALS.value);

            // Index 4
            challenge.add(mathsEngine.generateInteger(2, this.maxCropAmount*2));

            this.challenges.add(challenge);
        }

        List<Object> challenge = new ArrayList<>();
        for (int i = 0 ; i < 3 ; i++) challenge.add(Integer.MAX_VALUE-i);
        challenge.add(MathsEngine.Operator.EQUALS.value);
        challenge.add(MathsEngine.Operator.QUESTION_MARK.value);
        this.challenges.add(challenge);

    }

    private void populateQuestionFrames(List<Object> challenge) throws InvalidFrameWriteException {
        for (int i = 0 ; i < challenge.size() ; i++) {
            BlockEntityItemFrame questionFrame = this.questionFrames.get(i);
            if (this.challenges.indexOf(challenge) == (this.challenges.size()-1)) {
                switch (i) {
                    case 0, 1, 2 -> Arithmetic.writeFrame(questionFrame, Integer.MAX_VALUE-i, 7);
                    case 3 -> Arithmetic.writeFrame(questionFrame, MathsEngine.Operator.EQUALS.value, 1);
                    case 4 -> Arithmetic.writeFrame(questionFrame, MathsEngine.Operator.QUESTION_MARK.value, 1);
                }
            } else {
                if (challenge.get(i) instanceof Integer)
                    Arithmetic.writeFrame(questionFrame, (int) challenge.get(i), 1);
                else if (challenge.get(i) instanceof CropType)
                    Arithmetic.writeFrame(questionFrame, ((CropType) challenge.get(i)).getItemID(), 7);
            }
        }
    }

    private void fetchQuestionFrames(){
        this.questionFrames = new ArrayList<>();
        for (int i = 0 ; ; i++){
            BlockEntityItemFrame newAnswer = Arithmetic.getFrame(this.player, this.questionStub, i+1);
            if (newAnswer == null) {
                break;
            }
            this.questionFrames.add(i, newAnswer);
        }
    }

    private void findPlots() {
        Block testBlock;
        double startX = this.player.x - 40;
        double startY = this.player.y - 7;
        if (startY <= 0) {
            startY = 1;
        }
        double startZ = this.player.z - 40;
        double endX = this.player.x + 40;
        double endY = this.player.y + 7;
        double endZ = this.player.z + 40;

        for (double z = startZ; z < endZ; z++) {
            for (double y = startY; y < endY; y++) {
                for (double x = startX; x < endX; x++) {
                    Location testLocation = new Location (x, y, z, this.level);
                    testBlock = this.level.getBlock(testLocation);
                    // Testing for farmland blocks and whatever is two blocks below them
                    // Plot blocks won't be detected unless they're two blocks below farmland
                    if (this.level.getBlock(testBlock.add(new Location (0, 2))).getId() == 60) {
                        for (Map.Entry<CropType, List<Location>> plot : this.plots.entrySet()) {
                            if (testBlock.getId() == plot.getKey().plotBlockID) {
                                plot.getValue().add(testLocation.add(new Location(0, 3)));
                            }
                        }
                    }
                }
            }
        }
    }

    private void generateRandomCrops(){
        for (Map.Entry<CropType, List<Location>> plot : this.plots.entrySet()){
            List<Location> locations = plot.getValue();
            // Resetting all plots spaces to empty
            for (Location l : locations) {
                if (!(l.getLevel().getBlock(l) instanceof BlockAir)) {
                    l.getLevel().setBlock(l, new BlockAir());
                }
            }
            // Picking random spots in the plot and assigning that plot's crop to them
            int cropCount = ThreadLocalRandom.current().nextInt(1,this.maxCropAmount+1);
            Collections.shuffle(locations);
            List<Location> cropLocations = locations.subList(0, cropCount);
            for (Location l : cropLocations) {
                if (this.level.getBlock(l) instanceof BlockAir) {
                    BlockCrops crop = plot.getKey().block;
                    if (crop != null) {
                        crop.setDamage(7);
                        this.level.setBlock(l, crop);
                        this.plantedCrops.put(crop.getLocation(), plot.getKey());
                    }
                }
            }
        }
    }

    private void giveSeeds(int total) {
        PlayerInventory inventory = this.player.getInventory();
        for (CropType cropType : this.plots.keySet()) {
            Item seed = cropType.seed;
            if (inventory.contains(seed)) inventory.remove(seed);
            if (this.cropCounts.get(cropType.name) != null) {
                seed.setCount(total - this.cropCounts.get(cropType.name));
                inventory.setItem(inventory.firstEmpty(seed), seed);
            }
        }
    }

    public void plant(CropType cropType, Location location) throws InvalidFrameWriteException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        BlockCrops crop;
        Item seed = this.player.getInventory().getItemInHand();
        seed.setCount(seed.getCount()-1);
        this.player.getInventory().setItemInHand(seed);
        crop = cropType.getBlock().getClass().getConstructor().newInstance();
        crop.setDamage(7);
        this.level.setBlock(location, crop);
        this.level.addSound(this.player.getLocation(), Sound.USE_STEM);
        this.level.addParticle(new BoneMealParticle(new Vector3(location.add(0.5).x, location.add(0,0.5).y, location.add(0,0,0.5).z)));
        this.plantedCrops.put(crop.getLocation(), cropType);
        this.updateCropNumbers(false);
    }

    public void uproot(BlockCrops crop) throws InvalidFrameWriteException {
        this.level.setBlock(crop, new BlockAir());
        PlayerInventory inventory = this.player.getInventory();
        boolean found = false;
        for (CropType cropType : CropType.values()) {
            if (cropType.blockID == crop.getId()) {
                for (Map.Entry<Integer, Item> slot : inventory.getContents().entrySet()) {
                    Item item = slot.getValue();
                    if (item.getId() == cropType.itemID) {
                        item.setCount(item.getCount()+1);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    Item seed = Item.get(cropType.itemID);
                    seed.setCount(1);
                    inventory.addItem(seed);
                }
            }
        }
        this.plantedCrops.remove(crop.getLocation());
        this.updateCropNumbers(false);
        this.level.addSound(this.player.getLocation(), Sound.BLOCK_COMPOSTER_EMPTY);
        ParticleFXSequence tpFX = new ParticleFXSequence(ParticleFX.FARM_UPROOT, this.level, crop.add(0,-1));
        synchronized (tpFX) {
            tpFX.run();
        }
    }

    @Override
    public void reset() throws InvalidFrameWriteException, InterruptedException {
        PlayerInventory inventory = this.player.getInventory();
        int[] itemIDs = new int[this.plots.size()];
        int index = 0;
        for (CropType crop: this.plots.keySet()) {
            itemIDs[index] = crop.itemID;
            index++;
        }
        for (Map.Entry<Integer, Item> itemSlot : inventory.getContents().entrySet()) {
            if (ArrayUtils.contains(itemIDs, itemSlot.getValue().getId())) {
                inventory.clear(itemSlot.getKey());
            }
        }
        Farm newFarm = new Farm(this.player, this.id, this.topic);
        Arithmetic.currentPuzzle = newFarm;
        MATHS_InteractionHandler.reinitializePuzzle(newFarm);
    }

    @Override
    public boolean solve() throws InvalidFrameWriteException, InterruptedException, CloneNotSupportedException {
        if (Arithmetic.currentPuzzle != null && Arithmetic.currentPuzzle.getName().equals(this.name)) {
            if (this.currentChallenge > this.challengeCount) {
                return this.puzzleCorrect();
            }
            List<Object> challenge = this.challenges.get(this.currentChallenge - 1);
            int[] sum = new int[challenge.size()];
            int index = 0;
            for (Object element : challenge) {
                if (element instanceof Integer i) sum[index] = i;
                else if (element instanceof CropType crop) {
                    sum[index] = crop.countFrame.namedTag.getInt(crop.frameTag);
                }
                index++;
            }
            int solution = new MathsEngine(this.topic).solveSum(sum);
            if (this.currentChallenge == this.challengeCount) {
                player.sendMessage("Let's try talking to the farmer!");
                if (finalQuestion)
                    // Mid-final question
                    return false;
            }
            if (solution == (int) challenge.get(challenge.size()-1)) {
                this.challengeCorrect();
                if (this.currentChallenge == this.challengeCount) {
                    player.sendMessage("Let's try talking to the farmer!");
                    if (finalQuestion)
                        // Mid-final question
                        return false;

                    // Start final question
                    int allCrops = 0;
                    for (int i : this.cropCounts.values()) allCrops += i;
                    this.finalQuestionAnswer = allCrops;
                    float min = ThreadLocalRandom.current().nextInt(allCrops-10, allCrops+1);
                    this.cropInputValue = (int) ((Math.floor(min/10)) * 10);
                    finalQuestion = true;
                    this.farmer.setNameTag(TextFormat.AQUA + "Talk to me!\n" + TextFormat.WHITE + this.farmer.getName());
                    new EntityMovement(this.farmer);
                }
                this.populateQuestionFrames(this.challenges.get(this.currentChallenge-1));
                return true;
            } else {
                // Incorrect
                Arithmetic.mark(this.player, false);
                return false;
            }
        }
        return false;
    }

    //TODO: Common method among different puzzle types; can we code this differently?
    private void challengeCorrect() throws InvalidFrameWriteException {
        this.player.sendTitle("§2Correct!","",5, 75, 5);
        switch (this.currentChallenge){ //TODO: Rigid amount of stages
            case 4: {
                Chord correct1 = new Chord(Note.C4, ChordType.MAJ, 1, false);
                Chord correct2 = new Chord(Note.C5, ChordType.MAJ, 1, false);
                Chord[] chords = new Chord[]{correct1, correct2};
                MusicMaker.playArpeggio(this.player, chords, 50, NOTE_HARP);
                break;
            }
            case 3: MusicMaker.playNote(this.player, Note.G5, NOTE_HARP,0.75F);
            case 2: MusicMaker.playNote(this.player, Note.E5, NOTE_HARP,0.75F);
            case 1: MusicMaker.playNote(this.player, Note.C4, NOTE_HARP,0.75F);
        }
        this.currentChallenge++;
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
                } catch (InvalidFrameWriteException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskLater(Main.s_plugin, 60);

        for (BlockEntityItemFrame beif : questionFrames) {
            ParticleFXSequence pFX = new ParticleFXSequence(ParticleFX.COMPLETE, beif.getLevel(), beif.getLocation());
            synchronized (pFX) {
                pFX.run();
            }
        }
        return true;
    }

    private void fx() {
        Chord[] chords = new Chord[2];
        chords[0] = new Chord(Note.E3, ChordType.SIX);
        chords[1] = new Chord(Note.E4, ChordType.MAJ, 0, true);
        MusicMaker.playNote(player, Note.E3, Sound.NOTE_BASS);
        MusicMaker.playArpeggio(player, chords, 80,  Sound.NOTE_BANJO);
        MusicMaker.playSFX(SFX.Type.FARM_BEGIN, player, 70, this.plots.size() + this.questionFrames.size());
        for (List<Location> plot : this.plots.values()) {
            for (Location space : plot) {
                ParticleFXSequence tpFX = new ParticleFXSequence(ParticleFX.FARM_START, this.level, space);
                tpFX.run();
            }
        }
    }

    public int getCurrentChallenge() {
        return currentChallenge;
    }

    public void setCurrentChallenge(int currentChallenge) {
        this.currentChallenge = currentChallenge;
    }

    public boolean isFinalQuestion() {
        return finalQuestion;
    }

    public void setFinalQuestion(boolean finalQuestion) {
        finalQuestion = finalQuestion;
    }

    public int getCropInputValue() {
        return cropInputValue;
    }

    public int getFinalQuestionAnswer() {
        return finalQuestionAnswer;
    }

}
