-- Otras causas posibles de los síntomas en una zona, además de una raíz nerviosa. El resultado de
-- la evaluación las muestra para cada zona marcada: la herramienta da opciones, no un diagnóstico.
-- Pendiente de revisión clínica antes de publicar (README, hoja de ruta, paso 5).
--
-- Fuentes:
--   [Weir 2015]     Weir A, Brukner P, Delahunt E, et al. Doha agreement meeting on terminology and
--                   definitions in groin pain in athletes. Br J Sports Med. 2015;49(12):768-774.
--   [Bradshaw 1997] Bradshaw C, McCrory P, Bell S, Brukner P. Obturator nerve entrapment: a cause of
--                   groin pain in athletes. Am J Sports Med. 1997;25(3):402-408.
--   [Yukawa 1997]   Groin pain associated with lower lumbar disc herniation. Spine. 1997. PMID 9259784.
--   [Nakamura 1996] J Bone Joint Surg Br. 1996;78(4):606-612 (ver V5).
--   [Padua 2016]    Padua L, Coraci D, Erra C, et al. Carpal tunnel syndrome: clinical features,
--                   diagnosis, and management. Lancet Neurol. 2016;15(12):1273-1284.
--   [PLoS ONE 2021] Prevalence and predictive factors for bilateral carpal tunnel syndrome by
--                   electrodiagnosis: a retrospective study. PLoS ONE. 2021;16(12):e0260578.
--                   Bilateral en el 80.7% de 327 pacientes.

CREATE TABLE other_cause (
    id               VARCHAR(40) PRIMARY KEY,
    sort_order       INTEGER     NOT NULL UNIQUE,
    title            TEXT        NOT NULL,
    description      TEXT        NOT NULL,
    source_reference TEXT        NOT NULL
);

CREATE TABLE other_cause_zone (
    other_cause_id VARCHAR(40) NOT NULL REFERENCES other_cause (id) ON DELETE CASCADE,
    zone           VARCHAR(40) NOT NULL CHECK (zone IN (
        'LATERAL_SHOULDER', 'LATERAL_FOREARM', 'THUMB', 'INDEX_FINGER', 'MIDDLE_FINGER',
        'RING_FINGER', 'LITTLE_FINGER', 'MEDIAL_FOREARM', 'MEDIAL_ARM',
        'GROIN', 'UPPER_ANTERIOR_THIGH', 'ANTERIOR_THIGH', 'MEDIAL_KNEE', 'MEDIAL_LEG',
        'MEDIAL_MALLEOLUS', 'LATERAL_LEG', 'DORSUM_FOOT', 'GREAT_TOE', 'LATERAL_FOOT', 'SOLE',
        'POSTERIOR_CALF', 'POSTERIOR_THIGH')),
    PRIMARY KEY (other_cause_id, zone)
);

INSERT INTO other_cause (id, sort_order, title, description, source_reference) VALUES
    ('PUBALGIA', 1, 'Pubalgia',
        'Dolor en el pubis y en los músculos que se insertan en él: aductores y abdominales. Suele doler '
        || 'al apretar el pubis, al hacer abdominales, patear o correr, y con frecuencia de ambos lados.',
        'Weir 2015 (Br J Sports Med 49:768-774)'),
    ('HIP', 2, 'Cadera',
        'El dolor de la articulación de la cadera se siente en la ingle, no en el costado. Suele empeorar '
        || 'al girar la pierna, sentarse en un asiento bajo o cruzar las piernas.',
        'Weir 2015 (Br J Sports Med 49:768-774)'),
    ('GROIN_NERVE_ENTRAPMENT', 3, 'Nervio atrapado en la ingle',
        'Los nervios que pasan por la ingle (obturador, ilioinguinal, genitofemoral) pueden quedar '
        || 'atrapados entre músculos o tejidos y dar ardor u hormigueo en la ingle, junto a los genitales o '
        || 'en la cara interna del muslo.',
        'Bradshaw 1997 (Am J Sports Med 25:402-408)'),
    ('REFERRED_LOW_LUMBAR', 4, 'Dolor referido desde la columna baja',
        'Discos más bajos de la columna, como L4-L5, pueden doler en la ingle aunque no irriten las '
        || 'raíces de esa zona.',
        'Yukawa 1997 (Spine, PMID 9259784); Nakamura 1996 (J Bone Joint Surg Br 78:606-612)'),
    ('CARPAL_TUNNEL', 5, 'Túnel carpiano',
        'El nervio mediano se aprieta a su paso por la muñeca. Da hormigueo en el pulgar, el índice y el '
        || 'medio, suele empeorar de noche o al usar la mano, y con frecuencia afecta ambas manos.',
        'Padua 2016 (Lancet Neurol 15:1273-1284); PLoS ONE 2021 (16:e0260578)');

INSERT INTO other_cause_zone (other_cause_id, zone) VALUES
    ('PUBALGIA', 'GROIN'),
    ('HIP', 'GROIN'),
    ('GROIN_NERVE_ENTRAPMENT', 'GROIN'),
    ('REFERRED_LOW_LUMBAR', 'GROIN'),
    ('CARPAL_TUNNEL', 'THUMB'), ('CARPAL_TUNNEL', 'INDEX_FINGER'), ('CARPAL_TUNNEL', 'MIDDLE_FINGER');
