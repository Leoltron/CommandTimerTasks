package ru.leoltron.commandtimertasks.timertask;

import org.bukkit.Bukkit;

public class MessageBroadcastTimerTask extends CustomTimerTask {

    private String message;

    public MessageBroadcastTimerTask(String message, int delay, int period) {
        this.message = message;
        this.delay = delay;
        this.period = period;
    }

    @Override
    public void run() {
        Bukkit.broadcastMessage(message);
    }
}
