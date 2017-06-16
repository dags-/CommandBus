package me.dags.commandbus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author dags <dags@dags.me>
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {

    String value() default "";

    String description() default "";

    Assignment assign() default @Assignment(role = "", permit = false);
}
