package de.spraener.jsynth;

public enum PianoKeys {
    NONE(0),
    A0(1),
    Ais0(2),
    B0(3),
    C1(4),
    Cis1(5),
    D1(6),
    Dis1(7),
    E1(8),
    F1(9),
    Fis1(10),
    G1(11),
    Gis1(12),
    A1(13),
    Ais1(14),
    B1(15),
    C2(16),
    Cis2(17),
    D2(18),
    Dis2(19),
    E2(20),
    F2(21),
    Fis2(22),
    G2(23),
    Gis2(24),
    A2(25),
    Ais2(26),
    B2(27),
    C3(28),
    Cis3(29),
    D3(30),
    Dis3(31),
    E3(32),
    F3(33),
    Fis3(34),
    G3(35),
    Gis3(36),
    A3(37),
    Ais3(38),
    B3(39),
    C4(40),
    Cis4(41),
    D4(42),
    Dis4(43),
    E4(44),
    F4(45),
    Fis4(46),
    G4(47),
    Gis4(48),
    A4(49),
    Ais4(50),
    B4(51),
    C5(52),
    Cis5(53),
    D5(54),
    Dis5(55),
    E5(56),
    F5(57),
    Fis5(58),
    G5(59),
    Gis5(60),
    A5(61),
    Ais5(62),
    B5(63),
    C6(64),
    Cis6(65),
    D6(66),
    Dis6(67),
    E6(68),
    F6(69),
    Fis6(70),
    G6(71),
    Gis6(72),
    A6(73),
    Ais6(74),
    B6(75),
    C7(76),
    Cis7(77),
    D7(78),
    Dis7(79),
    E7(80),
    F7(81),
    Fis7(82),
    G7(83),
    Gis7(84),
    A7(85),
    Ais7(86),
    B7(87),
    C8(88),

    A_TUNE(49)
    ;

    private int n;
    private float f;

    PianoKeys(int n) {
        this.n = n;
        this.f = (float)(Math.pow(2, (n-49) / 12.0) * 440.0);
        if( n == 0 ) {
            this.f = 0;
        }
    }

    public static float toFreq(float n) {
        return n==0 ? 0 : (float)(Math.pow(2, n-49 / 12.0) * 440.0);
    }

    public static PianoKeys keyOf(int n) {
        if( n==0 ) {
            return PianoKeys.NONE;
        }
        for( PianoKeys key : values()) {
            if( n == key.n ) {
                return key;
            }
        };
        return PianoKeys.NONE;
    }

    public int n() {
        return n;
    }

    public float f() {
        return f;
    }
}
