# SmartSportHub Web (Developer B)

ASP.NET Core MVC đọc dữ liệu trực tiếp từ Java Core API. Ứng dụng không có database hoặc bản sao business logic Java. `SmartSportHubQueryService` thực hiện Q01–Q20 bằng LINQ trên `List<T>`/`IEnumerable<T>` sau khi JSON được map sang DTO.

## Chạy

Yêu cầu JDK (với `lib/sqlite-jdbc-3.46.1.3.jar`) và .NET SDK. Trên Windows dùng PowerShell, không cần WSL/Git Bash — dùng script `run-video.ps1` ở thư mục gốc repository:

```powershell
cd "D:\New folder\OOP_GK"

# 1. Terminal thứ nhất: chạy Java API (keep cửa sổ này mở)
.\run-video.ps1 -Role Api

# 2. Terminal thứ hai: chạy C# web (keep cửa sổ này mở)
.\run-video.ps1 -Role Web

# 3. Mở trình duyệt
.\run-video.ps1 -Role Open
```

Script tự tìm `D:\dev\dotnet\dotnet.exe` khi dotnet không có trong PATH. Muốn chỉ chạy Java test thì dùng `-Role Build`.

Java API chạy mặc định ở `http://localhost:18083` (cổng `8080` có thể đã bị chiếm bởi Apache nên repo chọn 18083), web chạy ở `http://localhost:5080` — mở địa chỉ này để dùng. Nếu Java chạy ở cổng khác, chỉnh `SmartSportHubApi:BaseUrl` trong `appsettings.json` hoặc biến môi trường `SmartSportHubApi__BaseUrl`.

Các trang: `/`, `/fields`, `/customers`, `/bookings`, `/services`, `/reports`, `/queries`. Trang sân lọc theo loại và trạng thái; trang booking lọc theo trạng thái, loại sân, khách và ngày. Hai danh sách này phân trang 10 dòng. Q19 minh họa `Skip`/`Take` trên trang 2 của booking hoàn thành.

Doanh thu, chi tiêu khách và lượt dùng dịch vụ chỉ tính booking `COMPLETED`. Q14 tìm booking hoàn thành có tên dịch vụ chứa “vợt”; seed hiện tại không có dịch vụ này nên kết quả rỗng cho đến khi Java cung cấp dữ liệu phù hợp. Giá trị booking và các thành phần phí được nhận nguyên từ Java, không tính lại tại web.

Lưu ý tích hợp: `/api/health.totalRevenue` hiện tính cả booking `CONFIRMED` (3.050.300 ₫ với seed hiện tại), còn Reports/Q08 theo kế hoạch chỉ tính `COMPLETED` (2.037.200 ₫). Web tính trực tiếp từ các booking được Java trả về, theo định nghĩa của từng báo cáo.
