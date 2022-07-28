package com.gms.paper.events;

import org.bukkit.entity.Player;
import com.gms.paper.util.TextFormat;
import com.gms.paper.custom.sound.Chord;
import com.gms.paper.custom.sound.ChordType;
import com.gms.paper.custom.sound.MusicMaker;
import com.gms.paper.custom.sound.Note;
import com.gms.paper.data.Course;
import com.gms.paper.data.User;
import com.gms.paper.interact.puzzles.maths.Arithmetic;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import org.bukkit.event.Listener;

import java.util.concurrent.atomic.AtomicInteger;

public class RHListener implements Listener, IHandler<SimpleFormButtonClickEvent> {

    public RHListener() {
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {

        try {
            Item clickedItem = event.getItem();
            Player player = event.getPlayer();
            Block block = event.getBlock();

            if (clickedItem != null && clickedItem.getNamedTag() != null) {
                boolean isReturnHomeTag = clickedItem.getNamedTag().getBoolean("RH");

                if (!isReturnHomeTag) {
                    var namedTag = clickedItem.getNamedTag();

                    if (namedTag.getTags().containsKey("display")) {
                        CompoundTag displayItem = (CompoundTag) namedTag.getTags().get("display");
                        StringTag nameTag = (StringTag) displayItem.getTags().get("Name");
                        isReturnHomeTag = nameTag.data.equals("Return Home");
                    }
                }

                if (isReturnHomeTag && (block.getId() != Block.STONE_BUTTON) && (block.getId() != Block.WOODEN_BUTTON)) {// && (block.getId() != Block.AIR)){
                    if (block.getId() != Block.AIR) {
                        event.setCancelled();
                        return;
                    }
                    event.setCancelled();
//                    ReturnHomeThread rht = new ReturnHomeThread(player);
//                    rht.run();
                    showDlg(player);
                }
            }
        }
        catch (Exception e) {
            Log.exception(e, "Exception occurred in RHListener!");
        }
    }

    private void returnHome(Player player, int type) {
        String world = "world";

        //Reset any level-dependent variables
        Arithmetic.reset();

        //FX & setting location
        switch (type) {
            case 0 -> {
                Log.logGeneric(player, "" + TextFormat.DARK_AQUA + "Returning home."); // <-- Intended to be seen by the player
                Chord home = new Chord(Note.D4, ChordType.MAJ7, 1, false);
                MusicMaker.playArpeggio(player, home, 60, Sound.NOTE_BELL);
                world = Helper.s_mainWorld;
            }
            case 1 -> {
                /// Get the current lesson for the user
                Course course = User.getCurrent().getCurrentCourse();

                if (course != null) {
                    world = course.getLobbyWorldId();
                    world = world.replace(".", "");
                }
                else
                    world = Helper.s_mainWorld;
            }
        }

        Helper.teleportToWorld(player, world);
    }

    void showDlg(Player player) {
        new SimpleWindowForm("simple", "§l§8Return?", "§l§7Exit this activity?")
                .addButton("lobby", "Return to the last lobby")
                .addButton("home", "Return home")
                .addButton("cancel", "Cancel")
                .addHandler(this)
                .sendTo(player);
    }

    @Override
    public void handle(SimpleFormButtonClickEvent e) {
        AtomicInteger type = new AtomicInteger();

        if (e.isClosed()) {
            Log.debug("Form closed!");
            return;
        }

        switch (e.getButton().getName()) {
            case "home" -> type.set(0);
            case "lobby" -> type.set(1);
            case "cancel" -> {
                Log.debug("Cancelled go to home!");
                return;
            }
        }

        returnHome(e.getPlayer(), type.get());
    }
}


