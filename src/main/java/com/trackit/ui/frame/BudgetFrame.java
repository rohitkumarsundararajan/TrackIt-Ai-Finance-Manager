package com.trackit.ui.frame;

import com.trackit.controller.BudgetController;
import com.trackit.controller.FinancialAdviceController;
import com.trackit.model.BudgetSummary;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;

/**
 * Swing frame for managing monthly budgets in TrackIT.
 * <p>
 * This frame renders budget inputs and summary labels and delegates
 * all operations to {@link BudgetController}.
 * </p>
 */
public class BudgetFrame extends JFrame {

    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 460;

    private static final String WINDOW_TITLE = "TrackIT - Monthly Budget";
    private static final String TITLE_LABEL_TEXT = "Monthly Budget";

    private static final int TITLE_FONT_SIZE = 20;
    private static final int PADDING = 16;
    private static final int COMPONENT_SPACING = 8;
    private static final int SUMMARY_FONT_SIZE = 14;

    private static final int SELECTOR_COMBO_WIDTH = 140;
    private static final int SELECTOR_COMBO_HEIGHT = 26;
    private static final int YEARS_RANGE = 5;

    private static final double WARNING_THRESHOLD = 80.0;
    private static final double EXCEEDED_THRESHOLD = 100.0;

    private final BudgetController budgetController;
    private final FinancialAdviceController financialAdviceController;

    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> yearComboBox;
    private JTextField budgetAmountField;

    private JLabel totalSpendingValueLabel;
    private JLabel remainingBudgetValueLabel;
    private JLabel usagePercentageValueLabel;
    private JLabel statusLabel;

    /**
     * Creates a new {@link BudgetFrame}.
     *
     * @param budgetController          budget controller
     * @param financialAdviceController advice controller (popup dialog; uses JDBC + OpenRouter when applicable)
     */
    public BudgetFrame(BudgetController budgetController, FinancialAdviceController financialAdviceController) {
        this.budgetController = budgetController;
        this.financialAdviceController = financialAdviceController;

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

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        contentPanel.add(centerPanel, BorderLayout.CENTER);

        centerPanel.add(createSelectorsPanel());
        centerPanel.add(Box.createVerticalStrut(COMPONENT_SPACING * 2));
        centerPanel.add(createBudgetInputPanel());
        centerPanel.add(Box.createVerticalStrut(COMPONENT_SPACING * 2));
        centerPanel.add(createSummaryPanel());
        centerPanel.add(Box.createVerticalStrut(COMPONENT_SPACING * 2));
        centerPanel.add(createAdvicePanel());
    }

    private JPanel createAdvicePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JButton adviceButton = new JButton("Get AI advice");
        adviceButton.addActionListener(e -> {
            int month = monthComboBox.getSelectedIndex() + 1;
            int year = (Integer) yearComboBox.getSelectedItem();
            financialAdviceController.showAdviceDialogIfLimitExceeded(this, month, year);
        });

