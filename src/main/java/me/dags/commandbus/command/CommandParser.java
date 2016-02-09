package me.dags.commandbus.command;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */

public class CommandParser
{
    private final String in;
    private int pos = 0;

    public CommandParser(String in)
    {
        this.in = in;
    }

    private boolean hasNext()
    {
        return pos < in.length() - 1;
    }

    private char read()
    {
        return pos < in.length() ? in.charAt(pos) : (char) -1;
    }

    public <T> Optional<CommandEvent<T>> parse(T caller)
    {
        try
        {
            String command = readCommand();
            if (command.isEmpty()) return Optional.empty();

            CommandEvent<T> event = new CommandEvent<T>(caller, command);
            readFlags(event);

            return Optional.of(event);
        }
        catch (Throwable t)
        {
            return Optional.empty();
        }
    }

    public void readFlags(CommandEvent e)
    {
        while (hasNext())
        {
            String key = readKey();
            if (key.isEmpty()) break;
            String value = readValue();
            if (value.isEmpty()) break;
            e.add(key, parseValue(value));
        }
    }

    private String readCommand()
    {
        int start = in.charAt(0) == '/' ? 1 : 0, lastSpace = start;
        while (hasNext() && read() != ':')
        {
            if (read() == ' ')
            {
                lastSpace = pos;
            }
            pos++;
            if (!hasNext())
            {
                lastSpace = in.length();
            }
        }
        pos = lastSpace + 1;
        return in.substring(start, lastSpace).trim();
    }

    private String readKey()
    {
        int start = pos;
        while (hasNext() && read() != ':')
        {
            pos++;
        }
        return in.substring(start, pos++).trim();
    }

    private String readValue()
    {
        int start = pos, lastSpace = start;
        while (hasNext() && read() != ':')
        {
            if (read() == ' ')
            {
                lastSpace = pos;
            }
            pos++;
            if (!hasNext())
            {
                lastSpace = in.length();
            }
        }
        pos = lastSpace + 1;
        return in.substring(start, lastSpace).trim();
    }

    private Value parseValue(String in)
    {
        if (isNumber(in))
        {
            if (in.contains("."))
            {
                return new Value(Double.valueOf(in));
            }
            return new Value(Integer.valueOf(in));
        }
        if (in.equalsIgnoreCase("true") || in.equalsIgnoreCase("false"))
        {
            return new Value(Boolean.valueOf(in));
        }
        return new Value(in);
    }

    public static boolean isNumber(String s)
    {
        if (s == null || s.isEmpty())
        {
            return false;
        }
        boolean decimal = false;
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            if (Character.isDigit(c) || (i == 0 && c == '-' && s.length() > 1))
            {
                continue;
            }
            if (!decimal && (decimal = c == '.') && s.length() - 1 > i)
            {
                continue;
            }
            return false;
        }
        return true;
    }
}
