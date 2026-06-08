package xyz.redking.commanddeck.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.redking.commanddeck.data.command_actions.ActionData;
import xyz.redking.commanddeck.data.command_actions.CommandActionData;
import xyz.redking.commanddeck.data.command_actions.ConditionActionData;
import xyz.redking.commanddeck.data.command_actions.DelayActionData;
import xyz.redking.commanddeck.data.command_actions.FolderActionData;
import xyz.redking.commanddeck.data.command_actions.KeybindActionData;
import xyz.redking.commanddeck.data.command_actions.ScreenActionData;
import xyz.redking.commanddeck.data.command_actions.SoundActionData;
import xyz.redking.commanddeck.data.command_actions.UrlActionData;

import java.util.ArrayList;
import java.util.List;

public class ActionButtonDataJson {
    public String name;
    public List<List<String>> actions = new ArrayList<>();
    public String icon;
    public String customModelData;
    public List<Integer> keybind = new ArrayList<>();
    public boolean isFolder;
    public List<ActionButtonDataJson> children = new ArrayList<>();

    public ActionButtonData toActionButtonData() {
        ActionButtonData data = new ActionButtonData();
        data.name = name == null ? "" : name;
        data.keybind = keybind == null ? new ArrayList<>() : new ArrayList<>(keybind);
        data.isFolder = isFolder;

        if (actions != null) {
            for (List<String> actionList : actions) {
                if (actionList.size() < 2) {
                    continue;
                }
                String type = actionList.get(0);
                String value = actionList.get(1);
                if ("cmd".equals(type)) {
                    data.actions.add(new CommandActionData(value));
                } else if ("key".equals(type)) {
                    data.actions.add(new KeybindActionData(value));
                } else if ("url".equals(type)) {
                    data.actions.add(new UrlActionData(value));
                } else if ("screen".equals(type)) {
                    data.actions.add(new ScreenActionData(value));
                } else if ("delay".equals(type)) {
                    data.actions.add(new DelayActionData(value));
                } else if ("condition".equals(type)) {
                    data.actions.add(new ConditionActionData(value));
                } else if ("sound".equals(type)) {
                    data.actions.add(new SoundActionData(value));
                } else if ("folder".equals(type)) {
                    data.actions.add(new FolderActionData(value));
                }
            }
        }

        if (icon != null && !icon.isBlank()) {
            try {
                ResourceLocation itemId = ResourceLocation.tryParse(icon);
                if (itemId != null) {
                    Item item = BuiltInRegistries.ITEM.get(itemId);
                    ItemStack stack = item.getDefaultInstance();
                    if (customModelData != null && !customModelData.isEmpty()) {
                        stack.getOrCreateTag().putInt("CustomModelData", Integer.parseInt(customModelData));
                    }
                    data.icon = stack;
                }
            } catch (Exception ignored) {
            }
        }

        if (children != null) {
            for (ActionButtonDataJson child : children) {
                data.children.add(child.toActionButtonData());
            }
        }

        return data;
    }

    public static ActionButtonDataJson fromActionButtonData(ActionButtonData data) {
        ActionButtonDataJson json = new ActionButtonDataJson();
        json.name = data.name;
        json.keybind = new ArrayList<>(data.keybind);
        json.isFolder = data.isFolder;

        for (ActionData action : data.actions) {
            json.actions.add(List.of(action.getType(), action.getValue()));
        }

        if (!data.icon.isEmpty()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(data.icon.getItem());
            if (itemId != null) {
                json.icon = itemId.toString();
            }
            CompoundTag tag = data.icon.getTag();
            if (tag != null && tag.contains("CustomModelData")) {
                json.customModelData = Integer.toString(tag.getInt("CustomModelData"));
            }
        }

        for (ActionButtonData child : data.children) {
            json.children.add(fromActionButtonData(child));
        }

        return json;
    }
}
