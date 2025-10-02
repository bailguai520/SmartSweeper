package com.qiyu.smartsweeper;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = SmartSweeper.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Item clear configuration
    public static final ModConfigSpec.BooleanValue CLEAR_ENABLED = BUILDER
            .comment("Enable automatic item clearing")
            .define("clearEnabled", true);

    public static final ModConfigSpec.IntValue CLEAR_INTERVAL = BUILDER
            .comment("Item clear interval in seconds (minimum 10 seconds)")
            .defineInRange("clearInterval", 300, 10, 3600);

    public static final ModConfigSpec.BooleanValue SHOW_CLEAR_MESSAGE = BUILDER
            .comment("Show message in chat when items are cleared")
            .define("showClearMessage", true);

    public static final ModConfigSpec.IntValue WARNING_TIME = BUILDER
            .comment("Warning time before clearing in seconds (fixed at 10 seconds)")
            .defineInRange("warningTime", 10, 0, 300);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> WHITELIST = BUILDER
            .comment("List of items that will NOT be cleared (format: modid:itemname)")
            .defineListAllowEmpty("whitelist",
                    List.of("minecraft:diamond", "minecraft:netherite_ingot", "minecraft:nether_star"),
                    () -> "",
                    Config::validateItemName);

    public static final ModConfigSpec.BooleanValue CLEAR_ONLY_NATURAL = BUILDER
            .comment("Only clear naturally spawned items (items from broken blocks, not player drops)")
            .define("clearOnlyNatural", false);

    static final ModConfigSpec SPEC = BUILDER.build();

    // Cached whitelist for performance
    private static Set<Item> whitelistCache = new HashSet<>();

    private static boolean validateItemName(final Object obj) {
        if (obj instanceof String itemName) {
            try {
                ResourceLocation location = ResourceLocation.parse(itemName);
                return BuiltInRegistries.ITEM.getValue(location) != null;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        refreshWhitelist();
    }

    public static void refreshWhitelist() {
        whitelistCache.clear();
        for (String itemName : WHITELIST.get()) {
            ResourceLocation location = ResourceLocation.parse(itemName);
            Item item = BuiltInRegistries.ITEM.getValue(location);
            if (item != null) {
                whitelistCache.add(item);
            }
        }
    }

    public static boolean isWhitelisted(Item item) {
        return whitelistCache.contains(item);
    }

    public static Set<Item> getWhitelistedItems() {
        return new HashSet<>(whitelistCache);
    }

    public static void addToWhitelist(String itemId) {
        List<String> currentList = new ArrayList<>(WHITELIST.get());
        if (!currentList.contains(itemId) && validateItemName(itemId)) {
            currentList.add(itemId);
            WHITELIST.set(currentList);
            refreshWhitelist();
        }
    }

    public static void removeFromWhitelist(String itemId) {
        List<String> currentList = new ArrayList<>(WHITELIST.get());
        if (currentList.remove(itemId)) {
            WHITELIST.set(currentList);
            refreshWhitelist();
        }
    }
}

