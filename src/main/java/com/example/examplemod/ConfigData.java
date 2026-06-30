package com.example.examplemod;

import net.minecraft.client.Minecraft;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

// Хранилище настроек + сохранение в config/touchextras.properties
public class ConfigData {
    public static boolean crossEnabled = true;
    public static int crossX = -1;   // -1 = авто (правый верхний угол)
    public static int crossY = 6;
    public static int crossSize = 22;

    public static boolean f5Enabled = true;

    private static Path file() {
        return new File(Minecraft.getInstance().gameDirectory,
                "config/touchextras.properties").toPath();
    }

    public static void save() {
        try {
            Properties p = new Properties();
            p.setProperty("crossEnabled", String.valueOf(crossEnabled));
            p.setProperty("crossX", String.valueOf(crossX));
            p.setProperty("crossY", String.valueOf(crossY));
            p.setProperty("crossSize", String.valueOf(crossSize));
            p.setProperty("f5Enabled", String.valueOf(f5Enabled));
            Files.createDirectories(file().getParent());
            try (var out = Files.newOutputStream(file())) {
                p.store(out, "TouchExtras config");
            }
        } catch (Exception ignored) {}
    }

    public static void load() {
        try {
            if (!Files.exists(file())) return;
            Properties p = new Properties();
            try (var in = Files.newInputStream(file())) { p.load(in); }
            crossEnabled = Boolean.parseBoolean(p.getProperty("crossEnabled", "true"));
            crossX = Integer.parseInt(p.getProperty("crossX", "-1"));
            crossY = Integer.parseInt(p.getProperty("crossY", "6"));
            crossSize = Integer.parseInt(p.getProperty("crossSize", "22"));
            f5Enabled = Boolean.parseBoolean(p.getProperty("f5Enabled", "true"));
        } catch (Exception ignored) {}
    }
}