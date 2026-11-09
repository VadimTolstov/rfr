package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.data.StaticUserFixture;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.StaticUser;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/** Resolves static user parameters through JVM-wide queue leases. */
public final class UsersQueueExtension implements BeforeEachCallback, ParameterResolver {
  private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(UsersQueueExtension.class);

  @Override
  public void beforeEach(ExtensionContext context) {
    Method method = context.getTestMethod()
        .orElseThrow(() -> new ParameterResolutionException("Test method was not found for " + context.getDisplayName()));
    Parameter[] parameters = method.getParameters();
    ExtensionContext.Store store = context.getStore(NAMESPACE);
    boolean found = false;
    for (int index = 0; index < parameters.length; index++) {
      if (parameters[index].isAnnotationPresent(StaticUser.class)) {
        found = true;
        String key = storeKey(context, index);
        if (store.get(key) != null) {
          continue;
        }
        StaticUserFixture fixture = new StaticUserFixture();
        store.put(key, fixture);
        fixture.create();
      }
    }
    if (!found) {
      throw new ParameterResolutionException("StaticUser annotation was not found for test " + context.getDisplayName());
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return parameterContext.getParameter().getType().equals(TestUser.class)
        && parameterContext.isAnnotated(StaticUser.class)
        && extensionContext.getTestMethod().filter(parameterContext.getDeclaringExecutable()::equals).isPresent();
  }

  @Override
  public TestUser resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    StaticUserFixture fixture = extensionContext.getStore(NAMESPACE)
        .get(storeKey(extensionContext, parameterContext.getIndex()), StaticUserFixture.class);
    if (fixture == null) {
      throw new ParameterResolutionException("Static user fixture was not created for test " + extensionContext.getDisplayName());
    }
    return fixture.user();
  }

  private String storeKey(ExtensionContext context, int parameterIndex) {
    return context.getUniqueId() + ":" + parameterIndex;
  }
}
