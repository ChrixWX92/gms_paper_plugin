package com.gms.paper.interact.puzzles.handlers;

import cn.nukkit.Player;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.gms.mc.error.InvalidBackendQueryException;
import com.gms.mc.interact.InteractionHandler;
import com.gms.mc.interact.puzzles.BackendUtils;
import com.gms.mc.interact.puzzles.Checks;
import com.gms.mc.interact.puzzles.maths.Arithmetic;
import com.gms.mc.util.Log;
import com.gms.paper.error.InvalidBackendQueryException;
import com.gms.paper.interact.puzzles.BackendUtils;
import com.gms.paper.interact.puzzles.Checks;
import com.gms.paper.util.Log;
import com.gms.paper.util.TextFormat;
import com.gms.paper.util.Vector3D;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class CHECKS_InteractionHandler extends PUZZLE_InteractionHandler {
    @Override
    public void handle(PlayerInteractEvent event) throws IOException, InvalidBackendQueryException {

        try {
            super.handle(event);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        AtomicReference<String> id = new AtomicReference<>();
        AtomicReference<String> facing = new AtomicReference<>();
        AtomicReference<Integer> apothem = new AtomicReference<>();

        FormAPI.customWindowForm("checks", "§lGenerate CHECKS Puzzle")
                .addLabel("§lQuestion Set ID")
                .addInput("ID","(e.g. \"#tlp.1.1\")")
                .addLabel("§lFacing")
                .addStepSlider("Facing", "The cardinal direction towards which the puzzle will face", Arithmetic.getFacings())
                .addLabel("§lApothem")
                .addSlider("Apothem","In blocks, the size of this puzzle's Anchor's apothem",2,50,1,8)
                .addHandler((e) -> {
                    if (e.isClosed()) return;
                    id.set(e.getForm().getResponse().getInputResponse(1));
                    facing.set(e.getForm().getResponse().getStepSliderResponse(3).getElementContent());
                    apothem.set((int) e.getForm().getResponse().getSliderResponse(5));
                    HashMap<Integer, String> columns = new HashMap<>();
                    HashMap<Integer, String> rows = new HashMap<>();
                    switch(id.get().toLowerCase()) {
                        case "0" -> {
                            columns.put(0, "§l§bSAMPLE");
                            columns.put(1, "§l§bSAMPLE");
                            rows.put(0, "Line 1");
                            rows.put(1, "Line 2");
                            rows.put(2, "Line 3");
                            rows.put(3, "Line 4");
                            rows.put(4, "Line 5");
                        }
                        case "1" -> {
                            columns.put(0, "§l§eTRUE");
                            columns.put(1, "§l§6FALSE");
                            rows.put(0, "Roses are red.");
                            rows.put(1, "Creepers are blue.");
                            rows.put(2, "Pigs come from fountains.");
                            rows.put(3, "There's an egg in the zoo");
                        }
                        case "2" -> {
                            columns.put(0, "§l§bNOUN");
                            columns.put(1, "§l§bPRONOUN");
                            columns.put(2, "§l§bADJECTIVE");
                            columns.put(3, "§l§bVERB");
                            columns.put(4, "§l§bADVERB");
                            rows.put(0, "§lAre");
                            rows.put(1, "§lQuietly");
                            rows.put(2, "§lPig");
                            rows.put(3, "§lCreeper");
                            rows.put(4, "§lSpherical");
                            rows.put(5, "§lBlue");
                            rows.put(6, "§lIt");
                        }
                        default -> {
                            try {
                                BackendUtils.setPuzzleData(player, "Checks", id.get().toLowerCase(), 1);
                            } catch (InvalidBackendQueryException invalidBackendQueryException) {
                                BackendUtils.resetPuzzleData();
                                Log.error(TextFormat.RED + "Unable to retrieve data from backend. Ensure that questions have been uploaded and a valid question set ID is specified.");
                                invalidBackendQueryException.printStackTrace();
                            }
                            HashMap<String, String> data = BackendUtils.getChecksData();
                            if (data != null) {
                                int counter = 0;
                                for (Map.Entry<String, String> entry : data.entrySet()) {
                                    columns.put(counter, entry.getValue()); // TODO: Colour + embolden these
                                    rows.put(counter, entry.getKey());
                                    counter++;
                                }
                            } else {
                                NullPointerException exception = new NullPointerException("Puzzle data retrieval unsuccessful: getPuzzleData() produces null.");
                                exception.printStackTrace();
                            }
                        }
                    }

                    try {
                        Checks.generateChecks(event.getPlayer(), event.getClickedBlock().getLocation(), facing.get(), columns, rows, apothem.get());
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }

                }).sendTo(player);

    }

    public static void fetchChecks(Player player, String questionSetID, String facing, Vector3D buttonLoc) {
        HashMap<String, String> data;
        HashMap<Integer, String> columns = new HashMap<>();
        HashMap<Integer, String> rows = new HashMap<>();

        try {
            BackendUtils.setPuzzleData(player, "Checks", questionSetID.toLowerCase(), 1);
        } catch (InvalidBackendQueryException invalidBackendQueryException) {
            BackendUtils.resetPuzzleData();
            Log.error(TextFormat.RED + "Unable to retrieve data from backend. Ensure that questions have been uploaded and a valid question set ID is specified.");
            invalidBackendQueryException.printStackTrace();
            return;
        }
        data = BackendUtils.getChecksData();
        if (data != null) {
            int counter = 0;
            for (Map.Entry<String, String> entry : data.entrySet()) {
                columns.put(counter, entry.getValue()); // TODO: Colour + embolden these
                rows.put(counter, entry.getKey());
                counter++;
            }
        } else {
            NullPointerException exception = new NullPointerException("Puzzle data retrieval unsuccessful: getPuzzleData() produces null.");
            exception.printStackTrace();
        }

        Checks.initializeChecks(player, buttonLoc, facing, columns, rows);

    }

}
