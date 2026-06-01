package xyz.redking.commanddeck.other;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;

public final class ModKeybindings {
    private ModKeybindings() {
    }

    public static boolean isMenuOpenKeyDown(Minecraft client) {
        return InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_G);
    }

    public static boolean matchesMenuOpenKey(KeyEvent event) {
        return event.key() == GLFW.GLFW_KEY_G;
    }

    public static boolean matchesMenuOpenKey(MouseButtonEvent event) {
        return false;
    }
}
