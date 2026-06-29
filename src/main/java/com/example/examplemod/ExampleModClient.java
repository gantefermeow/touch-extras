package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.world.item.ShieldItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.lang.reflect.Method;

// ВСЁ клиентское: крестик + щит на присед + бедрок-тапы
@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class ExampleModClient {

    // ============================================================
    // ФИЧА 1: НАРИСОВАННЫЙ КРЕСТИК (X) НА ЛЮБОМ ЭКРАНЕ
    // ============================================================
    private static final int SIZE = 22;
    private static final int PAD  = 6;

    private static int btnX(Screen s) { return s.width - SIZE - PAD; }
    private static int btnY()         { return PAD; }

    private static boolean shouldShow(Screen s) {
        return !(s instanceof TitleScreen) && !(s instanceof PauseScreen);
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();
        if (!shouldShow(screen)) return;

        GuiGraphics g = event.getGuiGraphics();
        int x = btnX(screen);
        int y = btnY();

        int mx = event.getMouseX();
        int my = event.getMouseY();
        boolean hover = mx >= x && mx <= x + SIZE && my >= y && my <= y + SIZE;

        g.fill(x, y, x + SIZE, y + SIZE, hover ? 0xFFCC0000 : 0xCC000000);
        g.renderOutline(x, y, SIZE, SIZE, 0xFFFFFFFF);
        g.drawCenteredString(Minecraft.getInstance().font, "X",
                x + SIZE / 2, y + (SIZE - 8) / 2, 0xFFFFFFFF);
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        Screen screen = event.getScreen();
        if (!shouldShow(screen)) return;
        if (event.getButton() != 0) return;

        double mx = event.getMouseX();
        double my = event.getMouseY();
        int x = btnX(screen);
        int y = btnY();

        if (mx >= x && mx <= x + SIZE && my >= y && my <= y + SIZE) {
            screen.onClose();
            event.setCanceled(true);
        }
    }

    // ============================================================
    // ФИЧА 2: ЩИТ НА СНИК (присел = щит, атака опускает на удар)
    // ============================================================
    @SubscribeEvent
    public static void onShieldTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (mc.screen != null) {
            mc.options.keyUse.setDown(false);
            return;
        }

        boolean mainShield = mc.player.getMainHandItem().getItem() instanceof ShieldItem;
        boolean offShield  = mc.player.getOffhandItem().getItem()  instanceof ShieldItem;
        boolean hasShield  = mainShield || offShield;

        boolean attacking = mc.options.keyAttack.isDown();
        boolean wantBlock = mc.player.isShiftKeyDown() && hasShield && !attacking;

        mc.options.keyUse.setDown(wantBlock);
    }

    // ============================================================
    // ФИЧА 3: БЕДРОК-ТАПЫ (короткий тап = юз/поставить, зажал = ломать)
    // ============================================================
    private static final long HOLD_MS = 180;

    private static boolean prevDown = false;
    private static long    downAt   = 0;
    private static Method  useMethod = null;

    @SubscribeEvent
    public static void onTapTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null || mc.screen != null) {
            prevDown = false;
            return;
        }

        long now = System.currentTimeMillis();
        boolean down = mc.options.keyAttack.isDown();

        if (down && !prevDown) {
            downAt = now;
        }

        if (down) {
            if (now - downAt < HOLD_MS) {
                mc.options.keyAttack.setDown(false); // придерживаем ломку (вдруг тап)
            }
        }

        if (!down && prevDown) {
            long dur = now - downAt;
            if (dur < HOLD_MS) {
                doUse(mc); // короткий тап → используем/ставим
            }
        }

        prevDown = down;
    }

    private static void doUse(Minecraft mc) {
        try {
            if (useMethod == null) {
                for (Method m : Minecraft.class.getDeclaredMethods()) {
                    if (m.getName().equals("startUseItem") && m.getParameterCount() == 0) {
                        m.setAccessible(true);
                        useMethod = m;
                        break;
                    }
                }
            }
            if (useMethod != null) {
                useMethod.invoke(mc);
            }
        } catch (Exception ignored) {}
    }
}
