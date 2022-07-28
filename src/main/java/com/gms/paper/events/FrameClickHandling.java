package com.gms.paper.events;

import org.bukkit.entity.Player;
import cn.nukkit.block.*;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemMap;
import cn.nukkit.nbt.tag.Tag;
import cn.nukkit.scheduler.NukkitRunnable;
import com.gms.paper.util.TextFormat;
import com.gms.paper.Main;
import com.gms.paper.error.InvalidFrameWriteException;
import com.gms.paper.interact.puzzles.BackendUtils;
import com.gms.paper.interact.puzzles.Pairs;
import com.gms.paper.interact.puzzles.PuzzleType;
import com.gms.paper.interact.puzzles.maths.Arithmetic;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.HashMap;

import static com.gms.paper.interact.puzzles.PuzzleType.*;

public class FrameClickHandling implements Listener {

    private static boolean flipped = false;
    private boolean hold = false;
    private static long cachedID = 9223372036854775807L;
    private static boolean initialReset = false;

    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGH)
    public void ItemFrameListener(PlayerInteractEvent event) throws Exception {

        if (hold){
            event.setCancelled();
            return;
        }

        Block block = event.getBlock();
        Player player = event.getPlayer();

        if(event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && block instanceof BlockItemFrame frame && player.getGamemode() == 2) {

            event.setCancelled(true);
            BlockEntityItemFrame beif = (BlockEntityItemFrame) frame.getWorld().getBlockEntity(frame);
            if (beif.namedTag.contains("freeze") && beif.namedTag.getBoolean("freeze")) {
                return;
            }

            if(BackendUtils.getPuzzleType() == CHECKS) {

                String tagName;
                boolean value;

                Collection<Tag> beifTags = beif.namedTag.getAllTags();
                for (Tag tag : beifTags) {
                    if (tag.getName().startsWith(BackendUtils.getQuestionSetID())) {
                        tagName = tag.getName();
                        value = beif.namedTag.getBoolean(tagName);
                        int val = (value) ? 0 : 1;
                        beif.namedTag.putBoolean(tagName, !value);
                        Arithmetic.writeFrame(beif, val, 4);
                        break;
                    }
                }

            } else if (BackendUtils.getPuzzleType() == PAIRS) {

                String questionSet = BackendUtils.getQuestionSetID();
                if (this.isFlipped() & flip(beif, questionSet)) verifyPairsFrames(player, questionSet, beif); //Returns true only if an item frame has been flipped and it is not the current item frame being flipped

            } else if (BackendUtils.getPuzzleType() == ASSEMBLE) {
                boolean occupied = beif.namedTag.getBoolean("Assemble");
                Item cachedMap = beif.getItem();
                int index = player.getInventory().getHeldItemIndex();
  
                if (player.getInventory().getItemInHand() instanceof ItemMap handMap) {
                    beif.setItem(handMap);
                    beif.namedTag.putBoolean("Assemble", true);
                    if (occupied) {
                        player.getInventory().setItemInHand(cachedMap);
                    } else {
                        player.getInventory().clear(index);
                    }
                } else {
                    if (occupied) {
                        if (player.getInventory().getItemInHand().getId() == 0) {
                            player.getInventory().setItemInHand(cachedMap);
                            beif.namedTag.putBoolean("Assemble", false);
                            Arithmetic.writeFrame(beif, 2147483642, 1, true, "Assemble1");
                        }
                    }
                }
            }
        }
    }

    public static void resetPairsFrames(Player player, String questionSet) throws InvalidFrameWriteException {
        for (BlockEntityItemFrame beif : Arithmetic.getFrames(player, questionSet)) {
            if (!beif.namedTag.getBoolean("freeze")) {
                Arithmetic.writeFrame(beif, 0, 6, true, questionSet);
                beif.namedTag.putBoolean("Flipped", false);
            }
        }
        setFlipped(false);
    }

    private void verifyPairsFrames(Player player, String questionSet, BlockEntityItemFrame beif) throws InvalidFrameWriteException {
        boolean reset = true;
        hold = true;

        for (BlockEntityItemFrame beif2 : Arithmetic.getFrames(player, questionSet)){
            HashMap<Integer, String> pairsData = new HashMap<>();
            pairsData.put(0, beif.namedTag.getString(questionSet));
            pairsData.put(1, beif2.namedTag.getString(questionSet));

            if (!(beif2.namedTag.getString(questionSet).equals(beif.namedTag.getString(questionSet))) && beif2.namedTag.getBoolean("Flipped")) {
                if (!beif.namedTag.exist("freeze") && !beif2.namedTag.exist("freeze")) {
                    if (beif.namedTag.getInt("Pair") == beif2.namedTag.getInt("Pair")) {
                        beif.namedTag.putBoolean("freeze", true);
                        beif2.namedTag.putBoolean("freeze", true);
                        Pairs.setSolved(Pairs.getSolved() + 1);
                        player.sendTitle(TextFormat.AQUA + "Pair found!");
                        if (Pairs.getSolved() != Pairs.getToSolve()) {
                            new NukkitRunnable() {
                                @Override
                                public void run() {
                                    try {
                                        resetPairsFrames(player, questionSet);
                                    } catch (InvalidFrameWriteException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.runTaskLater(Main.s_plugin, 1);
                        }
                        reset = false;
                        hold = false;


                        BackendUtils.markAnswers(player, PuzzleType.PAIRS, true, pairsData);
                    } else {
                        BackendUtils.markAnswers(player, PuzzleType.PAIRS, false, pairsData);
                        player.sendTitle(TextFormat.MINECOIN_GOLD + "No match.");
                    }
                }
            }
        }
        if (reset) {
            new NukkitRunnable() {
                @Override
                public void run () {
                    try {
                        resetPairsFrames(player, questionSet);
                        hold = false;
                    } catch (InvalidFrameWriteException e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskLater(Main.s_plugin, 70);
        }
        cachedID = 9223372036854775807L;
        FrameClickHandling.setFlipped(false);

        if (Pairs.getSolved() == Pairs.getToSolve()) {
            Pairs.resetPairsData();
            hold = false;
            Pairs.solvePairs(player, questionSet);
        }
    }

    /**
     * "Flips" a BlockEntityItemFrame to its opposite side;
     * @param beif
     * @param questionSet
     * @return A boolean of the current frame's flipped state after running the method (true = face up).
     * @throws Exception In the case of passing a frame that does not contain the "Flipped" boolean tag.
     */
    public boolean flip(BlockEntityItemFrame beif, String questionSet) throws Exception {

        if (!beif.namedTag.exist("Flipped")) {throw new Exception("Invalid BlockEntityItemFrame - cannot flip.");}
        boolean flipped = beif.namedTag.getBoolean("Flipped");

        Arithmetic.writeFrame(beif, flipped ? 0 : 1, 6, true, questionSet);
        beif.namedTag.putBoolean("Flipped", !flipped);

        if (cachedID != 9223372036854775807L) {
            if ((!flipped && !isFlipped()) && beif.getId() != cachedID) setFlipped(true);
            else if (flipped && isFlipped()) setFlipped(false);
        }
        else setFlipped(true);

        if (cachedID != beif.getId()) cachedID = beif.getId();
        else {cachedID = 9223372036854775807L;}

        return !flipped;

    }

    public boolean isFlipped() {
        return flipped;
    }

    public static void setFlipped(boolean flipped) {
        FrameClickHandling.flipped = flipped;
    }

    public static long getCachedID() {
        return cachedID;
    }

    public static void setCachedID(long id) {
        FrameClickHandling.cachedID = id;
    }

    public static boolean isInitialReset() {
        return initialReset;
    }

    public static void setInitialReset(boolean initialReset) {
        FrameClickHandling.initialReset = initialReset;
    }


}
