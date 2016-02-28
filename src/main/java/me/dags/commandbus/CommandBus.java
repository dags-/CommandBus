package me.dags.commandbus;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.command.CommandContainer;
import me.dags.commandbus.command.CommandEvent;
import me.dags.commandbus.command.CommandParser;
import me.dags.commandbus.command.Result;
import me.dags.commandbus.platform.PermissionCheck;
import me.dags.commandbus.platform.Platform;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class CommandBus
{
	private final Registry registry;
	private final Optional<PermissionCheck> permissionCheck = Optional.empty();

    public CommandBus()
    {
        this(Platform.NONE);
    }

    public CommandBus(Platform platform)
    {
        registry = new Registry(platform.getPlatformRegistrar());
    }

	public Registry registry()
	{
		return registry;
	}

    public <T> Result post(T caller, String input)
    {
        Optional<CommandEvent<T>> event = new CommandParser(input).parse(caller);
        if (!event.isPresent())
        {
            return Result.Type.PARSE_ERROR.toResult(input);
        }
        return post(event.get());
    }

	public <T> Result post(CommandEvent<T> event)
	{
		Optional<CommandContainer> optional = registry.matchOne(event);
		if (!optional.isPresent())
		{
            List<CommandContainer> options = registry.matchAny(event);
            if (options.isEmpty())
            {
                return Result.Type.NOT_RECOGNISED.toResult(event.toString());
            }
            StringBuilder sb = new StringBuilder(event.toString()).append("\nTry:");
            options.forEach(c -> sb.append("\n").append(c));
            return Result.Type.TOO_MANY_ARGS.toResult(sb.toString());
		}
        CommandContainer c = optional.get();
        if (c.hasPermission() && permissionCheck.isPresent() && !permissionCheck.get().hasPermission(event.command(), c.permission()))
        {
            return Result.Type.NO_PERMISSION.toResult(c.permission());
        }
        return c.call(event);
	}

    public void register(Object plugin, Class<?> c)
    {
        try
        {
            Object o = c.newInstance();
            registry(plugin, o);
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    public void registry(Object plugin, Object command)
    {
        Class<?> c = command.getClass();
        do
        {
            for (Method m : command.getClass().getDeclaredMethods())
            {
                if (m.isAnnotationPresent(Command.class))
                {
                    Command cmd = m.getAnnotation(Command.class);
                    CommandContainer container = new CommandContainer(cmd, command, m);
                    registry.register(this, plugin, container);
                }
            }
            c = c.getSuperclass();
        }
        while (!Object.class.equals(c));
    }
}
