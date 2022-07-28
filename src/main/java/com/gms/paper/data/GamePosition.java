package com.gms.paper.data;

import com.gms.paper.util.Vector3D;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class GamePosition extends Vector3D {
    public boolean absolute = false;
    transient GamePosition prevPos;
    transient Vector3D relativePos;

    GamePosition() {
    }

    public GamePosition(GamePosition prevPos, Vector3D v, boolean absolute) {
        super(v.x, v.y, v.z);

        relativePos = v;
        this.prevPos = prevPos;
        this.absolute = absolute;

        if (prevPos != null) {
            x += prevPos.x;
            y += prevPos.y;
            z += prevPos.z;
        }
    }

    public GamePosition round() {
        GamePosition copy = this.clone();
        copy.x = Math.round(this.x);
        copy.y = Math.round(this.y);
        copy.z = Math.round(this.z);
        return copy;
    }

    public GamePosition clone() {
        GamePosition clone = new GamePosition();

        clone.x = x;
        clone.y = y;
        clone.z = z;

        clone.absolute = absolute;
        clone.relativePos = new Vector3D(relativePos.x, relativePos.y, relativePos.z);
        clone.prevPos = prevPos;

        return clone;
    }

    @Override
    public String toString() {
        return String.format("%d, %d, %d [world = %s]", (int)x, (int)y, (int)z, absolute ? "true" : "false");
    }

    public GamePosition(GamePosition prevPos, Vector3D v) {
        this(prevPos, v, prevPos == null);
    }

    public GamePosition add(Vector3D v) {
        return new GamePosition(this, v, false);
    }

    public GamePosition add(GamePosition rhs) {
        /// If the RHS is absolute, then we don't do anything
        if (rhs.absolute)
            return rhs;
        return new GamePosition(this, rhs);
    }

    public GamePosition add(int x, int y, int z) {
        return add(new Vector3D(x, y, z));
    }

//    public GamePosition(Vector3D v, boolean absolute) {
//        super(v.x, v.y, v.z);
//        this.prevPos = null;
//        this.absolute = absolute;
//        this.worldPos = this.toLocation();
//    }

//    public Location getWorldPosition() {
//        return worldPos;
//    }

//    private Location toLocation() {
//        return new Location(x, y, z);
//    }

    public GamePosition asAbsolute() {
        return new GamePosition(null, this, true);
    }

    public JsonElement toJson() {
        return (new Gson()).toJsonTree(this);
    }

    public static GamePosition fromJson(String json) {
        return (new Gson()).fromJson(json, GamePosition.class);
    }
}
