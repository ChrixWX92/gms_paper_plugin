package com.gms.paper.interact.puzzles.maths;

import cn.nukkit.Player;
import cn.nukkit.block.BlockButtonStone;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.BoneMealParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import com.gms.mc.custom.sound.*;
import com.gms.mc.data.GamePosition;
import com.gms.mc.error.InvalidFrameWriteException;
import com.gms.paper.error.InvalidFrameWriteException;
import io.netty.util.internal.ThreadLocalRandom;
import nukkitcoders.mobplugin.entities.animal.flying.Bat;
import nukkitcoders.mobplugin.entities.animal.flying.Bee;
import nukkitcoders.mobplugin.entities.animal.flying.Parrot;
import nukkitcoders.mobplugin.entities.animal.jumping.Rabbit;
import nukkitcoders.mobplugin.entities.animal.swimming.*;
import nukkitcoders.mobplugin.entities.animal.walking.*;
import nukkitcoders.mobplugin.entities.monster.walking.*;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import static cn.nukkit.level.ParticleEffect.HUGE_EXPLOSION_LAB_MISC;
import static cn.nukkit.level.Sound.*;

public class Pen {

    private static String mobType;
    private static int challengeAmount;
    private static String[] cc = new String[]{"§l§6CHALLENGE", "§r§l§aCOMPLETE!"};
    private static int min; //TODO: Should be difficulty-dependent
    private static int max; //TODO: Should be difficulty-dependent
    private static int challengesComplete = 0;

    public Pen(){}

    public static void doPen(Player p, String pen, String operator, String amount) throws InvalidFrameWriteException { //TODO: Overload for fourth parameter, or change method call

        Arithmetic.puzzleName = pen;
        Level l = p.getLevel();
        Entity[] mobList;

        switch (pen) {
            case "Pen1" -> {

                Chicken chicken = (Chicken)setMob(p,pen); //What mob(s) goes in the pen TODO: Overly simplified now - entity type(s) must be informed by the backend - editing MobPlugin may be a smoother way to pull this off
                chicken.setBaby(false); //Do we want babies of this one?
                mobList = new Entity[]{chicken};
                setMin(2);
                setMax(15);
                setChallengeAmount(6);

                switch (operator) { //TODO: Can we make this switch/case modular and define variables in outer switch (pen) instead?
                    case ("+") -> {
                        Entity mob = chicken; //TODO: Random mob choice would involve assigning different entity types to mob
                        mob.spawnToAll();
                        l.addParticleEffect(new Vector3(mob.x, mob.y, mob.z),HUGE_EXPLOSION_LAB_MISC);
                        if (amount != null) {
                            for (int amountInt = Integer.parseInt(amount); amountInt > 1; amountInt--) {
                                mob = setMob(p,pen);
                                mob.spawnToAll();
                                l.addParticleEffect(new Vector3(mob.x, mob.y, mob.z),HUGE_EXPLOSION_LAB_MISC);
                            }
                        }
                        l.addSound(p.getPosition(), MOB_CHICKEN_PLOP, 1F, 0.7F);
                        l.addSound(p.getPosition(), ITEM_TRIDENT_RIPTIDE_1, 0.8F, 1.4F);
                    }
                    //case ("-") ->
                    //case ("*") ->
                    //case ("/") ->

                    case ("clear") -> {
                        if (Arithmetic.puzzleName.equals(pen)) {
                            for (Entity mob : mobList) {
                                Arithmetic.deleteMobs(l, pen, mob.getClass(), true);
                                l.addSound(p.getPosition(), MOB_CHICKEN_HURT, 1F, 0.9F);
                                l.addSound(p.getPosition(), ITEM_TRIDENT_RIPTIDE_3, 0.8F, 2.2F);
                            }
                        }
                    }
                    case ("reset") -> {
                        if (Arithmetic.puzzleName.equals(pen)) {
                            resetPen(p, pen);
                            for (Entity mob : mobList) {
                                Arithmetic.deleteMobs(l, pen, mob.getClass(), true);
                                l.addSound(p.getPosition(), MOB_CHICKEN_HURT, 1F, 0.9F);
                            }
                        }
                    }
                }
            }
        }
    }

