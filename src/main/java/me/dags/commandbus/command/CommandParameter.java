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
import me.dags.commandbus.exception.InvalidParameterException;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Key;
import me.dags.commandbus.exception.MissingAnnotationException;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author dags <dags@dags.me>
 */

public class CommandParameter
{
    private static final Map<Class<?>, Function<String, CommandElement>> types = init();

    private final String key;
    private final Class<?> type;
    private final boolean caller;

    public CommandParameter(Object owner, Method method, Parameter parameter)
    {
        if (parameter.isAnnotationPresent(Caller.class))
        {
            this.caller = true;
            this.key = "@";
            this.type = parameter.getType();
        }
        else if (parameter.isAnnotationPresent(Key.class))
        {
            if (!CommandParameter.validParameterType(parameter.getType()))
            {
                throw new InvalidParameterException(parameter.getType());
            }
            Key key = parameter.getAnnotation(Key.class);
            this.caller = false;
            this.key = key.value();
            this.type = parameter.getType();
        }
        else
        {
            throw new MissingAnnotationException(owner.getClass(), method);
        }
    }

    public String key()
    {
        return key;
    }

    public Class<?> type()
    {
        return type;
    }

    public boolean callerParameter()
    {
        return caller;
    }

    public CommandElement element()
    {
        if (caller)
        {
            return GenericArguments.none();
        }
        return CommandParameter.of(type, key);
    }

    @Override
    public String toString()
    {
        return "<" + key + ">";
    }

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
        map.put(Vector3d.class, s -> GenericArguments.vector3d(Text.of(s)));
        map.put(World.class, s -> GenericArguments.world(Text.of(s)));
        return map;
    }

    public static boolean validParameterType(Class<?> type)
    {
        return types.containsKey(type);
    }

    public static CommandElement of(Class<?> type, String key)
    {
        Function<String, CommandElement> f = types.get(type);
        if (f != null)
        {
            return f.apply(key);
        }
        return GenericArguments.none();
    }
}
