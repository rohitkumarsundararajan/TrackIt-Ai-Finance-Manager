package com.trackit.repository;

import com.trackit.config.JdbcConnectionProvider;
import com.trackit.model.CategoryMonthTotal;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC access for {@code expenses} and {@code monthly_limit} tables.
 */
public class ExpenseRepository {

    private static final String SQL_SUM_BY_CATEGORY = """
            SELECT category, SUM(amount) AS total
            FROM expenses
            WHERE MONTH(date) = ? AND YEAR(date) = ?
            GROUP BY category
            ORDER BY total DESC
            """;

    private static final String SQL_SUM_MONTH = """
            SELECT COALESCE(SUM(amount), 0) AS total
            FROM expenses
            WHERE MONTH(date) = ? AND YEAR(date) = ?
            """;

    private static final String SQL_MONTHLY_LIMIT = """
            SELECT limit_amount
            FROM monthly_limit
            ORDER BY id DESC
            LIMIT 1
            """;

    /**
     * Category-wise totals for the given calendar month (newest categories with highest spend first).
     *
     * @param month month 1-12
     * @param year  four-digit year
     */
    public List<CategoryMonthTotal> findCategoryTotalsForMonth(int month, int year) throws SQLException {
        try (Connection connection = JdbcConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SUM_BY_CATEGORY)) {
            statement.setInt(1, month);
            statement.setInt(2, year);
            try (ResultSet rs = statement.executeQuery()) {
                List<CategoryMonthTotal> rows = new ArrayList<>();
                while (rs.next()) {
                    String category = rs.getString("category");
                    BigDecimal total = BigDecimal.valueOf(rs.getDouble("total"));
                    rows.add(new CategoryMonthTotal(category, total));
                }
                return rows;
            }
        }
    }

    /**
     * Total spent in the given calendar month.
     */
    public BigDecimal sumTotalForMonth(int month, int year) throws SQLException {
        try (Connection connection = JdbcConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SUM_MONTH)) {
            statement.setInt(1, month);
            statement.setInt(2, year);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return BigDecimal.valueOf(rs.getDouble("total"));
                }
                return BigDecimal.ZERO;
            }
        }
    }

    /**
     * Latest configured monthly limit, if any row exists.
     */
    public Optional<BigDecimal> fetchMonthlyLimit() throws SQLException {
        try (Connection connection = JdbcConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_MONTHLY_LIMIT);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return Optional.of(BigDecimal.valueOf(rs.getDouble("limit_amount")));
            }
            return Optional.empty();
        }
    }
}
