package xyz.redking.commanddeck.other;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class ModKeybindings {
    public static final KeyMapping MENU_OPEN = new KeyMapping(
            "key.commanddeck.open",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.commanddeck"
    );

    private ModKeybindings() {
    }

    public static boolean isMenuOpenKeyDown(Minecraft client) {
        return MENU_OPEN.isDown();
    }

    public static boolean matchesMenuOpenKey(int keyCode) {
        return MENU_OPEN.getKey().getType() == InputConstants.Type.KEYSYM && MENU_OPEN.getKey().getValue() == keyCode;
    }

    public static boolean matchesMenuOpenMouseButton(int button) {
        return false;
    }
}
