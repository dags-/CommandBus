package me.dags.commandbus.platform.bukkit;

import me.dags.commandbus.CommandBus;
import me.dags.commandbus.command.CommandContainer;

/**
 * @author dags <dags@dags.me>
 */

public class BukkitCommand
{
    private final CommandContainer commandContainer;
    private final CommandBus commandBus;

    public BukkitCommand(CommandContainer commandContainer, CommandBus commandBus)
    {
        this.commandContainer = commandContainer;
        this.commandBus = commandBus;
    }
}
/*
public class BukkitCommand implements CommandExecutor
{
    private final CommandContainer commandContainer;
    private final CommandBus commandBus;

    public BukkitCommand(CommandContainer commandContainer, CommandBus commandBus)
    {
        this.commandContainer = commandContainer;
        this.commandBus = commandBus;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
    {
        return false;
    }
}
*/
