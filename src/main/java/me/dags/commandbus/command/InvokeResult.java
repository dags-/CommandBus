package me.dags.commandbus.command;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tristate;

/**
 * @author dags <dags@dags.me>
 */
class InvokeResult {

    static final InvokeResult EMPTY = InvokeResult.of(Tristate.UNDEFINED, "");
    static final InvokeResult SUCCESS = InvokeResult.of(Tristate.TRUE, "");
    static final InvokeResult UNKNOWN = InvokeResult.of(Tristate.UNDEFINED, "An error occurred whilst invoking the command");
    static final InvokeResult NO_PERM = InvokeResult.of(Tristate.FALSE, "You do not have permission to do that");

    private final Tristate success;
    private final Text info;

    private InvokeResult(Tristate success, Text info) {
        this.success = success;
        this.info = info;
    }

    Text info() {
        return info;
    }

    Tristate state() {
        return success;
    }

    InvokeResult or(InvokeResult other) {
        if (this.success == Tristate.TRUE) {
            return this;
        }
        if (this == EMPTY || other.success != Tristate.UNDEFINED) {
            return other;
        }
        return this;
    }

    static InvokeResult of(Tristate result, String info) {
        TextColor color = result == Tristate.FALSE ? TextColors.RED : TextColors.GRAY;
        Text message = Text.builder(info).color(color).build();
        return new InvokeResult(result, message);
    }
}
