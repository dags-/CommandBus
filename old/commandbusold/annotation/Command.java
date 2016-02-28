package me.dags.commandbusold.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author dags <dags@dags.me>
 */

@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Command
{
    public String[] alias();

    public String permission() default "";

    public String description() default "";
}
