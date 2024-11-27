package com.karpen.skyAntiXray;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SkyAntiXray extends JavaPlugin implements Listener {

    private final Map<UUID, Long> lastDiamondBreakTime = new HashMap<>();
    private final Map<UUID, Integer> diamondBreakCounter = new HashMap<>();

    private long thresholdTime;
    private int requiredBreaks;

    @Override
    public void onEnable() {
        loadConfig();

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
    }

    private void loadConfig(){
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        thresholdTime = config.getLong("thresholdTime");
        requiredBreaks = config.getInt("requiredBreaks");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("skyantixray")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")){
                loadConfig();
                sender.sendMessage(ChatColor.GREEN + "Конфигурация SkyAntiXray успешно перезагружена");
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Использование: /skyantixray reload");
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.DIAMOND_ORE || event.getBlock().getType() == Material.DEEPSLATE_DIAMOND_ORE) {
            UUID playerId = event.getPlayer().getUniqueId();
            long currentTime = System.currentTimeMillis();

            if (lastDiamondBreakTime.containsKey(playerId)) {
                long lastBreakTime = lastDiamondBreakTime.get(playerId);

                if (currentTime - lastBreakTime < thresholdTime) {
                    diamondBreakCounter.put(playerId, diamondBreakCounter.getOrDefault(playerId, 0) + 1);

                    if (diamondBreakCounter.get(playerId) >= requiredBreaks) {
                        String message = String.format(ChatColor.RED + "Подозрительная активность от игрока " + ChatColor.WHITE + "%s" + ChatColor.RED + " на координатах " + ChatColor.WHITE + "[%d, %d, %d]",
                                event.getPlayer().getName(),
                                event.getBlock().getX(),
                                event.getBlock().getY(),
                                event.getBlock().getZ()
                        );

                        Bukkit.getOnlinePlayers().stream()
                                .filter(player -> player.isOp())
                                .forEach(op -> op.sendMessage(message));
                    }
                } else {
                    diamondBreakCounter.put(playerId, 1);
                }
            } else {
                diamondBreakCounter.put(playerId, 1);
            }

            lastDiamondBreakTime.put(playerId, currentTime);
        } else {
            diamondBreakCounter.remove(event.getPlayer().getUniqueId());
        }
    }
}
