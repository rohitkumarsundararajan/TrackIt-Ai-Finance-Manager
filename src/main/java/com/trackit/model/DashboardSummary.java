package com.trackit.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Value object representing a monthly dashboard summary.
 * <p>
 * This class contains aggregated information for a given month and year,
 * including totals and per-category breakdowns.
 * </p>
 */
public final class DashboardSummary {

    private final BigDecimal totalSpending;
    private final int totalTransactions;
    private final String topCategory;
    private final Map<String, BigDecimal> categoryTotals;
    private final Map<String, Double> categoryPercentages;

    /**
     * Creates a new {@link DashboardSummary} instance.
     *
     * @param totalSpending       total spending amount
     * @param totalTransactions   number of transactions
     * @param topCategory         name of the top spending category (may be {@code null})
     * @param categoryTotals      map of category name to total amount
     * @param categoryPercentages map of category name to percentage of total spending
     */
    public DashboardSummary(BigDecimal totalSpending,
                            int totalTransactions,
                            String topCategory,
                            Map<String, BigDecimal> categoryTotals,
                            Map<String, Double> categoryPercentages) {
        this.totalSpending = Objects.requireNonNull(totalSpending, "totalSpending must not be null");
        this.totalTransactions = totalTransactions;
        this.topCategory = topCategory;
        this.categoryTotals = Collections.unmodifiableMap(
                Objects.requireNonNull(categoryTotals, "categoryTotals must not be null"));
        this.categoryPercentages = Collections.unmodifiableMap(
                Objects.requireNonNull(categoryPercentages, "categoryPercentages must not be null"));
    }

    /**
     * Returns the total spending amount.
     *
     * @return total spending
     */
    public BigDecimal getTotalSpending() {
        return totalSpending;
    }

    /**
     * Returns the total number of transactions.
     *
     * @return transaction count
     */
    public int getTotalTransactions() {
        return totalTransactions;
    }

    /**
     * Returns the name of the top spending category, or {@code null}
     * if no expenses exist.
     *
     * @return top category name or {@code null}
     */
    public String getTopCategory() {
        return topCategory;
    }

    /**
     * Returns an immutable map of category totals.
     *
     * @return category totals by name
     */
    public Map<String, BigDecimal> getCategoryTotals() {
        return categoryTotals;
    }

    /**
     * Returns an immutable map of category percentages.
     *
     * @return category percentages by name
     */
    public Map<String, Double> getCategoryPercentages() {
        return categoryPercentages;
    }
}


