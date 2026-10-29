using System.Globalization;
using SmartSportHub.Web.Dtos;
using SmartSportHub.Web.ViewModels;

namespace SmartSportHub.Web.Services;

/// <summary>
/// Kết quả của một truy vấn LINQ, đã ở dạng sẵn sàng để hiển thị.
/// Nhờ vậy view Razor không cần biết LINQ: chỉ đọc Id, Title, Question, Columns, Rows.
/// Đây là phần của Developer B (C#) -- nghiệp vụ thật nằm ở Java, chỗ này chỉ tổng hợp để hiển thị.
/// </summary>
public sealed record QueryResult(string Id, string Title, string Question, string Concepts,
    string[] Columns, List<string[]> Rows)
{
    /// <summary>Số dòng kết quả, dùng để in "N kết quả" và để test kiểm tra câu trả lời có rỗng không.</summary>
    public int Count => Rows.Count;
}

/// <summary>
/// Lớp chứa 20 truy vấn LINQ (Q01–Q20) và các truy vấn phục vụ từng trang danh mục.
/// Nguyên tắc kiến trúc: C# KHÔNG mở database và KHÔNG tự tính tiền thuê.
/// Lớp này chỉ chạy LINQ trong bộ nhớ trên ApiData mà Java Core đã trả về,
/// nên mọi con số tiền ở đây là số Java đã tính sẵn (TotalAmount, UnitPrice...).
/// </summary>
public sealed class SmartSportHubQueryService
{
    // Culture vi-VN để định dạng số kiểu Việt Nam: 2.037.200 thay vì 2,037,200.
    private static readonly CultureInfo Vi = CultureInfo.GetCultureInfo("vi-VN");
    // Tiền hiển thị dạng "N0" (không phần thập phân) kèm ký hiệu ₫.
    private static string Money(decimal amount) => amount.ToString("N0", Vi) + " ₫";
    // Số lượng (giờ thuê) hiển thị "0.##": bỏ số 0 thừa nhưng vẫn giữ .5 khi cần.
    private static string Number(decimal value) => value.ToString("0.##", Vi);
    // Điều kiện dùng lại rất nhiều lần: chỉ booking COMPLETED mới được tính doanh thu/giờ thuê.
    // Tách thành hàm nhỏ để 20 truy vấn không phải viết lại chuỗi "COMPLETED".
    private static bool Completed(BookingDto booking) => booking.Status == "COMPLETED";

    /// <summary>Tổng doanh thu = cộng TotalAmount của mọi booking COMPLETED (tiền do Java tính).</summary>
    // LINQ: Where lọc theo điều kiện, Sum cộng một thuộc tính của các phần tử còn lại.
    public decimal TotalRevenue(ApiData data) =>
        data.Bookings.Where(Completed).Sum(b => b.TotalAmount);

    /// <summary>
    /// Gom 6 con số cho trang Tổng quan: tổng sân, sân đang mở, khách, booking, booking hoàn thành, doanh thu.
    /// Count() đếm tất cả, Count(điều kiện) đếm số phần tử thoả điều kiện.
    /// </summary>
    public DashboardModel Dashboard(ApiData d) => new(
        d.Fields.Count, d.Fields.Count(f => f.Status == "AVAILABLE"), d.Customers.Count,
        d.Bookings.Count, d.Bookings.Count(Completed), TotalRevenue(d));

    /// <summary>
    /// Cắt một danh sách thành đúng một trang để hiển thị.
    /// LINQ: Skip(n) bỏ n phần tử đầu, Take(m) lấy m phần tử tiếp theo -- đây là cặp toán tử chuẩn để phân trang.
    /// </summary>
    private static PagedList<T> Page<T>(IEnumerable<T> source, int page, int pageSize)
    {
        // ToList() để chỉ duyệt nguồn một lần: cần biết tổng số dòng trước khi cắt trang.
        var list = source.ToList();
        // Kẹp số trang vào khoảng hợp lệ: người dùng gõ page=99 hay page=-1 đều không làm vỡ trang.
        var validPage = Math.Clamp(page, 1, Math.Max(1, (int)Math.Ceiling((double)list.Count / pageSize)));
        return new(list.Skip((validPage - 1) * pageSize).Take(pageSize).ToList(),
            validPage, pageSize, list.Count);
    }

