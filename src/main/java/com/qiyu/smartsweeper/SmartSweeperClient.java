package com.qiyu.smartsweeper;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = SmartSweeper.MODID, dist = Dist.CLIENT)
public class SmartSweeperClient {
    public SmartSweeperClient(IEventBus modEventBus, ModContainer container) {
        // Register our custom config screen
        container.registerExtensionPoint(IConfigScreenFactory.class, 
            (minecraft, parent) -> new ItemClearConfigScreen(parent));
        
        // Register client setup listener on the MOD event bus
        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        SmartSweeper.LOGGER.info("Item Clear Mod - Client Setup Complete");
        SmartSweeper.LOGGER.info("Player: {}", Minecraft.getInstance().getUser().getName());
    }
}
