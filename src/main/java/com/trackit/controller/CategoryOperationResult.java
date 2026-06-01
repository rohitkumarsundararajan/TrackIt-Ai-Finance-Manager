package com.trackit.controller;

import com.trackit.model.Category;

import java.util.Collections;
import java.util.List;

/**
 * Data transfer object representing the result of a category operation.
 * <p>
 * Used by {@link CategoryController} to communicate success/failure outcomes
 * back to the UI without exposing service exceptions directly.
 * </p>
 */
public final class CategoryOperationResult {

    private final boolean success;
    private final String errorMessage;
    private final List<Category> categories;

    private CategoryOperationResult(boolean success, String errorMessage, List<Category> categories) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.categories = categories == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(categories);
    }

    /**
     * Creates a successful operation result.
     *
     * @param categories current list of categories after the operation
     * @return successful result
     */
    public static CategoryOperationResult success(List<Category> categories) {
        return new CategoryOperationResult(true, null, categories);
    }

    /**
     * Creates a failed operation result.
     *
     * @param errorMessage description of the error
     * @param categories   current list of categories when the error occurred
     * @return failed result
     */
    public static CategoryOperationResult failure(String errorMessage, List<Category> categories) {
        return new CategoryOperationResult(false, errorMessage, categories);
    }

    /**
     * Indicates whether the operation was successful.
     *
     * @return {@code true} if successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the error message if the operation failed.
     *
     * @return error message or {@code null}
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns an immutable list of categories representing the state
     * after the operation.
     *
     * @return categories list
     */
    public List<Category> getCategories() {
        return categories;
    }
}


