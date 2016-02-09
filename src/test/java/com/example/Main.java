package com.example;

import me.dags.commandbus.CommandBus;
import me.dags.commandbus.command.Result;

/**
 * @author dags_ <dags@dags.me>
 */

public class Main
{
    public static final String example1 = "/main";
    public static final String example2 = "/main s1 name:some string derp:false herp:adasdasd";
    public static final String example3 = "/main s1 boop:723.7190 bap:342 324 12";

    private static final CommandBus bus = new CommandBus().register(null, ExampleCommands.class);

    public static void main(String[] args)
    {
        runTest("Example1", example1);
        runTest("Example2", example2);
        runTest("Example3", example3);
    }

    private static void runTest(String owner, String command)
    {
        System.out.println("=========: " + owner);
        Result result = bus.post(owner, command);
        result.onFail(r -> System.out.println(r.message));
        System.out.println();
    }
}
