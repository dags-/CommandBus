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

import me.dags.commandbus.command.ParameterTypes;
import me.dags.commandbus.exception.CommandRegistrationException;
import me.dags.commandbus.format.FMT;
import me.dags.commandbus.format.Format;
import me.dags.commandbus.format.FormatSerializer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

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
public final class CommandBus {

    static {
        TypeSerializers.getDefaultSerializers().registerType(Format.TYPE_TOKEN, FormatSerializer.INSTANCE);
    }

    private final Registrar registrar = new Registrar(this);
    private final ParameterTypes parameterTypes;
    private final boolean logging;
    private final Logger logger;
    private final Format format;

    private CommandBus(Builder builder) {
        this.logger = builder.logger;
        this.logging = builder.logging;
        this.format = builder.format;
        this.parameterTypes = new ParameterTypes(builder.types);
    }

    /**
     * Register Commands for the given class(es).
     * CommandBus will attempt to create a new instance and then register
     * it's Methods annotated with @Command. The class must have an accessible
     * default constructor.
     *
     * @param classes The class(es) to register.
     * @return The current CommandBus instance (for chaining).
     */
    public CommandBus register(Class<?>... classes) {
        for (Class<?> c : classes) {
            register(c);
        }
        return this;
    }

    private void register(Class<?> clazz) {
        try {
            Object object = clazz.newInstance();
            register(object);
        } catch (InstantiationException | IllegalAccessException e) {
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
     * @param objects The object(s) to register.
     * @return The current CommandBus instance (for chaining).
     */
    public CommandBus register(Object... objects) {
        for (Object o : objects) {
            register(o);
        }
        return this;
    }

    private void register(Object object) {
        registrar.register(object);
    }

    /**
     * Submits registered Commands to Sponge.
     * This method should be called after Command classes/objects have been
     * registered via the register() methods.
     *
     * @param plugin The Plugin associated with the Commands to be registered.
     */
    public void submit(Object plugin) {
        Optional<PluginContainer> container = Sponge.getPluginManager().fromInstance(plugin);
        if (!container.isPresent()) {
            String warn = "Attempted to register commands for %s, but it is not a valid Sponge Plugin!";
            throw new CommandRegistrationException(warn, plugin.getClass());
        }
        info("Registering commands for {}", container.get().getId());
        registrar.submit(plugin);
    }

    void info(String message, Object... args) {
        if (logging) {
            logger.info(message, args);
        }
    }

    void warn(String message, Object... args) {
        if (logging) {
            logger.warn(message, args);
        }
    }

    void error(String message, Object... args) {
        if (logging) {
            logger.error(message, args);
        }
    }

    ParameterTypes getParameterTypes() {
        return parameterTypes;
    }

    Format getFormat() {
        return format;
    }

    public static CommandBus create() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Map<Class<?>, Function<Text, CommandElement>> types = new HashMap<>();
        private Logger logger = LoggerFactory.getLogger(CommandBus.class.getSimpleName());
        private Format format = FMT.copy();
        private boolean logging = true;

        public Builder logging(boolean logging) {
            this.logging = logging;
            return this;
        }

        public Builder logger(Logger logger) {
            if (logger != null) {
                this.logger = logger;
            }
            return this;
        }

        public Builder format(Format format) {
            this.format = format;
            return this;
        }

        public Builder parameter(Class<?> type, Function<Text, CommandElement> function) {
            this.types.put(type, function);
            return this;
        }

        public CommandBus build() {
            return new CommandBus(this);
        }
    }
}
