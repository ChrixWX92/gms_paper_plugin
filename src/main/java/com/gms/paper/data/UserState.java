package com.gms.paper.data;

import com.gms.paper.Main;
import com.gms.paper.interact.InteractionHandler;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import com.gms.paper.util.Vector3D;
import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UserState {
    public static final String s_stateFilename = "state.json";

    private String worldId;
    private Vector3D pos = new Vector3D(Helper.s_mainLobbySpawnPos.x, Helper.s_mainLobbySpawnPos.y, Helper.s_mainLobbySpawnPos.z);
    public float headYaw = 0;
    public String progressId = "";

    public static Path getPath(User user) {
        return Paths.get(user.getProfile().getProfileDir().toString(), s_stateFilename);
    }

    public Vector3D getPos() { return pos; }

    public void setProgressId(String progressId) {
        this.progressId = progressId;
    }

    public String getWorldId() {
        return worldId;
    }

    public void save(User user) {

        String filename = getPath(user).toString();
        File file = new File(filename);
        Log.debug(String.format("Saving user state: %s", getPath(user).toString()));

        try {
            Gson gson = new Gson();
            String json = gson.toJson(this);
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(json);

            writer.close();
        }
        catch (IOException e) {
            Log.exception(e, String.format("Error saving user profile to path: %s", getPath(user).toString()));
            e.printStackTrace();
        }
    }

    private static UserState initDefault() {
        UserState state = new UserState();
        state.worldId = Helper.s_mainWorld;
        return state;
    }

    public static UserState load(User user) throws IOException {
        File file = new File(getPath(user).toString());
        if (!file.exists())
            return initDefault();

        Log.debug(String.format("Loading user state: %s", getPath(user).toString()));

        /// Otherwise
        UserState state = (new Gson()).fromJson(Files.readString(Paths.get(getPath(user).toString())), UserState.class);

        if (state == null)
            return initDefault();

        return state;
    }

    public void loadInventory(User user) {
        File srcPath = new File(Paths.get(user.getProfile().getProfileDir().toString(), "players").toString());
        File dstPath = new File(Paths.get(Main.s_plugin.getDataFolder().getPath(), "players").toString());

        Log.debug(String.format("Loading inventory: %s => %s", srcPath, dstPath));

        if (!srcPath.exists())
            return;

        Helper.copyFolder(srcPath, dstPath);
    }

    public void saveInventory(User user) {
        File srcPath = new File(Paths.get(Main.s_plugin.getDataFolder().getPath(), "players").toString());
        File dstPath = new File(Paths.get(user.getProfile().getProfileDir().toString(), "players").toString());

        Log.debug(String.format("Saving inventory: %s => %s", srcPath, dstPath));

        if (!srcPath.exists())
            return;

        Helper.copyFolder(srcPath, dstPath);
    }

    public void updateWorld(String world) {
        this.worldId = world;

        if(worldId.contains("\\") || worldId.contains("//")){
            File file = new File(worldId);
            worldId = file.getName();
        }

        Main.s_plugin.getConfig();
    }

    public void updatePos(Vector3D pos, float headYaw) {
        if(!InteractionHandler.isPendingTeleport()) {
            this.pos = pos;
            this.headYaw = headYaw;

            Log.debug(String.format("CHECKPOINT: position saved [Position : %s]", pos));
        }
    }
}
