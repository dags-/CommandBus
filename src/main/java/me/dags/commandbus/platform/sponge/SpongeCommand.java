package me.dags.commandbus.platform.sponge;

import me.dags.commandbus.CommandBus;
import me.dags.commandbus.command.CommandContainer;
import me.dags.commandbus.command.Result;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */

public class SpongeCommand implements CommandCallable
{
    private final CommandContainer commandContainer;
    private final CommandBus commandBus;

    public SpongeCommand(CommandContainer commandContainer, CommandBus commandBus)
    {
        this.commandContainer = commandContainer;
        this.commandBus = commandBus;
    }

    @Override
    public CommandResult process(CommandSource commandSource, String s) throws CommandException
    {
        Result result = commandBus.post(commandSource, commandContainer.command() + " " + s);

        if (result.type == Result.Type.SUCCESS)
        {
            return CommandResult.success();
        }
        commandSource.sendMessage(Text.of(result.message));
        return CommandResult.empty();
    }

    @Override
    public List<String> getSuggestions(CommandSource commandSource, String s) throws CommandException
    {
        return commandBus.getRegister().matchCommands(s);
    }

    @Override
    public boolean testPermission(CommandSource commandSource)
    {
        return !commandContainer.hasPermission() || commandSource.hasPermission(commandContainer.permission());
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource commandSource)
    {
        return Optional.of(Text.of(commandContainer.description()));
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource commandSource)
    {
        return Optional.of(getUsage(commandSource));
    }

    @Override
    public Text getUsage(CommandSource commandSource)
    {
        return Text.of(commandContainer);
    }
}