    /// <summary>
    /// Dữ liệu trang Danh sách sân: lọc theo loại/trạng thái, phân trang, kèm thống kê đầu trang.
    /// </summary>
    public FieldsModel Fields(ApiData d, string? type, string? status, int page)
    {
        // LINQ: Where giữ lại phần tử thoả biểu thức; điều kiện rỗng (null) được coi là "không lọc".
        // OrderBy sắp tăng dần theo Id để thứ tự hiển thị ổn định giữa các lần tải.
        var filtered = d.Fields.Where(f => (string.IsNullOrEmpty(type) || f.Type == type) &&
                    (string.IsNullOrEmpty(status) || f.Status == status)).OrderBy(f => f.Id);

                // Thống kê tính trên toàn bộ sân, không phụ thuộc bộ lọc đang xem.
                var stats = new FieldStats(d.Fields.Count,
                    d.Fields.Count(f => f.Status == "AVAILABLE"),
                    d.Fields.Count(f => f.Status == "OCCUPIED"),
                    d.Fields.Count(f => f.Status == "MAINTENANCE"),
                    // Average/Max tính trên toàn bộ danh sách sân; Math.Round(..., 0) làm tròn về đồng.
                    Math.Round(d.Fields.Average(f => f.BasePricePerHour), 0),
                    d.Fields.Max(f => f.BasePricePerHour));

                return new(Page(filtered, page, 10), type, status,
                    // Distinct() bỏ giá trị trùng để đổ vào ô chọn lọc; OrderBy cho danh sách chọn luôn cùng thứ tự.
                    d.Fields.Select(f => f.Type).Distinct().OrderBy(x => x).ToList(),
                    d.Fields.Select(f => f.Status).Distinct().OrderBy(x => x).ToList(),
                    stats);
    }

    /// <summary>
    /// Dữ liệu trang Khách hàng: mỗi khách kèm số booking hoàn thành và tổng chi.
    /// </summary>
    public CustomersModel Customers(ApiData d)
        {
            var rows = d.Customers
                // LINQ: GroupJoin nối hai tập theo khoá nhưng giữ nguyên vế trái, mỗi khách nhận về
                // một tập booking con (kể cả rỗng) -- nhờ vậy khách chưa đặt lần nào vẫn xuất hiện.
                // Chỉ nối với booking COMPLETED nên số lượt và tổng chi đều là số đã hoàn thành.
                .GroupJoin(d.Bookings.Where(Completed), c => c.Id, b => b.CustomerId,
                    (c, bookings) => new CustomerRow(c, bookings.Count(), bookings.Sum(b => b.TotalAmount)))
                .OrderBy(x => x.Customer.Id).ToList();

            // "Có đặt" = khách đã có ít nhất một booking, không phân biệt trạng thái.
            // ToHashSet() tra cứu nhanh hơn List khi chỉ cần hỏi "có hay không".
            var booked = d.Bookings.Select(b => b.CustomerId).ToHashSet();
            var stats = new CustomerStats(d.Customers.Count,
                d.Customers.Count(c => c.CustomerType == "VIP"),
                d.Customers.Count(c => c.CustomerType == "STANDARD"),
                d.Customers.Count(c => booked.Contains(c.Id)),
                // Tổng chi của cả hệ thống = cộng tổng chi từng khách vừa tính ở trên.
                rows.Sum(r => r.TotalSpending));

            return new(rows, stats);
        }

