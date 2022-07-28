package com.gms.paper;

import com.gms.paper.data.CmsApi;
import com.gms.paper.data.GamePosition;
import com.gms.paper.data.Lesson;
import com.gms.paper.data.User;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import com.gms.paper.util.Vector3D;

import java.util.Date;

public class MCServer extends Thread {
    public static final int s_initFailedErrorCode = 6;
    public static final int s_noDBRecord = 7;

    private static final long s_tickIntervalMS = 15 * 1000;
    private static final long s_playerTimeoutMS = 60 * 30 * 1000;
    private static final int s_maxInitTries = 5;

    MyServer _serverInfo;
    String _ipAddress;
    boolean _running = true;

    Date _moveTime = null;

    GamePosition _playerPos = new GamePosition(null, new Vector3D(0, 0, 0), true);
    float _playerYaw = 0;
    float _playerPitch = 0;
    boolean _playerConnected = false;
    int _numInitTries = 0;

    MCServer() throws Exception {
        _ipAddress = Helper.getPublicIPAddress();
        Log.setIPAddress(_ipAddress);
        Log.debug(String.format("[MCServer] Got IP Address: %s", _ipAddress));
    }

    public static CmsApi getAPI() {
        if (Main.s_mcServer != null && Main.s_mcServer._serverInfo != null)
            return Main.s_mcServer._serverInfo.getAPI();

        if (Helper.isProd())
            return null;

        return User.getCurrent() != null ? User.getCurrent().getAPI() : null;
    }

    public MyServer getServerInfo() {
        return _serverInfo;
    }

    private void initMyServer() throws Exception {
        if (Helper.isDev()) {
            return;
        }

        Log.debug(String.format("Initialising my-server [Try: %d] ...", _numInitTries + 1));
        _serverInfo = MyServer.loadForIp(_ipAddress);

        if (_serverInfo == null) {
            _numInitTries++;

            if (_numInitTries > s_maxInitTries) {
                Log.warn(String.format("Unable to load my-server for IP address: %s. Disabling MCServer! [Tried: %d, Max tries: %d]", _ipAddress, _numInitTries, s_maxInitTries));

                if (Helper.isProd()) {
                    Log.error("MCServer running in production mode couldn't find entry in the DB. Exiting ...");
                    System.exit(s_noDBRecord);
                }
            }
        }
        else {
            /// Reset the tries
            _numInitTries = 0;
        }
    }

    public boolean canJoin(String minecraftId) {
        if (Helper.isDev())
            return true;

        if (_serverInfo == null) {
            return !Helper.isProd();
        }

        return _serverInfo.minecraftId_Target.equalsIgnoreCase(minecraftId);
    }

    public void playerConnected(String minecraftId) throws Exception {
        _playerConnected = true;

        if (Helper.isDev()) {
            if (User.getCurrent() == null) {
                User.connectDev();
            }

            /// Nothing to do over here
            return;
        }

        try {
            /// just reload the server info, on player connected
            _serverInfo = null;

            Log.debug("Player connected. Reloading server info to get updated JWT tokens etc ...");
            _serverInfo = MyServer.loadForIp(_ipAddress);

            if (_serverInfo.user == null || _serverInfo.user.isEmpty()) {
                Log.error(String.format("No user token found for server id: %s. Putting the server back into idle ...", _serverInfo._id));
                _serverInfo.updateStatus("idle", "", true);
                _serverInfo = null;

                throw new Exception("No user found!");
            }

            Log.debug(String.format("Server info loaded for user: %s", _serverInfo.user));

            _serverInfo.playerConnected(minecraftId);
            _moveTime = new Date();
        }
        catch (Exception e) {
            Log.exception(e, "playerJoined error.");
        }
    }

    public void playerDisconnected(boolean kicked) {
        _playerConnected = false;
        _moveTime = null;

        if (Helper.isDev()) {
            User.disconnect(kicked, new Date());
            return;
        }

        if (_serverInfo == null) {
            User.disconnect(kicked, new Date());
            return;
        }

        try {
            /// This will, in turn call User disconnect
            _serverInfo.playerDisconnected(kicked, new Date());
        }
        catch (Exception e) {
            Log.error("playerJoined error.");
        }
    }

    void checkPlayerTimeout() {
        if (Helper.isDev())
            return;

        if (_serverInfo == null)
            return;

        /// There is no current player ... we just ignore
        if (_serverInfo.minecraftId_Current == null || _serverInfo.minecraftId_Current.isEmpty())
            return;

        if (_serverInfo.status.equalsIgnoreCase("idle"))
            return;

        if (_moveTime != null) {
            long interval = (new Date()).getTime() - _moveTime.getTime();

            if (interval > s_playerTimeoutMS) {
                Log.warn(String.format("Player last moved more than %d seconds ago. Timing out ...", (int)(interval * 0.001)));
                PlayerInstance pis = PlayerInstance.getOwner();
                pis.getPlayer().kick(PlayerKickEvent.Reason.LOGIN_TIMEOUT);

                _serverInfo.updateStatus("idle", "", true);
            }
        }
    }

    public synchronized void tick() {
        if (Helper.isDev())
            return;

        try {
            if (_serverInfo == null) {
                initMyServer();
                return;
            }

            if (_serverInfo.tick()) {
                User user = User.getCurrent();

                if (user != null) {
                    Lesson lesson = user.getCurrentLesson();
                    if (lesson != null) {
                        /// Tick the lesson activity!
                        lesson.tick();
                    }
                }

                if (_serverInfo.isConnected()) {
                    checkPlayerTimeout();
                }
                else {
                    /// Repurpose the server if we've been waiting for it for a while
//                    if (_serverInfo.checkWaitTimeout()) {
//                        _serverInfo.repurposeServer();
//                    }

                    /// If server is idle, then we need to keep reading the DB again
                    /// until the server gets repurposed by the daemon
                    //                else if (_serverInfo.isIdle()) {
                    //                    _serverInfo = MyServer.loadForIp(_ipAddress);
                    //                }
                }
            }
            else {
                /// If the tick fails for whatever reason, then we need to re-initialise the
                /// server. This can happen if the JWT token is lost or the server is taken
                /// away from a particular user

                /// We just clear the server info. This will get reinitialised in the next
                /// tick cycle
                _serverInfo = null;
            }

        }
        catch (Exception e) {
            Log.exception(e, "Error with server tick.");

            /// Reset the server info so that it can be re-initialised
            _serverInfo = null;
        }
    }

    public void updateMove(MovePlayerPacket packet) {
        if (!_playerConnected)
            return;

        /// Do nothing if the DB failed to initiialise
        if (Helper.isDev())
            return;

        _playerPos.setComponents(packet.x, packet.y, packet.z);
        _playerYaw = packet.yaw;
        _playerPitch = packet.pitch;
        _moveTime = new Date();
    }

    @Override
    public void run() {
        while (_running) {
            tick();
            try {
                Thread.sleep(s_tickIntervalMS);
            }
            catch (InterruptedException e) {
                Log.exception(e, "Exception in Thread.sleep!");
            }
        }
    }
}

