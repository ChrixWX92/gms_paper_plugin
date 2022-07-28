package com.gms.paper.interact.puzzles.maths;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.math.Vector3;
import com.gms.mc.custom.blocks.BlockGSAnvil;
import com.gms.mc.error.InvalidFrameWriteException;
import com.gms.mc.interact.puzzles.MathsPuzzle;
import com.gms.mc.interact.puzzles.MathsTopic;
import com.gms.mc.interact.puzzles.PuzzleType;
import com.gms.mc.interact.puzzles.Resettable;
import com.gms.mc.interact.puzzles.maths.threads.ClearAnvils;
import com.gms.mc.interact.puzzles.maths.threads.ResetAnvils;
import com.gms.mc.util.Log;

import java.util.Arrays;

import static cn.nukkit.level.Sound.CONDUIT_DEACTIVATE;

public class Anvils extends MathsPuzzle implements Resettable {

    /**
     <pre>
     <h1>ANVILS</h1>
     </pre> <br>
     <h2>Tag Formats:</h2>
     <b> -N/A- <br>
     <br>
     <h2>Mechanics:</h2>
     <b>• Reset</b> <br>
     All columns' (whose vectors are stored in <var>this.columns</var> and <var>this.answer</var>) blocks are replaced
     with air. All columns other than the answer columns are populated randomly with falling blocks. N.B. In subtraction
     questions, the latter column will never be a lesser amount than the other columns.
     <b>• Input</b> <br>
     Other mechanics are supported, but currently, a falling block is added to the apex of the column specified in
     <var>this.answer</var>.<br>
     <b>• Solution</b> <br>
     Submitting the puzzle checks for answer frames tags, parsing their names by retrieving the puzzle's
     items' names from their IDs in this.objects. After finding their respective int values, they're
     checked against the amount of the amount in the GRID (<var>this.objects</var>' values);
     </pre>
     <br> <br> <br>
     */

    Block block;
    Vector3[] columns;
    Vector3 answer;

    public Anvils(Player player, String id, MathsTopic topic) throws InvalidFrameWriteException, InterruptedException {
        super(player, PuzzleType.ANVILS, id, topic);
        if (columns != null){Arrays.fill(columns, null);}
        //TODO: BACKEND INTEGRATION - Get content based on ID
        switch (this.id) {
            case "1" -> {
                this.block = new BlockGSAnvil();
                Vector3 columnOne = new Vector3(100, 39, -224); // Y values of these is apex of block stack
                Vector3 columnTwo = new Vector3(96, 39, -224);
                this.answer = new Vector3(92, 39, -224);
                this.columns = new Vector3[]{columnOne, columnTwo};
            }
            case "2" -> {
                this.block = new BlockGSAnvil();
                Vector3 columnOne = new Vector3(96, 39, -435);
                Vector3 columnTwo = new Vector3(92, 39, -435);
                this.answer = new Vector3(88, 39, -435);
                this.columns = new Vector3[]{columnOne, columnTwo};
            }
        }
        this.reset();
    }

    @Override
    public void reset() throws InvalidFrameWriteException, InterruptedException {
        BlockGSAnvil.setDelete(true);

        for (var column : columns) {
            ClearAnvils clearAnvils = new ClearAnvils(this.level, column, 31); //TODO: Let's not have this y value hard-coded
            clearAnvils.start();
            clearAnvils.join();
        }

        ClearAnvils clearAnvils = new ClearAnvils(this.level, answer, 31); //TODO: Let's not have this y value hard-coded
        clearAnvils.start();
        clearAnvils.join();

        BlockGSAnvil.setDelete(false);

        ResetAnvils resetAnvils = new ResetAnvils(this);
        resetAnvils.start();
    }

    @Override
    public boolean solve() throws InvalidFrameWriteException, InterruptedException {

            ResetAnvils resetAnvils = new ResetAnvils(this);
            resetAnvils.join();

            if (columns != null) {
                int[] counts = new int[columns.length];
                int index = 0;
                for (var column : columns) { //31 represents the y coordinate of the floor
                    counts[index] = count(column);
                    index++;
                }

                boolean correct;
                    switch (this.topic) {
                        case ADDITION -> correct = (Arrays.stream(counts).sum() == count(this.answer));
                        case SUBTRACTION -> correct = (Arrays.stream(counts).max().orElse(0) - Arrays.stream(counts).min().orElse(0) == count(this.answer));
                        default -> {
                            Log.error("We haven't written this yet.");
                            return false;
                        }
                    }

                    Arithmetic.mark(this.player, correct);
                    if (correct) this.reset();
                    return correct;
            }
        return false;
    }

    public void setBlocks(MathsTopic operator){
        switch (operator) {
            case ADDITION -> {
                if (this.level.getBlock(answer.add(0,1)) instanceof BlockAir) this.level.setBlock(answer.add(0,1), this.block);
            }
            case SUBTRACTION, MULTIPLICATION, DIVISION -> {} // No mechanics implemented yet
        }
    }

    public void clear() throws InterruptedException {
        BlockGSAnvil.setDelete(true);
        ClearAnvils clearAnvils = new ClearAnvils(this.level, answer, 31);
        clearAnvils.start();
        clearAnvils.join();
        BlockGSAnvil.setDelete(false);
        this.level.addSound(answer, CONDUIT_DEACTIVATE, 1F, 3F);
    }

    private int count(Vector3 column){
        int n = 0;
        for (int i = (int) Math.ceil(column.y); i > 31; i--) { //TODO: Let's not have this y value hard-coded
            if (this.level.getBlock((int) column.x, i, (int) column.z).getClass().isAssignableFrom(this.block.getClass())) {
                n++;
            }
        }
        return n;
    }

    public Vector3[] getColumns() {
        return columns;
    }

    public Vector3 getAnswer() {
        return answer;
    }

    public Block getBlock() {
        return block;
    }

}
