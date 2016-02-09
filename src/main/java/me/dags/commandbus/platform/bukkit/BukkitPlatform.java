package me.dags.commandbus.platform.bukkit;

import me.dags.commandbus.CommandBus;
import me.dags.commandbus.command.CommandContainer;
import me.dags.commandbus.platform.PlatformRegistrar;

/**
 * @author dags <dags@dags.me>
 */

public class BukkitPlatform implements PlatformRegistrar
{
    @Override
    public void register(Object pluginSource, CommandContainer commandContainer, CommandBus commandBus)
    {
        // Bukkit.getPluginCommand(commandContainer.command()).setExecutor(new BukkitCommand(commandContainer, commandBus));
    }
}
