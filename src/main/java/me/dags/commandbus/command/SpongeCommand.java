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

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.parsing.SingleArg;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tristate;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class SpongeCommand implements CommandCallable {

    private static final Text NO_PERM = Text.builder("You do not have permission to do that!").color(TextColors.RED).build();
    private static final Text NOT_FOUND = Text.builder("Command not recognised").color(TextColors.GRAY).build();

    private final CommandNode root;

    public SpongeCommand(CommandNode root) {
        this.root = root;
    }

    public List<String> aliases() {
        return root.aliases();
    }

    private List<CommandMethod.Instance> findMatches(CommandSource source, CommandInput input, CommandNode node) {
        List<CommandMethod.Instance> matchList = new ArrayList<>();
        node.derp(source, input, matchList);
        if (matchList.size() > 1) {
            Collections.sort(matchList);
        }
        return matchList;
    }

    @Override
    public CommandResult process(CommandSource source, String rawArgs) throws CommandException {
        List<CommandMethod.Instance> commands = findMatches(source, new CommandInput(rawArgs), root);
        for (CommandMethod.Instance instance : commands) {
            Tristate result = instance.invoke(source);
            if (result == Tristate.TRUE) {
                return CommandResult.success();
            } else if (result == Tristate.FALSE) {
                source.sendMessage(NO_PERM);
                return CommandResult.empty();
            }
        }
        source.sendMessage(NOT_FOUND);
        return CommandResult.empty();
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        List<String> suggestions = new ArrayList<>();

        CommandInput input = new CommandInput(arguments);
        CommandNode node = root, previous = node;
        String lastArg = "";

        while (input.currentState().hasNext()) {
            node = node.getChild(lastArg = input.currentState().peek());
            if (node == null) {
                break;
            }
            previous = node;
            input.currentState().next();
            input.next();
        }

        if (node == null) {
            node = previous;
            suggestions.addAll(node.suggestions(lastArg));
        }

        if (suggestions.isEmpty()) {
            findMatches(source, input, node).stream().flatMap(c -> c.getSuggestions(source).stream()).forEach(suggestions::add);
        }

        return suggestions;
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return root.testPermission(source);
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource source) {
        return Optional.empty();
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource source) {
        Text.Builder builder = Text.builder();
        Iterator<String> usage = root.usage(source).iterator();
        while (usage.hasNext()) {
            builder.append(Text.of(usage.next()));
            if (usage.hasNext()) {
                builder.append(Text.NEW_LINE);
            }
        }
        return Optional.of(builder.build());
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.EMPTY;
    }
}
