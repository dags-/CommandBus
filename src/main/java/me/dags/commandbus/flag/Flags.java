package me.dags.commandbus.flag;

import me.dags.commandbus.annotation.FlagFilter;
import me.dags.commandbus.command.Result;

import java.util.Arrays;
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

    public boolean hasAny(String... flags)
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

    public boolean hasAll(String... flags)
    {
        for (String s : flags)
        {
            if (!hasFlag(s))
            {
                return false;
            }
        }
        return true;
    }

    public Result filter(FlagFilter filter)
    {
        if (!hasAll(filter.require()))
        {
            return Result.Type.MISSING_FLAGS.toResult(Arrays.toString(filter.require()));
        }
        if (hasAny(filter.block()))
        {
            return Result.Type.ILLEGAL_FLAGS.toResult(Arrays.toString(filter.block()));
        }
        return Result.Type.SUCCESS.toResult("Passed all flag filters!");
    }

    public FlagValue get(String flag)
    {
        return flags.get(flag.toLowerCase());
    }

    public Optional<FlagValue> get(String... flags)
    {
        for (String s : flags)
        {
            if (hasFlag(s))
            {
                return Optional.of(get(s));
            }
        }
        return Optional.empty();
    }

    public Flags ifPresent(String flag, Consumer<FlagValue> consumer)
    {
        if (hasFlag(flag))
        {
            consumer.accept(get(flag));
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
}
