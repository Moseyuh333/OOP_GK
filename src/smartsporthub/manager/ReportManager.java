package smartsporthub.manager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import smartsporthub.model.Booking;
import smartsporthub.model.BookingStatus;
import smartsporthub.model.SportField;

/** Báo cáo đơn giản từ dữ liệu booking và danh sách sân của A. */
public class ReportManager {

    private final FieldManager fieldManager;
    private final BookingManager bookingManager;

    public ReportManager(FieldManager fieldManager, BookingManager bookingManager) {
        if (fieldManager == null || bookingManager == null) {
            throw new IllegalArgumentException("Các manager không được null.");
        }
        this.fieldManager = fieldManager;
        this.bookingManager = bookingManager;
    }

    public double calculateTotalRevenue() {
        double revenue = 0;
        for (Booking booking : bookingManager.getAllBookings()) {
            if (booking.getStatus() == BookingStatus.COMPLETED) {
                revenue += booking.getInvoice().getTotal();
            }
        }
        if (!Double.isFinite(revenue)) {
            throw new IllegalArgumentException("Tổng doanh thu vượt giới hạn.");
        }
        return revenue;
    }

    /** Đếm booking chưa hủy; hòa số lượt thì xếp tên loại sân tăng dần. */
    public Map<String, Integer> getTop3FieldTypes() {
        Map<String, Integer> counts = new TreeMap<>();
        for (Booking booking : bookingManager.getAllBookings()) {
            if (booking.getStatus() != BookingStatus.CANCELLED) {
                String type = booking.getField().getClass().getSimpleName();
                if (type.endsWith("Field")) {
                    type = type.substring(0, type.length() - "Field".length());
                }
                Integer count = counts.get(type);
                counts.put(type, count == null ? 1 : count + 1);
            }
        }
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(counts.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> first, Map.Entry<String, Integer> second) {
                int countOrder = Integer.compare(second.getValue(), first.getValue());
                return countOrder != 0 ? countOrder : first.getKey().compareTo(second.getKey());
            }
        });
        Map<String, Integer> result = new LinkedHashMap<>();
        for (int index = 0; index < entries.size() && index < 3; index++) {
            Map.Entry<String, Integer> entry = entries.get(index);
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public List<SportField> findAvailableFields(LocalDateTime start, LocalDateTime end) {
        Booking.validateTimeRange(start, end);
        List<SportField> result = new ArrayList<>();
        for (SportField field : fieldManager.findAvailableFields()) {
            if (bookingManager.isTimeSlotAvailable(field.getFieldId(), start, end)) {
                result.add(field);
            }
        }
        return result;
    }
}
