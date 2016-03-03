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
import me.dags.commandbus.exception.ParameterAnnotationException;

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

    protected SpongeCommand parent = null;

    public SpongeCommand(Object owner, Method target)
    {
        Parameter[] parameters = target.getParameters();
        this.owner = owner;
        this.target = target;
        this.command = target.getAnnotation(Command.class);
        this.parameters = new CommandParameter[parameters.length];
        for (int i = 0; i < parameters.length; i++)
        {
            this.parameters[i] = CommandParameter.from(owner, target, parameters[i]);
            if (this.parameters[i].join() && i + 1 < parameters.length)
            {
                String warn = "The @Retaining annotation should only by used on the last Paramter in a Method: %s in %s";
                throw new ParameterAnnotationException(warn, target.getName(), owner);
            }
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
        child.parent = this;
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

    protected Optional<SpongeCommand> findMatch(String arg, CommandSource source, CommandContext context)
    {
        if (parent != null)
        {
            return parent.children.stream().filter(c -> c.matchFor(arg, source, context)).findFirst();
        }
        return Optional.empty();
    }

    protected boolean matchFor(String arg, CommandSource source, CommandContext context)
    {
        if (!this.main().equals(arg))
        {
            return false;
        }
        for (CommandParameter p : parameters)
        {
            if (p.caller() && p.type().isInstance(source))
            {
                continue;
            }
            if (context.hasAny(p.key()))
            {
                continue;
            }
            return false;
        }
        return true;
    }

    private CommandElement sequence()
    {
        List<CommandElement> elements = Stream.of(parameters)
                .filter(p -> !p.caller())
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
        if (!this.matchFor(main(), source, context))
        {
            Optional<SpongeCommand> child = findMatch(main(), source, context);
            if (child.isPresent())
            {
                return child.get().execute(source, context);
            }
            return CommandResult.empty();
        }

        int i = 0;
        Object[] params = new Object[parameters.length];
        for (CommandParameter p : parameters)
        {
            if (p.caller())
            {
                params[i++] = source;
            }
            else
            {
                if (p.collect())
                {
                    params[i++] = context.getAll(p.key());
                }
                else
                {
                    params[i++] = context.getOne(p.key()).get();
                }
            }
        }

        try
        {
            target.invoke(owner, params);
            return CommandResult.success();
        }
        catch (InvocationTargetException | IllegalAccessException e)
        {
            return CommandResult.empty();
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("/");
        if (!isMain()) sb.append(command.parent()).append(" ");
        sb.append(main());
        for (CommandParameter parameter : parameters)
        {
            if (!parameter.caller())
            {
                sb.append(" ").append(parameter);
            }
        }
        if (!command.perm().isEmpty()) sb.append(" - [").append(command.perm()).append("]");
        return sb.toString();
    }
}
