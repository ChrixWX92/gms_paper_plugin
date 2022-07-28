package com.gms.paper.util;

public class Vector3Df implements Cloneable {
    public static final int SIDE_DOWN = 0;
    public static final int SIDE_UP = 1;
    public static final int SIDE_NORTH = 2;
    public static final int SIDE_SOUTH = 3;
    public static final int SIDE_WEST = 4;
    public static final int SIDE_EAST = 5;
    public float x;
    public float y;
    public float z;

    public Vector3Df() {
        this(0.0F, 0.0F, 0.0F);
    }

    public Vector3Df(float x) {
        this(x, 0.0F, 0.0F);
    }

    public Vector3Df(float x, float y) {
        this(x, y, 0.0F);
    }

    public Vector3Df(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }

    public Vector3Df setX(float x) {
        this.x = x;
        return this;
    }

    public Vector3Df setY(float y) {
        this.y = y;
        return this;
    }

    public Vector3Df setZ(float z) {
        this.z = z;
        return this;
    }

    public int getFloorX() {
        return NukkitMath.floorFloat(this.x);
    }

    public int getFloorY() {
        return NukkitMath.floorFloat(this.y);
    }

    public int getFloorZ() {
        return NukkitMath.floorFloat(this.z);
    }

    public float getRight() {
        return this.x;
    }

    public float getUp() {
        return this.y;
    }

    public float getForward() {
        return this.z;
    }

    public float getSouth() {
        return this.x;
    }

    public float getWest() {
        return this.z;
    }

    public Vector3Df add(float x) {
        return this.add(x, 0.0F, 0.0F);
    }

    public Vector3Df add(float x, float y) {
        return this.add(x, y, 0.0F);
    }

    public Vector3Df add(float x, float y, float z) {
        return new Vector3Df(this.x + x, this.y + y, this.z + z);
    }

    public Vector3Df add(Vector3Df x) {
        return new Vector3Df(this.x + x.getX(), this.y + x.getY(), this.z + x.getZ());
    }

    public Vector3Df subtract() {
        return this.subtract(0.0F, 0.0F, 0.0F);
    }

    public Vector3Df subtract(float x) {
        return this.subtract(x, 0.0F, 0.0F);
    }

    public Vector3Df subtract(float x, float y) {
        return this.subtract(x, y, 0.0F);
    }

    public Vector3Df subtract(float x, float y, float z) {
        return this.add(-x, -y, -z);
    }

    public Vector3Df subtract(Vector3Df x) {
        return this.add(-x.getX(), -x.getY(), -x.getZ());
    }

    public Vector3Df multiply(float number) {
        return new Vector3Df(this.x * number, this.y * number, this.z * number);
    }

    public Vector3Df divide(float number) {
        return new Vector3Df(this.x / number, this.y / number, this.z / number);
    }

    public Vector3Df ceil() {
        return new Vector3Df((float) ((int) Math.ceil((double) this.x)), (float) ((int) Math.ceil((double) this.y)), (float) ((int) Math.ceil((double) this.z)));
    }

    public Vector3Df floor() {
        return new Vector3Df((float) this.getFloorX(), (float) this.getFloorY(), (float) this.getFloorZ());
    }

    public Vector3Df round() {
        return new Vector3Df((float) Math.round(this.x), (float) Math.round(this.y), (float) Math.round(this.z));
    }

    public Vector3Df abs() {
        return new Vector3Df((float) ((int) Math.abs(this.x)), (float) ((int) Math.abs(this.y)), (float) ((int) Math.abs(this.z)));
    }

    public Vector3Df getSide(int side) {
        return this.getSide(side, 1);
    }

    public Vector3Df getSide(int side, int step) {
        switch (side) {
            case 0:
                return new Vector3Df(this.x, this.y - (float) step, this.z);
            case 1:
                return new Vector3Df(this.x, this.y + (float) step, this.z);
            case 2:
                return new Vector3Df(this.x, this.y, this.z - (float) step);
            case 3:
                return new Vector3Df(this.x, this.y, this.z + (float) step);
            case 4:
                return new Vector3Df(this.x - (float) step, this.y, this.z);
            case 5:
                return new Vector3Df(this.x + (float) step, this.y, this.z);
            default:
                return this;
        }
    }

