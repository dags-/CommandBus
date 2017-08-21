package me.dags.commandbus.elements;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

/**
 * @author dags <dags@dags.me>
 */
public class CatalogElement<T extends CatalogType> extends CommandElement {

    private final Class<T> type;

    public CatalogElement(String key, Class<T> type) {
        super(Text.of(key));
        this.type = type;
    }

    public Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        final String unformattedPattern = args.next();
        Pattern pattern = getFormattedPattern(unformattedPattern);

        List<String> filteredChoices = getChoices()
                .filter(s -> pattern.matcher(s).find())
                .sorted((s1, s2) -> compare(s1, s2, unformattedPattern))
                .collect(Collectors.toList());

        for (String el : filteredChoices) {
            if (el.equalsIgnoreCase(unformattedPattern)) {
                return Collections.singleton(getValue(el));
            }
        }

        Iterable<Object> ret = filteredChoices.stream().map(this::getValue).collect(Collectors.toList());
        if (!ret.iterator().hasNext()) {
            throw args.createError(t("No values matching pattern '%s' present for %s!", unformattedPattern, getKey()));
        }

        return ret;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        final Optional<String> nextArg = args.nextIfPresent();
        if (nextArg.isPresent()) {
            String unformatted = nextArg.get();
            Pattern pattern = getFormattedPattern(unformatted);
            return getChoices()
                    .filter(input -> pattern.matcher(input).find())
                    .sorted((s1, s2) -> compare(s1, s2, unformatted))
                    .collect(Collectors.toList());
        }
        return getChoices().collect(Collectors.toList());
    }

    private Stream<String> getChoices() {
        return Sponge.getRegistry().getAllOf(type)
                .stream()
                .map(CatalogType::getId);
    }

    private Object getValue(String choice) {
        final Optional<T> ret = Sponge.getGame().getRegistry().getType(type, choice);
        if (!ret.isPresent()) {
            throw new IllegalArgumentException("Invalid input " + choice + " was found");
        }
        return ret.get();
    }

    private static Pattern getFormattedPattern(String input) {
        String literal = ".*?" + Pattern.quote(input);

        if (!input.startsWith("^")) {
            input = "^" + input;
        }

        try {
            return Pattern.compile(input + "|" + literal, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            return Pattern.compile(literal, Pattern.CASE_INSENSITIVE);
        }
    }

    private static int compare(String s1, String s2, String match) {
        int i1 = s1.indexOf(match);
        int i2 = s2.indexOf(match);
        int c1 = i1 != i2 ? i1 : s1.length();
        int c2 = i1 != i2 ? i2 : s2.length();
        return Integer.compare(c1, c2);
    }
}
