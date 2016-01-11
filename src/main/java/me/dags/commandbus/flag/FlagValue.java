package me.dags.commandbus.flag;

/**
 * @author dags_ <dags@dags.me>
 */

public class FlagValue
{
    private final Object value;

    private FlagValue(Object in)
    {
        value = in;
    }

    public String string()
    {
        return value.toString();
    }

    public Number number()
    {
        return (Number) value;
    }

    public boolean bool()
    {
        return (Boolean) value;
    }

    public String toString()
    {
        return string();
    }

    public boolean isNumber()
    {
        return value instanceof Number;
    }

    public boolean isBool()
    {
        return value instanceof Boolean;
    }

    public static FlagValue of(String value)
    {
        if (isNumber(value))
        {
            return new FlagValue(Double.parseDouble(value));
        }
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
        {
            return new FlagValue(Boolean.parseBoolean(value.toString()));
        }
        return new FlagValue(value);
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
