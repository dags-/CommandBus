package me.dags.commandbus.command;

import me.dags.commandbus.flag.FlagValue;
import me.dags.commandbus.flag.Flags;

import java.util.Optional;

/**
 * @author dags_ <dags@dags.me>
 */

public class CommandParser
{
    private final char[] buf;
    private int pos = 0;

    public CommandParser(String in)
    {
        buf = in.toCharArray();
    }

    public <T> Optional<CommandEvent<T>> parse(T caller)
    {
        try
        {
            String main = "";
            Flags flags = new Flags();
            StringBuilder sb = new StringBuilder();

            if (buf[pos] == '/')
            {
                pos++;
            }

            while (pos < buf.length)
            {
                String key = readString(' ', ':');
                if (pos < buf.length && buf[pos - 1] == ':')
                {
                    if (main.isEmpty())
                    {
                        main = sb.deleteCharAt(sb.length() - 1).toString();
                    }
                    char match = ' ';
                    if (buf[pos] == '\'' && pos + 2 < buf.length)
                    {
                        match = buf[pos++];
                    }
                    String value = readString(match);
                    flags.add(key, FlagValue.of(value));
                }
                else
                {
                    sb.append(key).append(" ");
                }
            }
            if (main.isEmpty() && sb.length() > 0)
            {
                main = sb.deleteCharAt(sb.length() - 1).toString();
            }
            return Optional.of(new CommandEvent<>(caller, main, flags));
        }
        catch (Throwable t)
        {
            return Optional.empty();
        }
    }

    private String readString(char... until)
    {
        int start = pos;
        int shorten = 0;
        outer:
        while (pos < buf.length)
        {
            char c = buf[pos++];
            for (char match : until)
            {
                if (match == c)
                {
                    shorten = 1;
                    break outer;
                }
            }
        }
        return new String(buf, start, pos - start - shorten);
    }
}
