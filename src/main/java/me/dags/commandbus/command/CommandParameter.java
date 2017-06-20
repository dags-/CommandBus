package me.dags.commandbus.command;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import me.dags.commandbus.annotation.*;
import me.dags.commandbus.utils.FlagElement;
import me.dags.commandbus.utils.JoinedStringElement;
import me.dags.commandbus.utils.VarargElement;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
class CommandParameter {

    private final Text id;
    private final String name;
    private final String usage;
    private final Class<?> type;
    private final boolean join;
    private final boolean varargs;
    private final boolean caller;
    private final boolean collect;
    private final boolean flags;
    private final int priority;
    private final CommandElement element;

    private CommandParameter(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.usage = builder.usage;
        this.type = builder.type;
        this.join = builder.join;
        this.varargs = builder.varargs;
        this.caller = builder.caller;
        this.collect = builder.collect;
        this.element = builder.element;
        this.flags = builder.flag;
        this.priority = calcPriority();
    }

    void parse(CommandSource source, CommandArgs args, CommandContext context) throws CommandException {
        if (caller) {
            if (!type.isInstance(source)) {
                String error = String.format("You must be a %s to use this command", type.getSimpleName());
                throw new CommandException(Text.of(error));
            }
        }

        element.parse(source, args, context);
    }

    Object read(CommandSource source, CommandContext context) throws CommandException {
        if (caller) {
            return source;
        }

        if (collect) {
            return context.getAll(id);
        }

        if (varargs) {
            return Iterables.toArray(context.getAll(id), type);
        }

        Optional<Object> val = context.getOne(id);
        if (val.isPresent()) {
            return cast(val.get());
        }

        if (context.hasAny(id)) {
            throw new CommandException(Text.of("Parsed multiple possible values for parameter ", id, " but was expecting only one"));
        }

        return null;
    }

    private Object cast(Object value) {
        // support lower bit number types than just Integers & Doubles
        if (Number.class.isInstance(value)) {
            return castNumber(value, type);
        }
        return value;
    }

    int priority() {
        return priority;
    }

    boolean caller() {
        return caller;
    }

    boolean flags() {
        return flags;
    }

    public boolean string() {
        return type == String.class;
    }

    CommandElement element() {
        return element;
    }

    String usage() {
        return usage;
    }

    @Override
    public String toString() {
        return "<" + name + ">";
    }

    private int calcPriority() {
        if (join) {
            return 4;
        }

        if (varargs) {
            return type == String.class ? 3 : 2;
        }

        if (type == String.class) {
            return 1;
        }

        return 0;
    }

    private static Object castNumber(Object in, Class<?> type) {
        Number number = Number.class.cast(in);
        if (type == float.class || type == Float.class) {
            return number.floatValue();
        }
        if (type == short.class || type == Short.class) {
            return number.shortValue();
        }
        if (type == byte.class || type == Byte.class) {
            return number.byteValue();
        }
        return in;
    }

    static CommandParameter of(Parameter parameter, TypeIds typeIds, Flags flags) {
        Builder builder = new Builder(parameter, typeIds, flags);

        Src src = parameter.getAnnotation(Src.class);
        if (src != null || parameter.getType() == CommandSource.class) {
            return builder.caller().build();
        }

        Arg arg = parameter.getAnnotation(Arg.class);

        if (parameter.getType() == Collection.class) {
            return builder.all(arg).build();
        }

        if (parameter.getType().isArray() || parameter.isVarArgs()) {
            return builder.var(arg).build();
        }

        Join join = parameter.getAnnotation(Join.class);
        if (join != null) {
            return builder.join(join).build();
        }

        return builder.one(arg).build();
    }

    private static class Builder {

        private final Parameter parameter;
        private final TypeIds typeIds;
        private final Flags flags;

        private String name = "";
        private String usage = "";
        private String separator = "";
        private Text id = Text.EMPTY;
        private Class<?> type = null;
        private CommandElement element = GenericArguments.none();
        private boolean join = false;
        private boolean caller = false;
        private boolean collect = false;
        private boolean varargs = false;
        private boolean flag = false;

