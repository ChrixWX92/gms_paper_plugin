package com.gms.paper.interact.puzzles.maths;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.block.BlockWaterStill;
import cn.nukkit.level.Location;
import cn.nukkit.utils.TextFormat;
import com.gms.mc.custom.particles.ParticleFX;
import com.gms.mc.custom.particles.ParticleFXSequence;
import com.gms.mc.custom.sound.MusicMaker;
import com.gms.mc.custom.sound.SFX;
import com.gms.mc.error.InvalidFrameWriteException;
import com.gms.mc.interact.puzzles.maths.threads.FindFrames;
import com.gms.mc.util.Log;
import io.netty.util.internal.ThreadLocalRandom;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Freefall {

    public Freefall(){}

    private static Location freefallCentre;
    private static String freefallFacing;
    private static String freefallSum;
    private static int freefallHeight;
    private static int freefallValue;

    public static void doFreefall(Player p, String freefall, String facing, Location centre, boolean mark) throws InterruptedException, InvalidFrameWriteException {
        Arithmetic.puzzleName = freefall;
        setFreefallCentre(centre);
        int max;
        int tpYaw;

        switch (freefall) {
            case "Freefall1" -> {

                //Generate a random number to be summed
                max = 15; //TODO: This should be difficulty dependent
                setFreefallHeight(160); //TODO: This too
                int freefallValue = ThreadLocalRandom.current().nextInt(2, max + 1);

                //Generate an addend and a solution //TODO: The amount of these should also depend on difficulty
                int addend1 = ThreadLocalRandom.current().nextInt(1, freefallValue);
                int solution = freefallValue - addend1;
                String title;
                if (mark) {
                    title = "§b" + addend1 + " §f+ §l§5? §r§f= " + "§b" + freefallValue;
                    Arithmetic.mark(p, true, title, true);
                    Arithmetic.puzzleName = freefall;
                } else {
                    title = "§b" + addend1 + " §f+ §l§5? §r§f= " + "§b" + freefallValue;
                    p.sendTitle(title,"",5, 60, 5);
                }

                setFreefallSum(title);

                //Creating an array of our solution and three distinct, random, alternative numbers
                Integer[] printNumbers = new Integer[4];
                int randInd = ThreadLocalRandom.current().nextInt(0, 3 + 1);
                printNumbers[randInd] = solution;
                while (Arrays.asList(printNumbers).contains(null)) {
                    randInd = ThreadLocalRandom.current().nextInt(0, 3 + 1);
                    if (printNumbers[randInd] == null) {
                        int randInt = ThreadLocalRandom.current().nextInt(1, 9 + 1);
                        while (randInt == solution || Arrays.asList(printNumbers).contains(randInt)) {
                            randInt = ThreadLocalRandom.current().nextInt(1, 9 + 1);
                        }
                        printNumbers[randInd] = randInt;
                    }
                }

                //Setting the starting positions for our numbers
                Location one;
                Location two;
                Location three;
                Location four;
                switch (facing) {
                    case "N" -> {
                        one = centre.add(-6, 0, -6);
                        two = one.add(7);
                        three = one.add(0, 0, 7);
                        four = one.add(7, 0, 7);
                        tpYaw = -180;
                    }
                    case "E" -> {
                        one = centre.add(6, 0, -6);
                        two = one.add(0, 0, 7);
                        three = one.add(-7);
                        four = one.add(-7, 0, 7);
                        tpYaw = -90;
                    }
                    case "S" -> {
                        one = centre.add(6, 0, 6);
                        two = one.add(-7);
                        three = one.add(0, 0, -7);
                        four = one.add(-7, 0, -7);
                        tpYaw = 0;
                    }
                    case "W" -> {
                        one = centre.add(-6, 0, 6);
                        two = one.add(0, 0, -7);
                        three = one.add(7);
                        four = one.add(7, 0, -7);
                        tpYaw = 90;
                    }
                    default -> {
                        Log.error(TextFormat.RED + "Invalid direction " + TextFormat.WHITE + "\"" + facing + "\"" + TextFormat.RED + " specified on puzzle sign. Please use " + TextFormat.WHITE + "\"N\"" + TextFormat.RED + ", "+ TextFormat.WHITE + "\"E\"" + TextFormat.RED + ", "+ TextFormat.WHITE + "\"S\"" + TextFormat.RED + " or "+ TextFormat.WHITE + "\"W\"" + TextFormat.RED + ".");
                        return;
                    }
                }
                setFreefallFacing(facing);
                Set<Location> numberPositions = Stream.of(one, two, three, four)
                        .collect(Collectors.toCollection(HashSet::new));
                //Printing our numbers
                int count = 0;
                for (Location numberPosition : numberPositions) {
                    numberPosition.level = p.getLevel();
                    try {
                        printFreefallNumber(numberPosition, facing, printNumbers[count], printNumbers[count] == solution);
                    } catch(Exception e){e.printStackTrace();}
                    count++;
                }
                p.sendMessage(title);
                //Teleporting the player
                Location fixYaw = new Location(centre.x, centre.y+getFreefallHeight(), centre.z, tpYaw);
                p.teleport(fixYaw);
                MusicMaker.playSFX(SFX.Type.FREEFALL_TELEPORT, p);
                ParticleFXSequence tpFX = new ParticleFXSequence(ParticleFX.FREEFALL_TELEPORT, p.getLevel(), p.getLocation());
                synchronized (tpFX) {
                    tpFX.run();
                }

                //Searching for the question item frames to print
                FindFrames ff = new FindFrames(p,freefall);
                Thread t = new Thread(ff);
                t.start();
                t.join();
                Arithmetic.writeSumAnswers(FindFrames.getMap().values(), freefall, freefallValue, true, addend1);
            }
        }
    }

    private static HashSet<Location> numberPosition;

    public static void printFreefallNumber(Location start, String facing, int number, boolean solution) {
        Block block;
        Location end;
        List<Integer> numBlocks;
        HashSet<Location> cachedPositions = new HashSet<>();

        switch (number) {
            case 1 -> numBlocks = List.of(3, 4, 10, 16, 22, 28, 33, 34, 35);
            case 2 -> numBlocks = List.of(2, 3, 4, 5, 11, 17, 20, 21, 22, 23, 26, 32, 33, 34, 35);
            case 3 -> numBlocks = List.of(2, 3, 4, 5, 11, 17, 21, 22, 23, 29, 32, 33, 34, 35);
            case 4 -> numBlocks = List.of(2, 8, 14, 16, 20, 21, 22, 23, 28, 34);
            case 5 -> numBlocks = List.of(2, 3, 4, 5, 8, 14, 20, 21, 22, 23, 29, 32, 33, 34, 35);
            case 6 -> numBlocks = List.of(2, 8, 14, 20, 21, 22, 23, 26, 29, 32, 33, 34, 35);
            case 7 -> numBlocks = List.of(2, 3, 4, 5, 11, 17, 23, 29, 35);
            case 8 -> numBlocks = List.of(2, 3, 4, 5, 8, 11, 14, 17, 20, 21, 22, 23, 26, 29, 32, 33, 34, 35);
            case 9 -> numBlocks = List.of(2, 3, 4, 5, 8, 11, 14, 15, 16, 17, 23, 29, 35);
            case 10 -> numBlocks = List.of(1, 3, 4, 5, 6, 7, 9, 12, 13, 15, 18, 19, 21, 24, 25, 27, 30, 31, 33, 34, 35, 36);
            case 11 -> numBlocks = List.of(2, 5, 8, 11, 14, 17, 20, 23, 26, 29, 32, 35);
            case 12 -> numBlocks = List.of(1, 3, 4, 5, 6, 7, 12, 13, 18, 19, 21, 22, 23, 24, 25, 27, 31, 33, 34, 35, 36);
            case 13 -> numBlocks = List.of(1, 3, 4, 5, 6, 7, 12, 13, 18, 19, 21, 22, 23, 24, 25, 30, 31, 33, 34, 35, 36);
            case 14 -> numBlocks = List.of(1, 3, 7, 9, 13, 15, 17, 19, 21, 22, 23, 24, 25, 29, 31, 35);
            case 15 -> numBlocks = List.of(1, 3, 4, 5, 6, 7, 9, 13, 15, 19, 21, 22, 23, 24, 25, 30, 31, 33, 34, 35, 36);
            default -> throw new IllegalStateException("Unexpected value: " + number);
        }
        for (int i = 0 ; i <= 35 ; i++) {
            int axis1 = i % 6;
            int axis2 = Math.floorDiv(i, 6);

            switch (facing) {
                case "N" -> end = start.add(axis1, 0, axis2);
                case "E" -> end = start.add(-axis2, 0, axis1);
                case "S" -> end = start.add(-axis1, 0, -axis2);
                case "W" -> end = start.add(axis2, 0, -axis1);
                default -> {return;}
            }
            if (numBlocks.contains(i+1)) {
                block = new BlockWaterStill();
            } else {
                block = new BlockAir();
            }
            if (solution) {cachedPositions.add(end);}
            start.level.setBlock(end, block,false,false);
        }
        if (solution) {setNumberPosition(cachedPositions);}
    }



    public static Location getFreefallCentre() {
        return freefallCentre;
    }

    public static void setFreefallCentre(Location freefallCentre) {
        Freefall.freefallCentre = freefallCentre;
    }

    public static String getFreefallFacing() {
        return freefallFacing;
    }

    public static void setFreefallFacing(String freefallFacing) {
        Freefall.freefallFacing = freefallFacing;
    }

    public static String getFreefallSum() {
        return freefallSum;
    }

    public static void setFreefallSum(String freefallSum) {
        Freefall.freefallSum = freefallSum;
    }

    public static int getFreefallHeight() {
        return freefallHeight;
    }

    public static void setFreefallHeight(int freefallHeight) {
        Freefall.freefallHeight = freefallHeight;
    }

    public static HashSet<Location> getNumberPosition() {
        return numberPosition;
    }

    public static void setNumberPosition(HashSet<Location> numberPosition) {
        Freefall.numberPosition = numberPosition;
    }

}
