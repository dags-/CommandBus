package me.dags.commandbus;

import me.dags.commandbus.annotation.Cmd;
import me.dags.commandbus.annotation.FlagFilter;
import me.dags.commandbus.command.Command;
import me.dags.commandbus.command.CommandEvent;
import me.dags.commandbus.command.Result;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dags_ <dags@dags.me>
 */

public class CommandBus<T>
{
    protected final Map<String, Command<T>> commandMap = new HashMap<>();
    protected Optional<PermissionCheck<T>> permissionCheck = Optional.empty();

    public final List<String> listAll()
    {
        return commandMap.keySet().stream().sorted().collect(Collectors.toList());
    }

    public final List<String> listMatching(String input)
    {
        return commandMap.keySet().stream().filter(s -> s.startsWith(input)).sorted().collect(Collectors.toList());
    }

    public final CommandBus<T> providePermissionCheck(PermissionCheck<T> check)
    {
        if (check != null)
        {
            permissionCheck = Optional.of(check);
        }
        return this;
    }

    public final CommandBus<T> register(Class<?> c)
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

    public final CommandBus<T> register(Object o)
    {
        register(o, o.getClass());
        return this;
    }

    public final Result<T> call(T caller, String commandInput)
    {
        Optional<CommandEvent<T>> event = CommandEvent.from(caller, commandInput);
        if (event.isPresent())
        {
            return call(event.get());
        }
        return Result.parseError("Unable to parse command: " + commandInput);
    }

    public final Result<T> call(CommandEvent<T> event)
    {
        Command<T> c = commandMap.get(event.command());
        if (c != null)
        {
            if (permissionCheck.isPresent() && !c.permission().isEmpty() && !permissionCheck.get().hasPermission(event.caller(), c.permission()))
            {
                return Result.builder(event).success(false).message(Result.PERMISSION_DENIED).build();
            }
            return c.call(event);
        }
        return Result.builder(event).success(false).message(Result.NOT_RECOGNISED).build();
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
                Cmd cmd = m.getAnnotation(Cmd.class);
                if (cmd != null)
                {
                    FlagFilter filter = m.getAnnotation(FlagFilter.class);
                    Command<T> cd = filter != null ? new Command<>(o, m, cmd, filter) : new Command<>(o, m, cmd);
                    cd.register(commandMap);
                }
            }
            c = c.getSuperclass();
        }
    }

    public interface PermissionCheck<T>
    {
        boolean hasPermission(T caller, String permission);
    }
}
