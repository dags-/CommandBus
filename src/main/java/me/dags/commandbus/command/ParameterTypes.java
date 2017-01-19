package me.dags.commandbus.command;

import com.flowpowered.math.vector.Vector3d;
import me.dags.commandbus.exception.ParameterAnnotationException;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author dags <dags@dags.me>
 */
public class ParameterTypes {

    private static final Map<Class<?>, Function<String, CommandElement>> types = init();

    private static Map<Class<?>, Function<String, CommandElement>> init() {
        Map<Class<?>, Function<String, CommandElement>> map = new HashMap<>();
        map.put(boolean.class, s -> GenericArguments.bool(Text.of(s)));
        map.put(Boolean.class, map.get(boolean.class));
        map.put(double.class, s -> GenericArguments.doubleNum(Text.of(s)));
        map.put(Double.class, map.get(double.class));
        map.put(float.class, map.get(double.class));
        map.put(Float.class, map.get(float.class));
        map.put(int.class, s -> GenericArguments.integer(Text.of(s)));
        map.put(Integer.class, map.get(int.class));
        map.put(long.class, map.get(int.class));
        map.put(Long.class, map.get(long.class));
        map.put(Location.class, s -> GenericArguments.location(Text.of(s)));
        map.put(Player.class, s -> GenericArguments.player(Text.of(s)));
        map.put(String.class, s -> GenericArguments.string(Text.of(s)));
        map.put(User.class, s -> GenericArguments.user(Text.of(s)));
        map.put(Vector3d.class, s -> GenericArguments.vector3d(Text.of(s)));
        map.put(World.class, s -> GenericArguments.world(Text.of(s)));
        return Collections.unmodifiableMap(map);
    }

    private final Map<Class<?>, Function<Text, CommandElement>> custom;

    public ParameterTypes() {
        this.custom = Collections.emptyMap();
    }

    public ParameterTypes(Map<Class<?>, Function<Text, CommandElement>> custom) {
        this.custom = Collections.unmodifiableMap(custom);
    }

    public void typeCheck(Class<?> type, Parameter source) {
        if (!validParameterType(type)) {
            String warn = "Parameter %s is not supported a supported type %s!";
            throw new ParameterAnnotationException(warn, source, type);
        }
    }

    public boolean validParameterType(Class<?> type) {
        return types.containsKey(type) || custom.containsKey(type)
                || CatalogType.class.isAssignableFrom(type)
                || Enum.class.isAssignableFrom(type);
    }

    @SuppressWarnings("unchecked")
    public CommandElement of(Class<?> type, String key) {
        Function<String, CommandElement> f1 = types.get(type);
        if (f1 != null) {
            return f1.apply(key);
        }
        Function<Text, CommandElement> f2 = custom.get(type);
        if (f2 != null) {
            return f2.apply(Text.of(key));
        }
        if (CatalogType.class.isAssignableFrom(type)) {
            return GenericArguments.catalogedElement(Text.of(key), (Class<? extends CatalogType>) type);
        }
        if (Enum.class.isAssignableFrom(type)) {
            return GenericArguments.enumValue(Text.of(key), (Class<? extends Enum>) type);
        }
        return GenericArguments.none();
    }
}
