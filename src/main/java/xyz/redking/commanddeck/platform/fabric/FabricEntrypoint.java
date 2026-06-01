package xyz.redking.commanddeck.platform.fabric;

import net.fabricmc.api.ModInitializer;
import xyz.redking.commanddeck.CommandDeck;

public class FabricEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandDeck.initialize();
    }
}
