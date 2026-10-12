# KỊCH BẢN QUAY VIDEO BÁO CÁO — SmartSportHub

**Môn học:** OOPR240279 — Lập trình hướng đối tượng (Bài tập lớn MIS)
**Thành viên:** Dev A — Java Core + Q01–Q10 · Dev B — C# Web + Q11–Q20

---

## 0. Chuẩn bị trước khi quay (quan trọng — làm trước, không quay trong lúc chạy lệnh)

Mở **3 cửa sổ PowerShell** tại `D:\New folder\OOP_GK`:

```powershell
# Cửa sổ 1 — Java API (giữ mở)
.\run-video.ps1 -Role Api
#   in ra: SmartSportHub API đang chạy tại http://localhost:18083/api/data

# Cửa sổ 2 — C# Web (giữ mở)
.\run-video.ps1 -Role Web
#   in ra: Now listening on: http://localhost:5080

# Cửa sổ 3 — lệnh rải rác + mở trình duyệt
.\run-video.ps1 -Role Open
```

> Nếu PowerShell chặn script: gõ `Set-ExecutionPolicy -Scope Process Bypass` trước khi chạy.
> `run-video.ps1` tự tìm `dotnet.exe` ở `D:\dev\dotnet` (không cần có trong PATH), tự tìm `java`/`javac`, tự tải driver SQLite nếu thiếu.
> Nếu muốn chạy lại test trước khi quay: `.\run-video.ps1 -Role Build` (113 test Java) và `.\run-video.ps1 -Role Test` (43 test C#).

**Khi quay**, mở sẵn các màn hình (chuyển tab khi trình bày):

```text
http://localhost:5080/                    trang chủ
http://localhost:5080/fields              danh sách sân
http://localhost:5080/bookings            danh sách booking
http://localhost:5080/reports             báo cáo
http://localhost:5080/queries?id=1        truy vấn (đổi id 1..20)
http://localhost:18083/api/health         Java API (json)
```

**Luồng trình bày "chạy Main":** `SmartSportHubApi.main()` (Java) khởi động API ở 18083 → C# Web gọi API → nhận JSON → map sang DTO/List → 20 câu LINQ chạy trên collection trong bộ nhớ → hiển thị ở trang `/queries`.

**Về khảo sát hiện trạng — nói đúng sự thật:**
Nhóm phân tích hiện trạng nghiệp vụ theo **kịch bản mô phỏng** nhà thi đấu Minh Phát (6 sân, 12 lượt đặt trong tháng 2/2026, dịch vụ thuê vợt/dụng cụ/nước/HLV). Không có biên bản phỏng vấn doanh nghiệp thật → dữ liệu là dữ liệu mẫu phục vụ học tập. Đừng nói "đã khảo sát trực tiếp tại doanh nghiệp".

---

# PHẦN DEV A — Java Core, ERD, Q01–Q10

## A1. Mở đầu — giới thiệu đề tài (slide tiêu đề)

> Em xin chào thầy/cô. Em là [TÊN A], phụ trách phần Developer A của nhóm.
> Đề tài nhóm em là **SmartSportHub — hệ thống thông tin quản lý đặt sân thể thao và thanh toán**, thuộc nhóm **MIS — Management Information System**: hệ thống không chỉ lưu giao dịch mà còn xử lý dữ liệu thành thông tin cho người quản lý.

## A2. Vì sao là MIS (mở trang chủ)

> Hệ thống quản lý: khách hàng, sân, dịch vụ, lượt đặt, hóa đơn.
> Từ dữ liệu đó hệ thống hỗ trợ: xem sân trống, chặn đặt trùng lịch, tính phí sân + phí giờ cao điểm + phí dịch vụ + giảm giá, lập hóa đơn, thống kê doanh thu, sân thuê nhiều, khách chi tiêu cao, dịch vụ phổ biến.
> Vì vậy hệ thống có cả phần **vận hành giao dịch** lẫn phần **cung cấp thông tin quản lý** — đúng bản chất MIS.

## A3. Hiện trạng thực tế (slide hiện trạng)

> Nhóm khảo sát hiện trạng theo kịch bản mô phỏng nhà thi đấu Minh Phát: 6 sân (2 bóng đá, 2 cầu lông, 1 tennis, 1 pickleball), 12 lượt khách trong tháng 2/2026 kèm dịch vụ. Nhóm chưa phỏng vấn trực tiếp doanh nghiệp nên số liệu là dữ liệu mẫu.
> Qua hiện trạng, xác định 4 vấn đề của quản lý thủ công:
> - V1: không biết chính xác sân nào đang trống;
> - V2: tính tiền tay dễ sai phí giờ cao điểm (17:00 ≤ bắt đầu < 20:00, mỗi loại sân phí riêng);
> - V3: lịch sử giao dịch rải rác, không lập được báo cáo;
> - V4: không lưu hồ sơ khách → không biết VIP, không áp dụng ưu đãi.
> Từ đó đặt mục tiêu: quản lý sân/khách/booking/hóa đơn, tính tiền tự động, báo cáo quản trị.

## A4. Các bước phân tích để ra ERD (mở `docs/erd.drawio`)

> **Bước 1** — đọc mô tả nghiệp vụ, gạch chân danh từ chính → 5 thực thể gốc: CUSTOMER, SPORTFIELD, SERVICE, BOOKING, INVOICE.
> **Bước 2** — xác định thuộc tính từng thực thể (khách: mã, họ tên, SĐT, email, loại, chiết khấu; sân: mã, tên, loại, giá/giờ, trạng thái; …).
> **Bước 3** — xác định khóa chính/khóa ngoại (BOOKING giữ FK tới khách, sân, dịch vụ; INVOICE liên kết 1–1 với BOOKING).
> **Bước 4** — xác định quan hệ + lực lượng: khách 1–n booking; sân 1–n booking; dịch vụ 1–n booking; booking 1–1 invoice; SPORTFIELD chuyên biệt hóa 4 loại sân.
> **Bước 5** — chuẩn hóa: thông tin chung để ở thực thể gốc; phí riêng (đèn 50k/lượt, vợt 20k + thảm 10k, dụng cụ tennis 40k/lượt, dụng cụ pickleball 15k/giờ) tách bảng chuyên biệt trong ERD.
> **Bước 6** — vẽ bằng draw.io và rà lại khóa + lực lượng quan hệ.

**Nói rõ điểm ERD 9 bảng → SQLite 5 bảng (tránh thầy/cô hỏi):**
> ERD khái niệm tách SPORTFIELD thành 4 bảng chuyên biệt. Khi triển khai SQLite nhóm gộp về bảng `fields` với `field_type` + cột phí tương ứng → database vật lý 5 bảng: customers, fields, services, bookings, invoices. Còn trong Java vẫn giữ 4 lớp con để thể hiện abstraction/inheritance/polymorphism.

## A5. Cấu trúc project (mở cây thư mục)

> `src/smartsporthub/model` — 11 lớp thực thể: Customer (abstract) + StandardCustomer/VipCustomer, SportField (abstract) + FootballField/BadmintonField/TennisField/PickleballField, Service, Booking, Invoice, các enum trạng thái.
> `interfaces` — IPromotional (giảm giá), IPeakHourCalculable (giờ cao điểm).
> `manager` — CustomerManager, FieldManager, ServiceManager, BookingManager, InvoiceManager (thêm/tìm/cập nhật trên List).
> `data` — SeedData + SmartSportHubData (dữ liệu mẫu khởi tạo).
> `dto` — CustomerDto, FieldDto, ServiceDto, BookingDto, InvoiceDto (xuất JSON).
> `json` — JsonWriter.
> `service` — BookingService (đặt/hủy/đổi/hoàn thành/thu tiền/hoàn tiền), BusinessException (lỗi nghiệp vụ → mã HTTP).
> `db` — Database (JDBC + SQLite, WAL), Repository (đọc/ghi, transaction, chặn trùng lịch).
> `api` — SmartSportHubApi (server HTTP + endpoint đọc), WriteApi (endpoint ghi POST/PATCH).
> `test` — 8 bộ kiểm thử (113 phép thử); `build.ps1`/`build.sh` biên dịch + chạy.

## A6. Các bước làm ra project

> 1) Xác định bài toán → 2) hiện trạng + vấn đề → 3) yêu cầu chức năng → 4) thực thể/thuộc tính/khóa → 5) ERD → 6) chuyển ERD sang lớp Java theo OOP → 7) manager + seed → 8) DTO + API JSON → 9) JDBC + SQLite → 10) Dev B: web + LINQ → 11) kiểm thử 2 phía.
> Kiến trúc cuối: `Browser → C# ASP.NET Core → Java API → Java Core + SQLite`. C# không mở DB, chỉ nhận JSON rồi chạy LINQ trên collection trong bộ nhớ.

