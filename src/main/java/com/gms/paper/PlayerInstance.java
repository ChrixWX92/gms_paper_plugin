package com.gms.paper;

import com.gms.paper.data.ChildProfile;
import com.gms.paper.data.User;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerInstance {
    private static PlayerInstance s_owner;
    private static HashMap<String, PlayerInstance> s_players = new HashMap<>();

    Player _player;
    User _gmsUser;
    ChildProfile _profile;
    HashMap<String, long[]> questionTimes = new HashMap<String, long[]>();

    public static void addPlayer(PlayerInstance pis, boolean isOwner) {
        s_players.put(pis._player.getName(), pis);
        s_players.put(pis._player.getNameTag(), pis);
        s_players.put(pis._player.getDisplayName(), pis);

        if (isOwner)
            s_owner = pis;
    }

    public static PlayerInstance getOwner() { return s_owner; }
    public static PlayerInstance getPlayer(String key) {
        return s_players.get(key);
    }

    public Player getPlayer() { return _player; }

    public PlayerInstance(Player player, User gmsUser) {
        _player = player;
        _gmsUser = gmsUser;
        _profile = gmsUser.getProfile();

        /// TODO: Add support for a default profile for DEV mode
    }

    public ChildProfile getProfile() { return _profile; }

//    public void increaseTickets(int by) {
//        this.tickets += by;
//        Backend.setTickets(this.id, this.tickets);
//    }
//
//    public boolean purchaseWithTickets(int amount) {
//        if (this.tickets - amount >= 0) {
//            this.tickets -= amount;
//            Backend.setTickets(this.id, this.tickets);
//            return true;
//        }
//        else {
//            return false;
//        }
//    }
}
