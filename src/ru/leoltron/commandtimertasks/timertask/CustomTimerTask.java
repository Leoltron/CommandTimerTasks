package ru.leoltron.commandtimertasks.timertask;

import java.util.Timer;
import java.util.TimerTask;

public abstract class CustomTimerTask extends TimerTask {
    protected int delay;
    protected int period;

    public void runInTimer(Timer timer) {
        if (period <= 0)
            timer.schedule(this, delay);
        else
            timer.schedule(this, delay, period);
    }
}
