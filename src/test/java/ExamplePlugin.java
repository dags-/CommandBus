import me.dags.commandbus.CommandBus;
import me.dags.commandbus.annotation.*;
import me.dags.commandbus.command.CommandFlags;
import me.dags.fmt.Fmt;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.format.TextColor;

import java.util.Collection;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
@Plugin(id = "example", name = "Example", version = "1.0", description = "Example plugin")
public class ExamplePlugin {

    @Listener
    public void init(GameInitializationEvent event) {
        CommandBus.create(this).register(this).submit();
    }

    @Permission
    @Command(alias = "pm")
    @Description("Send a private message")
    public void pm(@Src Player src, Player target, @Join String message) {
        Fmt.stress("You -> %s: ", target.getName()).info(message).tell(src);
        Fmt.stress("%s -> You: ", src.getName()).info(message).tell(src);
    }

    @Permission
    @Command(alias = "all", parent = "pm")
    @Description("Send a private message")
    public void pma(CommandSource src, Collection<Player> targets, @Join String message) {
        Fmt.stress("You -> All: ").info(message).tell(src);
        Fmt.stress("%s -> You: ", src.getName()).info(message).tell(targets);
    }

    @Permission
    @Command(alias = "comp", parent = "test")
    @Assignment(role = "admin", permit = true)
    @Description("Test command completions for an input string")
    public void test(CommandSource src, @Join("command") String input) {
        List<String> list = Sponge.getCommandManager().getSuggestions(src, input, null);
        Fmt.stress("Suggestions: ").tell(src);
        list.forEach(s -> Fmt.info(" - ").stress(s).tell(src));
    }

    @Permission
    @Command(alias = "test")
    @Description("Test a command with a CatalogType argument and Flags")
    @Flags({@Flag("accept"), @Flag(value = "block", type = BlockType.class)})
    public void test(CommandSource src, TextColor color, CommandFlags flags) {
        boolean accept = flags.getOrDefault("accept", false);
        BlockType type = flags.getOrDefault("block", BlockTypes.AIR);
        Fmt.info("Color: ").stress(color).tell(src);
        Fmt.info("Accept: ").stress(accept).tell(src);
        Fmt.info("Block Type: ").stress(type).tell(src);
    }
}
