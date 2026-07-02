package com.group1.recruitment.exception;

import java.util.LinkedHashMap;
import java.util.Map;

/** Carries field-keyed validation errors from services back to the form views. */
public class ValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super("Validation failed: " + errors);
        this.errors = errors;
    }

    public static ValidationException of(String field, String message) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put(field, message);
        return new ValidationException(m);
    }

    /** A single non-field error, rendered as a page-level alert. */
    public static ValidationException global(String message) {
        return of("global", message);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
