package smartsporthub.model;

import java.time.LocalTime;

/** Nguồn cấu hình chung cho giá và khung giờ của Field Domain. */
public final class FieldPricing {

    public static final LocalTime PEAK_START = LocalTime.of(17, 0);
    public static final LocalTime PEAK_END = LocalTime.of(20, 0);
    public static final double PEAK_SURCHARGE_RATE = 0.20;

    public static final double FOOTBALL_NIGHT_LIGHT_FEE = 50_000;
    public static final double BADMINTON_RACKET_FEE = 20_000;
    public static final double BADMINTON_MAT_FEE = 10_000;
    public static final double TENNIS_EQUIPMENT_FEE = 40_000;
    public static final double PICKLEBALL_EQUIPMENT_FEE = 15_000;

    private FieldPricing() {
    }
}