    /// <summary>
    /// Dữ liệu trang Booking: lọc theo trạng thái/loại sân/khách/ngày, phân trang,
    /// kèm các danh sách cần cho ô chọn và form đặt sân mới.
    /// </summary>
    public BookingsModel Bookings(ApiData d, string? status, string? fieldType,
        string? customerId, DateOnly? date, int page)
    {
        // Bốn điều kiện lọc độc lập, mỗi điều kiện bỏ qua khi tham số rỗng.
        // OrderBy + ThenBy: sắp theo giờ bắt đầu, cùng giờ thì theo Id cho thứ tự xác định.
        var filtered = d.Bookings.Where(b =>
                (string.IsNullOrEmpty(status) || b.Status == status) &&
                (string.IsNullOrEmpty(fieldType) || b.FieldType == fieldType) &&
                (string.IsNullOrEmpty(customerId) || b.CustomerId == customerId) &&
                // So sánh theo ngày (bỏ phần giờ) để lọc "booking trong ngày X" đúng ý người dùng.
                (date is null || DateOnly.FromDateTime(b.StartTime) == date))
            .OrderBy(b => b.StartTime).ThenBy(b => b.Id);
        return new(Page(filtered, page, 10), status, fieldType, customerId, date,
                    d.Bookings.Select(b => b.Status).Distinct().OrderBy(x => x).ToList(),
                    d.Fields.Select(f => f.Type).Distinct().OrderBy(x => x).ToList(),
                    d.Customers.OrderBy(c => c.FullName).ToList(),
                    // Chỉ sân AVAILABLE được đưa vào form đặt sân; sân bảo trì/đang dùng không cho chọn.
                    d.Fields.Where(f => f.Status == "AVAILABLE").OrderBy(f => f.Name).ToList(),
                    d.Services.Where(s => s.Active).OrderBy(s => s.Name).ToList());
    }

    /// <summary>
    /// Dữ liệu trang Hóa đơn: lọc, phân trang, ghép thông tin booking/khách và cộng tổng theo trạng thái.
    /// </summary>
    public InvoicesModel Invoices(ApiData d, string? status, string? customerId,
        DateOnly? date, int page, int pageSize)
    {
        // Giữ toàn bộ hóa đơn (đã sắp theo mã) để phần tổng tiền bên dưới tính trên tất cả, không theo bộ lọc.
        var all = d.Invoices.OrderBy(i => i.Id).ToList();
        var filtered = all.Where(i =>
                (string.IsNullOrEmpty(status) || i.Status == status) &&
                (string.IsNullOrEmpty(customerId) || i.CustomerId == customerId) &&
                (date is null || i.IssueDate == date))
            .OrderBy(i => i.Id).ToList();

        // Cắt trang trước rồi mới ghép dữ liệu: chỉ xử lý 10 dòng đang hiển thị thay vì cả bảng.
        var pageList = Page(filtered, page, pageSize);
        // ToDictionary tạo bảng tra theo Id -- tra cứu O(1) thay vì First() lặp lại cho từng dòng.
        var bookings = d.Bookings.ToDictionary(b => b.Id);
        var customers = d.Customers.ToDictionary(c => c.Id);

        // Ghép mỗi hóa đơn với booking và khách của nó; GetValueOrDefault trả null nếu thiếu,
        // khi đó dùng ?? để hiện giá trị dự phòng (mã khách, mã sân, dấu gạch) thay vì làm vỡ trang.
        var rows = pageList.Items.Select(i => new InvoiceRow(i,
                bookings.GetValueOrDefault(i.BookingId),
                customers.GetValueOrDefault(i.CustomerId)?.FullName ?? i.CustomerId,
                bookings.GetValueOrDefault(i.BookingId)?.FieldName ?? i.FieldId,
                bookings.GetValueOrDefault(i.BookingId)?.Status ?? "—")).ToList();

        // Tổng số tiền theo trạng thái: PAID = đã thu, UNPAID = còn phải thu,
        // REFUNDED = đã hoàn trả. Tính trên toàn bộ hóa đơn, không lọc theo bộ lọc đang xem.
        // Ba phép Sum riêng cho PAID/UNPAID/REFUNDED, đều chạy trên `all` chứ không trên `filtered`.
        var totals = new InvoiceTotals(
            all.Where(i => i.Status == "PAID").Sum(i => i.Amount),
            all.Where(i => i.Status == "UNPAID").Sum(i => i.Amount),
            all.Where(i => i.Status == "REFUNDED").Sum(i => i.Amount));

        return new(rows, totals, status, customerId, date, pageList.Page, pageList.PageSize,
            pageList.TotalCount,
            all.Select(i => i.Status).Distinct().OrderBy(x => x).ToList(),
            d.Customers.OrderBy(c => c.FullName).ToList());
    }

