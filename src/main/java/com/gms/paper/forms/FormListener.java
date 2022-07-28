package com.gms.paper.forms;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.response.FormResponseSimple;
import com.denzelcode.form.element.Input;
import com.denzelcode.form.event.CustomFormSubmitEvent;
import com.denzelcode.form.event.ModalFormSubmitEvent;
import com.denzelcode.form.window.CustomWindowForm;
import com.denzelcode.form.window.ModalWindowForm;
import com.denzelcode.form.window.SimpleWindowForm;
import com.gms.mc.commands.FarmTalk;
import com.gms.mc.commands.Pets;
import com.gms.mc.error.InvalidFrameWriteException;
import com.gms.mc.interact.puzzles.Puzzle;
import com.gms.mc.interact.puzzles.maths.Arithmetic;
import com.gms.mc.interact.puzzles.maths.Farm;
import com.gms.mc.util.Log;
import nukkitcoders.mobplugin.entities.GSPetData;

import static com.gms.mc.commands.Pets.getPet;
import static com.gms.mc.commands.Pets.spawnHandler;
import static com.gms.mc.forms.PuzzleForm.*;
import static com.gms.mc.interact.puzzles.PuzzleType.FARM;

public class FormListener implements Listener {

    // Pets
    public static String petName;
    private FormResponse cachedResponse;

    // No API
    @EventHandler(priority = EventPriority.NORMAL)
    public void onSimpleFormSubmit(PlayerFormRespondedEvent event) throws InvalidFrameWriteException, InterruptedException, CloneNotSupportedException {
        if (event.getResponse() instanceof FormResponseSimple frs) {
            if (Arithmetic.currentPuzzle != null && Arithmetic.currentPuzzle.getPuzzleType() == FARM) {
                if (frs.getClickedButton().getText().equals("Answer")) {
                    FarmTalk.sendForm(event.getPlayer(), FARM3.getForm());
                }
            }
        }
        else if (event.getResponse() instanceof FormResponseCustom frc) {
            if (Arithmetic.currentPuzzle != null) {
                Puzzle puzzle = Arithmetic.currentPuzzle;
                if (puzzle.getPuzzleType() == FARM) {
                    Farm farm = (Farm) puzzle;
                    if ((int) frc.getSliderResponse(0) == farm.getFinalQuestionAnswer()) {
                        farm.setCurrentChallenge(farm.getCurrentChallenge() + 1);
                        farm.solve();
                    } else {
                        Arithmetic.mark(farm.getPlayer(), false);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onModalFormSubmit(ModalFormSubmitEvent event) throws Exception {

        ModalWindowForm form = event.getForm();
        if (form.wasClosed()) return;
        if (form.getResponse() != cachedResponse) {
             Player p = event.getPlayer();

             String formName = event.getForm().getName();
             if (!event.isFormValid(formName)) return;

             switch (formName) {
                 case "indivPet" -> {
                     if (event.isAccepted()) {
                         int petCost = GSPetData.petPrices.get(Pets.getChosenPet());
                         int tickets = Pets.getProfile().tickets;
                         if ((petCost > tickets)) {
                             ((SimpleWindowForm) BROKE.getForm(petCost, tickets)).sendTo(p);
                         } else {
                             ((CustomWindowForm) NAME.getForm()).sendTo(p);
                         }
                     }
                 }
                 default -> throw wrongForm();
            }
        }
        cachedResponse = form.getResponse();

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCustomFormSubmit(CustomFormSubmitEvent event) throws Exception {

        CustomWindowForm form = event.getForm();
        if (form.wasClosed()) return;
        if (form.getResponse() != cachedResponse) {
            Player p = event.getPlayer();

            String formName = event.getForm().getName();
            if (!event.isFormValid(formName)) return;

            switch (formName) {
                case "petName" -> {

                    String name = ((Input) form.getElement("name")).getValue();
                    if (!name.replace(" ", "").equals("")) {
                        spawnHandler(Pets.getChosenPet(), p, name);
                    } else {
                        spawnHandler(Pets.getChosenPet(), p, "");
                        petName = null;
                    }
                }
                case "pickPet" -> {

                    FormResponseCustom data = form.getResponse();
                    String petType = data.getDropdownResponse(1).getElementContent();

                    String spawnName;
                    if (!(data.getInputResponse(3).replace(" ", "").equals(""))) {
                        petName = data.getInputResponse(3);
                        spawnName = data.getInputResponse(3);
                    } else {
                        petName = null;
                        spawnName = "";
                    }

                    spawnHandler(petType, p, spawnName);

                    if (Pets.petName != null && !Pets.petName.equals("")){
                        ((Entity) getPet()).setNameTag(Pets.petName);
                        ((Entity) getPet()).setNameTagVisible(true);
                        ((Entity) getPet()).setNameTagAlwaysVisible(true);
                    }

                    petName = null;

                }
                default -> throw wrongForm();
            }
        }
        cachedResponse = form.getResponse();
    }

    private Exception wrongForm(){
        Log.error("Unknown form passed to NewFormListener.");
        return new Exception();
    }
}
