package me.dags.commandbus.command;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.parsing.InputTokenizer;
import org.spongepowered.api.command.args.parsing.SingleArg;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class SpongeCommand implements CommandCallable {

    private static final InputTokenizer TOKENIZER = InputTokenizer.quotedStrings(false);
    private final CommandNode root;

    public SpongeCommand(CommandNode root) {
        this.root = root;
    }

    public List<String> aliases() {
        return root.aliases();
    }

    @Override
    public CommandResult process(CommandSource source, String rawArgs) throws CommandException {
        List<SingleArg> singleArgs = TOKENIZER.tokenize(rawArgs, true);
        CommandArgs args = new CommandArgs(rawArgs, singleArgs);
        LinkedList<CommandMethod.Instance> instances = new LinkedList<>();
        LinkedList<CommandException> exceptions = new LinkedList<>();

        root.parse(source, args, singleArgs.size(), instances, exceptions);
        if (instances.isEmpty()) {
            if (exceptions.isEmpty()) {
                throw new CommandException(Text.of("Command not recognized for input '" + rawArgs + "'"));
            }
            throw exceptions.getLast();
        }

        Collections.sort(instances);
        Exception exception = null;
        for (CommandMethod.Instance instance : instances) {
            try {
                instance.invoke(source);
                return CommandResult.success();
            } catch (Exception e) {
                e.printStackTrace();
                exception = e;
            }
        }

        if (exception != null) {
            throw new CommandException(Text.of("An error occurred whilst executing the command"), exception);
        }

        return CommandResult.empty();
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> location) throws CommandException {
        Set<String> suggestions = new LinkedHashSet<>();
        List<SingleArg> singleArgs = TOKENIZER.tokenize(arguments, true);
        CommandArgs args = new CommandArgs(arguments, singleArgs);
        root.suggest(source, args, singleArgs.size(), suggestions);
        return new ArrayList<>(suggestions);
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return root.testPermission(source);
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        String command = "/help " + root.getAlias();
        Text.Builder builder = Text.builder();
        builder.append(Text.of("See "));
        builder.append(Text.builder(command)
                .color(TextColors.GREEN)
                .style(TextStyles.UNDERLINE)
                .onClick(TextActions.runCommand(command)).build());
        builder.append(Text.of(" for more info"));
        builder.onHover(TextActions.showText(generateHelp(source)));
        return Optional.of(builder.build());
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return Optional.of(generateHelp(source));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of("USE", Text.NEW_LINE, "AGAIN");
    }

    private Text generateHelp(CommandSource source) {
        List<Text> help = new LinkedList<>();
        root.populateHelp(source, "/", help);
        Text.Builder builder = Text.builder();
        boolean first = true;
        for (Text line : help) {
            if (!first) {
                builder.append(Text.NEW_LINE);
            }
            builder.append(line);
            first = false;
        }
        return builder.build();
    }
}
