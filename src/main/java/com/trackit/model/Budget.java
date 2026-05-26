package com.trackit.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;

/**
 * Domain model representing a monthly budget in the TrackIT application.
 * <p>
 * Each budget is associated with a specific month and year and defines
 * a spending limit amount.
 * </p>
 */
public final class Budget {

    private final YearMonth period;
    private final BigDecimal limitAmount;

    /**
     * Creates a new {@link Budget} instance.
     *
     * @param period      month/year period of the budget
     * @param limitAmount budget limit amount
     */
    public Budget(YearMonth period, BigDecimal limitAmount) {
        this.period = Objects.requireNonNull(period, "period must not be null");
        this.limitAmount = Objects.requireNonNull(limitAmount, "limitAmount must not be null");
    }

    /**
     * Returns the budget period.
     *
     * @return {@link YearMonth} representing the budget period
     */
    public YearMonth getPeriod() {
        return period;
    }

    /**
     * Returns the budget limit amount.
     *
     * @return budget limit amount
     */
    public BigDecimal getLimitAmount() {
        return limitAmount;
    }
}