    /// <summary>
    /// Dữ liệu trang Dịch vụ: mỗi dịch vụ kèm số lượt được dùng trong booking hoàn thành,
    /// và tổng phí dịch vụ đã thu.
    /// </summary>
    public ServicesModel Services(ApiData d)
        {
            // GroupJoin: giữ đủ mọi dịch vụ, kể cả dịch vụ chưa ai dùng (Count = 0).
            var rows = d.Services
                .GroupJoin(d.Bookings.Where(Completed), s => s.Id, b => b.ServiceId,
                    (s, bookings) => new ServiceRow(s, bookings.Count()))
                .OrderBy(x => x.Service.Id).ToList();

            // Doanh thu phát sinh từ phí dịch vụ: mỗi booking COMPLETED gánh 1 dịch vụ.
            // Bảng tra giá dịch vụ để cộng phí; ContainsKey chặn trường hợp booking trỏ tới dịch vụ đã bị xóa.
            var services = d.Services.ToDictionary(s => s.Id);
            // Lưu ý: đây là cộng UnitPrice của dịch vụ, KHÔNG phải tính lại ServiceFee --
            // phí thật đã được Java tính trong booking; chỗ này chỉ tổng hợp để hiển thị.
            var revenue = d.Bookings.Where(Completed).Where(b => b.ServiceId != null
                    && services.ContainsKey(b.ServiceId))
                .Sum(b => services[b.ServiceId].UnitPrice);

            var stats = new ServiceStats(d.Services.Count,
                d.Services.Count(s => s.Active),
                d.Services.Count(s => !s.Active),
                revenue);

            return new(rows, stats);
        }

    /// <summary>
    /// Dữ liệu trang Báo cáo: gom sáu bảng tổng hợp, mỗi bảng là một QueryResult dùng lại
    /// các truy vấn đã có (Q05, Q08, Q15, Q17) cộng thêm bảng xếp hạng khách chi tiêu nhiều nhất.
    /// </summary>
    public ReportsModel Reports(ApiData d)
    {
        // GroupBy gom các phần tử có cùng khoá thành từng nhóm; sau đó Count() đếm số phần tử mỗi nhóm.
        // Đây là truy vấn đầu tiên của nhóm GroupBy nên xem kỹ: kết quả là IEnumerable<IGrouping<K, T>>.
        var byType = d.Bookings.Where(Completed).GroupBy(b => b.FieldType)
            .OrderBy(g => g.Key).Select(g => new[] { g.Key, g.Count().ToString() });
        var bookingCount = new QueryResult("R01", "Booking theo loại sân",
            "Số booking COMPLETED theo loại sân", "Where · GroupBy · Count",
            ["Loại sân", "Số booking"], byType.ToList());
        var topCustomers = new QueryResult("R02", "Khách hàng chi tiêu nhiều nhất",
            "Xếp hạng khách theo tổng giá trị booking COMPLETED", "GroupJoin · Sum · OrderByDescending",
            ["Mã", "Khách hàng", "Booking hoàn thành", "Tổng chi"],
            // Dùng lại kết quả trang Khách hàng rồi sắp giảm dần theo tổng chi; hoà thì xếp theo mã cho ổn định.
            Customers(d).Rows.OrderByDescending(x => x.TotalSpending).ThenBy(x => x.Customer.Id)
                .Select(x => new[] { x.Customer.Id, x.Customer.FullName,
                    x.CompletedBookings.ToString(), Money(x.TotalSpending) }).ToList());
        return new(TotalRevenue(d), Q08(d), bookingCount, Q15(d), Q17(d), Q05(d), topCustomers);
    }

    /// <summary>Chạy lần lượt Q01..Q20 để trang LINQ Lab có đủ 20 bảng.</summary>
    public IReadOnlyList<QueryResult> RunAll(ApiData data) =>
        Enumerable.Range(1, 20).Select(id => Run(data, id)).ToList();

