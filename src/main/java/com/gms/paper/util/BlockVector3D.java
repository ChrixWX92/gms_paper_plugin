package com.gms.paper.util;

import org.bukkit.block.BlockFace;

public class BlockVector3D implements Cloneable {
    public int x;
    public int y;
    public int z;

    public BlockVector3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockVector3D() {
    }

    public BlockVector3D setComponents(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public BlockVector3D setX(int x) {
        this.x = x;
        return this;
    }

    public BlockVector3D setY(int y) {
        this.y = y;
        return this;
    }

    public BlockVector3D setZ(int z) {
        this.z = z;
        return this;
    }

    public Vector3D add(double x) {
        return this.add(x, 0.0D, 0.0D);
    }

    public Vector3D add(double x, double y) {
        return this.add(x, y, 0.0D);
    }

    public Vector3D add(double x, double y, double z) {
        return new Vector3D((double)this.x + x, (double)this.y + y, (double)this.z + z);
    }

    public Vector3D add(Vector3D x) {
        return new Vector3D((double)this.x + x.getX(), (double)this.y + x.getY(), (double)this.z + x.getZ());
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

    public BlockVector3D add(int x) {
        return this.add(x, 0, 0);
    }

    public BlockVector3D add(int x, int y) {
        return this.add(x, y, 0);
    }

    public BlockVector3D add(int x, int y, int z) {
        return new BlockVector3D(this.x + x, this.y + y, this.z + z);
    }

    public BlockVector3D add(BlockVector3D x) {
        return new BlockVector3D(this.x + x.getX(), this.y + x.getY(), this.z + x.getZ());
    }

    public BlockVector3D subtract() {
        return this.subtract(0, 0, 0);
    }

    public BlockVector3D subtract(int x) {
        return this.subtract(x, 0, 0);
    }

    public BlockVector3D subtract(int x, int y) {
        return this.subtract(x, y, 0);
    }

    public BlockVector3D subtract(int x, int y, int z) {
        return this.add(-x, -y, -z);
    }

    public BlockVector3D subtract(BlockVector3D x) {
        return this.add(-x.getX(), -x.getY(), -x.getZ());
    }

    public BlockVector3D multiply(int number) {
        return new BlockVector3D(this.x * number, this.y * number, this.z * number);
    }

    public BlockVector3D divide(int number) {
        return new BlockVector3D(this.x / number, this.y / number, this.z / number);
    }

    public BlockVector3D getSide(BlockFace face) {
        return this.getSide(face, 1);
    }

    public BlockVector3D getSide(BlockFace face, int step) {
        return new BlockVector3D(this.getX() + face.getXOffset() * step, this.getY() + face.getYOffset() * step, this.getZ() + face.getZOffset() * step);
    }

    public BlockVector3D up() {
        return this.up(1);
    }

    public BlockVector3D up(int step) {
        return this.getSide(BlockFace.UP, step);
    }

    public BlockVector3D down() {
        return this.down(1);
    }

    public BlockVector3D down(int step) {
        return this.getSide(BlockFace.DOWN, step);
    }

    public BlockVector3D north() {
        return this.north(1);
    }

    public BlockVector3D north(int step) {
        return this.getSide(BlockFace.NORTH, step);
    }

    public BlockVector3D south() {
        return this.south(1);
    }

    public BlockVector3D south(int step) {
        return this.getSide(BlockFace.SOUTH, step);
    }

    public BlockVector3D east() {
        return this.east(1);
    }

    public BlockVector3D east(int step) {
        return this.getSide(BlockFace.EAST, step);
    }

    public BlockVector3D west() {
        return this.west(1);
    }

    public BlockVector3D west(int step) {
        return this.getSide(BlockFace.WEST, step);
    }

    public double distance(Vector3D pos) {
        return Math.sqrt(this.distanceSquared(pos));
    }

    public double distance(BlockVector3D pos) {
        return Math.sqrt(this.distanceSquared(pos));
    }

    public double distanceSquared(Vector3D pos) {
        return this.distanceSquared(pos.x, pos.y, pos.z);
    }

    public double distanceSquared(BlockVector3D pos) {
        return this.distanceSquared((double)pos.x, (double)pos.y, (double)pos.z);
    }

    public double distanceSquared(double x, double y, double z) {
        return Math.pow((double)this.x - x, 2.0D) + Math.pow((double)this.y - y, 2.0D) + Math.pow((double)this.z - z, 2.0D);
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (!(o instanceof BlockVector3D)) {
            return false;
        } else {
            BlockVector3D that = (BlockVector3D)o;
            return this.x == that.x && this.y == that.y && this.z == that.z;
        }
    }

    public final int hashCode() {
        return this.x ^ this.z << 12 ^ this.y << 24;
    }

    public String toString() {
        return "BlockPosition(level=,x=" + this.x + ",y=" + this.y + ",z=" + this.z + ")";
    }

    public BlockVector3D clone() {
        try {
            return (BlockVector3D)super.clone();
        } catch (CloneNotSupportedException var2) {
            return null;
        }
    }

    public Vector3D asVector3D() {
        return new Vector3D((double)this.x, (double)this.y, (double)this.z);
    }

    public Vector3Df asVector3Df() {
        return new Vector3Df((float)this.x, (float)this.y, (float)this.z);
    }
}
