package com.trackit.service;

import com.trackit.model.BudgetSummary;
import com.trackit.model.CategoryMonthTotal;
import com.trackit.model.Expense;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Uses the same in-memory budget and expenses as the Budget screen ({@link BudgetService} /
 * {@link ExpenseService}): totals and category breakdown come from the app, not MySQL.
 * Calls {@link AIService} only when the monthly budget is exceeded.
 */
public class FinancialAdviceService {

    private static final int PERCENT_SCALE = 2;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal ONE = BigDecimal.ONE;

    private final BudgetService budgetService;
    private final ExpenseService expenseService;
    private final AIService aiService;

    public FinancialAdviceService(BudgetService budgetService, ExpenseService expenseService, AIService aiService) {
        this.budgetService = budgetService;
        this.expenseService = expenseService;
        this.aiService = aiService;
    }

    /**
     * When the in-app budget for the month is exceeded, builds a summary and returns AI advice.
     * Does not call the AI API when within limit or when no budget is set for that month.
     *
     * @param month month 1-12
     * @param year  four-digit year
     * @return empty if no advice applies; otherwise advice or a user-facing error message
     */
    public Optional<String> getAdviceWhenLimitExceeded(int month, int year) {
        BudgetSummary summary = budgetService.calculateUsage(month, year);
        if (summary.getBudgetLimit() == null) {
            return Optional.empty();
        }
        if (!summary.isExceeded()) {
            return Optional.empty();
        }

        BigDecimal monthlyLimit = summary.getBudgetLimit();
        BigDecimal totalSpent = summary.getTotalSpending();
        if (totalSpent.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        List<CategoryMonthTotal> byCategory = buildCategoryTotalsFromExpenses(month, year);
        String summaryText = buildSummary(monthlyLimit, totalSpent, byCategory);
        String aiAdvice = aiService.generateAdvice(summaryText);
        if (looksLikeAiFailure(aiAdvice)) {
            return Optional.of(buildBackupAdvice(monthlyLimit, totalSpent, byCategory));
        }
        return Optional.of(aiAdvice);
    }

    private List<CategoryMonthTotal> buildCategoryTotalsFromExpenses(int month, int year) {
        List<Expense> expenses = expenseService.getExpensesByMonth(month, year);
        Map<String, BigDecimal> totals = new HashMap<>();
        for (Expense expense : expenses) {
            String name = expense.getCategory().getName();
            totals.merge(name, expense.getAmount(), BigDecimal::add);
        }
        List<Map.Entry<String, BigDecimal>> entries = new ArrayList<>(totals.entrySet());
        entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        return entries.stream()
                .map(e -> new CategoryMonthTotal(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private boolean looksLikeAiFailure(String adviceText) {
        if (adviceText == null || adviceText.isBlank()) {
            return true;
        }
        String t = adviceText.trim();
        return t.startsWith("AI advice is unavailable:")
                || t.startsWith("Could not get AI advice")
                || t.startsWith("Could not reach the AI service")
                || t.startsWith("Could not parse AI response")
                || t.startsWith("OpenRouter returned an error:");
    }

    private String buildSummary(BigDecimal monthlyLimit, BigDecimal totalSpent, List<CategoryMonthTotal> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("Monthly limit: ").append(monthlyLimit.stripTrailingZeros().toPlainString()).append('\n');
        sb.append("Total spent: ").append(totalSpent.stripTrailingZeros().toPlainString()).append("\n\n");
        sb.append("Category breakdown:\n");

        for (CategoryMonthTotal row : rows) {
            BigDecimal pct = row.getTotal()
                    .multiply(ONE_HUNDRED)
                    .divide(totalSpent, PERCENT_SCALE, RoundingMode.HALF_UP);
            sb.append(row.getCategory())
                    .append(": ")
                    .append(row.getTotal().stripTrailingZeros().toPlainString())
                    .append(" (")
                    .append(pct.stripTrailingZeros().toPlainString())
                    .append("%)\n");
        }

        String top = rows.isEmpty() ? "N/A" : rows.get(0).getCategory();
        sb.append("\nTop spending category: ").append(top).append("\n\n");
        sb.append("Give a short financial advice.");
        return sb.toString();
    }

    /**
     * Deterministic backup advice used when the AI call fails.
     * Calculations are based on the Java-computed totals (no AI math).
     */
    private String buildBackupAdvice(BigDecimal monthlyLimit, BigDecimal totalSpent, List<CategoryMonthTotal> rows) {
        BigDecimal overspent = totalSpent.subtract(monthlyLimit);
        BigDecimal overspentPct = overspent
                .multiply(ONE_HUNDRED)
                .divide(monthlyLimit, PERCENT_SCALE, RoundingMode.HALF_UP);

        if (rows == null || rows.isEmpty()) {
            return "You exceeded your monthly limit by "
                    + overspent.stripTrailingZeros().toPlainString()
                    + " ("
                    + overspentPct.stripTrailingZeros().toPlainString()
                    + "%). Review your transactions and reduce non-essential spending next month.";
        }

        CategoryMonthTotal top = rows.get(0);
        BigDecimal topPct = top.getTotal()
                .multiply(ONE_HUNDRED)
                .divide(totalSpent.max(ONE), PERCENT_SCALE, RoundingMode.HALF_UP);

        String topCategory = top.getCategory() == null ? "" : top.getCategory().trim();
        return buildCategorySpecificBackupAdvice(topCategory, overspent, overspentPct, topPct);
    }

    private String buildCategorySpecificBackupAdvice(String topCategory,
                                                     BigDecimal overspent,
                                                     BigDecimal overspentPct,
                                                     BigDecimal topPct) {
        String overspentText = overspent.stripTrailingZeros().toPlainString();
        String overspentPctText = overspentPct.stripTrailingZeros().toPlainString();
        String topPctText = topPct.stripTrailingZeros().toPlainString();

        String normalized = topCategory == null ? "" : topCategory.toLowerCase(Locale.ROOT).trim();
        BackupAdviceTemplate[] templates;
        if (normalized.contains("food")) {
            templates = BackupAdviceTemplate.FOOD;
        } else if (normalized.contains("travel") || normalized.contains("transport")) {
            templates = BackupAdviceTemplate.TRAVEL;
        } else if (normalized.contains("shop")) {
            templates = BackupAdviceTemplate.SHOPPING;
        } else {
            templates = BackupAdviceTemplate.GENERIC;
        }

        BackupAdviceTemplate chosen = templates[ThreadLocalRandom.current().nextInt(templates.length)];
        return chosen.render(topCategory, overspentText, overspentPctText, topPctText);
    }

    /**
     * Small template set to keep advice fresh without relying on AI.
     */
    private enum BackupAdviceTemplate {
        // Food templates
        FOOD_1(
                "You’re over budget by %s (%s%%). Food is your biggest spend (%s%% of total).",
                "This week: plan 3–4 home meals and limit eating out to 1–2 times.",
                "Quick win: set a daily food cap and track it after each purchase."
        ),
        FOOD_2(
                "You crossed your monthly limit by %s (%s%%). Food spending is leading (%s%%).",
                "Try meal-prepping on weekends and carry snacks to avoid impulse buys.",
                "Reduce delivery frequency and switch to budget-friendly staples for the rest of the month."
        ),
        FOOD_3(
                "Budget exceeded by %s (%s%%). Most of your spend is in Food (%s%%).",
                "Pick one change: cook dinner at home on weekdays or stop ordering beverages/desserts outside.",
                "Keep a simple grocery list and avoid shopping when hungry."
        ),

        // Travel templates
        TRAVEL_1(
                "You’re over the limit by %s (%s%%). Travel/transport is highest (%s%% of total).",
                "Consider batching errands, using public transport, or carpooling where possible.",
                "Set a weekly travel cap and review commute/ride-hailing expenses mid-week."
        ),
        TRAVEL_2(
                "Monthly limit exceeded by %s (%s%%). Travel is your top category (%s%%).",
                "Cut 1–2 discretionary trips and prefer off-peak or shared rides when needed.",
                "If you commute daily, compare monthly pass vs pay-per-ride to reduce cost."
        ),
        TRAVEL_3(
                "You overspent by %s (%s%%). The biggest share is Travel (%s%%).",
                "Aim for fewer high-cost rides: walk short distances and combine multiple stops in one trip.",
                "For the remaining days, track each trip and ask: “Is this necessary?” before booking."
        ),

        // Shopping templates
        SHOPPING_1(
                "You exceeded your limit by %s (%s%%). Shopping is the biggest spend (%s%% of total).",
                "Pause non-essential purchases for 7 days and keep a wishlist instead of buying instantly.",
                "Set a strict ‘fun spend’ cap and only buy items that still feel necessary after a day."
        ),
        SHOPPING_2(
                "Over budget by %s (%s%%). Shopping is leading your spending (%s%%).",
                "Try the 24-hour rule for online purchases and remove saved cards to reduce impulse buys.",
                "Return/avoid duplicate items and focus on essentials for the rest of the month."
        ),
        SHOPPING_3(
                "Budget crossed by %s (%s%%). Highest category: Shopping (%s%%).",
                "Plan your remaining purchases: essentials first, then stop when the cap is reached.",
                "Unsubscribe from promo alerts and avoid browsing shopping apps when bored."
        ),

        // Generic templates
        GENERIC_1(
                "You exceeded your monthly limit by %s (%s%%). Your top category is %s (%s%% of total).",
                "Set a smaller cap for %s and pause non-essential spending for the next few days.",
                "Track every expense for a week to spot leaks and adjust your monthly limit realistically."
        ),
        GENERIC_2(
                "You’re over budget by %s (%s%%). Highest spending is in %s (%s%%).",
                "Pick one concrete cut in %s (reduce frequency or choose cheaper options).",
                "Also set a daily spending cap for the rest of the month to prevent further overshoot."
        ),
        GENERIC_3(
                "Monthly limit exceeded by %s (%s%%). Biggest contributor: %s (%s%% of total).",
                "Split the remaining month into weekly mini-budgets and stop spending when a week’s cap is hit.",
                "Review subscriptions/recurring costs and cancel anything you didn’t use this month."
        );

        static final BackupAdviceTemplate[] FOOD = {FOOD_1, FOOD_2, FOOD_3};
        static final BackupAdviceTemplate[] TRAVEL = {TRAVEL_1, TRAVEL_2, TRAVEL_3};
        static final BackupAdviceTemplate[] SHOPPING = {SHOPPING_1, SHOPPING_2, SHOPPING_3};
        static final BackupAdviceTemplate[] GENERIC = {GENERIC_1, GENERIC_2, GENERIC_3};

        private final String line1;
        private final String line2;
        private final String line3;

        BackupAdviceTemplate(String line1, String line2, String line3) {
            this.line1 = line1;
            this.line2 = line2;
            this.line3 = line3;
        }

        String render(String topCategory, String overspent, String overspentPct, String topPct) {
            String safeTop = (topCategory == null || topCategory.isBlank()) ? "your top category" : topCategory.trim();
            String l1 = formatLine(line1, overspent, overspentPct, safeTop, topPct);
            String l2 = formatLine(line2, safeTop, overspent, overspentPct, topPct);
            String l3 = formatLine(line3, safeTop, overspent, overspentPct, topPct);
            return l1 + "\n" + l2 + "\n" + l3;
        }

        private String formatLine(String template,
                                  String overspent,
                                  String overspentPct,
                                  String topCategory,
                                  String topPct) {
            // We keep formatting flexible: templates can refer to %s multiple times.
            // Order used across templates: overspent, overspentPct, topCategory, topPct, topCategory (again).
            try {
                return String.format(template, overspent, overspentPct, topCategory, topPct, topCategory);
            } catch (RuntimeException ex) {
                // Safety fallback: return raw template if formatting ever mismatches.
                return template;
            }
        }
    }
}
