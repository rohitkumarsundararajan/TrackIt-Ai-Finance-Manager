package com.trackit.service;

import com.trackit.model.Category;
import com.trackit.model.DashboardSummary;
import com.trackit.model.Expense;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for calculating dashboard-related aggregates.
 * <p>
 * Uses in-memory data from {@link ExpenseService} to compute monthly
 * spending summaries and per-category breakdowns.
 * </p>
 */
public class DashboardService {

    private static final int PERCENT_SCALE = 2;

    private final ExpenseService expenseService;

    /**
     * Creates a new {@link DashboardService}.
     *
     * @param expenseService service providing access to expenses
     */
    public DashboardService(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    /**
     * Calculates a dashboard summary for the given month and year.
     *
     * @param month month number (1-12)
     * @param year  four-digit year
     * @return {@link DashboardSummary} with aggregated values
     */
    public DashboardSummary getDashboardSummary(int month, int year) {
        List<Expense> expenses = expenseService.getExpensesByMonth(month, year);

        if (expenses.isEmpty()) {
            return createEmptySummary();
        }

        BigDecimal totalSpending = BigDecimal.ZERO;
        int totalTransactions = expenses.size();

        Map<String, BigDecimal> categoryTotals = new HashMap<>();

        for (Expense expense : expenses) {
            BigDecimal amount = expense.getAmount();
            totalSpending = totalSpending.add(amount);

            Category category = expense.getCategory();
            String categoryName = category.getName();

            BigDecimal currentTotal = categoryTotals.getOrDefault(categoryName, BigDecimal.ZERO);
            categoryTotals.put(categoryName, currentTotal.add(amount));
        }

        Map<String, Double> categoryPercentages = calculatePercentages(totalSpending, categoryTotals);
        String topCategory = findTopCategory(categoryTotals);

        return new DashboardSummary(
                totalSpending,
                totalTransactions,
                topCategory,
                categoryTotals,
                categoryPercentages
        );
    }

    private DashboardSummary createEmptySummary() {
        return new DashboardSummary(
                BigDecimal.ZERO,
                0,
                null,
                new HashMap<>(),
                new HashMap<>()
        );
    }

    private Map<String, Double> calculatePercentages(BigDecimal totalSpending,
                                                     Map<String, BigDecimal> categoryTotals) {
        Map<String, Double> percentages = new HashMap<>();

        if (totalSpending.compareTo(BigDecimal.ZERO) == 0) {
            return percentages;
        }

        for (Map.Entry<String, BigDecimal> entry : categoryTotals.entrySet()) {
            String categoryName = entry.getKey();
            BigDecimal categoryAmount = entry.getValue();

            BigDecimal percentage = categoryAmount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalSpending, PERCENT_SCALE, RoundingMode.HALF_UP);

            percentages.put(categoryName, percentage.doubleValue());
        }

        return percentages;
    }

    private String findTopCategory(Map<String, BigDecimal> categoryTotals) {
        String topCategory = null;
        BigDecimal maxAmount = BigDecimal.ZERO;

        for (Map.Entry<String, BigDecimal> entry : categoryTotals.entrySet()) {
            String categoryName = entry.getKey();
            BigDecimal amount = entry.getValue();

            if (amount.compareTo(maxAmount) > 0) {
                maxAmount = amount;
                topCategory = categoryName;
            }
        }

        return topCategory;
    }
}


