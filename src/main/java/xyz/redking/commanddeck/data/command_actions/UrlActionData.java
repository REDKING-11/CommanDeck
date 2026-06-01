package xyz.redking.commanddeck.data.command_actions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;

public class UrlActionData extends ActionData {
    public final String url;

    public UrlActionData() {
        this("https://");
    }

    public UrlActionData(String url) {
        this.url = url;
    }

    @Override
    public String getType() {
        return "url";
    }

    @Override
    public String getValue() {
        return url;
    }

    @Override
    public String getTypeString() {
        return "URL";
    }

    @Override
    public String getDisplayString() {
        return url;
    }

    @Override
    public void run() {
        if (url == null || url.isBlank()) {
            return;
        }
        ConfirmLinkScreen.confirmLinkNow(Minecraft.getInstance().screen, url, true);
    }
}
