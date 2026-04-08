package com.screenbridge.mirror.application;

import com.screenbridge.mirror.domain.WindowSize;

/**
 * Calculates a window size that fits inside the host screen while preserving a target aspect ratio.
 */
public final class WindowFitCalculator {
    private WindowFitCalculator() {
    }

    public static WindowSize fit(int maxWidth, int maxHeight, String rawAspectRatio) {
        if (maxWidth <= 0 || maxHeight <= 0) {
            throw new IllegalArgumentException("Host screen size must be positive.");
        }

        String aspectRatio = rawAspectRatio == null ? "" : rawAspectRatio.trim();
        String[] parts = aspectRatio.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Aspect ratio must have the form width:height.");
        }

        int widthRatio = parsePositive(parts[0]);
        int heightRatio = parsePositive(parts[1]);

        long widthLimitedByHeight = (long) maxHeight * widthRatio / heightRatio;
        int resolvedWidth;
        int resolvedHeight;
        if (widthLimitedByHeight <= maxWidth) {
            resolvedWidth = (int) widthLimitedByHeight;
            resolvedHeight = maxHeight;
        } else {
            resolvedWidth = maxWidth;
            resolvedHeight = (int) ((long) maxWidth * heightRatio / widthRatio);
        }

        if (resolvedWidth <= 0 || resolvedHeight <= 0) {
            throw new IllegalArgumentException("Resolved window size must be positive.");
        }

        return new WindowSize(resolvedWidth, resolvedHeight);
    }

    private static int parsePositive(String rawValue) {
        try {
            int parsed = Integer.parseInt(rawValue.trim());
            if (parsed <= 0) {
                throw new IllegalArgumentException("Aspect ratio values must be positive.");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Aspect ratio values must be integers.", exception);
        }
    }
}
