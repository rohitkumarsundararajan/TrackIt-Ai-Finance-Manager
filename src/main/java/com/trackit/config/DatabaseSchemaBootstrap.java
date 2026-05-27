package com.trackit.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates required MySQL tables if they do not exist ({@code CREATE TABLE IF NOT EXISTS}).
 * <p>
 * Expected database name is whatever appears in the JDBC URL (e.g. {@code finance_manager}).
 * </p>
 */
public final class DatabaseSchemaBootstrap {

    private static final Object LOCK = new Object();
    private static volatile boolean ensured;

    private static final String CREATE_MONTHLY_LIMIT = """
            CREATE TABLE IF NOT EXISTS monthly_limit (
                id INT NOT NULL AUTO_INCREMENT,
                limit_amount DOUBLE NOT NULL,
                PRIMARY KEY (id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;

    private static final String CREATE_EXPENSES = """
            CREATE TABLE IF NOT EXISTS expenses (
                id INT NOT NULL AUTO_INCREMENT,
                amount DOUBLE NOT NULL,
                category VARCHAR(255) NOT NULL,
                date DATE NOT NULL,
                PRIMARY KEY (id),
                KEY idx_expenses_date (date)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;

    private DatabaseSchemaBootstrap() {
    }

    /**
     * Ensures {@code monthly_limit} and {@code expenses} exist. Safe to call repeatedly; runs DDL at most once per JVM.
     *
     * @param connection open JDBC connection (not closed by this method)
     */
    public static void ensureSchema(Connection connection) throws SQLException {
        if (ensured) {
            return;
        }
        synchronized (LOCK) {
            if (ensured) {
                return;
            }
            try (Statement statement = connection.createStatement()) {
                statement.execute(CREATE_MONTHLY_LIMIT.trim());
                statement.execute(CREATE_EXPENSES.trim());
            }
            ensured = true;
        }
    }
}
