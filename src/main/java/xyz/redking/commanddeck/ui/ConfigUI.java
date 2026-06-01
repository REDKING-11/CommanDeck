package xyz.redking.commanddeck.ui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import xyz.redking.commanddeck.CommandDeck;
import xyz.redking.commanddeck.other.ModConfig;

public class ConfigUI extends Screen {
    private final Screen parent;
    private final ModConfig config = CommandDeck.CONFIG;

    public ConfigUI(Screen parent) {
        super(Component.translatable("text.config.commanddeck.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = 220;
        int buttonHeight = 20;
        int x = (width - buttonWidth) / 2;
        int y = 42;

        addOptionButton(x, y, buttonWidth, buttonHeight, "text.config.commanddeck.option.buttonsPerRow", Integer.toString(config.buttonsPerRow), () -> {
            config.buttonsPerRow = config.buttonsPerRow >= 15 ? 1 : config.buttonsPerRow + 1;
            rebuildWidgets();
        });
        y += 24;

        addOptionButton(x, y, buttonWidth, buttonHeight, "text.config.commanddeck.option.visibleRows", Integer.toString(config.visibleRows), () -> {
            config.visibleRows = config.visibleRows >= 10 ? 1 : config.visibleRows + 1;
            rebuildWidgets();
        });
        y += 24;

        addToggleButton(x, y, buttonWidth, buttonHeight, "text.config.commanddeck.option.closeOnKeyReleased", config.closeOnKeyReleased, () -> {
            config.closeOnKeyReleased = !config.closeOnKeyReleased;
            rebuildWidgets();
        });
        y += 24;

        addToggleButton(x, y, buttonWidth, buttonHeight, "text.config.commanddeck.option.hideEditIcon", config.hideEditIcon, () -> {
            config.hideEditIcon = !config.hideEditIcon;
            rebuildWidgets();
        });
        y += 24;

        addToggleButton(x, y, buttonWidth, buttonHeight, "text.config.commanddeck.option.keepNavigationHistory", config.keepNavigationHistory, () -> {
            config.keepNavigationHistory = !config.keepNavigationHistory;
            rebuildWidgets();
        });
        y += 24;

        addToggleButton(x, y, buttonWidth, buttonHeight, "text.config.commanddeck.option.closeOnAction", config.closeOnAction, () -> {
            config.closeOnAction = !config.closeOnAction;
            rebuildWidgets();
        });
        y += 24;

        addToggleButton(x, y, buttonWidth, buttonHeight, "text.config.commanddeck.option.showActionsInTooltip", config.showActionsInTooltip, () -> {
            config.showActionsInTooltip = !config.showActionsInTooltip;
            rebuildWidgets();
        });
        y += 24;

        addOptionButton(x, y, buttonWidth, buttonHeight, "text.config.commanddeck.option.displayRunText", displayRunTextLabel(), () -> {
            config.displayRunText = switch (config.displayRunText) {
                case ALWAYS -> ModConfig.DisplayRunText.KEYBIND_ONLY;
                case KEYBIND_ONLY -> ModConfig.DisplayRunText.NEVER;
                case NEVER -> ModConfig.DisplayRunText.ALWAYS;
            };
            rebuildWidgets();
        });
        y += 24;

        addOptionButton(x, y, buttonWidth, buttonHeight, "text.config.commanddeck.option.moveModifier", keyLabel(config.moveModifier), () -> {
            config.moveModifier = nextModifier(config.moveModifier);
            rebuildWidgets();
        });
        y += 24;

        addOptionButton(x, y, buttonWidth, buttonHeight, "text.config.commanddeck.option.deleteModifier", keyLabel(config.deleteModifier), () -> {
            config.deleteModifier = nextModifier(config.deleteModifier);
            rebuildWidgets();
        });

        addRenderableWidget(Button.builder(Component.translatable("menu.editor.button.finish"), button -> onClose())
                .pos((width - 100) / 2, height - 32)
                .size(100, buttonHeight)
                .build());
    }

    private void addToggleButton(int x, int y, int width, int height, String key, boolean value, Runnable onPress) {
        addOptionButton(x, y, width, height, key, value ? "ON" : "OFF", onPress);
    }

    private void addOptionButton(int x, int y, int width, int height, String key, String value, Runnable onPress) {
        addRenderableWidget(Button.builder(Component.literal(Component.translatable(key).getString() + ": " + value), button -> onPress.run())
                .pos(x, y)
                .size(width, height)
                .build());
    }

    private String displayRunTextLabel() {
        return switch (config.displayRunText) {
            case ALWAYS -> "Always";
            case KEYBIND_ONLY -> "Keybind Only";
            case NEVER -> "Never";
        };
    }

    private String keyLabel(String keyName) {
        return InputConstants.getKey(keyName).getDisplayName().getString();
    }

    private String nextModifier(String current) {
        return switch (current) {
            case "key.keyboard.left.control" -> "key.keyboard.left.shift";
            case "key.keyboard.left.shift" -> "key.keyboard.left.alt";
            default -> "key.keyboard.left.control";
        };
    }

    @Override
    public void onClose() {
        config.save();
        minecraft.setScreen(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.centeredText(font, title, width / 2, 20, 0xFFFFFFFF);
    }
}
