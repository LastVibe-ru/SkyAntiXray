package com.karpen.skyAntiXray;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
    private final long thresholdTime = 120000;
    private final int requiredBreaks = 4;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
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
