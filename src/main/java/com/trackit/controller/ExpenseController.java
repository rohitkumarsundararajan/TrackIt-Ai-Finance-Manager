package com.trackit.controller;

import com.trackit.model.BudgetSummary;
import com.trackit.model.Category;
import com.trackit.model.Expense;
import com.trackit.service.BudgetService;
import com.trackit.service.ExpenseService;

import java.time.LocalDate;

/**
 * Controller responsible for handling user interactions related to expenses.
 * <p>
 * Coordinates requests between UI components and the {@link ExpenseService}.
 * The controller does not implement validation logic; validation is delegated
 * to the service layer.
 * </p>
 *
 * <p>
 * After a successful expense creation, this controller checks the monthly
 * budget usage via {@link BudgetService} to support threshold alerts.
 * </p>
 */
public class ExpenseController {

    private final ExpenseService expenseService;
    private final BudgetService budgetService;

    /**
     * Creates a new instance of {@link ExpenseController}.
     *
     * @param expenseService service providing expense-related operations
     * @param budgetService  service providing budget-related operations
     */
    public ExpenseController(ExpenseService expenseService, BudgetService budgetService) {
        this.expenseService = expenseService;
        this.budgetService = budgetService;
    }

    /**
     * Adds a new expense with the provided data.
     *
     * @param title       raw title text
     * @param category    selected category
     * @param description raw description text (optional)
     * @param amountText  raw amount text
     * @param dateText    raw date text
     * @return result describing the outcome including whether the budget is exceeded
     * @throws IllegalArgumentException if validation fails in the service layer
     */
    public ExpenseAddResult addExpense(String title,
                                       Category category,
                                       String description,
                                       String amountText,
                                       String dateText) {
        Expense expense = expenseService.addExpense(title, category, description, amountText, dateText);

        LocalDate date = expense.getDate();
        BudgetSummary summary = budgetService.calculateUsage(date.getMonthValue(), date.getYear());

        return new ExpenseAddResult(expense, summary.isExceeded());
    }

    /**
     * Result object for add-expense operations.
     * <p>
     * This keeps Swing concerns out of the controller while allowing the UI
     * to decide how to present alerts.
     * </p>
     */
    public static final class ExpenseAddResult {
        private final Expense expense;
        private final boolean budgetExceeded;

        /**
         * Creates a new {@link ExpenseAddResult}.
         *
         * @param expense         created expense
         * @param budgetExceeded  whether the budget was exceeded after this expense
         */
        public ExpenseAddResult(Expense expense, boolean budgetExceeded) {
            this.expense = expense;
            this.budgetExceeded = budgetExceeded;
        }

        /**
         * Returns the created expense.
         *
         * @return expense
         */
        public Expense getExpense() {
            return expense;
        }

        /**
         * Indicates whether the monthly budget was exceeded.
         *
         * @return {@code true} if exceeded
         */
        public boolean isBudgetExceeded() {
            return budgetExceeded;
        }
    }
}


