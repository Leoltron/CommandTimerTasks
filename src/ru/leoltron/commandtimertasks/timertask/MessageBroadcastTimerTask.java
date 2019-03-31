package ru.leoltron.commandtimertasks.timertask;

import org.bukkit.Bukkit;
import ru.leoltron.commandtimertasks.RepeatOptions;

public class MessageBroadcastTimerTask extends CustomTimerTask {

    private String message;

    public MessageBroadcastTimerTask(String message, RepeatOptions repeatOptions)
    {
        super(repeatOptions);
        this.message = message;
    }

    @Override
    public void run() {
        Bukkit.broadcastMessage(message);
    }
}
