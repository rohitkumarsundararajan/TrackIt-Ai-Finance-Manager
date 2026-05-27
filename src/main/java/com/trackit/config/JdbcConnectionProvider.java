package com.trackit.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides JDBC connections using runtime-resolved DB configuration.
 * <p>
 * Resolution is handled by {@link DBConfig}: env vars first, then console prompts for missing values.
 * </p>
 */
public final class JdbcConnectionProvider {

    private JdbcConnectionProvider() {
    }

    /**
     * Opens a new connection to the finance database.
     *
     * @return JDBC connection (caller must close)
     * @throws SQLException if connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        try {
            DBConfig.DBDetails config = DBConfig.resolve();
            Connection connection = DriverManager.getConnection(
                    config.getUrl(), config.getUsername(), config.getPassword());
            DatabaseSchemaBootstrap.ensureSchema(connection);
            return connection;
        } catch (IllegalArgumentException ex) {
            throw new SQLException("Invalid database configuration: " + ex.getMessage(), ex);
        }
    }
}
