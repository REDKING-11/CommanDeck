package xyz.redking.commanddeck.data.command_actions;

public abstract class ActionData {
    public abstract String getType();

    public abstract String getValue();

    public abstract String getTypeString();

    public abstract String getDisplayString();

    public abstract void run();

    public int getDelayTicks() {
        return 0;
    }

    public boolean canContinue() {
        return true;
    }
}
