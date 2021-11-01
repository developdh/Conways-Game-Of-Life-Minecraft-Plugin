package com.developdh.cgolplugin;

public class LimitedBoard3D extends Board3D {
    private int xSize;
    private int ySize;
    private int zSize;

    public LimitedBoard3D(int xSize, int ySize, int zSize) {
        super();
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
    }

    @Override
    protected boolean nextGenerationOne(int x, int y, int z) {
        if (x < 0 || x >= xSize || y < 0 || y >= ySize || z < 0 || z >= zSize) return false;

        return super.nextGenerationOne(x, y, z);
    }

    public int getXSize() {
        return xSize;
    }

    public void setXSize(int xSize) {
        this.xSize = xSize;
    }

    public int getYSize() {
        return ySize;
    }

    public void setYSize(int ySize) {
        this.ySize = ySize;
    }

    public int getZSize() {
        return zSize;
    }

    public void setZSize(int zSize) {
        this.zSize = zSize;
    }
}