    /// <summary>Chạy một truy vấn theo số hiệu 1..20; số ngoài khoảng thì báo lỗi rõ ràng.</summary>
    // switch expression: ánh xạ số hiệu sang hàm tương ứng, gọn hơn chuỗi if/else.
    public QueryResult Run(ApiData data, int number) => number switch
    {
        1 => Q01(data), 2 => Q02(data), 3 => Q03(data), 4 => Q04(data),
        5 => Q05(data), 6 => Q06(data), 7 => Q07(data), 8 => Q08(data),
        9 => Q09(data), 10 => Q10(data), 11 => Q11(data), 12 => Q12(data),
        13 => Q13(data), 14 => Q14(data), 15 => Q15(data), 16 => Q16(data),
        17 => Q17(data), 18 => Q18(data), 19 => Q19(data), 20 => Q20(data),
        _ => throw new ArgumentOutOfRangeException(nameof(number))
    };

    /// <summary>
    /// Hàm phụ tạo QueryResult, tự sinh mã dạng Q01..Q20 và duyệt kết quả thành List một lần.
    /// </summary>
    private static QueryResult Result(int id, string title, string question, string concepts,
        string[] columns, IEnumerable<string[]> rows) =>
        new($"Q{id:00}", title, question, concepts, columns, rows.ToList());

    /// <summary>
    /// Q01: sân đang mở xếp theo giá thuê tăng dần.
    /// LINQ: Where (lọc trạng thái) · OrderBy/ThenBy (sắp giá, hoà thì theo mã) · Select (chiếu sang mảng chuỗi để view in).
    /// </summary>
    private static QueryResult Q01(ApiData d) => Result(1, "Sân đang mở theo giá",
        "Sân AVAILABLE nào có giá thuê thấp nhất?", "Where · OrderBy · Select",
        ["Mã", "Sân", "Loại", "Giá/giờ"],
        d.Fields.Where(f => f.Status == "AVAILABLE")
            .OrderBy(f => f.BasePricePerHour).ThenBy(f => f.Id)
            .Select(f => new[] { f.Id, f.Name, f.Type, Money(f.BasePricePerHour) }));

    /// <summary>
    /// Q02: sân có giá cao hơn mức trung bình của toàn bộ sân.
    /// Average tính trung bình trước, rồi Where so từng sân với mốc đó (không thể so trong một câu Where).
    /// </summary>
    private static QueryResult Q02(ApiData d)
    {
        // Chặn chia cho 0: Average trên danh sách rỗng sẽ ném InvalidOperationException.
        var average = d.Fields.Count == 0 ? 0 : d.Fields.Average(f => f.BasePricePerHour);
        return Result(2, "Giá cao hơn trung bình", "Sân nào có giá cao hơn mức trung bình của tất cả sân?",
            "Average · Where", ["Mã", "Sân", "Giá/giờ", "Giá trung bình"],
            d.Fields.Where(f => f.BasePricePerHour > average)
                .OrderByDescending(f => f.BasePricePerHour)
                .Select(f => new[] { f.Id, f.Name, Money(f.BasePricePerHour), Money(average) }));
    }

    /// <summary>Q03: số sân theo từng loại. LINQ: GroupBy + Count.</summary>
    private static QueryResult Q03(ApiData d) => Result(3, "Số sân theo loại",
        "Mỗi loại có bao nhiêu sân?", "GroupBy · Count", ["Loại sân", "Số sân"],
        d.Fields.GroupBy(f => f.Type).OrderBy(g => g.Key)
            .Select(g => new[] { g.Key, g.Count().ToString() }));

