package com.trackit.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Domain model representing a spending category in the TrackIT application.
 * <p>
 * Each category has a unique identifier and a human-readable name.
 * </p>
 */
public final class Category {

    private final UUID id;
    private final String name;

    /**
     * Creates a new {@link Category} instance.
     *
     * @param id   unique identifier of the category
     * @param name display name of the category
     */
    public Category(UUID id, String name) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
    }

    /**
     * Returns the unique identifier of the category.
     *
     * @return category identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the display name of the category.
     *
     * @return category name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the category name for display in UI components such as lists.
     *
     * @return category name
     */
    @Override
    public String toString() {
        return name;
    }
}


