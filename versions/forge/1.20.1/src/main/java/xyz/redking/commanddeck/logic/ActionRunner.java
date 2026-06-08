package xyz.redking.commanddeck.logic;

import xyz.redking.commanddeck.data.command_actions.ActionData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ActionRunner {
    private static final List<Sequence> ACTIVE_SEQUENCES = new ArrayList<>();

    private ActionRunner() {
    }

    public static void run(List<ActionData> actions) {
        Sequence sequence = new Sequence(actions);
        sequence.tick();
        if (!sequence.finished()) {
            ACTIVE_SEQUENCES.add(sequence);
        }
    }

    public static void tick() {
        Iterator<Sequence> iterator = ACTIVE_SEQUENCES.iterator();
        while (iterator.hasNext()) {
            Sequence sequence = iterator.next();
            sequence.tick();
            if (sequence.finished()) {
                iterator.remove();
            }
        }
    }

    private static class Sequence {
        private final List<ActionData> actions;
        private int index;
        private int waitTicks;
        private boolean stopped;

        private Sequence(List<ActionData> actions) {
            this.actions = actions;
        }

        private void tick() {
            if (stopped) {
                return;
            }
            if (waitTicks > 0) {
                waitTicks--;
                return;
            }

            while (index < actions.size()) {
                ActionData action = actions.get(index++);
                if (!action.canContinue()) {
                    stopped = true;
                    return;
                }

                int delayTicks = action.getDelayTicks();
                if (delayTicks > 0) {
                    waitTicks = delayTicks;
                    return;
                }

                action.run();
            }
        }

        private boolean finished() {
            return stopped || index >= actions.size();
        }
    }
}
