using System.Globalization;
using SmartSportHub.Web.Dtos;
using SmartSportHub.Web.ViewModels;

namespace SmartSportHub.Web.Services;

public sealed record QueryResult(string Id, string Title, string Question, string Concepts,
    string[] Columns, List<string[]> Rows)
{
    public int Count => Rows.Count;
}

public sealed class SmartSportHubQueryService
{
    private static readonly CultureInfo Vi = CultureInfo.GetCultureInfo("vi-VN");
    private static string Money(decimal amount) => amount.ToString("N0", Vi) + " ₫";
    private static string Number(decimal value) => value.ToString("0.##", Vi);
    private static bool Completed(BookingDto booking) => booking.Status == "COMPLETED";

    public decimal TotalRevenue(ApiData data) =>
        data.Bookings.Where(Completed).Sum(b => b.TotalAmount);

    public DashboardModel Dashboard(ApiData d) => new(
        d.Fields.Count, d.Fields.Count(f => f.Status == "AVAILABLE"), d.Customers.Count,
        d.Bookings.Count, d.Bookings.Count(Completed), TotalRevenue(d));

    private static PagedList<T> Page<T>(IEnumerable<T> source, int page, int pageSize)
    {
        var list = source.ToList();
        var validPage = Math.Clamp(page, 1, Math.Max(1, (int)Math.Ceiling((double)list.Count / pageSize)));
        return new(list.Skip((validPage - 1) * pageSize).Take(pageSize).ToList(),
            validPage, pageSize, list.Count);
    }

    public FieldsModel Fields(ApiData d, string? type, string? status, int page)
    {
        var filtered = d.Fields.Where(f => (string.IsNullOrEmpty(type) || f.Type == type) &&
            (string.IsNullOrEmpty(status) || f.Status == status)).OrderBy(f => f.Id);
        return new(Page(filtered, page, 10), type, status,
            d.Fields.Select(f => f.Type).Distinct().OrderBy(x => x).ToList(),
            d.Fields.Select(f => f.Status).Distinct().OrderBy(x => x).ToList());
    }

    public CustomersModel Customers(ApiData d) => new(d.Customers
        .GroupJoin(d.Bookings.Where(Completed), c => c.Id, b => b.CustomerId,
            (c, bookings) => new CustomerRow(c, bookings.Count(), bookings.Sum(b => b.TotalAmount)))
        .OrderBy(x => x.Customer.Id).ToList());

    public BookingsModel Bookings(ApiData d, string? status, string? fieldType,
        string? customerId, DateOnly? date, int page)
    {
        var filtered = d.Bookings.Where(b =>
                (string.IsNullOrEmpty(status) || b.Status == status) &&
                (string.IsNullOrEmpty(fieldType) || b.FieldType == fieldType) &&
                (string.IsNullOrEmpty(customerId) || b.CustomerId == customerId) &&
                (date is null || DateOnly.FromDateTime(b.StartTime) == date))
            .OrderBy(b => b.StartTime).ThenBy(b => b.Id);
        return new(Page(filtered, page, 10), status, fieldType, customerId, date,
            d.Bookings.Select(b => b.Status).Distinct().OrderBy(x => x).ToList(),
            d.Fields.Select(f => f.Type).Distinct().OrderBy(x => x).ToList(),
            d.Customers.OrderBy(c => c.FullName).ToList());
    }

    public ServicesModel Services(ApiData d) => new(d.Services
        .GroupJoin(d.Bookings.Where(Completed), s => s.Id, b => b.ServiceId,
            (s, bookings) => new ServiceRow(s, bookings.Count()))
        .OrderBy(x => x.Service.Id).ToList());

    public ReportsModel Reports(ApiData d)
    {
        var byType = d.Bookings.Where(Completed).GroupBy(b => b.FieldType)
            .OrderBy(g => g.Key).Select(g => new[] { g.Key, g.Count().ToString() });
        var bookingCount = new QueryResult("R01", "Booking theo loại sân",
            "Số booking COMPLETED theo loại sân", "Where · GroupBy · Count",
            ["Loại sân", "Số booking"], byType.ToList());
        var topCustomers = new QueryResult("R02", "Khách hàng chi tiêu nhiều nhất",
            "Xếp hạng khách theo tổng giá trị booking COMPLETED", "GroupJoin · Sum · OrderByDescending",
            ["Mã", "Khách hàng", "Booking hoàn thành", "Tổng chi"],
            Customers(d).Rows.OrderByDescending(x => x.TotalSpending).ThenBy(x => x.Customer.Id)
                .Select(x => new[] { x.Customer.Id, x.Customer.FullName,
                    x.CompletedBookings.ToString(), Money(x.TotalSpending) }).ToList());
        return new(TotalRevenue(d), Q08(d), bookingCount, Q15(d), Q17(d), Q05(d), topCustomers);
    }

