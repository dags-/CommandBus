package me.dags.commandbus;

import me.dags.commandbus.annotation.Assignment;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.command.CommandMethod;
import me.dags.commandbus.command.CommandNode;
import me.dags.commandbus.command.SpongeCommand;
import me.dags.commandbus.utils.TableWriter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.parsing.InputTokenizer;
import org.spongepowered.api.command.args.parsing.SingleArg;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dags <dags@dags.me>
 */
class Registrar {

    private final Map<String, CommandNode> roots = new HashMap<>();
    private final List<CommandMethod> methods = new LinkedList<>();
    private final CommandBus commandBus;

    Registrar(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    void register(Object object) {
        Class<?> c = object.getClass();
        do {
            for (Method method : c.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Command.class)) {
                    registerMethod(object, method);
                }
            }
            c = c.getSuperclass();
        } while (c != null && !c.equals(Object.class));
        commandBus.info("Found {} commands in class {}", methods.size(), object.getClass().getSimpleName());
    }

    void submit() {
        final Object instance = commandBus.getInstance();
        final AtomicInteger counter = new AtomicInteger(0);
        final CommandManager commandManager = Sponge.getCommandManager();
        final PermissionService permissionService = Sponge.getServiceManager().provideUnchecked(PermissionService.class);

        roots.values().stream()
                .map(SpongeCommand::new)
                .forEach(command -> commandManager.register(instance, command, command.aliases()));

        methods.stream()
                .filter(method -> !method.permission().value().isEmpty())
                .forEach(method -> submitPermission(instance, permissionService, method, counter));

        try (TableWriter writer = getDocsWriter(commandBus.getOwner())) {
            writer.writeHeaders();
            methods.stream()
                    .sorted((m1, m2) -> m1.commandString().compareTo(m2.commandString()))
                    .forEach(writer::writeMethod);
        } catch (IOException e) {
            e.printStackTrace();
        }

        commandBus.info("Registered {} main commands", roots.size());
        commandBus.info("Registered {} permissions", counter.get());

        roots.clear();
        methods.clear();
    }

    private void registerMethod(Object src, Method method) {
        try {
            CommandMethod commandMethod = new CommandMethod(commandBus.getOwner().getId(), src, method);
            CommandNode commandNode = getParentTree(commandMethod.command());
            commandNode.addAliases(commandMethod.command().alias());
            commandNode.addCommandMethod(commandMethod);
            methods.add(commandMethod);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void submitPermission(Object plugin, PermissionService service, CommandMethod method, AtomicInteger counter) {
        service.newDescriptionBuilder(plugin).ifPresent(builder -> {
            Permission permission = method.permission();
            Assignment assignment = permission.assign();
            builder.id(permission.value());
            builder.description(Text.of(permission.description()));
            if (!assignment.role().isEmpty()) {
                builder.assign(assignment.role(), assignment.permit());
            }
            builder.register();
            counter.getAndAdd(1);
        });
    }

    private CommandNode getRoot(String arg) {
        CommandNode root = roots.get(arg);
        if (root == null) {
            roots.put(arg, root = new CommandNode(arg));
        }
        return root;
    }

    private CommandNode getParentTree(Command command) throws ArgumentParseException {
        if (command.parent().isEmpty()) {
            return getRoot(command.alias()[0]);
        } else {
            List<SingleArg> list = InputTokenizer.quotedStrings(false).tokenize(command.parent(), true);
            CommandArgs args = new CommandArgs(command.parent(), list);
            CommandNode node = getRoot(args.next());
            while (args.hasNext()) {
                String next = args.next();
                node = node.getOrCreateChild(next);
            }
            return node.getOrCreateChild(command.alias()[0]);
        }
    }

    private static TableWriter getDocsWriter(PluginContainer plugin) throws IOException {
        Path commandBus = Sponge.getGame().getGameDirectory().resolve("config").resolve("commandbus");

        if (!Files.exists(commandBus)) {
            Files.createDirectories(commandBus);
        }

        Path file = commandBus.resolve(plugin.getId() + ".md");
        Writer writer = Files.newBufferedWriter(file);

        return new TableWriter(writer);
    }
}
