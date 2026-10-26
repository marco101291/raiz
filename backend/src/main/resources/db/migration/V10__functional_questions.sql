-- Functional questions for the neck (README, Forms, step 4). Self-testing arm strength is unreliable, so
-- instead of tests the person is asked about everyday tasks. Each question names the movement it reflects;
-- its roots are derived from the myotome in nerve_root, like the strength tests (sources: see V2 and V3).
-- The answers are not sent as weakness: the result shows them as a reason for a professional examination.
-- Pending clinical review before publishing (README, roadmap, step 5).

CREATE TABLE functional_question (
    id           VARCHAR(40) PRIMARY KEY,
    sort_order   INTEGER     NOT NULL UNIQUE,
    question     TEXT        NOT NULL,
    motor_action VARCHAR(40) NOT NULL CHECK (motor_action IN (
        'SHOULDER_ABDUCTION', 'ELBOW_FLEXION', 'WRIST_EXTENSION', 'ELBOW_EXTENSION', 'WRIST_FLEXION',
        'FINGER_FLEXION', 'FINGER_ABDUCTION'))
);

INSERT INTO functional_question (id, sort_order, question, motor_action) VALUES
    ('ARM_RAISE', 1,
        '¿Te cuesta levantar el brazo hacia un lado, por ejemplo para peinarte o alcanzar algo alto?',
        'SHOULDER_ABDUCTION'),
    ('ELBOW_BEND', 2,
        '¿Te cuesta doblar el codo para cargar algo, como una bolsa del súper?',
        'ELBOW_FLEXION'),
    ('ARM_PUSH', 3,
        '¿Te cuesta estirar el brazo para empujar, como al levantarte de una silla apoyando las manos?',
        'ELBOW_EXTENSION'),
    ('HAND_GRIP', 4,
        '¿Te falta fuerza para apretar con la mano, como al abrir un frasco o exprimir un trapo?',
        'FINGER_FLEXION');