    public IReadOnlyList<QueryResult> RunAll(ApiData data) =>
        Enumerable.Range(1, 20).Select(id => Run(data, id)).ToList();

    public QueryResult Run(ApiData data, int number) => number switch
    {
        1 => Q01(data), 2 => Q02(data), 3 => Q03(data), 4 => Q04(data),
        5 => Q05(data), 6 => Q06(data), 7 => Q07(data), 8 => Q08(data),
        9 => Q09(data), 10 => Q10(data), 11 => Q11(data), 12 => Q12(data),
        13 => Q13(data), 14 => Q14(data), 15 => Q15(data), 16 => Q16(data),
        17 => Q17(data), 18 => Q18(data), 19 => Q19(data), 20 => Q20(data),
        _ => throw new ArgumentOutOfRangeException(nameof(number))
    };

    private static QueryResult Result(int id, string title, string question, string concepts,
        string[] columns, IEnumerable<string[]> rows) =>
        new($"Q{id:00}", title, question, concepts, columns, rows.ToList());

    private static QueryResult Q01(ApiData d) => Result(1, "Sân đang mở theo giá",
        "Sân AVAILABLE nào có giá thuê thấp nhất?", "Where · OrderBy · Select",
        ["Mã", "Sân", "Loại", "Giá/giờ"],
        d.Fields.Where(f => f.Status == "AVAILABLE")
            .OrderBy(f => f.BasePricePerHour).ThenBy(f => f.Id)
            .Select(f => new[] { f.Id, f.Name, f.Type, Money(f.BasePricePerHour) }));

    private static QueryResult Q02(ApiData d)
    {
        var average = d.Fields.Count == 0 ? 0 : d.Fields.Average(f => f.BasePricePerHour);
        return Result(2, "Giá cao hơn trung bình", "Sân nào có giá cao hơn mức trung bình của tất cả sân?",
            "Average · Where", ["Mã", "Sân", "Giá/giờ", "Giá trung bình"],
            d.Fields.Where(f => f.BasePricePerHour > average)
                .OrderByDescending(f => f.BasePricePerHour)
                .Select(f => new[] { f.Id, f.Name, Money(f.BasePricePerHour), Money(average) }));
    }

    private static QueryResult Q03(ApiData d) => Result(3, "Số sân theo loại",
        "Mỗi loại có bao nhiêu sân?", "GroupBy · Count", ["Loại sân", "Số sân"],
        d.Fields.GroupBy(f => f.Type).OrderBy(g => g.Key)
            .Select(g => new[] { g.Key, g.Count().ToString() }));

    private static QueryResult Q04(ApiData d) => Result(4, "Loại sân đắt nhất",
        "Loại sân nào có giá thuê trung bình cao nhất?", "GroupBy · Average · Max",
        ["Loại sân", "Giá trung bình"],
        d.Fields.GroupBy(f => f.Type)
            .Select(g => new { Type = g.Key, Average = g.Average(f => f.BasePricePerHour) })
            .Where(x => x.Average == d.Fields.GroupBy(f => f.Type)
                .Select(g => g.Average(f => f.BasePricePerHour)).DefaultIfEmpty().Max())
            .OrderBy(x => x.Type).Select(x => new[] { x.Type, Money(x.Average) }));

    private static QueryResult Q05(ApiData d) => Result(5, "Sân đã được thuê",
        "Sân nào có booking hoàn thành và được thuê bao nhiêu lần?", "GroupJoin · Count",
        ["Mã", "Sân", "Lượt hoàn thành"],
        d.Fields.GroupJoin(d.Bookings.Where(Completed), f => f.Id, b => b.FieldId,
                (f, bookings) => new { Field = f, Count = bookings.Count() })
            .Where(x => x.Count > 0).OrderByDescending(x => x.Count).ThenBy(x => x.Field.Id)
            .Select(x => new[] { x.Field.Id, x.Field.Name, x.Count.ToString() }));

    private static QueryResult Q06(ApiData d) => Result(6, "Sân chưa có lượt hoàn thành",
        "Sân nào chưa từng có booking COMPLETED?", "Where · Any",
        ["Mã", "Sân", "Loại"], d.Fields.Where(f => !d.Bookings.Any(b => Completed(b) && b.FieldId == f.Id))
            .Select(f => new[] { f.Id, f.Name, f.Type }));

