package me.dags.commandbus.command;

import me.dags.commandbus.args.CommandArg;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */

public class CommandEvent<T>
{
    private final Map<String, Value> flags = new HashMap<String, Value>();
    private final T caller;
    private String command;

    public CommandEvent(T caller)
    {
        this.caller = caller;
    }

    public CommandEvent(T caller, String command)
    {
        this.caller = caller;
        this.command = command;
    }

    protected CommandEvent<T> setCommand(String command)
    {
        this.command = command.toLowerCase();
        return this;
    }

    public void add(String key, Object value)
    {
        flags.put(key.toLowerCase(), new Value(value));
    }

    public void add(String key, Value value)
    {
        flags.put(key.toLowerCase(), value);
    }

    public boolean has(String flag)
    {
        return flags.containsKey(flag.toLowerCase());
    }

    public String command()
    {
        return command;
    }

    public T caller()
    {
        return caller;
    }

    public int size()
    {
        return flags.size();
    }

    public boolean hasOneOf(String... flags)
    {
        for (String flag : flags)
        {
            if (has(flag))
            {
                return true;
            }
        }
        return false;
    }

    public Value get(String flag)
    {
        return flags.get(flag.toLowerCase());
    }

    public Value first(String... flag)
    {
        for (String s : flag)
        {
            Value value = flags.get(s.toLowerCase());
            if (value != null)
            {
                return value;
            }
        }
        return Value.empty;
    }

    public void ifPresent(String key, Consumer<Value> consumer)
    {
        if (hasOneOf(key))
        {
            consumer.accept(get(key));
        }
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder("/").append(command());
        for (Map.Entry<String, Value> e : flags.entrySet())
        {
            sb.append(" ").append(e.getKey()).append(":").append(e.getValue());
        }
        return sb.toString();
    }

    protected Object get(CommandArg arg)
    {
        for (String flag : arg.aliases())
        {
            Value value = flags.get(flag.toLowerCase());
            if (value != null)
            {
                return value.as(arg.type());
            }
        }
        if (arg.type().isInstance(caller))
        {
            return caller;
        }
        if (arg.type().equals(CommandEvent.class))
        {
            return this;
        }
        return null;
    }
}
