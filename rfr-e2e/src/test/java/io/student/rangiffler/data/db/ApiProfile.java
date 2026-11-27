package io.student.rangiffler.data.db;

import java.util.UUID;

/** Creates and checks the API profile belonging to a test user. */
public record ApiProfile(String username) {
  public void create() {
    create(UUID.randomUUID());
  }

  public void create(UUID id) {
    Jdbc.inTransaction(Databases.API, jdbc -> Jdbc.requireOneRow(jdbc.sql(Jdbc.loadSql("sql/api/insert_user.sql"))
        .param("id", id.toString())
        .param("username", username)
        .update(), "API user"));
  }

  public void delete(UUID id) {
    Jdbc.inTransaction(Databases.API, jdbc -> {
      jdbc.sql("""
          DELETE FROM friendship
          WHERE requester_id = UUID_TO_BIN(:id, TRUE) OR addressee_id = UUID_TO_BIN(:id, TRUE)
          """).param("id", id.toString()).update();
      jdbc.sql("DELETE FROM `user` WHERE id = UUID_TO_BIN(:id, TRUE)")
          .param("id", id.toString()).update();
    });
  }

  public void delete() {
    Jdbc.inTransaction(Databases.API, jdbc -> {
      jdbc.sql(Jdbc.loadSql("sql/api/delete_user_friendships.sql"))
          .param("username", username)
          .update();
      jdbc.sql(Jdbc.loadSql("sql/api/delete_user.sql"))
          .param("username", username)
          .update();
    });
  }

  public boolean exists() {
    return Jdbc.getJdbcClient(Databases.API).sql(Jdbc.loadSql("sql/api/exists_profile.sql"))
        .param("username", username)
        .query(Boolean.class)
        .single();
  }
}
