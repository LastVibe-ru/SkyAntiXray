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
    private final long thresholdTime = 120000; // 2 минуты в миллисекундах
    private final int requiredBreaks = 4; // Количество подряд добытых алмазов

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Логика завершения работы плагина
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.DIAMOND_ORE || event.getBlock().getType() == Material.DEEPSLATE_DIAMOND_ORE) {
            UUID playerId = event.getPlayer().getUniqueId();
            long currentTime = System.currentTimeMillis();

            // Проверяем, если игрок уже добывал алмазы
            if (lastDiamondBreakTime.containsKey(playerId)) {
                long lastBreakTime = lastDiamondBreakTime.get(playerId);

                // Проверяем, если время между добычами меньше порога
                if (currentTime - lastBreakTime < thresholdTime) {
                    // Увеличиваем счётчик добытых алмазов
                    diamondBreakCounter.put(playerId, diamondBreakCounter.getOrDefault(playerId, 0) + 1);

                    // Проверяем, достиг ли счётчик необходимого количества
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
                    // Если время между добычами больше порога, сбрасываем счётчик
                    diamondBreakCounter.put(playerId, 1);
                }
            } else {
                // Если это первая добыча, инициализируем счётчик
                diamondBreakCounter.put(playerId, 1);
            }

            // Обновляем время последней добычи
            lastDiamondBreakTime.put(playerId, currentTime);
        } else {
            // Если игрок добывает не алмазы, сбрасываем счётчик
            diamondBreakCounter.remove(event.getPlayer().getUniqueId());
        }
    }
}
