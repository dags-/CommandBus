package me.dags.commandbusold;

import me.dags.commandbusold.annotation.Command;
import me.dags.commandbusold.command.CommandContainer;
import me.dags.commandbusold.command.CommandEvent;
import me.dags.commandbusold.command.CommandParser;
import me.dags.commandbusold.command.Result;
import me.dags.commandbusold.platform.Platform;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */

public class CommandBus
{
    private Optional<PermissionCheck> permissionCheck = Optional.empty();
    private final CommandRegister register;

    public CommandBus()
    {
        this(Platform.NONE);
    }

    public CommandBus(Platform platform)
    {
        register = new CommandRegister(this, platform.getPlatformRegistrar());
    }

    public final CommandRegister getRegister()
    {
        return register;
    }

    public final CommandBus providePermissionCheck(PermissionCheck check)
    {
        if (check != null)
        {
            permissionCheck = Optional.of(check);
        }
        return this;
    }

    public final CommandBus register(Object owner, Class<?> c)
    {
        try
        {
            register(owner, c.newInstance(), c);
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return this;
    }

    public final CommandBus register(Object owner, Object o)
    {
        register(owner, o, o.getClass());
        return this;
    }

    public final <T> Result post(T caller, String commandInput)
    {
        Optional<CommandEvent<T>> event = new CommandParser(commandInput).parses(caller);
        if (event.isPresent())
        {
            return post(event.get());
        }
        return Result.Type.PARSE_ERROR.toResult(commandInput);
    }

    public final <T> Result post(CommandEvent<T> event)
    {
        if (register.hasCommand(event.command()))
        {
            CommandContainer c = register.find(event);
            if (c != null)
            {
                if (permissionCheck.isPresent() && c.hasPermission() && !permissionCheck.get().hasPermission(event.caller(), c.permission()))
                {
                    return Result.Type.NO_PERMISSION.toResult(c.permission());
                }
                return c.call(event);
            }
            return Result.Type.MISSING_FLAG.toResult(event.toString());
        }
        return Result.Type.NOT_RECOGNISED.toResult(event.command());
    }

    private void register(Object owner, Object o, Class<?> c)
    {
        if (o == null || c == null)
        {
            return;
        }

        while (!Object.class.equals(c))
        {
            for (Method m : c.getDeclaredMethods())
            {
                if (m.isAnnotationPresent(Command.class))
                {
                    CommandContainer container = new CommandContainer(m.getAnnotation(Command.class), m, o);
                    register.addCommand(owner, container);
                }
            }
            c = c.getSuperclass();
        }
    }

    public interface PermissionCheck
    {
        boolean hasPermission(Object target, String permission);
    }
}