## A7. Chạy Main (quay tại cửa sổ PowerShell 1)

> Em chạy Main của Java bằng `.\run-video.ps1 -Role Api`. `SmartSportHubApi.main()` mở database, tạo schema nếu cần, nạp dữ liệu mẫu khi DB trống (in ra `customers=6, fields=6, bookings=12, …`) rồi khởi động HTTP API ở cổng 18083.

## A8. Trình bày Q01–Q10 (mở `http://localhost:5080/queries?id=N`, đổi N 1→10)

Mỗi câu nói theo khuôn 3 ý: (1) câu hỏi, (2) toán tử LINQ, (3) ý nghĩa quản lý.

- **Q01 — Sân AVAILABLE rẻ nhất** (`Where · OrderBy · Select`): tìm sân đang mở có giá thấp nhất → nhân viên gợi ý sân rẻ còn trống.
- **Q02 — Giá cao hơn trung bình** (`Average · Where`): sân nào đắt hơn mặt bằng chung → phân khúc giá.
- **Q03 — Số sân theo loại** (`GroupBy · Count`): 2 bóng đá, 2 cầu lông, 1 tennis, 1 pickleball → cơ cấu tài sản.
- **Q04 — Loại sân đắt nhất** (`GroupBy · Average · Max`): loại có giá TB cao nhất, không chỉ nhìn 1 sân.
- **Q05 — Sân đã được thuê** (`GroupJoin · Count`): đếm booking COMPLETED theo sân → sân thực sự sinh doanh thu.
- **Q06 — Sân chưa có lượt hoàn thành** (`Where · Any`): sân chưa từng có booking COMPLETED → sân ít khai thác.
- **Q07 — Sân nhiều giờ thuê nhất** (`GroupBy · Sum · Max · Join`): tổng giờ COMPLETED cao nhất → mức độ sử dụng theo thời lượng.
- **Q08 — Doanh thu theo loại sân** (`Where · GroupBy · Sum`): loại nào đóng góp nhiều doanh thu nhất → báo cáo quản trị.
- **Q09 — Sân vượt doanh thu trung bình** (`GroupJoin · Sum · Average · Where`): kể cả sân doanh thu 0 trong phép tính TB → mặt bằng toàn bộ sân.
- **Q10 — Sân có cả giờ thường và cao điểm** (`GroupBy · Any · All`): từng có cả 2 khung giờ → nhu cầu phân bổ ngày.

