package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.data.db.Jdbc;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class UserFixture implements ExtensionContext.Store.CloseableResource {
  private final TestUser user;
  private final BCryptPasswordEncoder passwordEncoder;

  public UserFixture(TestUser user) {
    this.user = user;
    passwordEncoder = new BCryptPasswordEncoder();
  }

  public void create() {
    String encodedPassword = "{bcrypt}" + passwordEncoder.encode(user.password());
    Jdbc.inTransaction(Databases.AUTH, jdbc -> {
      jdbc.sql(Jdbc.loadSql("sql/auth/insert_user.sql"))
          .param("username", user.username())
          .param("password", encodedPassword)
          .update();
      insertAuthority(jdbc, "read");
      insertAuthority(jdbc, "write");
    });
    Jdbc.inTransaction(Databases.API, jdbc -> jdbc.sql(Jdbc.loadSql("sql/api/insert_user.sql"))
        .param("username", user.username())
        .update());
  }

  public TestUser user() {
    return user;
  }

  @Override
  public void close() {
    try {
      Jdbc.inTransaction(Databases.API, jdbc -> jdbc.sql(Jdbc.loadSql("sql/api/delete_user.sql"))
          .param("username", user.username())
          .update());
    } finally {
      Jdbc.inTransaction(Databases.AUTH, jdbc -> {
        jdbc.sql(Jdbc.loadSql("sql/auth/delete_authority.sql"))
            .param("username", user.username())
            .update();
        jdbc.sql(Jdbc.loadSql("sql/auth/delete_user.sql"))
            .param("username", user.username())
            .update();
      });
    }
  }

  private void insertAuthority(JdbcClient jdbc, String authority) {
    jdbc.sql(Jdbc.loadSql("sql/auth/insert_authority.sql"))
        .param("username", user.username())
        .param("authority", authority)
        .update();
  }
}
