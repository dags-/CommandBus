package me.dags.commandbus.command;

import com.flowpowered.math.vector.Vector3d;
import me.dags.commandbus.elements.CatalogElement;
import me.dags.commandbus.elements.UserElement;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author dags <dags@dags.me>
 */
class ParameterTypes {

    private static final Map<Class<?>, Function<String, CommandElement>> types;

    static {
        Map<Class<?>, Function<String, CommandElement>> map = new HashMap<>();
        map.put(boolean.class, s -> GenericArguments.bool(Text.of(s)));
        map.put(Boolean.class, map.get(boolean.class));
        map.put(byte.class, s -> GenericArguments.integer(Text.of(s)));
        map.put(Byte.class, map.get(byte.class));
        map.put(double.class, s -> GenericArguments.doubleNum(Text.of(s)));
        map.put(Double.class, map.get(double.class));
        map.put(float.class, map.get(double.class));
        map.put(Float.class, map.get(float.class));
        map.put(int.class, s -> GenericArguments.integer(Text.of(s)));
        map.put(Integer.class, map.get(int.class));
        map.put(long.class, map.get(int.class));
        map.put(Long.class, map.get(long.class));
        map.put(short.class, s -> GenericArguments.integer(Text.of(s)));
        map.put(Short.class, map.get(short.class));
        map.put(Location.class, s -> GenericArguments.location(Text.of(s)));
        map.put(Player.class, s -> GenericArguments.player(Text.of(s)));
        map.put(String.class, s -> GenericArguments.string(Text.of(s)));
        map.put(User.class, UserElement::new);
        map.put(Vector3d.class, s -> GenericArguments.vector3d(Text.of(s)));
        map.put(World.class, s -> GenericArguments.world(Text.of(s)));
        types = map;
    }

    public static void register(Class<?> type, Function<String, CommandElement> func) {
        types.put(type, func);
    }

    static void typeCheck(Class<?> type, Parameter source) {
        if (!validParameterType(type)) {
            String warn = "Parameter %s is not supported a supported type %s!";
            String message = String.format(warn, source, type);
            throw new IllegalArgumentException(message);
        }
    }

    private static boolean validParameterType(Class<?> type) {
        return types.containsKey(type)
                || CatalogType.class.isAssignableFrom(type)
                || Enum.class.isAssignableFrom(type)
                || CommandFlags.class == type;
    }

    @SuppressWarnings("unchecked")
    static CommandElement of(Class<?> type, String key) {
        Function<String, CommandElement> f1 = types.get(type);
        if (f1 != null) {
            return f1.apply(key);
        }
        if (CatalogType.class.isAssignableFrom(type)) {
            return new CatalogElement<>(key, (Class<? extends CatalogType>) type);
        }
        if (Enum.class.isAssignableFrom(type)) {
            return GenericArguments.enumValue(Text.of(key), (Class<? extends Enum>) type);
        }
        return GenericArguments.none();
    }
}
