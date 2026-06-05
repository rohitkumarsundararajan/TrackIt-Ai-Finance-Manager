package com.trackit.ui.frame;

import com.trackit.controller.ExpenseController;
import com.trackit.model.Category;
import com.trackit.service.CategoryService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Swing frame for adding a new expense within TrackIT.
 * <p>
 * This frame renders the expense entry form and delegates actions to
 * {@link ExpenseController}. It contains no validation or calculation logic.
 * </p>
 */
public class ExpenseFrame extends JFrame {

    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 460;

    private static final String WINDOW_TITLE = "TrackIT - Add Expense";
    private static final String TITLE_LABEL_TEXT = "Add Expense";

    private static final int TITLE_FONT_SIZE = 20;
    private static final int PADDING = 16;
    private static final int COMPONENT_SPACING = 8;
    private static final int FIELD_LABEL_SPACING = 8;
    private static final int FIELD_HEIGHT = 26;
    private static final int DESCRIPTION_ROWS = 4;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ExpenseController expenseController;
    private final CategoryService categoryService;

    private JTextField titleField;
    private JComboBox<Category> categoryComboBox;
    private JTextArea descriptionArea;
    private JTextField amountField;
    private JTextField dateField;

    /**
     * Creates a new {@link ExpenseFrame}.
     *
     * @param expenseController controller responsible for expense operations
     * @param categoryService   service providing categories for selection
     */
    public ExpenseFrame(ExpenseController expenseController, CategoryService categoryService) {
        this.expenseController = expenseController;
        this.categoryService = categoryService;

        initializeFrame();
        initializeContent();
        populateCategories();
        setDefaultDate();
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

        JPanel formPanel = createFormPanel();
        contentPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        titleField = new JTextField();
        setPreferredFieldHeight(titleField);

        categoryComboBox = new JComboBox<>();

        descriptionArea = new JTextArea(DESCRIPTION_ROWS, 0);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);

        amountField = new JTextField();
        setPreferredFieldHeight(amountField);

        dateField = new JTextField();
        setPreferredFieldHeight(dateField);

        formPanel.add(labeled("Title:", titleField));
        formPanel.add(Box.createVerticalStrut(COMPONENT_SPACING));

        formPanel.add(labeled("Category:", categoryComboBox));
        formPanel.add(Box.createVerticalStrut(COMPONENT_SPACING));

        formPanel.add(labeled("Description:", descriptionScrollPane));
        formPanel.add(Box.createVerticalStrut(COMPONENT_SPACING));

        formPanel.add(labeled("Amount:", amountField));
        formPanel.add(Box.createVerticalStrut(COMPONENT_SPACING));

        formPanel.add(labeled("Date (yyyy-MM-dd):", dateField));

        return formPanel;
    }

    private JPanel labeled(String labelText, java.awt.Component component) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(labelText);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, FIELD_LABEL_SPACING, 0));
        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> handleAddExpense());

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(addButton);
        buttonPanel.add(Box.createHorizontalStrut(COMPONENT_SPACING));
        buttonPanel.add(clearButton);
        buttonPanel.add(Box.createHorizontalGlue());

        return buttonPanel;
    }

    private void setPreferredFieldHeight(JTextField field) {
        Dimension preferredSize = field.getPreferredSize();
        preferredSize.height = FIELD_HEIGHT;
        field.setPreferredSize(preferredSize);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT));
    }

    private void populateCategories() {
        List<Category> categories = categoryService.getAllCategories();
        categoryComboBox.removeAllItems();
        for (Category category : categories) {
            categoryComboBox.addItem(category);
        }
        categoryComboBox.setSelectedIndex(categories.isEmpty() ? -1 : 0);
    }

    private void setDefaultDate() {
        dateField.setText(LocalDate.now().format(DATE_FORMATTER));
    }

    private void handleAddExpense() {
        String title = titleField.getText();
        Category category = (Category) categoryComboBox.getSelectedItem();
        String description = descriptionArea.getText();
        String amountText = amountField.getText();
        String dateText = dateField.getText();

        try {
            ExpenseController.ExpenseAddResult result =
                    expenseController.addExpense(title, category, description, amountText, dateText);

            JOptionPane.showMessageDialog(this, "Expense added successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            if (result.isBudgetExceeded()) {
                JOptionPane.showMessageDialog(this, "Monthly budget exceeded!", "Budget Alert",
                        JOptionPane.WARNING_MESSAGE);
            }

            clearForm();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        titleField.setText("");
        descriptionArea.setText("");
        amountField.setText("");
        setDefaultDate();

        if (categoryComboBox.getItemCount() > 0) {
            categoryComboBox.setSelectedIndex(0);
        } else {
            categoryComboBox.setSelectedIndex(-1);
        }
    }
}


