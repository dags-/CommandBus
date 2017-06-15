package me.dags.commandbus.command;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
class TypeIds {

    private final Map<Class<?>, Integer> typeIds = new HashMap<>();

    String nextId(Class<?> in) {
        Integer id = typeIds.getOrDefault(in, -1);
        typeIds.put(in, ++id);
        return String.format("%s#%s", in.getSimpleName(), id);
    }
}
