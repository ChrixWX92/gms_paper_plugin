package com.gms.paper.interact.puzzles.maths;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.Tag;
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
import com.gms.mc.util.Log;
import io.netty.util.internal.ThreadLocalRandom;

import java.util.*;

import static cn.nukkit.level.ParticleEffect.CAMERA_SHOOT_EXPLOSION;
import static cn.nukkit.level.Sound.NOTE_HARP;
import static cn.nukkit.level.Sound.NOTE_XYLOPHONE;
import static com.gms.mc.interact.puzzles.MathsTopic.ADDITION;
import static com.gms.mc.interact.puzzles.PuzzleType.TOWER;

public class Tower extends MathsPuzzle implements Resettable{

    /**
     <pre>
     <h1>TOWER</h1>
     I THINK THE PLAYER SHOULD GO DOWN A FLOOR ON A WRONG ANSWER
     The player must climb a tower by solving an equation on each floor. Answering correctly teleports the player up a floor,
     where they'll be presented with a slightly more difficult equation. The player completes the puzzle by reaching the top
     of the tower. <br>
     </pre> <br>
     <h2>Tag Formats:</h2>
     <b>• Answer Frame</b> <br> <code>INT</code> - answerStub + floor number + frame ID<br> Value: Current numeric value of the frame. <br>
     <b>• Question Frames</b> <br> <code>INT</code> - questionStub + floor number<br> Value: Index of the frame in the question. <br>
     <br>
     <h2>Mechanics:</h2>
     <b>• Reset</b> <br>
     All floors' equations and answers are redone - player teleports to the bottom of the tower and can start the puzzle again from the very beginning.
     One of the answer frames found, per floor, is populated with that floor's correct answer, the remainder are populated with red herrings.
     <b>• Input</b> <br>
     The player must press a button that triggers a sign formatted like so:<br><br>
     TWR<br>
     [puzzle ID)<br>
     [frame ID]<br> <br>
     with the frame ID being provided in the answer frame's answer tag. The value assigned to the answer frame is then
     checked against the answer for the current floor being played to determine whether the player has answered
     correctly. <br>
     The player also has the opportunity to exit the puzzle prematurely from any floor. Floors higher than the ground floor
     should feature buttons activating signs that read:<br><br>
     TWR<br>
     EXIT<br> <br>
     This will deactivate puzzle and teleport the player to the ground floor.<br><br>

     <b>• Solution</b> <br>
     <code><var>this.currentFloor</var></code> keeps track of the player's current stage in the puzzle.
     After ascending each floor (with inputs being checked against correct answers on each stage), <code><var>this.floors.length</var></code>
     is checked against <code><var>this.currentFloor</var></code> to determine if the player reached the top of the tower. If they did, the
     puzzle is marked as correct. The player can then choose between resetting, by speaking with the tower's NPC, or exiting by pressing
     the top floor's exit sign.
     </pre>
     <br> <br> <br>
     */

    private int currentFloor = 0;
    private int floorSize;
    private int answersPerFloor;
    private final ArrayList<Set<BlockEntityItemFrame>> answerFrames;
    private List<List<BlockEntityItemFrame>> questionFrames;
    private int[][] floors;
    private int[] answers;
    private int maxValue;


    public Tower (Player player, String id, MathsTopic topic) throws InvalidFrameWriteException {
        super(player, TOWER, id, topic);

        MathsEngine mathsEngine = new MathsEngine(this.topic);

        switch (this.id) {
            case "1", "2" -> {
                //How many floors are in the tower?
                this.currentFloor = 1;
                this.floorSize = 8;
                this.answersPerFloor = 4;
                this.floors = new int[4][];
                this.answers = new int[4];
                this.maxValue = this.floors.length * 5;
                this.questionFrames = new ArrayList<>();
                player.sendMessage(this.topic.name());
            }
        }

        this.answerFrames = new ArrayList<>();

        //Generate sums
        for (int i = 0 ; i < this.floors.length ; i++){
            this.floors[i] = mathsEngine.setAddQuestionMark(true)
                .generateSum(this.maxValue / (this.floors.length-i)); //Max algorithm
            switch (this.topic) {
                case ADDITION -> this.answers[i] = (this.floors[i][0] + this.floors[i][2]);
                case SUBTRACTION -> this.answers[i] = (this.floors[i][0] - this.floors[i][2]);
                default -> Log.error("We haven't written this yet.");
            }
        }

        this.populateQuestionFrames();
        this.populateAnswerFrames();

        //TODO Find answer item frames code needs replacing with appropriate method throughout

        // Change floor sizes for irregular towers here

        //Starting anew
        MusicMaker.playSFX(SFX.Type.TOWER_BEGIN, player);
        player.sendTitle(TextFormat.LIGHT_PURPLE +"Let's climb the tower!", "§fGood luck!");

    }

