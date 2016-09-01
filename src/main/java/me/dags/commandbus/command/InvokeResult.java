package me.dags.commandbus.command;

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
    private final String message;

    private InvokeResult(Tristate success, String message) {
        this.success = success;
        this.message = message;
    }

    String message() {
        return message;
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
        return new InvokeResult(result, info);
    }
}
