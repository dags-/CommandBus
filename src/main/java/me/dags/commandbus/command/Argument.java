package me.dags.commandbus.command;

import me.dags.commandbus.annotation.Arg;
import me.dags.commandbus.annotation.Caller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

public class Argument
{
	private static final String[] empty = new String[0];

	private final Class<?> type;
	private final String[] aliases;

	protected Argument(Parameter parameter)
	{
		for (Annotation a : parameter.getAnnotations())
		{
			if (a instanceof Caller)
			{
				type = nonPrimitive(parameter.getType());
				aliases = empty;
				return;
			}
			if (a instanceof Arg)
			{
				type = nonPrimitive(parameter.getType());
				aliases = ((Arg) a).value();
				return;
			}
		}
		throw new UnsupportedOperationException("Missing Annotation!");
	}

	protected boolean isArg()
	{
		return aliases != empty;
	}

	protected Class<?> type()
	{
		return type;
	}

	protected String[] aliases()
	{
		return aliases;
	}

	@Override
	public String toString()
	{
		if (!isArg())
		{
			return "{}";
		}
		return aliases[0] + "(" + type().getSimpleName() + ")";
	}

	private static Class<?> nonPrimitive(Class<?> type)
	{
		if (type.isPrimitive())
		{
			if (type.equals(int.class)) type = Integer.class;
			if (type.equals(double.class)) type = Double.class;
			if (type.equals(boolean.class)) type = Boolean.class;
		}
		return type;
	}
}
