package io.student.rangiffler.config;

public interface TestConfig {
  String FRONTEND_URL = getProperty("rfr.frontUrl", "http://localhost:3001");
  String AUTH_URL = getProperty("rfr.authUrl", "http://localhost:9001");
  String API_URL = getProperty("rfr.apiUrl", "http://localhost:8081");
  String AUTH_DB_URL = getProperty("rfr.authDbUrl", "jdbc:mysql://localhost:3306/rangiffler-auth");
  String API_DB_URL = getProperty("rfr.apiDbUrl", "jdbc:mysql://localhost:3306/rangiffler-api");
  String DB_USERNAME = getProperty("rfr.dbUser", "root");
  String DB_PASSWORD = getProperty("rfr.dbPassword", "secret");

  private static String getProperty(String key, String defaultValue) {
    String systemProperty = System.getProperty(key);
    if (systemProperty != null && !systemProperty.isBlank()) {
      return systemProperty;
    }

    String envKey = key.replaceAll("([a-z0-9])([A-Z])", "$1_$2")
        .toUpperCase()
        .replace('.', '_');
    String environmentVariable = System.getenv(envKey);
    if (environmentVariable != null) {
      return environmentVariable;
    }

    return defaultValue;
  }
}