## A9. Kết thúc phần A

> Phần của em đã trình bày: bài toán MIS, hiện trạng, phân tích ERD, cấu trúc Java Core, quy trình và Q01–Q10. Điểm chính: nghiệp vụ và dữ liệu tập trung ở Java; C# không tự tính tiền, không mở DB. Em xin chuyển phần cho bạn [TÊN B] với Q11–Q20.

---

# PHẦN DEV B — C# Web, LINQ Q11–Q20

## B1. Mở đầu (tiếp nối)

> Em xin tiếp tục. Em là [TÊN B], phụ trách Developer B — giao diện web, kết nối Java API và 20 truy vấn LINQ. C# dùng dữ liệu từ Java API, không mở SQLite, không dùng Entity Framework thay nghiệp vụ Java.

## B2. Nhắc lại hiện trạng + ERD (ngắn)

> Hiện trạng: nhà thi đấu nhỏ quản lý sân/khách/dịch vụ/booking/hóa đơn thủ công, 4 vấn đề: sân trống khó biết, tính tiền sai phí cao điểm, không báo cáo, không hồ sơ khách.
> ERD: 5 thực thể gốc; quan hệ khách 1–n booking, sân 1–n booking, dịch vụ 1–n booking, booking 1–1 invoice. ERD là cơ sở để Java dựng nghiệp vụ và để C# biết cấu trúc JSON nhận từ API.

## B3. Cấu trúc phần C# (mở cây `web/`)

> `Program.cs` — khởi tạo ASP.NET Core MVC.
> `Controllers/HomeController.cs` — nhận request, gọi API, trả view; thêm 7 action ghi: đặt sân, hủy, hoàn thành, đổi lịch, thu tiền, hoàn tiền, đổi trạng thái sân.
> `Services/SmartSportHubApiClient.cs` — gọi Java API bằng HTTP.
> `Services/SmartSportHubQueryService.cs` — 20 câu LINQ.
> `Dtos/ApiData.cs` — DTO nhận JSON. `ViewModels/PageModels.cs` — dữ liệu cho từng trang.
> `Views/Home` — trang sân/khách/booking/hóa đơn/dịch vụ/báo cáo/truy vấn. `wwwroot` — CSS.
> `SmartSportHub.Web.Tests` — 43 test C#.

## B4. Các bước làm phần C#

