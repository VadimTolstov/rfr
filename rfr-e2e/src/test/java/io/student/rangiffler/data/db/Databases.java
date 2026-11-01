package io.student.rangiffler.data.db;

import io.student.rangiffler.config.TestConfig;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

public enum Databases {
  AUTH(TestConfig.AUTH_DB_URL),
  API(TestConfig.API_DB_URL);

  private final DataSource dataSource;
  private final PlatformTransactionManager transactionManager;

  Databases(String url) {
    DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
    driverManagerDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
    driverManagerDataSource.setUrl(url);
    driverManagerDataSource.setUsername(TestConfig.DB_USERNAME);
    driverManagerDataSource.setPassword(TestConfig.DB_PASSWORD);
    dataSource = driverManagerDataSource;
    transactionManager = new DataSourceTransactionManager(dataSource);
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public PlatformTransactionManager getTransactionManager() {
    return transactionManager;
  }
}
