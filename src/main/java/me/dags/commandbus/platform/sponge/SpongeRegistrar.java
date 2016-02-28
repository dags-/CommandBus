package me.dags.commandbus.platform.sponge;

import me.dags.commandbus.CommandBus;
import me.dags.commandbus.command.CommandContainer;
import me.dags.commandbus.platform.Registrar;
import org.spongepowered.api.Sponge;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */

public class SpongeRegistrar implements Registrar
{
    private static final List<String> registerd = new ArrayList<>();

    @Override
    public void register(Object plugin, CommandBus commandBus, CommandContainer commandContainer, List<String> aliases)
    {
        aliases.removeAll(registerd);
        if (aliases.size() > 0)
        {
            Sponge.getCommandManager().register(plugin, new SpongeCommand(commandContainer, commandBus), aliases);
            registerd.addAll(aliases);
        }
    }
}
