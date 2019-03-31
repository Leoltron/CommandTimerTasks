package ru.leoltron.commandtimertasks.timertask;

import ru.leoltron.commandtimertasks.RepeatOptions;

import java.util.Timer;
import java.util.TimerTask;

public abstract class CustomTimerTask extends TimerTask {
    private final RepeatOptions repeatOptions;

    CustomTimerTask(RepeatOptions repeatOptions)
    {
        this.repeatOptions = repeatOptions;
    }

    public void runInTimer(Timer timer) {
        if (repeatOptions.shouldRepeat())
        {
            timer.schedule(this, repeatOptions.getDelayMillis());
        }
        else
        {
            timer.schedule(this, repeatOptions.getDelayMillis(), repeatOptions.getPeriodMillis());
        }
    }
}
