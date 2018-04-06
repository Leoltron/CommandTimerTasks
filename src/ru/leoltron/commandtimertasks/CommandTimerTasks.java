package ru.leoltron.commandtimertasks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.leoltron.commandtimertasks.timertask.CommandExecutorTimerTask;
import ru.leoltron.commandtimertasks.timertask.CustomTimerTask;
import ru.leoltron.commandtimertasks.timertask.MessageBroadcastTimerTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class CommandTimerTasks extends JavaPlugin {

    private Pattern LINE_PATTERN = Pattern.compile("TIME -?([\\d]+) PERIOD -?([\\d]+) (COMMAND|MESSAGE) (.*)");

    public static final String COMMANDS_FILE = "commands.txt";

    private Timer timer;
    private List<CustomTimerTask> tasks = new ArrayList<>();

    @Override
    public void onEnable() {
        super.onEnable();

        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(COMMANDS_FILE));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Can't read \"" + COMMANDS_FILE + "\", plugin will not work.", e);
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        parseTasks(lines);
        startTasks();
    }

    private void parseTasks(List<String> lines) {
        for (String line : lines)
            if (line.length() > 0)
                tryParseTask(line);
    }

    private void startTasks() {
        timer = new Timer();
        for (CustomTimerTask task : tasks)
            task.runInTimer(timer);
    }

    private boolean tryParseTask(String string) {
        final Matcher matcher = LINE_PATTERN.matcher(string);
        if (matcher.matches()) {
            int delay = Integer.parseInt(matcher.group(1));
            if (delay < 0) {
                getLogger().warning("Can't parse task \"" + string + "\": invalid format");
                return false;
            }
            int period = Integer.parseInt(matcher.group(2));
            String type = matcher.group(3);
            String value = matcher.group(4);

            if (type.equalsIgnoreCase("message")) {
                tasks.add(new MessageBroadcastTimerTask(value, delay, period));
            } else if (type.equalsIgnoreCase("command"))
                tasks.add(new CommandExecutorTimerTask(value, delay, period));
            else {
                getLogger().warning("Can't parse task \"" + string + "\": invalid type");
                return false;
            }

        } else {
            getLogger().warning("Can't parse task \"" + string + "\": invalid format");
        }
        return matcher.matches();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        timer.cancel();
        tasks.clear();
    }
}
