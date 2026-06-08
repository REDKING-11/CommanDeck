package xyz.redking.commanddeck.data;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import xyz.redking.commanddeck.CommandDeck;
import xyz.redking.commanddeck.data.command_actions.ActionData;
import xyz.redking.commanddeck.logic.ActionRunner;
import xyz.redking.commanddeck.other.ModConfig;

import java.util.ArrayList;
import java.util.List;

public class ActionButtonData {
    public String name;
    public List<ActionData> actions;
    public ItemStack icon;
    public List<Integer> keybind;
    public boolean isFolder;
    public List<ActionButtonData> children;
    public boolean keyPressed;

    public ActionButtonData() {
        this("", new ArrayList<>(), ItemStack.EMPTY, new ArrayList<>(), false, new ArrayList<>());
    }

    public ActionButtonData(String name, List<ActionData> actions, ItemStack icon, List<Integer> keybind, boolean isFolder, List<ActionButtonData> children) {
        this.name = name;
        this.actions = actions;
        this.icon = icon;
        this.keybind = keybind;
        this.isFolder = isFolder;
        this.children = children;
    }

    public ActionButtonData copy() {
        return new ActionButtonData(name, new ArrayList<>(actions), icon.copy(), new ArrayList<>(keybind), isFolder, new ArrayList<>(children));
    }

    public InputConstants.Key getKey() {
        if (keybind.size() < 4) {
            return null;
        }
        return InputConstants.Type.KEYSYM.getOrCreate(keybind.get(0));
    }

    public void run() {
        run(false);
    }

    public void run(boolean isKeybind) {
        if (isFolder) {
            return;
        }

        ModConfig.DisplayRunText displayRunText = CommandDeck.CONFIG.displayRunText;
        if (displayRunText == ModConfig.DisplayRunText.ALWAYS || (displayRunText == ModConfig.DisplayRunText.KEYBIND_ONLY && isKeybind)) {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(Component.literal("Ran action \"" + name + "\""), true);
            }
        }

        ActionRunner.run(new ArrayList<>(actions));
    }
}
