package com.mendeley.api.request.params;

/**
 * Available sort orders.
 */
public enum Order {
    /**
     * Ascending order.
     */
    ASC("asc"),
    /**
     * Descending order.
     */
    DESC("desc");

    private final String value;
    Order(String value) {
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
