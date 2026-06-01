package xyz.redking.commanddeck.other;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import xyz.redking.commanddeck.platform.Platform;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ModConfig {
    public enum DisplayRunText {
        ALWAYS,
        KEYBIND_ONLY,
        NEVER
    }

    public int buttonsPerRow = 5;
    public int visibleRows = 2;
    public boolean closeOnKeyReleased = false;
    public boolean hideEditIcon = false;
    public boolean closeOnAction = true;
    public boolean showActionsInTooltip = true;
    public DisplayRunText displayRunText = DisplayRunText.KEYBIND_ONLY;
    public boolean keepNavigationHistory = false;
    public String moveModifier = "key.keyboard.left.control";
    public String deleteModifier = "key.keyboard.left.shift";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = Platform.getConfigDir().resolve("commanddeck.json").toFile();

    public static ModConfig createAndLoad() {
        if (!CONFIG_FILE.exists()) {
            ModConfig config = new ModConfig();
            config.save();
            return config;
        }

        try {
            String content = Files.readString(CONFIG_FILE.toPath(), StandardCharsets.UTF_8);
            ModConfig config = GSON.fromJson(content, ModConfig.class);
            return config != null ? config : new ModConfig();
        } catch (Exception e) {
            e.printStackTrace();
            return new ModConfig();
        }
    }

    public void save() {
        try {
            Files.writeString(CONFIG_FILE.toPath(), GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