        panel.add(adviceButton);
        return panel;
    }

    private JPanel createSelectorsPanel() {
        JPanel selectorsPanel = new JPanel();
        selectorsPanel.setLayout(new BoxLayout(selectorsPanel, BoxLayout.X_AXIS));

        monthComboBox = new JComboBox<>(getMonthNames());
        setComboBoxSize(monthComboBox);

        yearComboBox = new JComboBox<>(getRecentYears());
        setComboBoxSize(yearComboBox);

        selectorsPanel.add(new JLabel("Month:"));
        selectorsPanel.add(Box.createHorizontalStrut(COMPONENT_SPACING));
        selectorsPanel.add(monthComboBox);
        selectorsPanel.add(Box.createHorizontalStrut(COMPONENT_SPACING * 2));

        selectorsPanel.add(new JLabel("Year:"));
        selectorsPanel.add(Box.createHorizontalStrut(COMPONENT_SPACING));
        selectorsPanel.add(yearComboBox);

        return selectorsPanel;
    }

    private JPanel createBudgetInputPanel() {
        JPanel budgetPanel = new JPanel();
        budgetPanel.setLayout(new BoxLayout(budgetPanel, BoxLayout.X_AXIS));

        budgetAmountField = new JTextField();
        setFieldHeight(budgetAmountField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> handleSave());

        budgetPanel.add(new JLabel("Budget Amount:"));
        budgetPanel.add(Box.createHorizontalStrut(COMPONENT_SPACING));
        budgetPanel.add(budgetAmountField);
        budgetPanel.add(Box.createHorizontalStrut(COMPONENT_SPACING));
        budgetPanel.add(saveButton);

        return budgetPanel;
    }

    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary"));

        totalSpendingValueLabel = valueLabel();
        remainingBudgetValueLabel = valueLabel();
        usagePercentageValueLabel = valueLabel();
        statusLabel = new JLabel("-");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, SUMMARY_FONT_SIZE));

        summaryPanel.add(row("Total Spending:", totalSpendingValueLabel));
        summaryPanel.add(Box.createVerticalStrut(COMPONENT_SPACING));
        summaryPanel.add(row("Remaining Budget:", remainingBudgetValueLabel));
        summaryPanel.add(Box.createVerticalStrut(COMPONENT_SPACING));
        summaryPanel.add(row("Usage Percentage:", usagePercentageValueLabel));
        summaryPanel.add(Box.createVerticalStrut(COMPONENT_SPACING));
        summaryPanel.add(statusLabel);

        return summaryPanel;
    }

    private JLabel valueLabel() {
        JLabel label = new JLabel("-");
        label.setFont(label.getFont().deriveFont(Font.PLAIN, SUMMARY_FONT_SIZE));
        return label;
    }

    private JPanel row(String labelText, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(labelText), BorderLayout.WEST);
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }

    private void setComboBoxSize(JComboBox<?> comboBox) {
        Dimension preferredSize = new Dimension(SELECTOR_COMBO_WIDTH, SELECTOR_COMBO_HEIGHT);
        comboBox.setPreferredSize(preferredSize);
        comboBox.setMaximumSize(preferredSize);
        comboBox.setMinimumSize(preferredSize);
    }

    private void setFieldHeight(JTextField field) {
        Dimension preferredSize = field.getPreferredSize();
        preferredSize.height = SELECTOR_COMBO_HEIGHT;
        field.setPreferredSize(preferredSize);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, SELECTOR_COMBO_HEIGHT));
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
        refreshSummary();
    }

    private void handleSave() {
        int month = monthComboBox.getSelectedIndex() + 1;
        int year = (Integer) yearComboBox.getSelectedItem();

        try {
            budgetController.saveBudget(month, year, budgetAmountField.getText());
            JOptionPane.showMessageDialog(this, "Budget saved successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            refreshSummary();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshSummary() {
        int month = monthComboBox.getSelectedIndex() + 1;
        int year = (Integer) yearComboBox.getSelectedItem();

        BudgetSummary summary = budgetController.loadSummary(month, year);
        updateSummary(summary);
    }

    private void updateSummary(BudgetSummary summary) {
        totalSpendingValueLabel.setText(summary.getTotalSpending().toString());

        if (summary.getBudgetLimit() == null) {
            remainingBudgetValueLabel.setText("-");
            usagePercentageValueLabel.setText("0.00%");
            statusLabel.setText("No budget set (unlimited).");
            statusLabel.setForeground(getForeground());
            return;
        }

        remainingBudgetValueLabel.setText(summary.getRemainingAmount().toString());
        usagePercentageValueLabel.setText(String.format("%.2f%%", summary.getUsagePercentage()));

        if (summary.isExceeded()) {
            statusLabel.setText("Budget exceeded!");
            statusLabel.setForeground(Color.RED);
        } else if (summary.isWarning()) {
            statusLabel.setText("Approaching budget limit.");
            statusLabel.setForeground(Color.ORANGE);
        } else {
            statusLabel.setText("Within budget.");
            statusLabel.setForeground(new Color(0, 128, 0));
        }
    }
}


