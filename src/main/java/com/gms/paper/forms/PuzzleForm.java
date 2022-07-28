package com.gms.paper.forms;

import cn.nukkit.form.element.*;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.event.ModalFormSubmitEvent;
import com.denzelcode.form.window.CustomWindowForm;
import com.denzelcode.form.window.ModalWindowForm;
import com.denzelcode.form.window.SimpleWindowForm;
import com.gms.mc.commands.Pets;
import com.gms.mc.interact.puzzles.maths.Arithmetic;
import com.gms.mc.interact.puzzles.maths.Farm;
import nukkitcoders.mobplugin.entities.GSPetData;

import java.util.ArrayList;
import java.util.List;

public enum PuzzleForm implements Form {

    PICKPET {
        @Override
        public CustomWindowForm getForm() {
            return new CustomWindowForm("pickPet","§l§n§1 Pick a pet!")
                    .addLabel("§l§bChoose your pet:")
                    .addDropdown("§fPets", "petList", Pets.petList)
                    .addLabel("§l§bName your pet:")
                    .addInput("name", "");
        }
    },

    BUYPET {
        @Override
        public ModalWindowForm getForm() {

            String title;
            String mid;
            char firstChar = Pets.getChosenPet().charAt(0);
            int price = GSPetData.petPrices.get(Pets.getChosenPet());
            if ("AEIOUaeiou".indexOf(firstChar) != -1) {
                title = "§l§n§1 Buy an ";
                mid = "buy an ";
            }
            else {
                title = "§l§n§1 Buy a ";
                mid = "buy a ";
            }
            title = title.concat(TextFormat.DARK_PURPLE + Pets.getChosenPet() + "§l§n§1?");

            return new ModalWindowForm("indivPet", title, "§l§bWould you like to " + mid + TextFormat.GOLD + Pets.getChosenPet() + "§l§b for " + TextFormat.LIGHT_PURPLE + price + "§l§b tickets?", "Yes", "No")
                    .addHandler(ModalFormSubmitEvent::isAccepted).addHandler(ModalFormSubmitEvent::isClosed);
        }
    },

    BROKE { // Not enough money to buy a pet - could be made more generic
        @Override
        public SimpleWindowForm getForm(int... values) {
            int petCost = values[0];
            int tickets = values[1];
            return new SimpleWindowForm("broke", "§o§l§cNot enough tickets!", "§fIt costs " + TextFormat.AQUA + "§l" + petCost + "§r§f tickets to buy a \n\n" + TextFormat.GOLD + " §d" + Pets.getChosenPet() + "(s) \n\n§fYou have " + TextFormat.AQUA + "§l" + tickets + "§r§f tickets.\n\nYou need " + TextFormat.AQUA + "§l" + (petCost - tickets) + "§r§f more tickets!\n\nWhy not play some lessons to earn more tickets?\n")
                    .addButton("Okay", "Okay");
        }
    },

    NAME {
        @Override
        public CustomWindowForm getForm() {
            return new CustomWindowForm("petName", "§l§n§1 Name your " + TextFormat.DARK_PURPLE + Pets.getChosenPet() + "§l§n§1?")
                    .addLabel("If you'd like to give your new " + Pets.getChosenPet() + " a name, you can enter it below!")
                    .addInput("name", "");
        }
    },

    FARM1 {
        @Override
        public FormWindowSimple getForm() {
            ElementButtonImageData avatar = new ElementButtonImageData("path", "textures/buttons/avatars/Farmer.png");
            ElementButton button = new ElementButton("Okay!", avatar);
            List<ElementButton> buttons = new ArrayList<>();
            buttons.add(button);
            return new FormWindowSimple("§l§n§1Farmer", "\nWell, hello there!\n\nI'm having a mighty tough time keeping track of my crops.\n\nCould you help me by making sure that these two crop types equal the right amount?\n\n", buttons);
        }
    },

    FARM2 {
        @Override
        public FormWindowSimple getForm() {
            ElementButtonImageData avatar = new ElementButtonImageData("path", "textures/buttons/avatars/Farmer.png");
            ElementButton button1 = new ElementButton("Answer", avatar);
            ElementButton button2 = new ElementButton("Have a look around");
            List<ElementButton> buttons = new ArrayList<>();
            buttons.add(button1);
            buttons.add(button2);
            return new FormWindowSimple("§l§n§1Farmer", "Alrighty, that's great! So, how many crops do I have altogether now?", buttons);
        }
    },

    FARM3 {
        @Override
        public FormWindowCustom getForm() {
            List<Element> elements = new ArrayList<>();
            int value = ((Farm) Arithmetic.currentPuzzle).getCropInputValue();
            elements.add(new ElementSlider("Total",value, value+20, 1));
            return new FormWindowCustom("§l§n§1Farmer", elements);
        }
    };


}

