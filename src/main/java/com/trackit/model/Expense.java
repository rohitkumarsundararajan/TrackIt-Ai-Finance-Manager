package com.trackit.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain model representing an expense in the TrackIT application.
 * <p>
 * Each expense has a unique identifier, title, associated category,
 * optional description, monetary amount, and a date.
 * </p>
 */
public final class Expense {

    private final UUID id;
    private final String title;
    private final Category category;
    private final String description;
    private final BigDecimal amount;
    private final LocalDate date;

    /**
     * Creates a new {@link Expense} instance.
     *
     * @param id          unique identifier of the expense
     * @param title       short title describing the expense
     * @param category    category associated with the expense
     * @param description optional free-text description
     * @param amount      monetary amount of the expense
     * @param date        date on which the expense occurred
     */
    public Expense(UUID id,
                   String title,
                   Category category,
                   String description,
                   BigDecimal amount,
                   LocalDate date) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.category = Objects.requireNonNull(category, "category must not be null");
        this.description = description == null ? "" : description;
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.date = Objects.requireNonNull(date, "date must not be null");
    }

    /**
     * Returns the unique identifier of the expense.
     *
     * @return expense identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the title of the expense.
     *
     * @return expense title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the category associated with the expense.
     *
     * @return expense category
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Returns the description of the expense.
     *
     * @return expense description (may be empty)
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the monetary amount of the expense.
     *
     * @return expense amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Returns the date of the expense.
     *
     * @return expense date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns a human-readable representation of the expense,
     * suitable for basic list display.
     *
     * @return string representation of the expense
     */
    @Override
    public String toString() {
        return title + " - " + amount + " (" + date + ")";
    }
}


