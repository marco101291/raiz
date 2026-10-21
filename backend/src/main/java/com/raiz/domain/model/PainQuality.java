package com.raiz.domain.model;

public enum PainQuality {
    BURNING("Ardor o quemazón", true),
    TINGLING("Hormigueo", true),
    NUMBNESS("Adormecimiento", true),
    ELECTRIC("Como descarga eléctrica", true),
    DULL_ACHE("Dolor sordo, como cansancio", false),
    STABBING("Punzada o pinchazo", false),
    STIFFNESS("Rigidez", false);

    private final String label;
    private final boolean neuropathic;

    PainQuality(String label, boolean neuropathic) {
        this.label = label;
        this.neuropathic = neuropathic;
    }

    public String label() {
        return label;
    }

    /** Typical descriptors of nerve pain. */
    public boolean neuropathic() {
        return neuropathic;
    }
}
