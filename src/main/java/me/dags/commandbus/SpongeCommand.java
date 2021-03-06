package me.dags.commandbus;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import me.dags.command.command.Command;
import me.dags.command.command.CommandExecutor;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
class SpongeCommand extends Command<CommandSource> implements CommandCallable {

    SpongeCommand(Collection<String> aliases, Collection<CommandExecutor> executors) {
        super(aliases, executors);
    }

    @Override
    public boolean testPermission(CommandSource source, String permission) {
        return source.hasPermission(permission);
    }

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        try {
            super.processArguments(source, arguments);
            return CommandResult.success();
        } catch (Throwable t) {
            throw new CommandException(Text.of("Error executing command"), t);
        }
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) throws CommandException {
        return super.suggestCommand(source, arguments);
    }

    @Override
    public boolean testPermission(CommandSource source) {
        for (CommandExecutor e : getExecutors()) {
            if (testPermission(source, e.getPermission().value())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        String command = "/help " + getAlias();
        Text.Builder builder = Text.builder();
        builder.append(Text.of("See "));
        builder.append(Text.builder(command)
                .color(TextColors.GREEN)
                .style(TextStyles.UNDERLINE)
                .onClick(TextActions.runCommand(command)).build());
        builder.append(Text.of(" for more information"));
        builder.onHover(TextActions.showText(generateHelp(source)));
        return Optional.of(builder.build());
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return Optional.of(generateHelp(source));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.builder(String.format("See '/help %s' for more information", getAlias()))
                .onClick(TextActions.runCommand(String.format("/help %s", getAlias())))
                .build();
    }

    public String getAlias() {
        return getAliases().get(0);
    }

    private Text generateHelp(CommandSource source) {
        AtomicBoolean first = new AtomicBoolean(true);
        Text.Builder builder = Text.builder();

        getExecutors().stream()
                .filter(e -> testPermission(source, e.getPermission().value()))
                .sorted(Comparator.comparing(e -> e.getUsage().value()))
                .map(e -> Text.builder("/" + e.getUsage().value())
                        .onHover(TextActions.showText(Text.of(e.getDescription().value())))
                        .build()
                ).forEach(t -> {
                    if (!first.getAndSet(false)) {
                        builder.append(Text.NEW_LINE);
                    }
                    builder.append(t);
                });

        return builder.build();
    }
}