    private void populateQuestionFrames() throws InvalidFrameWriteException {
        int index = 0;
        for (int[] question : this.floors) {
            index++;
            String concatQF = this.addTagModifier(this.questionStub, index);
            List<BlockEntityItemFrame> floorFrames = new ArrayList<>();
            int index2 = 0;
            for (int element : question) {
                index2++;
                BlockEntityItemFrame questionFrame = Arithmetic.getFrame(this.player, concatQF, index2);
                Arithmetic.writeFrame(questionFrame, element, 1);
                floorFrames.add(questionFrame);
            }
            this.questionFrames.add(floorFrames);
        }
    }

    private void populateAnswerFrames() throws InvalidFrameWriteException {

        for (int floor = 1 ; floor <= this.floors.length ; floor++) {

            Set<BlockEntityItemFrame> floorFrames = new HashSet<>();
            int answer = this.answers[floor-1];

            Set<BlockEntityItemFrame> cachedFrames = this.collectAnswerFrames(floor);
            BlockEntityItemFrame chosenFrame = this.setAnswerToRandomFrame(cachedFrames, answer);
            cachedFrames.remove(chosenFrame);
            floorFrames.add(chosenFrame);

            //Set random numbers to all other answer item frames
            //Below algorithm reflects increasing difficulty as you climb
            int max = this.maxValue / (this.floors.length-(floor-1));
            Set<Integer> wrongAnswers = new HashSet<>(Collections.emptySet());
            for (BlockEntityItemFrame wrongFrame : cachedFrames) {
                int wrongAnswer = ThreadLocalRandom.current().nextInt(0, max + 1); //TODO: Have maths engine do this
                while (wrongAnswer == this.answers[floor-1] || wrongAnswers.contains(wrongAnswer)) {
                    wrongAnswer = ThreadLocalRandom.current().nextInt(0, max + 1);
                }
                wrongAnswers.add(wrongAnswer);
                Arithmetic.writeFrame(wrongFrame, wrongAnswer, 1);
                String frameTag = null;
                for (Tag tag : wrongFrame.namedTag.getAllTags()) {
                    if (tag.getName().startsWith(this.answerStub)) {
                        frameTag = tag.getName();
                    }
                }
                if (frameTag != null) wrongFrame.namedTag.putInt(frameTag, wrongAnswer);
                floorFrames.add(wrongFrame);
            }
            this.answerFrames.add(floor-1, floorFrames);

            for (BlockEntityItemFrame frame : floorFrames) {
                this.level.addParticleEffect(new Vector3(frame.add(0.5).x, frame.add(0,0.5).y, frame.add(0,0,0.5).z),CAMERA_SHOOT_EXPLOSION);
            }

        }

    }

    private void climb() throws InvalidFrameWriteException {
        this.currentFloor++;
        MusicMaker.playSFX(SFX.Type.TOWER_CLIMB, this.player);
        this.player.teleport(new Vector3(this.player.x,(this.player.y+this.floorSize), this.player.z));
        ParticleFXSequence tpFX = new ParticleFXSequence(ParticleFX.TOWER_TELEPORT, this.level, this.player.getLocation());
        new NukkitRunnable() {
            @Override
            public void run() {
                synchronized (tpFX) { //TODO: Runnable delay
                    tpFX.run();
                }
            }
        }.runTaskLater(Main.s_plugin, 1);

        if (this.currentFloor == this.floors.length){
            return;
        }

        this.populateQuestionFrames();
        this.populateAnswerFrames();
    }

    private boolean markTowerFloor(boolean result, String sub) {

        Note[] allNotes = Note.getNotes();
        Note arpRoot = allNotes[((Arrays.asList(allNotes).indexOf(Note.C5))-(this.floors.length-(this.currentFloor+1)))];
        Note fail = allNotes[((Arrays.asList(allNotes).indexOf(Note.C5))-(24+(this.floors.length-(this.currentFloor+1))))];

        if (result) {
            this.player.sendTitle("§2Correct!",sub);
            Chord correct = new Chord(arpRoot, ChordType.MAJ, 1, false);
            MusicMaker.playArpeggio(this.player,correct,105,NOTE_HARP);
            return true;
        } else {
            this.player.sendTitle(TextFormat.GOLD + "Not quite, \nbut try again!", sub);
            MusicMaker.playNote(this.player,fail,NOTE_XYLOPHONE);
            return false;
        }
    }

    @Override
    public void reset() throws InvalidFrameWriteException, InterruptedException {

        //Starting anew from the bottom of the tower
        this.player.teleport(new Location(this.player.x, (this.player.y - (this.floorSize * (this.currentFloor - 1))), this.player.z, 180));
        MusicMaker.playSFX(SFX.Type.TOWER_BEGIN, this.player);
        ParticleFXSequence tpFX = new ParticleFXSequence(ParticleFX.TOWER_TELEPORT, this.level, this.player.getLocation());
        synchronized (tpFX) {
            tpFX.run();
        }
        this.player.sendTitle(TextFormat.LIGHT_PURPLE + "Let's climb the tower\nagain!", "§fGood luck!");
        new Tower(this.player, this.id, this.topic);
    }

