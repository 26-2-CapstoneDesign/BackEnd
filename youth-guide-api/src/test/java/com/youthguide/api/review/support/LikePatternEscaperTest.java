package com.youthguide.api.review.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LikePatternEscaperTest {

    @Test
    void wrapsKeywordForContainsSearch() {
        assertThat(LikePatternEscaper.toContainsPattern("정보처리"))
                .isEqualTo("%정보처리%");
    }

    @Test
    void escapesLikeWildcardsAndEscapeCharacter() {
        assertThat(LikePatternEscaper.toContainsPattern("100%_C:\\temp"))
                .isEqualTo("%100\\%\\_C:\\\\temp%");
    }

    @Test
    void preservesSqlLikeTextAsBoundLiteralContent() {
        assertThat(LikePatternEscaper.toContainsPattern("%' OR 1=1 --"))
                .isEqualTo("%\\%' OR 1=1 --%");
    }

    @Test
    void returnsNullForMissingOrBlankKeyword() {
        assertThat(LikePatternEscaper.toContainsPattern(null)).isNull();
        assertThat(LikePatternEscaper.toContainsPattern("")).isNull();
        assertThat(LikePatternEscaper.toContainsPattern("   ")).isNull();
    }
}
