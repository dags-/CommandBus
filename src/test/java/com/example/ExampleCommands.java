package com.example;

import me.dags.commandbus.annotation.Arg;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.command.CommandEvent;

/**
 * @author dags_ <dags@dags.me>
 */

public class ExampleCommands
{
    @Command(alias = "main")
    public void example1(CommandEvent<String> commandEvent)
    {
        System.out.println("Caller: " + commandEvent.caller());
        commandEvent.ifPresent("name", f -> System.out.println("name: " + f.string()));
    }

    @Command(alias = {"main sub1", "main s1", "m s1"})
    public void example2(@Caller CharSequence caller, @Arg(a="herp") String herp, @Arg(a="derp") boolean derp)
    {
        System.out.println("Caller: " + caller);
        System.out.println("herp: " + herp);
        System.out.println("derp: " + derp);
    }

    @Command(alias = {"main sub1", "main s1", "m s1"})
    public void example3(@Caller CharSequence caller, @Arg(a="boop") double boop, @Arg(a="baap")String bap)
    {
        System.out.println("Caller: " + caller);
        System.out.println("boop: " + boop);
        System.out.println("baap: " + bap);
    }
}