    private static QueryResult Q07(ApiData d)
    {
        var totals = d.Bookings.Where(Completed).GroupBy(b => b.FieldId)
            .Select(g => new { Id = g.Key, Hours = g.Sum(b => b.Hours) }).ToList();
        var max = totals.Select(x => x.Hours).DefaultIfEmpty().Max();
        return Result(7, "Sân có nhiều giờ thuê nhất", "Sân nào có tổng giờ COMPLETED cao nhất?",
            "GroupBy · Sum · Max · Join", ["Mã", "Sân", "Tổng giờ"],
            totals.Where(x => x.Hours == max).Join(d.Fields, x => x.Id, f => f.Id,
                (x, f) => new[] { f.Id, f.Name, Number(x.Hours) }));
    }

    private static QueryResult Q08(ApiData d) => Result(8, "Doanh thu theo loại sân",
        "Booking COMPLETED mang lại bao nhiêu doanh thu theo loại sân?", "Where · GroupBy · Sum",
        ["Loại sân", "Doanh thu"], d.Bookings.Where(Completed).GroupBy(b => b.FieldType)
            .OrderBy(g => g.Key).Select(g => new[] { g.Key, Money(g.Sum(b => b.TotalAmount)) }));

    private static QueryResult Q09(ApiData d)
    {
        var revenue = d.Fields.GroupJoin(d.Bookings.Where(Completed), f => f.Id, b => b.FieldId,
            (f, bookings) => new { Field = f, Amount = bookings.Sum(b => b.TotalAmount) }).ToList();
        var average = revenue.Count == 0 ? 0 : revenue.Average(x => x.Amount);
        return Result(9, "Sân có doanh thu vượt trung bình",
            "Sân nào có doanh thu COMPLETED cao hơn trung bình của tất cả sân, kể cả sân chưa có doanh thu?",
            "GroupJoin · Sum · Average · Where", ["Mã", "Sân", "Doanh thu", "Trung bình"],
            revenue.Where(x => x.Amount > average).OrderByDescending(x => x.Amount)
                .Select(x => new[] { x.Field.Id, x.Field.Name, Money(x.Amount), Money(average) }));
    }

    private static QueryResult Q10(ApiData d) => Result(10, "Sân có cả hai khung giờ",
        "Sân nào từng được đặt cả giờ thường và cao điểm?", "GroupBy · Any · All",
        ["Mã", "Sân"], d.Bookings.GroupBy(b => b.FieldId)
            .Where(g => new[] { true, false }.All(peak => g.Any(b => b.PeakHour == peak)))
            .Join(d.Fields, g => g.Key, f => f.Id, (g, f) => new[] { f.Id, f.Name }));

    private static QueryResult Q11(ApiData d) => Result(11, "Chi tiêu từng khách hàng",
        "Mỗi khách có bao nhiêu lượt COMPLETED và chi tiêu bao nhiêu, kể cả khách chưa đặt?",
        "GroupJoin · DefaultIfEmpty · Count · Sum", ["Mã", "Khách hàng", "Lượt hoàn thành", "Tổng chi"],
        d.Customers.GroupJoin(d.Bookings.Where(Completed), c => c.Id, b => b.CustomerId,
                (c, bookings) => new { Customer = c, Bookings = bookings })
            .SelectMany(x => x.Bookings.DefaultIfEmpty(), (x, booking) => new { x.Customer, Booking = booking })
            .GroupBy(x => x.Customer.Id)
            .Select(g => new[] { g.Key, g.First().Customer.FullName,
                g.Count(x => x.Booking is not null).ToString(),
                Money(g.Sum(x => x.Booking?.TotalAmount ?? 0)) }));

    private static QueryResult Q12(ApiData d)
    {
        var totals = d.Bookings.Where(Completed).GroupBy(b => b.CustomerId)
            .Select(g => new { Id = g.Key, Hours = g.Sum(b => b.Hours) }).ToList();
        var max = totals.Select(x => x.Hours).DefaultIfEmpty().Max();
        return Result(12, "Khách có nhiều giờ thuê nhất", "Khách nào có tổng giờ COMPLETED cao nhất?",
            "GroupBy · Sum · Max · Join", ["Mã", "Khách hàng", "Tổng giờ"],
            totals.Where(x => x.Hours == max).Join(d.Customers, x => x.Id, c => c.Id,
                (x, c) => new[] { c.Id, c.FullName, Number(x.Hours) }));
    }

    private static QueryResult Q13(ApiData d) => Result(13, "Khách thuê nhiều loại sân",
        "Khách nào đã hoàn thành lượt thuê ở ít nhất hai loại sân?", "GroupBy · Distinct · Count · Join",
        ["Mã", "Khách hàng", "Số loại"], d.Bookings.Where(Completed).GroupBy(b => b.CustomerId)
            .Select(g => new { Id = g.Key, Types = g.Select(b => b.FieldType).Distinct().Count() })
            .Where(x => x.Types >= 2).Join(d.Customers, x => x.Id, c => c.Id,
                (x, c) => new[] { c.Id, c.FullName, x.Types.ToString() }));

