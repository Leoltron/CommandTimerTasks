package ru.leoltron.commandtimertasks.timertask;

import org.bukkit.Bukkit;

public class CommandExecutorTimerTask extends CustomTimerTask {

    private String command;

    public CommandExecutorTimerTask(String command, int delay, int period) {
        this.command = command;
        this.delay = delay;
        this.period = period;
    }

    @Override
    public void run() {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
