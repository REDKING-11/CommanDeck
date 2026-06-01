package xyz.redking.commanddeck;

import xyz.redking.commanddeck.other.ActionButtonDataHandler;
import xyz.redking.commanddeck.other.ModConfig;

public final class CommandDeck {
    public static final ModConfig CONFIG = ModConfig.createAndLoad();

    private CommandDeck() {
    }

    public static void initialize() {
        ActionButtonDataHandler.initialize();
    }
}
