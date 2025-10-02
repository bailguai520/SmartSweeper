package com.qiyu.smartsweeper;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = SmartSweeper.MODID)
public class ItemClearManager {
    private static int tickCounter = 0;
    private static boolean hasWarnedThisCycle = false;
    private static MinecraftServer server = null;

    // Statistics
    private static long totalItemsCleared = 0L;         // Count of individual items cleared
    private static long totalEntitiesCleared = 0L;      // Count of ItemEntity stacks cleared
    private static int clearCount = 0;                  // Number of clear operations performed

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        server = event.getServer();
        tickCounter = 0;
        hasWarnedThisCycle = false;
        SmartSweeper.LOGGER.info("Item Clear Manager initialized");
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (server == null || !Config.CLEAR_ENABLED.get()) {
            return;
        }

        tickCounter++;

        int clearIntervalTicks = Config.CLEAR_INTERVAL.get() * 20; // Convert seconds to ticks
        int warningTimeTicks = 10 * 20; // Fixed at 10 seconds
        
        // 确保清理间隔大于警告时间
        if (clearIntervalTicks <= warningTimeTicks) {
            // 如果清理间隔太短，不发送警告，直接清理
            if (tickCounter >= clearIntervalTicks) {
                clearItems();
                tickCounter = 0;
                hasWarnedThisCycle = false;
            }
            return;
        }

        // Send warning message (10 seconds before clearing)
        if (!hasWarnedThisCycle && tickCounter >= (clearIntervalTicks - warningTimeTicks)) {
            sendWarningMessage();
            hasWarnedThisCycle = true;
        }
        
        // Send countdown messages (5 seconds and less)
        if (hasWarnedThisCycle && Config.SHOW_CLEAR_MESSAGE.get()) {
            int remainingTicks = clearIntervalTicks - tickCounter;
            int remainingSeconds = remainingTicks / 20;
            
            // 在5秒及以下每秒提示一次
            if (remainingSeconds <= 5 && remainingSeconds > 0 && remainingTicks % 20 == 0) {
                Component countdownMsg = Component.translatable("SmartSweeper.clear.countdown", remainingSeconds);
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    player.displayClientMessage(countdownMsg, true); // Action bar message
                }
            }
        }

        // Clear items
        if (tickCounter >= clearIntervalTicks) {
            clearItems();
            tickCounter = 0;
            hasWarnedThisCycle = false;
        }
    }

    private static void sendWarningMessage() {
        if (!Config.SHOW_CLEAR_MESSAGE.get() || server == null) {
            return;
        }

        Component message = Component.translatable("SmartSweeper.clear.warning", 10);
        
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(message);
        }
    }

    private static void clearItems() {
        if (server == null) {
            return;
        }

        long itemsClearedThisRun = 0L;
        int entitiesClearedThisRun = 0;

        for (ServerLevel level : server.getAllLevels()) {
            List<ItemEntity> itemsToClear = new ArrayList<>();
            
            for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
                if (!(entity instanceof ItemEntity itemEntity)) {
                    continue;
                }
                
                // Skip if item is in whitelist
                if (Config.isWhitelisted(itemEntity.getItem().getItem())) {
                    continue;
                }

                // Skip player-dropped items if configured
                if (Config.CLEAR_ONLY_NATURAL.get() && itemEntity.getOwner() != null) {
                    continue;
                }

                // Skip items that are too young (just dropped)
                if (itemEntity.getAge() < 20) { // Less than 1 second old
                    continue;
                }

                itemsToClear.add(itemEntity);
            }

            // Remove the items
            for (ItemEntity item : itemsToClear) {
                // Count by actual item quantity, not stacks
                itemsClearedThisRun += item.getItem().getCount();
                entitiesClearedThisRun++;
                item.discard();
            }
        }

        // Update statistics
        if (entitiesClearedThisRun > 0) {
            totalItemsCleared += itemsClearedThisRun;
            totalEntitiesCleared += entitiesClearedThisRun;
            clearCount++;
        }

        // Send clear completion message
        if (Config.SHOW_CLEAR_MESSAGE.get()) {
            Component message = Component.translatable("SmartSweeper.clear.complete", itemsClearedThisRun);
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.sendSystemMessage(message);
            }
        }

        SmartSweeper.LOGGER.info("Cleared {} items ({} entities) from all worlds", itemsClearedThisRun, entitiesClearedThisRun);
    }

    /**
     * Manually trigger item clearing (can be called from commands or GUI)
     */
    public static int manualClear() {
        if (server == null) {
            return 0;
        }

        long itemsClearedThisRun = 0L;
        int entitiesClearedThisRun = 0;

        for (ServerLevel level : server.getAllLevels()) {
            for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
                if (!(entity instanceof ItemEntity itemEntity)) {
                    continue;
                }
                
                if (!Config.isWhitelisted(itemEntity.getItem().getItem())) {
                    itemsClearedThisRun += itemEntity.getItem().getCount();
                    entitiesClearedThisRun++;
                    itemEntity.discard();
                }
            }
        }

        // Reset timer after manual clear
        tickCounter = 0;
        hasWarnedThisCycle = false;

        // Update statistics for manual clear
        if (entitiesClearedThisRun > 0) {
            totalItemsCleared += itemsClearedThisRun;
            totalEntitiesCleared += entitiesClearedThisRun;
            clearCount++;
        }

        return (int) itemsClearedThisRun;
    }

    /**
     * Get time until next clear in seconds
     */
    public static int getTimeUntilClear() {
        int clearIntervalTicks = Config.CLEAR_INTERVAL.get() * 20;
        int remainingTicks = clearIntervalTicks - tickCounter;
        return Math.max(0, remainingTicks / 20);
    }

    // --- Statistics API ---
    public static long getTotalItemsCleared() {
        return totalItemsCleared;
    }

    public static long getTotalEntitiesCleared() {
        return totalEntitiesCleared;
    }

    public static int getClearCount() {
        return clearCount;
    }

    public static void resetStatistics() {
        totalItemsCleared = 0L;
        totalEntitiesCleared = 0L;
        clearCount = 0;
    }
}

