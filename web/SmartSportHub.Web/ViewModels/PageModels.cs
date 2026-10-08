using SmartSportHub.Web.Dtos;
using SmartSportHub.Web.Services;

namespace SmartSportHub.Web.ViewModels;

public sealed record DashboardModel(int TotalFields, int AvailableFields, int TotalCustomers,
    int TotalBookings, int CompletedBookings, decimal TotalRevenue);

public sealed record PagedList<T>(IReadOnlyList<T> Items, int Page, int PageSize, int TotalCount)
{
    public int TotalPages => Math.Max(1, (int)Math.Ceiling((double)TotalCount / PageSize));
}

public sealed record FieldStats(int Total, int Available, int Occupied, int Maintenance,
    decimal AveragePrice, decimal HighestPrice);

public sealed record FieldsModel(PagedList<FieldDto> Results, string? Type, string? Status,
    IReadOnlyList<string> Types, IReadOnlyList<string> Statuses, FieldStats Stats);

public sealed record CustomerRow(CustomerDto Customer, int CompletedBookings, decimal TotalSpending);
public sealed record CustomerStats(int Total, int Vip, int Standard, int Active, decimal TotalSpending);
public sealed record CustomersModel(IReadOnlyList<CustomerRow> Rows, CustomerStats Stats);

public sealed record BookingsModel(PagedList<BookingDto> Results, string? Status, string? FieldType,
    string? CustomerId, DateOnly? Date, IReadOnlyList<string> Statuses,
    IReadOnlyList<string> FieldTypes, IReadOnlyList<CustomerDto> Customers);

public sealed record ServiceRow(ServiceDto Service, int UsageCount);
public sealed record ServiceStats(int Total, int Active, int Inactive, decimal TotalRevenue);
public sealed record ServicesModel(IReadOnlyList<ServiceRow> Rows, ServiceStats Stats);

public sealed record InvoiceRow(InvoiceDto Invoice, BookingDto? Booking, string CustomerName,
    string FieldName, string BookingStatus);
public sealed record InvoiceTotals(decimal Paid, decimal Unpaid, decimal Refunded)
{
    public decimal Collected => Paid;
    public decimal Outstanding => Unpaid;
    public decimal Returned => Refunded;
}

public sealed record InvoicesModel(IReadOnlyList<InvoiceRow> Rows, InvoiceTotals Totals,
    string? Status, string? CustomerId, DateOnly? Date, int Page, int PageSize, int TotalCount,
    IReadOnlyList<string> Statuses, IReadOnlyList<CustomerDto> Customers)
{
    public int TotalPages => Math.Max(1, (int)Math.Ceiling((double)TotalCount / (double)PageSize));
}

public sealed record ReportsModel(decimal TotalRevenue, QueryResult RevenueByType,
    QueryResult BookingsByType, QueryResult BookingsByHour, QueryResult MostUsedService,
    QueryResult TopFields, QueryResult TopCustomers);

public sealed record QueriesModel(IReadOnlyList<QueryResult> Queries, QueryResult Selected);
