package smartsporthub.dto;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import smartsporthub.model.Invoice;

/** DTO phẳng cho hóa đơn. */
public final class InvoiceDto implements JsonDto {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final String id;
    private final String bookingId;
    private final String customerId;
    private final String fieldId;
    private final String issueDate;
    private final String status;
    private final double amount;

    public InvoiceDto(Invoice invoice) {
        id = invoice.getInvoiceId();
        bookingId = invoice.getBookingId();
        customerId = invoice.getCustomerId();
        fieldId = invoice.getFieldId();
        issueDate = invoice.getIssueDate().format(DATE_FORMAT);
        status = invoice.getStatus().name();
        amount = invoice.getAmount();
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("bookingId", bookingId);
        map.put("customerId", customerId);
        map.put("fieldId", fieldId);
        map.put("issueDate", issueDate);
        map.put("status", status);
        map.put("amount", amount);
        return map;
    }

    public String getId() {
        return id;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getFieldId() {
        return fieldId;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public String getStatus() {
        return status;
    }

    public double getAmount() {
        return amount;
    }
}