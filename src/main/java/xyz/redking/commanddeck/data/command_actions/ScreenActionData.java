package xyz.redking.commanddeck.data.command_actions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.ChatOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;

public class ScreenActionData extends ActionData {
    public static final String[] SCREENS = {"options", "controls", "video", "sound", "chat", "language", "accessibility", "advancements", "stats"};

    public final String screen;

    public ScreenActionData() {
        this("options");
    }

    public ScreenActionData(String screen) {
        this.screen = screen;
    }

    @Override
    public String getType() {
        return "screen";
    }

    @Override
    public String getValue() {
        return screen;
    }

    @Override
    public String getTypeString() {
        return "GUI";
    }

    @Override
    public String getDisplayString() {
        return label(screen);
    }

    @Override
    public void run() {
        Minecraft client = Minecraft.getInstance();
        var previous = client.screen;
        var options = client.options;

        switch (screen) {
            case "controls" -> client.setScreen(new ControlsScreen(previous, options));
            case "video" -> client.setScreen(new VideoSettingsScreen(previous, client, options));
            case "sound" -> client.setScreen(new SoundOptionsScreen(previous, options));
            case "chat" -> client.setScreen(new ChatOptionsScreen(previous, options));
            case "language" -> client.setScreen(new LanguageSelectScreen(previous, options, client.getLanguageManager()));
            case "accessibility" -> client.setScreen(new AccessibilityOptionsScreen(previous, options));
            case "advancements" -> {
                if (client.player != null && client.player.connection != null) {
                    client.setScreen(new AdvancementsScreen(client.player.connection.getAdvancements(), previous));
                }
            }
            case "stats" -> {
                if (client.player != null) {
                    client.setScreen(new StatsScreen(previous, client.player.getStats()));
                }
            }
            case "chat_input" -> client.setScreen(new ChatScreen("", false));
            default -> client.setScreen(new OptionsScreen(previous, options, false));
        }
    }

    public static String label(String value) {
        return switch (value) {
            case "controls" -> "Controls";
            case "video" -> "Video Settings";
            case "sound" -> "Sound Settings";
            case "chat" -> "Chat Settings";
            case "language" -> "Language";
            case "accessibility" -> "Accessibility";
            case "advancements" -> "Advancements";
            case "stats" -> "Stats";
            case "chat_input" -> "Chat";
            default -> "Options";
        };
    }
}
