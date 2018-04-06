package ru.leoltron.commandtimertasks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.leoltron.commandtimertasks.timertask.CommandExecutorTimerTask;
import ru.leoltron.commandtimertasks.timertask.CustomTimerTask;
import ru.leoltron.commandtimertasks.timertask.MessageBroadcastTimerTask;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class CommandTimerTasks extends JavaPlugin {

    private Pattern LINE_PATTERN = Pattern.compile("TIME (-?[\\d]+) PERIOD (-?[\\d]+) (COMMAND|MESSAGE) (.*)");

    private static final String COMMANDS_FILE = "plugins/CommandTimerTasksCommands.txt";

    private Timer timer;
    private Collection<CustomTimerTask> tasks = new ArrayList<>();

    @Override
    public void onEnable() {
        super.onEnable();

        List<String> lines;
        try {
            File commandsFile = new File(COMMANDS_FILE);
            if (!commandsFile.exists()) {
                createExampleCommandsFile(commandsFile);
                getLogger().log(Level.WARNING, "Created file \"" + COMMANDS_FILE + "\", check it to learn how to" +
                        " use plugin, modify it and reload the plugin. Plugin will be disabled.");
                Bukkit.getServer().getPluginManager().disablePlugin(this);
                return;
            }
            lines = Files.readAllLines(Paths.get(COMMANDS_FILE));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Can't read \"" + COMMANDS_FILE + "\", plugin will be disabled.", e);
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        parseTasks(lines);
        startTasks();
    }

    private static void createExampleCommandsFile(File commandsFile) throws IOException {
        commandsFile.createNewFile();
        try (FileWriter writer = new FileWriter(commandsFile)) {
            writer.write("//Command format:\n" +
                    "//TIME <delay> PERIOD <period> <COMMAND|MESSAGE> text:\n" +
                    "//delay: delay in milliseconds before command is executed or message is sent\n" +
                    "//period: time in milliseconds between command executions/message sending" +
                    " (set negative value to use command/message only once)\n" +
                    "//text: message to send or command to execute (without '/')\n" +
                    "\n" +
                    "//Examples:\n" +
                    "//TIME 500 PERIOD 10000 MESSAGE Hello world!\n" +
                    "//TIME 30000 PERIOD -1 COMMAND xp 255L Leoltron\n");
        }
    }

    private void parseTasks(Iterable<String> lines) {
        int tasksParsed = 0;
        for (String line : lines)
            if (line.length() > 0)
                if (tryParseTask(line))
                    tasksParsed++;
        getLogger().info(String.format("Parsed %d %s", tasksParsed, tasksParsed == 1 ? "task" : "tasks"));
    }

    private void startTasks() {
        timer = new Timer();
        for (CustomTimerTask task : tasks)
            task.runInTimer(timer);
    }

    private boolean tryParseTask(String string) {
        if (string.startsWith("//")) return false;
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
        if (timer != null)
            timer.cancel();
        tasks.clear();
    }
}
