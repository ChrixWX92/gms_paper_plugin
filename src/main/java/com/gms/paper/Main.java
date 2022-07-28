package com.gms.paper;

import com.gms.paper.events.*;
import com.gms.paper.level.gslevels.GSLevelManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;

import com.gms.paper.commands.*;
import com.gms.paper.forms.FormListener;
import com.gms.paper.commands.Pets;
import com.gms.paper.data.CmsApi;
import com.gms.paper.interact.InteractionHandler;
import com.gms.paper.interact.puzzles.ResetPuzzles;
import com.gms.paper.interact.puzzles.handlers.anchors.unique.RemoveDeny;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import org.reflections.Reflections;

public class Main extends JavaPlugin {
    public static Main s_plugin;
    public static String s_ipAddress = null;
    public static MCServer s_mcServer = null;
    private static GSLevelManager gsLevelManager;

    public static GSLevelManager getGsLevelManager() {
        return gsLevelManager;
    }

    public static void setGsLevelManager(GSLevelManager gsLevelManager) {
        Main.gsLevelManager = gsLevelManager;
    }

    @Override
    public void onEnable() {
        s_plugin = this;
        Log.s_console = getServer().getConsoleSender();

        Log.debug("Game School plugin init ...");

        Helper.initEnv();

        Log.debug(String.format("Game School environment: %s", Helper.s_env));

        /// Init the content
        initContent();

        /// This is just some debug code to see if the /drv drive has mounted correctly
        /// on the linux container instance
        if (Helper.isLinux() && Helper.isProd()) {
            File drvDir = new File("/drv");
            if (drvDir.isDirectory()) {
                File[] filesList = drvDir.listFiles();
                Log.debug("Listing files in: " + drvDir.toString());
                for (File f : filesList){
                    Log.debug(String.format("    -> Entry: %s", f.toString()));
                }

//                /// Create a test file
//                Log.debug("Creating test file in drv!");
//                try {
//                    File file = new File(Paths.get(drvDir.toString(), "nukkit-drv-test.txt").toString());
//                    file.createNewFile();
//                }
//                catch (Exception e) {
//                    Log.exception(e, "Error writing file to drv!");
//                }
            }
            else {
                Log.debug("/drv does not exist or is not a directory!");
            }
        }

        try {
            s_mcServer = new MCServer();

            /// Only start the server in production mode
            if (Helper.isProd())
                s_mcServer.start();
        }
        catch (Exception e) {
            e.printStackTrace();

            if (Helper.isProd()) {
                Log.debug("MCServer init failed on a prod server. Unloading the server ...");
                System.exit(MCServer.s_initFailedErrorCode);
            }
        }

        Log.debug(String.format("DB Name: %s", System.getenv("GMS_DB_HOST")));
        Log.debug(String.format("Metadata filename: %s", System.getenv("ECS_CONTAINER_METADATA_FILE")));

        Log.debug(String.format("gms-server endpoint: %s [GMS_CMS_SERVER: %s]", CmsApi.s_public.serverUrl, Helper.getEnv("GMS_CMS_SERVER", true)));

        @NotNull FileConfiguration config = getConfig();

        String folder = getDataFolder().toString();
        this.saveDefaultConfig();

//        if (!Helper.isProd()) {
//            String jwt = Helper.getEnv("jwt", false);
//            Log.debug(String.format("JWT: %s", jwt));
//
//            if (jwt == null || jwt.isEmpty()) {
//                Log.warn("No JWT token given. Trying username/password!");
//                String username = Helper.getEnv("GMS_USERNAME", true);
//                String password = Helper.getEnv("GMS_PASSWORD", true);
//
//                try {
//                    User.connect(username, password, true);
//                }
//                catch (Exception e) {
//                    Log.exception(e, "Error connecting user!");
//                }
//            }
//            else {
//                try {
//                    User.connect(jwt);
//                }
//                catch (Exception e) {
//                    Log.exception(e, String.format("Exception while trying to login with JWT: %s", jwt));
//                    System.exit(-1);
//                }
//            }
//        }

        CmsApi.initCache();
        InteractionHandler.initHandlers();

        ArrayList<PlayerInstance> players = new ArrayList<>();
        config.set("playerInstances", players);

        /// Events
        try {
            initEvents();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        /// Commands
        initCommands();

        /// Custom Content
        initCustomContent();

        /// Settings
        initSettings();

        try {
            setGsLevelManager(new GSLevelManager());
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        // Registering form listeners:
        getServer().getPluginManager().registerEvents(new FormListener(), this);
        getServer().getCommandMap().register("talk", new Pets());

        Log.debug(String.format("Server data path: %s", this.getDataFolder().getPath()));
        //Log.debug(String.format("Plugin file: %s", )); TODO: See outstanding 2
        Log.debug(String.format("Images dir: %s", Helper.getImagesDir()));
        Log.debug(String.format("Current directory: %s", Paths.get("").toAbsolutePath()));
        Log.debug("Game School plugin init DONE successfully!");
    }


    void initSettings() {
        AnchorScanner.setForceLoad(true);
    }

    void initContent() {
//        Helper.copyWorld("nether", true);
//        Helper.copyWorld("fz0", true);
//        Helper.copyWorld("world", true);
//        Helper.copyContent(Helper.s_skinsDirName);
//        Helper.copyContent(Helper.s_imagesDirName);
    }

    void initCommands() {
        getServer().getCommandMap().register("home", new Home());
        getServer().getCommandMap().register("world", new World());
        getServer().getCommandMap().register("viewProgress", new ViewProgress());
        getServer().getCommandMap().register("reset", new ResetPuzzles());
        getServer().getCommandMap().register("resetMCQ", new ResetMCQ());
        if (Helper.isDev()) {
            getServer().getCommandMap().register("override", new Overrides());
            Overrides.loadDefault();
            getServer().getCommandMap().register("teleport", new Teleport());
            getServer().getCommandMap().register("removeDeny", new RemoveDeny());
        }
    }

    void initCustomContent() {
    }

    void initEvents() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        @NotNull PluginManager pm = getServer().getPluginManager();
        Reflections reflections = new Reflections("com.gms.paper.events");
        Set<Class<? extends Listener>> listenerClasses = reflections.getSubTypesOf(Listener.class);
        for (Class<? extends Listener> listener : listenerClasses) {
            pm.registerEvents(listener.getConstructor().newInstance(), this);
        }
//        getServer().setPlayerDataSerializer(new GSPlayerDataSerializer()); TODO: See outstanding 1
    }




}

