package com.meetime.hubspot.util;

public final class HttpUtils {

    public static int parseIntHeader(String headerValue, int defaultValue) {
        if (headerValue == null) return defaultValue;
        try {
            return Integer.parseInt(headerValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
