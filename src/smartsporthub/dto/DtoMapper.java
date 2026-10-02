package smartsporthub.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import smartsporthub.model.Booking;
import smartsporthub.model.Customer;
import smartsporthub.model.Invoice;
import smartsporthub.model.Service;
import smartsporthub.model.SportField;

/**
 * Chuyển đổi tập trung từ domain object sang DTO.
 * <p>
 * Đặt ở tầng DTO để không phải đụng tới package {@code model} đã hoàn thành từ Part A.
 */
public final class DtoMapper {

    private DtoMapper() {
        // Utility class
    }

    public static CustomerDto toDto(Customer customer) {
        return new CustomerDto(customer);
    }

    public static FieldDto toDto(SportField field) {
        return new FieldDto(field);
    }

    public static ServiceDto toDto(Service service) {
        return new ServiceDto(service);
    }

    public static BookingDto toDto(Booking booking) {
        return new BookingDto(booking);
    }

    public static InvoiceDto toDto(Invoice invoice) {
        return new InvoiceDto(invoice);
    }

    /** Chuyển danh sách DTO thành mảng map sẵn sàng cho {@code JsonWriter}. */
    public static List<Map<String, Object>> toMaps(List<? extends JsonDto> dtos) {
        return dtos.stream().map(JsonDto::toMap).toList();
    }

    /** Gom toàn bộ dữ liệu thành một map cho endpoint {@code /api/data}. */
    public static Map<String, Object> snapshot(List<Customer> customers,
            List<SportField> fields,
            List<Service> services,
            List<Booking> bookings,
            List<Invoice> invoices) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("customers", customers.stream().map(DtoMapper::toDto).map(CustomerDto::toMap).toList());
        root.put("fields", fields.stream().map(DtoMapper::toDto).map(FieldDto::toMap).toList());
        root.put("services", services.stream().map(DtoMapper::toDto).map(ServiceDto::toMap).toList());
        root.put("bookings", bookings.stream().map(DtoMapper::toDto).map(BookingDto::toMap).toList());
        root.put("invoices", invoices.stream().map(DtoMapper::toDto).map(InvoiceDto::toMap).toList());
        return root;
    }
}