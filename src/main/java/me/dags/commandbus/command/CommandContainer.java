package me.dags.commandbus.command;

import me.dags.commandbus.annotation.Command;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandContainer
{
	private final Command command;
	private final Object owner;
	private final Method method;
	private final List<Argument> args;
    private final String root;

	public CommandContainer(Command command, Object owner, Method method)
	{
		if (command.alias().length == 0 || command.alias()[0].isEmpty())
		{
			throw new UnsupportedOperationException("Command String cannot be empty!");
		}

		Parameter[] params = method.getParameters();
		List<Argument> arguments = new ArrayList<>();
		for (Parameter p : params)
		{
			arguments.add(new Argument(p));
		}

		if (arguments.isEmpty())
		{
			throw new UnsupportedOperationException("Missing parameters!");
		}

		this.command = command;
		this.owner = owner;
		this.method = method;
		this.args = Collections.unmodifiableList(arguments);
        this.root = command.alias()[0].split(" ")[0];
	}

    public boolean exactMatchFor(CommandEvent<?> event)
    {
        return event.exactMatch(args);
    }

    public boolean matchFor(CommandEvent<?> event)
	{
		for (Argument a :  args)
		{
			if (!event.has(a))
			{
				return false;
			}
		}
		return true;
	}

    public <T> Result call(CommandEvent<T> event)
	{
		Object[] params = new Object[args.size()];
		for (int i = 0; i < args.size(); i++)
		{
			Argument arg = args.get(i);
			Object object;
			if (arg.isArg())
			{
				Value value = event.get(arg);
				if (value == null)
				{
					return Result.Type.MISSING_ARG.toResult(arg.toString());
				}
				if ((object = value.as(arg.type())) == null)
				{
					return Result.Type.INCORRECT_TYPE.toResult(value.debug());
				}
			}
			else
			{
				object = event.callerOrEvent(arg);
			}
			params[i] = object;
		}

		try
		{
			method.invoke(owner, params);
			return Result.Type.SUCCESS.toResult(event.toString());
		}
		catch(Throwable t)
		{
			return Result.Type.CALL_ERROR.toResult("Command: " + this + " -> Input: " + event);
		}
	}

	public boolean hasPermission()
	{
		return !command.perm().isEmpty();
	}

	public boolean hasDescription()
	{
		return !command.desc().isEmpty();
	}

    public String root()
    {
        return root;
    }

	public String command()
	{
		return command.alias()[0];
	}

	public String[] aliases()
	{
		return command.alias();
	}

	public String permission()
	{
		return command.perm();
	}

	public String description()
	{
		return command.desc();
	}

	public String arguments()
	{
		StringBuilder sb = new StringBuilder();
		args.stream().filter(Argument::isArg).forEach(a -> sb.append(" ").append(a));
		return sb.toString();
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("/");
		sb.append(command.alias()[0]);
		sb.append(arguments());
		if (this.hasDescription())
			sb.append(" - ").append(description());
		if (this.hasPermission())
			sb.append(" [").append(permission()).append("]");
		return sb.toString();
	}
}
