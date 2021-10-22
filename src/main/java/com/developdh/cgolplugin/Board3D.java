package com.developdh.cgolplugin;

import java.util.TreeSet;

/**
 * Abstract Conway's game of life Board in 3D with Red Black Tree
 * no padding
 */
public class Board3D {

    private TreeSet<Cell3D> map, tempMap;

    private boolean[] liveToLiveWhen, deadToLiveWhen;

    public Board3D() {
        liveToLiveWhen = new boolean[27];
        deadToLiveWhen = new boolean[27];

        map = new TreeSet<>();
        tempMap = new TreeSet<>();

    }

    /**
     * return ture if any cell is located in coordinate
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return is located
     */
    public boolean hasCell(int x, int y, int z) {
        return map.contains(new Cell3D(x, y, z));
    }


    /**
     * Swap map with tempMap
     */
    private void swapMap() {
        TreeSet<Cell3D> tmp = this.tempMap;
        this.tempMap = this.map;
        this.map = tmp;
    }


    /**
     * Get neighbor cell count.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return neighbor cell count
     */
    private int CountNeighborCell(int x, int y, int z) {
        int sum = 0;
        Cell3D cur = new Cell3D();
        for (int i = z - 1; i <= z + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                for (int k = x - 1; k <= x + 1; k++) {
                    if (i == z && j == y && k == x)
                        continue;

                    cur.setPos(k, j, i);
                    if (map.contains(cur))
                        sum++;

                }
            }
        }
        return sum;
    }


    /**
     * Get one coordinate's survival or not in next generation.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return survival or not
     */
    private boolean nextGenerationOne(int x, int y, int z) {
        final int neighborCount = CountNeighborCell(x, y, z);
        final Cell3D finding = new Cell3D(x, y, z);
        final boolean alive = map.contains(finding);

        return (!alive && deadToLiveWhen[neighborCount]) || (alive && liveToLiveWhen[neighborCount]);
    }


    /**
     * Change map to next generation.
     */
    public void nextGeneration() {
        tempMap.clear();

        int eXSize, eYSize, eZSize;
        for (var e : map) {
            eXSize = e.getXPos();
            eYSize = e.getYPos();
            eZSize = e.getZPos();

            for (int z = eZSize - 1; z <= eZSize + 1; z++) {
                for (int y = eYSize - 1; y <= eYSize + 1; y++) {
                    for (int x = eXSize - 1; x <= eXSize + 1; x++) {
                        if (nextGenerationOne(x, y, z))
                            tempMap.add(new Cell3D(x, y, z));
                    }
                }
            }
        }

        swapMap();
    }


    /**
     * Import map as 3d boolean
     * @param loadingMap map
     * @param startX start x coordinate  (default : 0)
     * @param startY start y coordinate  (default : 0)
     * @param startZ start z coordinate  (default : 0)
     */
    public void loadMap(boolean[][][] loadingMap, int startX, int startY, int startZ) {
        final int loadingZ = loadingMap.length;
        final int loadingY = loadingMap[0].length;
        final int loadingX = loadingMap[0][0].length;

        for (int z = 0; z < loadingZ; z++) {
            for (int y = 0; y < loadingY; y++) {
                for (int x = 0; x < loadingX; x++) {
                    if (loadingMap[z][y][x]) {
                        final Cell3D cell = new Cell3D(startX + x, startY + y, startZ + z);

                        map.add(cell);
                    }
                }
            }
        }
    }

    public void loadMap(boolean[][][] loadingMap) {
        loadMap(loadingMap, 0, 0, 0);
    }


    /**
     * Export map as 3d boolean
     * @return 3d boolean map
     */
    public boolean[][][] saveMap(int xSize, int ySize, int zSize) {
        var result = new boolean[zSize][ySize][xSize];

        for (Cell3D e : map) {
            int x = e.getXPos();
            int y = e.getYPos();
            int z = e.getZPos();
            result[z][y][x] = true;
        }

        return result;
    }


    /**
     * set true liveToLiveWhen with indexes.
     * @param nums indexes
     */
    public void enableLiveToLiveWhen(int[] nums) {
        for (var e : nums) {
            liveToLiveWhen[e] = true;
        }
    }


    /**
     * set false liveToLiveWhen with indexes.
     * @param nums indexes
     */
    public void disableLiveToLiveWhen(int[] nums) {
        for (var e : nums) {
            liveToLiveWhen[e] = false;
        }
    }


    /**
     * set true deadToLiveWhen with indexes.
     * @param nums indexes
     */
    public void enableDeadToLiveWhen(int[] nums) {
        for (var e : nums) {
            deadToLiveWhen[e] = true;
        }
    }


    /**
     * set false deadToLiveWhen with indexes.
     * @param nums indexes
     */
    public void disableDeadToLiveWhen(int[] nums) {
        for (var e : nums) {
            deadToLiveWhen[e] = false;
        }
    }

    public TreeSet<Cell3D> getMap() {
        return map;
    }

}