package me.dags.commandbus;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.FlagFilter;
import me.dags.commandbus.command.CommandContainer;
import me.dags.commandbus.command.CommandEvent;
import me.dags.commandbus.command.Result;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author dags_ <dags@dags.me>
 */

public class CommandBus
{
    protected final Map<String, CommandContainer> commandMap = new HashMap<>();
    protected Optional<PermissionCheck> permissionCheck = Optional.empty();
    
    public final CommandBus providePermissionCheck(PermissionCheck check)
    {
        if (check != null)
        {
            permissionCheck = Optional.of(check);
        }
        return this;
    }

    public final CommandBus register(Class<?> c)
    {
        try
        {
            register(c.newInstance(), c);
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return this;
    }

    public final CommandBus register(Object o)
    {
        register(o, o.getClass());
        return this;
    }

    public final <T> Result call(T caller, String commandInput)
    {
        Optional<CommandEvent<T>> event = CommandEvent.from(caller, commandInput);
        if (event.isPresent())
        {
            return call(event.get());
        }
        return Result.Type.PARSE_ERROR.toResult(commandInput);
    }

    public final <T> Result call(CommandEvent<T> event)
    {
        CommandContainer c = commandMap.get(event.command());
        if (c != null)
        {
            if (permissionCheck.isPresent() && !c.permission().isEmpty() && !permissionCheck.get().hasPermission(event.caller(), c.permission()))
            {
                return Result.Type.NO_PERMISSION.toResult(c.permission());
            }
            return c.call(event);
        }
        return Result.Type.NOT_RECOGNISED.toResult(event.command());
    }

    public final List<String> listAll()
    {
        return commandMap.keySet().stream().sorted().collect(Collectors.toList());
    }

    public final List<String> listMatching(String input)
    {
        return commandMap.keySet().stream().filter(s -> s.startsWith(input)).sorted().collect(Collectors.toList());
    }

    private void register(Object o, Class<?> c)
    {
        if (o == null || c == null)
        {
            return;
        }

        while (!Object.class.equals(c))
        {
            for (Method m : c.getDeclaredMethods())
            {
                Command cmd = m.getAnnotation(Command.class);
                if (cmd != null)
                {
                    FlagFilter filter = m.getAnnotation(FlagFilter.class);
                    CommandContainer command = filter != null ? new CommandContainer(o, m, cmd, filter) : new CommandContainer(o, m, cmd);
                    for (String s : cmd.command()) commandMap.put(s, command);
                }
            }
            c = c.getSuperclass();
        }
    }

    public interface PermissionCheck
    {
        public boolean hasPermission(Object target, String permission);
    }
}
