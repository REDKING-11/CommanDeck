package xyz.redking.commanddeck.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import xyz.redking.commanddeck.data.ActionButtonData;
import xyz.redking.commanddeck.data.command_actions.ActionData;
import xyz.redking.commanddeck.data.command_actions.CommandActionData;
import xyz.redking.commanddeck.data.command_actions.ConditionActionData;
import xyz.redking.commanddeck.data.command_actions.DelayActionData;
import xyz.redking.commanddeck.data.command_actions.FolderActionData;
import xyz.redking.commanddeck.data.command_actions.KeybindActionData;
import xyz.redking.commanddeck.data.command_actions.ScreenActionData;
import xyz.redking.commanddeck.data.command_actions.SoundActionData;
import xyz.redking.commanddeck.data.command_actions.UrlActionData;
import xyz.redking.commanddeck.ui.popups.ActionPickerUI;
import xyz.redking.commanddeck.ui.popups.KeybindPickerUI;

import java.util.ArrayList;
import java.util.List;

public class ActionListEditorUI extends Screen {
    public ActionEditorUI previousScreen;

    private final ActionButtonData actionButtonData;
    private int menuX;
    private int menuY;
    private final int menuWidth = 260;
    private final int menuHeight = 180;
    private int scrollOffset = 0;
    private final int actionRowHeight = 25;
    private boolean isDraggingScrollbar = false;
    private final List<CommandSuggestions> commandSuggestions = new ArrayList<>();

    public ActionListEditorUI(ActionButtonData actionButtonData) {
        super(Component.literal("Edit Actions"));
        this.actionButtonData = actionButtonData;
    }

