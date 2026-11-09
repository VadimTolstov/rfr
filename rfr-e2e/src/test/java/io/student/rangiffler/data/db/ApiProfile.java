package io.student.rangiffler.data.db;

/** Creates and checks the API profile belonging to a test user. */
public record ApiProfile(String username) {
  public void create() {
    Jdbc.inTransaction(Databases.API, jdbc -> Jdbc.requireOneRow(jdbc.sql(Jdbc.loadSql("sql/api/insert_user.sql"))
        .param("username", username)
        .update(), "API user"));
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
