package me.dags.commandbus;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public final class CommandBus {

    private final Registrar registrar;
    private final boolean logging;
    private final Logger logger;

    private CommandBus(Builder builder) {
        this.logger = builder.logger;
        this.logging = builder.logging;
        this.registrar = new Registrar(this);
    }

    public CommandBus registerPackageOf(Class<?> child) {
        return registerPackage(false, child.getPackage().getName());
    }

    public CommandBus registerSubPackagesOf(Class<?> child) {
        return registerPackage(true, child.getPackage().getName());
    }

    public CommandBus registerPackage(String... path) {
        return registerPackage(false, path);
    }

    public CommandBus registerPackage(boolean recurse, String... path) {
        info("Scanning package {} for commands...", Arrays.toString(path));
        ScanResult result = new FastClasspathScanner(path).disableRecursiveScanning(!recurse).scan();
        List<String> matches = result.getNamesOfAllClasses();
        info("Discovered {} Command classes in package {}", matches.size(), path);
        for (String name : matches) {
            try {
                Class<?> clazz = Class.forName(name);
                register(clazz);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

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

    public CommandBus register(Object... objects) {
        for (Object o : objects) {
            register(o);
        }
        return this;
    }

    private void register(Object object) {
        registrar.register(object);
    }

    public void submit(Object plugin) {
        Optional<PluginContainer> container = Sponge.getPluginManager().fromInstance(plugin);
        if (!container.isPresent()) {
            String warn = "Attempted to register commands for %s, but it is not a valid Sponge Plugin!";
            String message = String.format(warn, plugin.getClass());
            throw new IllegalArgumentException(message);
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

    public static CommandBus create() {
        return builder().build();
    }

    public static CommandBus create(Logger logger) {
        return builder().logger(logger).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Logger logger = LoggerFactory.getLogger(CommandBus.class.getSimpleName());
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

        public CommandBus build() {
            return new CommandBus(this);
        }
    }
}
