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

import me.dags.commandbus.annotation.All;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Join;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.exception.ParameterAnnotationException;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

/**
 * @author dags <dags@dags.me>
 */

/**
 * Used internally by CommandBus to hold information about a Method Parameter.
 */
class CommandParameter {

    private final Text id;
    private final String name;
    private final Class<?> type;
    private final boolean join;
    private final boolean caller;
    private final boolean collect;
    private final CommandElement element;

    CommandParameter(ParameterTypes types, Parameter parameter, String id) {
        if (parameter.isAnnotationPresent(Caller.class)) {
            this.id = Text.of(id);
            this.name = "@caller";
            this.type = parameter.getType();
            this.join = false;
            this.caller = true;
            this.collect = false;
            this.element = GenericArguments.none();
        } else if (parameter.isAnnotationPresent(All.class) || Collection.class.equals(parameter.getType())) {
            if (!Collection.class.equals(parameter.getType())) {
                String warn = "Parameter %s is annotated with @Collect but is not of type %s";
                throw new ParameterAnnotationException(warn, parameter.getName(), Collection.class);
            }
            All all = parameter.getAnnotation(All.class);
            ParameterizedType paramT = (ParameterizedType) parameter.getParameterizedType();
            Class<?> type = (Class<?>) paramT.getActualTypeArguments()[0];
            this.id = Text.of(id);
            this.type = type;
            this.join = false;
            this.caller = false;
            this.collect = true;
            this.element = types.of(type, id);
            this.name = all != null && !all.value().isEmpty() ? all.value() : type.getSimpleName().toLowerCase();
            types.typeCheck(type, parameter);
        } else if (parameter.isAnnotationPresent(Join.class)) {
            if (!String.class.equals(parameter.getType())) {
                String warn = "Parameter %s is annotated with @Join but is not of type %s";
                throw new ParameterAnnotationException(warn, parameter.getName(), String.class);
            }
            Join join = parameter.getAnnotation(Join.class);
            Class<?> type = String.class;
            this.id = Text.of(id);
            this.type = type;
            this.join = true;
            this.caller = false;
            this.collect = false;
            this.name = (join != null && !join.value().isEmpty() ? join.value() : type.getSimpleName().toLowerCase()) + "...";
            this.element = GenericArguments.remainingJoinedStrings(Text.of(id));
            types.typeCheck(type, parameter);
        } else {
            One one = parameter.getAnnotation(One.class);
            Class<?> type = parameter.getType();
            this.id = Text.of(id);
            this.type = type;
            this.join = false;
            this.caller = false;
            this.collect = false;
            this.element = types.of(type, id);
            this.name = one != null && !one.value().isEmpty() ? one.value() : type.getSimpleName().toLowerCase();
            types.typeCheck(type, parameter);
        }
    }

    Object cast(Object value) {
        // GenericArgs only support doubles, so need to manually convert to float as required
        if (Float.class.isAssignableFrom(type()) && Number.class.isInstance(value)) {
            return Number.class.cast(value).floatValue();
        }
        return value;
    }

    Text getId() {
        return id;
    }

    Class<?> type() {
        return type;
    }

    boolean collect() {
        return collect;
    }

    boolean caller() {
        return caller;
    }

    boolean join() {
        return join;
    }

    CommandElement element() {
        return this.element;
    }

    @Override
    public String toString() {
        return "<" + name + ">";
    }
}
