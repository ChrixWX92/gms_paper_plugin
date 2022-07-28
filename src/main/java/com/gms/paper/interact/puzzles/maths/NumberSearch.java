package com.gms.paper.interact.puzzles.maths;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.level.particle.BoneMealParticle;
import cn.nukkit.level.particle.SporeParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.utils.TextFormat;
import com.gms.mc.Main;
import com.gms.mc.custom.sound.*;
import com.gms.mc.error.InvalidFrameWriteException;
import com.gms.mc.interact.puzzles.MathsPuzzle;
import com.gms.mc.interact.puzzles.MathsTopic;
import com.gms.mc.interact.puzzles.PuzzleType;
import com.gms.mc.interact.puzzles.Resettable;
import com.gms.mc.util.Log;
import io.netty.util.internal.ThreadLocalRandom;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.nukkit.level.ParticleEffect.CAMERA_SHOOT_EXPLOSION;
import static cn.nukkit.level.Sound.NOTE_HARP;
import static cn.nukkit.level.Sound.PLACE_LARGE_AMETHYST_BUD;

public class NumberSearch extends MathsPuzzle implements Resettable {

    /**
     <pre>
     <h1>NUMBERSEARCH</h1>
     The player must locate the answer to each challenge in order, pressing a button below an answer frame to submit the
     value on that frame. If they are correct, the next challenge will begin. Once all challenges have been solved, the
     puzzle is complete. This puzzle format can feature any amount of answer signs.<br>
     </pre> <br>
     <h2>Tag Formats:</h2>
     <b>• Answer Frame</b> <br> <code>INT</code> - answerStub + frame ID<br> Value: Current numeric value of the frame. <br>
     <b>• Question Frames</b> <br> <code>INT</code> - questionStub<br> Value: Index of the frame in the question. <br>
     <b>• Challenge Frames</b> <br> <code>BOOLEAN</code> - identifierStub + challenge number<br> Value: Challenge complete? <br>
     <br>
     <h2>Mechanics:</h2>
     <b>• Reset</b> <br>
     All challenges and answers are redone - puzzle starts from the very beginning: A pre-determined amount of arithmetic
     questions (challenges) are generated and their answers spread randomly on item frames around the puzzle area, along
     with red herrings if more answer frames than challenges are provided. <br>
     <b>• Input</b> <br>
     The player must press a button that triggers a sign formatted like so:<br><br>
     NSR<br>
     [puzzle ID)<br>
     [frame ID]<br> <br>
     with the frame ID being provided in the answer frame's answer tag. The value assigned to the answer frame is then
     checked against the answer for the current challenge being played to determine whether the player has answered
     correctly. <br>
     <b>• Solution</b> <br>
     The player's progress through the question is determined by its challenge frames' identifier tags. After completing
     each challenge, the respective challenge's challenge frame's identifier tag boolean will turn to <code>true</code>. These
     signs' tags are counted each time the player correctly solves a challenge to determine which stage that player is at.
     The is tracked with <var>this.challengesComplete</var>. If <code><var>this.challengesComplete</var> == <var>this.challengeAmount</var></code>
     then the puzzle is completed.
     </pre>
     <br> <br> <br>
     */

    private final int[] answers;
    private int guess;
    private final int[][] challenges;
    private int challengeAmount;
    private int challengesComplete = 0;
    private List<BlockEntityItemFrame> questionFrames; // TODO: This should really be used
    private Map<Integer, BlockEntityItemFrame> answerFrames;
    private Map<Integer, BlockEntityItemFrame> challengeFrames;
    private int maxBound; //TODO: This upper bound is dependent on difficulty

    public NumberSearch(Player player, String id, MathsTopic topic) throws InvalidFrameWriteException {
        super(player, PuzzleType.NUMBERSEARCH, id, topic);

        MathsEngine mathsEngine = new MathsEngine(this.topic).setSameAversion(10).setAddQuestionMark(true);

        //TODO: BACKEND INTEGRATION - Get content based on ID
        switch (this.id) {
            case "1", "2" -> {
                this.maxBound = 20;
                this.challengeAmount = 5;
            }
        }

        this.challenges = new int[this.challengeAmount][];
        this.answers = new int[this.challengeAmount];

        for (int i = 0 ; i < this.challenges.length ; i++){
            this.challenges[i] = mathsEngine.setAddQuestionMark(true)
                    .generateSum(20);

            switch (this.topic) {
                case ADDITION -> this.answers[i] = (this.challenges[i][0] + this.challenges[i][2]);
                case SUBTRACTION -> this.answers[i] = (this.challenges[i][0] - this.challenges[i][2]);
                default -> Log.error("We haven't written this yet.");
            }

        }
        updateQuestionFrames(this.challenges[challengesComplete]);

        // Set the answers to frames
        this.answerFrames = fetchAnswerFrames();
        this.resetAnswerFrames();
        
        // Find and reset challenge frames
        this.challengeFrames = this.fetchChallengeFrames();
        this.resetChallengeFrames();

        MusicMaker.playSFX(SFX.Type.NSEARCH_BEGIN,this.player,75,7);

    }

