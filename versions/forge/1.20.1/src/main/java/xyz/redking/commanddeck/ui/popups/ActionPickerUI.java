package xyz.redking.commanddeck.ui.popups;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import xyz.redking.commanddeck.data.command_actions.ActionData;
import xyz.redking.commanddeck.data.command_actions.CommandActionData;
import xyz.redking.commanddeck.data.command_actions.ConditionActionData;
import xyz.redking.commanddeck.data.command_actions.DelayActionData;
import xyz.redking.commanddeck.data.command_actions.FolderActionData;
import xyz.redking.commanddeck.data.command_actions.KeybindActionData;
import xyz.redking.commanddeck.data.command_actions.ScreenActionData;
import xyz.redking.commanddeck.data.command_actions.SoundActionData;
import xyz.redking.commanddeck.data.command_actions.UrlActionData;

import java.util.function.Consumer;

public class ActionPickerUI extends Screen {
    public Consumer<ActionData> onSelectedAction = action -> {
    };
    public Screen previousScreen;

    private int menuX;
    private int menuY;
    private final int menuWidth = 180;
    private final int menuHeight = 220;

    public ActionPickerUI() {
        super(Component.translatable("menu.action_picker.title"));
    }

    @Override
    protected void init() {
        menuX = (width - menuWidth) / 2;
        menuY = (height - menuHeight) / 2;
        int startY = menuY + 10;

        addRenderableWidget(Button.builder(Component.translatable("menu.action_picker.command"), button -> {
                    onSelectedAction.accept(new CommandActionData());
                    onClose();
                })
                .pos(menuX + 10, startY)
                .size(160, 20)
                .build());

        addRenderableWidget(Button.builder(Component.translatable("menu.action_picker.keybind"), button -> {
                    onSelectedAction.accept(new KeybindActionData());
                    onClose();
                })
                .pos(menuX + 10, startY + 25)
                .size(160, 20)
                .build());

        addRenderableWidget(Button.builder(Component.literal("Open URL"), button -> {
                    onSelectedAction.accept(new UrlActionData());
                    onClose();
                })
                .pos(menuX + 10, startY + 50)
                .size(160, 20)
                .build());

        addRenderableWidget(Button.builder(Component.literal("Open Screen"), button -> {
                    onSelectedAction.accept(new ScreenActionData());
                    onClose();
                })
                .pos(menuX + 10, startY + 75)
                .size(160, 20)
                .build());

        addRenderableWidget(Button.builder(Component.literal("Delay"), button -> {
                    onSelectedAction.accept(new DelayActionData());
                    onClose();
                })
                .pos(menuX + 10, startY + 100)
                .size(160, 20)
                .build());

        addRenderableWidget(Button.builder(Component.literal("Condition"), button -> {
                    onSelectedAction.accept(new ConditionActionData());
                    onClose();
                })
                .pos(menuX + 10, startY + 125)
                .size(160, 20)
                .build());

        addRenderableWidget(Button.builder(Component.literal("Ding Sound"), button -> {
                    onSelectedAction.accept(new SoundActionData());
                    onClose();
                })
                .pos(menuX + 10, startY + 150)
                .size(160, 20)
                .build());

        addRenderableWidget(Button.builder(Component.literal("Open Folder"), button -> {
                    onSelectedAction.accept(new FolderActionData());
                    onClose();
                })
                .pos(menuX + 10, startY + 175)
                .size(160, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, 0xCC000000);
        super.render(context, mouseX, mouseY, delta);
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
