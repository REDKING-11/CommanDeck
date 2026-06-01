package xyz.redking.commanddeck.ui.popups;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import xyz.redking.commanddeck.other.KeybindHandler;

import java.util.function.Consumer;

public class KeybindPickerUI extends Screen {
    public Consumer<KeyMapping> onSelectedKeybind = mapping -> {
    };
    public Screen previousScreen;

    private int menuX;
    private int menuY;
    private final int menuWidth = 320;
    private final int menuHeight = 240;
    private int scrollOffset = 0;
    private final int entryHeight = 22;

    public KeybindPickerUI() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        menuX = (width - menuWidth) / 2;
        menuY = (height - menuHeight) / 2;
        rebuildList();
    }

    private void rebuildList() {
        clearWidgets();
        KeyMapping[] keyBindings = KeybindHandler.getKeybindings();
        int startX = menuX + 10;
        int startY = menuY + 10;

        for (int index = 0; index < keyBindings.length; index++) {
            KeyMapping keyBinding = keyBindings[index];
            int btnY = startY + index * entryHeight - scrollOffset;
            if (btnY >= startY && btnY + 20 <= startY + menuHeight - 20) {
                addRenderableWidget(Button.builder(Component.translatable(keyBinding.getName()), button -> {
                            onSelectedKeybind.accept(keyBinding);
                            onClose();
                        })
                        .pos(startX, btnY)
                        .size(menuWidth - 20, 20)
                        .build());
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        KeyMapping[] keyBindings = KeybindHandler.getKeybindings();
        int maxScroll = Math.max(0, keyBindings.length * entryHeight - (menuHeight - 20));
        scrollOffset = clamp(scrollOffset - (int) (verticalAmount * entryHeight), 0, maxScroll);
        rebuildList();
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, 0xCC000000);
        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(previousScreen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
