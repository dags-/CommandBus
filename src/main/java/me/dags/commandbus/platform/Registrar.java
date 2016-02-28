package me.dags.commandbus.platform;

import me.dags.commandbus.CommandBus;
import me.dags.commandbus.command.CommandContainer;

import java.util.List;

/**
 * @author dags <dags@dags.me>
 */

public interface Registrar
{
    void register(Object plugin, CommandBus commandBus, CommandContainer commandContainer, List<String> aliases);
}
