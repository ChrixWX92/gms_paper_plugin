package com.gms.paper.interact.puzzles.handlers;

import cn.nukkit.Player;
import cn.nukkit.block.BlockWallSign;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.window.CustomWindowForm;
import com.gms.mc.error.InvalidBackendQueryException;
import com.gms.mc.error.InvalidFrameWriteException;
import com.gms.mc.interact.puzzles.BackendUtils;
import com.gms.mc.interact.puzzles.MobGroup;
import com.gms.mc.interact.puzzles.maths.Arithmetic;
import com.gms.mc.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MOBGROUP_InteractionHandler extends PUZZLE_InteractionHandler {

    @Override
    public void handle(PlayerInteractEvent event) throws IOException {

        try {
            super.handle(event);
        } catch (IOException | InvalidBackendQueryException ioException) {
            ioException.printStackTrace();
        }

        AtomicBoolean signReplace = new AtomicBoolean(false);

        AtomicReference<HashMap<Integer, String[]>> data = new AtomicReference<>(new HashMap<>());
        
        AtomicReference<String> id = new AtomicReference<>();
        AtomicReference<String> facing = new AtomicReference<>();
        AtomicReference<Integer> size = new AtomicReference<>();

        CustomWindowForm window = FormAPI.customWindowForm("mobgroup", "§lGenerate MOBGROUP Puzzle")
                .addLabel("§lQuestion Set ID")
                .addInput("ID", "(e.g. \"#tlp.1.1\")")
                .addLabel("§lFacing")
                .addStepSlider("Facing", "The cardinal direction towards which the puzzle will face", Arithmetic.getFacings())
                .addLabel("§lSize")
                .addSlider("Size", "(In blocks, the size of each side of the pen) - (halved (ceiling) to determine Anchor apothem)", 2, 50, 1, 8)
                .addLabel("§lPen?")
                .addToggle("Pen", "(Toggle on to have a pen generate automatically)");

        window.sendTo(player);

        window.addHandler((e) -> {
            if (!this.handled) {
                if (e.isClosed()) return;
                id.set(e.getForm().getResponse().getInputResponse(1));
                facing.set(e.getForm().getResponse().getStepSliderResponse(3).getElementContent());
                size.set((int) e.getForm().getResponse().getSliderResponse(5));
                HashMap<Object, Object> values = new HashMap<>();

                switch (id.get().toLowerCase()) {
                    case "0" -> {
                        values.put(0, "Smart");
                        values.put(1, "Train");
                        values.put(2, "Hour");
                        values.put(3, "Brave");
                        values.put(4, "Town");
                        values.put(5, "Mist");
                        values.put(6, "Sad");
                        values.put(7, "Big");
                        values.put(8, "Great");
                        values.put(9, "House");
                        values.put(10, "Simple");
                        values.put(11, "Monster");
                        values.put(12, "Lonely");
                        values.put(13, "Calm");
                        values.put(14, "Zebra");
                        values.put(15, "Bucket");
                    }
                    default -> {
                        try {
                            BackendUtils.setPuzzleData(player, "MobGroup", id.get().toLowerCase(), 1);
                            signReplace.set(true);
                        } catch (InvalidBackendQueryException invalidBackendQueryException) {
                            BackendUtils.resetPuzzleData();
                            signReplace.set(false);
                            Log.error(TextFormat.RED + "Unable to retrieve data from backend. Ensure that questions have been uploaded and a valid question set ID is specified.");
                            invalidBackendQueryException.printStackTrace();
                            return;
                        }
                        data.set(BackendUtils.getMobGroupData());
                        for (String[] strings : data.get().values()) {
                            values.put(strings[0], Arithmetic.getKeyColorDamageValue(strings[2]));
                        }
                    }
                }

                MobGroup.setAnswerMap(values);

                try {
                    MobGroup.generateMobGroup(event.getPlayer(), event.getBlock().getLocation(), facing.get(), size.get(), e.getForm().getResponse().getToggleResponse(7), data.get());
                    if (signReplace.get())
                        Arithmetic.replacePuzzleSign((BlockWallSign) this.signBlock, BackendUtils.getQuestionSetID());
                } catch (InvalidFrameWriteException invalidFrameWriteException) {
                    invalidFrameWriteException.printStackTrace();
                }
                this.handled = true;
            }
        });
    }

    public static void fetchMobGroup(Player player, String questionSetID) {
        HashMap<Integer, String[]> data;
        HashMap<Object, Object> values = new HashMap<>();

        try {
            BackendUtils.setPuzzleData(player, "MobGroup", questionSetID.toLowerCase(), 1);
        } catch (InvalidBackendQueryException invalidBackendQueryException) {
            BackendUtils.resetPuzzleData();
            Log.error(TextFormat.RED + "Unable to retrieve data from backend. Ensure that questions have been uploaded and a valid question set ID is specified.");
            invalidBackendQueryException.printStackTrace();
            return;
        }
        data = BackendUtils.getMobGroupData();
        for (String[] strings : data.values()) {
            values.put(strings[0], Arithmetic.getKeyColorDamageValue(strings[2]));
        }

        MobGroup.setAnswerMap(values);
        MobGroup.initializeMobGroup(player, data);

    }

}
