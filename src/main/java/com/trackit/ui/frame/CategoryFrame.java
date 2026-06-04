package com.trackit.ui.frame;

import com.trackit.controller.CategoryController;
import com.trackit.controller.CategoryOperationResult;
import com.trackit.model.Category;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

/**
 * Swing frame for managing categories within TrackIT.
 * <p>
 * This frame is responsible for rendering UI components and delegating
 * actions to {@link CategoryController}. It contains no business logic.
 * </p>
 */
public class CategoryFrame extends JFrame {

    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 400;

    private static final String WINDOW_TITLE = "TrackIT - Manage Categories";
    private static final String TITLE_LABEL_TEXT = "Manage Categories";

    private static final int TITLE_FONT_SIZE = 20;
    private static final int PADDING = 16;
    private static final int COMPONENT_SPACING = 8;
    private static final int TEXT_FIELD_COLUMNS = 20;
    private static final int CATEGORY_LIST_VISIBLE_ROWS = 10;

    private final CategoryController categoryController;

    private JTextField categoryNameField;
    private DefaultListModel<Category> categoryListModel;
    private JList<Category> categoryList;

    /**
     * Creates a new {@link CategoryFrame}.
     *
     * @param categoryController controller responsible for category operations
     */
    public CategoryFrame(CategoryController categoryController) {
        this.categoryController = categoryController;

        initializeFrame();
        initializeContent();
        loadInitialCategories();
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

        JPanel inputPanel = createInputPanel();
        centerPanel.add(inputPanel);
        centerPanel.add(Box.createVerticalStrut(COMPONENT_SPACING));

        JScrollPane listScrollPane = createCategoryListScrollPane();
        centerPanel.add(listScrollPane);
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));

        categoryNameField = new JTextField(TEXT_FIELD_COLUMNS);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> handleAddCategory());

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> handleDeleteCategory());

        inputPanel.add(categoryNameField);
        inputPanel.add(Box.createHorizontalStrut(COMPONENT_SPACING));
        inputPanel.add(addButton);
        inputPanel.add(Box.createHorizontalStrut(COMPONENT_SPACING));
        inputPanel.add(deleteButton);

        return inputPanel;
    }

    private JScrollPane createCategoryListScrollPane() {
        categoryListModel = new DefaultListModel<>();
        categoryList = new JList<>(categoryListModel);
        categoryList.setVisibleRowCount(CATEGORY_LIST_VISIBLE_ROWS);

        JScrollPane scrollPane = new JScrollPane(categoryList);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        return scrollPane;
    }

    private void loadInitialCategories() {
        List<Category> categories = categoryController.getAllCategories();
        refreshCategoryList(categories);
    }

    private void handleAddCategory() {
        String rawName = categoryNameField.getText();
        CategoryOperationResult result = categoryController.addCategory(rawName);

        if (result.isSuccess()) {
            refreshCategoryList(result.getCategories());
            categoryNameField.setText("");
        } else {
            showErrorDialog(result.getErrorMessage());
        }
    }

    private void handleDeleteCategory() {
        Category selectedCategory = categoryList.getSelectedValue();
        if (selectedCategory == null) {
            showErrorDialog("Please select a category to delete.");
            return;
        }

        CategoryOperationResult result = categoryController.deleteCategory(selectedCategory.getName());
        if (result.isSuccess()) {
            refreshCategoryList(result.getCategories());
        } else {
            showErrorDialog(result.getErrorMessage());
        }
    }

    private void refreshCategoryList(List<Category> categories) {
        categoryListModel.clear();
        for (Category category : categories) {
            categoryListModel.addElement(category);
        }
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}


