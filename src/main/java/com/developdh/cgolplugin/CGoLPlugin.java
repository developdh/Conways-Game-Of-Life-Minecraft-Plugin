package com.developdh.cgolplugin;

//import org.bukkit.command.CommandExecutor;
//import org.bukkit.plugin.java.JavaPlugin;
//import org.bukkit.scheduler.BukkitRunnable;
//import java.util.LinkedList;
//import java.util.Queue;
//
//public final class CGoLPlugin extends JavaPlugin {
//
//    @Override
//    public void onEnable() {
//        // Plugin startup logic
//        getLogger().info("라이프 게임 플러그인 입갤");
//        getCommand("lifegame").setExecutor((CommandExecutor) new StartCommand());
//    }
//
//    @Override
//    public void onDisable() {
//        getLogger().info("라이프 게임 플러그인 퇴갤");
//    }
//}

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class CGoLPlugin extends JavaPlugin {
    Ticker ticker = new Ticker();
    PointsManagerMap pointsManagerMap = new PointsManagerMap();
    HashMap<UUID, Generator> generatorMap = new HashMap<>();
    EventListener eventListener = new EventListener(ticker, pointsManagerMap, generatorMap);

    @Override
    public void onEnable(){
        Bukkit.getPluginManager().registerEvents(eventListener, this);
        this.getCommand("input").setExecutor(eventListener);
        this.getCommand("gen").setExecutor(eventListener);
        this.getCommand("kgen").setExecutor(eventListener);
        this.getCommand("cl").setExecutor(eventListener);
        this.getCommand("genstop").setExecutor(eventListener);
        Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "Plugin enabled");

        ticker.runTaskTimer(this, 0L, 1L);
    }

    @Override
    public void onDisable(){
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Plugin disabled");
    }

}


class PointsManagerMap extends HashMap<UUID, PointsManager> {
    PointsManager get(UUID key) {
        if(!super.containsKey(key))
            super.put(key, new PointsManager());
        return super.get(key);
    }
}

class EventListener implements Listener, CommandExecutor {
    private Ticker ticker;
    private PointsManagerMap pointsManagerMap;
    private HashMap<UUID, Generator> generatorMap;

    public EventListener(Ticker ticker, PointsManagerMap pointsManagerMap, HashMap<UUID, Generator> generatorMap) {
        this.ticker = ticker;
        this.pointsManagerMap = pointsManagerMap;
        this.generatorMap = generatorMap;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.getPlayer().getItemInHand().getType() == Material.STONE_AXE) {
            Block block = event.getBlock();
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            event.getPlayer().sendMessage("First point: " + "(" + x +  "," + y +  "," + z + ")");
            event.setCancelled(true);

            Player player = event.getPlayer();
            pointsManagerMap.get(player.getUniqueId()).setFirstPoint(new Point(x, y, z));
        }
    }

    @EventHandler
    public void antiFortification(PlayerInteractEvent event) {
        // final Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem() != null && event.getItem().getType() == Material.STONE_AXE) {
                Block block = event.getClickedBlock();
                int x = block.getX();
                int y = block.getY();
                int z = block.getZ();
                event.getPlayer().sendMessage("Second point: " + "(" + x +  "," + y +  "," + z + ")");

                Player player = event.getPlayer();
                pointsManagerMap.get(player.getUniqueId()).setSecondPoint(new Point(x, y, z));
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        World world = player.getWorld();
        PointsManager pointsManager = pointsManagerMap.get(uuid);

        String cmdName = command.getName();

        if(cmdName.equals("gen")) {

            Point[] outputPoints = pointsManager.getCurrentSortedAdjustedPoints();

            Generator gen = new Generator(ticker, Bukkit.getServer(), world, outputPoints, () -> {
                generatorMap.remove(uuid);
            });

            if(!generatorMap.containsKey(uuid)) {
                generatorMap.put(uuid, gen);
                gen.run();
                sender.sendMessage("GEN START!");
            } else {
                sender.sendMessage("Sorry, only one work for one player. /genstop to stop current work or wait to finish current work.");
            }
        } else if(cmdName.equals("genstop")) {
            if(generatorMap.containsKey(uuid)) {
                Generator generator = generatorMap.get(uuid);
                generator.stop();
                sender.sendMessage("Stopped successfully.");
            } else {
                sender.sendMessage("There is no jobs to stop.");
            }
        } else if(cmdName.equals("cl")) {
            Point[] outputPoints = pointsManager.getCurrentSortedAdjustedPoints();

            for(int x = outputPoints[0].x; x <= outputPoints[1].x; x++) {
                for(int y = outputPoints[0].y; y <= outputPoints[1].y; y++) {
                    for(int z = outputPoints[0].z; z <= outputPoints[1].z; z++) {
                        world.getBlockAt(x, y, z).setType(Material.AIR);
                    }
                }
            }

            sender.sendMessage("ssck-ssack");
        }
        return true;
    }
}

