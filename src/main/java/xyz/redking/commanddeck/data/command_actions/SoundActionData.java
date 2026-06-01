package xyz.redking.commanddeck.data.command_actions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public class SoundActionData extends ActionData {
    public final String sound;

    public SoundActionData() {
        this("button");
    }

    public SoundActionData(String sound) {
        this.sound = sound;
    }

    @Override
    public String getType() {
        return "sound";
    }

    @Override
    public String getValue() {
        return sound;
    }

    @Override
    public String getTypeString() {
        return "SFX";
    }

    @Override
    public String getDisplayString() {
        return "Ding";
    }

    @Override
    public void run() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.4F, 0.7F));
    }
}
