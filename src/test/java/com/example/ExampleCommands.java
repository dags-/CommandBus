package com.example;

import me.dags.commandbus.annotation.Cmd;
import me.dags.commandbus.annotation.FlagFilter;
import me.dags.commandbus.command.CommandEvent;

/**
 * @author dags_ <dags@dags.me>
 */

public class ExampleCommands
{
    @Cmd(aliases = "main")
    public void example1(CommandEvent<String> commandEvent)
    {
        System.out.println("Caller: " + commandEvent.caller());
        commandEvent.flags().ifPresent("name", f -> System.out.println("name: " + f.string()));
    }

    @Cmd(aliases = {"main sub1", "main s1", "m s1"})
    @FlagFilter(requireFlags = {"herp", "derp"})
    public void example2(CommandEvent<String> commandEvent)
    {
        int herp = commandEvent.flags().getFlag("herp").number().intValue();
        boolean derp = commandEvent.flags().getFlag("derp").bool();

        System.out.println("Caller: " + commandEvent.caller());
        System.out.println("herp: " + herp);
        System.out.println("derp: " + derp);
    }
}
