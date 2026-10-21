package com.raiz.persistence;

import com.raiz.domain.assessment.Assessment.NoRootCorrelation.Note;
import com.raiz.domain.assessment.PatternInsight;
import com.raiz.domain.assessment.ReferralReason;
import com.raiz.domain.assessment.ReferralReason.Kind;
import com.raiz.domain.model.SpinalRegion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Result texts for the person: the engine returns reasons and they are turned into plain language here.
 * Same idea as internationalization: the decision does not change when the wording does.
 *
 * <p>Fails at startup if any reason lacks its text. Adding an enum value without its migration therefore
 * breaks startup (and the tests), not a person's response in production.
 */
@Component
public class ResultTextCatalog {

    private final Map<String, String> texts;

    @Autowired
    ResultTextCatalog(ResultTextRepository repository) {
        this(repository.findAll().stream()
                .collect(Collectors.toUnmodifiableMap(ResultTextEntity::code, ResultTextEntity::text)));
    }

    ResultTextCatalog(Map<String, String> texts) {
        this.texts = Map.copyOf(texts);
        List<String> missing = requiredCodes().filter(code -> !texts.containsKey(code)).toList();
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing result texts in result_text: " + missing);
        }
    }

    public String disclaimer() {
        return texts.get("disclaimer");
    }

    public String referral(ReferralReason reason) {
        return texts.get(referralCode(reason.kind(), reason.region()));
    }

    public String note(Note note) {
        return texts.get("note." + note.name());
    }

    public String pattern(PatternInsight.Type type) {
        return texts.get("pattern." + type.name());
    }

    private static String referralCode(Kind kind, SpinalRegion region) {
        return "referral." + kind.name() + (region == null ? "" : "." + region.name());
    }

    /** Every code the engine can produce. */
    private static Stream<String> requiredCodes() {
        Stream<String> referrals = Arrays.stream(Kind.values()).flatMap(kind -> kind == Kind.BILATERAL_COMMON_CAUSE
                ? Arrays.stream(SpinalRegion.values()).map(region -> referralCode(kind, region))
                : Stream.of(referralCode(kind, null)));
        return Stream.of(
                        Stream.of("disclaimer"),
                        referrals,
                        Arrays.stream(Note.values()).map(note -> "note." + note.name()),
                        Arrays.stream(PatternInsight.Type.values()).map(type -> "pattern." + type.name()))
                .flatMap(codes -> codes);
    }
}
