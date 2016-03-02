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

import me.dags.commandbus.annotation.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author dags <dags@dags.me>
 */

public class SpongeCommand implements CommandExecutor
{
    private final Object owner;
    private final Method target;
    private final Command command;
    private final CommandParameter[] parameters;
    protected final Set<SpongeCommand> children = new LinkedHashSet<>();

    public SpongeCommand(Object owner, Method target)
    {
        Parameter[] parameters = target.getParameters();
        this.owner = owner;
        this.target = target;
        this.command = target.getAnnotation(Command.class);
        this.parameters = new CommandParameter[parameters.length];
        for (int i = 0; i < parameters.length; i++)
        {
            this.parameters[i] = new CommandParameter(owner, target, parameters[i]);
        }
    }

    protected SpongeCommand()
    {
        owner = null;
        target = null;
        command = null;
        parameters = null;
    }

    public SpongeCommand child(SpongeCommand child)
    {
        children.add(child);
        return this;
    }

    public boolean isMain()
    {
        return command.parent().isEmpty();
    }

    public CommandPath path()
    {
        return new CommandPath(command.parent());
    }

    public String pathString()
    {
        return command.parent() + " " + main();
    }

    public String main()
    {
        return command.aliases()[0];
    }

    public String[] aliases()
    {
        return command.aliases();
    }

    public CommandSpec spec()
    {
        CommandSpec.Builder builder = CommandSpec.builder();

        Text.Builder extendedInfo = Text.builder();
        appendExtendedInfo(extendedInfo);
        Text description = Text.of(command.desc().isEmpty() ? this.toString() : command.desc());

        children.forEach(c -> builder.child(c.spec(), c.aliases()));

        builder.extendedDescription(extendedInfo.build());
        builder.description(description);
        builder.arguments(sequence());
        builder.executor(this);

        return builder.build();
    }

    private CommandElement sequence()
    {
        List<CommandElement> elements = Stream.of(parameters)
                .filter(p -> !p.callerParameter())
                .map(CommandParameter::element)
                .collect(Collectors.toList());

        return GenericArguments.seq(elements.toArray(new CommandElement[elements.size()]));
    }

    protected void appendExtendedInfo(Text.Builder builder)
    {
        builder.append(Text.of(this));
        children.forEach(c -> {
            builder.append(Text.NEW_LINE);
            c.appendExtendedInfo(builder);
        });
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Object[] params = new Object[parameters.length];
        int i = 0;
        for (CommandParameter p : parameters)
        {
            if (p.callerParameter())
            {
                if (!p.type().isInstance(source))
                {
                    source.sendMessage(Text.of("Must be a " + p.type().getSimpleName() + " to execute this command!"));
                    return CommandResult.empty();
                }
                params[i++] = source;
            }
            else
            {
                Optional<?> optional = context.getOne(p.key());
                if (!optional.isPresent())
                {
                    return CommandResult.empty();
                }
                params[i++] = optional.get();
            }
        }
        try
        {
            target.invoke(owner, params);
            return CommandResult.success();
        }
        catch (InvocationTargetException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return CommandResult.empty();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("/");
        if (!isMain()) sb.append(command.parent()).append(" ");
        sb.append(main());
        for (CommandParameter parameter : parameters)
        {
            if (!parameter.callerParameter())
            {
                sb.append(" ").append(parameter);
            }
        }
        if (!command.perm().isEmpty()) sb.append(" - [").append(command.perm()).append("]");
        return sb.toString();
    }
}
