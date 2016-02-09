package me.dags.commandbus;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.command.CommandContainer;
import me.dags.commandbus.command.CommandEvent;
import me.dags.commandbus.command.CommandParser;
import me.dags.commandbus.command.Result;
import me.dags.commandbus.platform.Platform;

import java.lang.reflect.Method;
import java.util.List;
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
        Optional<CommandEvent<T>> event = new CommandParser(commandInput).parse(caller);
        if (event.isPresent())
        {
            return post(event.get());
        }
        return Result.Type.PARSE_ERROR.toResult(commandInput);
    }

    public final <T> Result post(CommandEvent<T> event)
    {
        List<CommandContainer> list = register.getCommand(event);
        if (!list.isEmpty())
        {
            Result result = null;
            for (CommandContainer c : list)
            {
                if (permissionCheck.isPresent() && c.hasPermission() && !permissionCheck.get().hasPermission(event.caller(), c.permission()))
                {
                    Result perm = Result.Type.NO_PERMISSION.toResult(c.permission());
                    result = result == null ? perm : result.type == Result.Type.SUCCESS ? result : perm;
                    continue;
                }
                Result call = c.call(event);
                if (call.type == Result.Type.SUCCESS)
                {
                    return call;
                }
                result = call;
            }
            return result;
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
        public boolean hasPermission(Object target, String permission);
    }
}
