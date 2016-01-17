package com.example;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.FlagFilter;
import me.dags.commandbus.command.CommandEvent;

/**
 * @author dags_ <dags@dags.me>
 */

public class ExampleCommands
{
    @Command(command = "main")
    public void example1(CommandEvent<String> commandEvent)
    {
        System.out.println("Caller: " + commandEvent.caller());
        commandEvent.flags().ifPresent("name", f -> System.out.println("name: " + f.string()));
    }

    @Command(command = {"main sub1", "main s1", "m s1"})
    @FlagFilter(require = {"h|herp", "d|derp"})
    public void example2(CommandEvent<String> commandEvent)
    {
        int herp;
        boolean derp;
        if (commandEvent.flags().hasFlag("h"))
        {
            herp = commandEvent.flags().get("h").number().intValue();
            derp = commandEvent.flags().get("d").bool();
        }
        else
        {
            herp = commandEvent.flags().get("herp").number().intValue();
            derp = commandEvent.flags().get("derp").bool();
        }

        System.out.println("Caller: " + commandEvent.caller());
        System.out.println("herp: " + herp);
        System.out.println("derp: " + derp);
    }
}
