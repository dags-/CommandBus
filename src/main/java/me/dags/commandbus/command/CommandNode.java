package me.dags.commandbus.command;

import me.dags.commandbus.annotation.Description;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class CommandNode {

    private final String main;
    private final Set<String> aliases = new LinkedHashSet<>();
    private final Set<CommandNode> children = new HashSet<>();
    private final Set<CommandMethod> methods = new HashSet<>();

    public void addCommandMethod(CommandMethod method) {
        methods.add(method);
    }

    public CommandNode(String... aliases) {
        main = aliases[0];
        addAliases(aliases);
    }

    public CommandNode addAliases(String... aliases) {
        Collections.addAll(this.aliases, aliases);
        return this;
    }

    public CommandNode getOrCreateChild(String alias) {
        CommandNode node = getChild(alias);
        if (node == null) {
            node = new CommandNode(alias);
            children.add(node);
        }
        return node;
    }

    String getAlias() {
        return main;
    }

    CommandNode getChild(String node) {
        for (CommandNode child : children) {
            if (child.matches(node)) {
                return child;
            }
        }
        return null;
    }

    void parse(CommandSource source, CommandArgs args, int length, List<CommandMethod.Instance> results, List<CommandException> errors) {
        int state = (int) args.getState();
        int remaining = length - state - 1;

        for (CommandMethod method : this.methods) {
            if (test(source, method)) {
                try {
                    args.setState(state);
                    CommandMethod.Instance instance = method.parse(source, args, remaining);
                    results.add(instance);
                } catch (CommandException e) {
                    errors.add(e);
                }
            } else {
                errors.add(new CommandException(Text.of("You do not have permission to use this command")));
            }
        }

        args.setState(state);
        Optional<String> next = args.nextIfPresent();

        if (next.isPresent()) {
            CommandNode node = getChild(next.get());
            if (node != null) {
                node.parse(source, args, length, results, errors);
            }
        }
    }

    void suggest(CommandSource source, CommandArgs args, int length, Collection<String> suggestions) throws ArgumentParseException {
        int state = (int) args.getState();
        int remaining = length - state - 1;

        String next = args.next();
        CommandNode child = getChild(next);

        if (child != null) {
            child.suggest(source, args, remaining, suggestions);
            return;
        }

        for (CommandMethod method : methods) {
            if (test(source, method)) {
                args.setState(state);
                method.suggest(source, args, remaining, suggestions);
            }
        }

        args.setState(state);
        for (CommandNode node : children) {
            if (node.testPermission(source)) {
                for (String alias : node.aliases) {
                    if (alias.startsWith(next) && alias.length() > next.length()) {
                        suggestions.add(alias);
                        break;
                    }
                }
            }
        }
    }

    void populateHelp(CommandSource source, String parent, List<Text> help) {
        String node = parent.equals("/") ? parent + main : parent + " " + main;

        for (CommandMethod method : methods) {
            if (!test(source, method)) {
                continue;
            }

            String usage = node + " " + method.usage();
            Description description = method.description();
            Text.Builder builder = Text.builder(usage).color(TextColors.YELLOW);

            if (!description.value().isEmpty()) {
                builder.append(Text.builder(" - " + description.value()).color(TextColors.WHITE).build());
            }

            help.add(builder.build());
        }

        for (CommandNode child : children) {
            child.populateHelp(source, node, help);
        }
    }

    List<String> aliases() {
        return new ArrayList<>(aliases);
    }

    boolean testPermission(CommandSource source) {
        for (CommandMethod method : methods) {
            if (test(source, method)) {
                return true;
            }
        }
        for (CommandNode child : children) {
            if (child.testPermission(source)) {
                return true;
            }
        }
        return false;
    }

    private boolean test(CommandSource source, CommandMethod method) {
        return method.permission().value().isEmpty() || source.hasPermission(method.permission().value());
    }

    private boolean matches(String alias) {
        return aliases.contains(alias);
    }
}
