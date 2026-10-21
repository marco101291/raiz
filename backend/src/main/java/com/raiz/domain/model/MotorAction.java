package com.raiz.domain.model;

/** Movements examined to assess each root's myotome. */
public enum MotorAction {
    SHOULDER_ABDUCTION("Separar el brazo del cuerpo"),
    ELBOW_FLEXION("Doblar el codo"),
    WRIST_EXTENSION("Extender la muñeca"),
    ELBOW_EXTENSION("Estirar el codo"),
    WRIST_FLEXION("Flexionar la muñeca"),
    FINGER_FLEXION("Cerrar el puño con fuerza"),
    FINGER_ABDUCTION("Separar los dedos de la mano"),

    HIP_FLEXION("Levantar el muslo"),
    HIP_ADDUCTION("Juntar las piernas"),
    KNEE_EXTENSION("Estirar la rodilla"),
    ANKLE_DORSIFLEXION("Levantar la punta del pie / caminar de talones"),
    GREAT_TOE_EXTENSION("Levantar el dedo gordo del pie"),
    HIP_ABDUCTION("Separar la pierna hacia afuera"),
    ANKLE_PLANTAR_FLEXION("Pararse de puntas"),
    FOOT_EVERSION("Girar el pie hacia afuera");

    private final String label;

    MotorAction(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public SpinalRegion region() {
        return switch (this) {
            case SHOULDER_ABDUCTION, ELBOW_FLEXION, WRIST_EXTENSION, ELBOW_EXTENSION, WRIST_FLEXION,
                 FINGER_FLEXION, FINGER_ABDUCTION -> SpinalRegion.CERVICAL;
            case HIP_FLEXION, HIP_ADDUCTION, KNEE_EXTENSION, ANKLE_DORSIFLEXION, GREAT_TOE_EXTENSION,
                 HIP_ABDUCTION, ANKLE_PLANTAR_FLEXION, FOOT_EVERSION -> SpinalRegion.LUMBOSACRAL;
        };
    }
}
