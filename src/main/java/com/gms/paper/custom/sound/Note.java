package com.gms.paper.custom.sound;

import java.util.Arrays;
import java.util.Collections;

public enum Note {

    B8(7902.133),
    ASHARP8_BFLAT8(7458.62),
    A8(7040),
    GSHARP8_AFLAT8(6644.875),
    G8(6271.927),
    FSHARP8_GFLAT8(5919.911),
    F8(5587.652),
    E8(5274.041),
    DSHARP8_EFLAT8(4978.032),
    D8(4698.636),
    CSHARP8_DFLAT8(4434.922),
    C8(4186.009),
    B7(3951.066),
    ASHARP7_BFLAT7(3729.31),
    A7(3520),
    GSHARP7_AFLAT7(3322.438),
    G7(3135.963),
    FSHARP7_GFLAT7(2959.955),
    F7(2793.826),
    E7(2637.02),
    DSHARP7_EFLAT7(2489.016),
    D7(2349.318),
    CSHARP7_DFLAT7(2217.461),
    C7(2093.005),
    B6(1975.533),
    ASHARP6_BFLAT6(1864.655),
    A6(1760),
    GSHARP6_AFLAT6(1661.219),
    G6(1567.982),
    FSHARP6_GFLAT6(1479.978),
    F6(1396.913),
    E6(1318.51),
    DSHARP6_EFLAT6(1244.508),
    D6(1174.659),
    CSHARP6_DFLAT6(1108.731),
    C6(1046.502),
    B5(987.7666),
    ASHARP5_BFLAT5(932.3275),
    A5(880),
    GSHARP5_AFLAT5(830.6094),
    G5(783.9909),
    FSHARP5_GFLAT5(739.9888),
    F5(698.4565),
    E5(659.2551),
    DSHARP5_EFLAT5(622.254),
    D5(587.3295),
    CSHARP5_DFLAT5(554.3653),
    C5(523.2511),
    B4(493.8833),
    ASHARP4_BFLAT4(466.1638),
    A4(440),
    GSHARP4_AFLAT4(415.3047),
    G4(391.9954),
    FSHARP4_GFLAT4(369.9944),
    F4(349.2282),
    E4(329.6276),
    DSHARP4_EFLAT4(311.127),
    D4(293.6648),
    CSHARP4_DFLAT4(277.1826),
    C4(261.6256),
    B3(246.9417),
    ASHARP3_BFLAT3(233.0819),
    A3(220),
    GSHARP3_AFLAT3(207.6523),
    G3(195.9977),
    FSHARP3_GFLAT3(184.9972),
    F3(174.6141),
    E3(164.8138),
    DSHARP3_EFLAT3(155.5635),
    D3(146.8324),
    CSHARP3_DFLAT3(138.5913),
    C3(130.8128),
    B2(123.4708),
    ASHARP2_BFLAT2(116.5409),
    A2(110),
    GSHARP2_AFLAT2(103.8262),
    G2(97.99886),
    FSHARP2_GFLAT2(92.49861),
    F2(87.30706),
    E2(82.40689),
    DSHARP2_EFLAT2(77.78175),
    D2(73.41619),
    CSHARP2_DFLAT2(69.29566),
    C2(65.40639),
    B1(61.73541),
    ASHARP1_BFLAT1(58.27047),
    A1(55),
    GSHARP1_AFLAT1(51.91309),
    G1(48.99943),
    FSHARP1_GFLAT1(46.2493),
    F1(43.65353),
    E1(41.20344),
    DSHARP1_EFLAT1(38.89087),
    D1(36.7081),
    CSHARP1_DFLAT1(34.64783),
    C1(32.7032),
    B0(30.86771),
    ASHARP0_BFLAT0(29.13524),
    A0(27.5),
    GSHARP0_AFLAT0(25.95654),
    G0(24.49971),
    FSHARP0_GFLAT0(23.12465),
    F0(21.82676),
    E0(20.60172),
    DSHARP0_EFLAT0(19.44544),
    D0(18.35405),
    CSHARP0_DFLAT0(17.32391),
    C0(16.3516);

    private final float pitch;
    private final float pitchCentre = 370F; //(Slightly sharp?) F#4
    public static final Note[] notes = Note.values();

    //private final EnumMap<Integer,Note> notes = this.getEnumConstants();

    Note(double freq) {
        this.pitch = (float)freq/pitchCentre;
    }

    public static Note[] getNotes(){
        Note[] nList = Note.values();
        Collections.reverse(Arrays.asList(nList));
        return nList;
    }

    public float getPitch() {
        return pitch;
    }

}
