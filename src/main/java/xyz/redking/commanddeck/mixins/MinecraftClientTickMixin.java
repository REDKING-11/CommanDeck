package xyz.redking.commanddeck.mixins;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.redking.commanddeck.logic.KeybindManager;

@Mixin(Minecraft.class)
public class MinecraftClientTickMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void commanddeck$afterClientTick(CallbackInfo ci) {
        KeybindManager.onClientTick((Minecraft) (Object) this);
    }
}
