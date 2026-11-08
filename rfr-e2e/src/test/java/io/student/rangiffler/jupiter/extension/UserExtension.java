package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.data.UserFixture;
import io.student.rangiffler.jupiter.request.UserRequest;
import io.student.rangiffler.jupiter.annotation.User;
import java.util.Arrays;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public final class UserExtension implements BeforeEachCallback, ParameterResolver {
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(UserExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        User annotation = context.getTestMethod()
                .stream()
                .flatMap(method -> Arrays.stream(method.getParameters()))
                .filter(parameter -> parameter.isAnnotationPresent(User.class))
                .map(parameter -> parameter.getAnnotation(User.class))
                .findFirst()
                .orElseThrow(() -> new ParameterResolutionException("User annotation was not found for test " + context.getDisplayName()));
        UserFixture fixture = new UserRequest(annotation.friends(), annotation.incomeInvitations()).fixture();
        context.getStore(NAMESPACE).put(context.getUniqueId(), fixture);
        fixture.create();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType().equals(TestUser.class)
                && parameterContext.isAnnotated(User.class)
                && extensionContext.getTestMethod().filter(parameterContext.getDeclaringExecutable()::equals).isPresent();
    }

    @Override
    public TestUser resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        UserFixture fixture = extensionContext.getStore(NAMESPACE)
                .get(extensionContext.getUniqueId(), UserFixture.class);
        if (fixture == null) {
            throw new ParameterResolutionException("User fixture was not created for test " + extensionContext.getDisplayName());
        }
        return fixture.user();
    }
}
