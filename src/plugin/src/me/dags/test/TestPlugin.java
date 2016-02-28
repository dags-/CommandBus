package me.dags.test;

import me.dags.commandbus.CommandBus;
import me.dags.commandbus.platform.Platform;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

/**
 * @author dags <dags@dags.me>
 */

@Plugin(name = "test", id = "test")
public class TestPlugin
{
    private static final CommandBus commandBus = new CommandBus(Platform.SPONGE);

    @Listener
    public void init(GameInitializationEvent e)
    {
        commandBus.register(this, Commands.class);
    }
}