    public static boolean solvePen(Player p, String pen, String challenge, BlockButtonStone button) throws InvalidFrameWriteException {

        Level l = p.getLevel();
        GamePosition blockLoc = new GamePosition(null, button.getLocation(), true);
        GamePosition completeFrameLoc = new GamePosition(blockLoc, new Location(0, 2, 0), false);
        BlockEntityItemFrame completeFrame = (BlockEntityItemFrame) l.getBlockEntity(completeFrameLoc.round());
        GamePosition numberSignLoc = new GamePosition(blockLoc, new Location(0, 1, 0), false);
        BlockEntitySign numberSign = (BlockEntitySign) l.getBlockEntity(numberSignLoc.round());
        int challengeNum = Integer.parseInt(challenge);
        String concatAF = pen + "A" + challengeNum;
        int valInt = numberSign.namedTag.getInt(concatAF);
        if (completeFrame.namedTag.getBoolean(concatAF)) {return false;}
        int challengeCount;


        if (Arithmetic.puzzleName != null) {
            if (Arithmetic.puzzleName.equals(pen)) {
                switch (pen) {
                    case ("Pen1") -> {
                        //Counting our chickens
                        Map<Long, ? extends FullChunk> chunksMap = l.getChunks();
                        Map<Long, Entity> entMap;
                        int count = 0;
                        for (var entry : chunksMap.entrySet()) {
                            entMap = entry.getValue().getEntities();
                            for (var chunkEntry : entMap.entrySet()) {
                                if (chunkEntry.getValue().getClass() == Chicken.class) {
                                    if (chunkEntry.getValue().namedTag.contains(pen)) {
                                        count++;
                                    }
                                }
                            }
                        }

                        if (count == valInt) {
                            if (checkChallengeFrames(p, pen, getChallengeAmount())){
                                Arithmetic.mark(p, true, "", false);
                                Chord correct1 = new Chord(Note.C4, ChordType.MAJ, 1, false);
                                Chord correct2 = new Chord(Note.C5, ChordType.MAJ, 1, false);
                                Chord[] chords = new Chord[]{correct1, correct2};
                                MusicMaker.playArpeggio(p, chords, 105, NOTE_HARP);
                                resetPen(p, pen);
                            } else {
                                penMark(p, getChallengesComplete()+1, numberSign, completeFrame, concatAF);
                            }
                            Arithmetic.deleteMobs(l,pen,Chicken.class,true);
                            return true;
                        } else {
                            Arithmetic.mark(p, false);
                            return false;
                        }

                    }
                }
            }
        }
        return false;
    }

    public static Entity setMobByType(Player p, String mobType, CompoundTag nbt) {return setMob(p, mobType, nbt, true);}

    public static Entity setMob(Player p, String puzzle){return setMob(p, puzzle, new CompoundTag(), false);}

    public static Entity setMob(Player p, String puzzle, CompoundTag nbt, boolean byType){ // Use with caution - MobPlugin will auto-spawn any initialized Entity objects
        Entity entity;
        if (!byType) {
            switch (puzzle) {
                case "Pen1" -> entity = new Chicken(p.chunk, Arithmetic.mathsNBTMaker(p, puzzle, 1));
                case "PenDye1" -> entity = new Sheep(p.chunk, nbt);
                default -> throw new IllegalStateException("Unexpected value: " + puzzle);
            }
        } else {
            entity = getMobByType(puzzle, p, nbt);
        }
        setMobType(entity.getName());
        return entity;
    }

    public static Entity getMobByType(String mobType, Player p, CompoundTag nbt) {
        switch (mobType) {
            case "Bat" ->  { return new  Bat(p.chunk, nbt);}
            case "Bee" ->  { return new  Bee(p.chunk, nbt);}
            case "Cat" ->  { return new  Cat(p.chunk, nbt);}
            case "Chicken" ->  { return new  Chicken(p.chunk, nbt);}
            case "Cod" ->  { return new  Cod(p.chunk, nbt);}
            case "Cow" ->  { return new  Cow(p.chunk, nbt);}
            case "Creeper" ->  { return new  Creeper(p.chunk, nbt);}
            case "Dolphin" ->  { return new  Dolphin(p.chunk, nbt);}
            case "Donkey" ->  { return new  Donkey(p.chunk, nbt);}
            case "Fox" ->  { return new  Fox(p.chunk, nbt);}
            case "Horse" ->  { return new  Horse(p.chunk, nbt);}
            case "Husk" ->  { return new  Husk(p.chunk, nbt);}
            case "Llama" ->  { return new  Llama(p.chunk, nbt);}
            case "Mooshroom" ->  { return new  Mooshroom(p.chunk, nbt);}
            case "Mule" ->  { return new  Mule(p.chunk, nbt);}
            case "Ocelot" ->  { return new  Ocelot(p.chunk, nbt);}
            case "Panda" ->  { return new  Panda(p.chunk, nbt);}
            case "Parrot" ->  { return new  Parrot(p.chunk, nbt);}
            case "Pig" ->  { return new  Pig(p.chunk, nbt);}
            case "Polar Bear" ->  { return new  PolarBear(p.chunk, nbt);}
            case "Pufferfish" ->  { return new  Pufferfish(p.chunk, nbt);}
            case "Rabbit" ->  { return new  Rabbit(p.chunk, nbt);}
            case "Salmon" ->  { return new  Salmon(p.chunk, nbt);}
            case "Sheep" ->  { return new  Sheep(p.chunk, nbt);}
            case "Skeleton" ->  { return new  Skeleton(p.chunk, nbt);}
            case "Squid" ->  { return new  Squid(p.chunk, nbt);}
            case "Strider" ->  { return new  Strider(p.chunk, nbt);}
            case "Tropical Fish" ->  { return new  TropicalFish(p.chunk, nbt);}
            case "Turtle" ->  { return new  Turtle(p.chunk, nbt);}
            case "Wolf" ->  { return new  Wolf(p.chunk, nbt);}
            case "Zombie" ->  { return new  Zombie(p.chunk, nbt);}
            default -> throw new IllegalStateException("Unexpected value: " + mobType);
        }
    }


