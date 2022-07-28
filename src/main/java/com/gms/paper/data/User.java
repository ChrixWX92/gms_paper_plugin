package com.gms.paper.data;

import org.bukkit.entity.Player;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import com.gms.paper.Main;
import com.gms.paper.commands.Overrides;
import com.gms.paper.interact.InteractionHandler;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class User {
    public String _id;
    public String name;
    public String username;
    public String email;
    public String provider;
    public boolean confirmed = false;
    public boolean blocked = false;
    public String alias;
    public String gender;
    public Object avatar;
    public Date dateOfBirth;
    public UserRole role;

    private transient User _parent;
    private transient ChildProfile _profile;
    private transient Course _currentCourse;
    private transient Lesson _currentLesson;
    private transient UserState _state;
    private CmsApi _api;

    private ArrayList<String> _prevSessionKeys = new ArrayList<>();

    public ChildProfile getProfile() {
        if (_profile != null)
            return _profile;

        _profile = _api.getId("child-profiles", _id, ChildProfile.class);
        return _profile;
    }

    public CmsApi getAPI() { return _api; }

    public boolean isMCServer() {
        return username.equalsIgnoreCase(Helper.getEnv("GMS_MC_USERNAME", true));
    }

    private static void setCurrentUser(User current) {
        if (s_current != null)
            s_previous = s_current;
        s_current = current;

        if (s_current != null) {
            File sessionBaseDir = new File(Helper.getSessionDir().toString());

            /// Clean up the session dir
            if (sessionBaseDir.exists()) {
                Log.info("Current user logged in. Cleaning up sessions dir: %s");

                File[] sessionDirs = sessionBaseDir.listFiles();

                if (sessionDirs != null && sessionDirs.length > 0) {
                    for (File sessionDir : sessionDirs) {
                        try {
                            File[] levelPaths = sessionDir.listFiles();

                            if (levelPaths != null && levelPaths.length > 0) {
                                for (var levelPath : levelPaths) {
                                    try {
                                        String levelPathDir = Helper.pathToDir(levelPath.toString());
                                        Level level = Main.s_plugin.getServer().getWorldByName(levelPathDir);
                                        if (level != null) {
                                            Log.debug(String.format("Unloading level: %s", levelPath));
                                            level.unload();
                                        }
                                    }
                                    catch (Exception e) {
                                        Log.exception(e, String.format("Error unloading level: %s", levelPath));
                                    }
                                }
                            }
                        }
                        catch (Exception e) {
                            Log.exception(e, String.format("Exception while fetching level from previous session dir: %s", sessionDir));
                        }
                    }
                }

                Helper.deleteDirectory(sessionBaseDir);
            }
        }
    }

    public String getSessionKey() {
//        if (_prevSessionKeys.size() > 5) {
//            var tmp = _prevSessionKeys;
//
//            /// Add the list
//            _prevSessionKeys = new ArrayList<>();
//            _prevSessionKeys.add(tmp.get(tmp.size() - 1));
//
//            /// We remove all but the last one, since the last one is going to be in use currently. We don't
//            /// want any unexpected behaviour, even though we're in the process of changing to the next
//            /// level right now.
//            for (int i = 0; i < tmp.size() - 1; i++) {
//                String prevSessionKey = tmp.get(i);
//                var dstPath = new File(Paths.get(Helper.getSessionDir().toString(), prevSessionKey).toString());
//                Log.debug(String.format("Clearing old session dir: %s [%s]", dstPath, prevSessionKey));
//
//                try {
//                    File[] levelPaths = dstPath.listFiles();
//
//                    if (levelPaths != null && levelPaths.length > 0) {
//                        for (var levelPath : levelPaths) {
//                            Level level = Main.s_plugin.getServer().getWorldByName(Helper.pathToDir(levelPath.toString()));
//                            if (level != null) {
//                                Log.debug(String.format("Unloading level: %s", levelPath));
//                                level.unload();
//                            }
//                        }
//                    }
//                }
//                catch (Exception e) {
//                    Log.exception(e, String.format("Exception while fetching level for session key: %s", prevSessionKey));
//                }
//
//                if (dstPath.exists()) {
//                    /// If we can't delete a certain directory (because its in use or whatever), then we keep it
//                    /// in the new array as well
//                    if (!Helper.deleteDirectory(dstPath))
//                        _prevSessionKeys.add(prevSessionKey);
//                }
//            }
//        }
//
//        String sessionKey = String.valueOf((new Date()).getTime());
//        _prevSessionKeys.add(sessionKey);
//
//        return sessionKey;
//        if (_sessionKey == null || _sessionKey.isEmpty()) {
//            _sessionKey = String.valueOf((new Date()).getTime());
//        }
//
//        return _sessionKey;

        return String.valueOf((new Date()).getTime());
    }

    public int getAge() {
        if (Overrides.hasOverride("age")) {
            return Integer.parseInt(Overrides.getOverride("age"));
        }

        /// Calculate age
        var current = Helper.getLocalDate(new Date());
        int years = (int) ChronoUnit.YEARS.between(current, Helper.getLocalDate(dateOfBirth));
        return years;
    }

    public Course getCurrentCourse() {
        return _currentCourse;
    }

    synchronized public Lesson getCurrentLesson() {
        return _currentLesson;
    }

    synchronized public void setCurrentCourse(Course currentCourse, Lesson lesson) {
        _currentCourse = currentCourse;
        _currentLesson = lesson;
    }

    public UserState getState() {
        return _state;
    }

    public boolean isDev() {
        return _id.equals(Helper.s_mongoId);
    }

    private static User s_current = null;
    private static User s_previous = null;

    synchronized public static User getCurrent() {
        return s_current;
    }

    synchronized public static User getPrevious() {
        return s_previous;
    }

//    synchronized public static CmsApi getCurrentUserAPI() {
//        return s_current.getAPI();
//    }

    static User initUser(CmsApi api) throws Exception {
        return initUser(api, "");
    }

    static User initUser(CmsApi api, String userId) throws Exception {
        if (s_current != null)
            return s_current;

        String currentUserJson;

        if (userId != null && !userId.isEmpty())
            currentUserJson = api.get(String.format("users/%s", userId), null);
        else
            currentUserJson = api.get("users/me", null);

        if (currentUserJson == null || currentUserJson.isEmpty()) {
            Log.error(String.format("Unable to get current user. User ID: %s [JWT: %s]", userId, api.getJWT()));
            return null;
        }

        Gson gson = new Gson();
        Log.debug(currentUserJson);

        User user = gson.fromJson(currentUserJson, User.class);
        user._api = api;

        try {
            user.getProfile();
            user._state = UserState.load(user);
            user._state.loadInventory(user);
        }
        catch (IOException e) {
            Log.exception(e, "Error loading user state");
        }

        return user;
    }
    static User initUser(String jwt) throws Exception {
        if (s_current != null)
            return s_current;

        if (jwt == null || jwt.isEmpty())
            throw new Exception("Trying to initialise a user with an invalid or empty JWT token!");

        CmsApi api = new CmsApi(jwt);
        return initUser(api);
    }

    public static User connect(CmsApi api, String userId) throws Exception {
        User currentUser = initUser(api, userId);
        Log.debug(String.format("Current User Id: %s", Objects.requireNonNull(currentUser)._id));

        setCurrentUser(currentUser);

        return currentUser;
    }

    public static User connect(String username, String password, boolean fallbackToDev) throws Exception {
        if (!Helper.isDev())
            throw new Exception("Login using username and password is only allowed on dev machines. Production access denied!");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            if (Helper.isDev()) {
                if (fallbackToDev) {
                    User currentUser = User.connectDev();
                    Log.debug(String.format("Current User Id: %s", currentUser._id));
                }
                return null;
            }
            else {
                Log.error("Invalid JWT token specified and no username/password was given either. Aborting GameSchool plugin load ...");
            }
        }
        else {
            if (Helper.isDev()) {
                Log.debug(String.format("Logging in user: %s (****)", username));
                User loggedInUser = User.login(username, password, true, new CmsApi());
                Log.debug(String.format("User logged in: %s [JWT: %s]", loggedInUser._id, loggedInUser._api.getJWT()));
            }
        }

        return s_current;
    }

    public static User connectDev() {
        if (!Helper.isDev())
            throw new RuntimeException("Only allowed in dev environment mode!");

        if (s_current != null && s_current.isDev())
            return s_current;

        setCurrentUser(null);

        try {
            String baseDir = Main.s_plugin.getServer().getDataPath();
            User user = (new Gson()).fromJson(Files.readString(Paths.get(baseDir, Helper.s_devDirName, "user.json")), User.class);

            /// Load the profile
            user._profile = (new Gson()).fromJson(Files.readString(Paths.get(baseDir, "dev", "child_profile.json")), ChildProfile.class);

            setCurrentUser(user);

            try {
                user._state = UserState.load(user);
                user._state.loadInventory(user);
            }
            catch (IOException e) {
                Log.exception(e, "Error loading user state");
            }

            return user;
        }
        catch (IOException e) {
            Log.exception(e, "Error while trying to load DEV user");
        }

        return null;
    }

    synchronized public static void disconnect(boolean kicked, Date lastUpdateTimestamp) {
        if (s_current == null)
            return;

        s_current._state.save(s_current);
        s_current._state.saveInventory(s_current);

        /// Make sure we end all the activities when the user disconnects
        Activity.endAll(lastUpdateTimestamp);

        if (InteractionHandler.getCurrent() != null)
            InteractionHandler.getCurrent().resetHandlerState(null);

        Lesson currentLesson = s_current.getCurrentLesson();
        if (currentLesson != null)
            currentLesson.exit();

        setCurrentUser(null);
    }

    public <T_Item extends Item> T_Item addItemToInventory_CheckExisting(Player p, T_Item newItem) {
        return addItemToInventory(p, newItem, false, true);
    }

    public <T_Item extends Item> T_Item addItemToInventory_ReplaceExisting(Player p, T_Item newItem) {
        return addItemToInventory(p, newItem, true, false);
    }

    public <T_Item extends Item> T_Item addItemToInventory(Player p, T_Item newItem) {
        return addItemToInventory_CheckExisting(p, newItem);
    }

    public <T_Item extends Item> T_Item addItemToInventory(Player p, T_Item newItem, boolean replaceExisting, boolean checkExisting) {
        if (replaceExisting)
            removeItemFromInventory(p, newItem);
        else if (checkExisting) {
            var existingItem = getExistingItemInInventory(p, newItem);
            if (existingItem != null)
                return (T_Item)existingItem;
        }

        p.getInventory().addItem(newItem);

        return newItem;
    }

    public Item removeItemFromInventory(Player p, Item item) {
        PlayerInventory inventory = p.getInventory();
        int size = inventory.getHotbarSize();
        String itemName = item.getName();
        String itemCustomName = item.getCustomName();

        for (var ii = 0; ii < size; ii++) {
            Item existingItem = inventory.getItem(ii);
            if (existingItem != null && (existingItem.getName().equals(itemName) || existingItem.getCustomName().equals(itemCustomName)))
                inventory.remove(existingItem);
        }

        return null;
    }

    public Item getExistingItemInInventory(Player p, Item item) {
        PlayerInventory inventory = p.getInventory();
        int size = inventory.getHotbarSize();
        String itemName = item.getName();
        String itemCustomName = item.getCustomName();

        for (var ii = 0; ii < size; ii++) {
            Item existingItem = inventory.getItem(ii);
            if (existingItem != null && (existingItem.getName().equals(itemName) || existingItem.getCustomName().equals(itemCustomName)))
                return existingItem;
        }

        return null;
    }

    synchronized public static <T_CmsApi extends CmsApi> User login(String username, String password, boolean setCurrent, CmsApi refAPI) throws RuntimeException {
        Map<String, String> args = new HashMap<String, String>();
        args.put("identifier", username);
        args.put("password", password);

        String loginJson = CmsApi.s_public.post("auth/local", new String[][] {
                {"identifier", username},
                {"password", password}
        });

        Log.info(String.format("User login: %s response => %s", username, loginJson));

        Gson gson = new Gson();

        JsonObject object = JsonParser.parseString(loginJson).getAsJsonObject();
        String jwt = object.get("jwt").toString().replace("\"", "");
        User user = null;

        try {
            String userJson = object.get("user").toString();
            Log.debug(userJson);
            user = gson.fromJson(userJson, User.class);

            Constructor<?>[] ctors = refAPI.getClass().getConstructors();
            Constructor<?> ctor = null;

            for (int cti = 0; cti < ctors.length && ctor == null; cti++) {
                var ct = ctors[cti];
                var paramTypes = ct.getParameterTypes();
                if (paramTypes.length == 1) {
                    var paramClass = paramTypes[0];
                    if (paramClass.isAssignableFrom(String.class))
                        ctor = ct;
                }
            }

            if (ctor != null) {
                user._api = (CmsApi)ctor.newInstance(jwt);
            }

            if (!user.isMCServer()) {
                user.getProfile();
                user._state = UserState.load(user);
                user._state.loadInventory(user);
            }
        }
        catch (IOException e) {
            Log.exception(e, "Error loading user state");
        }
        catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            Log.exception(e, "Error logging the user in!");
        }
        finally {
            if (setCurrent)
                setCurrentUser(user);
        }

        return user;
    }
}
