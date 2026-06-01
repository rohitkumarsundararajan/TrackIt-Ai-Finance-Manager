package com.trackit.controller;

import com.trackit.model.BudgetSummary;
import com.trackit.service.BudgetService;

import java.math.BigDecimal;

/**
 * Controller responsible for handling user interactions related to budgets.
 * <p>
 * Coordinates requests between UI components and the {@link BudgetService},
 * while performing input validation for user-entered budget values.
 * </p>
 */
public class BudgetController {

    private static final String ERROR_BUDGET_REQUIRED = "Budget amount must not be empty.";
    private static final String ERROR_BUDGET_INVALID = "Budget amount must be a valid decimal number.";
    private static final String ERROR_BUDGET_POSITIVE = "Budget amount must be greater than zero.";

    private final BudgetService budgetService;

    /**
     * Creates a new instance of {@link BudgetController}.
     *
     * @param budgetService service providing budget-related operations
     */
    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    /**
     * Saves the budget for the given month and year.
     *
     * @param month        month number (1-12)
     * @param year         four-digit year
     * @param budgetAmount raw budget amount text
     * @throws IllegalArgumentException if validation fails
     */
    public void saveBudget(int month, int year, String budgetAmount) {
        String trimmed = budgetAmount == null ? "" : budgetAmount.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(ERROR_BUDGET_REQUIRED);
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(trimmed);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(ERROR_BUDGET_INVALID);
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(ERROR_BUDGET_POSITIVE);
        }

        budgetService.saveBudget(month, year, amount);
    }

    /**
     * Loads the budget usage summary for the given month and year.
     *
     * @param month month number (1-12)
     * @param year  four-digit year
     * @return budget usage summary
     */
    public BudgetSummary loadSummary(int month, int year) {
        return budgetService.calculateUsage(month, year);
    }
}