    /// <summary>
    /// Q04: loại sân có giá trung bình cao nhất (có thể nhiều loại cùng đạt nếu bằng điểm).
    /// Cách làm: tính trung bình từng loại, tìm mức cao nhất, rồi giữ các loại đạt mức đó.
    /// DefaultIfEmpty() để Max() không ném lỗi khi không có dữ liệu.
    /// </summary>
    private static QueryResult Q04(ApiData d) => Result(4, "Loại sân đắt nhất",
        "Loại sân nào có giá thuê trung bình cao nhất?", "GroupBy · Average · Max",
        ["Loại sân", "Giá trung bình"],
        d.Fields.GroupBy(f => f.Type)
            .Select(g => new { Type = g.Key, Average = g.Average(f => f.BasePricePerHour) })
            .Where(x => x.Average == d.Fields.GroupBy(f => f.Type)
                .Select(g => g.Average(f => f.BasePricePerHour)).DefaultIfEmpty().Max())
            .OrderBy(x => x.Type).Select(x => new[] { x.Type, Money(x.Average) }));

    /// <summary>
    /// Q05: số lượt thuê hoàn thành của từng sân, sân nhiều lượt đứng trước.
    /// LINQ: GroupJoin nối sân với booking COMPLETED, Where loại sân chưa có lượt nào.
    /// </summary>
    private static QueryResult Q05(ApiData d) => Result(5, "Sân đã được thuê",
        "Sân nào có booking hoàn thành và được thuê bao nhiêu lần?", "GroupJoin · Count",
        ["Mã", "Sân", "Lượt hoàn thành"],
        d.Fields.GroupJoin(d.Bookings.Where(Completed), f => f.Id, b => b.FieldId,
                (f, bookings) => new { Field = f, Count = bookings.Count() })
            .Where(x => x.Count > 0).OrderByDescending(x => x.Count).ThenBy(x => x.Field.Id)
            .Select(x => new[] { x.Field.Id, x.Field.Name, x.Count.ToString() }));

    /// <summary>
    /// Q06: sân chưa từng có booking hoàn thành.
    /// LINQ: Any trả true nếu tồn tại phần tử thoả điều kiện; phủ định Any nghĩa là "không có cái nào".
    /// </summary>
    private static QueryResult Q06(ApiData d) => Result(6, "Sân chưa có lượt hoàn thành",
        "Sân nào chưa từng có booking COMPLETED?", "Where · Any",
        ["Mã", "Sân", "Loại"], d.Fields.Where(f => !d.Bookings.Any(b => Completed(b) && b.FieldId == f.Id))
            .Select(f => new[] { f.Id, f.Name, f.Type }));

    /// <summary>
    /// Q07: sân có tổng giờ thuê COMPLETED lớn nhất.
    /// GroupBy + Sum cộng giờ theo sân, Max tìm mức cao nhất, Join ghép lại tên sân.
    /// </summary>
    private static QueryResult Q07(ApiData d)
    {
        var totals = d.Bookings.Where(Completed).GroupBy(b => b.FieldId)
            .Select(g => new { Id = g.Key, Hours = g.Sum(b => b.Hours) }).ToList();
        // DefaultIfEmpty() trả về một phần tử 0 khi danh sách rỗng, nhờ đó Max() không ném lỗi.
        var max = totals.Select(x => x.Hours).DefaultIfEmpty().Max();
        return Result(7, "Sân có nhiều giờ thuê nhất", "Sân nào có tổng giờ COMPLETED cao nhất?",
            "GroupBy · Sum · Max · Join", ["Mã", "Sân", "Tổng giờ"],
            totals.Where(x => x.Hours == max).Join(d.Fields, x => x.Id, f => f.Id,
                (x, f) => new[] { f.Id, f.Name, Number(x.Hours) }));
    }

    /// <summary>
    /// Q08: doanh thu theo loại sân (chỉ booking COMPLETED).
    /// TotalAmount là tiền Java đã tính sẵn; C# chỉ cộng nhóm, không tự nhân giá.
    /// </summary>
    private static QueryResult Q08(ApiData d) => Result(8, "Doanh thu theo loại sân",
        "Booking COMPLETED mang lại bao nhiêu doanh thu theo loại sân?", "Where · GroupBy · Sum",
        ["Loại sân", "Doanh thu"], d.Bookings.Where(Completed).GroupBy(b => b.FieldType)
            .OrderBy(g => g.Key).Select(g => new[] { g.Key, Money(g.Sum(b => b.TotalAmount)) }));

