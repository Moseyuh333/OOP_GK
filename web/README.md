# SmartSportHub Web (Developer B)

ASP.NET Core MVC đọc dữ liệu trực tiếp từ Java Core API. Ứng dụng không có database hoặc bản sao business logic Java. `SmartSportHubQueryService` thực hiện Q01–Q20 bằng LINQ trên `List<T>`/`IEnumerable<T>` sau khi JSON được map sang DTO.

## Chạy

Yêu cầu JDK và .NET 10 SDK. Từ thư mục gốc repository:

```powershell
New-Item -ItemType Directory -Force out | Out-Null
$javaFiles = Get-ChildItem src -Recurse -Filter *.java | ForEach-Object FullName
javac -encoding UTF-8 -d out $javaFiles
java -cp out smartsporthub.api.SmartSportHubApi
```

Ở terminal khác:

```powershell
dotnet run --project web/SmartSportHub.Web/SmartSportHub.Web.csproj
```

Mở địa chỉ HTTP do `dotnet run` in ra. Nếu Java chạy ở cổng khác, chỉnh `SmartSportHubApi:BaseUrl` trong `appsettings.json` hoặc biến môi trường `SmartSportHubApi__BaseUrl`.

Các trang: `/`, `/fields`, `/customers`, `/bookings`, `/services`, `/reports`, `/queries`. Trang sân lọc theo loại và trạng thái; trang booking lọc theo trạng thái, loại sân, khách và ngày. Hai danh sách này phân trang 10 dòng. Q19 minh họa `Skip`/`Take` trên trang 2 của booking hoàn thành.

Doanh thu, chi tiêu khách và lượt dùng dịch vụ chỉ tính booking `COMPLETED`. Q14 tìm booking hoàn thành có tên dịch vụ chứa “vợt”; seed hiện tại không có dịch vụ này nên kết quả rỗng cho đến khi Java cung cấp dữ liệu phù hợp. Giá trị booking và các thành phần phí được nhận nguyên từ Java, không tính lại tại web.

Lưu ý tích hợp: `/api/health.totalRevenue` hiện tính cả booking `CONFIRMED` (3.050.300 ₫ với seed hiện tại), còn Reports/Q08 theo kế hoạch chỉ tính `COMPLETED` (2.037.200 ₫). Web tính trực tiếp từ các booking được Java trả về, theo định nghĩa của từng báo cáo.
