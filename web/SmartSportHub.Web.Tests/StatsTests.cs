using SmartSportHub.Web.Services;

namespace SmartSportHub.Web.Tests;

/// Thống kê đầu trang các danh mục. Ràng buộc quan trọng: số liệu phải tính trên
/// TOÀN BỘ dữ liệu, không phụ thuộc bộ lọc đang xem — nếu không thì lọc rồi
/// tổng nhảy theo và người xem tưởng đó là số của cả hệ thống.
public class StatsTests
{
    private readonly SmartSportHubQueryService _service = new();

    private static Dtos.ApiData Data => RealData.Load();

    [Fact]
    public void Fields_ThongKeDungVaDayDu()
    {
        var stats = _service.Fields(Data, null, null, 1).Stats;

        Assert.Equal(6, stats.Total);
        Assert.Equal(3, stats.Available);
        Assert.Equal(1, stats.Occupied);
        Assert.Equal(2, stats.Maintenance);
        Assert.Equal(150000, stats.HighestPrice);
        Assert.Equal(95833, stats.AveragePrice);
        Assert.Equal(stats.Available + stats.Occupied + stats.Maintenance, stats.Total);
    }

    [Fact]
    public void Fields_LocSanKhongLamDoiThongKe()
    {
        var all = _service.Fields(Data, null, null, 1);
        var filtered = _service.Fields(Data, "BADMINTON", "AVAILABLE", 1);

        Assert.Equal(all.Stats, filtered.Stats);
        Assert.Single(filtered.Results.Items);
        Assert.Equal("F003", filtered.Results.Items[0].Id);
    }

    [Fact]
    public void Customers_PhanBietVipVaStandard()
    {
        var stats = _service.Customers(Data).Stats;

        Assert.Equal(6, stats.Total);
        Assert.Equal(3, stats.Vip);
        Assert.Equal(3, stats.Standard);
        Assert.Equal(stats.Vip + stats.Standard, stats.Total);
    }

    [Fact]
    public void Customers_TongChiKhongTinhBookingChuaHoanThanh()
    {
        var stats = _service.Customers(Data).Stats;

        var expected = Data.Bookings.Where(b => b.Status == "COMPLETED").Sum(b => b.TotalAmount);
        Assert.Equal(expected, stats.TotalSpending);
        Assert.Equal(2037200, stats.TotalSpending);
    }

    [Fact]
    public void Customers_DaTungDatLaKhachCoItNhatMotBooking()
    {
        var stats = _service.Customers(Data).Stats;
        var expected = Data.Bookings.Select(b => b.CustomerId).Distinct().Count();
        Assert.Equal(expected, stats.Active);
    }

    [Fact]
    public void Services_PhanBietDangVaNungCungCap()
    {
        var stats = _service.Services(Data).Stats;

        Assert.Equal(6, stats.Total);
        Assert.Equal(5, stats.Active);
        Assert.Equal(1, stats.Inactive);
        Assert.Equal(stats.Active + stats.Inactive, stats.Total);
    }

    [Fact]
    public void Services_PhiDichVuTinhTrenBookingHoanThanh()
    {
        var stats = _service.Services(Data).Stats;
        var prices = Data.Services.ToDictionary(s => s.Id, s => s.UnitPrice);

        var expected = Data.Bookings
            .Where(b => b.Status == "COMPLETED" && b.ServiceId != null)
            .Sum(b => prices[b.ServiceId!]);

        Assert.Equal(expected, stats.TotalRevenue);
    }

    [Fact]
    public void Services_LuotDungKhopSoBookingHoanThanh()
    {
        var rows = _service.Services(Data).Rows;
        var total = rows.Sum(r => r.UsageCount);
        var withService = Data.Bookings.Count(b => b.Status == "COMPLETED" && b.ServiceId != null);
        Assert.Equal(withService, total);
    }
}