package me.dags.test;

import me.dags.commandbus.annotation.Arg;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.title.Title;

/**
 * @author dags <dags@dags.me>
 */

public class Commands
{
    @Command(alias = {"set title", "set t", "s t"})
    public void title(@Caller Player player, @Arg(value={"first","f"}) String first)
    {
        Title title = Title.of(Text.of(first));
        player.sendTitle(title);
    }

    @Command(alias = {"set subtitle", "set st", "s st"})
    public void subtitle(@Caller Player player, @Arg(value={"second","f"}) String second)
    {
        Title title = Title.of(Text.EMPTY, Text.of(second));
        player.sendTitle(title);
    }

    @Command(alias = {"set title", "set t", "s t"})
    public void both(@Caller Player player, @Arg(value={"first","f"})String first, @Arg(value={"second","f"})String second)
    {
        Title title = Title.of(Text.of(first), Text.of(second));
        player.sendTitle(title);
    }
}
