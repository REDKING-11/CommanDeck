package xyz.redking.commanddeck.ui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import xyz.redking.commanddeck.data.ActionButtonData;
import xyz.redking.commanddeck.other.ActionButtonDataHandler;
import xyz.redking.commanddeck.ui.components.CommandDeckButton;
import xyz.redking.commanddeck.ui.popups.ItemPickerUI;

import java.util.ArrayList;
import java.util.List;

public class ActionEditorUI extends Screen {
    public Screen previousScreen;

    private final ActionButtonData originalAction;
    private ActionButtonData actionButtonData = new ActionButtonData();
    private boolean isNewAction = true;
    private EditBox nameEditBox;
    private EditBox customModelDataEditBox;
    private Button keybindBtn;
    private boolean settingKeybind = false;
    private boolean isBoundKeybind = false;
    private final List<Integer> keybind = new ArrayList<>();
    private int menuX;
    private int menuY;
    private final int menuWidth = 300;
    private final int menuHeight = 180;
    private final int labelXOffset = 10;
    private final int inputXOffset = 125;
    private final int inputWidth = 160;

    public ActionEditorUI(ActionButtonData originalAction) {
        super(Component.literal("Action Editor"));
        this.originalAction = originalAction;
        if (originalAction != null) {
            actionButtonData = originalAction.copy();
            keybind.addAll(originalAction.keybind);
            isBoundKeybind = keybind.size() >= 4;
            isNewAction = false;
        }
    }

    @Override
    protected void init() {
        menuX = (width - menuWidth) / 2;
        menuY = (height - (menuHeight + 40)) / 2;

        nameEditBox = new EditBox(font, inputX(), menuY + 40, inputWidth, 20, Component.empty());
        nameEditBox.setValue(actionButtonData.name);
        addRenderableWidget(nameEditBox);

        CommandDeckButton iconButton = new CommandDeckButton(actionButtonData.icon, button -> {
            syncInputToData();
            ItemPickerUI itemPicker = new ItemPickerUI();
            itemPicker.previousScreen = this;
            itemPicker.onSelectedItem = item -> {
                if (item.isEmpty()) {
                    return;
                }
                actionButtonData.icon = item;
                rebuildWidgets();
            };
            minecraft.setScreen(itemPicker);
        });
        iconButton.setX(inputX());
        iconButton.setY(menuY + 68);
        addRenderableWidget(iconButton);

        customModelDataEditBox = new EditBox(font, inputX(), menuY + 98, inputWidth, 20, Component.empty());
        customModelDataEditBox.setValue(getCustomModelData(actionButtonData.icon));
        customModelDataEditBox.setResponder(value -> updateCustomModelData());
        addRenderableWidget(customModelDataEditBox);

        keybindBtn = Button.builder(Component.empty(), button -> {
                    syncInputToData();
                    settingKeybind = true;
                    updateKeybindLabel();
                })
                .pos(inputX(), menuY + 123)
                .size(inputWidth, 20)
                .build();
        addRenderableWidget(keybindBtn);
        updateKeybindLabel();

        addRenderableWidget(new Checkbox(labelX(), menuY + 152, 100, 20, Component.literal("Folder"), actionButtonData.isFolder) {
            @Override
            public void onPress() {
                super.onPress();
                toggleFolderMode(selected());
            }
        });

        if (!actionButtonData.isFolder) {
            addRenderableWidget(Button.builder(Component.literal("Edit Actions >"), button -> {
                        syncInputToData();
                        ActionListEditorUI listEditor = new ActionListEditorUI(actionButtonData);
                        listEditor.previousScreen = this;
                        minecraft.setScreen(listEditor);
                    })
                    .pos(inputX(), menuY + 150)
                    .size(inputWidth, 20)
                    .build());
        }

        int footerY = menuY + menuHeight + 5;
        addRenderableWidget(Button.builder(Component.literal("Finish"), button -> saveAndClose())
                .pos(menuX + 65, footerY)
                .size(80, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> onClose())
                .pos(menuX + 155, footerY)
                .size(80, 20)
                .build());
    }

