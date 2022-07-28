package com.gms.paper.custom.sound;

public enum Interval {

    PERF_1ST(0),
    DIM_2ND(0),
    AUG_1ST(1),
    MIN_2ND(1),
    MAJ_2ND(2),
    DIM_3RD(2),
    AUG_2ND(3),
    MIN_3RD(3),
    MAJ_3RD(4),
    DIM_4TH(4),
    AUG_3RD(5),
    PERF_4TH(5),
    AUG_4TH(6),
    DIM_5TH(6),
    PERF_5TH(7),
    DIM_6TH(7),
    AUG_5TH(8),
    MIN_6TH(8),
    MAJ_6TH(9),
    DIM_7TH(9),
    AUG_6TH(10),
    MIN_7TH(10),
    MAJ_7TH(11),
    DIM_8TH(11),
    AUG_7TH(12),
    PERF_8TH(12),
    DIM_9ND(12),
    AUG_8TH(13),
    MIN_9TH(13),
    MAJ_9TH(14),
    DIM_10TH(14),
    AUG_9TH(15),
    MIN_10TH(15),
    MAJ_10TH(16),
    DIM_11TH(16),
    AUG_10TH(17),
    PERF_11TH(17),
    AUG_11TH(18),
    DIM_12TH(18),
    PERF_12TH(19),
    DIM_13TH(19),
    AUG_12TH(20),
    MIN_13TH(20),
    MAJ_13TH(21),
    DIM_14TH(21),
    AUG_13TH(22),
    MIN_14TH(22),
    MAJ_14TH(23),
    DIM_15TH(23),
    AUG_14TH(24),
    PERF_15TH(24);

    private final int semitones;

    Interval(int semitones) {
        this.semitones = semitones;
    }

    public int getSemitones() {
        return semitones;
    }
}
