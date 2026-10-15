package io.student.rangiffler.config;

public final class TestConfig {

  private TestConfig() {}

  public static final String FRONTEND_URL = "http://localhost:3001";
  public static final String AUTH_URL = "http://localhost:9001";
  public static final String REGISTER_PAGE = AUTH_URL + "/register?theme=light";
  public static final String LOGIN_PAGE = AUTH_URL + "/login";
}