    //TODO: Common method among different puzzle types; can we code this differently?
    private void challengeCorrect() throws InvalidFrameWriteException {

        for (Map.Entry<Integer, BlockEntityItemFrame> entry : this.challengeFrames.entrySet()) {
            if (entry.getKey() == this.challengesComplete+1) {
                writeChallengeFrame(entry, true);
                break;
            }

        }
        this.player.sendTitle("§2Correct!","",5, 75, 5);
        switch (this.challengesComplete){ //TODO: Rigid amount of stages
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
            case 0: MusicMaker.playNote(this.player, Note.G4, NOTE_HARP,0.75F);
        }
        this.challengesComplete++;
    }

    private Map<Integer, BlockEntityItemFrame> fetchAnswerFrames(){
        this.answerFrames = new HashMap<>();
        Map<Integer, BlockEntityItemFrame> answerFrames = new HashMap<>();
        for (int i = 1 ; ; i++){
            String concatAF = this.addTagModifier(this.answerStub, i);
            BlockEntityItemFrame newAnswer = Arithmetic.getFrame(this.player, concatAF);
            if (newAnswer == null) {
                break;
            }
            answerFrames.put(i, newAnswer);
        }
        return answerFrames;
    }

    private Map<Integer, BlockEntityItemFrame> fetchChallengeFrames() throws InvalidFrameWriteException {
        this.challengeFrames = new HashMap<>();
        Map<Integer, BlockEntityItemFrame> challengeFrames = new HashMap<>();
        for (int i = 1 ; i <= this.challengeAmount ; i++){
            String concatCF = this.addTagModifier(this.identifierStub, i);
            BlockEntityItemFrame newChallenge = Arithmetic.getFrame(this.player, concatCF);
            if (newChallenge == null) {
                throw new InvalidFrameWriteException(2);
            } else {
                challengeFrames.put(i, newChallenge);
            }
        }
        return challengeFrames;
    }

    private void populateRandom(List<Map.Entry<Integer, BlockEntityItemFrame>> remainingFrames) throws InvalidFrameWriteException {
        //Set random numbers to all other answer item frames
        //TODO: Make sure red herring frames don't have the same answers as well. ALSO, ensure this.answers only contains unique values.
        for (Map.Entry<Integer, BlockEntityItemFrame> entry : remainingFrames){
            BlockEntityItemFrame beif = entry.getValue();
            int wrongAnswer = ThreadLocalRandom.current().nextInt(0, this.maxBound+1);
            while(ArrayUtils.contains(this.answers, wrongAnswer)) {
                wrongAnswer = ThreadLocalRandom.current().nextInt(0, this.maxBound + 1);
            }
            Arithmetic.writeFrame(beif, wrongAnswer, 1);
            beif.namedTag.putInt(this.addTagModifier(this.answerStub, entry.getKey()), wrongAnswer);
            this.level.addParticleEffect(new Vector3(beif.add(0.5).x, beif.add(0,0.5).y, beif.add(0,0,0.5).z),CAMERA_SHOOT_EXPLOSION);
        }
    }

    private void writeChallengeFrame(Map.Entry<Integer, BlockEntityItemFrame> entry, boolean value) throws InvalidFrameWriteException {
        BlockEntityItemFrame frame = entry.getValue();
        if (frame == null) {return;}
        frame.namedTag.putBoolean(this.addTagModifier(this.identifierStub, entry.getKey()), value);
        if (value) {
            this.level.addParticle(new BoneMealParticle(new Vector3(frame.add(0.5).x, frame.add(0,0.5).y, frame.add(0,0,0.5).z)));
        } else {
            this.level.addParticle(new SporeParticle(new Vector3(frame.add(0.5).x, frame.add(0,0.5).y, frame.add(0,0,0.5).z)));
        }
        Arithmetic.writeFrame(frame, value ? 1 : 0, 3);
        this.level.addSound(this.player.getPosition(), PLACE_LARGE_AMETHYST_BUD, 1F, 1.5F);
    }