class PointsManager {
    private Point[] points = {null, null};
    private Point[] patternPoints = {null, null};

    void setFirstPoint(Point point) {
        this.points[0] = point;
    }
    void setSecondPoint(Point point) {
        this.points[1] = point;
    }
    static Point[] getAdjustedPoints(Point[] points) {
        int bx = points[0].x > points[1].x ? -1 : +1;
        int by = points[0].y > points[1].y ? -1 : +1;
        int bz = points[0].z > points[1].z ? -1 : +1;


        Point newPoint1 = new Point(points[0].x + bx, points[0].y + by, points[0].z + bz);
        Point newPoint2 = new Point(points[1].x - bx, points[1].y - by, points[1].z - bz);

        Point[] points_ = {newPoint1, newPoint2};

        return points_;
    }
    static Point getMinPoint(Point[] points) {
        Point firstPoint = points[0];
        Point secondPoint = points[1];
        return new Point(Math.min(firstPoint.x, secondPoint.x), Math.min(firstPoint.y, secondPoint.y), Math.min(firstPoint.z, secondPoint.z));
    }
    static Point getMaxPoint(Point[] points) {
        Point firstPoint = points[0];
        Point secondPoint = points[1];
        return new Point(Math.max(firstPoint.x, secondPoint.x), Math.max(firstPoint.y, secondPoint.y), Math.max(firstPoint.z, secondPoint.z));
    }
    static Point[] getSortedAdjustedPoints(Point[] points) {
        Point[] adjusted = getAdjustedPoints(points);
        Point[] sortedPoints = {getMinPoint(adjusted), getMaxPoint(adjusted)};
        return sortedPoints;
    }
    Point[] getCurrentSortedAdjustedPoints() {
        return getSortedAdjustedPoints(points);
    }

    void setPatternPoints() {
        Point[] adjusted = getAdjustedPoints(points);
        patternPoints[0] = getMinPoint(adjusted);
        patternPoints[1] = getMaxPoint(adjusted);
    }
    Point[] getPatternPoints() {
        return patternPoints;
    }
}

class Point {
    int x;
    int y;
    int z;
    public Point(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}

class Ticker extends BukkitRunnable {
    Queue<Runnable> queue = new LinkedList<>();

