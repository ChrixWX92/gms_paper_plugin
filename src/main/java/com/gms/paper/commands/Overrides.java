package com.gms.paper.commands;

import com.gms.paper.Main;
import com.gms.paper.PlayerInstance;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Set;

public class Overrides extends Command {
    private static HashMap<String, String> s_overrides = new HashMap<>();

    public static String getOverride(String command) {
        return s_overrides.get(command);
    }

    public static boolean hasOverride(String command) {
        return s_overrides.containsKey(command);
    }

    public static void addOverride(String command, String value) {
        Log.debug(String.format("Override: %s => %s", command, value));
        s_overrides.put(command, value);
    }

    public static void loadDefault() {
        try {
            String baseDir = Main.s_plugin.getDataFolder().getPath();
            Path overrideJsonFile = Paths.get(baseDir, Helper.s_devDirName, "overrides.json");

            if (Files.exists(overrideJsonFile)) {
                JsonObject json = JsonParser.parseString(Files.readString(overrideJsonFile)).getAsJsonObject();

                if (json != null) {
                    Set<String> keys = json.keySet();
                    for (String key : keys) {
                        String value = json.get(key).getAsString();
                        addOverride(key, value);
                    }
                }
            }
        }
        catch (IOException e) {
            Log.error("Error while trying to load DEV overrides. Ignoring ...");
        }
    }
    
    public Overrides(){
        super("override");
        this.setDescription("Override some system variable");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            Log.logAndSend(sender, "This command is just for players!");
            return false;
        }

        Player p = (Player)sender;
        if (!Helper.isDev()) {
            Log.logAndSend(sender, "Only supported in dev mode!");
            return false;
        }

        String command = args[0];
        String value = args[1];

        if (command.equals("tickets")) {
            int numTickets = Integer.parseInt(value);
            PlayerInstance.getPlayer(p.getName()).getProfile().setTickets(p, numTickets);
        }
        else {
            addOverride(command, value);
        }

        return false;
    }
}