    private static QueryResult Q14(ApiData d) => Result(14, "Thuê dụng cụ nhưng chưa đặt tennis",
            "Khách nào đã dùng dịch vụ thuộc nhóm Thiết bị trong booking COMPLETED nhưng chưa từng đặt sân tennis?",
            "Where · Any · All", ["Mã", "Khách hàng"],
            d.Customers.Where(c => d.Bookings.Any(b => b.CustomerId == c.Id && Completed(b) &&
                    b.ServiceId != null &&
                    d.Services.Any(s => s.Id == b.ServiceId && s.Category == "Thiết bị"))
                && d.Bookings.Where(b => b.CustomerId == c.Id).All(b => b.FieldType != "TENNIS"))
                .Select(c => new[] { c.Id, c.FullName }));

    private static QueryResult Q15(ApiData d) => Result(15, "Booking theo giờ bắt đầu",
        "Có bao nhiêu booking COMPLETED bắt đầu ở mỗi giờ?", "Where · GroupBy · Count",
        ["Giờ", "Số booking"], d.Bookings.Where(Completed).GroupBy(b => b.StartTime.Hour)
            .OrderBy(g => g.Key).Select(g => new[] { $"{g.Key:00}:00", g.Count().ToString() }));

    private static QueryResult Q16(ApiData d)
    {
        var daily = d.Bookings.Where(Completed).GroupBy(b => b.StartTime.Date)
            .Select(g => new { Day = g.Key, Count = g.Count() }).ToList();
        var max = daily.Select(x => x.Count).DefaultIfEmpty().Max();
        var earliestBusiestDay = daily.Where(x => x.Count == max).Select(x => x.Day)
            .DefaultIfEmpty().Min();
        return Result(16, "Ngày bận nhất", "Ngày nào có nhiều booking COMPLETED nhất?",
            "GroupBy · Count · Max · Min", ["Ngày", "Số booking"],
            daily.Where(x => x.Count == max && x.Day == earliestBusiestDay)
                .Select(x => new[] { x.Day.ToString("dd/MM/yyyy"), x.Count.ToString() }));
    }

    private static QueryResult Q17(ApiData d)
    {
        var uses = d.Bookings.Where(Completed).Where(b => b.ServiceId is not null)
            .SelectMany(b => d.Services.Where(s => s.Id == b.ServiceId))
            .GroupBy(s => s.Id).Select(g => new { Service = g.First(), Count = g.Count() }).ToList();
        var max = uses.Select(x => x.Count).DefaultIfEmpty().Max();
        return Result(17, "Dịch vụ phổ biến nhất", "Dịch vụ nào được dùng nhiều nhất trong booking COMPLETED?",
            "SelectMany · GroupBy · Count · Max", ["Mã", "Dịch vụ", "Lượt dùng"],
            uses.Where(x => x.Count == max).OrderBy(x => x.Service.Id)
                .Select(x => new[] { x.Service.Id, x.Service.Name, x.Count.ToString() }));
    }

    private static QueryResult Q18(ApiData d) => Result(18, "Booking vượt mức của khách",
        "Booking nào có giá trị cao hơn trung bình booking của chính khách đó?", "GroupBy · Average · Where · SelectMany",
        ["Booking", "Khách hàng", "Giá trị", "Trung bình khách"],
        d.Bookings.GroupBy(b => b.CustomerId)
            .SelectMany(g => g.Where(b => b.TotalAmount > g.Average(x => x.TotalAmount))
                .Select(b => new[] { b.Id, b.CustomerName, Money(b.TotalAmount), Money(g.Average(x => x.TotalAmount)) }))
            .OrderBy(row => row[0]));

    private static QueryResult Q19(ApiData d) => Result(19, "Trang 2 booking hoàn thành",
        "Trang 2, mỗi trang 3 booking COMPLETED theo thời gian bắt đầu có những booking nào?",
        "Where · OrderBy · Skip · Take", ["Booking", "Khách hàng", "Sân", "Bắt đầu"],
        d.Bookings.Where(Completed).OrderBy(b => b.StartTime).ThenBy(b => b.Id)
            .Skip(3).Take(3)
            .Select(b => new[] { b.Id, b.CustomerName, b.FieldName, b.StartTime.ToString("dd/MM/yyyy HH:mm") }));

    private static QueryResult Q20(ApiData d) => Result(20, "Đã đặt nhưng chưa hoàn thành",
        "Khách nào đã từng booking nhưng chưa có booking COMPLETED?", "Any · Where",
        ["Mã", "Khách hàng"], d.Customers
            .Where(c => d.Bookings.Any(b => b.CustomerId == c.Id) &&
                !d.Bookings.Any(b => b.CustomerId == c.Id && Completed(b)))
            .Select(c => new[] { c.Id, c.FullName }));
}
