package me.dags.commandbus.args;

/**
 * @author dags <dags@dags.me>
 */

public interface CommandArg
{
    public String[] aliases();

    public Class<?> type();
}
