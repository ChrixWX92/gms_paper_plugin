package com.gms.paper.interact.puzzles.handlers;

import cn.nukkit.Player;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.window.CustomWindowForm;
import com.gms.mc.error.InvalidBackendQueryException;
import com.gms.mc.error.InvalidFrameWriteException;
import com.gms.mc.interact.puzzles.Assemble;
import com.gms.mc.interact.puzzles.BackendUtils;
import com.gms.mc.interact.puzzles.maths.Arithmetic;
import com.gms.mc.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ASSEMBLE_InteractionHandler extends PUZZLE_InteractionHandler {
    @Override
    public void handle(PlayerInteractEvent event) throws IOException {
        try {
            super.handle(event);
        }
        catch (IOException | InvalidBackendQueryException ioException) {
            ioException.printStackTrace();
        }

        AtomicReference<String> id = new AtomicReference<>();
        AtomicReference<String> facing = new AtomicReference<>();
        AtomicReference<Integer> apothem = new AtomicReference<>();

        CustomWindowForm window = FormAPI.customWindowForm("assemble", "§lGenerate ASSEMBLE Puzzle")
                .addLabel("§lQuestion Set ID")
                .addInput("ID", "(e.g. \"#tlp.1.1\")")
                .addLabel("§lFacing")
                .addStepSlider("Facing", "The cardinal direction towards which the puzzle will face", Arithmetic.getFacings())
                .addLabel("§lSize")
                .addSlider("Size", "In blocks, the size of this puzzle's Anchor's apothem", 2, 50, 1, 8);

        window.sendTo(player);

        CustomWindowForm handler = window.addHandler((e) -> {
            if (!this.handled) {
                if (e.isClosed()) return;
                id.set(e.getForm().getResponse().getInputResponse(1));
                facing.set(e.getForm().getResponse().getStepSliderResponse(3).getElementContent());
                apothem.set((int) e.getForm().getResponse().getSliderResponse(5));
                HashMap<Integer, String> answers = new HashMap<>();
                HashMap<Integer, String> questions = new HashMap<>();
                HashMap<Integer, String> inventory = new HashMap<>();
                switch (id.get().toLowerCase()) {
                    case "0" -> {
                        //Answer
                        answers.put(0, "Hello,");
                        answers.put(1, "my");
                        answers.put(2, "name");
                        answers.put(3, "is");
                        answers.put(4, "Steve.");
                        //Question
                        questions.put(0, "Hello,");
                        questions.put(1, "my");
                        questions.put(2, "");
                        questions.put(3, "is");
                        questions.put(4, "Steve.");
                        //Inventory
                        inventory.put(0, "name");
                        inventory.put(1, "said");
                        inventory.put(2, "blue");
                        inventory.put(3, "quickly");
                    }
                    case "1" -> {
                        //Answer
                        answers.put(0, "Steve");
                        answers.put(1, "is");
                        answers.put(2, "happy.");
                        //Question
                        questions.put(0, "Steve");
                        questions.put(1, "");
                        questions.put(2, "happy.");
                        //Inventory
                        inventory.put(0, "are");
                        inventory.put(1, "might");
                        inventory.put(2, "is");
                        inventory.put(3, "should");
                    }
                    case "2" -> {
                        //Answer
                        answers.put(0, "The");
                        answers.put(1, "old");
                        answers.put(2, "dog");
                        answers.put(3, "sat");
                        answers.put(4, "under");
                        answers.put(5, "the");
                        answers.put(6, "chestnut");
                        answers.put(7, "tree.");
                        //Question
                        questions.put(0, "The");
                        questions.put(1, "");
                        questions.put(2, "dog");
                        questions.put(3, "sat");
                        questions.put(4, "");
                        questions.put(5, "the");
                        questions.put(6, "chestnut");
                        questions.put(7, "tree.");
                        //Inventory
                        inventory.put(0, "town");
                        inventory.put(1, "quietly");
                        inventory.put(2, "under");
                        inventory.put(3, "while");
                        inventory.put(4, "old");
                        inventory.put(5, "sometimes");
                        inventory.put(6, "found");
                    }
                    default -> {
                        try {
                            BackendUtils.setPuzzleData(player, "Assemble", id.get().toLowerCase(), 1);
                        } catch (InvalidBackendQueryException invalidBackendQueryException) {
                            invalidBackendQueryException.printStackTrace();
                        }
                        HashMap<Integer, List<HashMap<Integer, String>>> data = BackendUtils.getAssembleData();
                        Log.debug(data.toString());
                        if (data != null) {
                            Assemble.setQuestionSet(data);
                            Assemble.setQuestionNumber(1);
                            Assemble.setQuestionSetSize(data.size());
                            for (int i = 0; i < 3; i++) {
                                for (Map.Entry<Integer, String> word : data.get(1).get(i).entrySet()) {
                                    switch (i) {
                                        case 0 -> answers.put(word.getKey(), word.getValue());
                                        case 1 -> questions.put(word.getKey(), word.getValue());
                                        case 2 -> inventory.put(word.getKey(), word.getValue());
                                    }
                                }
                            }
                        } else {
                            NullPointerException exception = new NullPointerException("Puzzle data retrieval unsuccessful: getPuzzleData() produces null.");
                            exception.printStackTrace();
                        }
                    }
                }

                try {
                    Assemble.generateAssemble(event.getPlayer(), event.getBlock().getLocation(), facing.get(), answers, questions, inventory, apothem.get());
                } catch (InvalidFrameWriteException | InterruptedException exception) {
                    exception.printStackTrace();
                }
                this.handled = true;
            }
        });
    }

    public static void fetchAssemble(Player player, String questionSetID) {

        HashMap<Integer, List<HashMap<Integer, String>>> data;

        try {
            BackendUtils.setPuzzleData(player, "Assemble", questionSetID.toLowerCase(), 1);
        }
        catch (InvalidBackendQueryException invalidBackendQueryException) {
            BackendUtils.resetPuzzleData();
            Log.error(TextFormat.RED + "Unable to retrieve data from backend. Ensure that questions have been uploaded and a valid question set ID is specified.");
            invalidBackendQueryException.printStackTrace();
            return;
        }
        data = BackendUtils.getAssembleData();
        HashMap<Integer, String> answers = new HashMap<>();
        HashMap<Integer, String> questions = new HashMap<>();
        HashMap<Integer, String> inventory = new HashMap<>();

        if (data != null) {
            Assemble.setQuestionSet(data);
            Assemble.setQuestionNumber(1);
            Assemble.setQuestionSetSize(data.size());
            //TODO get question set size
            //for (Map.Entry<Integer, String> word : data.get(1).get(2).entrySet()) inventory.put(word.getKey(), word.getValue());
            for (int i = 0; i < 3; i++) {
                for (Map.Entry<Integer, String> word : data.get(1).get(i).entrySet()) {
                    switch (i) {
                        case 0 -> answers.put(word.getKey(), word.getValue());
                        case 1 -> questions.put(word.getKey(), word.getValue());
                        case 2 -> inventory.put(word.getKey(), word.getValue());
                    }
                }
            }
        }
        else {
            NullPointerException exception = new NullPointerException("Puzzle data retrieval unsuccessful: getPuzzleData() produces null.");
            exception.printStackTrace();
        }

        Assemble.initializeAssemble(player, answers, questions, inventory);
    }
}
