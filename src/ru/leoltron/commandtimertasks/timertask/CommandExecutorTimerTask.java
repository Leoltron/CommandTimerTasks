package ru.leoltron.commandtimertasks.timertask;

import org.bukkit.Bukkit;
import ru.leoltron.commandtimertasks.RepeatOptions;

public class CommandExecutorTimerTask extends CustomTimerTask {

    private String command;

    public CommandExecutorTimerTask(String command, RepeatOptions repeatOptions)
    {
        super(repeatOptions);
        this.command = command;
    }

    @Override
    public void run() {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
