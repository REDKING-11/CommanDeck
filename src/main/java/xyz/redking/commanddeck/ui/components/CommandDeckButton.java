package xyz.redking.commanddeck.ui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class CommandDeckButton extends Button {
    public ItemStack itemIcon;
    public Consumer<CommandDeckButton> onRightClick;
    public boolean isFolder;

    public CommandDeckButton(ItemStack itemIcon, OnPress onPress) {
        this(itemIcon, onPress, button -> {
        }, false);
    }

    public CommandDeckButton(ItemStack itemIcon, OnPress onPress, Consumer<CommandDeckButton> onRightClick, boolean isFolder) {
        super(0, 0, 26, 26, Component.empty(), onPress, DEFAULT_NARRATION);
        this.itemIcon = itemIcon;
        this.onRightClick = onRightClick;
        this.isFolder = isFolder;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        int alpha = !active ? 0x22 : isHovered() ? 0x66 : 0x44;
        int color = (alpha << 24) | 0xFFFFFF;

        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, color);
        int borderColor = isHovered() && active ? 0xAAFFFFFF : 0x22FFFFFF;
        renderOutline(guiGraphics, getX(), getY(), width, height, borderColor);

        if (!itemIcon.isEmpty()) {
            int itemX = getX() + (width - 16) / 2;
            int itemY = getY() + (height - 16) / 2;
            guiGraphics.item(itemIcon, itemX, itemY);
            guiGraphics.itemDecorations(Minecraft.getInstance().font, itemIcon, itemX, itemY);
        }

        if (isFolder) {
            int fx = getX() + 2;
            int fy = getY() + 2;
            int folderColor = 0xFFFFAA00;
            guiGraphics.fill(fx, fy + 2, fx + 8, fy + 7, folderColor);
            guiGraphics.fill(fx, fy + 1, fx + 3, fy + 2, folderColor);
            renderOutline(guiGraphics, fx - 1, fy, 10, 8, 0x88000000);
        }
    }

    private void renderOutline(GuiGraphicsExtractor guiGraphics, int x, int y, int w, int h, int color) {
        guiGraphics.fill(x, y, x + w, y + 1, color);
        guiGraphics.fill(x, y + h - 1, x + w, y + h, color);
        guiGraphics.fill(x, y + 1, x + 1, y + h - 1, color);
        guiGraphics.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (active && visible && isMouseOver(event.x(), event.y()) && event.button() == GLFW.GLFW_MOUSE_BUTTON_2) {
            playDownSound(Minecraft.getInstance().getSoundManager());
            onRightClick.accept(this);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}
