package com.developdh.cgolplugin;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.LinkedList;
import java.util.Queue;

public final class CGoLPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("라이프 게임 플러그인 입갤");
        getCommand("lifegame").setExecutor((CommandExecutor) new StartCommand());
    }

    @Override
    public void onDisable() {
        getLogger().info("라이프 게임 플러그인 퇴갤");
    }
}