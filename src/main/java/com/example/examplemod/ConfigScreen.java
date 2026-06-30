package com.example.examplemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

// Меню настроек с ползунками + живое превью крестика
public class ConfigScreen extends Screen {
    private final Screen parent;

    public ConfigScreen(Screen parent) {
        super(Component.literal("TouchExtras — Настройки"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int w = 200, h = 20, gap = 24;
        int y = 40;

        addRenderableWidget(new IntSlider(cx - w/2, y, w, h, "Размер крестика",
                10, 60, ConfigData.crossSize, v -> ConfigData.crossSize = v));
        y += gap;

        int defX = ConfigData.crossX < 0 ? this.width - 28 : ConfigData.crossX;
        addRenderableWidget(new IntSlider(cx - w/2, y, w, h, "Крестик X",
                0, Math.max(1, this.width - 10), defX, v -> ConfigData.crossX = v));
        y += gap;

        addRenderableWidget(new IntSlider(cx - w/2, y, w, h, "Крестик Y",
                0, Math.max(1, this.height - 10), ConfigData.crossY, v -> ConfigData.crossY = v));
        y += gap;

        addRenderableWidget(Button.builder(crossLabel(), b -> {
            ConfigData.crossEnabled = !ConfigData.crossEnabled;
            b.setMessage(crossLabel());
        }).bounds(cx - w/2, y, w, h).build());
        y += gap;

        addRenderableWidget(Button.builder(f5Label(), b -> {
            ConfigData.f5Enabled = !ConfigData.f5Enabled;
            b.setMessage(f5Label());
        }).bounds(cx - w/2, y, w, h).build());
        y += gap;

        addRenderableWidget(Button.builder(Component.literal("Готово"),
                b -> this.onClose()).bounds(cx - w/2, y, w, h).build());
    }

    private Component crossLabel() {
        return Component.literal("Крестик: " + (ConfigData.crossEnabled ? "ВКЛ" : "ВЫКЛ"));
    }
    private Component f5Label() {
        return Component.literal("F5 (смена вида): " + (ConfigData.f5Enabled ? "ВКЛ" : "ВЫКЛ"));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        super.render(g, mouseX, mouseY, partial);
        g.drawCenteredString(this.font, this.title, this.width/2, 15, 0xFFFFFF);

        // живое превью крестика
        if (ConfigData.crossEnabled) {
            int size = ConfigData.crossSize;
            int x = ConfigData.crossX < 0 ? this.width - size - 6 : ConfigData.crossX;
            int yy = ConfigData.crossY;
            g.fill(x, yy, x + size, yy + size, 0xCC000000);
            g.renderOutline(x, yy, size, size, 0xFFFFFFFF);
            g.drawCenteredString(this.font, "X", x + size/2, yy + (size-8)/2, 0xFFFFFFFF);
        }
    }

    @Override
    public void onClose() {
        ConfigData.save();
        this.minecraft.setScreen(parent);
    }

    // Простой слайдер целых чисел
    private static class IntSlider extends AbstractSliderButton {
        private final int min, max;
        private final String label;
        private final IntConsumer onChange;

        IntSlider(int x, int y, int w, int h, String label,
                  int min, int max, int val, IntConsumer onChange) {
            super(x, y, w, h, Component.empty(),
                    (double)(val - min) / Math.max(1, (max - min)));
            this.min = min; this.max = max;
            this.label = label; this.onChange = onChange;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(label + ": " + cur()));
        }
        @Override
        protected void applyValue() {
            onChange.accept(cur());
        }
        private int cur() {
            return (int) Math.round(min + value * (max - min));
        }
    }
}