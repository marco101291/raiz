package com.raiz.domain.assessment;

import java.util.List;

/**
 * Lectura educativa del patrón de dolor ("¿es una hernia?"). Describe compatibilidades,
 * nunca un diagnóstico.
 *
 * @param features rasgos del cuestionario que apoyan la lectura, en lenguaje claro
 */
public record PatternInsight(Type type, PainProfile.Chronicity chronicity, List<String> features, String explanation) {

    public PatternInsight {
        features = List.copyOf(features);
    }

    public static PatternInsight notAssessed() {
        return new PatternInsight(Type.NOT_ASSESSED, null, List.of(),
                "No se respondió el cuestionario de dolor.");
    }

    public enum Type {
        /** Dolor de tipo nervioso que sigue una extremidad: compatible con irritación de raíz. */
        RADICULAR,
        /** Empeora con flexión, sentado o Valsalva: compatible con origen discal sin irritación de raíz. */
        DISCOGENIC,
        /** Empeora con extensión o de pie y mejora sentado: compatible con origen articular (facetario). */
        EXTENSION_SENSITIVE,
        /** Sin rasgos que orienten a una estructura concreta. */
        NONSPECIFIC,
        NOT_ASSESSED
    }
}
