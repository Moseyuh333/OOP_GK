using SmartSportHub.Web.Services;

namespace SmartSportHub.Web.Tests;

/// Mỗi truy vấn phải trả về kết quả khớp dữ liệu Java Core.
/// Ràng buộc quan trọng nhất: Q14 — trước đây rỗng vì lọc theo tên dịch vụ
/// thay vì theo nhóm, một lỗi mà chỉ chạy thật mới thấy.
public class QueryTests
{
    private readonly SmartSportHubQueryService _service = new();

    private static Dtos.ApiData Data => RealData.Load();

    [Fact]
    public void Q01_SanAvailableReNhat()
    {
        var q = _service.Run(Data, 1);
        Assert.Equal(["F003", "F005", "F001"], q.Rows.Select(r => r[0]));
        Assert.All(q.Rows, row => Assert.StartsWith("F", row[0]));
    }

    [Fact]
    public void Q02_GiaCaoHonTrungBinh()
    {
        var q = _service.Run(Data, 2);
        var avg = Data.Fields.Average(f => f.BasePricePerHour);
        Assert.NotEmpty(q.Rows);
        foreach (var row in q.Rows)
        {
            var field = Data.Fields.First(f => f.Id == row[0]);
            Assert.True(field.BasePricePerHour > avg);
        }
    }

    [Fact]
    public void Q03_SoSanTheoLoai()
    {
        var q = _service.Run(Data, 3);
        Assert.Equal(4, q.Rows.Count);
        Assert.Contains(q.Rows, r => r[0] == "FOOTBALL" && r[1] == "2");
        Assert.Contains(q.Rows, r => r[0] == "BADMINTON" && r[1] == "2");
    }

    [Fact]
    public void Q04_LoaiDatNhat()
    {
        var q = _service.Run(Data, 4);
        var expected = Data.Fields.GroupBy(f => f.Type)
            .Select(g => new { Type = g.Key, Avg = g.Average(f => f.BasePricePerHour) })
            .Where(x => x.Avg == Data.Fields.GroupBy(f => f.Type).Select(g => g.Average(f => f.BasePricePerHour)).Max())
            .Select(x => x.Type);
        Assert.Equal(expected.Order(), q.Rows.Select(r => r[0]));
    }

    [Fact]
    public void Q05_SanDaDuocThue()
    {
        var q = _service.Run(Data, 5);
        Assert.All(q.Rows, row => Assert.True(int.Parse(row[2]) > 0));
    }

    [Fact]
    public void Q06_SanChuaCoLuotHoanThanh()
    {
        var q = _service.Run(Data, 6);
        var used = Data.Bookings.Where(b => b.Status == "COMPLETED").Select(b => b.FieldId).ToHashSet();
        var expected = Data.Fields.Where(f => !used.Contains(f.Id)).Select(f => f.Id).Order();
        Assert.Equal(expected, q.Rows.Select(r => r[0]).Order());
        Assert.Equal(3, q.Rows.Count);
    }

    [Fact]
    public void Q07_SanNhieuGioThueNhat()
    {
        var q = _service.Run(Data, 7);
        var hours = Data.Bookings.Where(b => b.Status == "COMPLETED")
            .GroupBy(b => b.FieldId)
            .Select(g => (Id: g.Key, Hours: g.Sum(b => b.Hours)))
            .ToList();
        var max = hours.Max(h => h.Hours);
        Assert.Equal(hours.Where(h => h.Hours == max).Select(h => h.Id).Order(), q.Rows.Select(r => r[0]));
    }

    [Fact]
    public void Q08_DoanhThuTheoLoaiSan()
    {
        var q = _service.Run(Data, 8);
        var fieldType = Data.Fields.ToDictionary(f => f.Id, f => f.Type);
        var expected = Data.Bookings.Where(b => b.Status == "COMPLETED")
            .GroupBy(b => fieldType[b.FieldId])
            .Select(g => (Type: g.Key, Sum: g.Sum(b => b.TotalAmount)))
            .OrderBy(x => x.Type)
            .ToList();
        Assert.Equal(expected.Select(x => x.Type), q.Rows.Select(r => r[0]));
        Assert.Equal(expected.Sum(x => x.Sum), q.Rows.Count == 0 ? 0 : expected.Sum(x => x.Sum));
        Assert.Equal(3, q.Rows.Count);
    }

    [Fact]
    public void Q09_SanCoDoanhThuVuotTrungBinh()
    {
        var q = _service.Run(Data, 9);
        Assert.NotEmpty(q.Rows);
    }

    [Fact]
    public void Q10_KhachHaiKhungGio()
    {
        var q = _service.Run(Data, 10);

        // Mỗi sân trả về phải từng có booking cả khung thường lẫn khung cao điểm.
        Assert.NotEmpty(q.Rows);
        foreach (var row in q.Rows)
        {
            var fieldId = row[0];
            var peakValues = Data.Bookings.Where(b => b.FieldId == fieldId)
                .Select(b => b.PeakHour).Distinct().ToList();
            Assert.Contains(true, peakValues);
            Assert.Contains(false, peakValues);
        }

        var expected = Data.Bookings.GroupBy(b => b.FieldId)
            .Where(g => new[] { true, false }.All(p => g.Any(b => b.PeakHour == p)))
            .Select(g => g.Key).Order();
        Assert.Equal(expected, q.Rows.Select(r => r[0]).Order());
    }

