package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.data.UserFixture;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.utils.RandomDataUtils;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

public final class UserExtension implements BeforeEachCallback, ParameterResolver {
  private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(UserExtension.class);

  @Override
  public void beforeEach(ExtensionContext context) {
    TestUser user = new TestUser(RandomDataUtils.randomUsername(), RandomDataUtils.randomPassword());
    UserFixture fixture = new UserFixture(user);
    context.getStore(NAMESPACE).put(context.getUniqueId(), fixture);
    fixture.create();
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return parameterContext.getParameter().getType().equals(TestUser.class)
        && parameterContext.isAnnotated(User.class);
  }

  @Override
  public TestUser resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    UserFixture fixture = extensionContext.getStore(NAMESPACE)
        .get(extensionContext.getUniqueId(), UserFixture.class);
    return fixture.user();
  }
}
