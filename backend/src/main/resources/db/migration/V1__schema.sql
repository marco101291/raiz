-- Catálogo de raíces nerviosas. Las etiquetas visibles viven en los enums del dominio;
-- aquí solo se guardan los nombres de los enums como texto, validados con CHECK.

CREATE TABLE nerve_root (
    code             VARCHAR(4)  PRIMARY KEY,
    region           VARCHAR(20) NOT NULL CHECK (region IN ('CERVICAL', 'LUMBOSACRAL')),
    -- Orden anatómico: el motor lo usa para desempatar matches con la misma puntuación.
    sort_order       INTEGER     NOT NULL UNIQUE,
    patient_summary  TEXT,
    clinical_notes   TEXT,
    source_reference TEXT
);

CREATE TABLE nerve_root_dermatome (
    root_code VARCHAR(4)  NOT NULL REFERENCES nerve_root (code) ON DELETE CASCADE,
    zone      VARCHAR(40) NOT NULL CHECK (zone IN (
        'LATERAL_SHOULDER', 'LATERAL_FOREARM', 'THUMB', 'INDEX_FINGER', 'MIDDLE_FINGER',
        'RING_FINGER', 'LITTLE_FINGER', 'MEDIAL_FOREARM', 'MEDIAL_ARM',
        'GROIN', 'UPPER_ANTERIOR_THIGH', 'ANTERIOR_THIGH', 'MEDIAL_KNEE', 'MEDIAL_LEG',
        'MEDIAL_MALLEOLUS', 'LATERAL_LEG', 'DORSUM_FOOT', 'GREAT_TOE', 'LATERAL_FOOT', 'SOLE',
        'POSTERIOR_CALF', 'POSTERIOR_THIGH')),
    PRIMARY KEY (root_code, zone)
);

CREATE TABLE nerve_root_myotome (
    root_code VARCHAR(4)  NOT NULL REFERENCES nerve_root (code) ON DELETE CASCADE,
    action    VARCHAR(40) NOT NULL CHECK (action IN (
        'SHOULDER_ABDUCTION', 'ELBOW_FLEXION', 'WRIST_EXTENSION', 'ELBOW_EXTENSION', 'WRIST_FLEXION',
        'FINGER_FLEXION', 'FINGER_ABDUCTION',
        'HIP_FLEXION', 'HIP_ADDUCTION', 'KNEE_EXTENSION', 'ANKLE_DORSIFLEXION', 'GREAT_TOE_EXTENSION',
        'HIP_ABDUCTION', 'ANKLE_PLANTAR_FLEXION', 'FOOT_EVERSION')),
    PRIMARY KEY (root_code, action)
);

CREATE TABLE nerve_root_reflex (
    root_code VARCHAR(4)  NOT NULL REFERENCES nerve_root (code) ON DELETE CASCADE,
    reflex    VARCHAR(20) NOT NULL CHECK (reflex IN (
        'BICEPS', 'BRACHIORADIALIS', 'TRICEPS', 'PATELLAR', 'ACHILLES')),
    PRIMARY KEY (root_code, reflex)
);

-- Listas ordenadas: "position" conserva el orden en que se presentan.
CREATE TABLE nerve_root_disc_level (
    root_code VARCHAR(4) NOT NULL REFERENCES nerve_root (code) ON DELETE CASCADE,
    position  INTEGER    NOT NULL,
    level     TEXT       NOT NULL,
    PRIMARY KEY (root_code, position)
);

CREATE TABLE nerve_root_provocation_test (
    root_code VARCHAR(4) NOT NULL REFERENCES nerve_root (code) ON DELETE CASCADE,
    position  INTEGER    NOT NULL,
    test_name TEXT       NOT NULL,
    PRIMARY KEY (root_code, position)
);
