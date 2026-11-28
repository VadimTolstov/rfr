package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.data.TestUser;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.extension.ParameterResolutionException;

/** Applies the same fixture parameter contract during preparation and resolution. */
final class UserParameters {
    private UserParameters() {
    }

    static boolean supported(Parameter parameter, Class<? extends Annotation> annotation) {
        return parameter.isAnnotationPresent(annotation) && parameter.getType().equals(TestUser.class);
    }

    static List<Integer> indexes(Method method, Class<? extends Annotation> annotation) {
        Parameter[] parameters = method.getParameters();
        List<Integer> indexes = new ArrayList<>();
        for (int index = 0; index < parameters.length; index++) {
            Parameter parameter = parameters[index];
            if (parameter.isAnnotationPresent(annotation)) {
                if (!supported(parameter, annotation)) {
                    throw new ParameterResolutionException("@" + annotation.getSimpleName() + " parameter at index "
                            + index + " in " + method.getName() + " must have type TestUser");
                }
                indexes.add(index);
            }
        }
        return indexes;
    }
}
