package smartsporthub.dto;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import smartsporthub.model.Booking;
import smartsporthub.model.Customer;
import smartsporthub.model.Service;

/**
 * DTO phẳng cho booking.
 * <p>
 * Sẵn sàng phẳng hoá luôn tên khách, tên sân và tên dịch vụ để phía C# không cần
 * Join mới hiển thị được danh sách, nhưng vẫn giữ cả khóa {@code customerId}/{@code fieldId}/{@code serviceId}
 * cho các truy vấn LINQ.
 */
public final class BookingDto implements JsonDto {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final String id;
    private final String customerId;
    private final String customerName;
    private final String customerType;
    private final String fieldId;
    private final String fieldType;
    private final String fieldName;
    private final String serviceId;
    private final String serviceName;
    private final String startTime;
    private final String endTime;
    private final double hours;
    private final String status;
    private final boolean peakHour;
    private final double rentalFee;
    private final double peakSurcharge;
    private final double serviceFee;
    private final double discount;
    private final double totalAmount;

    public BookingDto(Booking booking) {
        Customer customer = booking.getCustomer();
        Service service = booking.getService();

        id = booking.getBookingId();
        customerId = customer.getCustomerId();
        customerName = customer.getFullName();
        customerType = customer.getCustomerType().name();
        fieldId = booking.getField().getFieldId();
        fieldType = FieldType.of(booking.getField()).name();
        fieldName = booking.getField().getFieldName();
        serviceId = service == null ? null : service.getServiceId();
        serviceName = service == null ? null : service.getServiceName();
        startTime = booking.getStartTime().format(TIME_FORMAT);
        endTime = booking.getEndTime().format(TIME_FORMAT);
        hours = booking.getHours();
        status = booking.getStatus().name();
        peakHour = booking.isPeakHourBooking();
        rentalFee = booking.getRentalFee();
        peakSurcharge = booking.getPeakSurcharge();
        serviceFee = booking.getServiceFee();
        discount = booking.getDiscount();
        totalAmount = booking.getTotalAmount();
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("customerId", customerId);
        map.put("customerName", customerName);
        map.put("customerType", customerType);
        map.put("fieldId", fieldId);
        map.put("fieldType", fieldType);
        map.put("fieldName", fieldName);
        map.put("serviceId", serviceId);
        map.put("serviceName", serviceName);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("hours", hours);
        map.put("status", status);
        map.put("peakHour", peakHour);
        map.put("rentalFee", rentalFee);
        map.put("peakSurcharge", peakSurcharge);
        map.put("serviceFee", serviceFee);
        map.put("discount", discount);
        map.put("totalAmount", totalAmount);
        return map;
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerType() {
        return customerType;
    }

    public String getFieldId() {
        return fieldId;
    }

    public String getFieldType() {
        return fieldType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public double getHours() {
        return hours;
    }

    public String getStatus() {
        return status;
    }

    public boolean isPeakHour() {
        return peakHour;
    }

    public double getRentalFee() {
        return rentalFee;
    }

    public double getPeakSurcharge() {
        return peakSurcharge;
    }

    public double getServiceFee() {
        return serviceFee;
    }

    public double getDiscount() {
        return discount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}