package com.gms.paper.interact.puzzles.maths;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.item.*;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import com.gms.mc.custom.sound.MusicMaker;
import com.gms.mc.custom.sound.SFX;
import com.gms.mc.error.InvalidFrameWriteException;
import com.gms.mc.interact.puzzles.*;
import com.gms.mc.util.Log;
import io.netty.util.internal.ThreadLocalRandom;

import java.util.*;

import static cn.nukkit.item.ItemID.*;
import static cn.nukkit.item.ItemID.COAL;
import static cn.nukkit.item.ItemID.DIAMOND;
import static cn.nukkit.item.ItemID.EMERALD;
import static cn.nukkit.level.ParticleEffect.*;
import static cn.nukkit.level.ParticleEffect.CAMERA_SHOOT_EXPLOSION;
import static cn.nukkit.level.ParticleEffect.SOUL;
import static cn.nukkit.level.ParticleEffect.VILLAGER_HAPPY;
import static cn.nukkit.level.ParticleEffect.WITHER_BOSS_INVULNERABLE;
import static com.gms.mc.interact.puzzles.MathsTopic.*;
import static com.gms.mc.interact.puzzles.MathsTopic.SUBTRACTION;

public class Grid extends MathsPuzzle implements Resettable {
    /**
     <pre>
     <h1>GRID</h1>
     The player must count items in the grid individually (input is provided by the COUNT mechanic). They must then sum
     these, dependent on topic. Any amount of question frames can be provided - they will be populated, provided they
     have the appropriate tag.<br>

     </pre> <br>
     <h2>Tag Formats:</h2>
     <b>• Answer Frame</b> <br> <code>INT</code> - answerStub + [name of item to be counted/"Total"]<br> Value: Current numeric value of the frame. <br>
     <b>• Question Frames</b> <br> <code>BOOLEAN</code> - questionStub<br> Value: Unused
     <br><br>
      <h2>Mechanics:</h2>
     <b>• Reset</b> <br>
           Question frames are found via their tags and populated randomly with items listed as keys in
            <var>this.objects</var>.
     <b>• Input</b> <br>
               The COUNT mechanic finds the answer signs and increases their value on button push. <br>
     <b>• Solution</b> <br>
               Submitting the puzzle checks for answer frames tags, parsing their names by retrieving the puzzle's
               items' names from their IDs in <var>this.objects</var>. After finding their respective int values, they're
               checked against the amount of the items in the GRID (<var>this.objects</var>' values).
     </pre>
     <br> <br> <br>
     */
    /* Key = Item ID, Value = Number present in the GRID */
    private final HashMap<Integer, Integer> objects;
    private final HashMap<Integer, Integer> values;
    private final String totalTag = addTagModifier(this.answerStub, "Total");

    public Grid(Player p, String id, MathsTopic topic) throws InvalidFrameWriteException {
        super(p, PuzzleType.GRID, id, topic);
        //TODO: BACKEND INTEGRATION - Get content based on ID
        this.objects = new HashMap<>();
        this.values = new HashMap<>();
        switch(this.id) {
            case "1" -> {
                this.objects.put(DIAMOND, 0);
                this.values.put(DIAMOND, 1);
                this.objects.put(EMERALD, 0);
                this.values.put(EMERALD, 1);
            }
            case "2" -> {
                this.objects.put(DIAMOND, 0);
                this.values.put(DIAMOND, 1);
                this.objects.put(COAL, 0);
                this.values.put(COAL, -1);
            }
        }
        this.reset();
    }