    private void resetChallengeFrames() throws InvalidFrameWriteException {
        MusicMaker.playSFX(SFX.Type.PEN_BEGIN, this.player,50, this.challengeAmount); //TODO: Correct sound?
        for (Map.Entry<Integer, BlockEntityItemFrame> frame : this.challengeFrames.entrySet()) {
            writeChallengeFrame(frame, false);
            frame.getValue().namedTag.putBoolean(this.addTagModifier(this.identifierStub, frame.getKey()), false);
        }
        this.challengesComplete = 0;
    }

    private void updateQuestionFrames(int[] challenge) throws InvalidFrameWriteException {
        //todo: sfx
        for (int i = 0 ; i < challenge.length ; i++) Arithmetic.findFrame(this.player, this.questionStub, i+1, challenge[i], 1);
    }

    private void resetAnswerFrames() throws InvalidFrameWriteException {
        List<Map.Entry<Integer, BlockEntityItemFrame>> cachedFrames = new ArrayList<>(List.copyOf(this.answerFrames.entrySet()));// <Map.Entry<Integer, BlockEntityItemFrame>> cachedFrames = this.answerFrames.entrySet();
        for (int answer : this.answers) {
            int frameID = ThreadLocalRandom.current().nextInt(0, cachedFrames.size());
            BlockEntityItemFrame chosenFrame = cachedFrames.get(frameID).getValue();
            Arithmetic.writeFrame(chosenFrame, answer, 1);
            chosenFrame.namedTag.putInt(this.addTagModifier(this.answerStub, cachedFrames.get(frameID).getKey()), answer);
            cachedFrames.remove(frameID);
            this.level.addParticleEffect(new Vector3(chosenFrame.add(0.5).x, chosenFrame.add(0, 0.5).y, chosenFrame.add(0, 0, 0.5).z), CAMERA_SHOOT_EXPLOSION);
        }
        // Randomly populate any remaining frames
        this.populateRandom(cachedFrames);
    }

    @Override
    public void reset() throws InvalidFrameWriteException, InterruptedException {
        new NumberSearch(this.player, this.id, this.topic);
    }

    @Override
    public boolean solve() throws InvalidFrameWriteException, InterruptedException, CloneNotSupportedException {

        if (Arithmetic.currentPuzzle != null && Arithmetic.currentPuzzle.getName().equals(this.name)) {
            // Correct
            if (this.guess == this.answers[this.challengesComplete]) {
                this.challengeCorrect();
                if (this.challengesComplete == this.challenges.length) {
                    Arithmetic.mark(this.player, true, "", false);
                    Chord correct1 = new Chord(Note.C4, ChordType.MAJ, 1, false);
                    Chord correct2 = new Chord(Note.C5, ChordType.MAJ, 1, false);
                    Chord[] chords = new Chord[]{correct1, correct2};
                    MusicMaker.playArpeggio(this.player, chords, 105, NOTE_HARP);
                    new NukkitRunnable() {
                        @Override
                        public void run() {
                        try {
                            MATHS_InteractionHandler.puzzle.reset();
                        } catch (InvalidFrameWriteException | InterruptedException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        }
                    }.runTaskLater(Main.s_plugin, 60);
                    return true;
                } else {
                    this.updateQuestionFrames(this.challenges[challengesComplete]);
                }
                return true;
            } else {
            // Incorrect
                Arithmetic.mark(this.player, false);
                return false;
            }
        }
        return false;
    }

    public boolean parseGuess(int frameID) throws InvalidFrameWriteException, InterruptedException, CloneNotSupportedException {
        String concatTag = this.addTagModifier(this.answerStub, frameID);
        BlockEntityItemFrame guessFrame = Arithmetic.getFrame(this.player, concatTag);
        int guessValue;
        if (guessFrame != null) {
            guessValue = guessFrame.namedTag.getInt(concatTag);
            return setGuess(guessValue);
        } else {
            Log.error(TextFormat.RED + "No item frame found with Integer tag " + TextFormat.WHITE + concatTag + TextFormat.RED + ".");
            return false;
        }
    }

    public int getGuess() {
        return guess;
    }

    public boolean setGuess(int guess) throws InvalidFrameWriteException, InterruptedException, CloneNotSupportedException {
        this.guess = guess;
        return this.solve();
    }
}