        private Builder(Parameter parameter, TypeIds typeIds, Flags flags) {
            this.parameter = parameter;
            this.typeIds = typeIds;
            this.type = parameter.getType();
            this.flags = flags;
        }

        private Builder caller() {
            if (!CommandSource.class.isAssignableFrom(type)) {
                throw new IllegalStateException("@Src must decorate a CommandSource parameter");
            }

            caller = true;
            type = parameter.getType();
            return this;
        }

        private Builder join(Join join) {
            if (type != String.class) {
                throw new IllegalStateException("@Join must decorate a String parameter");
            }

            type = String.class;
            this.join = true;
            this.separator = join.separator();
            name = (!join.value().isEmpty() ? join.value() : type.getSimpleName().toLowerCase()) + "..";
            return this;
        }

        private Builder all(Arg arg) {
            if (Collection.class != type) {
                throw new IllegalStateException("@All must decorate a Collection parameter");
            }

            ParameterizedType paramT = (ParameterizedType) parameter.getParameterizedType();
            type = (Class<?>) paramT.getActualTypeArguments()[0];
            collect = true;
            name = AnnotationHelper.getArgName(arg, type.getSimpleName().toLowerCase());
            return this;
        }

        private Builder var(Arg arg) {
            if (!type.isArray()) {
                throw new IllegalStateException("@Var must decorate an Array or Varargs parameter");
            }

            varargs = true;
            type = type.getComponentType();
            name = AnnotationHelper.getArgName(arg, type.getSimpleName().toLowerCase() + "...");
            return this;
        }

        private Builder one(Arg arg) {
            name = AnnotationHelper.getArgName(arg, type.getSimpleName().toLowerCase());
            return this;
        }

        private Map<String, CommandElement> flags() {
            if (flags == null) {
                return Collections.emptyMap();
            }

            ImmutableMap.Builder<String, CommandElement> builder = ImmutableMap.builder();
            for (Flag flag : flags.value()) {
                if (flag.type() == boolean.class || flag.type() == Boolean.class) {
                    String name = "-" + flag.value();
                    CommandElement element = GenericArguments.none();
                    builder.put(name, element);
                } else {
                    String name = "--" + flag.value();
                    CommandElement element = ParameterTypes.of(flag.type(), flag.value());
                    builder.put(name, element);
                }
            }
            return builder.build();
        }

        private String flagUsage() {
            if (flags == null) {
                return "";
            }

            StringBuilder builder = new StringBuilder();
            for (Flag flag : flags.value()) {
                if (flag.type() == boolean.class || flag.type() == Boolean.class) {
                    String name = "-" + flag.value();
                    builder.append(builder.length() > 0 ? " | " : "").append(name);
                } else {
                    String name = "--" + flag.value();
                    String type = "<" + flag.type().getSimpleName().toLowerCase() + ">";
                    builder.append(builder.length() > 0 ? " | " : "").append(name).append(" ").append(type);
                }
            }

            return builder.toString();
        }

        private CommandParameter build() {
            String id = typeIds.nextId(type);
            CommandElement element = GenericArguments.none();

            if (!caller) {
                ParameterTypes.typeCheck(type, parameter);
                usage = "<" + name + ">";

                if (join) {
                    element = new JoinedStringElement(Text.of(id), separator, AnnotationHelper.getFlags(flags));
                } else if (varargs) {
                    element = new VarargElement(Text.of(id), ParameterTypes.of(type, id), AnnotationHelper.getFlags(flags));
                } else if (type == CommandFlags.class){
                    flag = true;
                    usage = flagUsage();
                    element = new FlagElement(Text.of(id), flags());
                } else {
                    element = ParameterTypes.of(type, id);
                }
            }

            this.id = Text.of(id);
            this.element = element;

            return new CommandParameter(this);
        }
    }
}
