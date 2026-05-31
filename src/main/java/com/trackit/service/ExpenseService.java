package com.trackit.service;

import com.trackit.model.Category;
import com.trackit.model.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for business operations related to expenses.
 * <p>
 * This service encapsulates validation, creation, and retrieval of
 * expenses using in-memory storage only.
 * </p>
 *
 * <p>
 * This service does not depend on Swing or any UI framework.
 * </p>
 */
public class ExpenseService {

    private static final String ERROR_TITLE_REQUIRED = "Title must not be empty.";
    private static final String ERROR_CATEGORY_REQUIRED = "Category must be selected.";
    private static final String ERROR_AMOUNT_REQUIRED = "Amount must not be empty.";
    private static final String ERROR_AMOUNT_INVALID = "Amount must be a valid decimal number.";
    private static final String ERROR_AMOUNT_POSITIVE = "Amount must be greater than zero.";
    private static final String ERROR_DATE_REQUIRED = "Date must not be empty.";
    private static final String ERROR_DATE_FORMAT = "Date must be in format yyyy-MM-dd.";
    private static final String ERROR_DATE_FUTURE = "Date cannot be in the future.";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final List<Expense> expenses = new ArrayList<>();

    /**
     * Adds a new expense after validating input data.
     *
     * @param title       raw title text
     * @param category    selected category
     * @param description raw description text (optional)
     * @param amountText  raw amount text
     * @param dateText    raw date text in yyyy-MM-dd format
     * @return created {@link Expense}
     * @throws IllegalArgumentException if validation fails
     */
    public Expense addExpense(String title,
                              Category category,
                              String description,
                              String amountText,
                              String dateText) {

        String normalizedTitle = normalize(title);
        String normalizedDescription = normalizeNullable(description);
        String normalizedAmountText = normalize(amountText);
        String normalizedDateText = normalize(dateText);

        validateTitle(normalizedTitle);
        validateCategory(category);
        BigDecimal amount = parseAndValidateAmount(normalizedAmountText);
        LocalDate date = parseAndValidateDate(normalizedDateText);

        Expense expense = new Expense(
                UUID.randomUUID(),
                normalizedTitle,
                category,
                normalizedDescription,
                amount,
                date
        );
        expenses.add(expense);
        return expense;
    }

    /**
     * Returns an immutable copy of all expenses.
     *
     * @return list of {@link Expense} objects
     */
    public List<Expense> getAllExpenses() {
        return Collections.unmodifiableList(new ArrayList<>(expenses));
    }

    /**
     * Returns a list of expenses for a specific month and year.
     *
     * @param month month number (1-12)
     * @param year  four-digit year
     * @return list of {@link Expense} objects that occurred in the given month and year
     */
    public List<Expense> getExpensesByMonth(int month, int year) {
        List<Expense> result = new ArrayList<>();
        for (Expense expense : expenses) {
            LocalDate date = expense.getDate();
            if (date.getYear() == year && date.getMonthValue() == month) {
                result.add(expense);
            }
        }
        return Collections.unmodifiableList(result);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private void validateTitle(String title) {
        if (title.isEmpty()) {
            throw new IllegalArgumentException(ERROR_TITLE_REQUIRED);
        }
    }

    private void validateCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException(ERROR_CATEGORY_REQUIRED);
        }
    }

    private BigDecimal parseAndValidateAmount(String amountText) {
        if (amountText.isEmpty()) {
            throw new IllegalArgumentException(ERROR_AMOUNT_REQUIRED);
        }

        try {
            BigDecimal amount = new BigDecimal(amountText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(ERROR_AMOUNT_POSITIVE);
            }
            return amount;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(ERROR_AMOUNT_INVALID);
        }
    }

    private LocalDate parseAndValidateDate(String dateText) {
        if (dateText.isEmpty()) {
            throw new IllegalArgumentException(ERROR_DATE_REQUIRED);
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateText, DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(ERROR_DATE_FORMAT);
        }

        LocalDate today = LocalDate.now();
        if (date.isAfter(today)) {
            throw new IllegalArgumentException(ERROR_DATE_FUTURE);
        }
        return date;
    }
}


