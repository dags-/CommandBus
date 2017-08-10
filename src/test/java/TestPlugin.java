import me.dags.command.annotation.Command;
import me.dags.command.annotation.Join;
import me.dags.command.annotation.Src;
import me.dags.commandbus.CommandBus;
import me.dags.fmt.Fmt;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;

import java.util.Collection;

/**
 * @author dags <dags@dags.me>
 */
@Plugin(id = "test", name = "Test", version = "1", description = "shh")
public class TestPlugin {

    @Listener
    public void init(GameInitializationEvent e) {
        CommandBus.create(this).register(this).submit();
    }

    @Command("block|b <block> print")
    public void block(@Src CommandSource source, BlockState block) {
        source.sendMessage(Text.of("Block: ", block));
    }

    @Command("color|c <color> print")
    public void color(@Src CommandSource source, TextColor color) {
        source.sendMessage(Text.of("Color: ", color.getName(), color));
    }

    @Command("colors <color>")
    public void colors(@Src CommandSource source, Collection<TextColor> color) {
        for (TextColor c : color) {
            source.sendMessage(Text.of("Color: ", c.getName(), c));
        }
    }

    @Command("say")
    public void say(@Src CommandSource source, TextColor color, @Join String message) {
        source.sendMessage(Text.of("Message: ", message, color));
    }

    @Command("tell <player> message|msg <message>")
    public void tell(@Src CommandSource source, Player player, @Join String message) {
        Fmt.stress("You -> %s: ", source.getName()).info(message).tell(source);
        Fmt.stress("%s -> You: ", player.getName()).info(message).tell(source);
    }
}
