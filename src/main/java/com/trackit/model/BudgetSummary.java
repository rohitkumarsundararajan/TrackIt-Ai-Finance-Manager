package com.trackit.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value object representing a monthly budget usage summary.
 * <p>
 * Contains total spending, budget limit, remaining amount, usage percentage,
 * and flags for warning/exceeded thresholds.
 * </p>
 */
public final class BudgetSummary {

    private final BigDecimal totalSpending;
    private final BigDecimal budgetLimit;
    private final BigDecimal remainingAmount;
    private final double usagePercentage;
    private final boolean warning;
    private final boolean exceeded;

    /**
     * Creates a new {@link BudgetSummary} instance.
     *
     * @param totalSpending   total spending amount
     * @param budgetLimit     budget limit (may be {@code null} when not set)
     * @param remainingAmount remaining amount (may be {@code null} when not set)
     * @param usagePercentage usage percentage (0-100+)
     * @param warning         whether usage has reached the warning threshold (>= 80%)
     * @param exceeded        whether usage has exceeded the limit (>= 100%)
     */
    public BudgetSummary(BigDecimal totalSpending,
                         BigDecimal budgetLimit,
                         BigDecimal remainingAmount,
                         double usagePercentage,
                         boolean warning,
                         boolean exceeded) {
        this.totalSpending = Objects.requireNonNull(totalSpending, "totalSpending must not be null");
        this.budgetLimit = budgetLimit;
        this.remainingAmount = remainingAmount;
        this.usagePercentage = usagePercentage;
        this.warning = warning;
        this.exceeded = exceeded;
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
     * Returns the budget limit, or {@code null} if no budget is set.
     *
     * @return budget limit or {@code null}
     */
    public BigDecimal getBudgetLimit() {
        return budgetLimit;
    }

    /**
     * Returns the remaining amount, or {@code null} if no budget is set.
     *
     * @return remaining amount or {@code null}
     */
    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    /**
     * Returns the usage percentage.
     *
     * @return usage percentage
     */
    public double getUsagePercentage() {
        return usagePercentage;
    }

    /**
     * Indicates whether the usage has reached the warning threshold (>= 80%).
     *
     * @return {@code true} if warning threshold reached
     */
    public boolean isWarning() {
        return warning;
    }

    /**
     * Indicates whether the budget has been exceeded (>= 100%).
     *
     * @return {@code true} if exceeded
     */
    public boolean isExceeded() {
        return exceeded;
    }
}


