package xyz.redking.commanddeck.ui.popups;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.redking.commanddeck.ui.components.CommandDeckButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class ItemPickerUI extends Screen {
    public ItemStack selectedItem = ItemStack.EMPTY;
    public Consumer<ItemStack> onSelectedItem = item -> {
    };
    public Screen previousScreen;

    private EditBox searchBox;
    private int menuX;
    private int menuY;
    private final int menuWidth = 230;
    private final int menuHeight = 210;
    private int scrollOffset = 0;
    private final int buttonSize = 26;

    public ItemPickerUI() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        menuX = (width - menuWidth) / 2;
        menuY = (height - menuHeight) / 2;

        String previousSearch = searchBox == null ? "" : searchBox.getValue();
        searchBox = new EditBox(font, menuX + 10, menuY + 10, menuWidth - 20, 20, Component.empty());
        searchBox.setValue(previousSearch);
        searchBox.setResponder(value -> {
            scrollOffset = 0;
            updateItems();
        });

        updateItems();
    }

    private void updateItems() {
        clearWidgets();
        addRenderableWidget(searchBox);

        List<Item> items = filteredItems();
        int rowSize = 8;
        int startX = menuX + 10;
        int startY = menuY + 40;
        int visibleHeight = menuHeight - 50;

        for (int index = 0; index < items.size(); index++) {
            Item item = items.get(index);
            int row = index / rowSize;
            int col = index % rowSize;
            int btnX = startX + col * buttonSize;
            int btnY = startY + row * buttonSize - scrollOffset;

            if (btnY >= startY && btnY + buttonSize <= startY + visibleHeight) {
                ItemStack stack = item.getDefaultInstance();
                CommandDeckButton button = new CommandDeckButton(stack, ignored -> {
                    selectedItem = stack;
                    onClose();
                });
                button.setX(btnX);
                button.setY(btnY);
                button.setTooltip(Tooltip.create(stack.getHoverName()));
                addRenderableWidget(button);
            }
        }
    }

    private List<Item> filteredItems() {
        String search = searchBox.getValue().toLowerCase(Locale.ROOT);
        List<Item> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item.getName(item.getDefaultInstance()).getString().toLowerCase(Locale.ROOT).contains(search)) {
                items.add(item);
            }
        }
        return items;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int totalRows = (int) Math.ceil(filteredItems().size() / 8.0);
        int totalHeight = totalRows * buttonSize;
        int visibleHeight = menuHeight - 50;
        int maxScroll = Math.max(0, totalHeight - visibleHeight);
        scrollOffset = clamp(scrollOffset - (int) (delta * buttonSize), 0, maxScroll);
        updateItems();
        return true;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, 0xCC000000);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        if (!selectedItem.isEmpty()) {
            onSelectedItem.accept(selectedItem);
        }
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
