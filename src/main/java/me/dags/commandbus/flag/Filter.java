package me.dags.commandbus.flag;

import me.dags.commandbus.annotation.FlagFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author dags_ <dags@dags.me>
 */

public class Filter
{
    private final List<String[]> required = new ArrayList<>();
    private final List<String[]> blocked = new ArrayList<>();
    public final FlagFilter flagFilter;

    private Filter(FlagFilter f)
    {
        flagFilter = f;
    }

    public boolean allow(Set<String> in)
    {
        for (String[] aliases : required)
        {
            boolean match = false;
            for (String alias : aliases)
            {
                if (in.contains(alias))
                {
                    match = true;
                    break;
                }
            }
            if (!match)
            {
                return false;
            }
        }
        return true;
    }

    public boolean block(Set<String> in)
    {
        for (String[] aliases : blocked)
        {
            boolean match = false;
            for (String alias : aliases)
            {
                if (in.contains(alias))
                {
                    match = true;
                    break;
                }
            }
            if (match)
            {
                return true;
            }
        }
        return false;
    }

    public static Filter of(FlagFilter filter)
    {
        Filter f = new Filter(filter);
        for (String s : filter.require())
        {
            f.required.add(s.split("\\|"));
        }
        for (String s : filter.block())
        {
            f.blocked.add(s.split("\\|"));
        }
        return f;
    }
}
