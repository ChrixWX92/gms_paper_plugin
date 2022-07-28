package com.gms.paper.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;

public class Vector3D implements Cloneable {
    public double x;
    public double y;
    public double z;

    public Vector3D() {
        this(0.0D, 0.0D, 0.0D);
    }

    public Vector3D(double x) {
        this(x, 0.0D, 0.0D);
    }

    public Vector3D(double x, double y) {
        this(x, y, 0.0D);
    }

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public Vector3D setX(double x) {
        this.x = x;
        return this;
    }

    public Vector3D setY(double y) {
        this.y = y;
        return this;
    }

    public Vector3D setZ(double z) {
        this.z = z;
        return this;
    }

    public int getFloorX() {
        return (int) Math.floor(this.x);
    }

    public int getFloorY() {
        return (int) Math.floor(this.y);
    }

    public int getFloorZ() {
        return (int) Math.floor(this.z);
    }

    public int getChunkX() {
        return this.getFloorX() >> 4;
    }

    public int getChunkZ() {
        return this.getFloorZ() >> 4;
    }

    public double getRight() {
        return this.x;
    }

    public double getUp() {
        return this.y;
    }

    public double getForward() {
        return this.z;
    }

    public double getSouth() {
        return this.x;
    }

    public double getWest() {
        return this.z;
    }

    public Vector3D add(double x) {
        return this.add(x, 0.0D, 0.0D);
    }

    public Vector3D add(double x, double y) {
        return this.add(x, y, 0.0D);
    }

    public Vector3D add(double x, double y, double z) {
        return new Vector3D(this.x + x, this.y + y, this.z + z);
    }

    public Vector3D add(Vector3D x) {
        return new Vector3D(this.x + x.getX(), this.y + x.getY(), this.z + x.getZ());
    }

    public Vector3D subtract() {
        return this.subtract(0.0D, 0.0D, 0.0D);
    }

    public Vector3D subtract(double x) {
        return this.subtract(x, 0.0D, 0.0D);
    }

    public Vector3D subtract(double x, double y) {
        return this.subtract(x, y, 0.0D);
    }

    public Vector3D subtract(double x, double y, double z) {
        return this.add(-x, -y, -z);
    }

    public Vector3D subtract(Vector3D x) {
        return this.add(-x.getX(), -x.getY(), -x.getZ());
    }

    public Vector3D multiply(double number) {
        return new Vector3D(this.x * number, this.y * number, this.z * number);
    }

    public Vector3D divide(double number) {
        return new Vector3D(this.x / number, this.y / number, this.z / number);
    }

    public Vector3D ceil() {
        return new Vector3D((double) ((int) Math.ceil(this.x)), (double) ((int) Math.ceil(this.y)), (double) ((int) Math.ceil(this.z)));
    }

    public Vector3D floor() {
        return new Vector3D((double) this.getFloorX(), (double) this.getFloorY(), (double) this.getFloorZ());
    }

    public Vector3D round() {
        return new Vector3D((double) Math.round(this.x), (double) Math.round(this.y), (double) Math.round(this.z));
    }

    public Vector3D abs() {
        return new Vector3D((double) ((int) Math.abs(this.x)), (double) ((int) Math.abs(this.y)), (double) ((int) Math.abs(this.z)));
    }

    public Vector3D getSide(BlockFace face) {
        return this.getSide(face, 1);
    }

    public Vector3D getSide(BlockFace face, int step) {
        return new Vector3D(this.getX() + (double) (face.getXOffset() * step), this.getY() + (double) (face.getYOffset() * step), this.getZ() + (double) (face.getZOffset() * step));
    }

    public Vector3D getSideVec(BlockFace face) {
        return new Vector3D(this.getX() + (double) face.getXOffset(), this.getY() + (double) face.getYOffset(), this.getZ() + (double) face.getZOffset());
    }

    public Vector3D up() {
        return this.up(1);
    }

    public Vector3D up(int step) {
        return this.getSide(BlockFace.UP, step);
    }

    public Vector3D down() {
        return this.down(1);
    }

    public Vector3D down(int step) {
        return this.getSide(BlockFace.DOWN, step);
    }

    public Vector3D north() {
        return this.north(1);
    }

    public Vector3D north(int step) {
        return this.getSide(BlockFace.NORTH, step);
    }

    public Vector3D south() {
        return this.south(1);
    }

    public Vector3D south(int step) {
        return this.getSide(BlockFace.SOUTH, step);
    }

    public Vector3D east() {
        return this.east(1);
    }

