package ru.leoltron.commandtimertasks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.leoltron.commandtimertasks.timertask.CommandExecutorTimerTask;
import ru.leoltron.commandtimertasks.timertask.CustomTimerTask;
import ru.leoltron.commandtimertasks.timertask.MessageBroadcastTimerTask;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class CommandTimerTasks extends JavaPlugin
{

    private static final Pattern PART_REGEX = Pattern.compile("\\s*([\\d]+)(ms|s|m|h|d)", Pattern.CASE_INSENSITIVE);

    private static final String COMMANDS_FILE = "plugins/CommandTimerTasksCommands.txt";

    private Timer timer;
    private Collection<CustomTimerTask> tasks = new ArrayList<>();
    private static final int HOURS_IN_DAY = 24;
    private static final int MINUTES_IN_HOUR = 60;
    private static final int SECONDS_IN_HOUR = 60;
    private static final int MILLIS_IN_SECOND = 1000;
    private Pattern LINE_PATTERN = Pattern.compile("(TIME(?:(?:\\s+[\\d]+(?:ms|s|m|h|d))+)(?:\\s+PERIOD(?:(?:\\s+[\\d]+(?:ms|s|m|h|d))+))?)\\s+(COMMAND|MESSAGE)\\s+(.*)", Pattern.CASE_INSENSITIVE);
    private Random rand = new Random();

    private static void createExampleCommandsFile(File commandsFile) throws IOException
    {
        //noinspection ResultOfMethodCallIgnored
        commandsFile.createNewFile();
        try (FileWriter writer = new FileWriter(commandsFile))
        {
            writer.write("//Command format:\n" +
                    "//TIME <delay> [PERIOD <period>] <COMMAND|MESSAGE> text:\n" +
                    "//delay: delay before command is executed or message is sent\n" +
                    "//period: time between command executions/message sending\n" +
                    "//text: message to send or command to execute (without '/')\n" +
                    "\n" +
                    "//Examples:\n" +
                    "//TIME 1d 1h 1m 5s 2ms PERIOD 10000ms MESSAGE Hello world!\n" +
                    "//TIME 3h COMMAND xp 255L Leoltron\n");
        }
    }

    @Override
    public void onEnable()
    {
        super.onEnable();

        List<String> lines;
        try
        {
            File commandsFile = new File(COMMANDS_FILE);
            if (!commandsFile.exists())
            {
                createExampleCommandsFile(commandsFile);
                getLogger().log(Level.WARNING, "Created file \"" + COMMANDS_FILE + "\", check it to learn how to" +
                        " use plugin, modify it and reload the plugin. Plugin will be disabled.");
                Bukkit.getServer().getPluginManager().disablePlugin(this);
                return;
            }
            lines = Files.readAllLines(Paths.get(COMMANDS_FILE));
        } catch (IOException e)
        {
            getLogger().log(Level.SEVERE, "Can't read \"" + COMMANDS_FILE + "\", plugin will be disabled.", e);
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        parseTasks(lines);
        startTasks();
    }

    private void parseTasks(Iterable<String> lines)
    {
        List<List<CustomTimerTask>> taskLists = new ArrayList<>();

        List<CustomTimerTask> currentList = new ArrayList<>();
        int tasksParsed = 0;
        for (String line : lines)
        {
            if (line.equalsIgnoreCase("new list"))
            {
                taskLists.add(currentList);
                currentList = new ArrayList<>();
            }
            else if (line.length() > 0)
            {
                final CustomTimerTask task = tryParseTask(line);
                if (task != null)
                {
                    tasksParsed++;
                    currentList.add(task);
                }
            }
        }
        taskLists.add(currentList);

        getLogger().info(String.format("Parsed %d %s", tasksParsed, tasksParsed == 1 ? "task" : "tasks"));
        chooseTaskList(taskLists);
    }

    private void chooseTaskList(List<List<CustomTimerTask>> taskLists)
    {
        if (taskLists.size() == 1)
        {
            tasks = taskLists.get(0);
        }
        else
        {
            int listIndex = rand.nextInt(taskLists.size());
            getLogger().info(String.format("Parsed %d task %s, using list #%d", taskLists.size(), taskLists.size() == 1 ? "list" : "lists", listIndex));
            tasks = taskLists.get(listIndex);
        }
    }

    private void startTasks()
    {
        timer = new Timer();
        for (CustomTimerTask task : tasks)
        {
            task.runInTimer(timer);
        }
    }

    private CustomTimerTask tryParseTask(String string)
    {
        if (string.startsWith("//"))
        {
            return null;
        }
        final Matcher matcher = LINE_PATTERN.matcher(string);
        if (matcher.matches())
        {
            RepeatOptions repeatOptions = parseRepeatOptions(matcher.group(1));
            String type = matcher.group(2);
            String value = matcher.group(3);

            if (type.equalsIgnoreCase("message"))
            {
                return new MessageBroadcastTimerTask(value, repeatOptions);
            }
            else if (type.equalsIgnoreCase("command"))
            {
                return new CommandExecutorTimerTask(value, repeatOptions);
            }
            else
            {
                getLogger().warning(String.format("Can't parse task \"%s\": invalid type (\"%s\")", string, type));
                return null;
            }
        }
        else
        {
            getLogger().warning("Can't parse task \"" + string + "\": invalid format");
            return null;
        }
    }

    private RepeatOptions parseRepeatOptions(String string)
    {
        String[] parts = string.split("\\s+");
        if (!parts[0].equalsIgnoreCase("TIME"))
        {
            throw new IllegalArgumentException();
        }

        int delay = 0;
        int period = -1;

        for (int i = 1; i < parts.length; i++)
        {
            if (parts[i].equalsIgnoreCase("PERIOD"))
            {
                period = 0;
            }
            else
            {
                int value = parseRepeatOptionsPart(parts[i]);
                if (period >= 0)
                {
                    period += value;
                }
                else
                {
                    delay += value;
                }
            }
        }

        return new RepeatOptions(delay, period);
    }

    private int parseRepeatOptionsPart(CharSequence s)
    {
        Matcher m = PART_REGEX.matcher(s);
        if (!m.matches())
        {
            throw new IllegalArgumentException();
        }

        int value = Integer.parseInt(m.group(1));
        switch (m.group(2).toLowerCase())
        {
            case "d":
                value *= HOURS_IN_DAY;
            case "h":
                value *= MINUTES_IN_HOUR;
            case "m":
                value *= SECONDS_IN_HOUR;
            case "s":
                value *= MILLIS_IN_SECOND;
            case "ms":
                return value;
            default:
                throw new IllegalArgumentException("Unknown time measurement unit: " + m.group(2));
        }
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        if (timer != null)
        {
            timer.cancel();
        }
        tasks.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        String commandName = command.getName();
        if (commandName.equalsIgnoreCase("CommandTimerTasks")
                || commandName.equalsIgnoreCase("ctt")
                || commandName.equalsIgnoreCase("com"))
        {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload"))
            {
                reloadPlugin(this);
                sender.sendMessage(ChatColor.GREEN + "CommandTimerTasks has been successfully reloaded.");
                return true;
            }
        }
        return false;
    }

    private void reloadPlugin(Plugin plugin)
    {
        Bukkit.getServer().getPluginManager().disablePlugin(plugin);
        Bukkit.getServer().getPluginManager().enablePlugin(plugin);
    }
}
