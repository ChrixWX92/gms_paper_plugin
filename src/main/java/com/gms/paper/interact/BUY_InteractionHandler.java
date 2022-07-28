package com.gms.paper.interact;

import cn.nukkit.block.*;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityBanner;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.utils.DyeColor;
import com.gms.paper.util.TextFormat;
import com.denzelcode.form.window.CustomWindowForm;
import com.denzelcode.form.window.ModalWindowForm;
import com.denzelcode.form.window.SimpleWindowForm;
import com.gms.paper.PlayerInstance;
import com.gms.paper.data.GamePosition;
import com.gms.paper.error.InvalidBackendQueryException;
import com.gms.paper.util.BlockShopUtils;
import com.gms.paper.util.Log;
import org.apache.commons.text.WordUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

public class BUY_InteractionHandler extends InteractionHandler {

    private Item currentBlock;

    private void forms(int id, String blockName, int blockCount) throws IOException {

        switch (id) {

            case 0 -> // Shop window
                    new SimpleWindowForm("shop", "§5§l" + blockName, " ")
                            .addButton("one", "§lBuy one")
                            .addButton("stack", "§lBuy a stack (" + currentBlock.getMaxStackSize() + ")")
                            .addButton("custom", "§lPick an amount")
                            .addButton("cancel", "Cancel")
                            .addHandler((e) -> {
                                switch (e.getButton().getName()) {
                                    case "one" -> {
                                        try {
                                            forms(2, blockName, 1);
                                        } catch (IOException ioException) {
                                            ioException.printStackTrace();
                                        }
                                    }
                                    case "stack" -> {
                                        try {
                                            forms(2, blockName, currentBlock.getMaxStackSize());
                                        } catch (IOException ioException) {
                                            ioException.printStackTrace();
                                        }
                                    }
                                    case "custom" -> {
                                        try {
                                            forms(1, blockName, 0);
                                        } catch (IOException ioException) {
                                            ioException.printStackTrace();
                                        }
                                    }
                                    case "cancel" -> Log.debug("Cancelled block transaction.");
                                }
                            }).sendTo(player);

            case 1 -> //Custom window
                    new CustomWindowForm("custom", "§l§8Return?")
                            .addLabel("")
                            .addSlider("slider", "Amount", 1F, (float) currentBlock.getMaxStackSize(), 1)
                            .addHandler((e) -> {
                                        try {
                                            forms(2, blockName, (int) e.getForm().getResponse().getSliderResponse(1));
                                        } catch (IOException ioException) {
                                            ioException.printStackTrace();
                                        }
                                    }
                            ).sendTo(player);

            case 2 -> {//Price window
            int tickets = PlayerInstance.getPlayer(player.getName()).getProfile().tickets;
            float cost = Float.parseFloat(BlockShopUtils.getPrices().get(currentBlock.getName()));
            float total = cost * blockCount;
            if (total < tickets) {
                new ModalWindowForm("Confirm", "§fAre you sure you would like to buy\n" + TextFormat.GOLD + blockCount + " §d" + blockName + "(s) \n§ffor " + TextFormat.AQUA + "§l" + total + "§r§f tickets?", "Yes", "No")
                        .addHandler((e) -> {
                            if (e.isAccepted()) {
                                for (int i = 0; i < blockCount; i++) player.getInventory().addItem(currentBlock);
                                PlayerInstance.getPlayer(player.getName()).getProfile().spendTickets((int)total);
                            }

                        }).sendTo(player);
            } else { // No money window
                new SimpleWindowForm("broke", "§o§l§cNot enough tickets!", "§fIt costs " + TextFormat.AQUA + "§l" + total + "§r§f tickets to buy \n\n" + TextFormat.GOLD + blockCount + " §d" + blockName + "(s) \n\n§fYou have " + TextFormat.AQUA + "§l" + tickets + "§r§f tickets.\n\nYou need " + TextFormat.AQUA + "§l" + (total-tickets) + "§r§f more tickets!\n\nWhy not play some lessons to earn more tickets?\n")
                    .addButton("Okay", "Okay")
                    .addHandler((e) -> {
                        try {
                            forms(0, blockName, 0);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }).sendTo(player);
                }
            }
        }
    }

    @Override
    public void handle(PlayerInteractEvent event) throws IOException, InvalidBackendQueryException {

        super.handle(event);

        //int cost = Integer.parseInt(signText[1]);
        BlockButtonStone button = (BlockButtonStone) event.getBlock();
        GamePosition buttonLoc = new GamePosition(null, button.getLocation(), true);
        GamePosition labelSignLoc = new GamePosition(buttonLoc, new Location(0, 1, 0), false);
        HashMap<String, String> prices = BlockShopUtils.getPrices();

        if (level.getBlockEntity(labelSignLoc) instanceof BlockEntitySign labelSignEnt) {

            String[] initialString = labelSignEnt.getText();

            String[] newSignText = new String[2];
            Block labelSign = level.getBlock(labelSignLoc);
            Block block;

            switch (button.getFacing()) {
                case NORTH -> block = labelSign.up().south();
                case EAST -> block = labelSign.up().west();
                case SOUTH -> block = labelSign.up().north();
                case WEST -> block = labelSign.up().east();
                default -> {
                    //player.sendMessage(TextFormat.RED + "I don't know how you've done this, but that sign is facing upwards.");
                    Log.error(TextFormat.RED + "I don't know how you've done this, but that sign is facing upwards.");
                    return;
                }
            }

            String price = prices.get(block.getName())+ " Tickets";
            if (price.equals("N Tickets")) {price = TextFormat.RED + "NOT FOR SALE";}
            newSignText[0] = TextFormat.LIGHT_PURPLE + block.getName();
            newSignText[1] = TextFormat.AQUA + price;

            // Formatting text before affixing it to the sign
            try {
                labelSignEnt.setText(buyFix(newSignText, labelSignEnt, block));
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }

            if (Arrays.equals(initialString, labelSignEnt.getText())) {
                currentBlock = block.toItem();
                forms(0,labelSignEnt.getText()[0], 0);
            }

        }


        //BlockEntitySign labelSign = (BlockEntitySign) button.level.getBlock(labelSignLoc);


        /// check if player has tickets
        /*
        if (profile.tickets >= cost) {
            profile.spendTickets(cost);
            int itemID = Integer.parseInt(signText[2]);
            ItemsToSell.getItem(itemID, player);

            Helper.setPlayerTitle(player, TextFormat.GREEN + "Thanks for shopping,\ncome again soon!");
            profile.showTicketsStatus(player, String.format("You now have %d tickets left.", profile.tickets));
        }
        else {
            Log.logGeneric(player, "This item costs " + cost + "tickets and you only have " + profile.tickets);
            Log.logGeneric(player, "Head through to the lessons to earn more!");
            Helper.setPlayerTitle(player, TextFormat.AQUA + "You need more tickets!\nGo through a lesson to\n earn more!");
        }
        */
    }
    private String[] buyFix(String[] text, BlockEntitySign sign, Block block) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        sign.setText(text);
        String[] oldText = sign.getText();
        String[] newText = new String[4];
        String price = oldText[1];

        //Getting rid of carpet colour block caps
        if (block instanceof BlockCarpet) {
            int counter = 0;
            DyeColor carpetColor = DyeColor.getByWoolData(block.getDamage());
            for (String line : oldText) {
                if (line.contains(String.valueOf(carpetColor))) {
                    newText[counter] = line.replace(String.valueOf(carpetColor), carpetColor.getName());
                }
                else {
                    newText[counter] = oldText[counter];
                }
                counter++;
            }
        // Adding colour to names of blocks that don't already include it
        } else if (block instanceof BlockConcrete) {
            Method getDyeColor = block.getClass().getMethod("getDyeColor");
            DyeColor dyeColor = (DyeColor) getDyeColor.invoke(block);
            newText[0] = TextFormat.LIGHT_PURPLE + dyeColor.getName() + " " + block.getName();
            if (oldText.length - 1 >= 0) System.arraycopy(oldText, 1, newText, 1, oldText.length - 1);
            //Manual: for (int i = 1 ; i < oldText.length ; i++) {newText[i] = oldText[i];}
        } else if (block instanceof BlockConcretePowder) {
            switch (block.getDamage()) {
                case 0 -> newText[0] = TextFormat.LIGHT_PURPLE + "White Concrete Powder";
                case 1 -> newText[0] = TextFormat.LIGHT_PURPLE + "Orange Concrete Powder";
                case 2 -> newText[0] = TextFormat.LIGHT_PURPLE + "Magenta Concrete Powder";
                case 3 -> newText[0] = TextFormat.LIGHT_PURPLE + "Light Blue Concrete Powder";
                case 4 -> newText[0] = TextFormat.LIGHT_PURPLE + "Yellow Concrete Powder";
                case 5 -> newText[0] = TextFormat.LIGHT_PURPLE + "Lime Concrete Powder";
                case 6 -> newText[0] = TextFormat.LIGHT_PURPLE + "Pink Concrete Powder";
                case 7 -> newText[0] = TextFormat.LIGHT_PURPLE + "Grey Concrete Powder";
                case 8 -> newText[0] = TextFormat.LIGHT_PURPLE + "Light Grey Concrete Powder";
                case 9 -> newText[0] = TextFormat.LIGHT_PURPLE + "Cyan Concrete Powder";
                case 10 -> newText[0] = TextFormat.LIGHT_PURPLE + "Purple Concrete Powder";
                case 11 -> newText[0] = TextFormat.LIGHT_PURPLE + "Blue Concrete Powder";
                case 12 -> newText[0] = TextFormat.LIGHT_PURPLE + "Brown Concrete Powder";
                case 13 -> newText[0] = TextFormat.LIGHT_PURPLE + "Green Concrete Powder";
                case 14 -> newText[0] = TextFormat.LIGHT_PURPLE + "Red Concrete Powder";
                case 15 -> newText[0] = TextFormat.LIGHT_PURPLE + "Black Concrete Powder";
            }
        }
        else if (block instanceof BlockBanner banner) {
            if (banner.level != null) {
                BlockEntity blockEntity = banner.level.getBlockEntity(banner);
                if (blockEntity instanceof BlockEntityBanner) {
                    switch (((BlockEntityBanner) blockEntity).getBaseColor()) {
                        case 0 -> newText[0] = TextFormat.LIGHT_PURPLE + "Black Banner";
                        case 1 -> newText[0] = TextFormat.LIGHT_PURPLE + "Red Banner";
                        case 2 -> newText[0] = TextFormat.LIGHT_PURPLE + "Green Banner";
                        case 3 -> newText[0] = TextFormat.LIGHT_PURPLE + "Brown Banner";
                        case 4 -> newText[0] = TextFormat.LIGHT_PURPLE + "Blue Banner";
                        case 5 -> newText[0] = TextFormat.LIGHT_PURPLE + "Purple Banner";
                        case 6 -> newText[0] = TextFormat.LIGHT_PURPLE + "Cyan Banner";
                        case 7 -> newText[0] = TextFormat.LIGHT_PURPLE + "Light Grey Banner";
                        case 8 -> newText[0] = TextFormat.LIGHT_PURPLE + "Grey Banner";
                        case 9 -> newText[0] = TextFormat.LIGHT_PURPLE + "Pink Banner";
                        case 10 -> newText[0] = TextFormat.LIGHT_PURPLE + "Lime Banner";
                        case 11 -> newText[0] = TextFormat.LIGHT_PURPLE + "Yellow Banner";
                        case 12 -> newText[0] = TextFormat.LIGHT_PURPLE + "Light Blue Banner";
                        case 13 -> newText[0] = TextFormat.LIGHT_PURPLE + "Magenta Banner";
                        case 14 -> newText[0] = TextFormat.LIGHT_PURPLE + "Orange Banner";
                        case 15 -> newText[0] = TextFormat.LIGHT_PURPLE + "White Banner";
                    }
                }
            }
        }
        // Removing "Block" from the ends of names of blocks where it is incorrect/unnecessary
        else if (block instanceof BlockBed || block instanceof BlockDoor) {
            int counter = 0;
            for (String line : oldText) {
                if (line.contains("Block")) {newText[counter] = line.replace("Block", "");}
                else {newText[counter] = oldText[counter];}
                counter++;
            }
        }
        // Giving Ender Chests their actual name
        else if (block instanceof BlockEnderChest) {
            newText[0] =  TextFormat.LIGHT_PURPLE + "Ender Chest";
            newText[3] = text[1];
        }
        // Giving slabs their actual names
        else if (block instanceof BlockSlabRedSandstone && block.getDamage() > 1) {
            switch (block.getDamage()) {
                case 2 -> newText[0] = TextFormat.LIGHT_PURPLE + "Prismarine Slab";
                case 3 -> newText[0] = TextFormat.LIGHT_PURPLE + "Dark Prismarine Slab";
                case 4 -> newText[0] = TextFormat.LIGHT_PURPLE + "Prismarine Bricks Slab";
                case 5 -> newText[0] = TextFormat.LIGHT_PURPLE + "Mossy Cobblestone Slab";
                case 6 -> newText[0] = TextFormat.LIGHT_PURPLE + "Smooth Sandstone Slab";
                case 7 -> newText[0] = TextFormat.LIGHT_PURPLE + "Red Nether Brick Slab";
            }
            newText[3] = text[1];
        }
        // Giving wall blocks their actual names
        else if (block instanceof BlockWall && block.getDamage() > 1) {
            switch (block.getDamage()) {
                case 2 -> newText[0] = TextFormat.LIGHT_PURPLE + "Granite Wall";
                case 3 -> newText[0] = TextFormat.LIGHT_PURPLE + "Diorite Wall";
                case 4 -> newText[0] = TextFormat.LIGHT_PURPLE + "Andesite Wall";
                case 5 -> newText[0] = TextFormat.LIGHT_PURPLE + "Sandstone Wall";
                case 6 -> newText[0] = TextFormat.LIGHT_PURPLE + "Brick Wall";
                case 7 -> newText[0] = TextFormat.LIGHT_PURPLE + "Stone Brick Wall";
                case 8 -> newText[0] = TextFormat.LIGHT_PURPLE + "Mossy Stone Brick Wall";
                case 9 -> newText[0] = TextFormat.LIGHT_PURPLE + "Nether Brick Wall";
                case 10 -> newText[0] = TextFormat.LIGHT_PURPLE + "End Stone Brick Wall";
                case 11 -> newText[0] = TextFormat.LIGHT_PURPLE + "Prismarine Wall";
                case 12 -> newText[0] = TextFormat.LIGHT_PURPLE + "Red Sandstone Wall";
                case 13 -> newText[0] = TextFormat.LIGHT_PURPLE + "Red Nether Brick Wall";
            }
            newText[3] = text[1];
        } else {
            newText[0] = oldText[0];
            newText[3] = oldText[1];
        }

        // Running the array through setText once more before formatting
        sign.setText(newText);
        oldText = sign.getText();
        newText = new String[4];


        /* --- Formatting text properly --- */

        // All worlds must begin with capitals
        // All lines other than the bottom must be color-formatted
        int counter = 0;
        for (String line : oldText) {
            if (line != null) {
                line = TextFormat.clean(line);
                line = WordUtils.capitalize(line);
                line = line.replaceAll("\\b(\\p{L}+)\\b", "§d$1");
                //line = line.replaceAll("§", "§d");
                //line = line.replaceAll("\\b(\\p{L}+)\\b", "+$1");
                //if (line.startsWith("§0")) {newText[counter] = line.replace("§0", "§d");}
                //else if (!line.startsWith("§d")) {newText[counter] = "§d" + line;}
                //else {newText[counter] = line;}
                newText[counter] = line;
            }
            counter++;
        }
        //TODO: Ensuring block price is always the bottom line
        newText[1] = price;
        newText[2] = "";
        newText[3] = "";

        return newText;

    }

}
