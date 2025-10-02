package com.qiyu.smartsweeper;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(SmartSweeper.MODID)
public class SmartSweeper {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "smartsweeper";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public SmartSweeper(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        
        // Register network packets
        modEventBus.addListener(this::registerPayloads);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        
        LOGGER.info("Item Clear Mod initialized");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Item Clear Mod - Common Setup");
        LOGGER.info("Auto-clear enabled: {}", Config.CLEAR_ENABLED.get());
        LOGGER.info("Clear interval: {} seconds", Config.CLEAR_INTERVAL.get());
        LOGGER.info("Whitelist size: {}", Config.WHITELIST.get().size());
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
            OpenGuiPacket.TYPE,
            OpenGuiPacket.STREAM_CODEC,
            OpenGuiPacket::handle
        );
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Item Clear Mod - Server Starting");
        LOGGER.info("Registering event handlers");
    }
}
