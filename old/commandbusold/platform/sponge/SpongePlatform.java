package me.dags.commandbusold.platform.sponge;

import me.dags.commandbusold.CommandBus;
import me.dags.commandbusold.command.CommandContainer;
import me.dags.commandbusold.command.Result;
import me.dags.commandbusold.platform.PlatformRegistrar;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */

public class SpongePlatform implements PlatformRegistrar, CommandBus.PermissionCheck
{
    private CommandBus commandBus = null;

    @Override
    public void register(Object pluginSource, CommandContainer commandContainer, CommandBus commandBus)
    {
        if (this.commandBus == null)
        {
            this.commandBus = commandBus;
            this.commandBus.providePermissionCheck(this);
            Sponge.getEventManager().registerListeners(pluginSource, this);
        }

        Sponge.getCommandManager().register(pluginSource, new SpongeCommand(commandContainer, commandBus), commandContainer.aliases());
    }

    @Listener
    public void onCommand(SendCommandEvent e)
    {
        if (commandBus == null) return;

        Optional<CommandSource> source = e.getCause().first(CommandSource.class);
        if (source.isPresent())
        {
            Result result = commandBus.post(source.get(), e.getCommand() + " " + e.getArguments());
            result.onPass(r -> e.setCancelled(true)).onFail(r ->
            {
                if (r.type != Result.Type.PARSE_ERROR && r.type != Result.Type.NOT_RECOGNISED)
                {
                    e.setCancelled(true);
                    source.get().sendMessage(Text.builder(r.message).color(TextColors.RED).build());
                }
            });
        }
    }

    @Override
    public boolean hasPermission(Object target, String permission)
    {
        return ((CommandSource) target).hasPermission(permission);
    }
}
