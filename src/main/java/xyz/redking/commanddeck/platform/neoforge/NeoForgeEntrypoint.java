package xyz.redking.commanddeck.platform.neoforge;

import net.neoforged.fml.common.Mod;
import xyz.redking.commanddeck.CommandDeck;

@Mod("commanddeck")
public class NeoForgeEntrypoint {
    public NeoForgeEntrypoint() {
        CommandDeck.initialize();
    }
}
