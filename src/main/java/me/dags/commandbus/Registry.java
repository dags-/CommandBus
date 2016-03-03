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

import me.dags.commandbus.command.SpongeCommand;
import me.dags.commandbus.command.SpongeCommandStub;
import org.spongepowered.api.Sponge;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */

public final class Registry
{
    private final Set<SpongeCommand> commands = new HashSet<>();
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
        commandBus.info("Building command trees");

        Map<String, SpongeCommand> mainCommands = new HashMap<>();
        commandBus.info("Finding main commands");
        commands.stream().filter(SpongeCommand::isMain).forEach(c -> mainCommands.put(c.main(), c));

        commandBus.info("Assigning child commands");
        commands.forEach(c -> findParent(c, mainCommands));

        commandBus.info("Registering {} commands", mainCommands.size());
        mainCommands.values().forEach(c -> Sponge.getCommandManager().register(plugin, c.spec(), c.aliases()));
    }

    private void findParent(SpongeCommand command, Map<String, SpongeCommand> mainCommands)
    {
        if (command.isMain())
        {
            mainCommands.put(command.main(), command);
            return;
        }

        int depth = command.path().maxDepth();
        while (depth > 0)
        {
            String path = command.path().to(depth);
            Collection<SpongeCommand> found = find(path);
            if (found.isEmpty())
            {
                depth--;
                continue;
            }
            found.forEach(c -> c.child(command));
            return;
        }

        if (depth == 0)
        {
            String key = command.path().at(0);
            if (mainCommands.containsKey(key))
            {
                mainCommands.get(key).child(command);
                return;
            }
        }

        SpongeCommandStub dummy = new SpongeCommandStub(command.path().at(depth), command.path().to(depth - 1));
        dummy.child(command);
        findParent(dummy, mainCommands);
    }

    private Collection<SpongeCommand> find(String path)
    {
        return commands.stream().filter(c -> c.pathString().equals(path)).collect(Collectors.toList());
    }
}
