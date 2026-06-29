package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ShieldItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

// Работает только на клиенте. MODID берём из главного класса ExampleMod
@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class ExampleModClient {

    // ===== ФИЧА 1: КРЕСТИК НА КАЖДОМ ЭКРАНЕ =====
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();

        // В главном меню и паузе крестик не нужен
        if (screen instanceof TitleScreen || screen instanceof PauseScreen) {
            return;
        }

        int size = 20;
        int x = screen.width - size - 5; // правый верхний угол
        int y = 5;

        Button closeBtn = Button.builder(
                Component.literal("✕"),
                b -> screen.onClose()   // закрыть текущий экран
        ).bounds(x, y, size, size).build();

        event.addListener(closeBtn);
    }

    // ===== ФИЧА 2: ЩИТ НА СНИК (ПРИСЕД) =====
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Если открыт какой-то экран — не блокируем
        if (mc.screen != null) {
            mc.options.keyUse.setDown(false);
            return;
        }

        // Щит в любой руке?
        boolean hasShield =
                mc.player.getMainHandItem().getItem() instanceof ShieldItem
             || mc.player.getOffhandItem().getItem()  instanceof ShieldItem;

        // Сидим + есть щит = поднимаем щит (виртуально зажимаем ПКМ)
        boolean wantBlock = mc.player.isShiftKeyDown() && hasShield;

        mc.options.keyUse.setDown(wantBlock);
    }
}
