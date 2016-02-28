package com.example;

import me.dags.commandbus.annotation.Arg;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;

/**
 * @author dags_ <dags@dags.me>
 */

public class ExampleCommands
{
    @Command(alias = "main")
    public void example1(@Caller CharSequence caller)
    {
        System.out.println("Caller: " + caller);
    }

    @Command(alias = {"main sub1", "main s1", "m s1"})
    public void example2(@Caller CharSequence caller, @Arg("herp") String herp, @Arg("derp") boolean derp)
    {
        System.out.println("Caller: " + caller);
        System.out.println("herp: " + herp);
        System.out.println("derp: " + derp);
    }

    @Command(alias = {"main sub1", "main s1", "m s1"})
    public void example3(@Caller CharSequence caller, @Arg("boop") double boop, @Arg("baap")String bap)
    {
        System.out.println("Caller: " + caller);
        System.out.println("boop: " + boop);
        System.out.println("baap: " + bap);
    }
}
