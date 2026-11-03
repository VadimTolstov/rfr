package io.student.rangiffler.data;

public final class TestUser {
  private final String username;
  private final String password;

  public TestUser(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public String username() {
    return username;
  }

  public String password() {
    return password;
  }
}