> 1) Chốt API contract với Java → 2) DTO theo JSON → 3) ApiClient gọi HTTP → 4) viết Q01–Q20 LINQ trên List trong bộ nhớ → 5) controller + view → 6) form đặt sân + nút thao tác ghi → 7) test + chạy live 2 server.

## B5. Trình bày Q11–Q20 (mở `queries?id=N`, đổi N 11→20)

- **Q11 — Chi tiêu từng khách** (`GroupJoin · DefaultIfEmpty · Count · Sum`): kể cả khách chưa có booking COMPLETED — ví dụ báo cáo có đối tượng chưa phát sinh giao dịch.
- **Q12 — Khách nhiều giờ thuê nhất** (`GroupBy · Sum · Max · Join`): khách sử dụng dịch vụ nhiều nhất → nhận diện khách thân thiết.
- **Q13 — Khách thuê ≥ 2 loại sân** (`GroupBy · Distinct · Count · Join`): cơ sở chính sách chăm sóc khách dùng nhiều nhóm dịch vụ.
- **Q14 — Thuê dụng cụ nhưng chưa đặt tennis** (`Where · Any · All`): nhóm khách tiềm năng để giới thiệu sân tennis.
- **Q15 — Số booking theo giờ bắt đầu** (`Where · GroupBy · Count`): khung giờ cao điểm nhu cầu → điều chỉnh giá/bố trí nhân sự.
- **Q16 — Ngày bận nhất** (`GroupBy · Count · Max · Min`): nhiều ngày cùng số booking cao nhất thì chọn ngày sớm nhất (Min) → kết quả luôn xác định duy nhất.
- **Q17 — Dịch vụ phổ biến nhất** (`SelectMany · GroupBy · Count · Max`): dịch vụ dùng nhiều nhất trong booking COMPLETED.
- **Q18 — Booking vượt mức của chính khách** (`GroupBy · Average · Where · SelectMany`): so với TB của chính khách đó, không phải TB toàn hệ thống — phân tích sâu từng nhóm.
- **Q19 — Trang 2 booking hoàn thành** (`Where · OrderBy · Skip · Take`): Skip(3).Take(3), mỗi trang 3 dòng → phân trang trên collection sau khi nhận từ API.
- **Q20 — Đã đặt nhưng chưa hoàn thành** (`Any · Where`): khách có booking nhưng chưa có booking COMPLETED → nhắc lịch/chăm sóc.

## B6. Kiểm thử và kết luận (mở cửa sổ PowerShell chạy test)

> Kiểm thử: Java 113/113 (`.\run-video.ps1 -Role Build`), C# 43/43 (`.\run-video.ps1 -Role Test`).
> Kiểm chứng live: đặt sân trên web → Java kiểm tra sân/thời gian, tính phí, tạo invoice, lưu SQLite; hủy → CANCELLED + invoice theo nghiệp vụ; sân bảo trì → 409.
> Kết luận: một hệ thống MIS hoàn chỉnh ở mức bài tập lớn — Java: OOP + nghiệp vụ + lưu trữ + API; C#: giao diện + DTO + 20 truy vấn LINQ trên dữ liệu từ API. Kết quả không chỉ là app đặt sân mà còn cung cấp thông tin quản lý: doanh thu, mức độ sử dụng sân, hành vi khách hàng. Nhóm xin cảm ơn thầy/cô.

---

# CÂU HỎI THƯỜNG GẶP (chuẩn bị sẵn)

**"Có khảo sát thực tế không?"** — Nhóm phân tích hiện trạng theo kịch bản mô phỏng nhà thi đấu Minh Phát: tiếp nhận khách, kiểm tra sân, ghi booking, tính phí, dịch vụ, thanh toán. Chưa có phỏng vấn/biên bản doanh nghiệp thật → dữ liệu mẫu phục vụ học tập.

**"Tại sao ERD 9 bảng mà SQLite 5 bảng?"** — ERD là mô hình khái niệm, sân chuyên biệt hóa 4 nhóm. Khi triển khai SQLite gộp về `fields` + `field_type` + cột phí. Lớp con Java vẫn giữ để thể hiện 4 tính chất OOP.

**"20 câu có chạy SQL không?"** — Không. Java dùng SQLite để lưu trữ và cung cấp dữ liệu qua API. C# nhận JSON, 20 câu chạy bằng LINQ trên collection trong bộ nhớ; không EF Core, không SQL thay LINQ.

**"Cổng 8080 hay 18083?"** — Java API mặc định 18083 trong repo (8080 trên máy bị Apache chiếm). Web 5080. Cấu hình tại `appsettings.json` → `SmartSportHubApi:BaseUrl`.
