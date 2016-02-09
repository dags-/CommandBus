package me.dags.commandbus.platform;

import me.dags.commandbus.CommandBus;
import me.dags.commandbus.command.CommandContainer;

/**
 * @author dags <dags@dags.me>
 */

public interface PlatformRegistrar
{
    public void register(Object pluginSource, CommandContainer commandContainer, CommandBus commandBus);
}
