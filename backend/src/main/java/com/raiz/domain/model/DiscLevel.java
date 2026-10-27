package com.raiz.domain.model;

/**
 * Intervertebral disc level, as a person may know it from an MRI report ("L4-L5"). Only the levels that the
 * root catalog associates with a root are listed.
 */
public enum DiscLevel {
    C4_C5("C4-C5", SpinalRegion.CERVICAL),
    C5_C6("C5-C6", SpinalRegion.CERVICAL),
    C6_C7("C6-C7", SpinalRegion.CERVICAL),
    C7_T1("C7-T1", SpinalRegion.CERVICAL),
    T12_L1("T12-L1", SpinalRegion.LUMBOSACRAL),
    L1_L2("L1-L2", SpinalRegion.LUMBOSACRAL),
    L2_L3("L2-L3", SpinalRegion.LUMBOSACRAL),
    L3_L4("L3-L4", SpinalRegion.LUMBOSACRAL),
    L4_L5("L4-L5", SpinalRegion.LUMBOSACRAL),
    L5_S1("L5-S1", SpinalRegion.LUMBOSACRAL);

    private final String label;
    private final SpinalRegion region;

    DiscLevel(String label, SpinalRegion region) {
        this.label = label;
        this.region = region;
    }

    public String label() {
        return label;
    }

    public SpinalRegion region() {
        return region;
    }

    /** Whether a catalog entry such as "L4-L5 posterolateral" refers to this level. */
    public boolean describes(String typicalDiscLevel) {
        return typicalDiscLevel.equals(label) || typicalDiscLevel.startsWith(label + " ");
    }
}