    @Override
    public void run() {
        synchronized(queue) {
            int i = 0;
            while(!queue.isEmpty() && i < 1000) {
                Runnable r = queue.poll();
                r.run();
                i++;
            }
        }
    }
}

class Generator {
    Server server;
    World world;
    int output_size_x;
    int output_size_y;
    int output_size_z;
    Point[] outputPoints;
    Point outputStartPoint;
    Ticker ticker;
    Thread thread;
    boolean stopped;
    Runnable onEnd;
    public Generator(Ticker ticker, Server server, World world, Point[] outputPoints, Runnable onEnd) {
        this.ticker = ticker;
        this.server = server;
        this.world = world;
        this.output_size_x = outputPoints[1].x - outputPoints[0].x + 1;
        this.output_size_y = outputPoints[1].y - outputPoints[0].y + 1;
        this.output_size_z = outputPoints[1].z - outputPoints[0].z + 1;
        this.outputPoints = outputPoints;
        this.outputStartPoint = outputPoints[0];
        this.onEnd = onEnd;

//        if(isKeep) {
//            for(int x = outputPoints[0].x; x <= outputPoints[1].x; x++) {
//                for(int y = outputPoints[0].y; y <= outputPoints[1].y; y++) {
//                    for(int z = outputPoints[0].z; z <= outputPoints[1].z; z++) {
//                        int blockId = world.getBlockAt(x, y, z).getTypeId();
//                        if(blockId != 0) {
//                            int[] keep = {x - outputStartPoint.x, y - outputStartPoint.y, z - outputStartPoint.z, blockId};
//                            keeps.add(keep);
//                        }
//                    }
//                }
//            }
//        }
    }
    public void run() {
        Generator self = this;
        this.thread = new Thread(() -> {
            try {
//                Process process = Runtime.getRuntime().exec("./exe/wfc_stdio.exe");
//                self.process = process;
//                PrintWriter writer = new PrintWriter(process.getOutputStream());
//                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//
//                writer.println(input_size_x + " " + input_size_y + " " + input_size_z);
//                writer.println((periodic_inputs[0] ? 1 : 0) + " " + (periodic_inputs[1] ? 1 : 0) + " " + (periodic_inputs[2] ? 1 : 0));
//                writer.println((periodic_outputs[0] ? 1 : 0) + " " + (periodic_outputs[1] ? 1 : 0) + " " + (periodic_outputs[2] ? 1 : 0));
//                writer.println(output_size_x + " " + output_size_y + " " + output_size_z);
//                writer.println(N);
//
//                writer.println(keeps.size());
//                for(int[] keep: keeps) {
//                    writer.println(keep[0] + " " + keep[1] + " " + keep[2] + " " + keep[3]);
//                }
//
//                for(int x = 0; x < input_size_x; x++) {
//                    for(int y = 0; y < input_size_y; y++) {
//                        for(int z = 0; z < input_size_z; z++) {
//                            writer.println(input[x][y][z]);
//                        }
//                    }
//                }
//                writer.flush();
//
//                String str;
//                while((str=reader.readLine())!=null) {
//                    String[] args = str.split(" ");
//                    if(args[0].equals("SET")) {
//                        int x, y, z;
//                        x = Integer.parseInt(args[1]) + outputStartPoint.x;
//                        y = Integer.parseInt(args[2]) + outputStartPoint.y;
//                        z = Integer.parseInt(args[3]) + outputStartPoint.z;
//                        int blockId = Integer.parseInt(args[4]);
//
//                        self.ticker.queue.offer(() -> {
//                            Block block = world.getBlockAt(x, y, z);
//                            block.setType(Material.STONE);
//                        });
//                    } else if(args[0].equals("FINISHSET")) {
//                        int x, y, z;
//                        x = Integer.parseInt(args[1]) + outputStartPoint.x;
//                        y = Integer.parseInt(args[2]) + outputStartPoint.y;
//                        z = Integer.parseInt(args[3]) + outputStartPoint.z;
//                        int blockId = Integer.parseInt(args[4]);
//
//                        self.ticker.queue.offer(() -> {
//                            Block block = world.getBlockAt(x, y, z);
//                            block.setType(Material.STONE);
//                        });
//                    } else if(args[0].equals("UNDO")) {
//
//                    } else if(args[0].equals("FINISH")) {
//                        server.broadcastMessage("FINISH!!");
//                    } else if(args[0].equals("RESET")) {
//                        self.ticker.queue.offer(() -> {
//                            for(int x = self.outputPoints[0].x; x <= self.outputPoints[1].x; x++) {
//                                for(int y = self.outputPoints[0].y; y <= self.outputPoints[1].y; y++) {
//                                    for(int z = self.outputPoints[0].z; z <= self.outputPoints[1].z; z++) {
//                                        world.getBlockAt(x, y, z).setType(Material.AIR);
//                                    }
//                                }
//                            }
//                        });
//                    } else {
//                        // System.out.println(str);
//                    }
//                }

                var board = new Board3D();

                final boolean X = false;
                final boolean O = true;


                boolean[][][] map = new boolean[output_size_x + 1][output_size_y + 1][output_size_z + 1];



                for (int dx = 0; dx < output_size_x; dx++) {
                    for (int dy = 0; dy < output_size_y; dy++) {
                        for (int dz = 0; dz < output_size_z; dz++) {
                            int x, y, z;
                            x = dx + outputStartPoint.x;
                            y = dy + outputStartPoint.y;
                            z = dz + outputStartPoint.z;
                            if (world.getBlockAt(x, y, z).getType() == Material.STONE){
                                map[dx][dy][dz] = O;
                            }
                            else{
                                map[dx][dy][dz] = X;
                            }
                        }
                    }
                }

                int[] ltl = {4, 8}; //survive
                int[] dtl = {2, 5}; //born

                board.enableLiveToLiveWhen(ltl);
                board.enableDeadToLiveWhen(dtl);

                board.loadMap(map);
                while(!self.stopped) {
                    //player.sendMessage(TestRBT2D.grid2String(grid));
                    boolean[][][] resmap = board.saveMap(output_size_x, output_size_y, output_size_z);
                    self.ticker.queue.offer(() -> {
                        for (int dx = 0; dx < output_size_x; dx++) {
                            for (int dy = 0; dy < output_size_y; dy++) {
                                for (int dz = 0; dz < output_size_z; dz++) {
                                    int x, y, z;
                                    x = dx + outputStartPoint.x;
                                    y = dy + outputStartPoint.y;
                                    z = dz + outputStartPoint.z;

                                    if (resmap[dx][dy][dz])
                                        world.getBlockAt(x, y, z).setType(Material.STONE);
                                    else
                                        world.getBlockAt(x, y, z).setType(Material.AIR);

                                }
                            }
                        }
                    });

                    board.nextGeneration();

                    Thread.sleep(1000);
                }

                self.onEnd.run();


                // System.out.println("DEAD");
            } catch(ArrayIndexOutOfBoundsException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }
    public void stop() {
        this.stopped = true;
    }
}