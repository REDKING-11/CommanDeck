package xyz.redking.commanddeck.ui.popups;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public class BreadcrumbPopupUI extends Screen {
    public record OmittedFolder(int level, String name) {
    }

    private final List<OmittedFolder> omittedFolders;
    private final Consumer<Integer> onSelect;
    private final Screen previousScreen;
    private int menuX;
    private int menuY;
    private int menuWidth = 150;
    private int menuHeight;
    private final int rowHeight = 22;

    public BreadcrumbPopupUI(List<OmittedFolder> omittedFolders, Consumer<Integer> onSelect, Screen previousScreen) {
        super(Component.empty());
        this.omittedFolders = omittedFolders;
        this.onSelect = onSelect;
        this.previousScreen = previousScreen;
    }

    @Override
    protected void init() {
        menuHeight = Math.min(omittedFolders.size() * rowHeight + 20, height - 40);
        int maxTextWidth = 80;
        for (OmittedFolder folder : omittedFolders) {
            maxTextWidth = Math.max(maxTextWidth, font.width(folder.name()));
        }
        menuWidth = Math.max(120, maxTextWidth + 40);
        menuX = (width - menuWidth) / 2;
        menuY = (height - menuHeight) / 2;

        for (int i = 0; i < omittedFolders.size(); i++) {
            OmittedFolder folder = omittedFolders.get(i);
            int btnY = menuY + 10 + i * rowHeight;
            if (btnY + 20 < menuY + menuHeight) {
                addRenderableWidget(Button.builder(Component.literal(folder.name()), button -> {
                            onSelect.accept(folder.level());
                            onClose();
                        })
                        .pos(menuX + 10, btnY)
                        .size(menuWidth - 20, 20)
                        .build());
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(menuX - 1, menuY - 1, menuX + menuWidth + 1, menuY + menuHeight + 1, 0x44000000);
        guiGraphics.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, 0xEE121212);
        int color = 0x44FFFFFF;
        guiGraphics.fill(menuX, menuY, menuX + menuWidth, menuY + 1, color);
        guiGraphics.fill(menuX, menuY + menuHeight - 1, menuX + menuWidth, menuY + menuHeight, color);
        guiGraphics.fill(menuX, menuY, menuX + 1, menuY + menuHeight, color);
        guiGraphics.fill(menuX + menuWidth - 1, menuY, menuX + menuWidth, menuY + menuHeight, color);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(previousScreen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
