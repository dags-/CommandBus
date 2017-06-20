package me.dags.commandbus.utils;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * @author dags <dags@dags.me>
 */
public class VarargElement extends CommandElement {

    private final CommandElement element;
    private final Set<String> flags;

    public VarargElement(Text key, CommandElement element, Set<String> flags) {
        super(key);
        this.element = element;
        this.flags = flags;
    }

    @Override
    public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        while (args.hasNext()) {
            if (flags.contains(args.peek())) {
                break;
            }
            element.parse(source, args, context);
        }
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        return null;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return element.complete(src, args, context);
    }
}
