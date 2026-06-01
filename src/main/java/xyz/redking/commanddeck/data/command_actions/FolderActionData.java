package xyz.redking.commanddeck.data.command_actions;

import net.minecraft.client.Minecraft;
import xyz.redking.commanddeck.ui.MainUI;

public class FolderActionData extends ActionData {
    public final String path;

    public FolderActionData() {
        this("");
    }

    public FolderActionData(String path) {
        this.path = path;
    }

    @Override
    public String getType() {
        return "folder";
    }

    @Override
    public String getValue() {
        return path;
    }

    @Override
    public String getTypeString() {
        return "OPEN";
    }

    @Override
    public String getDisplayString() {
        return path == null || path.isBlank() ? "Folder" : path;
    }

    @Override
    public void run() {
        if (MainUI.navigateToFolderPath(path)) {
            Minecraft.getInstance().setScreen(new MainUI(true));
        }
    }
}
