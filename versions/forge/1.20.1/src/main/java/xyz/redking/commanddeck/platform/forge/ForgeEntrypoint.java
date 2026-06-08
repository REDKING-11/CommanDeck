package xyz.redking.commanddeck.platform.forge;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import xyz.redking.commanddeck.CommandDeck;
import xyz.redking.commanddeck.logic.KeybindManager;
import xyz.redking.commanddeck.other.ModKeybindings;

@Mod(CommandDeck.MOD_ID)
public class ForgeEntrypoint {
    public ForgeEntrypoint() {
        CommandDeck.initialize();

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::registerKeyMappings);
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeybindings.MENU_OPEN);
    }

    @Mod.EventBusSubscriber(modid = CommandDeck.MOD_ID, value = Dist.CLIENT)
    private static final class ClientEvents {
        private ClientEvents() {
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                KeybindManager.onClientTick(Minecraft.getInstance());
            }
        }
    }
}
