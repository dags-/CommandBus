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
import me.dags.commandbus.exception.CommandRegistrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */

/**
 * The CommandBus handles the processing of Objects/Classes utilising
 * the @Command annotation.
 * Commands should first be registered via the register() methods, and finally,
 * submitted to Sponge via the submit() method - all subcommands of a given command
 * should be registered through the same CommandBus instance, before being submitted.
 */
public final class CommandBus
{
    private final Registry registry = new Registry(this);
    private final boolean logging;
    private final Logger logger;

    private CommandBus()
    {
        this(true);
    }

    private CommandBus(boolean logging)
    {
        this.logging = logging;
        this.logger = LoggerFactory.getLogger(CommandBus.class);
    }

    private CommandBus(Logger logger)
    {
        this.logging = true;
        this.logger = logger;
    }

    /**
     * Register Commands for the given class(es).
     * CommandBus will attempt to create a new instance and then register
     * it's Methods annotated with @Command. The class must have an accessible
     * default constructor.
     *
     * @param clazz The class to register.
     * @return The current CommandBus instance (for chaining).
     */
    public CommandBus register(Class<?>... classes)
    {
        for (Class<?> c : classes)
        {
            register(c);
        }
        return this;
    }

    private void register(Class<?> clazz)
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
    }

    /**
     * Register Commands for the given object(s).
     * CommandBus will search for Methods annotated with @Command
     * and generate a Command from the provided infomation and Method
     * Parameters.
     * This method also searches through an object's super class heirarchy.
     *
     * @param object The object to register.
     * @return The current CommandBus instance (for chaining).
     */
    public CommandBus register(Object... objects)
    {
        for (Object o : objects)
        {
            register(o);
        }
        return this;
    }

    private void register(Object object)
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
                    SpongeCommand command = new SpongeCommand(object, method, method.getAnnotation(Command.class));
                    registry.add(command);
                }
            }
            c = c.getSuperclass();
        }
        while (!c.equals(Object.class));
        info("Discovered {} command methods in class {}", count, object.getClass());
    }

    /**
     * Submits registered Commands to Sponge.
     * This method should be called after Command classes/objects have been
     * registered via the register() methods.
     *
     * @param plugin The Plugin associated with the Commands to be registered.
     */
    public void submit(Object plugin)
    {
        Optional<PluginContainer> container = Sponge.getPluginManager().fromInstance(plugin);
        if (!container.isPresent())
        {
            String warn = "Attempted to register commands for %s, but it is not a valid Sponge Plugin!";
            throw new CommandRegistrationException(warn, plugin.getClass());
        }
        info("Registering commands for {}", container.get().getName());
        registry.submit(plugin);
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

    /**
     * Get a new default CommandBus instance.
     *
     * @return The newly created CommandBus instance using the default logger.
     */
    public static CommandBus newInstance()
    {
        return new CommandBus();
    }

    /**
     * Get a new CommandBus instance using the provided logger.
     *
     * @param logger The logger this new CommandBus should use
     * @return The newly created CommandBus using the provided logger.
     */
    public static CommandBus newInstance(Logger logger)
    {
        if (logger == null)
        {
            return newInstance();
        }
        return new CommandBus(logger);
    }

    /**
     * Get a new CommandBus instance with logging disabled.
     *
     * @return The newly created CommandBus with logging disabled.
     */
    public static CommandBus newSilentInstance()
    {
        return new CommandBus(false);
    }
}
