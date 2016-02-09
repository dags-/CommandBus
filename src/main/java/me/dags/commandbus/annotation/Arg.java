package me.dags.commandbus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author dags <dags@dags.me>
 */

@Target(value = ElementType.PARAMETER)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Arg
{
    public String[] a();
}
