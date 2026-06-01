package xyz.redking.commanddeck.data.command_actions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class ConditionActionData extends ActionData {
    public static final String[] CONDITIONS = {"sneaking", "sprinting", "creative", "survival", "on_ground", "in_water"};

    public final String condition;

    public ConditionActionData() {
        this("sneaking");
    }

    public ConditionActionData(String condition) {
        this.condition = condition;
    }

    @Override
    public String getType() {
        return "condition";
    }

    @Override
    public String getValue() {
        return condition;
    }

    @Override
    public String getTypeString() {
        return "IF";
    }

    @Override
    public String getDisplayString() {
        return label(condition);
    }

    @Override
    public boolean canContinue() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }

        return switch (condition) {
            case "sneaking" -> isSneaking(player);
            case "sprinting" -> player.isSprinting() || player.input.keyPresses.sprint();
            case "creative" -> player.isCreative();
            case "survival" -> !player.isCreative() && !player.isSpectator();
            case "on_ground" -> player.onGround();
            case "in_water" -> player.isInWater();
            default -> true;
        };
    }

    @Override
    public void run() {
    }

    private boolean isSneaking(LocalPlayer player) {
        return player.input.keyPresses.shift() || player.isShiftKeyDown() || player.isCrouching();
    }

    public static String label(String value) {
        return switch (value) {
            case "sneaking" -> "If Sneaking";
            case "sprinting" -> "If Sprinting";
            case "creative" -> "If Creative";
            case "survival" -> "If Survival";
            case "on_ground" -> "If On Ground";
            case "in_water" -> "If In Water";
            default -> "If " + value;
        };
    }
}
