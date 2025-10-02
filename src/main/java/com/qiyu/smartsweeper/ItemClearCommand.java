package com.qiyu.smartsweeper;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = SmartSweeper.MODID)
public class ItemClearCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        
        dispatcher.register(Commands.literal("smartsweeper")
            .requires(source -> source.hasPermission(2)) // Requires operator permission
            .then(Commands.literal("gui")
                .executes(ItemClearCommand::openGui))
            .then(Commands.literal("now")
                .executes(ItemClearCommand::clearNow))
            .then(Commands.literal("toggle")
                .executes(ItemClearCommand::toggle))
            .then(Commands.literal("interval")
                .then(Commands.argument("seconds", IntegerArgumentType.integer(10, 3600))
                    .executes(ItemClearCommand::setInterval)))
            .then(Commands.literal("whitelist")
                .then(Commands.literal("add")
                    .then(Commands.argument("item", StringArgumentType.string())
                        .executes(ItemClearCommand::addWhitelist)))
                .then(Commands.literal("remove")
                    .then(Commands.argument("item", StringArgumentType.string())
                        .executes(ItemClearCommand::removeWhitelist)))
                .then(Commands.literal("list")
                    .executes(ItemClearCommand::listWhitelist)))
            .then(Commands.literal("stats")
                .then(Commands.literal("show")
                    .executes(ItemClearCommand::showStats))
                .then(Commands.literal("reset")
                    .executes(ItemClearCommand::resetStats)))
            .then(Commands.literal("status")
                .executes(ItemClearCommand::status))
        );
    }

    private static int openGui(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            OpenGuiPacket packet = new OpenGuiPacket(
                ItemClearManager.getTotalItemsCleared(),
                ItemClearManager.getTotalEntitiesCleared(),
                ItemClearManager.getClearCount(),
                ItemClearManager.getTimeUntilClear(),
                Config.CLEAR_ENABLED.get(),
                Config.CLEAR_INTERVAL.get(),
                Config.WHITELIST.get().size()
            );
            PacketDistributor.sendToPlayer(player, packet);
            return 1;
        }
        context.getSource().sendFailure(Component.translatable("SmartSweeper.command.player_only"));
        return 0;
    }

    private static int clearNow(CommandContext<CommandSourceStack> context) {
        int cleared = ItemClearManager.manualClear();
        context.getSource().sendSuccess(
            () -> Component.translatable("SmartSweeper.command.clear.success", cleared), 
            true
        );
        return cleared;
    }

    private static int toggle(CommandContext<CommandSourceStack> context) {
        boolean newState = !Config.CLEAR_ENABLED.get();
        Config.CLEAR_ENABLED.set(newState);
        Config.SPEC.save();
        
        context.getSource().sendSuccess(
            () -> Component.translatable("SmartSweeper.command.toggle", 
                newState ? Component.translatable("SmartSweeper.command.enabled") 
                        : Component.translatable("SmartSweeper.command.disabled")), 
            true
        );
        return newState ? 1 : 0;
    }

    private static int setInterval(CommandContext<CommandSourceStack> context) {
        int seconds = IntegerArgumentType.getInteger(context, "seconds");
        Config.CLEAR_INTERVAL.set(seconds);
        Config.SPEC.save();
        
        context.getSource().sendSuccess(
            () -> Component.translatable("SmartSweeper.command.interval.set", seconds), 
            true
        );
        return seconds;
    }

    private static int addWhitelist(CommandContext<CommandSourceStack> context) {
        String itemId = StringArgumentType.getString(context, "item");
        
        try {
            ResourceLocation location = ResourceLocation.parse(itemId);
            Item item = BuiltInRegistries.ITEM.getValue(location);
            if (item == null) {
                context.getSource().sendFailure(
                    Component.translatable("SmartSweeper.command.whitelist.invalid", itemId)
                );
                return 0;
            }
            
            Config.addToWhitelist(itemId);
            context.getSource().sendSuccess(
                () -> Component.translatable("SmartSweeper.command.whitelist.added", itemId), 
                true
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                Component.translatable("SmartSweeper.command.whitelist.invalid", itemId)
            );
            return 0;
        }
    }

    private static int removeWhitelist(CommandContext<CommandSourceStack> context) {
        String itemId = StringArgumentType.getString(context, "item");
        
        if (Config.WHITELIST.get().contains(itemId)) {
            Config.removeFromWhitelist(itemId);
            context.getSource().sendSuccess(
                () -> Component.translatable("SmartSweeper.command.whitelist.removed", itemId), 
                true
            );
            return 1;
        } else {
            context.getSource().sendFailure(
                Component.translatable("SmartSweeper.command.whitelist.not_found", itemId)
            );
            return 0;
        }
    }

    private static int listWhitelist(CommandContext<CommandSourceStack> context) {
        var whitelist = Config.WHITELIST.get();
        
        if (whitelist.isEmpty()) {
            context.getSource().sendSuccess(
                () -> Component.translatable("SmartSweeper.command.whitelist.empty"), 
                false
            );
        } else {
            context.getSource().sendSuccess(
                () -> Component.translatable("SmartSweeper.command.whitelist.header", whitelist.size()), 
                false
            );
            for (String item : whitelist) {
                context.getSource().sendSuccess(
                    () -> Component.literal("  - " + item), 
                    false
                );
            }
        }
        return whitelist.size();
    }

    private static int showStats(CommandContext<CommandSourceStack> context) {
        long totalItems = ItemClearManager.getTotalItemsCleared();
        long totalEntities = ItemClearManager.getTotalEntitiesCleared();
        int clearCount = ItemClearManager.getClearCount();
        
        context.getSource().sendSuccess(
            () -> Component.translatable("SmartSweeper.command.stats.header"), 
            false
        );
        context.getSource().sendSuccess(
            () -> Component.translatable("SmartSweeper.command.stats.total_items", totalItems), 
            false
        );
        context.getSource().sendSuccess(
            () -> Component.translatable("SmartSweeper.command.stats.total_entities", totalEntities), 
            false
        );
        context.getSource().sendSuccess(
            () -> Component.translatable("SmartSweeper.command.stats.clear_count", clearCount), 
            false
        );
        
        return 1;
    }

    private static int resetStats(CommandContext<CommandSourceStack> context) {
        ItemClearManager.resetStatistics();
        context.getSource().sendSuccess(
            () -> Component.translatable("SmartSweeper.command.stats.reset_success"), 
            true
        );
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        boolean enabled = Config.CLEAR_ENABLED.get();
        int interval = Config.CLEAR_INTERVAL.get();
        int timeUntil = ItemClearManager.getTimeUntilClear();
        int whitelistSize = Config.WHITELIST.get().size();
        
        context.getSource().sendSuccess(
            () -> Component.translatable("SmartSweeper.command.status.header"), 
            false
        );
        context.getSource().sendSuccess(
            () -> Component.translatable("SmartSweeper.command.status.enabled", 
                enabled ? Component.translatable("SmartSweeper.command.enabled") 
                       : Component.translatable("SmartSweeper.command.disabled")), 
            false
        );
        context.getSource().sendSuccess(
            () -> Component.translatable("SmartSweeper.command.status.interval", interval), 
            false
        );
        context.getSource().sendSuccess(
            () -> Component.translatable("SmartSweeper.command.status.next", 
                timeUntil / 60, timeUntil % 60), 
            false
        );
        context.getSource().sendSuccess(
            () -> Component.translatable("SmartSweeper.command.status.whitelist", whitelistSize), 
            false
        );
        
        return 1;
    }
}

