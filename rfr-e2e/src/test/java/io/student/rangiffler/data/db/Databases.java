package io.student.rangiffler.data.db;

import io.student.rangiffler.config.TestConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

public enum Databases {
  AUTH("rangiffler-auth"),
  API("rangiffler-api");

  private final String schema;
  private HikariDataSource dataSource;
  private DataSourceTransactionManager transactionManager;

  Databases(String schema) {
    this.schema = schema;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public PlatformTransactionManager getTransactionManager() {
    return transactionManager;
  }

  public String schema() {
    return schema;
  }

  public synchronized DataSource dataSource() {
    if (dataSource == null) {
      dataSource = new HikariDataSource();
      dataSource.setJdbcUrl(TestConfig.dbUrl(schema));
      dataSource.setUsername(TestConfig.dbUser());
      dataSource.setPassword(TestConfig.dbPassword());
      dataSource.setMaximumPoolSize(4);
      dataSource.setPoolName("rfr-e2e-" + schema);
    }
    return dataSource;
  }

  public synchronized PlatformTransactionManager transactionManager() {
    if (transactionManager == null) {
      transactionManager = new DataSourceTransactionManager(dataSource());
    }
    return transactionManager;
  }

  public synchronized void close() {
    if (dataSource != null) {
      dataSource.close();
      dataSource = null;
      transactionManager = null;
    }
  }
}
