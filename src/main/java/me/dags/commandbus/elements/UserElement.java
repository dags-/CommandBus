package me.dags.commandbus.elements;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author dags <dags@dags.me>
 */
public class UserElement extends CommandElement {

    public UserElement(String key) {
        super(Text.of(key));
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String next = args.next();
        List<User> users = match(next).collect(Collectors.toList());
        for (User user : users) {
            if (user.getName().equalsIgnoreCase(next)) {
                return user;
            }
        }
        return users;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        Optional<String> next = args.nextIfPresent();
        if (next.isPresent()) {
            return match(next.get())
                    .map(User::getName)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Stream<User> match(String name) {
        UserStorageService service = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
        return service.match(name).stream()
                .map(service::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted((u1, u2) -> {
                    if (u1.isOnline() != u2.isOnline()) {
                        // reverse u1/u2 so that true values come first
                        return Boolean.compare(u2.isOnline(), u1.isOnline());
                    }
                    // shortest name comes first
                    return Integer.compare(u1.getName().length(), u2.getName().length());
                });
    }
}
