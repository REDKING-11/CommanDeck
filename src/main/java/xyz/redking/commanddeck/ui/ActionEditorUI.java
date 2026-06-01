package xyz.redking.commanddeck.ui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
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
    private final int menuWidth = 260;
    private final int menuHeight = 180;

    public ActionEditorUI(ActionButtonData originalAction) {
        super(Component.translatable("menu.editor.title"));
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

        nameEditBox = new EditBox(font, menuX + 100, menuY + 40, 140, 20, Component.empty());
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
        iconButton.setX(menuX + 100);
        iconButton.setY(menuY + 68);
        addRenderableWidget(iconButton);

        customModelDataEditBox = new EditBox(font, menuX + 100, menuY + 98, 140, 20, Component.empty());
        customModelDataEditBox.setValue(getCustomModelData(actionButtonData.icon));
        customModelDataEditBox.setResponder(value -> updateCustomModelData());
        addRenderableWidget(customModelDataEditBox);

        keybindBtn = Button.builder(Component.empty(), button -> {
                    syncInputToData();
                    settingKeybind = true;
                    updateKeybindLabel();
                })
                .pos(menuX + 100, menuY + 123)
                .size(140, 20)
                .build();
        addRenderableWidget(keybindBtn);
        updateKeybindLabel();

        addRenderableWidget(Checkbox.builder(Component.literal("Folder"), font)
                .pos(menuX + 10, menuY + 152)
                .selected(actionButtonData.isFolder)
                .onValueChange((checkbox, value) -> toggleFolderMode(value))
                .build());

        if (!actionButtonData.isFolder) {
            addRenderableWidget(Button.builder(Component.literal("Edit Actions >"), button -> {
                        syncInputToData();
                        ActionListEditorUI listEditor = new ActionListEditorUI(actionButtonData);
                        listEditor.previousScreen = this;
                        minecraft.setScreen(listEditor);
                    })
                    .pos(menuX + 100, menuY + 150)
                    .size(140, 20)
                    .build());
        }

        int footerY = menuY + menuHeight + 5;
        addRenderableWidget(Button.builder(Component.translatable("menu.editor.button.finish"), button -> saveAndClose())
                .pos(menuX + 40, footerY)
                .size(80, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("menu.editor.button.cancel"), button -> onClose())
                .pos(menuX + 140, footerY)
                .size(80, 20)
                .build());
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
        var cmd = item.get(DataComponents.CUSTOM_MODEL_DATA);
        if (cmd == null || cmd.strings().isEmpty()) {
            return "";
        }
        return cmd.strings().get(0);
    }

    private void updateCustomModelData() {
        if (customModelDataEditBox == null || actionButtonData.icon.isEmpty()) {
            return;
        }
        String text = customModelDataEditBox.getValue();
        try {
            if (!text.isEmpty()) {
                actionButtonData.icon.set(DataComponents.CUSTOM_MODEL_DATA, new ActionButtonData.CustomModelDataValues(text).getComponent());
            } else {
                actionButtonData.icon.remove(DataComponents.CUSTOM_MODEL_DATA);
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
    public boolean keyPressed(KeyEvent event) {
        if (settingKeybind) {
            if (event.key() != GLFW.GLFW_KEY_ESCAPE) {
                if (event.key() != GLFW.GLFW_KEY_LEFT_CONTROL && event.key() != GLFW.GLFW_KEY_RIGHT_CONTROL
                        && event.key() != GLFW.GLFW_KEY_LEFT_SHIFT && event.key() != GLFW.GLFW_KEY_RIGHT_SHIFT
                        && event.key() != GLFW.GLFW_KEY_LEFT_ALT && event.key() != GLFW.GLFW_KEY_RIGHT_ALT) {
                    isBoundKeybind = true;
                    keybind.clear();
                    keybind.add(event.key());
                    keybind.add(event.scancode());
                    keybind.add(event.modifiers());
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
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (settingKeybind) {
            if (event.button() <= 2) {
                isBoundKeybind = true;
                keybind.clear();
                keybind.add(event.button());
                keybind.add(0);
                keybind.add(0);
                keybind.add(1);
            }
            settingKeybind = false;
            updateKeybindLabel();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(menuX - 1, menuY - 1, menuX + menuWidth + 1, menuY + menuHeight + 1, 0x44000000);
        guiGraphics.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, 0x77121212);
        renderBorder(guiGraphics);
        guiGraphics.fill(menuX + 5, menuY + 5, menuX + menuWidth - 5, menuY + 29, 0xDD141414);
        guiGraphics.text(font, title, menuX + (menuWidth - font.width(title)) / 2, menuY + 12, -1, true);
        guiGraphics.fill(menuX + 10, menuY + 66, menuX + 82, menuY + 67, 0x66FFFFFF);
        guiGraphics.fill(menuX + 10, menuY + 148, menuX + 82, menuY + 149, 0x66FFFFFF);
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.text(font, Component.translatable("menu.editor.property.name"), menuX + 10, menuY + 45, 0xFFFFFFFF, true);
        guiGraphics.text(font, Component.translatable("menu.editor.property.icon"), menuX + 10, menuY + 73, 0xFFFFFFFF, true);
        guiGraphics.text(font, "CustomModelData", menuX + 10, menuY + 103, 0xFFFFFFFF, true);
        guiGraphics.text(font, Component.translatable("menu.editor.property.keybind"), menuX + 10, menuY + 128, 0xFFFFFFFF, true);
    }

    private void renderBorder(GuiGraphicsExtractor guiGraphics) {
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
        public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.fill(0, 0, width, height, 0xCC000000);
            guiGraphics.centeredText(font, title, width / 2, height / 2 - 30, -1);
            guiGraphics.centeredText(font, message, width / 2, height / 2 - 15, -1);
            super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        }
    }
}
