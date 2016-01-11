package me.dags.commandbus.command;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author dags_ <dags@dags.me>
 */

public class Result<T>
{
    public static final String NOT_RECOGNISED = "Command not recognized!";
    public static final String PERMISSION_DENIED = "Insufficient permissions!";

    private final Optional<CommandEvent<T>> event;
    private final String message;
    private final boolean parsed;
    private final boolean success;

    private Result(Builder<T> builder)
    {
        this.event = builder.event == null ? Optional.<CommandEvent<T>>empty() : Optional.of(builder.event);
        this.message = builder.message;
        this.parsed = builder.parsed;
        this.success = builder.success;
    }

    public Optional<CommandEvent<T>> event()
    {
        return event;
    }

    public String message()
    {
        return message;
    }

    public boolean parsed()
    {
        return parsed;
    }

    public boolean success()
    {
        return success;
    }

    public void onPass(Consumer<Result<T>> consumer)
    {
        if (success)
        {
            consumer.accept(this);
        }
    }

    public void onFail(Consumer<Result<T>> consumer)
    {
        if (!success)
        {
            consumer.accept(this);
        }
    }

    public static <T> Builder<T> builder(CommandEvent<T> e)
    {
        return new Builder<>(e);
    }

    public static <T> Result<T> parseError(String input)
    {
        return new Builder<T>(null).message(input).success(false).parsed(false).build();
    }

    public static class Builder<T>
    {
        private CommandEvent<T> event;
        private String message = "";
        private boolean parsed = true;
        private boolean success = false;

        private Builder(CommandEvent<T> e)
        {
            event = e;
        }

        public Builder<T> message(String s)
        {
            message = s;
            return this;
        }

        public Builder<T> success(boolean b)
        {
            success = b;
            return this;
        }

        public Builder<T> parsed(boolean b)
        {
            parsed = b;
            return this;
        }

        public boolean success()
        {
            return success;
        }

        public Result<T> build()
        {
            return new Result<>(this);
        }
    }
}
