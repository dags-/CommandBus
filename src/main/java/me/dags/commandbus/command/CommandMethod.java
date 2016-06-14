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
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.util.Tristate;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class CommandMethod {

    private final Command command;
    private final Object owner;
    private final Method target;
    private final CommandElement element;
    private final CommandParameter[] parameters;

    private final boolean join;
    private final int argCount;

    public CommandMethod(Object owner, Method target) {
        this.command = target.getDeclaredAnnotation(Command.class);
        this.owner = owner;
        this.target = target;
        this.parameters = getParameters(target);
        CommandElement[] elements = toElements(parameters);
        this.element = elements.length == 0 ? GenericArguments.none() : GenericArguments.seq(elements);
        this.argCount = elements.length;
        boolean join = false;
        for (CommandParameter parameter : parameters) {
            if (parameter.join()) {
                join = true;
            }
        }
        this.join = join;
    }

    public Command command() {
        return command;
    }

    boolean join() {
        return join;
    }

    int parameterCount() {
        return argCount;
    }

    CommandElement parameters() {
        return element;
    }

    String usage() {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (CommandParameter parameter : parameters) {
            if (!parameter.caller()) {
                builder.append(first ? "" : " ").append(parameter);
                first = false;
            }
        }
        return builder.toString();
    }

    boolean fitsContext(CommandContext context) {
        for (CommandParameter parameter : parameters) {
            if (parameter.caller()) {
                continue;
            }
            Optional<?> optional = context.getOne(parameter.getId());
            if (!optional.isPresent()) {
                return false;
            }
        }
        return true;
    }

    void invoke(CommandSource source, CommandContext context) throws Exception {
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            CommandParameter parameter = parameters[i];
            if (parameter.caller()) {
                args[i] = source;
                continue;
            }
            if (parameter.collect()) {
                args[i] = context.getAll(parameter.getId());
                continue;
            }
            Optional<?> arg = context.getOne(parameter.getId());
            if (arg.isPresent()) {
                args[i] = arg.get();
            } else {
                return;
            }
        }
        target.invoke(owner, args);
    }

    private static CommandParameter[] getParameters(Method method) {
        Parameter[] parameters = method.getParameters();
        CommandParameter[] commandParameters = new CommandParameter[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            commandParameters[i] = new CommandParameter(parameters[i], "#" + i);
        }
        return commandParameters;
    }

    private static CommandElement[] toElements(CommandParameter[] parameters) {
        int callers = 0;
        for (CommandParameter parameter : parameters) {
            if (parameter.caller()) {
                callers++;
            }
        }
        CommandElement[] elements = new CommandElement[parameters.length - callers];
        for (int i = 0, j = 0; i < elements.length && j < parameters.length; j++) {
            CommandParameter parameter = parameters[j];
            if (parameter.caller()) {
                continue;
            }
            elements[i++] = parameter.element();
        }
        return elements;
    }

    static class Instance implements Comparable<Instance> {

        private final CommandMethod method;
        private final CommandArgs commandArgs;
        private final CommandContext commandContext;

        Instance(CommandMethod method, CommandArgs commandArgs, CommandContext commandContext) {
            this.method = method;
            this.commandArgs = commandArgs;
            this.commandContext = commandContext;
        }

        List<String> getSuggestions(CommandSource source) {
            commandArgs.setState(-1);
            return method.parameters().complete(source, commandArgs, commandContext);
        }

        Tristate invoke(CommandSource source) {
            if (!method.command().perm().isEmpty() && !source.hasPermission(method.command().perm())) {
                return Tristate.FALSE;
            }
            try {
                method.invoke(source, commandContext);
                return Tristate.TRUE;
            } catch (Exception e) {
                e.printStackTrace();
                return Tristate.UNDEFINED;
            }
        }

        @Override
        public int compareTo(Instance in) {
            return method.join ? 2 : method.parameterCount() > in.method.parameterCount() ? 1 : -1;
        }

        @Override
        public String toString() {
            String string = method.command().aliases()[0] + " " + method.usage();
            return method.command().parent().isEmpty() ? string : method.command().parent() + " " + string;
        }
    }
}
