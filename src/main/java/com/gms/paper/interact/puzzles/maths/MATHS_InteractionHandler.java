package com.gms.paper.interact.puzzles.maths;

import com.gms.paper.custom.particles.ParticleFX;
import com.gms.paper.custom.particles.ParticleFXSequence;
import com.gms.paper.custom.sound.MusicMaker;
import com.gms.paper.custom.sound.SFX;
import com.gms.paper.data.ChildProfile;
import com.gms.paper.error.InvalidBackendQueryException;
import com.gms.paper.error.InvalidFrameWriteException;
import com.gms.paper.interact.InteractionHandler;
import com.gms.paper.interact.puzzles.*;
import com.gms.paper.util.Log;
import com.gms.paper.interact.puzzles.Resettable;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;

import static com.gms.paper.interact.puzzles.MathsTopic.ADDITION;
import static com.gms.paper.interact.puzzles.MathsTopic.SUBTRACTION;
import static com.gms.paper.interact.puzzles.PuzzleType.*;
import static com.gms.paper.interact.puzzles.PuzzleType.GRID;

public class MATHS_InteractionHandler extends InteractionHandler {

    static Resettable puzzle; //TODO: When all refactoring is done, this can be made non-static 👍

    @Override
    public void handle(PlayerInteractEvent event) throws IOException, InvalidBackendQueryException {

        // cool down to prevent button mashing (1.5 sec)
        if ((System.currentTimeMillis() - lastPressed) >= 1500) {
            lastPressed = System.currentTimeMillis();
        } else
            return;

        super.handle(event);


        MathsTopic topic = SUBTRACTION; //TODO: NEED INFLUENCING SOMEHOW

        // Is our sign referencing a valid puzzle type?
        if (Arrays.asList(PuzzleType.abbreviations()).contains(buttonType)) {
            for (PuzzleType puzzleType : PuzzleType.values()) {
                if (puzzleType.abbreviation.equals(buttonType)) {
                    // Does our sign instruct to do more than start the puzzle?
                    if (signText[2].length() == 0 && signText[3].length() == 0) {
                        // If not, we start the puzzle
                        try {
                            Class<? extends Puzzle> puzzleClass = puzzleType.clazz;
                            puzzle = (Resettable) puzzleClass.getConstructor(Player.class, String.class, MathsTopic.class)
                                    .newInstance(player, signText[1], topic);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    // If so, are we currently playing the appropriate puzzle?
                    } else if (Arithmetic.currentPuzzle != null && Arithmetic.currentPuzzle.getName().equals(puzzleType.abbreviation + "-" + signText[1])) {
                        //If so, signs function uniquely as below
                        switch (puzzleType) {
                            case ANVILS -> {
                                switch (signText[2]) {
                                    case "+" -> ((Anvils) puzzle).setBlocks(ADDITION); //TODO: Room for additional mechanics here
                                    case "clear" -> {
                                        try {
                                            ((Anvils) puzzle).clear();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    default -> Log.error("Unknown operator specified on ANVILS functional sign at " + signLoc + ".");
                                }
                            }
                            case NUMBERSEARCH -> {
                                if (Arithmetic.isStringNumeric(signText[2])) {
                                    try {
                                        ((NumberSearch) puzzle).parseGuess(Integer.parseInt(signText[2]));
                                    } catch (InvalidFrameWriteException | InterruptedException | CloneNotSupportedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            case GAUGES -> {
                                if (Arithmetic.isStringNumeric(signText[2])) {
                                    Gauges gauges = (Gauges) puzzle;
                                    Gauges.Gauge gauge = gauges.getGauges().get(Integer.parseInt(signText[2]));
                                    String[] instruction = signText[3].split(" ");
                                    if (instruction.length > 1) {
                                        if (Arithmetic.isStringNumeric(instruction[1])) {
                                            int number = Integer.parseInt(instruction[1]);
                                            switch (instruction[0]) {
                                                case "+" -> {
                                                    for (int i = 0; i < number; i++) gauge.addBlock();
                                                }
                                                case "-" -> {
                                                    for (int i = 0; i < number; i++) gauge.subtractBlock();
                                                }
                                            }
                                        } else {
                                            Log.error("Invalid second argument passed to GAUGES functional sign, line 4: " + instruction[1]);
                                        }
                                    } else {
                                        switch (instruction[0]) {
//                                            case "clear" -> {
//                                                try {
//                                                    ((Anvils) puzzle).clear();
//                                                } catch (InterruptedException e) {
//                                                    e.printStackTrace();
//                                                }
//                                            }
                                            default -> Log.error("Unknown operator specified on GAUGES functional sign at " + signLoc + ".");
                                        }
                                    }
                                }
                            }
                            default -> {
                                Log.info("MATHS_InteractionHandler: Additional functionality for " + Arithmetic.currentPuzzle.getName() + " not currently accessible.");
                                player.sendMessage("Press \"PRESS TO START\" to begin!");
                                return;
                            }
                        }
                    } else {
                        if (Arithmetic.currentPuzzle == null) Log.info("MATHS_InteractionHandler: No puzzle currently being played.");
                        else Log.info("MATHS_InteractionHandler: Additional functionality for " + Arithmetic.currentPuzzle.getName() + " not currently accessible.");
                        player.sendMessage("Press \"PRESS TO START\" to begin!");
                        return;
                    }
                }
            }

        } else {

            try {
                switch (buttonType) {
                    case "SUM" ->
                            //SUM
                            //The nbt tag name of the sum's signs
                            Arithmetic.doSum(player, signText[1], false);
                    case "PILLARS" ->
                            //PILLARS
                            //The nbt tag name of the pillars' signs
                            Pillars.doPillars(player, signText[1], false);
                    case "PEN" -> {
                        //PEN
                        //The nbt tag name of the pen puzzle's signs
                        //'+','-','*','/', "clear" or "reset" - an operator representing how to handle the mobs
                        //If the above above line is a mathematical operator, the addend, subtrahend, multiplicand or divisor
                        String noOverloading = "1";
                        if (!signText[3].isEmpty() && !signText[3].isBlank()) {
                            noOverloading = signText[3];
                        }
                        Pen.doPen(player, signText[1], signText[2], noOverloading);
                    }
                    case "ISLANDS" -> {
                        //ISLANDS
                        //The nbt tag name of the islands puzzle's signs
                        Islands.doIslands(player, signText[1], false);
                    }
                    case "FREEFALL" -> {
                        //FREEFALL
                        //The freefall puzzle's name (nothing else required below for resetting)
                        //The direction the puzzle is facing (top of numbers) (N, E, S or W)
                        //The coordinates of the very middle of the puzzle (as one would write then on a TP sign (x,y,z))
                        if (signText[3].length() == 0) {
                            switch (signText[2].toUpperCase()) {
                                case "N" -> {
                                    player.teleport(new Location(Freefall.getFreefallCentre().x, Freefall.getFreefallCentre().y + 1, Freefall.getFreefallCentre().z + 13, -180));
                                    Arithmetic.puzzleName = null;
                                    MusicMaker.playSFX(SFX.Type.FREEFALL_TELEPORT, player);
                                    ParticleFXSequence tpFX = new ParticleFXSequence(ParticleFX.FREEFALL_TELEPORT, player.getLevel(), player.getLocation());
                                    synchronized (tpFX) {
                                        tpFX.run();
                                    }
                                }
                                case "E" -> {
                                    player.teleport(new Location(Freefall.getFreefallCentre().x - 13, Freefall.getFreefallCentre().y + 1, Freefall.getFreefallCentre().z, -90));
                                    Arithmetic.puzzleName = null;
                                    MusicMaker.playSFX(SFX.Type.FREEFALL_TELEPORT, player);
                                    ParticleFXSequence tpFX = new ParticleFXSequence(ParticleFX.FREEFALL_TELEPORT, player.getLevel(), player.getLocation());
                                    synchronized (tpFX) {
                                        tpFX.run();
                                    }
                                }
                                case "S" -> {
                                    player.teleport(new Location(Freefall.getFreefallCentre().x, Freefall.getFreefallCentre().y + 1, Freefall.getFreefallCentre().z - 13, 0));
                                    Arithmetic.puzzleName = null;
                                    MusicMaker.playSFX(SFX.Type.FREEFALL_TELEPORT, player);
                                    ParticleFXSequence tpFX = new ParticleFXSequence(ParticleFX.FREEFALL_TELEPORT, player.getLevel(), player.getLocation());
                                    synchronized (tpFX) {
                                        tpFX.run();
                                    }
                                }
                                case "W" -> {
                                    player.teleport(new Location(Freefall.getFreefallCentre().x + 13, Freefall.getFreefallCentre().y + 1, Freefall.getFreefallCentre().z, 90));
                                    Arithmetic.puzzleName = null;
                                    MusicMaker.playSFX(SFX.Type.FREEFALL_TELEPORT, player);
                                    ParticleFXSequence tpFX = new ParticleFXSequence(ParticleFX.FREEFALL_TELEPORT, player.getLevel(), player.getLocation());
                                    synchronized (tpFX) {
                                        tpFX.run();
                                    }
                                }
                                default -> Log.logGeneric(player, TextFormat.RED + "Invalid direction " + TextFormat.WHITE + "\"" + signText[2] + "\"" + TextFormat.RED + " specified on puzzle sign. Please use " + TextFormat.WHITE + "\"N\"" + TextFormat.RED + ", " + TextFormat.WHITE + "\"E\"" + TextFormat.RED + ", " + TextFormat.WHITE + "\"S\"" + TextFormat.RED + " or " + TextFormat.WHITE + "\"W\"" + TextFormat.RED + ".");
                            }
                        } else {
                            Freefall.doFreefall(player, signText[1], signText[2].toUpperCase(), parseLocation(signText[3]), false);
                        }
                    }
                    case "SUBMIT" -> {
                        //SUBMIT
                        //The type of puzzle being submitted (as would be detailed on the top line (e.g GRID))
                        //The puzzle title, as used for its nbt tags (e.g. Grid1)
                        //For PILLARS + PEN - any integer value || For NSEARCH - the value associated with answer item frame's Integer nbt tag (NOTE: This number must not be greater than the amount of answer frames that are present in the puzzle)
                        submit(player, signText, buttonBlock, profile);
                    }
                    case "COUNT" -> { //TODO: This should be a dedicated method in Arithmetic
                        //COUNT
                        //Up or Down
                        //A specified step (blank steps by 1)
                        //An additional qualifier, if needed
                        Puzzle currentPuzzle = Arithmetic.currentPuzzle;
                        if (currentPuzzle.getPuzzleType() == GRID) {
                            String tag = currentPuzzle.addTagModifier(currentPuzzle.getAnswerStub(),signText[3]);
                            BlockEntityItemFrame frame = Arithmetic.getFrame(player, tag);
                            int count;
                            if (frame != null) {
                                count = frame.namedTag.getInt(tag);
                                if (count < 9) {
                                    frame.namedTag.putInt(tag, (count + 1));
                                    Arithmetic.writeFrame(frame, (count + 1), 1);
                                } else {
                                    frame.namedTag.putInt(tag, 0);
                                    Arithmetic.writeFrame(frame, 0, 1);
                                }
                            }
                        } else {
                            player.sendMessage("Press \"PRESS TO START\" to begin!");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void submit(Player player, String[] signText, Block buttonBlock, ChildProfile profile) throws Exception {
        
        if (signText[1].startsWith("#")) {
            switch (Objects.requireNonNull(PuzzleType.getPuzzleType(player, signText[1]))) {
                case MCTP, MCQ, TH, TPQS -> throw new Exception("No contingency for solving PuzzleType " + Objects.requireNonNull(PuzzleType.getPuzzleType(player, signText[1]))){};
                case CHECKS -> Checks.solveChecks(player, signText[1]);
                case MOBGROUP -> MobGroup.solveMobGroup(player, signText[1]);
                case PAIRS -> Pairs.solvePairs(player, signText[1]);
                case ASSEMBLE -> Assemble.solveAssemble(player, signText[1]);
                default ->
                        throw new Exception("Cannot get PuzzleType for Question Set ID " + signText[1]){};
            }
        } else if (Arithmetic.currentPuzzle.getName().equals(signText[1])) {
            if (puzzle.solve()) profile.earnTickets(1);
        }
        else {

            switch (signText[1]) {
                case "SUM" -> {
                    switch (signText[2]) {
                        case "Sum2" -> {
                            if (Arithmetic.solveForX) {
                                //Are we doing a sum right now?
                                Block numberFrame = player.getLevel().getBlock(buttonBlock.getLocation().add(new Location(0, 1, 0)));
                                BlockEntity nf = numberFrame.getLevel().getBlockEntity(numberFrame);
                                if (nf != null) {

                                    if (nf instanceof BlockEntityItemFrame) {

                                        CompoundTag namedTag = nf.namedTag;
                                        if (namedTag.contains("Sum2A")) {
                                            if (Arithmetic.sumX == namedTag.getInt("Sum2A")) {
                                                Arithmetic.solveForX = false;
                                                player.sendTitle(TextFormat.GREEN + "§2Correct, \nwell done!");
                                                profile.earnTickets(1);
                                                Arithmetic.doSum(player, "Sum2", false);
                                            } else {
                                                player.sendTitle(TextFormat.GOLD + "Not quite, \nbut try again!");
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                case "PILLARS" -> {
                    if (Pillars.solvePillars(player, signText[2], signText[3])) {
                        profile.earnTickets(1);
                    }
                }
                case "PEN" -> {
                    if (Pen.solvePen(player, signText[2], signText[3], (BlockButtonStone) buttonBlock)) {
                        profile.earnTickets(1);
                    }
                }
                case "TWR" -> {
                    if (puzzle instanceof Tower) {
                        if (((Tower) puzzle).parseGuess(signText[3])) { //TODO: Different interface for parseGuess()?
                            profile.earnTickets(1);
                        }
                    }
                }
                case "ISLANDS" -> {
                    if (Islands.solveIslands(player, signText[2], signText[3])) {
                        profile.earnTickets(1);
                    }
                }
                case "CHECKS" -> {
                    if (Checks.solveChecks(player, signText[2])) { //TODO: This can't live here forever
                        profile.earnTickets(1);
                    }
                }
            }
        }
    }

    public static void reinitializePuzzle(Resettable newPuzzle) {
        puzzle = null;
        puzzle = newPuzzle;
    }

    public static Location parseLocation(String textPos) {
        try {

            if (textPos.endsWith("W") || textPos.endsWith("w")) {
                textPos = textPos.replace("W", "").replace("w", "").trim();
            }

            String[] coords = textPos.split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            int z = Integer.parseInt(coords[2]);

            return new Location(x, y, z);
        }
        catch (Exception e) {
            Log.exception(e, String.format("Exception while parsing coords: %s", textPos));
        }

        return new Location(0, 0, 0);
    }

}
