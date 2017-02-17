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

import me.dags.commandbus.format.Format;
import me.dags.commandbus.format.Formatter;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class SpongeCommand implements CommandCallable {

    private static final String SEE_HELP = "Command not recognised. Hover this text for suggestions or see '/help %s'";

    private final CommandNode root;
    private final Format format;

    public SpongeCommand(CommandNode root, Format format) {
        this.root = root;
        this.format = format;
    }

    public List<String> aliases() {
        return root.aliases();
    }

    @Override
    public CommandResult process(CommandSource source, String rawArgs) throws CommandException {
        List<CommandMethod.Instance> commands = new ArrayList<>();
        root.parse(source, new CommandPath(rawArgs), commands);
        Collections.sort(commands);

        if (commands.isEmpty()) {
            String alias = aliases().get(0);
            Formatter hover  = format.message();
            getSuggestions(source, rawArgs, null).forEach(hover.newLine()::info);

            Formatter error = format.message().warn(SEE_HELP, alias);
            error.action(hover.toHoverAction()).action(hover.toHoverAction());

            throw new CommandException(error.build());
        }

        InvokeResult result = InvokeResult.EMPTY;
        for (CommandMethod.Instance instance : commands) {
            InvokeResult test = instance.invoke(source);
            if (test == InvokeResult.SUCCESS) {
                return CommandResult.success();
            }
            result = result.or(test);
        }

        if (result == InvokeResult.UNKNOWN) {
            throw new CommandException(format.warn(result.message()).build());
        }

        if (result == InvokeResult.NO_PERM) {
            throw new CommandException(format.error(result.message()).build());
        }

        return CommandResult.empty();
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> location) throws CommandException {
        List<String> suggestions = new ArrayList<>();

        CommandPath input = new CommandPath(arguments);
        CommandNode node = root, previous = node;
        String lastArg = "";

        while (input.currentState().hasNext()) {
            node = node.getChild(lastArg = input.currentState().peek());
            if (node == null) {
                break;
            }
            previous = node;
            input.currentState().next();
        }

        if (node == null) {
            node = previous;
            suggestions.addAll(node.suggestions(lastArg));
        }

        if (suggestions.isEmpty()) {
            input = input.trim();
            node.completions(source, input).forEach(suggestions::add);
        }

        return suggestions;
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return root.testPermission(source);
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.empty();
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        List<Text> usage = root.usage(source);
        Formatter formatter = format.message();
        usage.forEach(formatter.newLine()::append);
        return Optional.of(formatter.build());
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.EMPTY;
    }
}
