package ru.leoltron.commandtimertasks;

public class RepeatOptions
{
    private final int delayMillis;
    private final int periodMillis;

    public RepeatOptions(int delayMillis)
    {
        this(delayMillis, -1);
    }

    RepeatOptions(int delayMillis, int periodMillis)
    {
        if (delayMillis < 0)
        {
            throw new NumberFormatException("Delay cannot be negative");
        }
        this.delayMillis = delayMillis;
        this.periodMillis = periodMillis <= 0 ? -1 : periodMillis;
    }

    public int getDelayMillis()
    {
        return delayMillis;
    }

    public int getPeriodMillis()
    {
        return periodMillis;
    }

    public boolean shouldRepeat()
    {
        return periodMillis > 0;
    }

    @Override
    public String toString()
    {
        return "RepeatOptions{" +
                "delayMillis=" + delayMillis +
                ", periodMillis=" + periodMillis +
                '}';
    }
}
