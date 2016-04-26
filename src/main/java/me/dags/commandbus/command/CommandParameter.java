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

import com.flowpowered.math.vector.Vector3d;
import me.dags.commandbus.annotation.All;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Join;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.exception.ParameterAnnotationException;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author dags <dags@dags.me>
 */

/**
 * Used internally by CommandBus to hold information about a Method Parameter.
 */
public class CommandParameter
{
    private static final Map<Class<?>, Function<String, CommandElement>> types = init();

    private static Map<Class<?>, Function<String, CommandElement>> init()
    {
        Map<Class<?>, Function<String, CommandElement>> map = new HashMap<>();
        map.put(boolean.class, s -> GenericArguments.bool(Text.of(s)));
        map.put(Boolean.class, map.get(boolean.class));
        map.put(double.class, s -> GenericArguments.doubleNum(Text.of(s)));
        map.put(Double.class, map.get(double.class));
        map.put(int.class, s -> GenericArguments.integer(Text.of(s)));
        map.put(Integer.class, map.get(int.class));
        map.put(Location.class, s -> GenericArguments.location(Text.of(s)));
        map.put(Player.class, s -> GenericArguments.player(Text.of(s)));
        map.put(String.class, s -> GenericArguments.string(Text.of(s)));
        map.put(User.class, s -> GenericArguments.user(Text.of(s)));
        map.put(Vector3d.class, s -> GenericArguments.vector3d(Text.of(s)));
        map.put(World.class, s -> GenericArguments.world(Text.of(s)));
        return Collections.unmodifiableMap(map);
    }

    private final String key;
    private final Class<?> type;
    private final boolean idKey;
    private final boolean caller;
    private final boolean all;
    private final boolean join;

    private CommandParameter(String key, int id, Class<?> type, boolean caller, boolean collect, boolean remaining)
    {
        this.idKey = key.isEmpty();
        this.key = idKey ? "" + id : key;
        this.type = type;
        this.caller = caller;
        this.all = collect;
        this.join = remaining;
    }

    public String key()
    {
        return key;
    }

    public Class<?> type()
    {
        return type;
    }

    public boolean caller()
    {
        return caller;
    }

    public boolean collect()
    {
        return all;
    }

    public boolean join()
    {
        return join;
    }

    public CommandElement element()
    {
        if (caller())
        {
            return GenericArguments.none();
        }
        if (join())
        {
            return GenericArguments.remainingJoinedStrings(Text.of(key));
        }
        return CommandParameter.of(type, key);
    }

    @Override
    public String toString()
    {
        return "<" + (idKey ? type().getSimpleName().toLowerCase() : key) + ">";
    }

    protected static CommandParameter from(Object owner, Method method, Parameter parameter, int id)
    {
        if (parameter.isAnnotationPresent(Caller.class))
        {
            return new CommandParameter("@", id, parameter.getType(), true, false, false);
        }
        else if (parameter.isAnnotationPresent(All.class) || Collection.class.equals(parameter.getType()))
        {
            if (!Collection.class.equals(parameter.getType()))
            {
                String warn = "Parameter %s in Method %s in Class %s is annotated with @Collect but is not of type %s";
                throw new ParameterAnnotationException(warn, parameter, method.getName(), owner.getClass(), Collection.class);
            }
            ParameterizedType paramT = (ParameterizedType) parameter.getParameterizedType();
            Class<?> type = (Class<?>) paramT.getActualTypeArguments()[0];
            CommandParameter.typeCheck(type, parameter, method, owner);
            All collect = parameter.getAnnotation(All.class);
            String name = collect != null ? collect.value() : "";
            return new CommandParameter(name, id, type, false, true, false);
        }
        else if (parameter.isAnnotationPresent(Join.class))
        {
            if (!String.class.equals(parameter.getType()))
            {
                String warn = "Parameter %s in Method %s in Class %s is annotated with @Join but is not of type %s";
                throw new ParameterAnnotationException(warn, parameter, method.getName(), owner.getClass(), String.class);
            }
            CommandParameter.typeCheck(parameter.getType(), parameter, method, owner);
            Join remaining = parameter.getAnnotation(Join.class);
            return new CommandParameter(remaining.value(), id, parameter.getType(), false, false, true);
        }
        else
        {
            CommandParameter.typeCheck(parameter.getType(), parameter, method, owner);
            One one = parameter.getAnnotation(One.class);
            String name = one != null ? one.value() : "";
            return new CommandParameter(name, id, parameter.getType(), false, false, false);
        }
    }

    private static void typeCheck(Class<?> type, Parameter source, Method method, Object owner)
    {
        if (!CommandParameter.validParameterType(type))
        {
            String warn = "Parameter %s in Method %s in Class %s is not supported!";
            throw new ParameterAnnotationException(warn, source, method.getName(), owner.getClass());
        }
    }

    public static boolean validParameterType(Class<?> type)
    {
        return types.containsKey(type) || CatalogType.class.isAssignableFrom(type);
    }

    @SuppressWarnings("unchecked")
    public static CommandElement of(Class<?> type, String key)
    {
        Function<String, CommandElement> f = types.get(type);
        if (f != null)
        {
            return GenericArguments.optional(f.apply(key));
        }
        if (CatalogType.class.isAssignableFrom(type))
        {
            CommandElement e = GenericArguments.catalogedElement(Text.of(key), (Class<? extends CatalogType>) type);
            return GenericArguments.optional(e);
        }
        return GenericArguments.none();
    }
}