    public void exit() throws InvalidFrameWriteException {

        //Player returns to floor 1
        MusicMaker.playSFX(SFX.Type.TOWER_FALL, this.player);
        this.player.teleport(new Vector3(this.player.x, (this.player.y - (this.floorSize * (this.currentFloor-1))), this.player.z));
        ParticleFXSequence tpFX = new ParticleFXSequence(ParticleFX.TOWER_TELEPORT, this.level, this.player.getLocation());
        new NukkitRunnable(){
            @Override
            public void run() {
                tpFX.run();
            }
        }.runTaskLater(Main.s_plugin, 2);
        this.player.sendTitle(TextFormat.AQUA + "Maybe next time!", TextFormat.ITALIC + "§fPress \"RESET CHALLENGE\" to try again!");

        //Locate the answer item frames
        for (Set<BlockEntityItemFrame> answerFramesSet : this.answerFrames) {
            for (BlockEntityItemFrame blockEntityItemFrame : answerFramesSet) {
                Arithmetic.writeFrame(blockEntityItemFrame, 2147483642, 1);
                this.level.addParticleEffect(new Vector3(blockEntityItemFrame.add(0.5).x, blockEntityItemFrame.add(0, 0.5).y, blockEntityItemFrame.add(0, 0, 0.5).z), CAMERA_SHOOT_EXPLOSION);
            }
        }
        for (List<BlockEntityItemFrame> floorQuestionFrames : this.questionFrames) {
            for (BlockEntityItemFrame blockEntityItemFrame : floorQuestionFrames) {
                Arithmetic.writeFrame(blockEntityItemFrame, 2147483642, 1);
                this.level.addParticleEffect(new Vector3(blockEntityItemFrame.add(0.5).x, blockEntityItemFrame.add(0, 0.5).y, blockEntityItemFrame.add(0, 0, 0.5).z), CAMERA_SHOOT_EXPLOSION);
            }
        }

        Arithmetic.puzzleName = null; //TODO: Full exit out of puzzle
    }

    @Override
    public boolean solve() throws InvalidFrameWriteException, InterruptedException, CloneNotSupportedException {

        this.climb();
        if (this.currentFloor > (this.floors.length)) {
            Arithmetic.mark(this.player, true, (TextFormat.GREEN + "Congratulations! " + "§fYou've defeated the tower!"), true);
        } else {
            markTowerFloor(true, (TextFormat.ITALIC + "§fUp we go!"));
        }
        return true;
    }

    private Set<BlockEntityItemFrame> collectAnswerFrames(int floor) {
        Set<BlockEntityItemFrame> cachedFrames = new HashSet<>();

        for (int i = 1 ; i <= this.answersPerFloor ; i++) {
            String answerFrameTag = addTagModifier(addTagModifier(this.answerStub, floor), i);
            BlockEntityItemFrame frame = Arithmetic.getFrame(this.player, answerFrameTag);
            cachedFrames.add(frame);
        }

        return cachedFrames;
    }

    private BlockEntityItemFrame setAnswerToRandomFrame(Set<BlockEntityItemFrame> cachedFrames, int answer) throws InvalidFrameWriteException {

        //Get random frame
        Random random = new Random();
        int which = random.nextInt(cachedFrames.size());
        Iterator<BlockEntityItemFrame> iter = cachedFrames.iterator();
        for (int j = 0; j < which; j++) {
            iter.next();
        }
        BlockEntityItemFrame chosenFrame = iter.next();

        //Set the answer to the frame
        String answerFrameTag = null;
                Arithmetic.writeFrame(chosenFrame, answer,1);
        for (Tag tag : chosenFrame.namedTag.getAllTags()) {
            if (tag.getName().startsWith(this.answerStub)) {
                answerFrameTag = tag.getName();
            }
        }
        if (answerFrameTag != null) {
            chosenFrame.namedTag.putInt(answerFrameTag, answer);
        }
        return chosenFrame;
    }

    public boolean parseGuess(String guessFrame) throws InvalidFrameWriteException, InterruptedException, CloneNotSupportedException {

        // Getting chosen answer frame value
        String concatTag = addTagModifier(addTagModifier(answerStub, this.currentFloor), Integer.parseInt(guessFrame));
        BlockEntityItemFrame aFrame = Arithmetic.getFrame(this.player,concatTag);
        int v;

        if (aFrame != null) {
            v = aFrame.namedTag.getInt(concatTag);
        } else {
            Log.error(TextFormat.RED + "No item frame found with Integer tag " + TextFormat.WHITE + concatTag + TextFormat.RED + ".");
            return false;
        }

        if (Arithmetic.currentPuzzle != null) {
            if (Arithmetic.currentPuzzle.getPuzzleType() == TOWER) {
                if (v == this.answers[this.currentFloor-1]) { //TODO: Where does solve() fit in?
                    this.solve();
                } else {
                    markTowerFloor(false, " ");
                    return false;
                }
            }
        }
        return false;
    }

}
