package xyz.redking.commanddeck.data.command_actions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class CommandActionData extends ActionData {
    public final String command;

    public CommandActionData() {
        this("");
    }

    public CommandActionData(String command) {
        this.command = command;
    }

    @Override
    public String getType() {
        return "cmd";
    }

    @Override
    public String getValue() {
        return command;
    }

    @Override
    public String getTypeString() {
        return "CMD";
    }

    @Override
    public String getDisplayString() {
        return command;
    }

    @Override
    public void run() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        String commandToRun = command;
        if (commandToRun.startsWith("/")) {
            player.connection.sendCommand(commandToRun.substring(1));
        } else {
            if (commandToRun.length() > 256) {
                commandToRun = commandToRun.substring(0, 256);
            }
            player.connection.sendChat(commandToRun);
        }
    }
}
