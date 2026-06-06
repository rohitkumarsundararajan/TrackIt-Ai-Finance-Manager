package com.trackit.ui.frame;

import com.trackit.controller.AnalyticsController;
import com.trackit.controller.BudgetController;
import com.trackit.controller.CategoryController;
import com.trackit.controller.DashboardController;
import com.trackit.controller.ExpenseController;
import com.trackit.controller.FinancialAdviceController;
import com.trackit.service.AIService;
import com.trackit.service.AnalyticsService;
import com.trackit.service.BudgetService;
import com.trackit.service.CategoryService;
import com.trackit.service.DashboardService;
import com.trackit.service.ExpenseService;
import com.trackit.service.FinancialAdviceService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Main application window for TrackIT.
 * <p>
 * This frame hosts the home screen navigation buttons. It owns the
 * in-memory service instances and wires controllers and frames using
 * constructor injection (no static singletons).
 * </p>
 */
public class HomeFrame extends JFrame {

    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 600;

    private static final String WINDOW_TITLE = "TrackIT - Your Personal Finance Manager";

    private static final int TITLE_FONT_SIZE = 24;
    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 40;
    private static final int PADDING = 20;
    private static final int COMPONENT_SPACING = 12;

    private final CategoryService categoryService;
    private final ExpenseService expenseService;
    private final BudgetService budgetService;

    private final CategoryController categoryController;
    private final ExpenseController expenseController;
    private final DashboardController dashboardController;
    private final BudgetController budgetController;
    private final AnalyticsController analyticsController;
    private final FinancialAdviceController financialAdviceController;

    /**
     * Creates the home frame and initializes the UI.
     */
    public HomeFrame() {
        this.categoryService = new CategoryService();
        this.expenseService = new ExpenseService();
        this.budgetService = new BudgetService(expenseService);

        FinancialAdviceService financialAdviceService = new FinancialAdviceService(
                budgetService, expenseService, new AIService());
        this.financialAdviceController = new FinancialAdviceController(financialAdviceService);

        this.categoryController = new CategoryController(categoryService);
        this.expenseController = new ExpenseController(expenseService, budgetService);
        this.dashboardController = new DashboardController(new DashboardService(expenseService));
        this.budgetController = new BudgetController(budgetService);
        this.analyticsController = new AnalyticsController(new AnalyticsService(expenseService, budgetService));

        initializeFrame();
        initializeContent();
    }

    private void initializeFrame() {
        setTitle(WINDOW_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());
    }

    private void initializeContent() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        add(contentPanel, BorderLayout.CENTER);

        JLabel titleLabel = new JLabel(WINDOW_TITLE, JLabel.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, TITLE_FONT_SIZE));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, COMPONENT_SPACING * 2, 0));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        JButton addExpenseButton = createPrimaryButton("Add Expense");
        addExpenseButton.addActionListener(e -> onAddExpense());

        JButton dashboardButton = createPrimaryButton("Dashboard");
        dashboardButton.addActionListener(e -> onDashboard());

        JButton manageCategoriesButton = createPrimaryButton("Manage Categories");
        manageCategoriesButton.addActionListener(e -> onManageCategories());

        JButton analyticsButton = createPrimaryButton("Analytics");
        analyticsButton.addActionListener(e -> onAnalytics());

        JButton budgetManagementButton = createPrimaryButton("Budget Management");
        budgetManagementButton.addActionListener(e -> onBudgetManagement());

        JButton logoutButton = createPrimaryButton("Logout");
        logoutButton.addActionListener(e -> onLogout());

        buttonPanel.add(centered(addExpenseButton));
        buttonPanel.add(Box.createVerticalStrut(COMPONENT_SPACING));

        buttonPanel.add(centered(dashboardButton));
        buttonPanel.add(Box.createVerticalStrut(COMPONENT_SPACING));

        buttonPanel.add(centered(manageCategoriesButton));
        buttonPanel.add(Box.createVerticalStrut(COMPONENT_SPACING));

        buttonPanel.add(centered(analyticsButton));
        buttonPanel.add(Box.createVerticalStrut(COMPONENT_SPACING));

        buttonPanel.add(centered(budgetManagementButton));
        buttonPanel.add(Box.createVerticalStrut(COMPONENT_SPACING));

        buttonPanel.add(centered(logoutButton));

        contentPanel.add(buttonPanel, BorderLayout.CENTER);
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        return button;
    }

    private Box centered(JButton button) {
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(button);
        box.add(Box.createHorizontalGlue());
        return box;
    }

    private void onAddExpense() {
        ExpenseFrame expenseFrame = new ExpenseFrame(expenseController, categoryService);
        expenseFrame.setVisible(true);
    }

    private void onDashboard() {
        DashboardFrame dashboardFrame = new DashboardFrame(dashboardController);
        dashboardFrame.setVisible(true);
    }

    private void onManageCategories() {
        CategoryFrame categoryFrame = new CategoryFrame(categoryController);
        categoryFrame.setVisible(true);
    }

    private void onAnalytics() {
        AnalyticsFrame analyticsFrame = new AnalyticsFrame(analyticsController);
        analyticsFrame.setVisible(true);
    }

    private void onBudgetManagement() {
        BudgetFrame budgetFrame = new BudgetFrame(budgetController, financialAdviceController);
        budgetFrame.setVisible(true);
    }

    private void onLogout() {
        // TODO: Implement logout flow in a future phase.
    }
}


