package me.dags.commandbusold.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */

public class Value
{
    private static final Map<Class<?>, Adapter> typeAdapters = adapters();
    public static final Value empty = new Value(Double.NaN);

    private final Object value;

    public Value(Object in)
    {
        value = in;
    }

    public boolean present()
    {
        return this != empty;
    }

    public String string()
    {
        return value.toString();
    }

    public boolean bool()
    {
        if (!present())
        {
            return false;
        }
        return (Boolean) value;
    }

    public Number number()
    {
        return (Number) value;
    }

    @Override
    public String toString()
    {
        return value.toString();
    }

    protected Object as(Class<?> type)
    {
        if (typeAdapters.containsKey(type) && type.isInstance(value))
        {
            return typeAdapters.get(type).adapt(this);
        }
        return null;
    }

    interface Adapter
    {
        public Object adapt(Value value);
    }

    private static Map<Class<?>, Adapter> adapters()
    {
        Map<Class<?>, Adapter> typeAdapters = new HashMap<>();
        typeAdapters.put(boolean.class, Value::bool);
        typeAdapters.put(Boolean.class, Value::bool);
        typeAdapters.put(double.class, v -> v.number().doubleValue());
        typeAdapters.put(Double.class, v -> v.number().doubleValue());
        typeAdapters.put(int.class, v -> v.number().intValue());
        typeAdapters.put(Integer.class, v -> v.number().intValue());
        typeAdapters.put(String.class, Value::string);
        typeAdapters.put(Value.class, v -> v);
        return Collections.unmodifiableMap(typeAdapters);
    }

    protected static boolean validType(Class<?> clazz)
    {
        return typeAdapters.containsKey(clazz);
    }
}
