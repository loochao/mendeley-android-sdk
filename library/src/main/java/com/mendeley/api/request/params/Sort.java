package com.mendeley.api.request.params;

/**
 * Available fields to sort lists by.
 */
public enum Sort {
    /**
     * Sort by last modified date.
     */
    MODIFIED("last_modified"),
    /**
     * Sort by date added.
     */
    ADDED("created"),
    /**
     * Sort by title alphabetically.
     */
    TITLE("title");

    private final String value;
    Sort(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
    @Override
    public String toString() {
        return value;
    }
}