    @Override
    public void reset() throws InvalidFrameWriteException {

        // Resetting answer frames
        for (int item : this.objects.keySet()) {
            String tag = addTagModifier(this.answerStub, Item.get(item).getName());
            try {
                Objects.requireNonNull(Arithmetic.getFrame(this.player, tag)).namedTag.putInt(tag, 0); //Setting answer frame tags to 0
                Arithmetic.writeFrame(Objects.requireNonNull(Arithmetic.getFrame(this.player, tag)), 0, 1); //Resetting answer frames
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.error("Unable to find answer Item Frame. Ensure answer Item Frames' metadata has been correctly configured.");
            }
        }

        // Resetting total frame
        try {
            Objects.requireNonNull(Arithmetic.getFrame(this.player, this.totalTag)).namedTag.putInt(totalTag, 0); //Setting Total answer frame tag to 0
            Arithmetic.writeFrame(Objects.requireNonNull(Arithmetic.getFrame(this.player, this.totalTag)), 0, 1); //Resetting Total answer frame
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.error("Unable to find answer Item Frame. Ensure answer Item Frames' metadata has been correctly configured.");
        }

        //Resetting question frames
        Set<BlockEntityItemFrame> frames = Arithmetic.getFrames(this.player, this.name);
        HashMap<BlockEntityItemFrame, Item> cachedPairings = new HashMap<>();
        boolean subtractionCheck = true;
        while (subtractionCheck) {
            cachedPairings = new HashMap<>();
            this.objects.replaceAll((k,v) -> v = 0);
            for (BlockEntityItemFrame frame : frames) {
                frame.setItemDropChance(0);
                frame.dropItem(this.player);
                Item item = getItem(frame);
                if (item != null && item.getId() != 0) {
                    this.objects.put(item.getId(), this.objects.get(item.getId()) + 1);
                }
                cachedPairings.put(frame, item);
            }
            if (this.topic != SUBTRACTION) subtractionCheck = false;
            else if (checkTotal() >= 0) subtractionCheck = false;
        }
        for (Map.Entry<BlockEntityItemFrame, Item> pairing : cachedPairings.entrySet()) {
            if (pairing.getValue() != null) pairing.getKey().setItem(pairing.getValue());
        }

        MusicMaker.playSFX(SFX.Type.GRID_GEMS, this.player);

    }

    @Override
    public boolean solve() throws InvalidFrameWriteException, InterruptedException {

        String tagName;

        // Checking answer frame values against amount of items
        for (Map.Entry<Integer, Integer> count : this.objects.entrySet()) {

            tagName = this.addTagModifier(this.answerStub, Item.get(count.getKey()).getName());
            int number = Objects.requireNonNull(Arithmetic.getFrame(this.player, tagName)).namedTag.getInt(tagName);
            if (count.getValue() != number) {
                Arithmetic.mark(this.player, false);
                return false;
            }

        }

        // Checking total is correct
        if (checkTotal() != Objects.requireNonNull(Arithmetic.getFrame(this.player, totalTag)).namedTag.getInt(totalTag)) {
            Arithmetic.mark(this.player, false);
            return false;
        }

        Arithmetic.mark(this.player, true);
        reset();
        return true;

    }

    private Item getItem(BlockEntityItemFrame frame){

        Item item;
        CompoundTag empty = new CompoundTag();

        if (ThreadLocalRandom.current().nextInt(0, this.objects.size() + 1) == 0) {
            frame.namedTag.putCompound("Item", empty);
            return null;
        } else {
            int itemID = (int) this.objects.keySet().toArray()[new Random().nextInt(this.objects.keySet().size())];
            item = Item.get(itemID);
            this.playFX(itemID, frame);
        }

        return item;

    }

    private int checkTotal(){
        int total = 0;
        for (Map.Entry<Integer, Integer> entry : this.objects.entrySet()) {
            for (int i = 0 ; i < entry.getValue() ; i++) {
                total = total + this.values.get(entry.getKey());
            }
        }
        return total;
    }

    private void playFX(int itemID, Vector3 pos){
        Vector3 offset = new Vector3(pos.add(0.5).x, pos.add(0,0.5).y, pos.add(0,0,0.5).z);
        switch (itemID) {
            case DIAMOND -> this.level.addParticleEffect(offset,SOUL);
            case EMERALD -> this.level.addParticleEffect(offset,VILLAGER_HAPPY);
            case COAL -> this.level.addParticleEffect(offset,WITHER_BOSS_INVULNERABLE);
        }
        this.level.addParticleEffect(offset, CAMERA_SHOOT_EXPLOSION);
    }

}
