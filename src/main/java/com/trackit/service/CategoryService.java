package com.trackit.service;

import com.trackit.model.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Service responsible for business operations related to categories.
 * <p>
 * This service centralizes category validation and management.
 * Uses in-memory storage only.
 * </p>
 */
public class CategoryService {

    private static final String ERROR_CATEGORY_NAME_REQUIRED = "Category name must not be empty.";
    private static final String ERROR_CATEGORY_ALREADY_EXISTS = "Category with the same name already exists.";
    private static final String ERROR_CATEGORY_NOT_FOUND = "Category not found.";

    private final List<Category> categories = new ArrayList<>();

    /**
     * Adds a new category with the given name.
     *
     * @param name raw category name entered by the user
     * @return the created {@link Category}
     * @throws IllegalArgumentException if validation fails
     */
    public Category addCategory(String name) {
        String normalizedName = normalizeName(name);
        validateNameNotEmpty(normalizedName);
        validateNameIsUnique(normalizedName);

        Category category = new Category(UUID.randomUUID(), normalizedName);
        categories.add(category);
        return category;
    }

    /**
     * Deletes a category with the given name.
     *
     * @param name raw category name requested for deletion
     * @throws IllegalArgumentException if the name is invalid or category does not exist
     */
    public void deleteCategory(String name) {
        String normalizedName = normalizeName(name);
        validateNameNotEmpty(normalizedName);

        Category categoryToRemove = findCategoryByName(normalizedName);
        if (categoryToRemove == null) {
            throw new IllegalArgumentException(ERROR_CATEGORY_NOT_FOUND);
        }

        categories.remove(categoryToRemove);
    }

    /**
     * Returns an immutable copy of all categories.
     *
     * @return list of {@link Category} objects
     */
    public List<Category> getAllCategories() {
        return Collections.unmodifiableList(new ArrayList<>(categories));
    }

    private String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.trim();
    }

    private void validateNameNotEmpty(String name) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException(ERROR_CATEGORY_NAME_REQUIRED);
        }
    }

    private void validateNameIsUnique(String name) {
        String target = name.toLowerCase(Locale.ROOT);
        for (Category category : categories) {
            if (category.getName().toLowerCase(Locale.ROOT).equals(target)) {
                throw new IllegalArgumentException(ERROR_CATEGORY_ALREADY_EXISTS);
            }
        }
    }

    private Category findCategoryByName(String name) {
        String target = name.toLowerCase(Locale.ROOT);
        for (Category category : categories) {
            if (category.getName().toLowerCase(Locale.ROOT).equals(target)) {
                return category;
            }
        }
        return null;
    }
}


