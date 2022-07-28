package com.gms.paper.custom.sound;

public enum ChordType {

    MAJ(new Interval[]{Interval.MAJ_3RD, Interval.PERF_5TH}),
    MIN(new Interval[]{Interval.MIN_3RD, Interval.PERF_5TH}),
    SUS2(new Interval[]{Interval.MAJ_3RD, Interval.PERF_5TH}),
    SUS4(new Interval[]{Interval.PERF_4TH, Interval.PERF_5TH}),
    DIM(new Interval[]{Interval.MIN_3RD, Interval.DIM_5TH}),
    AUG(new Interval[]{Interval.MAJ_3RD, Interval.AUG_5TH}),
    DOM7(new Interval[]{Interval.MAJ_3RD, Interval.PERF_5TH, Interval.MIN_7TH}),
    MAJ7(new Interval[]{Interval.MAJ_3RD, Interval.PERF_5TH, Interval.MAJ_7TH}),
    MIN7(new Interval[]{Interval.MIN_3RD, Interval.PERF_5TH, Interval.MIN_7TH}),
    DIM7(new Interval[]{Interval.MIN_3RD, Interval.DIM_5TH, Interval.DIM_7TH}),
    AUGMAJ7(new Interval[]{Interval.MAJ_3RD, Interval.AUG_5TH, Interval.MAJ_7TH}),
    AUG7(new Interval[]{Interval.MAJ_3RD, Interval.AUG_5TH, Interval.MIN_7TH}),
    DOM7FLAT5(new Interval[]{Interval.MAJ_3RD, Interval.DIM_5TH, Interval.MIN_7TH}),
    MINMAJ7(new Interval[]{Interval.MIN_3RD, Interval.PERF_5TH, Interval.MAJ_7TH}),
    HALFDIM(new Interval[]{Interval.MIN_3RD, Interval.DIM_5TH, Interval.MIN_7TH}),
    DIMMAJ(new Interval[]{Interval.MIN_3RD, Interval.DIM_5TH, Interval.MAJ_7TH}),
    MAJ9(new Interval[]{Interval.MAJ_3RD, Interval.PERF_5TH, Interval.MAJ_7TH, Interval.MAJ_9TH}),
    SIX(new Interval[]{Interval.MAJ_3RD, Interval.PERF_5TH, Interval.MAJ_6TH}),
    MAJ6(new Interval[]{Interval.MAJ_3RD, Interval.PERF_5TH, Interval.MAJ_6TH, Interval.MAJ_7TH}),
    MIN6(new Interval[]{Interval.MIN_3RD, Interval.PERF_5TH, Interval.MAJ_6TH}),
    MAJFLAT6(new Interval[]{Interval.MAJ_3RD, Interval.PERF_5TH, Interval.MIN_6TH}),
    MINFLAT6(new Interval[]{Interval.MIN_3RD, Interval.PERF_5TH, Interval.MIN_6TH}),
    DIMFLAT6(new Interval[]{Interval.MIN_3RD, Interval.DIM_5TH, Interval.MIN_6TH}),
    SIX9(new Interval[]{Interval.MAJ_3RD, Interval.PERF_5TH, Interval.MAJ_6TH, Interval.MAJ_9TH});

    private final Interval[] intervals;

    ChordType(Interval[] intervals) {
        this.intervals = intervals;
    }

    public Interval[] getIntervals() {
        return intervals;
    }

}
