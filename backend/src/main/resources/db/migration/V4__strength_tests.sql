-- Pruebas de fuerza guiada que una persona puede hacer sola (README, Formularios, paso 4).
-- Las raíces de cada prueba no se guardan: se derivan del miotoma de nerve_root.
-- En cervical no hay pruebas: la autoexploración es poco fiable.

CREATE TABLE strength_test (
    id           VARCHAR(40) PRIMARY KEY,
    sort_order   INTEGER     NOT NULL UNIQUE,
    name         TEXT        NOT NULL,
    instructions TEXT        NOT NULL,
    safety_note  TEXT        NOT NULL,
    motor_action VARCHAR(40) NOT NULL CHECK (motor_action IN (
        'SHOULDER_ABDUCTION', 'ELBOW_FLEXION', 'WRIST_EXTENSION', 'ELBOW_EXTENSION', 'WRIST_FLEXION',
        'FINGER_FLEXION', 'FINGER_ABDUCTION',
        'HIP_FLEXION', 'HIP_ADDUCTION', 'KNEE_EXTENSION', 'ANKLE_DORSIFLEXION', 'GREAT_TOE_EXTENSION',
        'HIP_ABDUCTION', 'ANKLE_PLANTAR_FLEXION', 'FOOT_EVERSION'))
);

INSERT INTO strength_test (id, sort_order, name, instructions, safety_note, motor_action) VALUES
    ('HEEL_WALK', 1, 'Caminar de talones',
        'Da 10 pasos con la punta del pie levantada.',
        'Apóyate en una pared y detente si el dolor sube claramente.', 'ANKLE_DORSIFLEXION'),
    ('TOE_WALK', 2, 'Caminar de puntas',
        'Da 10 pasos sobre la punta de los pies.',
        'Apóyate en una pared y detente si el dolor sube claramente.', 'ANKLE_PLANTAR_FLEXION'),
    ('SINGLE_LEG_HEEL_RAISE', 3, 'Elevación de talón a una pierna',
        'Haz 10 repeticiones por lado, apoyado en la pared, y compara ambos lados.',
        'Apóyate en una pared y detente si el dolor sube claramente.', 'ANKLE_PLANTAR_FLEXION'),
    ('GREAT_TOE_RESISTANCE', 4, 'Dedo gordo contra resistencia',
        'Levanta el dedo gordo mientras un dedo de la mano lo empuja hacia abajo.',
        'Apóyate en una pared y detente si el dolor sube claramente.', 'GREAT_TOE_EXTENSION'),
    ('SINGLE_LEG_SIT_TO_STAND', 5, 'Levantarse de una silla con una pierna',
        'Levántate con apoyo de una mano y compara ambos lados.',
        'Apóyate en una pared y detente si el dolor sube claramente.', 'KNEE_EXTENSION'),
    ('SINGLE_LEG_STANCE', 6, 'Apoyo en una pierna',
        'Sostente 20 segundos sobre una pierna; la cadera del lado contrario no debe caer.',
        'Apóyate en una pared y detente si el dolor sube claramente.', 'HIP_ABDUCTION');
