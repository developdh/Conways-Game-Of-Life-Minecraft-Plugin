package com.developdh.cgolplugin;

public class TestRBT3D {
    public static String board3dToString(Board3D grid) {
        String result = "vec![ ";

        final int xSize = 51;
        final int ySize = 51;
        final int zSize = 51;


        final var map = grid.getMap();

        for (var e : map) {
            result += "(" + e.getXPos() + ", " + e.getYPos() + ", " + e.getZPos() + "), ";
        }

        result += "],";

        return result;
    }

    public static void main(String[] args) {
        var board = new Board3D();

        final boolean X = false;
        final boolean O = true;


        boolean[][][] map = {
                {
                        {X, X, X, X, X, X, X},
                        {X, X, X, X, X, X, X},
                        {X, X, X, X, X, X, X},
                        {X, X, X, X, X, X, X},
                        {X, X, X, X, X, X, X},
                        {X, X, X, X, X, X, X},
                        {X, X, X, X, X, X, X}
                }, {
                {X, X, X, X, X, X, X},
                {X, X, X, X, X, X, X},
                {X, X, X, X, X, X, X},
                {X, X, X, X, X, X, X},
                {X, X, X, X, X, X, X},
                {X, X, X, X, X, X, X},
                {X, X, X, X, X, X, X},
        }, {
                {X, X, X, X, X, X, X},
                {X, X, X, X, X, X, X},
                {X, X, X, X, X, X, X},
                {X, X, X, X, X, X, X},
                {X, X, X, X, X, X, X},
                {X, X, X, X, X, O, X},
                {X, X, X, X, X, X, X},
        }, {
                {X, X, X, X, X, X, X},
                {X, X, X, X, X, X, X},
                {X, X, X, X, X, X, X},
                {X, X, X, X, X, X, X},
                {X, X, X, X, X, O, X},
                {X, X, X, X, O, O, O},
                {X, X, X, X, X, O, X},
        }, {
                {X, X, X, X, X, X, X},
                {X, X, X, X, X, X, X},
                {X, X, X, X, X, X, X},
                {X, X, X, X, X, X, X},
                {X, X, X, X, X, X, X},
                {X, X, X, X, X, O, X},
                {X, X, X, X, X, X, X},
        },
        };


        int[] ltl = {5};
        int[] dtl = {4, 5};

        board.enableLiveToLiveWhen(ltl);
        board.enableDeadToLiveWhen(dtl);

        board.loadMap(map);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            System.out.println(TestRBT3D.board3dToString(board));
            board.nextGeneration();
        }

        // End
        long endTime = System.currentTimeMillis();

        // Result
        long Result_Time = endTime - startTime;
        System.out.println("Result : " + Result_Time + "(ms)");

    }
}