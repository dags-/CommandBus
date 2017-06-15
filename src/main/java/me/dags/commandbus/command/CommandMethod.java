package me.dags.commandbus.command;

import com.google.common.collect.ImmutableList;
import me.dags.commandbus.annotation.*;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class CommandMethod {

    private final Command command;
    private final Permission permission;
    private final Assignment assignment;
    private final Description description;
    private final Object owner;
    private final Method target;
    private final List<CommandParameter> parameters;

    private final int priority;
    private final int argCount;
    private final boolean variable;

    public CommandMethod(Object owner, Method target) {
        Flags flags = target.getAnnotation(Flags.class);
        Command command = target.getAnnotation(Command.class);
        Permission permission = target.getAnnotation(Permission.class);
        Assignment assignment = target.getAnnotation(Assignment.class);
        Description description = target.getAnnotation(Description.class);

        TypeIds typeIds = new TypeIds();
        ImmutableList.Builder<CommandParameter> builder = ImmutableList.builder();
        for (Parameter parameter : target.getParameters()) {
            builder.add(CommandParameter.of(parameter, typeIds, flags));
        }
        List<CommandParameter> parameters = builder.build();

        int argCount = 0;
        int priority = 0;
        boolean variable = false;
        for (CommandParameter parameter : parameters) {
            priority = Math.max(priority, parameter.priority());
            if (parameter.caller()) {
                continue;
            }
            if (parameter.flags()) {
                variable = true;
                continue;
            }
            argCount++;
        }

        this.owner = owner;
        this.target = target;
        this.command = command;
        this.priority = priority;
        this.argCount = argCount;
        this.variable = variable || priority > 1;
        this.parameters = parameters;
        this.permission = permission != null ? permission : command.permission();
        this.assignment = assignment != null ? assignment : (!this.permission.assign().role().isEmpty() ? this.permission.assign() : command.assign());
        this.description = description != null ? description : command.description();
    }

    public Command command() {
        return command;
    }

    public Description description() {
        return description;
    }

    public Permission permission() {
        return permission;
    }

    public Assignment assignment() {
        return assignment;
    }

    String usage() {
        StringBuilder builder = new StringBuilder();
        for (CommandParameter parameter : parameters) {
            if (parameter.caller()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(" ");
            }
            if (parameter.flags()) {
                builder.append("(");
            }

            builder.append(parameter.usage());

            if (parameter.flags()) {
                builder.append(")");
            }
        }
        return builder.toString();
    }

    Instance parse(CommandSource source, CommandArgs args, int remaining) throws CommandException {
        if (remaining < argCount) {
            throw args.createError(Text.of("Not enough arguments"));
        }

        if (remaining > argCount && !variable) {
            throw args.createError(Text.of("Too many arguments"));
        }

        CommandContext context = new CommandContext();
        for (CommandParameter parameter : parameters) {
            parameter.parse(source, args, context);
        }

        return new Instance(context);
    }

    void suggest(CommandSource source, CommandArgs args, int remaining, Collection<String> suggestions) throws ArgumentParseException {
        if (remaining < argCount) {
            return;
        }

        CommandContext dummy = new CommandContext();
        for (CommandParameter parameter : parameters) {
            CommandElement element = parameter.element();
            Object startState = args.getState();
            try {
                element.parse(source, args, dummy);
                Object endState = args.getState();
                if (!args.hasNext()) {
                    args.setState(startState);
                    List<String> list = element.complete(source, args, dummy);
                    args.setState(Math.max(-1, (int) args.getState() - 1));
                    if (!list.contains(args.next())) {
                        suggestions.addAll(list);
                        return;
                    }
                    args.setState(endState);
                }
            } catch (ArgumentParseException e) {
                args.setState(startState);
                List<String> list = element.complete(source, args, dummy);
                suggestions.addAll(list);
            }
        }
    }

    class Instance implements Comparable<Instance> {

        private final CommandContext context;
        private final int length = CommandMethod.this.argCount;
        private final int priority = CommandMethod.this.priority;

        private Instance(CommandContext context) {
            this.context = context;
        }

        void invoke(CommandSource source) throws Exception {
            Object[] args = new Object[parameters.size()];

            for (int i = 0 ; i < parameters.size(); i++) {
                CommandParameter parameter = parameters.get(i);
                Object value = parameter.read(source, context);
                if (value == null) {
                    throw new CommandException(Text.of("Missing parameter " + parameter));
                }
                args[i] = value;
            }

            target.invoke(owner, args);
        }

        @Override
        public int compareTo(Instance instance) {
            if (priority != instance.priority) {
                return priority > instance.priority ? 1 : -1;
            }
            return length > instance.length ? 1 : instance.length > length ? -1 : 0;
        }

        @Override
        public String toString() {
            return command.parent() + " " + command.alias()[0] + ":" + target.getName();
        }
    }
}
