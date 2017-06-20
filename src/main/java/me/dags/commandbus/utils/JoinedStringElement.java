package me.dags.commandbus.utils;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author dags <dags@dags.me>
 */
public class JoinedStringElement extends CommandElement {

    private final Set<String> flags;
    private final String separator;

    public JoinedStringElement(Text key, String separator, Set<String> flags) {
        super(key);
        this.separator = separator;
        this.flags = flags;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        StringBuilder sb = new StringBuilder();

        while (args.hasNext()) {
            if (flags.contains(args.peek())) {
                break;
            }
            sb.append(sb.length() > 0 ? separator : "").append(args.next());
        }

        return sb.toString();
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return Collections.emptyList();
    }
}
