package com.trackit.controller;

import com.trackit.model.DashboardSummary;
import com.trackit.service.DashboardService;

import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Controller responsible for handling user interactions related to the dashboard.
 * <p>
 * Coordinates requests between UI components and the {@link DashboardService}
 * and prepares a read-only table model for presentation.
 * </p>
 */
public class DashboardController {

    private static final String[] TABLE_COLUMNS = {
            "Category Name",
            "Total Amount",
            "Percentage of Total"
    };

    private static final String NO_DATA_TEXT = "No data available";

    private static final int PERCENT_DECIMALS = 2;

    private final DashboardService dashboardService;

    /**
     * Creates a new instance of {@link DashboardController}.
     *
     * @param dashboardService service providing dashboard-related aggregates
     */
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Loads a dashboard summary for the given month and year.
     *
     * @param month month number (1-12)
     * @param year  four-digit year
     * @return summary instance
     */
    public DashboardSummary loadSummary(int month, int year) {
        return dashboardService.getDashboardSummary(month, year);
    }

    /**
     * Builds a table model representing category breakdown for a summary.
     *
     * @param summary dashboard summary
     * @return read-only table model
     */
    public DefaultTableModel buildCategoryBreakdownTableModel(DashboardSummary summary) {
        DefaultTableModel model = new DefaultTableModel(TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        if (summary.getTotalTransactions() == 0) {
            return model;
        }

        for (Map.Entry<String, BigDecimal> entry : summary.getCategoryTotals().entrySet()) {
            String categoryName = entry.getKey();
            BigDecimal totalAmount = entry.getValue();
            Double percentage = summary.getCategoryPercentages().getOrDefault(categoryName, 0.0);

            Object[] row = {
                    categoryName,
                    totalAmount,
                    formatPercentage(percentage)
            };
            model.addRow(row);
        }

        return model;
    }

    /**
     * Returns a UI-friendly "no data" message when there are no expenses.
     *
     * @param summary dashboard summary
     * @return no-data message or empty string
     */
    public String getNoDataMessage(DashboardSummary summary) {
        if (summary.getTotalTransactions() == 0) {
            return NO_DATA_TEXT;
        }
        return "";
    }

    private String formatPercentage(double percentage) {
        return String.format("%." + PERCENT_DECIMALS + "f%%", percentage);
    }
}


