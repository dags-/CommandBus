package me.dags.commandbus.flag;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author dags_ <dags@dags.me>
 */

public class Flags
{
    private final Map<String, FlagValue> flags = new HashMap<>();

    public void add(String s, FlagValue f)
    {
        flags.put(s.toLowerCase(), f);
    }

    public boolean hasFlag(String flag)
    {
        return flags.containsKey(flag.toLowerCase());
    }

    public boolean hasFlag(String... flags)
    {
        for (String s : flags)
        {
            if (hasFlag(s))
            {
                return true;
            }
        }
        return false;
    }

    public FlagValue getFlag(String flag)
    {
        return flags.get(flag.toLowerCase());
    }

    public Optional<FlagValue> getFlag(String... flags)
    {
        for (String s : flags)
        {
            if (hasFlag(s))
            {
                return Optional.of(getFlag(s));
            }
        }
        return Optional.empty();
    }

    public Flags ifPresent(String flag, Consumer<FlagValue> consumer)
    {
        if (hasFlag(flag))
        {
            consumer.accept(getFlag(flag));
        }
        return this;
    }

    public Flags ifAbsent(String flag, Consumer<Flags> consumer)
    {
        if (!hasFlag(flag))
        {
            consumer.accept(this);
        }
        return this;
    }

    public Flags ifPresent(Consumer<FlagValue> consumer, String... flags)
    {
        getFlag(flags).ifPresent(consumer);
        return this;
    }

    public Flags ifAbsent(Consumer<Flags> consumer, String... flag)
    {
        if (!hasFlag(flag))
        {
            consumer.accept(this);
        }
        return this;
    }
}
