package smartsporthub.interfaces;

import java.time.LocalTime;

import smartsporthub.model.FieldPricing;

/** Khả năng kiểm tra giờ cao điểm và tính phụ phí tương ứng. */
public interface IPeakHourCalculable {

    double calculatePeakHourSurcharge(double hours);

    /** Khoảng cao điểm là [17:00, 20:00). */
    default boolean isPeakHour(LocalTime time) {
        return time != null
                && !time.isBefore(FieldPricing.PEAK_START)
                && time.isBefore(FieldPricing.PEAK_END);
    }
}
