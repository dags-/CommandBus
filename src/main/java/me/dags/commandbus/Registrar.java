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

import me.dags.commandbus.annotation.Assignment;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.command.CommandMethod;
import me.dags.commandbus.command.CommandNode;
import me.dags.commandbus.command.CommandPath;
import me.dags.commandbus.command.SpongeCommand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
class Registrar {

    private final Map<String, CommandNode> roots = new HashMap<>();
    private final Map<Permission, Assignment> permissions = new HashMap<>();
    private final CommandBus commandBus;
    private boolean submitted = false;

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
                        CommandMethod commandMethod = new CommandMethod(commandBus.getParameterTypes(), object, method);
                        CommandNode commandNode = getParentTree(commandMethod.command());
                        commandNode.addAliases(commandMethod.command().alias());
                        commandNode.addCommandMethod(commandMethod);
                        Permission permission = commandMethod.permission();
                        Assignment assignment = commandMethod.assignment();
                        if (!permission.value().isEmpty()) {
                            permissions.put(permission, assignment);
                        }
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
        if (submitted) {
            throw new UnsupportedOperationException("Cannot submit commands more than once");
        }

        final CommandManager commandManager = Sponge.getCommandManager();
        roots.values().stream()
                .map(node -> new SpongeCommand(node, commandBus.getFormat()))
                .forEach(command -> commandManager.register(plugin, command, command.aliases()));

        final PermissionService permissionService = Sponge.getServiceManager().provideUnchecked(PermissionService.class);
        permissions.entrySet().forEach(entry -> permissionService.newDescriptionBuilder(plugin).ifPresent(builder -> {
            Permission permission = entry.getKey();
            Assignment assignment = entry.getValue();
            builder.id(permission.value());
            builder.description(Text.of(permission.description()));
            if (!assignment.role().isEmpty()) {
                builder.assign(assignment.role(), assignment.permit());
            }
        }));

        commandBus.info("Registered {} main commands", roots.size());
        commandBus.info("Registered {} permissions", permissions.size());

        roots.clear();
        permissions.clear();

        submitted = true;
    }

    private CommandNode getRoot(String arg) {
        CommandNode root = roots.get(arg);
        if (root == null) {
            roots.put(arg, root = new CommandNode(arg));
        }
        return root;
    }

    private CommandNode getParentTree(Command command) throws ArgumentParseException {
        if (command.parent().isEmpty()) {
            return getRoot(command.alias()[0]);
        } else {
            CommandPath input = new CommandPath(command.parent());
            CommandNode node = getRoot(input.currentState().next());
            while (input.currentState().hasNext()) {
                node = node.getOrCreateChild(input.currentState().next());
            }
            return node.getOrCreateChild(command.alias()[0]);
        }
    }
}
