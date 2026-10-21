-- Knowledge and wording out of the code (redesign, phase A).
--
-- 1. Which zones have common, non-urgent bilateral causes is no longer a switch in BodyZone: a zone has
--    them if any of its other causes is usually bilateral.
-- 2. Texts for the person no longer live in the engine. The engine returns reasons (ReferralReason, Note,
--    PatternInsight.Type) and the web layer turns them into text with this table. The application does
--    not start if any reason lacks its text (ResultTextCatalog).
--
-- Texts are copied verbatim from the code. Pending clinical review before publishing (README, roadmap,
-- step 5).
--
-- Sources: see V6 (Weir 2015; PLoS ONE 2021).

ALTER TABLE other_cause ADD COLUMN common_bilateral BOOLEAN NOT NULL DEFAULT FALSE;

-- Pubalgia and hips: the pubis sits on the midline and both hips are often affected (Weir 2015).
-- Carpal tunnel: bilateral in 80.7% of electrodiagnosed cases (PLoS ONE 2021).
UPDATE other_cause SET common_bilateral = TRUE WHERE id IN ('PUBALGIA', 'HIP', 'CARPAL_TUNNEL');

CREATE TABLE result_text (
    code             VARCHAR(80) PRIMARY KEY,
    text             TEXT        NOT NULL,
    source_reference TEXT
);

INSERT INTO result_text (code, text, source_reference) VALUES
    ('disclaimer',
        'Esta herramienta es educativa y no sustituye una valoración médica. Solo un profesional puede '
        || 'confirmar qué estructura está afectada.', NULL),

    -- Referral reasons (ReferralReason.Kind)
    ('referral.EMERGENCY_RED_FLAG',
        'Estos síntomas pueden indicar compresión grave de la médula o de la cola de caballo. No esperes: '
        || 'acude a urgencias.', NULL),
    ('referral.RED_FLAG_RULE_OUT',
        'Estos datos obligan a descartar causas distintas a una hernia (infección, fractura, tumor) antes de '
        || 'cualquier tratamiento.', NULL),
    ('referral.NEUROLOGICAL_DEFICIT',
        'Hay datos compatibles con pérdida de fuerza o de reflejos. Conviene una exploración neurológica '
        || 'pronto y vigilar que no avance.', NULL),
    ('referral.SEVERE_PAIN',
        'El dolor es intenso. Busca valoración médica en los próximos días para controlarlo; el dolor mal '
        || 'controlado retrasa la recuperación.', NULL),
    ('referral.CHRONIC_RADICULAR',
        'Los síntomas nerviosos llevan más de 12 semanas. Conviene una valoración especializada que incluya '
        || 'estudios de imagen y, si aplica, opinión quirúrgica.', NULL),
    ('referral.SENSORY_ONLY',
        'Los síntomas sensitivos sin pérdida de fuerza suelen mejorar con tratamiento conservador. Un '
        || 'profesional debe confirmar el nivel y guiar el tratamiento.', NULL),
    ('referral.NO_TERRITORY_MATCH', 'Un profesional puede explorar otras causas.', NULL),
    ('referral.RADICULAR_UNMAPPED', 'Un profesional puede confirmar qué raíz está irritada.', NULL),
    ('referral.CHRONIC_PAIN',
        'Llevas más de 12 semanas con dolor: conviene una valoración integral, no solo de la espalda.', NULL),
    ('referral.SELF_LIMITED',
        'Si el dolor no mejora en 2 a 3 semanas, o aparece irradiación, consulta.', NULL),
    ('referral.BILATERAL_COMMON_CAUSE.LUMBOSACRAL',
        'Sientes síntomas en la ingle de ambos lados. Suele deberse a causas frecuentes que no son urgentes, '
        || 'como la pubalgia o las caderas, pero conviene una valoración en los próximos días. Acude a '
        || 'urgencias si además se te duerme la zona entre las piernas o los genitales, te cuesta orinar o '
        || 'evacuar, o pierdes fuerza en las piernas.',
        'Weir 2015 (Br J Sports Med 49:768-774)'),
    ('referral.BILATERAL_COMMON_CAUSE.CERVICAL',
        'Sientes síntomas en los dedos de ambas manos. Suele deberse a causas frecuentes que no son urgentes, '
        || 'como el túnel carpiano, pero conviene una valoración en los próximos días. Acude a urgencias si '
        || 'además notas torpeza al usar las manos o al caminar, o pierdes fuerza.',
        'PLoS ONE 2021 (16:e0260578)'),

    -- Notes when there is no correlation (Assessment.NoRootCorrelation.Note)
    ('note.NO_TERRITORY_MATCH',
        'Los síntomas reportados no coinciden con un territorio radicular del catálogo.', NULL),
    ('note.RADICULAR_UNMAPPED',
        'Describes dolor de tipo nervioso que baja por la extremidad. Marca en el mapa dónde lo sientes para '
        || 'ver qué raíces son compatibles.', NULL),
    ('note.NOT_RADICULAR', 'Lo que describes no sigue el territorio de una raíz nerviosa.', NULL),

    -- Explanation of each pain pattern (PatternInsight.Type)
    ('pattern.RADICULAR',
        'Este patrón es compatible con irritación de una raíz nerviosa. Una hernia discal es una causa '
        || 'frecuente, pero no la única.', NULL),
    ('pattern.DISCOGENIC',
        'Este patrón es compatible con dolor de origen discal, sin datos claros de que un nervio esté '
        || 'irritado.', NULL),
    ('pattern.EXTENSION_SENSITIVE',
        'Este patrón es más compatible con dolor de las articulaciones de la columna que con una hernia.', NULL),
    ('pattern.NONSPECIFIC',
        'Los rasgos del dolor no orientan a una estructura concreta. Es el patrón más común y suele mejorar '
        || 'con actividad gradual.', NULL),
    ('pattern.NOT_ASSESSED', 'No se respondió el cuestionario de dolor.', NULL);