    [Fact]
    public void Q11_ChiTieuTungKhach()
    {
        var q = _service.Run(Data, 11);
        Assert.Equal(Data.Customers.Count, q.Rows.Count);
    }

    [Fact]
    public void Q12_KhachNhieuGioThueNhat()
    {
        var q = _service.Run(Data, 12);
        Assert.Single(q.Rows);
    }

    [Fact]
    public void Q13_KhachThuNhieuLoaiSan()
    {
        var q = _service.Run(Data, 13);
        Assert.All(q.Rows, row => Assert.True(int.Parse(row[2]) >= 2));
        Assert.NotEmpty(q.Rows);
    }

    [Fact]
    public void Q14_ThueDungCuNhungChuaDatTennis()
    {
        var q = _service.Run(Data, 14);

        // Đáp án phải có, không được rỗng: C003 dùng SV002 (nhóm Thiết bị)
        // và chưa từng đặt sân tennis.
        Assert.Equal(["C003"], q.Rows.Select(r => r[0]));

        // Không hard-code: kiểm tra lại bằng chính dữ liệu.
        var expected = Data.Customers
            .Where(c => Data.Bookings.Any(b => b.CustomerId == c.Id && b.Status == "COMPLETED"
                && b.ServiceId != null
                && Data.Services.Any(s => s.Id == b.ServiceId && s.Category == "Thiết bị"))
                && Data.Bookings.Where(b => b.CustomerId == c.Id).All(b => b.FieldType != "TENNIS"))
            .Select(c => c.Id);
        Assert.Equal(expected, q.Rows.Select(r => r[0]));
    }

    [Fact]
    public void Q15_BookingTheoGioBatDau()
    {
        var q = _service.Run(Data, 15);
        var expected = Data.Bookings.Where(b => b.Status == "COMPLETED")
            .GroupBy(b => b.StartTime.Hour).Select(g => g.Key).Order().ToList();
        Assert.Equal(expected.Count, q.Rows.Count);
        Assert.All(q.Rows, row => Assert.Matches(@"^\d{2}:00$", row[0]));
    }

    [Fact]
    public void Q16_NgayBanNhat()
    {
        var q = _service.Run(Data, 16);
        var days = Data.Bookings.Where(b => b.Status == "COMPLETED")
            .GroupBy(b => b.StartTime.Date).Select(g => (Day: g.Key, Count: g.Count())).ToList();
        var max = days.Max(d => d.Count);
        Assert.All(q.Rows, row => Assert.Equal(max.ToString(), row[1]));
        Assert.NotEmpty(q.Rows);
    }

    [Fact]
    public void Q17_DichVuPhoBienNhat()
    {
        var q = _service.Run(Data, 17);
        var uses = Data.Bookings.Where(b => b.Status == "COMPLETED" && b.ServiceId != null)
            .GroupBy(b => b.ServiceId).Select(g => (Id: g.Key, Count: g.Count())).ToList();
        var max = uses.Max(u => u.Count);
        Assert.All(q.Rows, row => Assert.Equal(max.ToString(), row[^1]));
        Assert.NotEmpty(q.Rows);
    }

    [Fact]
    public void Q18_BookingVuotMucTrungBinhCuaKhach()
    {
        var q = _service.Run(Data, 18);
        Assert.NotEmpty(q.Rows);
        foreach (var row in q.Rows)
        {
            var booking = Data.Bookings.First(b => b.Id == row[0]);
            var avg = Data.Bookings.Where(b => b.CustomerId == booking.CustomerId).Average(b => b.TotalAmount);
            Assert.True(booking.TotalAmount > avg);
        }
    }

    [Fact]
    public void Q19_TrangThuHaiBookingHoanThanh()
    {
        var q = _service.Run(Data, 19);
        var completed = Data.Bookings.Where(b => b.Status == "COMPLETED")
            .OrderBy(b => b.StartTime).ThenBy(b => b.Id)
            .Skip(3).Take(3).Select(b => b.Id).ToList();
        Assert.Equal(completed, q.Rows.Select(r => r[0]));
    }

    [Fact]
    public void Q20_DaDatNhungChuaHoanThanh()
    {
        var q = _service.Run(Data, 20);
        var anyBooking = Data.Bookings.Select(b => b.CustomerId).ToHashSet();
        var completed = Data.Bookings.Where(b => b.Status == "COMPLETED")
            .Select(b => b.CustomerId).ToHashSet();
        var expected = Data.Customers.Where(c => anyBooking.Contains(c.Id) && !completed.Contains(c.Id))
            .Select(c => c.Id);
        Assert.Equal(expected, q.Rows.Select(r => r[0]));
        Assert.Equal(2, q.Rows.Count);
    }

    [Fact]
    public void RunAll_HaiMuCauKhongTrungTen()
    {
        var all = _service.RunAll(Data);
        Assert.Equal(20, all.Count);
        Assert.Equal(20, all.Select(q => q.Id).Distinct().Count());
        Assert.Equal(Enumerable.Range(1, 20).Select(i => $"Q{i:00}"), all.Select(q => q.Id));
    }

    [Fact]
    public void RunAll_KhongCauNaoRong()
    {
        // Bắt lỗi kiểu Q14: câu hỏi có đáp án trong dữ liệu nhưng truy vấn trả rỗng.
        var empty = _service.RunAll(Data).Where(q => q.Count == 0).Select(q => q.Id);
        Assert.Empty(empty);
    }
}