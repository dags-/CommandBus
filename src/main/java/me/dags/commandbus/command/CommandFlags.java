package me.dags.commandbus.command;

import org.spongepowered.api.text.Text;

import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class CommandFlags {

    private final Map<Text, LinkedList<Object>> flags = new HashMap<>();

    public boolean has(String key) {
        return has(Text.of(key));
    }

    public boolean has(Text key) {
        return flags.containsKey(key);
    }

    public <T> T getOrDefault(String key, T def) {
        return getOrDefault(Text.of(key), def);
    }

    public <T> T getOrDefault(Text key, T def) {
        Optional<T> t = get(key);
        return t.orElse(def);
    }

    public <T> Optional<T> get(String key) {
        return get(Text.of(key));
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(Text key) {
        LinkedList<Object> list = flags.get(key);
        if (list != null) {
            return Optional.ofNullable((T) list.getFirst());
        }
        return Optional.empty();
    }

    public <T> List<T> getAll(String key) {
        return getAll(Text.of(key));
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getAll(Text key) {
        List<T> list = (List<T>) flags.get(key);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    public <T> CommandFlags put(Text key, T value) {
        LinkedList<Object> list = flags.get(key);
        if (list == null) {
            flags.put(key, list = new LinkedList<>());
        }
        list.add(value);
        return this;
    }
}
