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

package me.dags.commandbus.command;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class CommandNode {

    private final String main;
    private final Set<String> aliases = new LinkedHashSet<>();
    private final Set<CommandNode> children = new HashSet<>();
    private final Set<CommandMethod> methods = new HashSet<>();

    public CommandNode(String... aliases) {
        main = aliases[0];
        addAliases(aliases);
    }

    public CommandNode addAliases(String... aliases) {
        Collections.addAll(this.aliases, aliases);
        return this;
    }

    public CommandNode getOrCreateChild(String alias) {
        for (CommandNode node : children) {
            if (node.matches(alias)) {
                return node;
            }
        }
        CommandNode node = new CommandNode(alias);
        children.add(node);
        return node;
    }

    CommandNode getChild(String node) {
        for (CommandNode child : children) {
            if (child.matches(node)) {
                return child;
            }
        }
        return null;
    }

    void populate(CommandSource source, CommandPath path, List<CommandMethod.Instance> list) {
        for (CommandMethod method : this.methods) {
            if (path.remaining() == method.parameterCount() || (method.join() && path.remaining() > method.parameterCount())) {
                CommandArgs args = path.remainingArgs();
                CommandContext commandContext = new CommandContext();
                try {
                    method.parameters().parse(source, args, commandContext);
                } catch (ArgumentParseException e) {
                    continue;
                }
                if (method.fitsContext(commandContext)) {
                    list.add(new CommandMethod.Instance(method, args, commandContext));
                }
            }
        }
    }

    List<String> aliases() {
        return new ArrayList<>(aliases);
    }

    List<String> suggestions() {
        List<String> list = new ArrayList<>();
        for (CommandNode child : this.children) {
            list.addAll(child.aliases);
        }
        return list;
    }

    List<String> suggestions(String match) {
        List<String> list = new ArrayList<>();
        for (CommandNode child : this.children) {
            for (String s : child.aliases) {
                if (s.startsWith(match)) {
                    list.add(s);
                }
            }
        }
        return list;
    }

    boolean testPermission(CommandSource source) {
        for (CommandMethod method : methods) {
            if (method.command().perm().isEmpty() || source.hasPermission(method.command().perm())) {
                return true;
            }
        }
        for (CommandNode child : children) {
            if (child.testPermission(source)) {
                return true;
            }
        }
        return false;
    }

    Collection<String> usage(CommandSource source) {
        Set<String> set = new LinkedHashSet<>();
        usage(source, "/" + main, set);
        return set.stream().sorted().collect(Collectors.toList());
    }

    private void usage(CommandSource source, String parent, Set<String> set) {
        for (CommandMethod method : methods) {
            if (method.command().perm().isEmpty() || source.hasPermission(method.command().perm())) {
                set.add(parent + " " + method.usage());
            }
        }
        for (CommandNode child : children) {
            child.usage(source, parent + " " + child.main, set);
        }
    }

    private boolean matches(String alias) {
        return aliases.contains(alias);
    }

    public void addCommandMethod(CommandMethod method) {
        methods.add(method);
    }
}
