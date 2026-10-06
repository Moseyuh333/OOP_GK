using SmartSportHub.Web.Dtos;
using SmartSportHub.Web.Services;

namespace SmartSportHub.Web.ViewModels;

public sealed record DashboardModel(int TotalFields, int AvailableFields, int TotalCustomers,
    int TotalBookings, int CompletedBookings, decimal TotalRevenue);

public sealed record PagedList<T>(IReadOnlyList<T> Items, int Page, int PageSize, int TotalCount)
{
    public int TotalPages => Math.Max(1, (int)Math.Ceiling((double)TotalCount / PageSize));
}

public sealed record FieldsModel(PagedList<FieldDto> Results, string? Type, string? Status,
    IReadOnlyList<string> Types, IReadOnlyList<string> Statuses);

public sealed record CustomerRow(CustomerDto Customer, int CompletedBookings, decimal TotalSpending);
public sealed record CustomersModel(IReadOnlyList<CustomerRow> Rows);

public sealed record BookingsModel(PagedList<BookingDto> Results, string? Status, string? FieldType,
    string? CustomerId, DateOnly? Date, IReadOnlyList<string> Statuses,
    IReadOnlyList<string> FieldTypes, IReadOnlyList<CustomerDto> Customers);

public sealed record ServiceRow(ServiceDto Service, int UsageCount);
public sealed record ServicesModel(IReadOnlyList<ServiceRow> Rows);

public sealed record ReportsModel(decimal TotalRevenue, QueryResult RevenueByType,
    QueryResult BookingsByType, QueryResult BookingsByHour, QueryResult MostUsedService,
    QueryResult TopFields, QueryResult TopCustomers);

public sealed record QueriesModel(IReadOnlyList<QueryResult> Queries, QueryResult Selected);
