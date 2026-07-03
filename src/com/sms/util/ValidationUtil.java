package com.sms.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Shared input-validation helpers. Every method either returns {@code null}
 * (valid) or a short, user-facing error message describing what's wrong —
 * callers display that message instead of crashing or silently failing.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{10}$");
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ValidationUtil() {
    }

    public static String validateRequired(String value, String fieldLabel) {
        if (value == null || value.trim().isEmpty()) {
            return fieldLabel + " is required.";
        }
        return null;
    }

    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email is required.";
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return "Enter a valid email address (e.g. name@example.com).";
        }
        return null;
    }

    /** Phone is optional; only validated when something is entered. */
    public static String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            return "Phone number must be exactly 10 digits.";
        }
        return null;
    }

    /** Parses a yyyy-MM-dd string, returning null if it cannot be parsed. */
    public static LocalDate parseDate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String validateDate(String text, String fieldLabel) {
        if (text == null || text.trim().isEmpty()) {
            return fieldLabel + " is required.";
        }
        if (parseDate(text) == null) {
            return fieldLabel + " must be in YYYY-MM-DD format.";
        }
        return null;
    }
}