    /// <summary>
    /// Q09: sân có doanh thu cao hơn trung bình của tất cả sân.
    /// Điểm cần chú ý: GroupJoin giữ cả sân chưa có doanh thu (Amount = 0), nên mẫu số của Average
    /// là TẤT CẢ sân -- đúng như câu hỏi nghiệp vụ đặt ra.
    /// </summary>
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

    /// <summary>
    /// Q10: sân từng được đặt ở cả khung giờ thường lẫn khung cao điểm.
    /// LINQ: GroupBy theo sân, Any kiểm tra có ít nhất một booking khớp từng giá trị PeakHour,
    /// All yêu cầu cả hai giá trị (true và false) đều phải có.
    /// </summary>
    private static QueryResult Q10(ApiData d) => Result(10, "Sân có cả hai khung giờ",
        "Sân nào từng được đặt cả giờ thường và cao điểm?", "GroupBy · Any · All",
        ["Mã", "Sân"], d.Bookings.GroupBy(b => b.FieldId)
            .Where(g => new[] { true, false }.All(peak => g.Any(b => b.PeakHour == peak)))
            .Join(d.Fields, g => g.Key, f => f.Id, (g, f) => new[] { f.Id, f.Name }));

    /// <summary>
    /// Q11: mỗi khách có bao nhiêu lượt hoàn thành và chi bao nhiêu, kể cả khách chưa đặt lần nào.
    /// LINQ: GroupJoin lấy tập booking con, SelectMany + DefaultIfEmpty làm phẳng thành từng cặp
    /// (khách, booking) và vẫn giữ khách không có booking với booking = null, GroupBy gom lại theo khách.
    /// </summary>
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

    /// <summary>Q12: khách có tổng giờ thuê COMPLETED cao nhất. GroupBy + Sum + Max + Join.</summary>
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

    /// <summary>
    /// Q13: khách đã hoàn thành lượt thuê ở ít nhất hai loại sân.
    /// Distinct() đếm số loại sân khác nhau của từng khách, rồi lọc những khách đạt từ 2 loại.
    /// </summary>
    private static QueryResult Q13(ApiData d) => Result(13, "Khách thuê nhiều loại sân",
        "Khách nào đã hoàn thành lượt thuê ở ít nhất hai loại sân?", "GroupBy · Distinct · Count · Join",
        ["Mã", "Khách hàng", "Số loại"], d.Bookings.Where(Completed).GroupBy(b => b.CustomerId)
            .Select(g => new { Id = g.Key, Types = g.Select(b => b.FieldType).Distinct().Count() })
            .Where(x => x.Types >= 2).Join(d.Customers, x => x.Id, c => c.Id,
                (x, c) => new[] { c.Id, c.FullName, x.Types.ToString() }));

    /// <summary>
    /// Q14: khách đã dùng dịch vụ nhóm "Thiết bị" trong booking COMPLETED nhưng chưa từng đặt sân tennis.
    /// Điều kiện 1: tồn tại booking hoàn thành có dịch vụ thuộc nhóm Thiết bị (tra qua d.Services).
    /// Điều kiện 2: mọi booking của khách đều không phải TENNIS (All trên tập con của khách đó).
    /// Lỗi cũ của câu này là lọc theo tên dịch vụ thay vì theo nhóm, khiến kết quả luôn rỗng.
    /// </summary>
    private static QueryResult Q14(ApiData d) => Result(14, "Thuê dụng cụ nhưng chưa đặt tennis",
            "Khách nào đã dùng dịch vụ thuộc nhóm Thiết bị trong booking COMPLETED nhưng chưa từng đặt sân tennis?",
            "Where · Any · All", ["Mã", "Khách hàng"],
            d.Customers.Where(c => d.Bookings.Any(b => b.CustomerId == c.Id && Completed(b) &&
                    b.ServiceId != null &&
                    d.Services.Any(s => s.Id == b.ServiceId && s.Category == "Thiết bị"))
                && d.Bookings.Where(b => b.CustomerId == c.Id).All(b => b.FieldType != "TENNIS"))
                .Select(c => new[] { c.Id, c.FullName }));

