package me.dags.commandbus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author dags <dags@dags.me>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    String[] alias();

    String parent() default "";

    Assignment assign() default @Assignment(role = "", permit = false);

    Permission permission() default @Permission("");

    Description description() default @Description("");
}
