package me.dags.commandbus.args;

import me.dags.commandbus.annotation.Arg;

/**
 * @author dags <dags@dags.me>
 */

public class AnnotatedArg implements CommandArg
{
    public final Arg arg;
    public final Class<?> type;

    public AnnotatedArg(Arg arg, Class<?> type)
    {
        this.arg = arg;
        this.type = type;
    }

    @Override
    public String[] aliases()
    {
        return arg.a();
    }

    @Override
    public Class<?> type()
    {
        return type;
    }

    @Override
    public String toString()
    {
        return arg.a()[0] + "(" + type.getSimpleName() + ")";
    }
}
