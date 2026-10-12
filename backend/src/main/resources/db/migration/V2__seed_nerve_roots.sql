-- Catálogo clínico semilla: README, sección "Catálogo clínico" (cervical C5–T1, lumbosacra L1–S1).
--
-- TODO(clinico): patient_summary de cada raíz. El README no lo define y no se inventa contenido clínico.
-- TODO(clinico): clinical_notes de las raíces cervicales (el README solo trae notas lumbosacras).
-- TODO(clinico): source_reference de cada raíz. El README cita fuentes base (NICE NG59, NASS) pero no
--                cuál respalda cada territorio radicular.
-- TODO(clinico): la nota "el tamaño de la hernia en la resonancia se correlaciona mal con los síntomas"
--                es general, no de una raíz; queda fuera de clinical_notes hasta definir dónde va.

INSERT INTO nerve_root (code, region, sort_order, patient_summary, clinical_notes, source_reference) VALUES
    ('C5', 'CERVICAL',    1,  NULL, NULL, NULL),
    ('C6', 'CERVICAL',    2,  NULL, NULL, NULL),
    ('C7', 'CERVICAL',    3,  NULL, NULL, NULL),
    ('C8', 'CERVICAL',    4,  NULL, NULL, NULL),
    ('T1', 'CERVICAL',    5,  NULL, NULL, NULL),
    ('L1', 'LUMBOSACRAL', 6,  NULL,
        'Dolor en ingle o cara anterior del muslo sugiere raíces altas (L1-L3), aunque también puede ser '
        || 'referido desde niveles bajos, facetario o sacroilíaco, o de cadera. Diferencial obligado: '
        || 'patología de cadera, hernia inguinal, pubalgia, causas viscerales y zóster.', NULL),
    ('L2', 'LUMBOSACRAL', 7,  NULL,
        'Dolor en ingle o cara anterior del muslo sugiere raíces altas (L1-L3), aunque también puede ser '
        || 'referido desde niveles bajos, facetario o sacroilíaco, o de cadera. Diferencial obligado: '
        || 'patología de cadera, hernia inguinal, pubalgia, causas viscerales y zóster.', NULL),
    ('L3', 'LUMBOSACRAL', 8,  NULL,
        'Dolor en ingle o cara anterior del muslo sugiere raíces altas (L1-L3), aunque también puede ser '
        || 'referido desde niveles bajos, facetario o sacroilíaco, o de cadera. Diferencial obligado: '
        || 'patología de cadera, hernia inguinal, pubalgia, causas viscerales y zóster.', NULL),
    ('L4', 'LUMBOSACRAL', 9,  NULL,
        'El SLR positivo entre 30° y 70° sugiere tensión radicular de L4 a S1. El SLR cruzado es más '
        || 'específico que sensible.', NULL),
    ('L5', 'LUMBOSACRAL', 10, NULL,
        'El SLR positivo entre 30° y 70° sugiere tensión radicular de L4 a S1. El SLR cruzado es más '
        || 'específico que sensible. Sin reflejo fiable: el isquiotibial medial es poco fiable.', NULL),
    ('S1', 'LUMBOSACRAL', 11, NULL,
        'El SLR positivo entre 30° y 70° sugiere tensión radicular de L4 a S1. El SLR cruzado es más '
        || 'específico que sensible.', NULL);

INSERT INTO nerve_root_dermatome (root_code, zone) VALUES
    ('C5', 'LATERAL_SHOULDER'),
    ('C6', 'LATERAL_FOREARM'), ('C6', 'THUMB'), ('C6', 'INDEX_FINGER'),
    ('C7', 'MIDDLE_FINGER'),
    ('C8', 'RING_FINGER'), ('C8', 'LITTLE_FINGER'), ('C8', 'MEDIAL_FOREARM'),
    ('T1', 'MEDIAL_ARM'),
    ('L1', 'GROIN'),
    ('L2', 'GROIN'), ('L2', 'UPPER_ANTERIOR_THIGH'),
    ('L3', 'ANTERIOR_THIGH'), ('L3', 'MEDIAL_KNEE'),
    ('L4', 'MEDIAL_KNEE'), ('L4', 'MEDIAL_LEG'), ('L4', 'MEDIAL_MALLEOLUS'),
    ('L5', 'LATERAL_LEG'), ('L5', 'DORSUM_FOOT'), ('L5', 'GREAT_TOE'),
    ('S1', 'LATERAL_FOOT'), ('S1', 'SOLE'), ('S1', 'POSTERIOR_CALF'), ('S1', 'POSTERIOR_THIGH');

