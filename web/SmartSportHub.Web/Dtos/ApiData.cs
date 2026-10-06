namespace SmartSportHub.Web.Dtos;

// Read-only transport models. Java remains the source of all pricing and booking rules.
public sealed class CustomerDto
{
    public string Id { get; set; } = "";
    public string FullName { get; set; } = "";
    public string PhoneNumber { get; set; } = "";
    public string Email { get; set; } = "";
    public string CustomerType { get; set; } = "";
    public decimal DiscountRate { get; set; }
    public string PromotionPolicy { get; set; } = "";
}

public sealed class FieldDto
{
    public string Id { get; set; } = "";
    public string Name { get; set; } = "";
    public string Type { get; set; } = "";
    public string Status { get; set; } = "";
    public decimal BasePricePerHour { get; set; }
}

public sealed class ServiceDto
{
    public string Id { get; set; } = "";
    public string Name { get; set; } = "";
    public string Category { get; set; } = "";
    public decimal UnitPrice { get; set; }
    public bool Active { get; set; }
}

public sealed class BookingDto
{
    public string Id { get; set; } = "";
    public string CustomerId { get; set; } = "";
    public string CustomerName { get; set; } = "";
    public string CustomerType { get; set; } = "";
    public string FieldId { get; set; } = "";
    public string FieldType { get; set; } = "";
    public string FieldName { get; set; } = "";
    public string? ServiceId { get; set; }
    public string? ServiceName { get; set; }
    public DateTime StartTime { get; set; }
    public DateTime EndTime { get; set; }
    public decimal Hours { get; set; }
    public string Status { get; set; } = "";
    public bool PeakHour { get; set; }
    public decimal RentalFee { get; set; }
    public decimal PeakSurcharge { get; set; }
    public decimal ServiceFee { get; set; }
    public decimal Discount { get; set; }
    public decimal TotalAmount { get; set; }
}

public sealed class InvoiceDto
{
    public string Id { get; set; } = "";
    public string BookingId { get; set; } = "";
    public string CustomerId { get; set; } = "";
    public string FieldId { get; set; } = "";
    public DateOnly IssueDate { get; set; }
    public string Status { get; set; } = "";
    public decimal Amount { get; set; }
}

public sealed class ApiData
{
    public List<CustomerDto> Customers { get; set; } = [];
    public List<FieldDto> Fields { get; set; } = [];
    public List<ServiceDto> Services { get; set; } = [];
    public List<BookingDto> Bookings { get; set; } = [];
    public List<InvoiceDto> Invoices { get; set; } = [];
}
