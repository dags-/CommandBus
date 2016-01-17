package com.example;

import me.dags.commandbus.CommandBus;
import me.dags.commandbus.command.Result;

/**
 * @author dags_ <dags@dags.me>
 */

public class Main
{
    public static final String example1 = "/main";
    public static final String example2 = "/main s1 name:'some string' derp:false herp:1337";
    public static final String example3 = "/main s1 name:'some string' d:false hs:1337";

    private static final CommandBus bus = new CommandBus().register(ExampleCommands.class);

    public static void main(String[] args)
    {
        runTest("Example1", example1);
        runTest("Example2", example2);

        // Demonstrates filters for required flags
        runTest("Example3", example3);
    }

    private static void runTest(String owner, String command)
    {
        System.out.println("=========: " + owner);

        Result result = bus.call(owner, command);
        result.onFail(r -> System.out.println(r.message));

        System.out.println();
    }
}
