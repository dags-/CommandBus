package me.dags.commandbusold.command;

import me.dags.commandbusold.annotation.Arg;
import me.dags.commandbusold.annotation.Caller;
import me.dags.commandbusold.annotation.Command;
import me.dags.commandbusold.args.AnnotatedArg;
import me.dags.commandbusold.args.CallerArg;
import me.dags.commandbusold.args.CommandArg;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.stream.Stream;

/**
 * @author dags <dags@dags.me>
 */

public class CommandContainer
{
    private final Command command;
    private final Method target;
    private final Object owner;
    private final CommandArg[] args;

    public CommandContainer(Command command, Method target, Object owner)
    {
        Class<?>[] paramTypes = target.getParameterTypes();
        Annotation[][] paramAnnos = target.getParameterAnnotations();
        if (paramTypes.length == 0)
        {
            throw new UnsupportedOperationException("No params");
        }
        this.command = command;
        this.target = target;
        this.owner = owner;
        this.args = args(paramTypes, paramAnnos);
    }

    public String command()
    {
        return command.alias()[0];
    }

    public String[] aliases()
    {
        return command.alias();
    }

    public boolean hasPermission()
    {
        return !permission().isEmpty();
    }

    public String permission()
    {
        return command.permission();
    }

    public String description()
    {
        return command.description();
    }

    public boolean matches(CommandEvent<?> event)
    {
        for (CommandArg arg : args)
        {
            if (arg instanceof CallerArg || event.hasOneOf(arg.aliases()))
            {
                continue;
            }
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("/").append(command()).append(" ");
        Stream.of(args).filter(AnnotatedArg.class::isInstance).map(CommandArg::toString).forEach(s -> sb.append(s).append(" "));
        if (sb.charAt(sb.length() - 1) == ' ') sb.deleteCharAt(sb.length() - 1);
        if (!description().isEmpty()) sb.append(" - ").append(description());
        if (!permission().isEmpty()) sb.append(" [").append(permission()).append("]");
        return sb.toString();
    }

    public <T> Result call(CommandEvent<T> event)
    {
        Object[] invokeArgs = new Object[args.length];
        int i = 0;
        for (CommandArg arg : args)
        {
            Object value = event.get(arg);
            if (value == null)
            {
                return Result.Type.MISSING_FLAG.toResult(arg.toString());
            }
            invokeArgs[i++] = value;
        }
        try
        {
            target.invoke(owner, invokeArgs);
            return Result.Type.SUCCESS.toResult("Pass");
        }
        catch (Exception e)
        {
            return Result.Type.CALL_ERROR.toResult(e.getMessage());
        }
    }

    private static CommandArg[] args(Class<?>[] paramTypes, Annotation[][] paramAnnos)
    {
        CommandArg[] args = new CommandArg[paramTypes.length];
        for (int i = 0; i < args.length; i++)
        {
            Annotation[] annos = paramAnnos[i];
            if (annos.length == 0)
            {
                if (!paramTypes[i].equals(CommandEvent.class))
                {
                    throw new UnsupportedOperationException("Method has unannotated parameter that is NOT of type 'CommandEvent'");
                }
                args[i] = new CallerArg(CommandEvent.class);
                continue;
            }
            for (Annotation a : annos)
            {
                Class<?> type = paramTypes[i];
                if (a instanceof Arg)
                {
                    if (!Value.validType(type))
                    {
                        throw new UnsupportedOperationException("Invalid parameter type " + type.getSimpleName());
                    }
                    if (type.isPrimitive())
                    {
                        if (type.equals(int.class)) type = Integer.class;
                        if (type.equals(double.class)) type = Double.class;
                        if (type.equals(boolean.class)) type = Boolean.class;
                    }
                    args[i] = new AnnotatedArg((Arg) a, type);
                    break;
                }
                if (a instanceof Caller)
                {
                    args[i] = new CallerArg(paramTypes[i]);
                }
            }
        }
        return args;
    }
}
