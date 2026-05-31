package com.trackit.service;

import com.trackit.model.Budget;
import com.trackit.model.BudgetSummary;
import com.trackit.model.Expense;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for managing monthly budgets and their usage calculations.
 * <p>
 * Uses in-memory storage for budgets and {@link ExpenseService} for
 * expense totals.
 * </p>
 *
 * <p>
 * This service does not depend on Swing or any UI framework.
 * </p>
 */
public class BudgetService {

    private static final int PERCENT_SCALE = 2;

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private static final double WARNING_THRESHOLD = 80.0;
    private static final double EXCEEDED_THRESHOLD = 100.0;

    private final Map<YearMonth, Budget> budgets = new HashMap<>();
    private final ExpenseService expenseService;

    /**
     * Creates a new {@link BudgetService}.
     *
     * @param expenseService service providing access to expenses
     */
    public BudgetService(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    /**
     * Saves or updates a budget for the given month and year.
     *
     * @param month       month number (1-12)
     * @param year        four-digit year
     * @param limitAmount positive budget limit amount
     */
    public void saveBudget(int month, int year, BigDecimal limitAmount) {
        YearMonth period = YearMonth.of(year, month);
        Budget budget = new Budget(period, limitAmount);
        budgets.put(period, budget);
    }

    /**
     * Returns the budget for the given month and year, or {@code null}
     * if no budget is set.
     *
     * @param month month number (1-12)
     * @param year  four-digit year
     * @return {@link Budget} or {@code null}
     */
    public Budget getBudget(int month, int year) {
        YearMonth period = YearMonth.of(year, month);
        return budgets.get(period);
    }

    /**
     * Calculates a budget summary for the given month and year.
     *
     * <ul>
     *     <li>If no budget is set, it is treated as unlimited and usage is 0%.</li>
     *     <li>If no expenses exist, usage is 0%.</li>
     *     <li>Division-by-zero is protected by validating a positive limit.</li>
     * </ul>
     *
     * @param month month number (1-12)
     * @param year  four-digit year
     * @return {@link BudgetSummary} instance
     */
    public BudgetSummary calculateUsage(int month, int year) {
        List<Expense> expenses = expenseService.getExpensesByMonth(month, year);

        BigDecimal totalSpending = BigDecimal.ZERO;
        for (Expense expense : expenses) {
            totalSpending = totalSpending.add(expense.getAmount());
        }

        Budget budget = getBudget(month, year);
        if (budget == null) {
            return new BudgetSummary(
                    totalSpending,
                    null,
                    null,
                    0.0,
                    false,
                    false
            );
        }

        BigDecimal limit = budget.getLimitAmount();
        if (limit.compareTo(BigDecimal.ZERO) <= 0) {
            return new BudgetSummary(
                    totalSpending,
                    limit,
                    limit,
                    0.0,
                    false,
                    false
            );
        }

        BigDecimal remaining = limit.subtract(totalSpending);

        BigDecimal percent = totalSpending
                .multiply(ONE_HUNDRED)
                .divide(limit, PERCENT_SCALE, RoundingMode.HALF_UP);

        double usagePercentage = percent.doubleValue();
        boolean warning = usagePercentage >= WARNING_THRESHOLD && usagePercentage < EXCEEDED_THRESHOLD;
        boolean exceeded = usagePercentage >= EXCEEDED_THRESHOLD;

        return new BudgetSummary(
                totalSpending,
                limit,
                remaining,
                usagePercentage,
                warning,
                exceeded
        );
    }
}


