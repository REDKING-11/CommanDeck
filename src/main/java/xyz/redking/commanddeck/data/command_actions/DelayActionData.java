package xyz.redking.commanddeck.data.command_actions;

public class DelayActionData extends ActionData {
    public final String ticks;

    public DelayActionData() {
        this("20");
    }

    public DelayActionData(String ticks) {
        this.ticks = ticks;
    }

    @Override
    public String getType() {
        return "delay";
    }

    @Override
    public String getValue() {
        return ticks;
    }

    @Override
    public String getTypeString() {
        return "WAIT";
    }

    @Override
    public String getDisplayString() {
        return ticks + " ticks";
    }

    @Override
    public int getDelayTicks() {
        try {
            return Math.max(0, Integer.parseInt(ticks.trim()));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    @Override
    public void run() {
    }
}
