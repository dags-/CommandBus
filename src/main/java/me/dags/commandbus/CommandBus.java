package me.dags.commandbus;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
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
    private final PluginContainer owner;
    private final Object instance;

    private CommandBus(PluginContainer owner, Object instance) {
        this.owner = owner;
        this.registrar = new Registrar(this);
        this.instance = instance;
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

    public void submit() {
        info("Registering commands for {}", owner.getId());
        registrar.submit();
    }

    PluginContainer getOwner() {
        return owner;
    }

    Object getInstance() {
        return instance;
    }

    void info(String message, Object... args) {
        owner.getLogger().info(message, args);
    }

    void warn(String message, Object... args) {
        owner.getLogger().warn(message, args);
    }

    void error(String message, Object... args) {
        owner.getLogger().error(message, args);
    }

    public static CommandBus create(Object plugin) {
        final PluginContainer container;
        final Object instance;

        if (plugin instanceof PluginContainer) {
            container = (PluginContainer) plugin;
            Optional<?> optional = container.getInstance();
            if (!optional.isPresent()) {
                throw new UnsupportedOperationException("Could not get plugin instance from container");
            }
            instance = optional.get();
        } else {
            Optional<PluginContainer> optional = Sponge.getPluginManager().fromInstance(plugin);
            if (!optional.isPresent()) {
                throw new UnsupportedOperationException("Provided object is not a plugin");
            }
            container = optional.get();
            instance = plugin;
        }

        return new CommandBus(container, instance);
    }
}
