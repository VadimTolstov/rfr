package io.student.rangiffler.jupiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import io.student.rangiffler.jupiter.extension.UserExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@ExtendWith(UserExtension.class)
public @interface User {
  int friends() default 0;

  int incomeInvitations() default 0;

  int outcomeInvitations() default 0;
}
