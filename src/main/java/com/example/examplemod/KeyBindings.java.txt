package com.example.examplemod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

// Бинды. По умолчанию НЕ привязаны — повесишь их в TouchController на кнопки.
@EventBusSubscriber(modid = ExampleMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyBindings {

    public static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "key.examplemod.open_config",
            InputConstants.UNKNOWN.getValue(),
            "key.categories.examplemod");

    public static final KeyMapping CYCLE_VIEW = new KeyMapping(
            "key.examplemod.cycle_view",
            InputConstants.UNKNOWN.getValue(),
            "key.categories.examplemod");

    @SubscribeEvent
    public static void onRegister(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CONFIG);
        event.register(CYCLE_VIEW);
    }
}