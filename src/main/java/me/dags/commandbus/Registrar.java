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

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.command.CommandMethod;
import me.dags.commandbus.command.CommandNode;
import me.dags.commandbus.command.CommandPath;
import me.dags.commandbus.command.SpongeCommand;
import org.spongepowered.api.Sponge;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
class Registrar {

    private final Map<String, CommandNode> roots = new HashMap<>();
    private final CommandBus commandBus;

    Registrar(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    void register(Object object) {
        Class<?> c = object.getClass();
        int count = 0;
        do {
            for (Method method : c.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Command.class)) {
                    try {
                        CommandMethod commandMethod = new CommandMethod(object, method);
                        CommandNode commandNode = getParentTree(commandMethod.command());
                        commandNode.addAliases(commandMethod.command().aliases());
                        commandNode.addCommandMethod(commandMethod);
                        count++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            c = c.getSuperclass();
        } while (c != null && !c.equals(Object.class));
        commandBus.info("Found {} commands in class {}", count, object.getClass().getSimpleName());
    }

    void submit(Object plugin) {
        roots.values().stream().map(SpongeCommand::new).forEach(c -> Sponge.getCommandManager().register(plugin, c, c.aliases()));
        commandBus.info("Registered {} main commands", roots.size());
    }

    private CommandNode getRoot(String arg) {
        CommandNode root = roots.get(arg);
        if (root == null) {
            roots.put(arg, root = new CommandNode(arg));
        }
        return root;
    }

    private CommandNode getParentTree(Command command) {
        if (command.parent().isEmpty()) {
            return getRoot(command.aliases()[0]);
        } else {
            CommandPath args = new CommandPath(command.parent());
            CommandNode node = getRoot(args.nextArg());
            while (args.hasNext()) {
                node = node.getOrCreateChild(args.nextArg());
            }
            return node.getOrCreateChild(command.aliases()[0]);
        }
    }
}
