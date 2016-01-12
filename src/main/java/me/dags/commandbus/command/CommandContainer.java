package me.dags.commandbus.command;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.FlagFilter;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @author dags_ <dags@dags.me>
 */

public class CommandContainer
{
    private final Object owner;
    private final Method target;
    private final Command cmd;
    private final Optional<FlagFilter> filter;

    public CommandContainer(Object o, Method m, Command c)
    {
        owner = o;
        target = m;
        cmd = c;
        filter = Optional.empty();
    }

    public CommandContainer(Object o, Method m, Command c, FlagFilter f)
    {
        owner = o;
        target = m;
        cmd = c;
        filter = Optional.of(f);
    }

    public String permission()
    {
        return cmd.permission();
    }

    public <T> Result call(CommandEvent<T> event)
    {
        Result result = filter(event);
        if (result.type == Result.Type.SUCCESS)
        {
            try
            {
                target.invoke(owner, event);
                return result;
            }
            catch (Throwable t)
            {
                t.printStackTrace();
                return Result.Type.CALL_ERROR.toResult(event.command());
            }
        }
        return result;
    }

    public <T> Result filter(CommandEvent<T> event)
    {
        if (filter.isPresent())
        {
            return event.flags().filter(filter.get());
        }
        return Result.Type.SUCCESS.toResult("No filters, automatic pass!");
    }
}
