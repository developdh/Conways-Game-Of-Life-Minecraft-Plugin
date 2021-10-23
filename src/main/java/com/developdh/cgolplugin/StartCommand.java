package com.developdh.cgolplugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        Player player = (Player) sender;
        World world = player.getWorld();

        player.sendMessage("lifegame start");

        var board = new Board3D();

        final boolean X = false;
        final boolean O = true;


        boolean[][][] map = new boolean[10][10][10];



        for(int x = 0; x < 10; x++){
            for(int y = 0; y < 10; y++) {
                for (int z = 0; z < 10; z++) {
                    if (world.getBlockAt(x, y + 4, z).getType() == Material.STONE){
                        map[x][y][z] = O;
                        player.sendMessage("hello");
                    }
                    else{
                        map[x][y][z] = X;
                    }
                }
            }
        }

//        for(int x = 0; x < 10; x++){
//            for(int z = 0; z < 10; z++){
//                world.getBlockAt(x, 4, z).setType(Material.STONE);
//            }
//            //player.sendMessage(world.getBlockAt(x, 4, 1).getType().toString());
//        }


        int[] ltl = {5};
        int[] dtl = {4, 5};

        board.enableLiveToLiveWhen(ltl);
        board.enableDeadToLiveWhen(dtl);

        board.loadMap(map);

        for (int i = 0; i < 2; i++) {
            //player.sendMessage(TestRBT2D.grid2String(grid));
            boolean[][][] resmap = board.saveMap(51, 51, 51);
            for(int x = 0; x < 10; x++){
                for(int y = 0; y < 10; y++) {
                    for (int z = 0; z < 10; z++) {
                        if (resmap[x][y][z])
                            world.getBlockAt(x, 4 + y, z).setType(Material.STONE);
                        else
                            world.getBlockAt(x, 4 + y, z).setType(Material.AIR);
                    }
                }
            }

            board.nextGeneration();

            player.sendMessage("T" + i);
        }

        player.sendMessage("lifegame end");

        return true;
    }
}
