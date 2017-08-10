package me.dags.commandbus;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import me.dags.command.command.CommandException;
import me.dags.command.command.Input;
import me.dags.command.element.function.ValueParser;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
class SpongeParsers {

    static final ValueParser<Player> PLAYER = s -> {
        for (Player player : Sponge.getServer().getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(s)) {
                return player;
            }
        }
        throw new CommandException("Could not find Player '%s'", s);
    };

    static final ValueParser<User> USER = s -> {
        UserStorageService service = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
        for (GameProfile profile : service.match(s)) {
            if (profile.getName().orElse("").equalsIgnoreCase(s)) {
                Optional<User> user = service.get(profile.getUniqueId());
                if (user.isPresent()) {
                    return user.get();
                }
            }
        }
        throw new CommandException("Could not find User '%s'", s);
    };

    static final ValueParser<World> WORLD = s -> {
        for (World world : Sponge.getServer().getWorlds()) {
            if (world.getName().equalsIgnoreCase(s)) {
                return world;
            }
        }
        throw new CommandException("Could not find World '%s'", s);
    };

    static final ValueParser<Vector3i> VEC3I = new ValueParser<Vector3i>() {
        @Override
        public Vector3i parse(Input input) throws CommandException {
            int x = Integer.parseInt(input.next());
            int y = Integer.parseInt(input.next());
            int z = Integer.parseInt(input.next());
            return new Vector3i(x, y, z);
        }

        @Override
        public Vector3i parse(String s) throws CommandException {
            return parse(new Input(s));
        }
    };

    static final ValueParser<Vector3d> VEC3D = new ValueParser<Vector3d>() {
        @Override
        public Vector3d parse(Input input) throws CommandException {
            double x = Double.parseDouble(input.next());
            double y = Double.parseDouble(input.next());
            double z = Double.parseDouble(input.next());
            return new Vector3d(x, y, z);
        }

        @Override
        public Vector3d parse(String s) throws CommandException {
            return parse(new Input(s));
        }
    };

    @SuppressWarnings("unchecked")
    static ValueParser<Object> catalogType(Class<?> type) {
        return new ValueParser<Object>() {
            @Override
            public Object parse(Input input) throws CommandException {
                String s = input.next();
                Optional<?> val = Sponge.getRegistry().getType((Class<? extends CatalogType>) type, s);
                if (val.isPresent()) {
                    return val.get();
                }
                throw new CommandException("'%s' is not a valid %s", s, type.getSimpleName());
            }

            @Override
            public Object parse(String s) throws CommandException {
                return parse(new Input(s));
            }
        };
    }
}
