/*
 * The MIT License (MIT)
 *
 * Copyright (c) dags <https://dags.me>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package me.dags.commandbus;

import me.dags.commandbus.command.CommandPath;
import me.dags.commandbus.command.SpongeCommand;
import me.dags.commandbus.command.SpongeCommandBase;
import org.spongepowered.api.Sponge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author dags <dags@dags.me>
 */

/**
 * Used internally by CommandBus to store store commands before registering them
 * with Sponge.
 */
public final class Registry
{
    private final Set<SpongeCommandBase> commands = new HashSet<>();
    private final CommandBus commandBus;

    protected Registry(CommandBus commandBus)
    {
        this.commandBus = commandBus;
    }

    protected void add(SpongeCommand command)
    {
        commands.add(command);
    }

    protected void submit(Object plugin)
    {
        commandBus.info("Registering {} command nodes", commands.size());
        Map<String, SpongeCommandBase> mainCommands = new HashMap<>();
        commands.stream().filter(SpongeCommandBase::isMain)
                .forEach(c -> mainCommands.put(c.alias(), c));

        commandBus.info("Assigning child nodes to parents");
        commands.forEach(c -> findParent(c, mainCommands));

        long count = mainCommands.values().stream().filter(SpongeCommandBase::isMain).count();
        commandBus.info("Registering {} main commands", count);
        mainCommands.values().stream()
                .filter(SpongeCommandBase::isMain)
                .forEach(c -> {
                    commandBus.info("Registering command {}", c.alias());
                    Sponge.getCommandManager().register(plugin, c.spec(), c.aliases());
                });

        commandBus.info("Clearing commands registry");
        commands.clear();
    }

    private void findParent(SpongeCommandBase command, Map<String, SpongeCommandBase> main)
    {
        if (command.isMain())
        {
            main.put(command.command(), command);
            return;
        }

        CommandPath path = command.path();
        String parentPath = path.all();
        if (main.containsKey(parentPath))
        {
            main.get(parentPath).addChild(command);
            return;
        }

        for (SpongeCommandBase c : commands)
        {
            if (c.command().equals(parentPath))
            {
                c.addChild(command);
                return;
            }
        }

        String stubPath = path.to(path.maxDepth() - 1);
        String stubAlias = path.at(path.maxDepth());

        SpongeCommandBase stub = new SpongeCommandBase(stubPath, stubAlias).addChild(command);
        main.put(stub.command(), stub);

        commandBus.info("Created stub for command {}", stub.command());
        findParent(stub, main);
    }
}
