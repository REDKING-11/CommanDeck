package xyz.redking.commanddeck;

import net.fabricmc.api.ModInitializer;
import xyz.redking.commanddeck.other.ActionButtonDataHandler;
import xyz.redking.commanddeck.other.ModConfig;

public class CommandDeck implements ModInitializer {
    public static final ModConfig CONFIG = ModConfig.createAndLoad();

    @Override
    public void onInitialize() {
        ActionButtonDataHandler.initialize();
    }
}
