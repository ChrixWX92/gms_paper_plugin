package com.gms.paper.util;

import com.gms.paper.Main;
import com.gms.paper.data.*;
import com.gms.paper.events.TeleportHandler;
import com.gms.paper.interact.puzzles.maths.Arithmetic;
import com.gms.paper.interact.puzzles.utils.TextCleaner;
import com.gms.paper.level.GameMode;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class Helper {
    public static String s_mainWorld = "fz0";
    public static String s_mongoId = "deadbeefdeadbeefdeadbeef";
    public static String s_devDirName = "dev";
    public static String s_profileDirName = ".profile";
    public static String s_mcContentDir = "gms_mc_content";
    public static String s_worldsDirName = "worlds";
    public static String s_skinsDirName = "skins";
    public static String s_imagesDirName = "images";
    public static String s_myServerDirName = ".my_servers";
    public static String s_myServerHeartbeatDirName = ".heartbeat";
    public static String s_playerDataDirName = "playerData";
    public static String s_sessionDataDir = ".session";

    public static String s_lobbyId = "#lobby";
    public static String s_ageId = "{age}";
    public static String s_env = "prod";
    public static String s_stagingSuffix = "_STAGING";
    public static String s_devSuffix = "_DEV";

    public static Vector3D s_mainLobbySpawnPos = new Vector3D(43, 5, -1);

    public static boolean isWindows() {
        return System.getProperty("os.name").contains("Windows");
    }
    public static boolean isLinux() {
        return !isWindows();
    }

    public static boolean isDev() {
        User user = User.getCurrent();

        if (user != null)
            return user.isDev();

        return s_env.equals("dev") || isContentDev();
    }

    public static void setDebugDevMode(Player player) {
        if (isDev()) {
            Log.error("Game mode set to dev mode to assist block debugging!");
            player.setGameMode(GameMode.S_DEV.bukkitGameMode);
        }
    }

    public static boolean isDevTesting() {
        return s_env.equals("dev-testing");
    }

    public static boolean isContentDev() {
        return s_env.equals("content-dev");
    }

    public static boolean isLocalDev() {
        if (isDev()) {
            User currentUser = User.getCurrent();
            return currentUser == null || currentUser.isDev();
        }

        return false;
    }
    public static boolean isProd() {
        return s_env.equals("prod");
    }
    public static boolean isStaging() {
        return s_env.equals("staging");
    }

    public static void initEnv() {
        String env = Helper.getEnv("env", true);
        if (env != null && !env.isEmpty())
            s_env =  env;
    }

    public static String[] splitMongoId(String mongoId) {
        return new String[] {
                mongoId.substring(0, 8),
                mongoId.substring(8, 16),
                mongoId.substring(16, 24)
        };
    }

    public static Path getDrvRoot() {
        return isWindows() ? Paths.get("Y:/") : Paths.get("/drv");
    }

    public static Path getNukkitDevDir() {
        return Paths.get(Main.s_plugin.getDataFolder().getPath(), s_devDirName);
    }
    public static Path getSessionDir() {
        return Paths.get(getNukkitDevDir().toString(), s_sessionDataDir);
    }

    public static String getCleanLevelName(String levelName) {
        File path = new File(levelName);
        return path.getName();
    }

    public static String getLevelPath(String levelName) {
        if (!levelName.contains("/") && !levelName.contains("\\"))
            return pathToDir(Paths.get(getNukkitWorldBaseDir().toString(), levelName).toString());
        return pathToDir((new File(levelName)).getPath());
    }

    public static String getEnvServerType(String key) {
        return getEnvServerType(key, false);
    }

    public static String getEnvServerType(String key, String defaultValue) {
        return getEnvServerType(key, false, defaultValue);
    }

    public static String getEnvServerType(String key, boolean differentiateDev) {
        return getEnvServerType(key, differentiateDev, "");
    }

    public static String getEnvServerType(String key, boolean differentiateDev, String defaultValue) {
        String fullKey = key;
        if (!Helper.isProd()) {
            if (!differentiateDev) {
                fullKey += s_stagingSuffix;
            }
            else {
                if (Helper.isDev())
                    fullKey += s_devSuffix;
                else if (Helper.isStaging())
                    fullKey += s_stagingSuffix;
            }
        }

        String value = getEnv(fullKey, true);
        if (value == null || value.isEmpty())
            return defaultValue;
        return value;
    }

    public static String getEnv(String key, boolean checkGlobalEnv) {
        /// First preference is always command line arguments
        String value = System.getProperty(key);

        /// Secondly, we check the config
        if ((value == null || value.isEmpty()) && Main.s_plugin != null)
            value = Main.s_plugin.getConfig().getString(key);

        /// If global/system env is added to check list, then we add that
        if (checkGlobalEnv) {
            if (value == null || value.isEmpty())
                value = System.getenv(key);
        }

        return value;
    }

    public static String fixMsg(String msg) {
        return msg.replaceAll("\r", "");
    }

    public static String[] titleLineBreaker(String title, String msg){
        int maxCharTitle = 15;
        int maxCharSubtitle = 35;

        String titleWithBreaks = textLineBreakers(title, maxCharTitle, " ");
        String subtitleWithBreaks = textLineBreakers(msg, maxCharSubtitle, " ");

        String[] updatedTitles = new String[2];
        updatedTitles[0] = titleWithBreaks;
        updatedTitles[1] = subtitleWithBreaks;

        Log.debug(String.format("Title : [%s] : Subtitle : [%s]", updatedTitles[0], updatedTitles[1]));

        return updatedTitles;
    }

    public static String textLineBreakers(String text, int maxCharacters, String splitIdentifier) {
        if(text == null || text.isEmpty()){
            return null;
        }

        String textFix = text.replaceAll("\\n", " ");
        String[] allLines = textFix.split(splitIdentifier);
        StringBuilder textWithLineBreaks = new StringBuilder();
        StringBuilder charLimit = new StringBuilder();

        for (int i = 0; i < allLines.length; i++) {
            if (charLimit.length() + allLines[i].length() < maxCharacters) {
                charLimit.append(allLines[i]);

                if ((i + 1 != allLines.length)) {
                    charLimit.append(" ");
                }
            }
            else {
                textWithLineBreaks.append(charLimit).append("\n");
                charLimit = new StringBuilder();
                i--;
            }
        }

        textWithLineBreaks.append(charLimit);

        Log.debug(String.format("Text with line breaks : [%s] ", textWithLineBreaks));

        return textWithLineBreaks.toString();
    }

    public static void setPlayerTitle(Player p, String msg) {
        Log.info(String.format("Set Player Title: %s", msg.replaceAll("\n", "\\n")));
        String nmsg = fixMsg(msg);
        //p.sendTitle(titles[0]); TODO: See outstanding 3
    }

    public static void setPlayerTitle(Player p, String title, String msg, int fadeIn, int stay, int fadeOut) {
        Log.info(String.format("Set Player Title: [%s] %s", title.replaceAll("\n", "\\n"), msg.replaceAll("\n", "\\n")));
        String nmsg = fixMsg(msg);
        String[] titles = titleLineBreaker(title, nmsg);
        p.sendTitle(titles[0], titles[1], fadeIn, stay, fadeOut); // TODO: See outstanding 3
    }

    public static void setPlayerTitle(Player p, String title, String msg) {
        setPlayerTitle(p, title, msg, 10, 500, 100);
    }

    public static void setPlayerSubtitle(Player p, String msg){
        Log.info(String.format("Set Player subtitle: %s", msg.replaceAll("\n", "\\n")));
        String nmsg = fixMsg(msg);
        String[] titles =  titleLineBreaker(null, nmsg);
        p.sendTitle(titles[0], titles[1]); // TODO: See outstanding 3
    }

    public static String[] splitWorldNameToCourseInfo(String worldName) {
        String[] parts = worldName.split("\\.");

        if (parts.length == 2)
            return parts;

        return worldName.split("(?<=\\D)(?=\\d)");
    }

    public static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        pw.flush();

        return sw.toString();
    }

    public static GamePosition parseLocation(String textPos) {
        try {
            boolean absolute = false;

            if (textPos.endsWith("W") || textPos.endsWith("w")) {
                absolute = true;
                textPos = textPos.replace("W", "").replace("w", "").trim();
            }

            String[] coords = textPos.split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            int z = Integer.parseInt(coords[2]);

            return new GamePosition(null, new Vector3D(x, y, z), absolute);
        }
        catch (Exception e) {
            Log.exception(e, String.format("Exception while parsing coords: %s", textPos));
        }

        return new GamePosition(null, new Vector3D(0, 0, 0), false);
    }

    public static void kickPlayer(Player player, String reason) {
        Log.logAndSend(player, reason);
        @NotNull TextComponent reasonComponent = LegacyComponentSerializer.legacySection().deserialize(reason);
        @NotNull TextComponent leaveMessage = LegacyComponentSerializer.legacySection().deserialize(""); //TODO: can change this if we want a bespoke message
        PlayerKickEvent kickEvent = new PlayerKickEvent(player, reasonComponent, leaveMessage, PlayerKickEvent.Cause.WHITELIST);
        Main.s_plugin.getServer().getPluginManager().callEvent(kickEvent);;
    }

    public static int generateRandomIntIntRange(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public static boolean teleportToLobby(Player player) {
        return teleportToWorld(player, s_mainWorld);
    }

    public static String formatAnswerSign(String prompt) {
        String formattedPrompt = "§3" + prompt.replace(" ", " §3");;
        return formattedPrompt.replaceAll("§r", "§r§3");
    }

    public static String formatQuestionSign(String prompt) {

        String formattedPrompt = "§5" + prompt.replace(" ", " §5");
        return formattedPrompt.replaceAll("§r", "§r§5");
    }

    public static Path getImagesDir() {
        File imagesDir = new File(Paths.get(Main.s_plugin.getDataFolder().getPath(), s_imagesDirName).toString());
        return imagesDir.toPath();
    }

    public static Path getNukkitWorldBaseDir() {
        return Paths.get(Main.s_plugin.getDataFolder().getPath(), s_worldsDirName);
    }

    public static Path getContentBaseDir() {
        Path contentBaseDir = Paths.get(getDrvRoot().toString(), s_mcContentDir);
        if (!Helper.isDev())
            return contentBaseDir;

        if (!(new File(contentBaseDir.toString())).exists())
            contentBaseDir = Paths.get(Main.s_plugin.getDataFolder().getPath(), "..", s_mcContentDir);

        return contentBaseDir;
    }

    public static Path getContentWorldDir(String worldName) {
        return Paths.get(getContentBaseDir().toString(), s_worldsDirName, worldName);
    }

    public static Path getNukkitWorldDir(String worldName) {
        return Paths.get(getNukkitWorldBaseDir().toString(), worldName);
    }

    public static boolean copyWorld(String worldName) {
        return copyWorld(worldName, false);
    }

    public static boolean copyContent(String contentName) {
        return false;
        /// TODO: later

//        String srcContentDir = Paths.get(getContentBaseDir().toString(), contentName).toString();
//        String dstContentDir = Paths.get(Main.s_plugin.getServer().getDataPath(), contentName).toString();
//
//        var srcPath = new File(srcContentDir);
//        var dstPath = new File(dstContentDir);
//
//        if (!srcPath.exists())
//            return false;
//
//        if (isContentDev()) {
//            if (dstPath.exists()) {
//                Log.debug(String.format("Content-Dev mode. Not overwriting existing data for: %s", contentName));
//                return false;
//            }
//        }
//
//        Log.debug(String.format("Copying content %s: %s => %s", contentName, srcPath.toString(), dstPath.toString()));
//
//        Helper.copyFolder(srcPath, dstPath);
//        return true;
    }

    public static boolean copyWorld(String worldName, boolean force) {
        /// This should be copied at startup
        if (worldName.equals(s_mainWorld) && !force)
            return false;

        String srcWorldDir = getContentWorldDir(worldName).toString();
        String dstWorldDir = getNukkitWorldDir(worldName).toString();

        File srcPath = new File(srcWorldDir);
        File dstPath = new File(dstWorldDir);

        if (!srcPath.exists())
            return false;

        /// If it's a content
        if (isContentDev()) {
            if (dstPath.exists()) {
                Log.debug(String.format("Content-Dev mode. Not overwriting existing world: %s", worldName));
                return false;
            }
        }

        Log.debug(String.format("Copying world %s: %s => %s", worldName, srcPath.toString(), dstPath.toString()));

        Helper.copyFolder(srcPath, dstPath);
        return true;
    }

    public static boolean deleteDirectory(File directoryToBeDeleted) {
        if (isContentDev())
            return false;

        try {
            File[] allContents = directoryToBeDeleted.listFiles();
            if (allContents != null && allContents.length > 0) {
                for (File file : allContents) {
                    deleteDirectory(file);
                }
            }
            boolean didDelete = directoryToBeDeleted.delete();
            if (!didDelete)
                Log.error(String.format("Unable to delete: %s", directoryToBeDeleted));
            return didDelete;
        }
        catch (Exception e) {
            Log.exception(e, String.format("Error deleting directory: %s", directoryToBeDeleted.toString()));
            return false;
        }
    }

    public static String sanitiseWorldName(String worldName) {
        String orgWorldName = worldName;

        if (!worldName.contains(".") && worldName.endsWith("l"))
            worldName = worldName.substring(0, worldName.length() - 1) + ".0";

        if (worldName.contains(s_ageId)) {
            int age = User.getCurrent().getAge();
            String ageStr = String.format("%d", age);
            worldName = worldName.replace(s_ageId, ageStr);
        }

        if (worldName.startsWith("#")) {
            if (worldName.equals(s_lobbyId)) {
                if (User.getCurrent().getCurrentCourse() != null) {
                    return String.format("%s.0", User.getCurrent().getCurrentCourse().contentId);
                }
            }
        }

        Log.debug(String.format("Changed world: %s => %s", orgWorldName, worldName));

        return worldName;
    }

    public synchronized static boolean teleportToWorld(Player player, String worldName) {
        return teleportToWorld(player, worldName, null, 0);
    }

    public static boolean teleportToWorld(Player player, String worldName, Vector3D pos, float headYaw) {
//        if (worldName.equals(s_mainWorld))
//            return teleportToLobby(player);

        Course previousCourse = User.getCurrent().getCurrentCourse();
        Lesson previousLesson = User.getCurrent().getCurrentLesson();

        TeleportHandler teleport = new TeleportHandler();
        worldName = sanitiseWorldName(worldName);

        String[] parts = Helper.splitWorldNameToCourseInfo(worldName);

        if (parts.length != 2) {
            Log.logAndSend(player, String.format("Command is not of the format: <courseId>.<lessonId> [%s]", worldName));
            return false;
        }

        String courseId = parts[0];
        String lessonId = parts[1];

//        setPlayerSubtitle(player, String.format("Teleporting to world: %s", worldName));

        Course course = Course.get(courseId);
        if (course == null) {
            Log.logAndSend(player, String.format("Unable to find course: %s", courseId));
            return false;
        }

        course.cache();

        int lessonIndex = Integer.parseInt(lessonId);

        Lesson lesson = course.getLessonAt(lessonIndex);
        if (lesson == null) {
            Log.logAndSend(player, String.format("Unable to load lesson: %s", worldName));
            return false;
        }

        String targetWorldName = worldName.replace(".", "");

        if (!course.refId.isEmpty())
            targetWorldName = String.format("%s%d", course.refId.replace(".", ""), lessonIndex);

        File worldPath = null;
        Path profileDir = User.getCurrent().getProfile().getProfileDir();

        if (!isContentDev()) {
            if (lesson.isUserBuildZone()) {
                Log.debug(String.format("World %s is user build zone!", worldName));
                worldPath = lesson.initUserBuildZone(profileDir);
            }
            else if (!lesson.isFunZone() || lesson.isLearningZone()) {
                Log.debug(String.format("World %s is lesson/adventure zone!", worldName));

                /// If it's not a fun zone or it's a learning zone, then we
                /// copy from the master directory to the user directory
                worldPath = lesson.initLessonZone(profileDir, User.getCurrent().getSessionKey());
            }
        }

        if (worldPath == null)
            targetWorldName = Helper.getNukkitWorldDir(targetWorldName).toString();
        else
            targetWorldName = worldPath.toString();

        Log.debug(String.format("World Name: %s => %s [worldPath = %s]", worldName, targetWorldName, worldPath));

        targetWorldName = pathToDir(targetWorldName);

        lesson.enter(targetWorldName);

        Location spawn;

        if (pos != null)
            spawn = new Location(pos.x, pos.y, pos.z);
        else
            spawn = new Location(lesson.spawnPos.x, lesson.spawnPos.y, lesson.spawnPos.z);

        teleport.tpWorld(player, targetWorldName, spawn, headYaw, worldName);

        User.getCurrent().setCurrentCourse(course, lesson);

        /// exit previous lesson
        if (previousLesson != null)
            previousLesson.exit();

        //remove puzzle inventory on teleporting (Would be better to check which puzzle is currently active but this is fine for now)
        Arithmetic.removePuzzleInventoryItems(player);

        return true;
    }

    public static String pathToDir(String path) {
        if (Helper.isWindows()) {
            if (!path.endsWith("\\"))
                path += "\\";
        }
        else {
            if (!path.endsWith("/"))
                path += "/";
        }

        return path;
    }

    public static String getPublicIPAddress() throws MalformedURLException, IOException {
        URL myIP = new URL("http://checkip.amazonaws.com");
        BufferedReader in = null;
        in = new BufferedReader(new InputStreamReader(
                myIP.openStream()));
        String ip = in.readLine();
        return ip;
    }

    public static LocalDate getLocalDate(Date dt) {
        return Instant.ofEpochMilli(dt.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static ArrayList<Integer> randomiseIndices(int min, int max) {
        int length = max - min + 1;
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = min; i < max; i++)
            list.add(Integer.valueOf(i));

        Collections.shuffle(list);

        return list;
    }

    public static boolean isMongoId(String id) {
        return id.length() == 24;
    }

    public static String getExceptionAsString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static boolean checkHttpError(String url, HttpResponse<String> response, String jwt) throws RuntimeException {
        String body = response.body();
        int statusCode = response.statusCode();

        if (response.statusCode() != 200) {
            Log.httpError(url, response, jwt);
            return false;
        }

        return true;
    }

    public static String getKeyDefault(JsonObject obj, String key, String defValue) {
        return obj.has(key) ? obj.get(key).getAsString() : defValue;
    }

    public static String getKey(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsString() : "";
    }

    public static void copyFolder(File source, File destination) {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdirs();
            }

            String files[] = source.list();

            for (String file : files) {
                File srcFile = new File(source, file);
                File destFile = new File(destination, file);

                copyFolder(srcFile, destFile);
            }
        }
        else {
            InputStream in = null;
            OutputStream out = null;

            try {
                in = new FileInputStream(source);
                out = new FileOutputStream(destination);

                byte[] buffer = new byte[1024];

                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
            catch (Exception e) {
                try {
                    if (in != null)
                        in.close();
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }

                try {
                    if (out != null)
                        out.close();
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
