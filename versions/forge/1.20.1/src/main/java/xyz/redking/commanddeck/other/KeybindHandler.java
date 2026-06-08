package xyz.redking.commanddeck.other;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public final class KeybindHandler {
    private static final List<KeyMapping> queuedKeys = new ArrayList<>();
    private static final List<KeyMapping> queuedRelease = new ArrayList<>();
    private static boolean didPress = false;

    private KeybindHandler() {
    }

    public static void runQueue() {
        if (didPress) {
            for (KeyMapping keyMapping : queuedRelease) {
                keyMapping.setDown(false);
            }
            didPress = false;
            queuedRelease.clear();
        }

        for (KeyMapping keyMapping : queuedKeys) {
            KeyMapping.click(keyMapping.getKey());
            keyMapping.setDown(true);
            didPress = true;
            queuedRelease.add(keyMapping);
        }

        queuedKeys.clear();
    }

    public static void pressKey(String translationKey) {
        KeyMapping keyMapping = getFromTranslationKey(translationKey);
        if (keyMapping != null) {
            queuedKeys.add(keyMapping);
        }
    }

    public static KeyMapping getFromTranslationKey(String translationKey) {
        Minecraft client = Minecraft.getInstance();
        for (KeyMapping mapping : client.options.keyMappings) {
            if (mapping.getName().equals(translationKey)) {
                return mapping;
            }
        }
        return null;
    }

    public static KeyMapping[] getKeybindings() {
        return Minecraft.getInstance().options.keyMappings;
    }
}
