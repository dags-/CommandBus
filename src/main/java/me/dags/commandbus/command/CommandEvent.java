package me.dags.commandbus.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CommandEvent<T>
{
	private final Map<String, Value> args = new HashMap<>();
	private final T caller;
	private final String command;

	public CommandEvent(T caller, String command)
	{
		this.caller = caller;
		this.command = command;
	}

	public T caller()
	{
		return caller;
	}

	public String command()
	{
		return command;
	}

    public boolean hasAlias(String arg)
    {
        return args.containsKey(arg.toLowerCase());
    }

	public Value get(String alias)
	{
		return args.get(alias.toLowerCase());
	}

    public Value first(String... flag)
    {
        for (String s : flag)
        {
            Value value = args.get(s.toLowerCase());
            if (value != null)
            {
                return value;
            }
        }
        return Value.empty;
    }

    public void ifPresent(String name, Consumer<Value> consumer)
    {
        if (hasAlias(name))
        {
            consumer.accept(args.get(name));
        }
    }

	protected boolean exactMatch(List<Argument> arguments)
    {
        for (Map.Entry<String, Value> e : args.entrySet())
        {
            boolean match = false;
            outer:
            for (Argument a : arguments)
            {
                for (String s : a.aliases())
                {
                    if (s.equalsIgnoreCase(e.getKey()))
                    {
                        match = true;
                        break outer;
                    }
                }
            }
            if (!match)
            {
                return false;
            }
        }
        return true;
    }

    protected void add(String arg, Value value)
    {
        args.put(arg,  value);
    }

	protected Object callerOrEvent(Argument arg)
	{
		if (arg.type().isInstance(caller))
		{
			return caller;
		}
		return this;
	}

	protected Value get(Argument arg)
	{
		for (String alias : arg.aliases())
		{
			Value v = get(alias);
			if (v != null)
			{
				return v;
			}
		}
		return null;
	}

	protected boolean has(Argument arg)
	{
		if (!arg.isArg())
		{
			return arg.type().isInstance(caller) || arg.type().equals(this.getClass());
		}
		for (String alias : arg.aliases())
		{
			if (hasAlias(alias))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("/").append(command);
		for (Map.Entry<String, Value> e : args.entrySet())
		{
			sb.append(" ").append(e.getKey()).append(e.getValue().debug());
		}
		return sb.toString();
	}
}
