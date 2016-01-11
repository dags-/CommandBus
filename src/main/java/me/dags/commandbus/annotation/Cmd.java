package me.dags.commandbus.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author dags_ <dags@dags.me>
 */

@Retention(value = RetentionPolicy.RUNTIME)
public @interface Cmd
{
    String[] aliases();

    String permission() default "";
}
