using SmartSportHub.Web.Services;

namespace SmartSportHub.Web.Tests;

public class DashboardTests
{
    [Fact]
    public void Dashboard_HienThiDuSoVaDoanhThu()
    {
        var data = RealData.Load();
        var dash = new SmartSportHubQueryService().Dashboard(data);

        Assert.Equal(6, dash.TotalFields);
        Assert.Equal(3, dash.AvailableFields); // AVAILABLE: F001, F003, F005
        Assert.Equal(6, dash.TotalCustomers);
        Assert.Equal(12, dash.TotalBookings);
        Assert.Equal(7, dash.CompletedBookings);
        Assert.Equal(2037200, dash.TotalRevenue);
    }

    [Fact]
    public void Dashboard_KhongHardcode_VoiDuLieuMoi()
    {
        // Nếu dataset thay đổi, dashboard phải tự tính lại — không ghi cứng số.
        var data = RealData.Load();
        var dash = new SmartSportHubQueryService().Dashboard(data);

        var revenue = data.Bookings
            .Where(b => b.Status == "COMPLETED")
            .Sum(b => b.TotalAmount);

        Assert.Equal(revenue, dash.TotalRevenue);
    }
}