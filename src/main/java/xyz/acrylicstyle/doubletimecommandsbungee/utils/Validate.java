package xyz.acrylicstyle.doubletimecommandsbungee.utils;

import java.util.Arrays;

public class Validate {
    public static void notNull(Object o) {
        if (o == null) throw new NullPointerException("Object cannot be null");
    }

    public static void notNull(Object... o) {
        Arrays.asList(o).forEach(obj -> {
            if (obj == null) throw new NullPointerException("Object cannot be null");
        });
    }

    public static void notNull(Object o, String message) {
        if (o == null) throw new NullPointerException(message);
    }

    public static void validateTrue(boolean expression) {
        if (!expression) throw new IllegalArgumentException("Expression must be true.");
    }

    public static void validateTrue(boolean expression, String message) {
        if (!expression) throw new IllegalArgumentException(message);
    }
}
