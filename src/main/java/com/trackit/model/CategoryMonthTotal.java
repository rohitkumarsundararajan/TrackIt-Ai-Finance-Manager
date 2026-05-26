package com.trackit.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Aggregated expense total for one category within a calendar month (JDBC row shape).
 */
public final class CategoryMonthTotal {

    private final String category;
    private final BigDecimal total;

    public CategoryMonthTotal(String category, BigDecimal total) {
        this.category = Objects.requireNonNull(category, "category must not be null");
        this.total = Objects.requireNonNull(total, "total must not be null");
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