    private int labelX() {
        return menuX + labelXOffset;
    }

    private int inputX() {
        return menuX + inputXOffset;
    }

    private void toggleFolderMode(boolean isFolder) {
        if (!isFolder && !actionButtonData.children.isEmpty()) {
            ConfirmScreen warningScreen = new ConfirmScreen(confirmed -> {
                if (confirmed) {
                    actionButtonData.isFolder = false;
                    rebuildWidgets();
                }
                minecraft.setScreen(this);
            }, Component.literal("Convert to Action"), Component.literal("This folder has items. Move them to the parent list?"));
            minecraft.setScreen(warningScreen);
        } else {
            actionButtonData.isFolder = isFolder;
            rebuildWidgets();
        }
    }

    private void syncInputToData() {
        if (nameEditBox != null) {
            actionButtonData.name = nameEditBox.getValue();
        }
        updateCustomModelData();
    }

    private void updateKeybindLabel() {
        String msg;
        if (!isBoundKeybind) {
            msg = "Not Bound";
        } else if (keybind.get(3) == 0) {
            String base = InputConstants.Type.KEYSYM.getOrCreate(keybind.get(0)).getDisplayName().getString();
            int mods = keybind.get(2);
            StringBuilder sb = new StringBuilder();
            if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) sb.append("Ctrl+");
            if ((mods & GLFW.GLFW_MOD_SHIFT) != 0) sb.append("Shift+");
            if ((mods & GLFW.GLFW_MOD_ALT) != 0) sb.append("Alt+");
            sb.append(base);
            msg = sb.toString();
        } else {
            msg = "Mouse " + keybind.get(0);
        }