INSERT INTO nerve_root_myotome (root_code, action) VALUES
    ('C5', 'SHOULDER_ABDUCTION'), ('C5', 'ELBOW_FLEXION'),
    ('C6', 'WRIST_EXTENSION'), ('C6', 'ELBOW_FLEXION'),
    ('C7', 'ELBOW_EXTENSION'), ('C7', 'WRIST_FLEXION'),
    ('C8', 'FINGER_FLEXION'),
    ('T1', 'FINGER_ABDUCTION'),
    ('L1', 'HIP_FLEXION'),
    ('L2', 'HIP_FLEXION'), ('L2', 'HIP_ADDUCTION'),
    ('L3', 'KNEE_EXTENSION'), ('L3', 'HIP_ADDUCTION'),
    ('L4', 'KNEE_EXTENSION'), ('L4', 'ANKLE_DORSIFLEXION'),
    ('L5', 'GREAT_TOE_EXTENSION'), ('L5', 'ANKLE_DORSIFLEXION'), ('L5', 'HIP_ABDUCTION'),
    ('S1', 'ANKLE_PLANTAR_FLEXION'), ('S1', 'FOOT_EVERSION');

-- C8, T1, L1, L2 y L5 no tienen reflejo en el catálogo.
INSERT INTO nerve_root_reflex (root_code, reflex) VALUES
    ('C5', 'BICEPS'),
    ('C6', 'BRACHIORADIALIS'), ('C6', 'BICEPS'),
    ('C7', 'TRICEPS'),
    ('L3', 'PATELLAR'),
    ('L4', 'PATELLAR'),
    ('S1', 'ACHILLES');

INSERT INTO nerve_root_disc_level (root_code, position, level) VALUES
    ('C5', 0, 'C4-C5'),
    ('C6', 0, 'C5-C6'),
    ('C7', 0, 'C6-C7'),
    ('C8', 0, 'C7-T1'),
    ('T1', 0, 'T1-T2'),
    ('L1', 0, 'T12-L1 posterolateral'), ('L1', 1, 'L1-L2 foraminal'),
    ('L2', 0, 'L1-L2 posterolateral'),  ('L2', 1, 'L2-L3 foraminal'),
    ('L3', 0, 'L2-L3 posterolateral'),  ('L3', 1, 'L3-L4 foraminal'),
    ('L4', 0, 'L3-L4 posterolateral'),  ('L4', 1, 'L4-L5 foraminal'),
    ('L5', 0, 'L4-L5 posterolateral'),  ('L5', 1, 'L5-S1 foraminal'),
    ('S1', 0, 'L5-S1 posterolateral');

INSERT INTO nerve_root_provocation_test (root_code, position, test_name) VALUES
    ('C5', 0, 'Spurling'), ('C5', 1, 'ULTT'),
    ('C6', 0, 'Spurling'), ('C6', 1, 'ULTT1 (mediano)'),
    ('C7', 0, 'Spurling'), ('C7', 1, 'ULTT1'),
    ('C8', 0, 'ULTT3 (cubital)'),
    ('T1', 0, 'ULTT3'),
    ('L1', 0, 'Prueba femoral (débil)'),
    ('L2', 0, 'Prueba femoral (prone knee bend)'),
    ('L3', 0, 'Prueba femoral'),
    ('L4', 0, 'Prueba femoral'), ('L4', 1, 'SLR'),
    ('L5', 0, 'SLR'), ('L5', 1, 'slump'), ('L5', 2, 'SLR cruzado'),
    ('S1', 0, 'SLR'), ('S1', 1, 'slump');
