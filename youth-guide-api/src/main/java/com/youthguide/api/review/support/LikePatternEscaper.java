package com.youthguide.api.review.support;

public final class LikePatternEscaper {

    private LikePatternEscaper() {
    }

    public static String toContainsPattern(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        String escapedKeyword = keyword
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");

        return "%" + escapedKeyword + "%";
    }
}
