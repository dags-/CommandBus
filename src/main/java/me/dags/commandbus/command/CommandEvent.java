package me.dags.commandbus.command;

import me.dags.commandbus.flag.Flags;

import java.util.Optional;

/**
 * @author dags_ <dags@dags.me>
 */

public class CommandEvent<T>
{
    private final String command;
    private final Flags flags;
    private final T caller;

    public CommandEvent(T caller, String main, Flags flags)
    {
        this.command = main;
        this.flags = flags;
        this.caller = caller;
    }

    public T caller()
    {
        return caller;
    }

    public String command()
    {
        return command;
    }

    public Flags flags()
    {
        return flags;
    }

    public static <T> Optional<CommandEvent<T>> from(T caller, String in)
    {
        return new CommandParser(in).parse(caller);
    }
}
