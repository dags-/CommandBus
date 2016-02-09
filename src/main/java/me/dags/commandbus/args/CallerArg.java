package me.dags.commandbus.args;

/**
 * @author dags <dags@dags.me>
 */

public class CallerArg implements CommandArg
{
    private final Class<?> type;

    public CallerArg(Class<?> type)
    {
        this.type = type;
    }

    @Override
    public String[] aliases()
    {
        return new String[0];
    }

    @Override
    public Class<?> type()
    {
        return type;
    }
}
