package com.trackit.model;

import java.math.BigDecimal;
import java.time.Month;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Value object representing analytics aggregates for a given selection.
 * <p>
 * Contains per-category totals, per-month totals for a year, and
 * budget usage information.
 * </p>
 */
public final class AnalyticsSummary {

    private final Map<String, BigDecimal> categoryTotals;
    private final Map<Month, BigDecimal> monthlyTotals;
    private final double budgetUsagePercentage;
    private final boolean hasData;

    /**
     * Creates a new {@link AnalyticsSummary} instance.
     *
     * @param categoryTotals        map of category name to total amount
     * @param monthlyTotals         map of month to total amount
     * @param budgetUsagePercentage budget usage percentage for the selected month
     * @param hasData               whether any data is available
     */
    public AnalyticsSummary(Map<String, BigDecimal> categoryTotals,
                            Map<Month, BigDecimal> monthlyTotals,
                            double budgetUsagePercentage,
                            boolean hasData) {
        this.categoryTotals = Collections.unmodifiableMap(
                Objects.requireNonNull(categoryTotals, "categoryTotals must not be null"));
        this.monthlyTotals = Collections.unmodifiableMap(
                Objects.requireNonNull(monthlyTotals, "monthlyTotals must not be null"));
        this.budgetUsagePercentage = budgetUsagePercentage;
        this.hasData = hasData;
    }

    /**
     * Returns an immutable map of category totals for the selected month.
     *
     * @return category totals
     */
    public Map<String, BigDecimal> getCategoryTotals() {
        return categoryTotals;
    }

    /**
     * Returns an immutable map of monthly totals for the selected year.
     *
     * @return monthly totals
     */
    public Map<Month, BigDecimal> getMonthlyTotals() {
        return monthlyTotals;
    }

    /**
     * Returns the budget usage percentage for the selected month.
     *
     * @return budget usage percentage
     */
    public double getBudgetUsagePercentage() {
        return budgetUsagePercentage;
    }

    /**
     * Indicates whether any analytics data is available.
     *
     * @return {@code true} if data exists; {@code false} otherwise
     */
    public boolean hasData() {
        return hasData;
    }
}


