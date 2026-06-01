package xyz.redking.commanddeck.data.command_actions;

import net.minecraft.network.chat.Component;
import xyz.redking.commanddeck.other.KeybindHandler;

public class KeybindActionData extends ActionData {
    public final String translationKey;

    public KeybindActionData() {
        this("");
    }

    public KeybindActionData(String translationKey) {
        this.translationKey = translationKey;
    }

    @Override
    public String getType() {
        return "key";
    }

    @Override
    public String getValue() {
        return translationKey;
    }

    @Override
    public String getTypeString() {
        return "KEY";
    }

    @Override
    public String getDisplayString() {
        return Component.translatable(translationKey).getString();
    }

    @Override
    public void run() {
        KeybindHandler.pressKey(translationKey);
    }
}
