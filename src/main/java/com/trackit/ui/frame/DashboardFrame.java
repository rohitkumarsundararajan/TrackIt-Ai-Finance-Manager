package com.trackit.ui.frame;

import com.trackit.controller.DashboardController;
import com.trackit.model.DashboardSummary;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;

/**
 * Swing frame for displaying a monthly dashboard summary.
 * <p>
 * This frame renders selectors, summary labels, and a category breakdown table.
 * All calculations are delegated to {@link DashboardController}/{@code DashboardService}.
 * </p>
 */
public class DashboardFrame extends JFrame {

    private static final int WINDOW_WIDTH = 820;
    private static final int WINDOW_HEIGHT = 520;

    private static final String WINDOW_TITLE = "TrackIT - Dashboard";
    private static final String TITLE_LABEL_TEXT = "Dashboard";

    private static final int TITLE_FONT_SIZE = 20;
    private static final int PADDING = 16;
    private static final int COMPONENT_SPACING = 8;
    private static final int SUMMARY_LABEL_FONT_SIZE = 14;

    private static final int SELECTOR_COMBO_WIDTH = 140;
    private static final int SELECTOR_COMBO_HEIGHT = 26;
    private static final int YEARS_RANGE = 5;

    private final DashboardController dashboardController;

    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> yearComboBox;

    private JLabel totalSpendingValueLabel;
    private JLabel totalTransactionsValueLabel;
    private JLabel topCategoryValueLabel;
    private JLabel noDataLabel;

    private JTable breakdownTable;

    /**
     * Creates a new {@link DashboardFrame}.
     *
     * @param dashboardController dashboard controller
     */
    public DashboardFrame(DashboardController dashboardController) {
        this.dashboardController = dashboardController;

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

        JPanel centerPanel = new JPanel(new BorderLayout(COMPONENT_SPACING, COMPONENT_SPACING));
        contentPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel summaryPanel = createSummaryPanel();
        centerPanel.add(summaryPanel, BorderLayout.WEST);

        JScrollPane tablePane = createTablePane();
        centerPanel.add(tablePane, BorderLayout.CENTER);
    }

    private JPanel createSelectorPanel() {
        JPanel selectorPanel = new JPanel();
        selectorPanel.setLayout(new BoxLayout(selectorPanel, BoxLayout.X_AXIS));
        selectorPanel.setBorder(new EmptyBorder(COMPONENT_SPACING * 2, 0, 0, 0));

        monthComboBox = new JComboBox<>(getMonthNames());
        setComboBoxSize(monthComboBox);

        yearComboBox = new JComboBox<>(getRecentYears());
        setComboBoxSize(yearComboBox);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshDashboard());

        selectorPanel.add(new JLabel("Month:"));
        selectorPanel.add(Box.createHorizontalStrut(COMPONENT_SPACING));
        selectorPanel.add(monthComboBox);
        selectorPanel.add(Box.createHorizontalStrut(COMPONENT_SPACING * 2));

        selectorPanel.add(new JLabel("Year:"));
        selectorPanel.add(Box.createHorizontalStrut(COMPONENT_SPACING));
        selectorPanel.add(yearComboBox);
        selectorPanel.add(Box.createHorizontalStrut(COMPONENT_SPACING * 2));

        selectorPanel.add(refreshButton);
        selectorPanel.add(Box.createHorizontalGlue());

        return selectorPanel;
    }

    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary"));
        summaryPanel.setPreferredSize(new Dimension(280, 0));

        totalSpendingValueLabel = createSummaryValueLabel();
        totalTransactionsValueLabel = createSummaryValueLabel();
        topCategoryValueLabel = createSummaryValueLabel();

        noDataLabel = new JLabel("");
        noDataLabel.setFont(noDataLabel.getFont().deriveFont(Font.ITALIC, SUMMARY_LABEL_FONT_SIZE));

        summaryPanel.add(summaryRow("Total Spending:", totalSpendingValueLabel));
        summaryPanel.add(Box.createVerticalStrut(COMPONENT_SPACING));
        summaryPanel.add(summaryRow("Total Transactions:", totalTransactionsValueLabel));
        summaryPanel.add(Box.createVerticalStrut(COMPONENT_SPACING));
        summaryPanel.add(summaryRow("Top Spending Category:", topCategoryValueLabel));
        summaryPanel.add(Box.createVerticalStrut(COMPONENT_SPACING));
        summaryPanel.add(noDataLabel);

        return summaryPanel;
    }

    private JScrollPane createTablePane() {
        breakdownTable = new JTable();
        breakdownTable.setFillsViewportHeight(true);
        return new JScrollPane(breakdownTable);
    }

    private JLabel createSummaryValueLabel() {
        JLabel label = new JLabel("-");
        label.setFont(label.getFont().deriveFont(Font.PLAIN, SUMMARY_LABEL_FONT_SIZE));
        return label;
    }

    private JPanel summaryRow(String labelText, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.add(new JLabel(labelText), BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.CENTER);
        return row;
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
        refreshDashboard();
    }

    private void refreshDashboard() {
        int month = monthComboBox.getSelectedIndex() + 1;
        int year = (Integer) yearComboBox.getSelectedItem();

        DashboardSummary summary = dashboardController.loadSummary(month, year);
        updateSummary(summary);

        DefaultTableModel model = dashboardController.buildCategoryBreakdownTableModel(summary);
        breakdownTable.setModel(model);
    }

    private void updateSummary(DashboardSummary summary) {
        totalSpendingValueLabel.setText(summary.getTotalSpending().toString());
        totalTransactionsValueLabel.setText(String.valueOf(summary.getTotalTransactions()));
        topCategoryValueLabel.setText(summary.getTopCategory() == null ? "-" : summary.getTopCategory());

        String noDataMessage = dashboardController.getNoDataMessage(summary);
        noDataLabel.setText(noDataMessage);
    }
}


