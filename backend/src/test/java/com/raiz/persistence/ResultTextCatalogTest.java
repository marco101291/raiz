package com.raiz.persistence;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class ResultTextCatalogTest {

    @Test
    void missingTextsFailAtStartupAndAreListed() {
        assertThatIllegalStateException()
                .isThrownBy(() -> new ResultTextCatalog(Map.of("disclaimer", "Educational tool.")))
                .withMessageContaining("referral.SEVERE_PAIN")
                .withMessageContaining("referral.BILATERAL_COMMON_CAUSE.CERVICAL")
                .withMessageContaining("note.NOT_RADICULAR")
                .withMessageContaining("pattern.NOT_ASSESSED");
    }
}
