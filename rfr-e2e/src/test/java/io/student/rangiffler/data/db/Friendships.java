package io.student.rangiffler.data.db;

/** Creates and removes friendship rows associated with one test user. */
public record Friendships(String username) {
  public void create(Friendship friendship) {
    if (!friendship.requester().equals(username)
        && !friendship.addressee().equals(username)) {
      throw new IllegalArgumentException("Friendship must include user " + username);
    }
    friendship.create();
  }

  public void delete() {
    Jdbc.inTransaction(Databases.API, jdbc -> jdbc.sql(Jdbc.loadSql("sql/api/delete_user_friendships.sql"))
        .param("username", username)
        .update());
  }

  public boolean exists() {
    return Jdbc.getJdbcClient(Databases.API).sql(Jdbc.loadSql("sql/api/exists_friendships.sql"))
        .param("username", username)
        .query(Boolean.class)
        .single();
  }
}