        if (settingKeybind) {
            msg = "> " + msg + " <";
        }
        keybindBtn.setMessage(Component.literal(msg));
    }

    private String getCustomModelData(ItemStack item) {
        if (item.isEmpty()) {
            return "";
        }
        CompoundTag tag = item.getTag();
        if (tag == null || !tag.contains("CustomModelData")) {
            return "";
        }
        return Integer.toString(tag.getInt("CustomModelData"));
    }

    private void updateCustomModelData() {
        if (customModelDataEditBox == null || actionButtonData.icon.isEmpty()) {
            return;
        }
        String text = customModelDataEditBox.getValue();
        try {
            if (!text.isEmpty()) {
                actionButtonData.icon.getOrCreateTag().putInt("CustomModelData", Integer.parseInt(text));
            } else {
                actionButtonData.icon.removeTagKey("CustomModelData");
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void rebuildWidgets() {
        clearWidgets();
        init();
    }

    private void saveAndClose() {
        syncInputToData();
        actionButtonData.keybind = isBoundKeybind ? new ArrayList<>(keybind) : new ArrayList<>();
        List<ActionButtonData> parentList = MainUI.currentFolder() != null ? MainUI.currentFolder().children : ActionButtonDataHandler.actions;

        if (isNewAction) {
            parentList.add(actionButtonData);
        } else if (originalAction != null) {
            originalAction.name = actionButtonData.name;
            originalAction.actions = actionButtonData.actions;
            originalAction.icon = actionButtonData.icon;
            originalAction.keybind = actionButtonData.keybind;

            if (originalAction.isFolder && !actionButtonData.isFolder && !actionButtonData.children.isEmpty()) {
                int index = parentList.indexOf(originalAction);
                if (index != -1) {
                    parentList.addAll(index + 1, actionButtonData.children);
                } else {
                    parentList.addAll(actionButtonData.children);
                }
                actionButtonData.children.clear();
            }

            originalAction.isFolder = actionButtonData.isFolder;
            originalAction.children = actionButtonData.children;
        }

        ActionButtonDataHandler.save();
        onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (settingKeybind) {
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                if (keyCode != GLFW.GLFW_KEY_LEFT_CONTROL && keyCode != GLFW.GLFW_KEY_RIGHT_CONTROL
                        && keyCode != GLFW.GLFW_KEY_LEFT_SHIFT && keyCode != GLFW.GLFW_KEY_RIGHT_SHIFT
                        && keyCode != GLFW.GLFW_KEY_LEFT_ALT && keyCode != GLFW.GLFW_KEY_RIGHT_ALT) {
                    isBoundKeybind = true;
                    keybind.clear();
                    keybind.add(keyCode);
                    keybind.add(scanCode);
                    keybind.add(modifiers);
                    keybind.add(0);
                    settingKeybind = false;
                    updateKeybindLabel();
                }
            } else {
                isBoundKeybind = false;
                settingKeybind = false;
                updateKeybindLabel();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (settingKeybind) {
            if (button <= 2) {
                isBoundKeybind = true;
                keybind.clear();
                keybind.add(button);
                keybind.add(0);
                keybind.add(0);
                keybind.add(1);
            }
            settingKeybind = false;
            updateKeybindLabel();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(menuX - 1, menuY - 1, menuX + menuWidth + 1, menuY + menuHeight + 1, 0x44000000);
        guiGraphics.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, 0x77121212);
        renderBorder(guiGraphics);
        guiGraphics.fill(menuX + 5, menuY + 5, menuX + menuWidth - 5, menuY + 29, 0xDD141414);
        guiGraphics.drawString(font, title, menuX + (menuWidth - font.width(title)) / 2, menuY + 12, -1, true);
        guiGraphics.fill(labelX(), menuY + 66, inputX() - 10, menuY + 67, 0x66FFFFFF);
        guiGraphics.fill(labelX(), menuY + 148, inputX() - 10, menuY + 149, 0x66FFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(font, Component.literal("Name"), labelX(), menuY + 45, 0xFFFFFFFF, true);
        guiGraphics.drawString(font, Component.literal("Icon"), labelX(), menuY + 73, 0xFFFFFFFF, true);
        guiGraphics.drawString(font, Component.literal("CustomModelData"), labelX(), menuY + 103, 0xFFFFFFFF, true);
        guiGraphics.drawString(font, Component.literal("Keybind"), labelX(), menuY + 128, 0xFFFFFFFF, true);
    }

    private void renderBorder(GuiGraphics guiGraphics) {
        int color = 0xAA000000;
        guiGraphics.fill(menuX, menuY, menuX + menuWidth, menuY + 1, color);
        guiGraphics.fill(menuX, menuY + menuHeight - 1, menuX + menuWidth, menuY + menuHeight, color);
        guiGraphics.fill(menuX, menuY, menuX + 1, menuY + menuHeight, color);
        guiGraphics.fill(menuX + menuWidth - 1, menuY, menuX + menuWidth, menuY + menuHeight, color);
        guiGraphics.fill(menuX + 1, menuY + 1, menuX + menuWidth - 1, menuY + 2, 0x33FFFFFF);
        guiGraphics.fill(menuX + 1, menuY + 1, menuX + 2, menuY + menuHeight - 1, 0x33FFFFFF);
    }

    @Override
    public void onClose() {
        if (previousScreen != null) {
            minecraft.setScreen(previousScreen);
        } else {
            super.onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static class ConfirmScreen extends Screen {
        private final java.util.function.Consumer<Boolean> callback;
        private final Component message;

        public ConfirmScreen(java.util.function.Consumer<Boolean> callback, Component title, Component message) {
            super(title);
            this.callback = callback;
            this.message = message;
        }

        @Override
        protected void init() {
            addRenderableWidget(Button.builder(Component.literal("Yes"), button -> callback.accept(true))
                    .pos(width / 2 - 105, height / 2 + 10)
                    .size(100, 20)
                    .build());
            addRenderableWidget(Button.builder(Component.literal("No"), button -> callback.accept(false))
                    .pos(width / 2 + 5, height / 2 + 10)
                    .size(100, 20)
                    .build());
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.fill(0, 0, width, height, 0xCC000000);
            guiGraphics.drawCenteredString(font, title, width / 2, height / 2 - 30, -1);
            guiGraphics.drawCenteredString(font, message, width / 2, height / 2 - 15, -1);
            super.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }
}
