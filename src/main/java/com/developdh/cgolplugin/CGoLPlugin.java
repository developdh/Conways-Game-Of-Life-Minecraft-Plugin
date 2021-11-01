package com.developdh.cgolplugin;

import java.util.*;

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
        this.getCommand("cl").setExecutor(eventListener);
        this.getCommand("genstop").setExecutor(eventListener);
        this.getCommand("setsurvive").setExecutor(eventListener);
        this.getCommand("setborn").setExecutor(eventListener);
        this.getCommand("status").setExecutor(eventListener);
        Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "Plugin enabled");

        ticker.runTaskTimer(this, 0L, 1L);
    }

    @Override
    public void onDisable(){
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Plugin disabled");
    }

}

class lifegameStatus {

    int[] ltl = {4, 8}; //survive
    int[] dtl = {2, 5}; //born

    public int[] getltl() {
        return this.ltl;
    }
    public void setltl(int[] ltl) {
        this.ltl = ltl;
    }
    public int[] getdtl() {
        return this.dtl;
    }
    public void setdtl(int[] dtl) {
        this.dtl = dtl;
    }

    public lifegameStatus(){}
}


class PointsManagerMap extends HashMap<UUID, PointsManager> {
    PointsManager get(UUID key) {
        if(!super.containsKey(key))
            super.put(key, new PointsManager());
        return super.get(key);
    }
}

class EventListener implements Listener, CommandExecutor {
    private final Ticker ticker;
    private final PointsManagerMap pointsManagerMap;
    private final HashMap<UUID, Generator> generatorMap;

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

            sender.sendMessage("쓱쓱싹싹");
        } else if(cmdName.equals("setsurvive")) {
            int[] surv = Arrays.stream(args).mapToInt(Integer::parseInt).toArray();
            //setsurvivecode return int array // size == args.length -1
        } else if(cmdName.equals("setborn")) {
            //setborncode
        } else if(cmdName.equals("status")) {
            //statuscode
        }else {
            sender.sendMessage("몰?루");
        }
        return true;
    }
}

class PointsManager {
    private final Point[] points = {null, null};
    private final Point[] patternPoints = {null, null};

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
    int[] ltl = {3}; //survive
    int[] dtl = {1, 5}; //born
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
    }
    public void run() {
        Generator self = this;
        this.thread = new Thread(() -> {
            try {
                var board = new LimitedBoard3D(output_size_x, output_size_y, output_size_z);

                final boolean X = false;
                final boolean O = true;


                boolean[][][] map = new boolean[output_size_x][output_size_y][output_size_z];



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
                this.stop();
            }
        });

        thread.start();
    }
    public void stop() {
        this.stopped = true;
    }
}
