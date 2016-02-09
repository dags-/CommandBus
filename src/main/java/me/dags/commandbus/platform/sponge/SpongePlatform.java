package me.dags.commandbus.platform.sponge;

import me.dags.commandbus.CommandBus;
import me.dags.commandbus.platform.PlatformRegistrar;
import me.dags.commandbus.command.CommandContainer;
import org.spongepowered.api.Sponge;

/**
 * @author dags <dags@dags.me>
 */

public class SpongePlatform implements PlatformRegistrar
{
    @Override
    public void register(Object pluginSource, CommandContainer commandContainer, CommandBus commandBus)
    {
        Sponge.getCommandManager().register(pluginSource, new SpongeCommand(commandContainer, commandBus), commandContainer.aliases());
    }
}
