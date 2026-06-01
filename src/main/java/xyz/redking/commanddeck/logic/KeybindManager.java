package xyz.redking.commanddeck.logic;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import xyz.redking.commanddeck.data.ActionButtonData;
import xyz.redking.commanddeck.other.ActionButtonDataHandler;
import xyz.redking.commanddeck.other.KeybindHandler;
import xyz.redking.commanddeck.other.ModKeybindings;
import xyz.redking.commanddeck.ui.MainUI;

import java.util.List;

public final class KeybindManager {
    private static boolean menuKeyPressed = false;

    private KeybindManager() {
    }

    public static void onClientTick(Minecraft client) {
        handleMenuKey(client);

        if (client.screen == null) {
            handleActionKeys(client, ActionButtonDataHandler.actions);
        }

        ActionRunner.tick();
        KeybindHandler.runQueue();
    }

    private static void handleMenuKey(Minecraft client) {
        if (ModKeybindings.isMenuOpenKeyDown(client)) {
            if (!menuKeyPressed) {
                client.setScreen(new MainUI());
            }
            menuKeyPressed = true;
        } else if (client.screen == null) {
            menuKeyPressed = false;
        }
    }

    private static boolean handleActionKeys(Minecraft client, List<ActionButtonData> actions) {
        for (ActionButtonData actionData : actions) {
            boolean shouldRun = shouldTriggerKeybind(client, actionData);

            if (actionData.isFolder) {
                if (shouldRun && MainUI.navigateToFolder(actionData)) {
                    client.setScreen(new MainUI(true));
                    return true;
                }
                if (handleActionKeys(client, actionData.children)) {
                    return true;
                }
                continue;
            }

            if (shouldRun) {
                actionData.run(true);
            }
        }

        return false;
    }

    private static boolean shouldTriggerKeybind(Minecraft client, ActionButtonData actionData) {
        boolean shouldRun = false;
        List<Integer> keybind = actionData.keybind;

        if (keybind.size() >= 4) {
            if (keybind.get(3) == 0) {
                InputConstants.Key key = actionData.getKey();
                if (key != null && InputConstants.isKeyDown(client.getWindow(), key.getValue())) {
                    int mods = keybind.get(2);
                    boolean reqCtrl = (mods & GLFW.GLFW_MOD_CONTROL) != 0;
                    boolean reqShift = (mods & GLFW.GLFW_MOD_SHIFT) != 0;
                    boolean reqAlt = (mods & GLFW.GLFW_MOD_ALT) != 0;

                    boolean isCtrlDown = InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL) || InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_RIGHT_CONTROL);
                    boolean isShiftDown = InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
                    boolean isAltDown = InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_LEFT_ALT) || InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_RIGHT_ALT);

                    if ((!reqCtrl || isCtrlDown) && (!reqShift || isShiftDown) && (!reqAlt || isAltDown)) {
                        if (!actionData.keyPressed) {
                            shouldRun = true;
                        }
                        actionData.keyPressed = true;
                    } else {
                        actionData.keyPressed = false;
                    }
                } else {
                    actionData.keyPressed = false;
                }
            } else {
                boolean pressed = switch (keybind.get(0)) {
                    case 0 -> client.mouseHandler.isLeftPressed();
                    case 1 -> client.mouseHandler.isRightPressed();
                    case 2 -> client.mouseHandler.isMiddlePressed();
                    default -> false;
                };
                if (pressed) {
                    if (!actionData.keyPressed) {
                        shouldRun = true;
                    }
                    actionData.keyPressed = true;
                } else {
                    actionData.keyPressed = false;
                }
            }
        }

        return shouldRun;
    }
}
