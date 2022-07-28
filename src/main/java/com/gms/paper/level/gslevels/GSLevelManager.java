package com.gms.paper.level.gslevels;

import org.bukkit.entity.Player;
import cn.nukkit.Server;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.LevelProvider;
import cn.nukkit.level.format.LevelProviderManager;
import cn.nukkit.utils.LevelException;
import com.gms.paper.Main;
import com.gms.paper.level.gslevels.rules.GSGameRule;
import com.gms.paper.level.gslevels.rules.GSGameRules;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;

import java.io.File;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GSLevelManager {

    private int gsBaseTickRate;
    private Map<String, GSLevel> gsLevels = new HashMap<>();
    private Map<String, Boolean> gsLevelCopied = new HashMap<>();
    private final Server server;
    private GSLevel gsLevelCurrent = null;

    private final Map<GameRule, Boolean> defaultGameRules = Map.ofEntries(
            new AbstractMap.SimpleImmutableEntry<>(GameRule.KEEP_INVENTORY, true),
            new AbstractMap.SimpleImmutableEntry<>(GameRule.PVP, false),
            new AbstractMap.SimpleImmutableEntry<>(GameRule.DO_DAYLIGHT_CYCLE, false),
            new AbstractMap.SimpleImmutableEntry<>(GameRule.DO_WEATHER_CYCLE, false),
            new AbstractMap.SimpleImmutableEntry<>(GameRule.DO_FIRE_TICK, false),
            new AbstractMap.SimpleImmutableEntry<>(GameRule.FIRE_DAMAGE, false),
            new AbstractMap.SimpleImmutableEntry<>(GameRule.TNT_EXPLODES, false),
            new AbstractMap.SimpleImmutableEntry<>(GameRule.FALL_DAMAGE, false),
            new AbstractMap.SimpleImmutableEntry<>(GameRule.DROWNING_DAMAGE, false),
            new AbstractMap.SimpleImmutableEntry<>(GameRule.FREEZE_DAMAGE, false)
    );

    public GSLevelManager() throws Throwable { //TODO: HELPER for any level information lost during cast
        this.server = Main.s_plugin.getServer();
//        this.server.getLogger().info(TextFormat.GREEN + "[GMS] --- Initialising GS Level Manager...");
//        this.gsBaseTickRate = this.server.getConfig("level-settings.base-tick-rate", 1);
//        this.server.getLogger().info(TextFormat.AQUA + "[GMS} Creating GS Levels:");
//        List<String> levelNameLog = new ArrayList<>();
//        int success = 0;
//        int fail = 0;
//        for (String levelName : getLevelsFromDirectory()) { //(Map.Entry<Integer, Level> e : this.server.getWorlds().entrySet()) { //Every registered level name
//            GSLevel gsLevel = null;
//            Level l;
//            try {
//                l = loadGSLevel(levelName);
//            }
//            catch (Exception e) {
//                levelNameLog.add(levelName);
//                fail++;
//                continue;
//            }
//            if (l != null) {
//                Level level = new GSLevel(l.getServer(), l, null, null, GSGameRules.getDefault(), l.getProvider().getClass());
//                try {
//                    gsLevel = (GSLevel) level;
//                    //if (e.getValue() instanceof GSLevel) gsLevel = (GSLevel) e.getValue(); //TODO: Either this or the constructor will need to reference level structure to determine child/parent levels
//                }
//                catch (ClassCastException exception) {
//                    exception.printStackTrace();
//                }
//                if (gsLevel != null) {
//                    this.applyGameRules(gsLevel);
//                    this.applyLevelSettings(gsLevel);
//                    gsLevels.put(l.getName(), gsLevel);
//                }
//                else {
//                    throw new NullPointerException("GSLevel null, or incorrectly constructed.");
//                }
//                success++;
//            }
//            else {
//                levelNameLog.add(levelName);
//                fail++;
//            }
//        }
//        this.server.getLogger().info(TextFormat.AQUA + "[GMS} " + TextFormat.GREEN + success + TextFormat.AQUA + " GS Levels successfully created.");
//        if (fail > 0) {
//            this.server.getLogger().info(TextFormat.AQUA + "[GMS} " + TextFormat.RED + fail + TextFormat.AQUA + " Levels could not be converted:");
//            for (String name : levelNameLog) {
//                this.server.getLogger().info(TextFormat.AQUA + "[GMS} --- " + TextFormat.RED + name);
//            }
//        }
//        this.server.getLogger().info(TextFormat.GREEN + "[GMS] --- GS Level Manager successfully initialised.");
    }

    public void clearCache() {
        gsLevels = new HashMap<>();
        gsLevelCopied = new HashMap<>();
        gsLevelCurrent = null;
    }

    public GSLevel initGSLevel(String levelName, String levelPath) {
        GSLevel gsLevel = null;

        try {
            Level l;
            l = loadGSLevel(levelName, levelPath);

            if (l != null) {
                gsLevel = new GSLevel(l.getServer(), Helper.getCleanLevelName(levelName), Helper.getWorldPath(levelPath),
                        null, null, GSGameRules.getDefault(), l.getProvider().getClass());
                this.applyGameRules(gsLevel);
                this.applyLevelSettings(gsLevel);

                gsLevels.put(levelName, gsLevel);
                gsLevels.put(levelPath, gsLevel);
            }
            else {
                Log.error(String.format("Unable to load GS level: %s [%s]", levelName, levelPath));
            }
        }
        catch (Throwable e) {
            Log.exception(e, String.format("Unable to create GSLevel: %s [%s]", levelName, levelPath));
        }

        return gsLevel;
    }

    public GSLevel getCurrent() {
        return gsLevelCurrent;
    }

    public void setCurrent(GSLevel level) {
        gsLevelCurrent = level;
    }

    private Level loadGSLevel(String name, String path) throws Throwable {
        if (Objects.equals(name.trim(), "")) {
            throw new LevelException("Empty level name");
        }

//        String path;
//        if (!name.contains("/") && !name.contains("\\")) {
//            path = Helper.getNukkitWorldDir(name).toString(); /// Paths.get(this.server.getDataPath(), "worlds", name).toString();
//            if (Helper.isWindows())
//                path += "\\";
//            else
//                path += "/";
//        }
//        else {
//            path = name;
//        }
        Class<? extends LevelProvider> provider = LevelProviderManager.getProvider(path);
        if (provider == null) {
            Log.error(String.format("No provider for GS level: %s", path));
            return null;
        }

        Level level;
        try {
            level = new Level(Main.s_plugin.getServer(), name, path, provider);
        }
        catch (Exception e) {
            Log.exception(e, String.format("Error loading level: %s", name));
            Throwable t = new Throwable(e.getMessage());
            throw t;
        }

        level.initLevel();
        //this.server.getPluginManager().callEvent(new LevelLoadEvent(level));
        level.setTickRate(gsBaseTickRate);

        Log.info(String.format("GS level loaded: %s", path));

        return level;
    }

    private String[] getLevelsFromDirectory() {
        File file = new File(this.server.getDataPath() + "worlds/");
        return file.list((current, name) -> new File(current, name).isDirectory());
    }

    public void applyGameRules(GSLevel gsLevel) {
        if (gsLevel.gameRules == null)
            gsLevel.gameRules = gsLevel.getProvider().getGamerules();

        for (Map.Entry<GameRule, Boolean> e : this.defaultGameRules.entrySet()) {
            gsLevel.gameRules.setGameRules(e.getKey(), String.valueOf(e.getValue()));
            gsLevel.setGSGameRules(GSGameRules.getDefault());
        }

        switch (gsLevel.getName()) {
            case "fz2" -> { // Build Zone
                gsLevel.getGSGameRules().setGSGameRule(GSGameRule.OPEN_CHEST, true);
                gsLevel.getGSGameRules().setGSGameRule(GSGameRule.USE_CRAFTING_TABLE, true);
                gsLevel.getGSGameRules().setGSGameRule(GSGameRule.USE_ANVIL, true);
                gsLevel.getGSGameRules().setGSGameRule(GSGameRule.CAN_FLY, true);

                gsLevel.gameRules.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                gsLevel.gameRules.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
            }
        }
    }

    //TODO: Should the below be called during teleport handling (after event)?
    public void applyLevelSettings(GSLevel gsLevel) { //TODO: Should be informed by the backend

        switch (gsLevel.getName()) {
            case "fz0" -> { // Build Zone
                for (Player player : gsLevel.getPlayers().values()) {
                    player.setGamemode(0);// Set gamemode to Survival
                }
            }
            default -> {
                gsLevel.setTime(6000); // Set time to noon
                clearWeather(gsLevel); // Clear weather
                for (Player player : gsLevel.getPlayers().values()) {
                    player.setGamemode(2);// Set gamemode to Adventure
                    player.removeAllEffects(); // Remove potion effects
                }
            }
        }
    }

    public GSLevel getGSLevel(Player player) {
        return getGSLevel(player.getWorld());
    }

    public GSLevel getGSLevel(Level level) {
        GSLevel gsLevel = gsLevels.get(level.getName());

        if (gsLevel == null)
            return initGSLevel(Helper.getCleanLevelName(level.getName()), level.getFolderName());

        return gsLevel;
    }

    public void clearWeather(GSLevel gsLevel) {
        gsLevel.setRaining(false);
        gsLevel.setThundering(false);
        gsLevel.sendWeather(gsLevel.getPlayers().values());
    }

    private <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    //TODO: cache inventory

    public Map<String, GSLevel> getGSLevels() {
        return gsLevels;
    }

    public Map<GameRule, Boolean> getDefaultGameRules() {
        return defaultGameRules;
    }

    public int getGsBaseTickRate() {
        return gsBaseTickRate;
    }

    public void setGsBaseTickRate(int gsBaseTickRate) {
        this.gsBaseTickRate = gsBaseTickRate;
    }
}
