package io.student.rangiffler.data.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StreamUtils;

public final class Jdbc {
  private Jdbc() {
  }

  public static JdbcClient getJdbcClient(Databases db) {
    return JdbcClient.create(db.getDataSource());
  }

  public static void inTransaction(Databases db, Consumer<JdbcClient> action) {
    JdbcClient jdbcClient = getJdbcClient(db);
    TransactionTemplate transactionTemplate = new TransactionTemplate(db.getTransactionManager());
    transactionTemplate.execute(status -> {
      action.accept(jdbcClient);
      return null;
    });
  }

  public static String loadSql(String filePath) {
    ClassPathResource resource = new ClassPathResource(filePath);
    try (InputStream inputStream = resource.getInputStream()) {
      return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to load SQL resource: " + filePath, exception);
    }
  }

  public static void requireOneRow(int rows, String operation) {
    if (rows != 1) {
      throw new IllegalStateException("Expected one row for " + operation + " but got " + rows);
    }
  }
}
