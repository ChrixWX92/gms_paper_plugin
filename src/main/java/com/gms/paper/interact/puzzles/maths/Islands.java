package com.gms.paper.interact.puzzles.maths;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import com.gms.mc.custom.sound.*;
import com.gms.mc.error.InvalidFrameWriteException;
import com.gms.mc.interact.puzzles.maths.threads.FindFrames;
import com.gms.mc.util.Log;
import com.gms.paper.error.InvalidFrameWriteException;
import io.netty.util.internal.ThreadLocalRandom;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import static cn.nukkit.level.ParticleEffect.CAMERA_SHOOT_EXPLOSION;
import static cn.nukkit.level.Sound.NOTE_DIDGERIDOO;
import static cn.nukkit.level.Sound.NOTE_FLUTE;

public class Islands {

    public Islands(){}

    private static int islValue;
    private static int islTotal;
    private static int islCount;

    public static void doIslands(Player p, String isl, boolean mark) throws InvalidFrameWriteException, InterruptedException {

        Arithmetic.puzzleName = isl;
        setIslTotal(0);
        setIslCount(0);
        int max;
        Map<Integer, BlockEntityItemFrame> answerFrames = new HashMap<>();

        // Search for the answer item frames
        for (int i = 1 ; ; i++){
            String concatAF = (isl + "A" + i);
            BlockEntityItemFrame newAnswer = Arithmetic.getFrame(p, concatAF);
            if (newAnswer == null) {
                break;
            }
            answerFrames.put(i, newAnswer);
        }

        switch (isl) {
            case "Islands1" -> {

                //Generate a random number to be summed
                max = 15; //TODO: This should be difficulty dependent
                setIslValue(ThreadLocalRandom.current().nextInt(2, max+1));

                if (mark){
                    Arithmetic.mark(p,true,"§l§5? §f+ §l§5? §r§f= " + "§b" + getIslValue(), true);
                    Arithmetic.puzzleName = isl;
                } else{
                    p.sendTitle("§l§5? §f+ §l§5? §r§f= " + "§b" + getIslValue());
                }

                //Generate two guaranteed addends //TODO: The amount of these should also depend on difficulty
                int[] addends = new int[2];
                int addend1 = ThreadLocalRandom.current().nextInt(1, getIslValue());
                addends[0] = addend1;
                int addend2 = getIslValue() - addend1;
                addends[1] = addend2;

                //Pick frames at random to set our addends to
                for (int i = 0 ; i <= (addends.length-1) ; i++) {
                    Integer which = ThreadLocalRandom.current().nextInt(1, answerFrames.size() + 1);
                    BlockEntityItemFrame chosenFrame = answerFrames.get(which);
                    //Set the addend to the frame
                    Arithmetic.writeFrame(chosenFrame, addends[i], 1);
                    chosenFrame.namedTag.putInt(isl + "A" + which, addends[i]);
                    p.getLevel().addParticleEffect(new Vector3(chosenFrame.add(0.5).x, chosenFrame.add(0, 0.5).y, chosenFrame.add(0, 0, 0.5).z), CAMERA_SHOOT_EXPLOSION);
                    //Remove the frame from the original map
                    answerFrames.remove(which);
                }

                //Set random numbers to all other answer item frames
                for (var entry : answerFrames.entrySet()){

                    BlockEntityItemFrame b = entry.getValue();

                    int wrongAnswer = ThreadLocalRandom.current().nextInt(1, max);

                    while (wrongAnswer == getIslValue()){
                        wrongAnswer = ThreadLocalRandom.current().nextInt(1, max);
                    }

                    Arithmetic.writeFrame(b, wrongAnswer, 1);
                    b.namedTag.putInt(isl+"A"+(entry.getKey()), wrongAnswer);

                    p.getLevel().addParticleEffect(new Vector3(b.add(0.5).x, b.add(0,0.5).y, b.add(0,0,0.5).z),CAMERA_SHOOT_EXPLOSION);

                }
            }
        }

        // Search for the question item frames
        FindFrames ff = new FindFrames(p,isl);
        Thread t = new Thread(ff);
        t.start();
        t.join();
        Arithmetic.writeSumAnswers(FindFrames.getMap().values(), isl, getIslValue(), false, 0);

        if (!mark) {
            MusicMaker.playNote(p, Note.FSHARP2_GFLAT2, NOTE_DIDGERIDOO);
            Chord chord = new Chord(Note.FSHARP3_GFLAT3, ChordType.SUS4, 0, true);
            MusicMaker.playArpeggio(p, chord, 200, NOTE_FLUTE);
            MusicMaker.playSFX(SFX.Type.ISLANDS_BEGIN, p, 75, 7);
        }
    }

    public static boolean solveIslands(Player p, String isl, String id) throws InvalidFrameWriteException, InterruptedException {

        String concatTag = isl+"A"+id;
        BlockEntityItemFrame aFrame = Arithmetic.getFrame(p,concatTag);
        int v;
        int islVal = getIslValue();
        int attempts;
        int prevValue = getIslTotal();

        if (aFrame != null) {
            v = aFrame.namedTag.getInt(concatTag);
        } else {
            Log.error(TextFormat.RED + "No item frame found with Integer tag " + TextFormat.WHITE + concatTag + TextFormat.RED + ".");
            return false;
        }

        if (Arithmetic.puzzleName != null) {
            if (Arithmetic.puzzleName.equals(isl)) {
                switch (isl) {
                    case ("Islands1") -> {

                        attempts = 2;

                        setIslCount(getIslCount() + 1);
                        setIslTotal(getIslTotal() + v);

                        if ((getIslCount() >= attempts && getIslTotal() != islVal) || getIslTotal() > islVal) {
                            Arithmetic.mark(p, false, "§b" + prevValue + "§f + §b" + v + " §f = §4" + getIslTotal(), true);
                            setIslTotal(0);
                            setIslCount(0);
                            return false;
                        }
                        else if (getIslTotal() == islVal) {
                            doIslands(p, isl, true);
                            return true;
                        } else {
                            p.sendTitle("§b" + v + " §f+ §l§5? §r§f= " + "§b" + islVal);
                            Arithmetic.writeSumAnswers(FindFrames.getMap().values(), isl, getIslValue(), true, v);
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static int getIslTotal() {
        return islTotal;
    }

    public static void setIslTotal(int islTotal) {
        Islands.islTotal = islTotal;
    }

    public static int getIslCount() {
        return islCount;
    }

    public static void setIslCount(int islCount) {
        Islands.islCount = islCount;
    }

    public static int getIslValue() {
        return islValue;
    }

    public static void setIslValue(int islValue) {
        Islands.islValue = islValue;
    }

}
