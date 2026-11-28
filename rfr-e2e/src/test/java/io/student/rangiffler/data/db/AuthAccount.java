package io.student.rangiffler.data.db;

import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Creates and checks the AUTH account and its required authorities. */
public record AuthAccount(String username, String password) {
  public void create() {
    create(UUID.randomUUID());
  }

  public void create(UUID id) {
    String encoded = "{bcrypt}" + new BCryptPasswordEncoder().encode(password);
    Jdbc.inTransaction(Databases.AUTH, jdbc -> {
      Jdbc.requireOneRow(jdbc.sql(Jdbc.loadSql("sql/auth/insert_user.sql"))
          .param("id", id.toString())
          .param("username", username)
          .param("password", encoded)
          .update(), "AUTH user");
      createAuthority(jdbc, "read");
      createAuthority(jdbc, "write");
    });
  }

  public void delete(UUID id) {
    Jdbc.inTransaction(Databases.AUTH, jdbc -> {
      jdbc.sql("DELETE FROM authority WHERE user_id = UUID_TO_BIN(:id, TRUE)")
          .param("id", id.toString()).update();
      jdbc.sql("DELETE FROM `user` WHERE id = UUID_TO_BIN(:id, TRUE)")
          .param("id", id.toString()).update();
    });
  }

  public void delete() {
    Jdbc.inTransaction(Databases.AUTH, jdbc -> {
      jdbc.sql(Jdbc.loadSql("sql/auth/delete_authority.sql"))
          .param("username", username)
          .update();
      jdbc.sql(Jdbc.loadSql("sql/auth/delete_user.sql"))
          .param("username", username)
          .update();
    });
  }

  public boolean exists() {
    return Jdbc.getJdbcClient(Databases.AUTH).sql(Jdbc.loadSql("sql/auth/exists_account.sql"))
        .param("username", username)
        .query(Boolean.class)
        .single();
  }

  private void createAuthority(JdbcClient jdbc, String authority) {
    Jdbc.requireOneRow(jdbc.sql(Jdbc.loadSql("sql/auth/insert_authority.sql"))
        .param("username", username)
        .param("authority", authority)
        .update(), "AUTH authority " + authority);
  }

}