    @Override
    protected void init() {
        menuX = (width - menuWidth) / 2;
        menuY = (height - (menuHeight + 40)) / 2;

        rebuildActionList();
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .pos(menuX + (menuWidth - 80) / 2, menuY + menuHeight + 10)
                .size(80, 20)
                .build());
    }

    private void rebuildActionList() {
        clearWidgets();
        commandSuggestions.clear();
        int listStartY = menuY + 10;
        int maxVisibleHeight = menuHeight - 20;

        for (int index = 0; index < actionButtonData.actions.size(); index++) {
            int currentIndex = index;
            ActionData action = actionButtonData.actions.get(index);
            int rowY = listStartY + index * actionRowHeight - scrollOffset;

            if (rowY >= listStartY && rowY + 20 <= listStartY + maxVisibleHeight) {
                if (action instanceof CommandActionData commandAction) {
                    EditBox cmdBox = new EditBox(font, menuX + 10, rowY, 190, 20, Component.empty());
                    updateLimit(cmdBox, commandAction.command);
                    cmdBox.setHint(Component.literal("/command or chat message"));
                    cmdBox.setValue(commandAction.command);
                    cmdBox.setResponder(text -> {
                        updateLimit(cmdBox, text);
                        actionButtonData.actions.set(currentIndex, new CommandActionData(text));
                        updateCommandSuggestions();
                    });
                    addRenderableWidget(cmdBox);
                    attachCommandSuggestions(cmdBox);
                } else if (action instanceof KeybindActionData keybindAction) {
                    addRenderableWidget(Button.builder(Component.literal("Key: " + Component.translatable(keybindAction.translationKey).getString()), button -> {
                                KeybindPickerUI picker = new KeybindPickerUI();
                                picker.previousScreen = this;
                                picker.onSelectedKeybind = mapping -> {
                                    actionButtonData.actions.set(currentIndex, new KeybindActionData(mapping.getName()));
                                    init();
                                };
                                minecraft.setScreen(picker);
                            })
                            .pos(menuX + 10, rowY)
                            .size(190, 20)
                            .build());
                } else if (action instanceof UrlActionData urlAction) {
                    EditBox urlBox = new EditBox(font, menuX + 10, rowY, 190, 20, Component.empty());
                    urlBox.setMaxLength(512);
                    urlBox.setHint(Component.literal("https://example.com"));
                    urlBox.setValue(urlAction.url);
                    urlBox.setResponder(text -> actionButtonData.actions.set(currentIndex, new UrlActionData(text)));
                    addRenderableWidget(urlBox);
                } else if (action instanceof DelayActionData delayAction) {
                    EditBox delayBox = new EditBox(font, menuX + 10, rowY, 190, 20, Component.empty());
                    delayBox.setMaxLength(6);
                    delayBox.setHint(Component.literal("ticks, 20 = 1 second"));
                    delayBox.setValue(delayAction.ticks);
                    delayBox.setResponder(text -> actionButtonData.actions.set(currentIndex, new DelayActionData(text)));
                    addRenderableWidget(delayBox);
                } else if (action instanceof FolderActionData folderAction) {
                    EditBox folderBox = new EditBox(font, menuX + 10, rowY, 190, 20, Component.empty());
                    folderBox.setMaxLength(256);
                    folderBox.setHint(Component.literal("Folder/Subfolder"));
                    folderBox.setValue(folderAction.path);
                    folderBox.setResponder(text -> actionButtonData.actions.set(currentIndex, new FolderActionData(text)));
                    addRenderableWidget(folderBox);
                } else if (action instanceof ScreenActionData screenAction) {
                    addRenderableWidget(Button.builder(Component.literal("Screen: " + ScreenActionData.label(screenAction.screen)), button -> {
                                actionButtonData.actions.set(currentIndex, new ScreenActionData(nextValue(ScreenActionData.SCREENS, screenAction.screen)));
                                init();
                            })
                            .pos(menuX + 10, rowY)
                            .size(190, 20)
                            .build());
                } else if (action instanceof ConditionActionData conditionAction) {
                    addRenderableWidget(Button.builder(Component.literal(ConditionActionData.label(conditionAction.condition)), button -> {
                                actionButtonData.actions.set(currentIndex, new ConditionActionData(nextValue(ConditionActionData.CONDITIONS, conditionAction.condition)));
                                init();
                            })
                            .pos(menuX + 10, rowY)
                            .size(190, 20)
                            .build());
                } else if (action instanceof SoundActionData) {
                    addRenderableWidget(Button.builder(Component.literal("Ding Sound"), button -> {
                            })
                            .pos(menuX + 10, rowY)
                            .size(190, 20)
                            .build());
                }

                addRenderableWidget(Button.builder(Component.literal("-"), button -> {
                            actionButtonData.actions.remove(currentIndex);
                            init();
                        })
                        .pos(menuX + 205, rowY)
                        .size(20, 20)
                        .build());
            }
        }

        int addActionY = listStartY + actionButtonData.actions.size() * actionRowHeight - scrollOffset;
        if (addActionY >= listStartY && addActionY + 20 <= listStartY + maxVisibleHeight) {
            addRenderableWidget(Button.builder(Component.literal("+ Add Action"), button -> {
                        ActionPickerUI picker = new ActionPickerUI();
                        picker.previousScreen = this;
                        picker.onSelectedAction = selected -> {
                            actionButtonData.actions.add(selected);
                            init();
                        };
                        minecraft.setScreen(picker);
                    })
                    .pos(menuX + 10, addActionY)
                    .size(100, 20)
                    .build());
        }
    }

    private void updateLimit(EditBox box, String text) {
        box.setMaxLength(text.startsWith("/") ? 32767 : 256);
    }

    private void attachCommandSuggestions(EditBox box) {
        CommandSuggestions suggestions = new CommandSuggestions(minecraft, this, box, font, false, false, 1, 10, true, 0xCC000000);
        suggestions.setAllowHiding(true);
        suggestions.setAllowSuggestions(true);
        if (minecraft.player != null) {
            var chatAbilities = minecraft.player.chatAbilities();
            suggestions.setRestrictions(chatAbilities.canSendMessages(), chatAbilities.canSendCommands());
        }
        suggestions.updateCommandInfo();
        commandSuggestions.add(suggestions);
    }

    private void updateCommandSuggestions() {
        for (CommandSuggestions suggestions : commandSuggestions) {
            suggestions.setAllowSuggestions(true);
            suggestions.updateCommandInfo();
        }
    }

    private String nextValue(String[] values, String current) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(current)) {
                return values[(i + 1) % values.length];
            }
        }
        return values[0];
    }

    private boolean isMouseOverScrollbar(double mouseX, double mouseY) {
        int totalHeight = (actionButtonData.actions.size() + 1) * actionRowHeight;
        if (totalHeight <= menuHeight - 20) {
            return false;
        }
        int sbX = menuX + menuWidth - 8;
        int sbY = menuY + 10;
        return mouseX >= sbX && mouseX <= sbX + 4 && mouseY >= sbY && mouseY <= sbY + (menuHeight - 20);
    }

    private void updateScrollFromMouse(double mouseY) {
        int totalHeight = (actionButtonData.actions.size() + 1) * actionRowHeight;
        int visibleHeight = menuHeight - 20;
        int maxScroll = Math.max(0, totalHeight - visibleHeight);
        int sbY = menuY + 10;
        double percentage = clamp((mouseY - sbY) / (double) visibleHeight, 0.0, 1.0);
        scrollOffset = ((int) (percentage * maxScroll) / actionRowHeight) * actionRowHeight;
        scrollOffset = clamp(scrollOffset, 0, maxScroll);
        init();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        for (CommandSuggestions suggestions : commandSuggestions) {
            if (suggestions.keyPressed(event)) {
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        for (CommandSuggestions suggestions : commandSuggestions) {
            if (suggestions.mouseClicked(event)) {
                return true;
            }
        }

        if (isMouseOverScrollbar(event.x(), event.y())) {
            isDraggingScrollbar = true;
            updateScrollFromMouse(event.y());
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (isDraggingScrollbar) {
            updateScrollFromMouse(event.y());
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        isDraggingScrollbar = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (CommandSuggestions suggestions : commandSuggestions) {
            if (suggestions.mouseScrolled(verticalAmount)) {
                return true;
            }
        }

        int totalHeight = (actionButtonData.actions.size() + 1) * actionRowHeight;
        int maxScroll = Math.max(0, totalHeight - (menuHeight - 20));
        scrollOffset = clamp(scrollOffset - (int) (verticalAmount * actionRowHeight), 0, maxScroll);
        init();
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.text(font, title, (width - font.width(title)) / 2, menuY - 15, -1, true);
        guiGraphics.fill(menuX - 1, menuY - 1, menuX + menuWidth + 1, menuY + menuHeight + 1, 0x44000000);
        guiGraphics.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, 0xCC121212);
        renderBorder(guiGraphics, menuX, menuY, menuWidth, menuHeight, 0x33FFFFFF);
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        int totalHeight = (actionButtonData.actions.size() + 1) * actionRowHeight;
        int visibleH = menuHeight - 20;
        if (totalHeight > visibleH) {
            int sbX = menuX + menuWidth - 6;
            int sbY = menuY + 10;
            guiGraphics.fill(sbX, sbY, sbX + 3, sbY + visibleH, 0x22FFFFFF);
            int thumbH = Math.max(4, (int) (visibleH / (double) totalHeight * visibleH));
            int thumbY = sbY + (int) (scrollOffset / (double) (totalHeight - visibleH) * (visibleH - thumbH));
            int thumbColor = isDraggingScrollbar || isMouseOverScrollbar(mouseX, mouseY) ? 0xAAFFFFFF : 0x66FFFFFF;
            guiGraphics.fill(sbX, thumbY, sbX + 3, thumbY + thumbH, thumbColor);
        }

        for (CommandSuggestions suggestions : commandSuggestions) {
            suggestions.extractRenderState(guiGraphics, mouseX, mouseY);
        }
    }

    private void renderBorder(GuiGraphicsExtractor guiGraphics, int x, int y, int w, int h, int color) {
        guiGraphics.fill(x, y, x + w, y + 1, color);
        guiGraphics.fill(x, y + h - 1, x + w, y + h, color);
        guiGraphics.fill(x, y, x + 1, y + h, color);
        guiGraphics.fill(x + w - 1, y, x + w, y + h, color);
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

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