    /// <summary>Q15: số booking COMPLETED theo giờ bắt đầu. GroupBy theo StartTime.Hour rồi Count.</summary>
    private static QueryResult Q15(ApiData d) => Result(15, "Booking theo giờ bắt đầu",
        "Có bao nhiêu booking COMPLETED bắt đầu ở mỗi giờ?", "Where · GroupBy · Count",
        ["Giờ", "Số booking"], d.Bookings.Where(Completed).GroupBy(b => b.StartTime.Hour)
            .OrderBy(g => g.Key).Select(g => new[] { $"{g.Key:00}:00", g.Count().ToString() }));

    /// <summary>
    /// Q16: ngày có nhiều booking COMPLETED nhất; nếu nhiều ngày bằng điểm thì lấy ngày sớm nhất.
    /// GroupBy theo ngày (StartTime.Date), Count mỗi ngày, Max tìm mức cao nhất, Min chọn ngày sớm nhất.
    /// </summary>
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

    /// <summary>
    /// Q17: dịch vụ được dùng nhiều nhất trong các booking COMPLETED.
    /// LINQ: SelectMany làm phẳng -- mỗi booking có dịch vụ sinh ra một dòng dịch vụ,
    /// sau đó GroupBy theo dịch vụ và Count số lần xuất hiện.
    /// </summary>
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

    /// <summary>
    /// Q18: booking có giá trị cao hơn mức trung bình booking của chính khách đó.
    /// Mỗi nhóm khách tự tính Average của mình, nên ngưỡng so sánh khác nhau giữa các khách.
    /// </summary>
    private static QueryResult Q18(ApiData d) => Result(18, "Booking vượt mức của khách",
        "Booking nào có giá trị cao hơn trung bình booking của chính khách đó?", "GroupBy · Average · Where · SelectMany",
        ["Booking", "Khách hàng", "Giá trị", "Trung bình khách"],
        d.Bookings.GroupBy(b => b.CustomerId)
            .SelectMany(g => g.Where(b => b.TotalAmount > g.Average(x => x.TotalAmount))
                .Select(b => new[] { b.Id, b.CustomerName, Money(b.TotalAmount), Money(g.Average(x => x.TotalAmount)) }))
            .OrderBy(row => row[0]));

    /// <summary>
    /// Q19: trang 2 của danh sách booking COMPLETED, mỗi trang 3 dòng.
    /// Skip(3).Take(3) = bỏ 3 dòng đầu rồi lấy 3 dòng tiếp theo, tức đúng trang thứ hai.
    /// </summary>
    private static QueryResult Q19(ApiData d) => Result(19, "Trang 2 booking hoàn thành",
        "Trang 2, mỗi trang 3 booking COMPLETED theo thời gian bắt đầu có những booking nào?",
        "Where · OrderBy · Skip · Take", ["Booking", "Khách hàng", "Sân", "Bắt đầu"],
        d.Bookings.Where(Completed).OrderBy(b => b.StartTime).ThenBy(b => b.Id)
            .Skip(3).Take(3)
            .Select(b => new[] { b.Id, b.CustomerName, b.FieldName, b.StartTime.ToString("dd/MM/yyyy HH:mm") }));

    /// <summary>
    /// Q20: khách đã từng đặt nhưng chưa có booking nào hoàn thành.
    /// Any thứ nhất: có ít nhất một booking. Phủ định Any thứ hai: không có booking COMPLETED nào.
    /// </summary>
    private static QueryResult Q20(ApiData d) => Result(20, "Đã đặt nhưng chưa hoàn thành",
        "Khách nào đã từng booking nhưng chưa có booking COMPLETED?", "Any · Where",
        ["Mã", "Khách hàng"], d.Customers
            .Where(c => d.Bookings.Any(b => b.CustomerId == c.Id) &&
                !d.Bookings.Any(b => b.CustomerId == c.Id && Completed(b)))
            .Select(c => new[] { c.Id, c.FullName }));
}
