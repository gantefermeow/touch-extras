package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

// Крестик (перетаскиваемый) + открытие конфига + F5 по биндам. Чисто клиент.
@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class ExampleModClient {

    private static boolean dragging = false;
    private static long pressStart = 0;
    private static double pressMx, pressMy;
    private static final long HOLD_TO_DRAG_MS = 300;
    private static final double MOVE_TOL = 4;
    private static boolean loaded = false;

    private static boolean shouldShow(Screen s) {
        if (s instanceof TitleScreen || s instanceof PauseScreen) return false;
        if (s instanceof ConfigScreen) return false;
        return ConfigData.crossEnabled;
    }

    private static int cx(Screen s) {
        return ConfigData.crossX < 0 ? s.width - ConfigData.crossSize - 6 : ConfigData.crossX;
    }
    private static int cy() { return ConfigData.crossY; }

    // ===== БИНДЫ: открыть конфиг + F5 =====
    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (!loaded) { ConfigData.load(); loaded = true; }

        while (KeyBindings.OPEN_CONFIG.consumeClick()) {
            mc.setScreen(new ConfigScreen(mc.screen));
        }

        if (ConfigData.f5Enabled && mc.screen == null && mc.player != null) {
            while (KeyBindings.CYCLE_VIEW.consumeClick()) {
                mc.options.setCameraType(mc.options.getCameraType().cycle());
            }
        }
    }

    // ===== РЕНДЕР КРЕСТИКА =====
    @SubscribeEvent
    public static void onRender(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();
        if (!shouldShow(screen)) return;

        int size = ConfigData.crossSize;
        int x = Math.max(0, Math.min(cx(screen), screen.width - size));
        int y = Math.max(0, Math.min(cy(), screen.height - size));

        GuiGraphics g = event.getGuiGraphics();
        int mx = event.getMouseX(), my = event.getMouseY();
        boolean hover = mx >= x && mx <= x+size && my >= y && my <= y+size;
        int bg = dragging ? 0xFFDDAA00 : (hover ? 0xFFCC0000 : 0xCC000000);

        g.fill(x, y, x+size, y+size, bg);
        g.renderOutline(x, y, size, size, 0xFFFFFFFF);
        g.drawCenteredString(Minecraft.getInstance().font, "X",
                x + size/2, y + (size-8)/2, 0xFFFFFFFF);
    }

    @SubscribeEvent
    public static void onPress(ScreenEvent.MouseButtonPressed.Pre event) {
        Screen screen = event.getScreen();
        if (!shouldShow(screen)) return;
        if (event.getButton() != 0) return;

        int size = ConfigData.crossSize;
        double mx = event.getMouseX(), my = event.getMouseY();
        int x = cx(screen), y = cy();
        if (mx >= x && mx <= x+size && my >= y && my <= y+size) {
            pressStart = System.currentTimeMillis();
            pressMx = mx; pressMy = my;
            dragging = false;
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onDrag(ScreenEvent.MouseDragged.Pre event) {
        if (pressStart == 0) return;
        double mx = event.getMouseX(), my = event.getMouseY();
        long held = System.currentTimeMillis() - pressStart;
        double moved = Math.abs(mx-pressMx) + Math.abs(my-pressMy);
        if (held >= HOLD_TO_DRAG_MS || moved > MOVE_TOL) dragging = true;
        if (dragging) {
            int size = ConfigData.crossSize;
            ConfigData.crossX = (int)(mx - size/2.0);
            ConfigData.crossY = (int)(my - size/2.0);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRelease(ScreenEvent.MouseButtonReleased.Pre event) {
        if (event.getButton() != 0) return;
        if (pressStart == 0) return;
        Screen screen = event.getScreen();
        long held = System.currentTimeMillis() - pressStart;
        double mx = event.getMouseX(), my = event.getMouseY();
        double moved = Math.abs(mx-pressMx) + Math.abs(my-pressMy);

        if (dragging) {
            ConfigData.save();
            event.setCanceled(true);
        } else if (held < HOLD_TO_DRAG_MS && moved <= MOVE_TOL) {
            screen.onClose();
            event.setCanceled(true);
        }
        pressStart = 0;
        dragging = false;
    }
}
