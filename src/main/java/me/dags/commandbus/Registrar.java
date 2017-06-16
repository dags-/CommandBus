package me.dags.commandbus;

import me.dags.commandbus.annotation.Assignment;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.command.CommandMethod;
import me.dags.commandbus.command.CommandNode;
import me.dags.commandbus.command.SpongeCommand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.parsing.InputTokenizer;
import org.spongepowered.api.command.args.parsing.SingleArg;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
class Registrar {

    private final Map<String, CommandNode> roots = new HashMap<>();
    private final Map<String, Permission> permissions = new HashMap<>();
    private final CommandBus commandBus;
    private boolean submitted = false;

    Registrar(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    void register(Object object) {
        Class<?> c = object.getClass();
        int count = 0;
        do {
            for (Method method : c.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Command.class)) {
                    try {
                        CommandMethod commandMethod = new CommandMethod(commandBus.getOwner().getId(), object, method);
                        CommandNode commandNode = getParentTree(commandMethod.command());
                        commandNode.addAliases(commandMethod.command().alias());
                        commandNode.addCommandMethod(commandMethod);
                        Permission permission = commandMethod.permission();
                        if (!permission.value().isEmpty()) {
                            permissions.put(commandMethod.commandString(), permission);
                        }
                        count++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            c = c.getSuperclass();
        } while (c != null && !c.equals(Object.class));
        commandBus.info("Found {} commands in class {}", count, object.getClass().getSimpleName());
    }

    void submit() {
        if (submitted) {
            throw new UnsupportedOperationException("Cannot submit commands more than once");
        }

        final Object instance = commandBus.getInstance();

        final CommandManager commandManager = Sponge.getCommandManager();
        roots.values().stream()
                .map(SpongeCommand::new)
                .forEach(command -> commandManager.register(instance, command, command.aliases()));

        final PermissionService permissionService = Sponge.getServiceManager().provideUnchecked(PermissionService.class);
        permissions.values().forEach(permission -> permissionService.newDescriptionBuilder(instance).ifPresent(builder -> {
            Assignment assignment = permission.assign();
            builder.id(permission.value());
            builder.description(Text.of(permission.description()));
            if (!assignment.role().isEmpty()) {
                builder.assign(assignment.role(), assignment.permit());
            }
            builder.register();
        }));

        commandBus.info("Registered {} main commands", roots.size());
        commandBus.info("Registered {} permissions", permissions.size());

        roots.clear();
        permissions.clear();

        submitted = true;
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
}
