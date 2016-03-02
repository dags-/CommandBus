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

package me.dags.commandbus;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.command.SpongeCommand;
import me.dags.commandbus.exception.InvalidPluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */

public final class CommandBus
{
    private final Logger logger = LoggerFactory.getLogger(CommandBus.class);
    private final Registry registry = new Registry(this);
    private final boolean logging;
    private final Object plugin;

    public CommandBus(Object plugin)
    {
        checkPlugin(plugin);
        this.plugin = plugin;
        this.logging = true;
    }

    public CommandBus(Object plugin, boolean logging)
    {
        checkPlugin(plugin);
        this.plugin = plugin;
        this.logging = logging;
    }

    public CommandBus register(Class<?> clazz)
    {
        info("Attempting to instantiate class {}", clazz);
        try
        {
            Object object = clazz.newInstance();
            register(object);
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            error("Failed to instantiate class {}, make sure there is an accessible default constructor", clazz);
            e.printStackTrace();
        }
        return this;
    }

    public CommandBus register(Object object)
    {
        Class<?> c = object.getClass();
        info("Scanning {} for @Command methods", c);
        int count = 0;
        do
        {
            for (Method method : object.getClass().getDeclaredMethods())
            {
                if (method.isAnnotationPresent(Command.class))
                {
                    count++;
                    SpongeCommand command = new SpongeCommand(object, method);
                    registry.add(command);
                }
            }
            c = c.getSuperclass();
        }
        while (!c.equals(Object.class));
        info("Discovered {} command methods in class {}", count, object.getClass());
        return this;
    }

    public CommandBus submitCommands()
    {
        Optional<PluginContainer> container = Sponge.getPluginManager().fromInstance(plugin);
        if (!container.isPresent())
        {
            throw new InvalidPluginException(plugin);
        }
        info("Registering commands for {}", container.get().getName());
        registry.submit(plugin);
        return this;
    }

    protected void info(String message, Object... args)
    {
        if (logging)
        {
            logger.info(message, args);
        }
    }

    protected void warn(String message, Object... args)
    {
        if (logging)
        {
            logger.warn(message, args);
        }
    }

    protected void error(String message, Object... args)
    {
        if (logging)
        {
            logger.error(message, args);
        }
    }

    private static void checkPlugin(Object plugin)
    {
        Optional<PluginContainer> container = Sponge.getPluginManager().fromInstance(plugin);
        if (!container.isPresent())
        {
            throw new InvalidPluginException(plugin);
        }
    }
}
