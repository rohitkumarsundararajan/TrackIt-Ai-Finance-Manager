package com.trackit.controller;

import com.trackit.model.AnalyticsSummary;
import com.trackit.service.AnalyticsService;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import java.math.BigDecimal;
import java.time.Month;
import java.util.Map;

/**
 * Controller responsible for handling user interactions related to analytics.
 * <p>
 * Coordinates requests between UI components and the {@link AnalyticsService}
 * and prepares JFreeChart datasets for display.
 * </p>
 */
public class AnalyticsController {

    private static final String BAR_SERIES_KEY = "Spending";

    private final AnalyticsService analyticsService;

    /**
     * Creates a new {@link AnalyticsController}.
     *
     * @param analyticsService analytics service
     */
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Loads analytics aggregates for the selected month/year.
     *
     * @param month month number (1-12)
     * @param year  four-digit year
     * @return analytics summary
     */
    public AnalyticsSummary loadSummary(int month, int year) {
        return analyticsService.buildSummary(month, year);
    }

    /**
     * Builds a pie dataset representing category distribution for the selected month.
     *
     * @param summary analytics summary
     * @return pie dataset
     */
    public PieDataset<String> buildCategoryPieDataset(AnalyticsSummary summary) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        for (Map.Entry<String, BigDecimal> entry : summary.getCategoryTotals().entrySet()) {
            BigDecimal amount = entry.getValue();
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                dataset.setValue(entry.getKey(), amount);
            }
        }
        return dataset;
    }

    /**
     * Builds a dataset representing monthly totals for the selected year.
     *
     * @param summary analytics summary
     * @return category dataset for bar chart
     */
    public CategoryDataset buildMonthlyBarDataset(AnalyticsSummary summary) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<Month, BigDecimal> entry : summary.getMonthlyTotals().entrySet()) {
            dataset.addValue(entry.getValue(), BAR_SERIES_KEY, entry.getKey().name());
        }

        return dataset;
    }
}