    public static void penMark(Player p, int stage, BlockEntitySign sign, BlockEntityItemFrame plaque, String concatTag) throws InvalidFrameWriteException {
        p.sendTitle("§2Correct!","",5, 75, 5);
        plaque.namedTag.putBoolean(concatTag, true);
        Arithmetic.writeFrame(plaque, 1, 3);
        sign.setText(getCc());
        sign.namedTag.putInt(concatTag, 0);
        p.getLevel().addSound(p.getPosition(), PLACE_LARGE_AMETHYST_BUD, 1F, 1.5F);
        p.getLevel().addParticle(new BoneMealParticle(new Vector3(plaque.add(0.5).x, plaque.add(0,0.5).y, plaque.add(0,0,0.5).z)));
        switch (stage-1){
            case 5: {
                Chord correct1 = new Chord(Note.C4, ChordType.MAJ, 1, false);
                Chord correct2 = new Chord(Note.C5, ChordType.MAJ, 1, false);
                Chord[] chords = new Chord[]{correct1, correct2};
                MusicMaker.playArpeggio(p, chords, 50, NOTE_HARP);
                break;
            }
            case 4: MusicMaker.playNote(p, Note.G5, NOTE_HARP,0.75F);
            case 3: MusicMaker.playNote(p, Note.E5, NOTE_HARP,0.75F);
            case 2: MusicMaker.playNote(p, Note.C4, NOTE_HARP,0.75F);
            case 1: MusicMaker.playNote(p, Note.G4, NOTE_HARP,0.75F);
            case 0: MusicMaker.playNote(p, Note.E4, NOTE_HARP,0.75F);
        }
    }

    public static boolean checkChallengeFrames(Player p, String pen, int limit) throws InvalidFrameWriteException {
        setChallengesComplete(0);
        for (int i = 1; i <= limit ; i++) {
            String concatAF = pen + "A" + i;
            BlockEntityItemFrame newAnswer = Arithmetic.getFrame(p, concatAF);
            if (newAnswer == null) {
                throw new InvalidFrameWriteException(2);
            } else {
                if (newAnswer.namedTag.getBoolean(concatAF)) {
                    setChallengesComplete(getChallengesComplete() + 1);
                }
            }
        }
        return getChallengesComplete() == (limit-1);
    }

    public static void resetPen(Player p, String pen) throws InvalidFrameWriteException {

        MusicMaker.playSFX(SFX.Type.PEN_BEGIN, p,50, getChallengeAmount());

        int[] randomValues = new int[getChallengeAmount()];
        while(!Arrays.stream(randomValues).allMatch(new HashSet<>()::add)){
            for (int i = 0 ; i < getChallengeAmount() ; i++) {
                randomValues[i] = ThreadLocalRandom.current().nextInt(getMin(), getMax()+1);
            }
        }
        for (int i = 1 ; ; i++) {
            //Finding the item frame
            String concatAF = pen + "A" + i;
            BlockEntityItemFrame newAnswer = Arithmetic.getFrame(p, concatAF);
            if (newAnswer == null) {return;}
            //Changing frame data
            newAnswer.namedTag.putBoolean(concatAF, false);
            Arithmetic.writeFrame(newAnswer, 0, 3);
            //Finding the sign
            BlockEntitySign newSign = Arithmetic.getSign(p, concatAF);
            if (newSign == null) {return;}
            //Changing sign data
            newSign.setText(getChallenge(String.valueOf(randomValues[i-1])));
            newSign.namedTag.putInt(concatAF, randomValues[i-1]);
        }
    }

    public static String[] getCc() {
        return cc;
    }

    public static void setCc(String[] cc) {
        Pen.cc = cc;
    }

    public static String getMobType() {
        return mobType;
    }

    public static void setMobType(String mobType) {
        Pen.mobType = mobType;
    }

    public static int getChallengeAmount() {
        return challengeAmount;
    }

    public static void setChallengeAmount(int challengeAmount) {
        Pen.challengeAmount = challengeAmount;
    }

    public static String[] getChallenge(String amount) {
       String[] text = {"§l§6CHALLENGE", "§r§0Put §l§b" + amount, "§r§0" + getMobType() + "s", "§r§0in the pen"};
       return text;
    }

    public static int getMax() {
        return max;
    }

    public static void setMax(int max) {
        Pen.max = max;
    }

    public static int getChallengesComplete() {
        return challengesComplete;
    }

    public static void setChallengesComplete(int challengesComplete) {
        Pen.challengesComplete = challengesComplete;
    }

    public static int getMin() {
        return min;
    }

    public static void setMin(int min) {
        Pen.min = min;
    }
}
