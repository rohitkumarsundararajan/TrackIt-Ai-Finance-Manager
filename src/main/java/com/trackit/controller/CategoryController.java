package com.trackit.controller;

import com.trackit.model.Category;
import com.trackit.service.CategoryService;

import java.util.List;

/**
 * Controller responsible for handling user interactions related to categories.
 * <p>
 * Coordinates requests between UI components and the {@link CategoryService}.
 * This controller contains no business rules; it delegates validation and
 * persistence decisions to the service layer.
 * </p>
 */
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Creates a new instance of {@link CategoryController}.
     *
     * @param categoryService service providing category-related operations
     */
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Attempts to add a new category with the given name.
     *
     * @param name raw category name entered by the user
     * @return result describing the outcome
     */
    public CategoryOperationResult addCategory(String name) {
        try {
            categoryService.addCategory(name);
            return CategoryOperationResult.success(categoryService.getAllCategories());
        } catch (IllegalArgumentException ex) {
            return CategoryOperationResult.failure(ex.getMessage(), categoryService.getAllCategories());
        }
    }

    /**
     * Attempts to delete an existing category with the given name.
     *
     * @param name raw category name requested for deletion
     * @return result describing the outcome
     */
    public CategoryOperationResult deleteCategory(String name) {
        try {
            categoryService.deleteCategory(name);
            return CategoryOperationResult.success(categoryService.getAllCategories());
        } catch (IllegalArgumentException ex) {
            return CategoryOperationResult.failure(ex.getMessage(), categoryService.getAllCategories());
        }
    }

    /**
     * Returns a snapshot of all categories.
     *
     * @return list of categories
     */
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }
}


