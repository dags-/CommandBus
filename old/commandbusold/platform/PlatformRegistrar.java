package me.dags.commandbusold.platform;

import me.dags.commandbusold.CommandBus;
import me.dags.commandbusold.command.CommandContainer;

/**
 * @author dags <dags@dags.me>
 */

public interface PlatformRegistrar
{
    public void register(Object pluginSource, CommandContainer commandContainer, CommandBus commandBus);
}
