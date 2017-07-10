package me.dags.commandbus.elements;

import me.dags.commandbus.command.CommandFlags;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class FlagElement extends CommandElement {

    private final Map<String, CommandElement> flagElements;

    public FlagElement(String key, Map<String, CommandElement> flags) {
        super(Text.of(key));
        this.flagElements = flags;
    }

    @Nullable
    @Override
    protected CommandFlags parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        Object state = args.getState();
        args.setState(-1);
        CommandFlags flags = new CommandFlags();
        while (args.hasNext()) {
            String next = args.next();

            if (next.startsWith("--")) {
                if (!flagElements.containsKey(next)) {
                    throw args.createError(Text.of("Unexpected flag '", next, "'"));
                }

                if (!args.hasNext()) {
                    throw args.createError(Text.of("Expected flag value for ", next));
                }

                Text key = Text.of(next.substring(2));
                CommandElement element = flagElements.get(next);
                CommandContext context = new CommandContext();
                element.parse(source, args, context);
                context.getOne(key).ifPresent(value -> flags.put(key, value));
                continue;
            }

            if (next.startsWith("-")) {
                if (!flagElements.containsKey(next)) {
                    throw args.createError(Text.of("Unexpected flag '", next, "'"));
                }

                flags.put(Text.of(next.substring(1)), true);
            }
        }
        args.setState(state);
        return flags;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        final int state = (int) args.getState();

        try {
            String next = args.next();
            if (next.startsWith("-")) {
                Pattern pattern = Pattern.compile("^" + Pattern.quote(next), Pattern.CASE_INSENSITIVE);
                return flagElements.keySet().stream().filter(s -> pattern.matcher(s).find()).collect(Collectors.toList());
            }

            if (state > 1) {
                args.setState(state - 1);
                String previous = args.next();
                args.setState(state);

                if (previous.startsWith("--")) {
                    CommandElement element = flagElements.get(previous);
                    if (element != null) {
                        return element.complete(src, args, context);
                    }
                }
            }

            return Collections.emptyList();
        } catch (ArgumentParseException e) {
            return Collections.emptyList();
        }
    }
}
