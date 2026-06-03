package com.trackit.ui.frame;

import com.trackit.controller.AnalyticsController;
import com.trackit.model.AnalyticsSummary;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.NumberFormat;
import java.time.LocalDate;

/**
 * Swing frame for displaying analytics charts in TrackIT.
 * <p>
 * This frame renders charts and delegates data preparation to {@link AnalyticsController}.
 * No business calculations are performed in the UI.
 * </p>
 */
public class AnalyticsFrame extends JFrame {

    private static final int WINDOW_WIDTH = 920;
    private static final int WINDOW_HEIGHT = 620;

    private static final String WINDOW_TITLE = "TrackIT - Analytics";
    private static final String TITLE_LABEL_TEXT = "Analytics";

    private static final int TITLE_FONT_SIZE = 20;
    private static final int PADDING = 16;
    private static final int COMPONENT_SPACING = 8;

    private static final int SELECTOR_COMBO_WIDTH = 140;
    private static final int SELECTOR_COMBO_HEIGHT = 26;
    private static final int YEARS_RANGE = 5;

    private static final int PROGRESS_MIN = 0;
    private static final int PROGRESS_MAX = 100;
    private static final double WARNING_THRESHOLD = 80.0;
    private static final double EXCEEDED_THRESHOLD = 100.0;

    private final AnalyticsController analyticsController;

    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> yearComboBox;

    private ChartPanel pieChartPanel;
    private ChartPanel barChartPanel;
    private JProgressBar budgetProgressBar;
    private JLabel noDataLabel;

    /**
     * Creates a new {@link AnalyticsFrame}.
     *
     * @param analyticsController analytics controller
     */
    public AnalyticsFrame(AnalyticsController analyticsController) {
        this.analyticsController = analyticsController;

        initializeFrame();
        initializeContent();
        loadInitialSummary();
    }

    private void initializeFrame() {
        setTitle(WINDOW_TITLE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());
    }

    private void initializeContent() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        add(contentPanel, BorderLayout.CENTER);

