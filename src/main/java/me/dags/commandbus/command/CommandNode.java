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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

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
        CommandNode node = getChild(alias);
        if (node == null) {
            node = new CommandNode(alias);
            children.add(node);
        }
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

    void parse(CommandSource source, CommandPath input, List<CommandMethod.Instance> results) {
        for (CommandMethod method : this.methods) {
            if (input.remaining() == method.parameterCount() || (method.join() && input.remaining() >= method.parameterCount())) {
                try {
                    CommandArgs commandArgs = input.copyState();
                    CommandContext context = new CommandContext();
                    method.elements().parse(source, commandArgs, context);
                    if (method.fitsContext(context)) {
                        results.add(new CommandMethod.Instance(method, context));
                    }
                } catch (ArgumentParseException ignored) {
                }
            }
        }
        if (input.currentState().hasNext()) {
            try {
                CommandNode child = getChild(input.currentState().next());
                if (child != null) {
                    child.parse(source, input, results);
                }
            } catch (ArgumentParseException ignored) {
            }
        }
    }

    Collection<String> completions(CommandSource source, CommandPath input) {
        Set<String> completions = new LinkedHashSet<>();
        for (CommandMethod method : this.methods) {
            if (method.parameterCount() > input.argIndex()) {
                CommandParameter parameter = method.parameter(input.argIndex());
                completions.addAll(parameter.element().complete(source, input.copyState(), new CommandContext()));
            }
        }
        return completions;
    }

    List<String> aliases() {
        return new ArrayList<>(aliases);
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

    List<Text> usage(CommandSource source) {
        Set<Text> set = new HashSet<>();
        usage(source, "/" + main, set);
        return set.stream().sorted().collect(Collectors.toList());
    }

    boolean testPermission(CommandSource source) {
        for (CommandMethod method : methods) {
            if (method.permission().value().isEmpty() || source.hasPermission(method.permission().value())) {
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

    private boolean matches(String alias) {
        return aliases.contains(alias);
    }

    private void usage(CommandSource source, String parent, Set<Text> set) {
        for (CommandMethod method : methods) {
            if (method.permission().value().isEmpty() || source.hasPermission(method.permission().value())) {
                String line = parent + " " + method.usage();
                Text description = Text.of(method.description());
                Text usage = Text.builder(line).onHover(TextActions.showText(description)).build();
                set.add(usage);
            }
        }
        for (CommandNode child : children) {
            child.usage(source, parent + " " + child.main, set);
        }
    }

    public void addCommandMethod(CommandMethod method) {
        methods.add(method);
    }
}
