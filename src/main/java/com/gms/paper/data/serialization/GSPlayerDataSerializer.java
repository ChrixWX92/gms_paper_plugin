package com.gms.paper.data.serialization;

import org.bukkit.entity.Player;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.utils.PlayerDataSerializer;
import com.gms.paper.data.User;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

public class GSPlayerDataSerializer implements PlayerDataSerializer {

    @Override
    public Optional<InputStream> read(String s, UUID uuid) {
        User user = User.getCurrent();

        if (user == null) {
            Log.warn("No currently logged in user. Not loading player data!");
            return Optional.empty();
        }

        File path = new File(Paths.get(user.getProfile().getProfileDir().toString(), Helper.s_playerDataDirName, uuid.toString()).toString());
        Log.debug(String.format("Loading player data %s => %s", uuid, path));

        try {
            return Optional.of(new FileInputStream(path));
        }
        catch (FileNotFoundException e) {
            Log.exception(e, String.format("Unable to load player data uuid: %s [%s]", uuid, path));
        }

        return Optional.empty();
    }

    @Override
    public OutputStream write(String s, UUID uuid) throws IOException {
        User user = User.getCurrent();

        if (user == null) {
            Log.warn("No currently logged in user. Trying previous user ...");

            User previous = User.getPrevious();
            if (previous == null) {
                Log.warn("No previously logged in user found either. Not writing user data!");
                return null;
            }

            user = previous;
        }

        File dirPath = new File(Paths.get(user.getProfile().getProfileDir().toString(), Helper.s_playerDataDirName).toString());
        dirPath.mkdirs();

        File path = new File(Paths.get(dirPath.toString(), uuid.toString()).toString());
        Log.debug(String.format("Saving player data %s => %s", uuid, path));

        return new FileOutputStream(path);
    }

    public String[] encodePlayerInventory(Player player) throws IllegalStateException {
        return encodePlayerInventory(player.getInventory());
    }
    /**
     * Converts the player inventory to a String[] of Base64 Strings. First String is content, second is armour.
     *
     * @param playerInventory to turn into an array of Strings
     * @return String[]: [main, armour]
     * @throws IllegalStateException from method calls
     */
    public String[] encodePlayerInventory(PlayerInventory playerInventory) throws IllegalStateException {
        String main = inventoryToBase64(playerInventory);
        String armour = itemStackArrayToBase64(playerInventory.getArmorContents());
        return new String[] {main, armour};
    }

    /**
     * Converts an Item array to Base64 String.
     *
     * @param items to turn into a Base64 String
     * @return Base64 String of the items
     * @throws IllegalStateException on conversion failure
     */
    private String itemStackArrayToBase64(Item[] items) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            GSOutputStream gsos = new GSOutputStream(outputStream);
            gsos.writeInt(items.length);
            for (Item item : items) gsos.writeObject(item);
            gsos.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            Log.error("Unable to encode inventory contents.");
            throw new IllegalStateException("Unable to encode inventory contents.", e);
        }
    }

    /**
     * Converts an inventory to a Base64 String.
     *
     * @param inventory to be converted
     * @return Base64 String of the inventory provided
     * @throws IllegalStateException on conversion failure
     */
    public String inventoryToBase64(Inventory inventory) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            GSOutputStream gsos = new GSOutputStream(outputStream);
            gsos.writeInt(inventory.getSize());
            for (int i = 0; i < inventory.getSize(); i++) gsos.writeObject(inventory.getItem(i));
            gsos.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            Log.error("Unable to encode inventory contents.");
            throw new IllegalStateException("Unable to encode inventory contents.", e);
        }
    }

    /**
     * Gets a Nukkit.Inventory object from an encoded, Base64 String.
     *
     * @param data Base64 String comprising inventory data
     * @return Inventory object from the Base64 String
     */
    public PlayerInventory base64ToPlayerInventory(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            GSInputStream gsis = new GSInputStream(inputStream);
            PlayerInventory inventory = new PlayerInventory(null);
            inventory.setSize(gsis.readInt());
            for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, (Item) gsis.readObject());
            gsis.close();
            return inventory;
        } catch (Exception e) {
            Log.error("Unable to decode String data.");
            throw new IllegalStateException("Unable to decode String data.", e);
        }
    }

    /**
     * Gets an Item[] from a Base64 String.
     *
     * @param data Base64 String to convert to Item array
     * @return Item array created from the Base64 String
     * @throws IOException on decoding failure
     */
    public Item[] base64ToItemArray(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            GSInputStream gsis = new GSInputStream(inputStream);
            Item[] items = new Item[gsis.readInt()];
            for (int i = 0; i < items.length; i++) items[i] = (Item) gsis.readObject();
            gsis.close();
            return items;
        } catch (ClassNotFoundException e) {
            Log.error("Unable to decode String data.");
            throw new IllegalStateException("Unable to decode String data.", e);
        }
    }

}
