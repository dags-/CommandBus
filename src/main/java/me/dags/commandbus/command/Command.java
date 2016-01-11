package me.dags.commandbus.command;

import me.dags.commandbus.annotation.Cmd;
import me.dags.commandbus.annotation.FlagFilter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * @author dags_ <dags@dags.me>
 */

public class Command<T>
{
    private final Object owner;
    private final Method target;
    private final Cmd cmd;
    private final Optional<FlagFilter> filter;

    public Command(Object o, Method m, Cmd c)
    {
        owner = o;
        target = m;
        cmd = c;
        filter = Optional.empty();
    }

    public Command(Object o, Method m, Cmd c, FlagFilter f)
    {
        owner = o;
        target = m;
        cmd = c;
        filter = Optional.of(f);
    }

    public void register(Map<String, Command<T>> commands)
    {
        for (String s : cmd.aliases()) commands.put(s, this);
    }

    public String permission()
    {
        return cmd.permission();
    }

    public Result<T> call(CommandEvent<T> event)
    {
        Result.Builder<T> resultBuilder = filter(event);
        if (resultBuilder.success())
        {
            try
            {
                target.invoke(owner, event);
                return resultBuilder.message("Successfully executed command.").build();
            }
            catch (Throwable t)
            {
                t.printStackTrace();
                return resultBuilder.message("Error occurred while invoking command!").build();
            }
        }
        return resultBuilder.build();
    }

    public Result.Builder<T> filter(CommandEvent<T> event)
    {
        if (!filter.isPresent())
        {
            return Result.builder(event).success(true).message("No filters present, all flags allowed.");
        }
        for (String s : filter.get().requireFlags())
        {
            if (!event.flags().hasFlag(s))
            {

                return Result.builder(event)
                        .success(false)
                        .message("The following flags are required: " + Arrays.toString(filter.get().requireFlags()));
            }
        }
        for (String s : filter.get().blockFlags())
        {
            if (event.flags().hasFlag(s))
            {
                return Result.builder(event)
                        .success(false)
                        .message("The following flags are blocked: " + Arrays.toString(filter.get().blockFlags()));
            }
        }
        return Result.builder(event).success(true).message("Passed all filters!");
    }
}
