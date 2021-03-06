package me.dags.commandbus;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import me.dags.command.CommandManager;
import me.dags.command.annotation.Permission;
import me.dags.command.command.CommandExecutor;
import me.dags.command.command.CommandFactory;
import me.dags.command.element.ElementFactory;
import me.dags.command.utils.MarkdownWriter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;

/**
 * @author dags <dags@dags.me>
 */
public class CommandBus extends CommandManager<SpongeCommand> {

    private final PluginContainer plugin;

    private CommandBus(CommandManager.Builder<SpongeCommand> builder) {
        super(builder);
        Object owner = getOwner();
        if (owner instanceof PluginContainer) {
            plugin = (PluginContainer) owner;
        } else {
            plugin = Sponge.getPluginManager().fromInstance(owner)
                    .orElseThrow(() -> new IllegalArgumentException("Provided object is not a plugin instance"));
        }
    }

    @Override
    public String getOwnerId() {
        return plugin.getId();
    }

    @Override
    public void info(String message, Object... args) {
        plugin.getLogger().info(String.format(message, args));
    }

    @Override
    public void warn(String message, Object... args) {
        plugin.getLogger().warn(String.format(message, args));
    }

    @Override
    protected void submit(Object owner, SpongeCommand command) {
        Sponge.getCommandManager().register(owner, command, command.getAliases());
        registerPermissions(command);
        generateDocs(command);
    }

    private void registerPermissions(SpongeCommand command) {
        PermissionService service = Sponge.getServiceManager().provideUnchecked(PermissionService.class);

        for (CommandExecutor e : command.getExecutors()) {
            Permission permission = e.getPermission();
            if (permission.value().isEmpty()) {
                continue;
            }

            PermissionDescription.Builder builder = service.newDescriptionBuilder(getOwner());
            builder.description(Text.of("Allows use of /", e.getUsage().value()));
            builder.id(permission.value());
            if (!permission.role().value().isEmpty()) {
                builder.assign(permission.role().value(), permission.role().permit());
            }

            builder.register();
        }
    }

    private void generateDocs(SpongeCommand command) {
        Path commandBus = Sponge.getGame().getGameDirectory().resolve("config").resolve("commandbus");
        Path file = commandBus.resolve(String.format("%s-%s.md", plugin.getId(), command.getAlias()));

        try {
            Files.createDirectories(commandBus);
            try (Writer writer = Files.newBufferedWriter(file)) {
                try (MarkdownWriter mdwriter = new MarkdownWriter(writer)) {
                    mdwriter.writeHeaders();
                    for (CommandExecutor e : command.getExecutors()) {
                        mdwriter.writeCommand(e);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ElementFactory.Builder elements() {
        return SpongeElementFactory.builder();
    }

    public static CommandBus.Builder builder() {
        return new Builder()
                .elements(elements().build())
                .commands(SpongeCommand::new);
    }

    public static CommandBus create() {
        Optional<PluginContainer> plugin = Sponge.getCauseStackManager().getCurrentCause().last(PluginContainer.class);

        if (!plugin.isPresent()) {
            plugin = Sponge.getCauseStackManager().getContext(EventContextKeys.PLUGIN);
        }

        PluginContainer container = plugin.orElseThrow(() -> new IllegalStateException("Unable to determine active PluginContainer"));

        return builder().owner(container).build();
    }

    public static CommandBus create(Object plugin) {
        return builder().owner(plugin).build();
    }

    public static class Builder extends CommandManager.Builder<SpongeCommand> {

        @Override
        public Builder commands(CommandFactory<SpongeCommand> command) {
            super.commands(command);
            return this;
        }

        @Override
        public Builder elements(ElementFactory factory) {
            super.elements(factory);
            return this;
        }

        @Override
        public Builder owner(Object plugin) {
            super.owner(plugin);
            return this;
        }

        public CommandBus build() {
            return super.build(CommandBus::new);
        }
    }
}
