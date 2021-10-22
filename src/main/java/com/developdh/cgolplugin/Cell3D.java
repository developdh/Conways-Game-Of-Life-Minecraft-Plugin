package com.developdh.cgolplugin;

public class Cell3D implements Comparable<Cell3D> {

    private int x;
    private int y;
    private int z;


    public Cell3D(int x, int y, int z) {
        this.setPos(x, y, z);
    }

    public Cell3D() {
        this(0, 0, 0);
    }


    public void setPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }


    public int getXPos() {
        return x;
    }

    public int getYPos() {
        return y;
    }

    public int getZPos() {
        return z;
    }

    @Override
    public int compareTo(Cell3D o) {
        if (this.z != o.z) {
            return this.z - o.z;
        }
        if (this.y != o.y) {
            return this.y - o.y;
        }
        if (this.x != o.x) {
            return this.x - o.x;
        }
        return 0;
    }
}