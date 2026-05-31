package com.trackit.service;

import com.trackit.model.AnalyticsSummary;
import com.trackit.model.BudgetSummary;
import com.trackit.model.Expense;

import java.math.BigDecimal;
import java.time.Month;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for computing analytics aggregates used by charts.
 * <p>
 * Uses in-memory data from {@link ExpenseService} and {@link BudgetService}
 * to build category and monthly totals and budget usage.
 * </p>
 *
 * <p>
 * This service does not depend on Swing or any UI framework.
 * </p>
 */
public class AnalyticsService {

    private final ExpenseService expenseService;
    private final BudgetService budgetService;

    /**
     * Creates a new {@link AnalyticsService}.
     *
     * @param expenseService service providing access to expenses
     * @param budgetService  service providing access to budget usage
     */
    public AnalyticsService(ExpenseService expenseService, BudgetService budgetService) {
        this.expenseService = expenseService;
        this.budgetService = budgetService;
    }

    /**
     * Builds an {@link AnalyticsSummary} for the given month and year.
     *
     * @param month month number (1-12)
     * @param year  four-digit year
     * @return analytics summary
     */
    public AnalyticsSummary buildSummary(int month, int year) {
        Map<String, BigDecimal> categoryTotals = calculateCategoryTotals(month, year);
        Map<Month, BigDecimal> monthlyTotals = calculateMonthlyTotals(year);

        boolean hasData = hasAnyData(categoryTotals, monthlyTotals);

        double budgetUsagePercentage = 0.0;
        BudgetSummary budgetSummary = budgetService.calculateUsage(month, year);
        if (budgetSummary.getBudgetLimit() != null) {
            budgetUsagePercentage = budgetSummary.getUsagePercentage();
        }

        return new AnalyticsSummary(categoryTotals, monthlyTotals, budgetUsagePercentage, hasData);
    }

    private Map<String, BigDecimal> calculateCategoryTotals(int month, int year) {
        List<Expense> expenses = expenseService.getExpensesByMonth(month, year);
        Map<String, BigDecimal> totals = new HashMap<>();

        for (Expense expense : expenses) {
            String categoryName = expense.getCategory().getName();
            BigDecimal amount = expense.getAmount();
            totals.merge(categoryName, amount, BigDecimal::add);
        }
        return totals;
    }

    private Map<Month, BigDecimal> calculateMonthlyTotals(int year) {
        Map<Month, BigDecimal> totals = new EnumMap<>(Month.class);

        for (Month month : Month.values()) {
            List<Expense> monthExpenses = expenseService.getExpensesByMonth(month.getValue(), year);
            BigDecimal monthTotal = BigDecimal.ZERO;

            for (Expense expense : monthExpenses) {
                monthTotal = monthTotal.add(expense.getAmount());
            }

            totals.put(month, monthTotal);
        }

        return totals;
    }

    private boolean hasAnyData(Map<String, BigDecimal> categoryTotals,
                               Map<Month, BigDecimal> monthlyTotals) {
        for (BigDecimal amount : categoryTotals.values()) {
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                return true;
            }
        }
        for (BigDecimal amount : monthlyTotals.values()) {
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                return true;
            }
        }
        return false;
    }
}


