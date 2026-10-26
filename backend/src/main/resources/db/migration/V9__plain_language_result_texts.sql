-- Plain language in the result texts the person reads (redesign, phase C3): no "raíz", "territorio
-- radicular", "nivel", "irradiación" or "compresión de la cola de caballo". The clinical meaning does not
-- change; the professional-facing content (clinical_notes, the "Para tu médico" section) keeps its terms.
-- Pending clinical review before publishing (README, roadmap, step 5).

UPDATE result_text SET text = CASE code
    WHEN 'disclaimer' THEN
        'Esta herramienta es educativa y no sustituye una valoración médica. Solo un profesional puede '
        || 'confirmar qué está causando tu dolor.'
    WHEN 'referral.EMERGENCY_RED_FLAG' THEN
        'Estos síntomas pueden indicar que la médula o los nervios del final de la columna están muy '
        || 'apretados. No esperes: acude a urgencias.'
    WHEN 'referral.RED_FLAG_RULE_OUT' THEN
        'Antes de cualquier tratamiento, un médico debe descartar otras causas del dolor, como una infección, '
        || 'una fractura o un tumor.'
    WHEN 'referral.NEUROLOGICAL_DEFICIT' THEN
        'Hay señales de que un nervio está perdiendo fuerza o reflejos. Conviene que te revisen pronto y '
        || 'vigilar que no empeore.'
    WHEN 'referral.CHRONIC_RADICULAR' THEN
        'Los síntomas del nervio llevan más de 12 semanas. Conviene que te vea un especialista, que puede '
        || 'pedir una resonancia y, si hace falta, valorar una cirugía.'
    WHEN 'referral.SENSORY_ONLY' THEN
        'Cuando hay hormigueo o adormecimiento sin pérdida de fuerza, lo habitual es que mejore sin cirugía, '
        || 'con ejercicio y tratamiento. Un profesional debe confirmar qué nervio es y guiarte.'
    WHEN 'referral.NO_TERRITORY_MATCH' THEN
        'Un profesional puede buscar otras causas.'
    WHEN 'referral.RADICULAR_UNMAPPED' THEN
        'Un profesional puede confirmar qué nervio está irritado.'
    WHEN 'referral.SELF_LIMITED' THEN
        'Si el dolor no mejora en 2 a 3 semanas, o empieza a bajar por el brazo o la pierna, consulta.'
    WHEN 'note.NO_TERRITORY_MATCH' THEN
        'Las zonas que marcaste no coinciden con el recorrido de ningún nervio de la columna.'
    WHEN 'note.RADICULAR_UNMAPPED' THEN
        'Describes un dolor de nervio que baja por el brazo o la pierna. Marca en el mapa dónde lo sientes '
        || 'para ver qué nervios podrían estar involucrados.'
    WHEN 'note.NOT_RADICULAR' THEN
        'Lo que describes no sigue el recorrido de un nervio de la columna.'
    WHEN 'pattern.RADICULAR' THEN
        'Se parece al dolor de un nervio irritado a su salida de la columna. Una hernia de disco es una '
        || 'causa frecuente, pero no la única.'
    WHEN 'pattern.DISCOGENIC' THEN
        'Se parece al dolor que viene de un disco de la columna, sin señales claras de que un nervio esté '
        || 'irritado.'
    WHEN 'pattern.EXTENSION_SENSITIVE' THEN
        'Se parece más al dolor de las pequeñas articulaciones de la columna que al de una hernia.'
    WHEN 'pattern.NONSPECIFIC' THEN
        'Los rasgos del dolor no apuntan a una causa concreta. Es el tipo de dolor más común y suele mejorar '
        || 'retomando la actividad poco a poco.'
    ELSE text
END;