    public static int getOppositeSide(int side) {
        switch (side) {
            case 0:
                return 1;
            case 1:
                return 0;
            case 2:
                return 3;
            case 3:
                return 2;
            case 4:
                return 5;
            case 5:
                return 4;
            default:
                return -1;
        }
    }

    public double distance(Vector3Df pos) {
        return Math.sqrt(this.distanceSquared(pos));
    }

    public double distanceSquared(Vector3Df pos) {
        return Math.pow((double) (this.x - pos.x), 2.0D) + Math.pow((double) (this.y - pos.y), 2.0D) + Math.pow((double) (this.z - pos.z), 2.0D);
    }

    public float maxPlainDistance() {
        return this.maxPlainDistance(0.0F, 0.0F);
    }

    public float maxPlainDistance(float x) {
        return this.maxPlainDistance(x, 0.0F);
    }

    public float maxPlainDistance(float x, float z) {
        return Math.max(Math.abs(this.x - x), Math.abs(this.z - z));
    }

    public float maxPlainDistance(Vector2f vector) {
        return this.maxPlainDistance(vector.x, vector.y);
    }

    public float maxPlainDistance(Vector3Df x) {
        return this.maxPlainDistance(x.x, x.z);
    }

    public double length() {
        return Math.sqrt((double) this.lengthSquared());
    }

    public float lengthSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public Vector3Df normalize() {
        float len = this.lengthSquared();
        return len > 0.0F ? this.divide((float) Math.sqrt((double) len)) : new Vector3Df(0.0F, 0.0F, 0.0F);
    }

    public float dot(Vector3Df v) {
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    public Vector3Df cross(Vector3Df v) {
        return new Vector3Df(this.y * v.z - this.z * v.y, this.z * v.x - this.x * v.z, this.x * v.y - this.y * v.x);
    }

    public Angle angleBetween(Vector3Df v) {
        return Angle.fromRadian(Math.acos((double) Math.min(Math.max(this.normalize().dot(v.normalize()), -1.0F), 1.0F)));
    }

    public Vector3Df getIntermediateWithXValue(Vector3Df v, float x) {
        float xDiff = v.x - this.x;
        float yDiff = v.y - this.y;
        float zDiff = v.z - this.z;
        if ((double) (xDiff * xDiff) < 1.0E-7D) {
            return null;
        } else {
            float f = (x - this.x) / xDiff;
            return !(f < 0.0F) && !(f > 1.0F) ? new Vector3Df(this.x + xDiff * f, this.y + yDiff * f, this.z + zDiff * f) : null;
        }
    }

    public Vector3Df getIntermediateWithYValue(Vector3Df v, float y) {
        float xDiff = v.x - this.x;
        float yDiff = v.y - this.y;
        float zDiff = v.z - this.z;
        if ((double) (yDiff * yDiff) < 1.0E-7D) {
            return null;
        } else {
            float f = (y - this.y) / yDiff;
            return !(f < 0.0F) && !(f > 1.0F) ? new Vector3Df(this.x + xDiff * f, this.y + yDiff * f, this.z + zDiff * f) : null;
        }
    }

    public Vector3Df getIntermediateWithZValue(Vector3Df v, float z) {
        float xDiff = v.x - this.x;
        float yDiff = v.y - this.y;
        float zDiff = v.z - this.z;
        if ((double) (zDiff * zDiff) < 1.0E-7D) {
            return null;
        } else {
            float f = (z - this.z) / zDiff;
            return !(f < 0.0F) && !(f > 1.0F) ? new Vector3Df(this.x + xDiff * f, this.y + yDiff * f, this.z + zDiff * f) : null;
        }
    }

    public Vector3Df setComponents(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public String toString() {
        return "Vector3D(x=" + this.x + ",y=" + this.y + ",z=" + this.z + ")";
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Vector3Df)) {
            return false;
        } else {
            Vector3Df other = (Vector3Df) obj;
            return this.x == other.x && this.y == other.y && this.z == other.z;
        }
    }

    public int rawHashCode() {
        return super.hashCode();
    }

    public Vector3Df clone() {
        try {
            return (Vector3Df) super.clone();
        } catch (CloneNotSupportedException var2) {
            return null;
        }
    }

    public Vector3D asVector3D() {
        return new Vector3D((double) this.x, (double) this.y, (double) this.z);
    }

    public BlockVector3D asBlockVector3D() {
        return new BlockVector3D(this.getFloorX(), this.getFloorY(), this.getFloorZ());
    }
}
