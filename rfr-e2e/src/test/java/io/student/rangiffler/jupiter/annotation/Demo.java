package io.student.rangiffler.jupiter.annotation;

import io.student.rangiffler.jupiter.extension.DemoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Registers the callback demonstration for a whole test class. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DemoExtension.class)
public @interface Demo {
}