    public Vector3D east(int step) {
        return this.getSide(BlockFace.EAST, step);
    }

    public Vector3D west() {
        return this.west(1);
    }

    public Vector3D west(int step) {
        return this.getSide(BlockFace.WEST, step);
    }

    public double distance(Vector3D pos) {
        return Math.sqrt(this.distanceSquared(pos));
    }

    public double distanceSquared(Vector3D pos) {
        return Math.pow(this.x - pos.x, 2.0D) + Math.pow(this.y - pos.y, 2.0D) + Math.pow(this.z - pos.z, 2.0D);
    }

    public double maxPlainDistance() {
        return this.maxPlainDistance(0.0D, 0.0D);
    }

    public double maxPlainDistance(double x) {
        return this.maxPlainDistance(x, 0.0D);
    }

    public double maxPlainDistance(double x, double z) {
        return Math.max(Math.abs(this.x - x), Math.abs(this.z - z));
    }

    public double maxPlainDistance(Vector2 vector) {
        return this.maxPlainDistance(vector.x, vector.y);
    }

    public double maxPlainDistance(Vector3D x) {
        return this.maxPlainDistance(x.x, x.z);
    }

    public double length() {
        return Math.sqrt(this.lengthSquared());
    }

    public double lengthSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public Vector3D normalize() {
        double len = this.lengthSquared();
        return len > 0.0D ? this.divide(Math.sqrt(len)) : new Vector3D(0.0D, 0.0D, 0.0D);
    }

    public double dot(Vector3D v) {
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    public Vector3D cross(Vector3D v) {
        return new Vector3D(this.y * v.z - this.z * v.y, this.z * v.x - this.x * v.z, this.x * v.y - this.y * v.x);
    }

    public Angle angleBetween(Vector3D v) {
        return Angle.fromRadian(Math.acos(Math.min(Math.max(this.normalize().dot(v.normalize()), -1.0D), 1.0D)));
    }

    public Vector3D getIntermediateWithXValue(Vector3D v, double x) {
        double xDiff = v.x - this.x;
        double yDiff = v.y - this.y;
        double zDiff = v.z - this.z;
        if (xDiff * xDiff < 1.0E-7D) {
            return null;
        } else {
            double f = (x - this.x) / xDiff;
            return !(f < 0.0D) && !(f > 1.0D) ? new Vector3D(this.x + xDiff * f, this.y + yDiff * f, this.z + zDiff * f) : null;
        }
    }

    public Vector3D getIntermediateWithYValue(Vector3D v, double y) {
        double xDiff = v.x - this.x;
        double yDiff = v.y - this.y;
        double zDiff = v.z - this.z;
        if (yDiff * yDiff < 1.0E-7D) {
            return null;
        } else {
            double f = (y - this.y) / yDiff;
            return !(f < 0.0D) && !(f > 1.0D) ? new Vector3D(this.x + xDiff * f, this.y + yDiff * f, this.z + zDiff * f) : null;
        }
    }

    public Vector3D getIntermediateWithZValue(Vector3D v, double z) {
        double xDiff = v.x - this.x;
        double yDiff = v.y - this.y;
        double zDiff = v.z - this.z;
        if (zDiff * zDiff < 1.0E-7D) {
            return null;
        } else {
            double f = (z - this.z) / zDiff;
            return !(f < 0.0D) && !(f > 1.0D) ? new Vector3D(this.x + xDiff * f, this.y + yDiff * f, this.z + zDiff * f) : null;
        }
    }

    public Vector3D setComponents(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public String toString() {
        return "Vector3D(x=" + this.x + ",y=" + this.y + ",z=" + this.z + ")";
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Vector3D)) {
            return false;
        } else {
            Vector3D other = (Vector3D) obj;
            return this.x == other.x && this.y == other.y && this.z == other.z;
        }
    }

    public int hashCode() {
        return (int) this.x ^ (int) this.z << 12 ^ (int) this.y << 24;
    }

    public int rawHashCode() {
        return super.hashCode();
    }

    public Vector3D clone() {
        try {
            return (Vector3D) super.clone();
        } catch (CloneNotSupportedException var2) {
            return null;
        }
    }

    public Vector3Df asVector3Df() {
        return new Vector3Df((float) this.x, (float) this.y, (float) this.z);
    }

    public BlockVector3D asBlockVector3D() {
        return new BlockVector3D(this.getFloorX(), this.getFloorY(), this.getFloorZ());
    }

    public Location toLocation(World world) {
        return new Location(world, this.x, this.y, this.z);
    }

}
