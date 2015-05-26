package com.mendeley.api.request.params;

/**
 * Extended document views. The view specifies which additional fields are returned for document objects.
 * All views return core fields.
 */
public enum View {
    /**
     * Core + bibliographic fields.
     */
    BIB("bib"),
    /**
     * Core + client fields.
     */
    CLIENT("client"),
    /**
     * Core + bibliographic + client fields.
     */
    ALL("all");

    private final String value;
    View(String value) {
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
