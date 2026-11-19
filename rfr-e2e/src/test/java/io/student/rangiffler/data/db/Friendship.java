package io.student.rangiffler.data.db;

/** Creates and checks one accepted friendship or pending invitation. */
public record Friendship(String requester, String addressee, FriendshipStatus status) {
  public void create() {
    Jdbc.inTransaction(Databases.API, jdbc -> Jdbc.requireOneRow(jdbc.sql(Jdbc.loadSql(insertSql()))
        .param("requesterUsername", requester)
        .param("addresseeUsername", addressee)
        .update(), "API " + status.name().toLowerCase()));
  }

  public void delete() {
    Jdbc.inTransaction(Databases.API, jdbc -> jdbc.sql(Jdbc.loadSql(deleteSql()))
        .param("requesterUsername", requester)
        .param("addresseeUsername", addressee)
        .update());
  }

  public boolean exists() {
    return Jdbc.getJdbcClient(Databases.API).sql(Jdbc.loadSql(existsSql()))
        .param("requesterUsername", requester)
        .param("addresseeUsername", addressee)
        .query(Boolean.class)
        .single();
  }

  private String insertSql() {
    return switch (status) {
      case ACCEPTED -> "sql/api/insert_friendship.sql";
      case PENDING -> "sql/api/insert_invitation.sql";
    };
  }

  private String deleteSql() {
    return switch (status) {
      case ACCEPTED -> "sql/api/delete_friendship.sql";
      case PENDING -> "sql/api/delete_invitation.sql";
    };
  }

  private String existsSql() {
    return switch (status) {
      case ACCEPTED -> "sql/api/exists_friendship.sql";
      case PENDING -> "sql/api/exists_invitation.sql";
    };
  }
}
