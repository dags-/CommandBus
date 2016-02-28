package me.dags.commandbusold.command;

import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */

public class Result
{
    public final Type type;
    public final String message;

    private Result(Result.Type type, String message)
    {
        this.type = type;
        this.message = message;
    }

    public Result onPass(Consumer<Result> consumer)
    {
        if (type == Result.Type.SUCCESS)
        {
            consumer.accept(this);
        }
        return this;
    }

    public Result onFail(Consumer<Result> consumer)
    {
        if (type != Result.Type.SUCCESS)
        {
            consumer.accept(this);
        }
        return this;
    }

    public enum Type
    {
        CALL_ERROR("Command failed to execute correctly"),
        MISSING_FLAG("Missing or incorrect flags provided"),
        NO_PERMISSION("Missing permission"),
        NOT_RECOGNISED("Command not recognised"),
        PARSE_ERROR("Unable to parse input"),
        SUCCESS("Success"),
        ;

        private final String prefix;

        private Type(String prefix)
        {
            this.prefix = prefix + ": ";
        }

        public Result toResult(String message)
        {
            return new Result(this, prefix + message);
        }
    }
}