        JLabel titleLabel = new JLabel(TITLE_LABEL_TEXT, JLabel.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, TITLE_FONT_SIZE));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, COMPONENT_SPACING * 2, 0));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel selectorPanel = createSelectorPanel();
        contentPanel.add(selectorPanel, BorderLayout.SOUTH);

        JPanel chartsPanel = createChartsPanel();
        contentPanel.add(chartsPanel, BorderLayout.CENTER);
    }

    private JPanel createSelectorPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(COMPONENT_SPACING * 2, 0, 0, 0));

        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));

        monthComboBox = new JComboBox<>(getMonthNames());
        setComboBoxSize(monthComboBox);

        yearComboBox = new JComboBox<>(getRecentYears());
        setComboBoxSize(yearComboBox);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshAnalytics());

        row.add(new JLabel("Month:"));
        row.add(Box.createHorizontalStrut(COMPONENT_SPACING));
        row.add(monthComboBox);
        row.add(Box.createHorizontalStrut(COMPONENT_SPACING * 2));

        row.add(new JLabel("Year:"));
        row.add(Box.createHorizontalStrut(COMPONENT_SPACING));
        row.add(yearComboBox);
        row.add(Box.createHorizontalStrut(COMPONENT_SPACING * 2));

        row.add(refreshButton);
        row.add(Box.createHorizontalGlue());

        budgetProgressBar = new JProgressBar(PROGRESS_MIN, PROGRESS_MAX);
        budgetProgressBar.setStringPainted(true);

        noDataLabel = new JLabel("");
        noDataLabel.setFont(noDataLabel.getFont().deriveFont(Font.ITALIC, 12f));

        panel.add(row);
        panel.add(Box.createVerticalStrut(COMPONENT_SPACING));
        panel.add(new JLabel("Budget Usage:"));
        panel.add(Box.createVerticalStrut(COMPONENT_SPACING));
        panel.add(budgetProgressBar);
        panel.add(Box.createVerticalStrut(COMPONENT_SPACING));
        panel.add(noDataLabel);

        return panel;
    }

    private JPanel createChartsPanel() {
        JPanel charts = new JPanel(new BorderLayout(COMPONENT_SPACING, COMPONENT_SPACING));

        pieChartPanel = new ChartPanel(null);
        pieChartPanel.setBorder(BorderFactory.createTitledBorder("Category Distribution"));
        pieChartPanel.setPreferredSize(new Dimension(WINDOW_WIDTH / 2 - PADDING, WINDOW_HEIGHT - 260));

        barChartPanel = new ChartPanel(null);
        barChartPanel.setBorder(BorderFactory.createTitledBorder("Monthly Trend"));
        barChartPanel.setPreferredSize(new Dimension(WINDOW_WIDTH / 2 - PADDING, WINDOW_HEIGHT - 260));

        charts.add(pieChartPanel, BorderLayout.WEST);
        charts.add(barChartPanel, BorderLayout.EAST);

        return charts;
    }

    private void setComboBoxSize(JComboBox<?> comboBox) {
        Dimension preferredSize = new Dimension(SELECTOR_COMBO_WIDTH, SELECTOR_COMBO_HEIGHT);
        comboBox.setPreferredSize(preferredSize);
        comboBox.setMaximumSize(preferredSize);
        comboBox.setMinimumSize(preferredSize);
    }

    private String[] getMonthNames() {
        return new String[]{
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
    }

    private Integer[] getRecentYears() {
        int currentYear = LocalDate.now().getYear();
        Integer[] years = new Integer[YEARS_RANGE];
        for (int i = 0; i < YEARS_RANGE; i++) {
            years[i] = currentYear - i;
        }
        return years;
    }

    private void loadInitialSummary() {
        LocalDate today = LocalDate.now();
        monthComboBox.setSelectedIndex(today.getMonthValue() - 1);
        yearComboBox.setSelectedItem(today.getYear());
        refreshAnalytics();
    }

    private void refreshAnalytics() {
        int month = monthComboBox.getSelectedIndex() + 1;
        int year = (Integer) yearComboBox.getSelectedItem();

        AnalyticsSummary summary = analyticsController.loadSummary(month, year);
        updateNoData(summary);
        updateCharts(summary);
        updateBudgetProgress(summary);
    }

    private void updateNoData(AnalyticsSummary summary) {
        if (!summary.hasData()) {
            noDataLabel.setText("No data available for the selected period.");
        } else {
            noDataLabel.setText("");
        }
    }

    private void updateCharts(AnalyticsSummary summary) {
        PieDataset<String> pieDataset = analyticsController.buildCategoryPieDataset(summary);
        CategoryDataset barDataset = analyticsController.buildMonthlyBarDataset(summary);

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Category Distribution",
                pieDataset,
                true,
                true,
                false
        );
        configurePieChart(pieChart);

        JFreeChart barChart = ChartFactory.createBarChart(
                "Monthly Trend",
                "Month",
                "Total Spending",
                barDataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        configureBarChart(barChart);

        pieChartPanel.setChart(pieChart);
        barChartPanel.setChart(barChart);
    }

    private void configurePieChart(JFreeChart pieChart) {
        PiePlot plot = (PiePlot) pieChart.getPlot();
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMinimumFractionDigits(2);
        PieSectionLabelGenerator generator =
                new StandardPieSectionLabelGenerator("{0} = {2}",
                        NumberFormat.getNumberInstance(),
                        percentFormat);
        plot.setLabelGenerator(generator);
    }

    private void configureBarChart(JFreeChart barChart) {
        CategoryPlot plot = barChart.getCategoryPlot();
        plot.setRangeGridlinePaint(Color.GRAY);
    }

    private void updateBudgetProgress(AnalyticsSummary summary) {
        double usage = summary.getBudgetUsagePercentage();

        int progress = (int) Math.max(PROGRESS_MIN, Math.min(PROGRESS_MAX, Math.round(usage)));
        budgetProgressBar.setValue(progress);
        budgetProgressBar.setString(String.format("%.2f%%", usage));

        if (usage >= EXCEEDED_THRESHOLD) {
            budgetProgressBar.setForeground(Color.RED);
        } else if (usage >= WARNING_THRESHOLD) {
            budgetProgressBar.setForeground(Color.ORANGE);
        } else {
            budgetProgressBar.setForeground(new Color(0, 128, 0));
        }
    }
}


