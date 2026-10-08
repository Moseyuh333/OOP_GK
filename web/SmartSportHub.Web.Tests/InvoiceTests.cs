using SmartSportHub.Web.Services;

namespace SmartSportHub.Web.Tests;

public class InvoiceTests
{
    private readonly SmartSportHubQueryService _service = new();

    private static Dtos.ApiData Data => RealData.Load();

    [Fact]
    public void Invoices_LietKeDayDuVaSapXepTheoMa()
    {
        var model = _service.Invoices(Data, null, null, null, 1, 10);

        Assert.Equal(12, model.TotalCount);
        Assert.Equal(10, model.Rows.Count); // phân trang, trang đầu 10 dòng
        Assert.Equal(2, model.TotalPages);
        Assert.Equal(model.Rows.Select(r => r.Invoice.Id).Order(), model.Rows.Select(r => r.Invoice.Id));
    }

    [Fact]
    public void Invoices_TrangHaiChuaPhanDu()
    {
        var first = _service.Invoices(Data, null, null, null, 1, 10);
        var second = _service.Invoices(Data, null, null, null, 2, 10);

        Assert.Equal(2, second.Rows.Count);
        Assert.Empty(first.Rows.Select(r => r.Invoice.Id)
            .Intersect(second.Rows.Select(r => r.Invoice.Id)));
    }

    [Fact]
    public void Invoices_LocTheoTrangThaiHoaDon()
    {
        var paid = _service.Invoices(Data, "PAID", null, null, 1, 10);
        Assert.Equal(6, paid.TotalCount);
        Assert.All(paid.Rows, r => Assert.Equal("PAID", r.Invoice.Status));

        var refunded = _service.Invoices(Data, "REFUNDED", null, null, 1, 10);
        Assert.Equal(2, refunded.TotalCount);
    }

    [Fact]
    public void Invoices_LocTheoKhachHang()
    {
        var model = _service.Invoices(Data, null, "C001", null, 1, 10);
        Assert.Equal(3, model.TotalCount);
        Assert.All(model.Rows, r => Assert.Equal("C001", r.Invoice.CustomerId));
    }

    [Fact]
    public void Invoices_LocTheoNgayPhatHanh()
    {
        var model = _service.Invoices(Data, null, null, new DateOnly(2026, 2, 15), 1, 10);
        Assert.Equal(1, model.TotalCount);
        Assert.Equal("I001", model.Rows[0].Invoice.Id);
    }

    [Fact]
    public void Invoices_LocKhongCoKetQua_PhaiRongChuKhongPhaiLoi()
    {
        var model = _service.Invoices(Data, "REJECTED", null, null, 1, 10);
        Assert.Equal(0, model.TotalCount);
        Assert.Empty(model.Rows);
        Assert.Equal(1, model.TotalPages);
    }

    [Fact]
    public void Invoices_TongTienTheoTrangThaiKhopDuLieu()
    {
        var model = _service.Invoices(Data, null, null, null, 1, 10);

        decimal Sum(string status) => Data.Invoices
            .Where(i => i.Status == status).Sum(i => i.Amount);

        Assert.Equal(Sum("PAID"), model.Totals.Paid);
        Assert.Equal(Sum("UNPAID"), model.Totals.Unpaid);
        Assert.Equal(Sum("REFUNDED"), model.Totals.Refunded);
        Assert.Equal(1887200, model.Totals.Collected);
    }

    [Fact]
    public void Invoices_TongKhongPhuThuocBoLoc()
    {
        // Lọc theo PAID vẫn phải thấy tổng của toàn bộ hóa đơn, không phải của riêng PAID.
        var all = _service.Invoices(Data, null, null, null, 1, 10);
        var filtered = _service.Invoices(Data, "PAID", null, null, 1, 10);
        Assert.Equal(all.Totals, filtered.Totals);
    }

    [Fact]
    public void Invoices_HoaDonHoanTraPhaiTroVeBookingDaHuy()
    {
        var model = _service.Invoices(Data, "REFUNDED", null, null, 1, 10);
        Assert.NotEmpty(model.Rows);
        Assert.All(model.Rows, r => Assert.Equal("CANCELLED", r.BookingStatus));
    }

    [Fact]
    public void Invoices_NoBookingVaTenKhachVaoBang()
    {
        var model = _service.Invoices(Data, null, null, null, 1, 10);
        Assert.All(model.Rows, row =>
        {
            Assert.NotNull(row.Booking);
            Assert.DoesNotContain("—", row.CustomerName);
            Assert.Equal(row.Booking!.CustomerName, row.CustomerName);
        });
    }

    [Fact]
    public void Invoices_SoTienHoaDonBangTienBooking()
    {
        foreach (var row in _service.Invoices(Data, null, null, null, 1, 10).Rows)
        {
            Assert.Equal(row.Booking!.TotalAmount, row.Invoice.Amount);
        }
    }
}