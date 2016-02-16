package me.dags.commandbus;

import me.dags.commandbus.command.CommandContainer;
import me.dags.commandbus.command.CommandEvent;
import me.dags.commandbus.platform.PlatformRegistrar;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */

public class CommandRegister
{
    private static final List<CommandContainer> empty = Collections.unmodifiableList(new ArrayList<>());

    private final Map<String, List<CommandContainer>> register = new HashMap<>();
    private final Map<CommandContainer, String> containerMap = new HashMap<>();
    private final PlatformRegistrar platform;
    private final CommandBus commandBus;

    public CommandRegister(CommandBus commandBus, PlatformRegistrar platform)
    {
        this.platform = platform;
        this.commandBus = commandBus;
    }

    protected CommandContainer find(CommandEvent<?> event)
    {
        List<CommandContainer> containers = getCommand(event);
        for (CommandContainer container : containers)
        {
            if (container.matches(event))
            {
                return container;
            }
        }
        return null;
    }

    protected void addCommand(Object owner, CommandContainer commandContainer)
    {
        if (!containerMap.containsValue(commandContainer.command()))
        {
            platform.register(owner, commandContainer, commandBus);
            containerMap.put(commandContainer, commandContainer.command());
        }

        List<CommandContainer> toAdd = new ArrayList<>();
        toAdd.add(commandContainer);

        for (String s : commandContainer.aliases())
        {
            List<CommandContainer> list = register.get(s);
            if (list == null)
            {
                register.put(s, toAdd);
            }
            else
            {
                list.addAll(toAdd);
            }
        }
    }

    protected boolean hasCommand(String in)
    {
        return register.containsKey(in.toLowerCase());
    }

    public List<CommandContainer> getCommand(CommandEvent event)
    {
        return getCommand(event.command());
    }

    public List<CommandContainer> getCommand(String command)
    {
        return register.getOrDefault(command, empty);
    }

    public List<String> listCommands()
    {
        return containerMap.keySet().stream()
                .map(CommandContainer::command)
                .sorted()
                .collect(Collectors.toList());
    }

    public final List<String> matchCommands(String input)
    {
        return containerMap.keySet().stream()
                .map(CommandContainer::command)
                .filter(c -> c.startsWith(input))
                .collect(Collectors.toSet())
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> listCommandInfo()
    {
        return containerMap.keySet().stream()
                .map(CommandContainer::toString)
                .sorted()
                .collect(Collectors.toList());
    }

    public final List<String> matchCommandInfo(String input)
    {
        return containerMap.keySet().stream()
                .filter(c -> c.command().startsWith(input))
                .map(CommandContainer::toString)
                .sorted()
                .collect(Collectors.toList());
    }
}
