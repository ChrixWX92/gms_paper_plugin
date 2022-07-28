package com.gms.paper;

import com.gms.paper.data.CmsApi;
import com.gms.paper.data.User;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class MyServer {
    public String _id;
    public String ipAddress;
    public Date startTime;
    public Date updateTime;
    public String status;
    public Date lastUserLogin;
    public Date lastUserUpdate;
    public String minecraftId_Target;
    public String minecraftId_Current;
    public String awsArn;
    public String serverUrl;
    public JsonObject awsTaskData;
    public Date requestedTime;
    public JsonObject metadata;
    public String jwt;
    public JsonObject errorDetails;
    public int numFailed;
    public String playerId;
    public String region;
//    public ISODate spawnInitTime;
    public Date spawnTime;

    private boolean _resetTarget = false;

    public static long s_maxWaitTimeMS = 60 * 1000;
    private transient Date _waitStartTime = new Date();
    private transient boolean _isConnected = false;
    private transient User _mcUser = null;
    private transient User _loggedInUser = null;

    public MyServer(String ipAddress) {
        _waitStartTime = new Date();
    }

    public String debugId() {
        return String.format("%s@%s", _id.toString(), ipAddress);
    }

    private void clearMCUser() {
        _mcUser = null;
    }

    private static User loginServerUser() {
        String mcUsername = Helper.getEnv("GMS_MC_USERNAME", true);
        String mcPassword = Helper.getEnv("GMS_MC_PASSWORD", true);

        return User.login(mcUsername, mcPassword, false);
    }

    private User loginMCUser() {
        if (_mcUser == null)
            _mcUser = loginServerUser();

        return _mcUser;
    }

    private User getMCUser() {
        if (_mcUser == null)
            return loginMCUser();
        return _mcUser;
    }

    public static MyServer loadForIp(String ipAddress) {
        try {
             User mcUser = loginServerUser();
             if (mcUser != null) {
                String docJson = mcUser.getAPI().get(String.format("my-servers/%s", ipAddress), null);

                if (docJson == null || docJson.isEmpty())
                    return null;

                Log.debug(String.format("My Servers Response: %s", docJson));

                Gson gson = new Gson();
                MyServer serverInfo = gson.fromJson(docJson, MyServer.class);
                Log.debug(docJson);

                /// Initial user can come from the object that we've already loaded
                serverInfo._mcUser = mcUser;
                serverInfo._waitStartTime = new Date();

                return serverInfo;
             }
        }
        catch (Exception e) {
            Log.exception(e, String.format("Error loading document for ip address: %s", ipAddress));

            /// OK, we just need to exit
            if (Helper.isProd()) {
                System.exit(MCServer.s_noDBRecord);
            }
        }

        return null;
    }

    public void updateStatus(String status, String minecraftId) {
        updateStatus(status, minecraftId, false);
    }

    public void updateStatus(String status, String minecraftId, boolean resetTarget) {
        /// Don't do anything if we've reset the target already
        if (_resetTarget)
            return;

        if (resetTarget) {
            minecraftId = "";
            _resetTarget = true;
        }

        Log.debug(String.format("MyServer [%s/%s] status change to => %s [Target minecraft id: %s]", _id, minecraftId_Target, status, minecraftId));

        getAPI().put("my-servers/update-status", new String[][] {
                { "id", _id },
                { "status", status },
                { "minecraftId", minecraftId },
                { "playerId", minecraftId },
                { "resetTarget", resetTarget ? "true" : "false" }
        });
    }

    public synchronized void playerConnected(String minecraftId) throws Exception {
        Log.debug(String.format("Player connected with minecraft ID: %s [JWT: %s]", minecraftId, jwt));
        _loggedInUser = User.connect(jwt);

        updateStatus("playing", minecraftId);
        _isConnected = true;
        _resetTarget = false;
    }

    public synchronized void playerDisconnected(boolean kicked, Date lastMoveTimestamp) {
        updateStatus("join_waiting", minecraftId_Target);

        _waitStartTime = new Date();
        _isConnected = false;

        User.disconnect(kicked, lastMoveTimestamp);
        _loggedInUser = null;
    }

    public boolean isConnected() {
        return _isConnected;
    }

    public void repurposeServer() {
        playerId = "";
        updateStatus("idle", "", true);
    }

    public boolean checkWaitTimeout() {
        if (isWaiting()) {
            long interval = (new Date()).getTime() - _waitStartTime.getTime();
            if (interval > s_maxWaitTimeMS) {
                return true;
            }
        }

        return false;
    }

    public boolean isWaiting() {
        return status.equals("join_waiting");
    }

    public boolean isIdle() {
        return status.equals("idle");
    }

    private CmsApi getAPI() {
        var mcUser = getMCUser();

        if (mcUser == null)
            return null;
        
        var api = getMCUser().getAPI();

        if (_loggedInUser != null)
            api = _loggedInUser.getAPI();

        return api;
    }

    private static Path getMyServerPath() {
        return Paths.get(Helper.getDrvRoot().toString(), Helper.s_myServerDirName, Helper.s_myServerHeartbeatDirName);
    }

    private void checkHeartbeat() {
        try {
            Log.debug("Check heartbeat file ...");
            File requestFile = Paths.get(getMyServerPath().toString(), String.format("%s.heartbeat.request", ipAddress)).toFile();

            if (requestFile.exists()) {
                File responseFile = new File(requestFile.toString().replace(".request", ".response"));
                Log.debug(String.format("Heartbeat file found: %s [Response: %s]", requestFile.toString(), responseFile.toString()));

                /// Delete the existing file
                if (responseFile.exists())
                    responseFile.delete();

                /// Then create a new one
                responseFile.createNewFile();
            }
            else {
                Log.debug("No heartbeat requests found!");
            }
        }
        catch (Exception e) {
            Log.exception(e, String.format("Error performing heartbeat request/response"));
        }
    }

    public synchronized boolean tick() {
        /// First of all check whether a heartbeat request has been made on the file-system.
        /// This can happen if the nukkitserver has temporarily lost contact with the
        /// gms_server, that its using for heartbeat
        checkHeartbeat();

        String id = "";
        var mcUser = getMCUser();

        if (mcUser != null) {
            var api = mcUser.getAPI();

            if (_loggedInUser != null) {
    //            api = _loggedInUser.getAPI();
                id = _loggedInUser._id;
            }

            Log.debug("Ticking server in the DB!");
            var responseData = api.put(String.format("my-servers/tick/%s", ipAddress), new String[][]
                    {
                            { "currentUserId", id }
                    });

            if (responseData == null || responseData.isEmpty()) {
                int lastStatus = api.getLastFailureStatus();
                if (lastStatus == 401 || lastStatus == 403) {
                    clearMCUser();
                }

                return false;
            }

            return true;
        }

        return false;
    }
}
