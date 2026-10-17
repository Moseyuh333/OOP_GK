# BÁO CÁO ĐỒ ÁN NHÓM
## HỆ THỐNG QUẢN LÝ ĐẶT SÂN THỂ THAO — SMARTSPORTHUB

**Môn học:** OOPR240279 — Lập trình hướng đối tượng
**Bài tập lớn:** Đồ án nhóm — Phần MIS (Hệ thống thông tin quản lý)
**Nhóm:** 2 thành viên — Developer A (Java Core) · Developer B (C# ASP.NET Core)
**Repository:** https://github.com/Moseyuh333/OOP_GK
**Ngôn ngữ:** Java 22 · C# ASP.NET Core MVC (.NET 10) · SQLite · LINQ
**Kiểm thử:** Java 113/113 · C# 43/43

---

## MỤC LỤC

1. [Tổng quan hệ thống](#1-tổng-quan-hệ-thống)
2. [Phát biểu vấn đề và hiện trạng](#2-phát-biểu-vấn-đề-và-hiện-trạng)
3. [Xác định các thực thể và thuộc tính](#3-xác-định-các-thực-thể-và-thuộc-tính)
4. [Xác định mối liên kết và xây dựng ERD](#4-xác-định-mối-liên-kết-và-xây-dựng-erd)
5. [Chuyển quan hệ ER/RR thành các lớp Java](#5-chuyển-quan-hệ-errr-thành-các-lớp-java)
6. [Dữ liệu mẫu và enum](#6-dữ-liệu-mẫu-và-enum)
7. [Giao diện dữ liệu cho Developer B](#7-giao-diện-dữ-liệu-cho-developer-b)
8. [Kiến trúc tổng thể và luồng chạy](#8-kiến-trúc-tổng-thể-và-luồng-chạy)
9. [Mã nguồn và các thành phần chính](#9-mã-nguồn-và-các-thành-phần-chính)
10. [Các bước thực hiện để ra project](#10-các-bước-thực-hiện-để-ra-project)
11. [Trình bày Main và các câu truy vấn](#11-trình-bày-main-và-các-câu-truy-vấn)
12. [Kiểm thử và kết quả](#12-kiểm-thử-và-kết-quả)
13. [Phụ lục](#13-phụ-lục)

---

# 1. TỔNG QUAN HỆ THỐNG

SmartSportHub là hệ thống quản lý việc đặt sân thể thao và tính tiền cho một nhà thi đấu nhỏ. Hệ thống thuộc nhóm **MIS — Management Information System**: không chỉ lưu trữ giao dịch đặt sân mà còn xử lý dữ liệu thành thông tin cho người quản lý (doanh thu, mức độ sử dụng sân, hành vi khách hàng, dịch vụ phổ biến).

Hệ thống gồm **hai phần** do hai thành viên cùng phát triển trong **một repository duy nhất**:

| Phần | Ngôn ngữ | Trách nhiệm |
|---|---|---|
| **Java Core** — Developer A | Java 22 | Toàn bộ nghiệp vụ: mô hình thực thể, tính phí, quản lý dữ liệu, xuất JSON, phục vụ REST API, lưu trữ SQLite |
| **Web UI** — Developer B | C# ASP.NET Core MVC (.NET 10) | Giao diện web, bảng LINQ 20 truy vấn thống kê, báo cáo và biểu đồ |

Hai phần không phải hai hệ thống riêng lẻ. Chúng là **một hệ thống duy nhất** với hai tầng:

```text
        ┌─────────────────────────────────────────────┐
        │              TRÌNH DUYỆT                   │
        └────────────────────┬────────────────────────┘
                             │
        ┌────────────────────▼────────────────────────┐
        │   C# ASP.NET CORE  (Developer B)            │
        │   Razor Pages · ViewModel · Bootstrap       │
        └────────────────────┬────────────────────────┘
                             │ gọi qua SmartSportHubApiClient
        ┌────────────────────▼────────────────────────┐
        │   C# DTO + Query Service  →  LINQ           │
        │   (DTO là bản sao dữ liệu, KHÔNG phải       │
        │    nghiệp vụ)                                │
        └────────────────────┬────────────────────────┘
                             │ HTTP / JSON
        ┌────────────────────▼────────────────────────┐
        │   JAVA CORE  (Developer A)                  │
        │   model · manager · dto · service · api      │
        └────────────────────┬────────────────────────┘
                             │ JDBC
        ┌────────────────────▼────────────────────────┐
        │   SQLite  (Database.java · Repository.java) │
        └─────────────────────────────────────────────┘
```

**Nguyên tắc bất di bất dịch:** chỉ có một hệ nghiệp vụ duy nhất, và nó nằm ở tầng Java. Lớp C# chỉ nhận dữ liệu dạng JSON để hiển thị và thống kê. Vì vậy khi có thay đổi về nghiệp vụ, chỉ có một chỗ cần sửa.

---

# 2. PHÁT BIỂU VẤN ĐỀ VÀ HIỆN TRẠNG

## 2.1 Bối cảnh

Nhà thi đấu Minh Phát đang vận hành 6 sân thể thao: 2 sân bóng đá, 2 sân cầu lông, 1 sân tennis, 1 sân pickleball. Trong tháng 2/2026 có 12 lượt khách đặt sân, kèm theo dịch vụ phát sinh như thuê vợt, thuê dụng cụ, nước uống, thuê huấn luyện viên.

Nhóm khảo sát hiện trạng theo **kịch bản mô phỏng** nhà thi đấu Minh Phát (chưa có phỏng vấn doanh nghiệp thật, dữ liệu là dữ liệu mẫu phục vụ học tập). Qua hiện trạng, xác định 4 vấn đề của quản lý thủ công bằng sổ tay:

| Mã | Vấn đề | Hậu quả |
|---|---|---|
| **V1** | Không biết chính xác sân nào đang trống | Khách đến nơi mới biết sân có trống hay không; nhiều khách phải chờ |
| **V2** | Tính tiền bằng tay, dễ tính nhầm phí giờ cao điểm | Bất đồng giữa tiền sân và tiền khách phải trả; mất uy tín |
| **V3** | Lịch sử giao dịch nằm rải rác trong sổ, không tra cứu được | Không lập được báo cáo doanh thu theo khách, theo loại sân, theo giờ |
| **V4** | Không lưu hồ sơ khách hàng | Không biết khách nào là VIP, không áp dụng được chính sách ưu đãi |

Điểm cốt lõi của V2: phí giờ cao điểm chỉ áp dụng trong khung **17:00 ≤ giờ bắt đầu < 20:00**, và mỗi loại sân có phí riêng khác nhau. Với sáu loại thuộc tính tính tiền cộng dồn, tính tay rất dễ quên bớt một khoản.

## 2.2 Mục tiêu của hệ thống mới

Hệ thống mới phải giải quyết trọn vẹn V1 đến V4, tức là phải thực hiện được sáu việc:

| Mã | Mục tiêu | Cách đạt |
|---|---|---|
| **M1** | Tra cứu sân trống theo trạng thái | Bảng `SPORTFIELD` với trường `status` |
| **M2** | Tính tiền chính xác, không thiếu khoản nào | Phương thức tính phí trong lớp Java, kiểm chứng bằng 113 phép thử |
| **M3** | Lập được báo cáo theo nhiều góc nhìn | Lưu `startTime`, `status` và các khoản phí đã tách riêng |
| **M4** | Quản lý hồ sơ khách và chính sách ưu đãi | Bảng `CUSTOMER` với `customerType` và `discountRate` |
| **M5** | Theo dõi thanh toán | Bảng `INVOICE` liên kết 1–1 với `BOOKING` |
| **M6** | Chuyển dữ liệu sang phần web để thống kê | Xuất JSON và phục vụ REST API |

## 2.3 Phạm vi và giới hạn

**Thuộc phạm vi:**
- Quản lý danh mục khách hàng, sân, dịch vụ
- Đặt sân theo khung giờ, gồm cả khung giờ cao điểm
- Tính phí thuê sân, phí cao điểm, phí dịch vụ, chiết khấu
- Phát hành hóa đơn, theo dõi trạng thái thanh toán
- Thống kê và báo cáo

**Ngoài phạm vi:**
- Thanh toán trực tuyến qua cổng ngân hàng
- Quản lý nhân sự, lịch làm việc nhân viên
- Quản lý nhiều chi nhánh
- Tích hợp thiết bị đọc thẻ thành viên

---

# 3. XÁC ĐỊNH CÁC THỰC THỂ VÀ THUỘC TÍNH

## 3.1 Danh mục thực thể

Quá trình phân tích nghiệp vụ dẫn đến **5 thực thể gốc** và **4 bảng chuyên biệt hoá** của thực thể sân:

| STT | Thực thể | Ý nghĩa nghiệp vụ | Loại |
|---|---|---|---|
| 1 | **CUSTOMER** | Khách hàng của nhà thi đấu | Thực thể gốc |
| 2 | **SPORTFIELD** | Sân thể thao | Thực thể gốc, có phân loại |
| 3 | **FOOTBALL_FIELD** | Riêng biệt của sân bóng đá | Bảng chuyên biệt hoá |
| 4 | **BADMINTON_FIELD** | Riêng biệt của sân cầu lông | Bảng chuyên biệt hoá |
| 5 | **TENNIS_FIELD** | Riêng biệt của sân tennis | Bảng chuyên biệt hoá |
| 6 | **PICKLEBALL_FIELD** | Riêng biệt của sân pickleball | Bảng chuyên biệt hoá |
| 7 | **SERVICE** | Dịch vụ phát sinh kèm theo | Thực thể gốc |
| 8 | **BOOKING** | Lượt đặt sân của khách | Thực thể gốc |
| 9 | **INVOICE** | Hóa đơn thanh toán | Thực thể gốc |

**Vì sao tách bốn bảng loại sân?** Bốn loại sân có bốn bộ phí riêng biệt, không loại nào giống loại nào:

| Loại sân | Phí riêng |
|---|---|
| Sân bóng đá | phí đèn 50.000 đồng mỗi lượt |
| Sân cầu lông | phí vợt 20.000 đồng + phí thảm 10.000 đồng mỗi lượt |
| Sân tennis | phí dụng cụ 40.000 đồng mỗi lượt |
| Sân pickleball | phí dụng cụ 15.000 đồng **mỗi giờ** thuê |

Nếu gộp tất cả vào bảng `SPORTFIELD`, mỗi dòng sẽ có 4 cột chứa giá trị rỗng không dùng đến. Tách ra thành bốn bảng riêng vừa đúng nguyên tắc chuẩn hoá, vừa tạo ra **đối xứng hoàn hảo** với bốn lớp con trong Java.

## 3.2 Ràng buộc toàn vẹn

| Mã | Ràng buộc | Cách đảm bảo |
|---|---|---|
| RB1 | `customerId`, `fieldId`, `serviceId`, `bookingId`, `invoiceId` là duy nhất | Khai báo `private final`, không có setter |
| RB2 | `basePricePerHour` > 0 | Kiểm tra trong setter |
| RB3 | `startTime < endTime` | Kiểm tra trong `setStartTime`/`setEndTime` |
| RB4 | `hours` > 0 | Kiểm tra trong `validateHours` |
| RB5 | `status` ∈ {AVAILABLE, MAINTENANCE, OCCUPIED} | Enum `FieldStatus` |
| RB6 | Một sân không thể có hai booking giao nhau thời gian | `requireSlotFree` trong `BookingService` |
| RB7 | `discountRate` và `tierLevel` chỉ tồn tại trên khách VIP | Đặt hai thuộc tính này vào riêng lớp `VipCustomer` |

## 3.3 Từ khoá thuộc tính trong tài liệu

| Ký hiệu | Ý nghĩa |
|---|---|
| **PK** | Thuộc tính khoá chính — giá trị duy nhất, không trùng lặp, không rỗng |
| **FK** | Thuộc tính khoá ngoại — tham chiếu tới khoá chính của bảng khác |
| **PK, FK** | Vừa là khoá chính vừa là khoá ngoại |

---

# 4. XÁC ĐỊNH MỐI LIÊN KẾT VÀ XÂY DỰNG ERD

## 4.1 Danh mục liên kết

| Mã | Quan hệ | Tỷ lệ | Ý nghĩa nghiệp vụ |
|---|---|---|---|
| **L1** | SPORTFIELD → FOOTBALL_FIELD | 1 : 0..1 | Kế thừa — sân bóng đá có thêm phí đèn |
| **L2** | SPORTFIELD → BADMINTON_FIELD | 1 : 0..1 | Kế thừa — sân cầu lông có thêm phí vợt và phí thảm |
| **L3** | SPORTFIELD → TENNIS_FIELD | 1 : 0..1 | Kế thừa — sân tennis có thêm phí dụng cụ |
| **L4** | SPORTFIELD → PICKLEBALL_FIELD | 1 : 0..1 | Kế thừa — sân pickleball có thêm phí dụng cụ theo giờ |
| **L5** | CUSTOMER → BOOKING | 1 : 0..N | Một khách đặt được nhiều lượt; mỗi lượt thuộc đúng một khách |
| **L6** | SPORTFIELD → BOOKING | 1 : 0..N | Một sân phục vụ nhiều lượt; mỗi lượt chiếm đúng một sân |
| **L7** | SERVICE → BOOKING | 0..1 : 0..N | Lượt đặt **có thể** kèm hoặc không kèm dịch vụ |
| **L8** | BOOKING → INVOICE | 1 : 0..1 | Mỗi lượt sinh tối đa một hóa đơn |

**Ghi chú về L5 và L6 — quan hệ nhiều – nhiều.** Một khách có thể đặt nhiều sân khác nhau, và một sân có thể phục vụ nhiều khách. Về mặt toán học đây là quan hệ nhiều – nhiều. Quan hệ nhiều – nhiều **không thể đặt thẳng trong ERD** — phải tách ra một thực thể liên kết. `BOOKING` chính là thực thể liên kết đó, vì nó vừa nối hai thực thể, vừa chứa thuộc tính riêng (`startTime`, `status`).

**Ghi chú về L7 — quan hệ 0..1.** Không phải lượt đặt nào cũng kèm dịch vụ. Khách chỉ thuê sân thì `serviceId` rỗng. Vì vậy đây là quan hệ tuỳ chọn, không phải bắt buộc.

## 4.2 Sơ đồ ERD

Sơ đồ được vẽ bằng **draw.io** và lưu tại `docs/erd.drawio`, gồm hai trang:

| Trang | Nội dung |
|---|---|
| Trang 1 | ERD — 9 bảng, đầy đủ thuộc tính khoá, thuộc tính mô tả, quan hệ, chú giải và quy mô dữ liệu |
| Trang 2 | Quy tắc chuyển quan hệ ER/RR thành lớp Java, bảng ánh xạ, enum, interface và công thức tính phí |

## 4.3 Lược đồ quan hệ dạng chữ

```text
    │                                ┌─────────────────┐
    │            1 : 0..N              │    CUSTOMER     │
    │  ┌────────────┴────────────┐     │ customerId (PK) │
    │  │                         │     │ fullName        │
    │  │     ┌───────────────┐   │     │ phoneNumber     │
    │  │     │   BOOKING     │   │     │ email           │
    │  │     │ bookingId(PK) │   │     │ customerType    │
    │  └────►│ customerId(FK)│◄──┘     │ discountRate    │
    │        │ fieldId (FK)  │         │ tierLevel       │
    │        │ serviceId(FK) │────┐    └─────────────────┘
    │        │ startTime     │    │
    │        │ endTime       │    │
    │        │ status        │    │
    │        │ hours         │    │
    │        │ rentalFee     │    │
    │        │ peakSurcharge │    │
    │        │ serviceFee    │    │
    │        │ discount      │    │
    │        │ totalAmount   │    │
    │        └───────┬───────┘    │
    │                │ 1 : 0..1   │
    │        ┌───────▼────────┐   │
    │        │    INVOICE     │   │
    │        │ invoiceId (PK) │   │
    │        │ bookingId (FK) │   │
    │        │ customerId(FK) │   │
    │        │ fieldId (FK)   │   │
    │        │ issueDate      │   │
    │        │ status         │   │
    │        │ amount         │   │
    │        └───────────────┘   │
    │                            │
    │                    ┌───────▼──────────┐
    │                    │   SERVICE        │
    │                    │ serviceId (PK)   │
    │                    │ name             │
    │                    │ category         │
    │                    │ unitPrice        │
    │                    │ active           │
    │                    └──────────────────┘
```

---

# 5. CHUYỂN QUAN HỆ ER/RR THÀNH CÁC LỚP JAVA

## 5.1 Sáu quy tắc chuyển đổi

| Mã | Quan hệ / đặc điểm ER | Cách biểu diễn trong Java | Lý do |
|---|---|---|---|
| **Q1** | Bảng ER | Một lớp, tên viết hoa chữ cái đầu, không dấu gạch | Giữ đúng tên bảng để đối chiếu hai chiều |
| **Q2** | Quan hệ RR | Không tạo bảng liên kết — lớp giữ **thuộc tính tham chiếu** tới lớp kia | Tránh dư thừa; quan hệ trở thành quan hệ đối tượng |
| **Q3** | Thuộc tính khoá | Khai báo `private final`, không có hàm thiết lập | Thuộc tính khoá bất biến suốt vòng đời đối tượng |
| **Q4** | Thuộc tính enum | Tách thành **kiểu enum** riêng | Kiểm tra giá trị hợp lệ lúc biên dịch, không phải lúc chạy |
| **Q5** | Quan hệ kế thừa | Bảng gốc → lớp trừu tượng, mỗi bảng con → một lớp con ghi đè phương thức trừu tượng | Tận dụng đặc tính đa hình của Java |
| **Q6** | Nhóm thao tác CRUD | Mỗi thực thể gốc có một **lớp quản lý** riêng giữ `List` | Tách trách nhiệm, dễ kiểm thử |

## 5.2 Cấu trúc thư mục project

```text
OOP_GK/
├── src/
│   └── smartsporthub/
│       ├── model/                    ← 11 lớp thực thể + 5 lớp phụ trợ
│       │   ├── Customer.java              (trừu tượng)
│       │   ├── StandardCustomer.java      (kế thừa)
│       │   ├── VipCustomer.java           (kế thừa)
│       │   ├── SportField.java            (trừu tượng)
│       │   ├── FootballField.java         (kế thừa)
│       │   ├── BadmintonField.java        (kế thừa)
│       │   ├── TennisField.java           (kế thừa)
│       │   ├── PickleballField.java       (kế thừa)
│       │   ├── Service.java
│       │   ├── Booking.java
│       │   ├── Invoice.java
│       │   ├── CustomerType.java
│       │   ├── FieldStatus.java
│       │   ├── BookingStatus.java
│       │   ├── InvoiceStatus.java
│       │   └── FieldPricing.java         ← hằng số phí, không phải thực thể
│       ├── interfaces/
│       │   ├── IPromotional.java
│       │   └── IPeakHourCalculable.java
│       ├── manager/
│       │   ├── CustomerManager.java
│       │   ├── FieldManager.java
│       │   ├── ServiceManager.java
│       │   ├── BookingManager.java
│       │   └── InvoiceManager.java
│       ├── dto/
│       │   ├── JsonDto.java               ← lớp cơ sở
│       │   ├── CustomerDto.java
│       │   ├── FieldDto.java
│       │   ├── ServiceDto.java
│       │   ├── BookingDto.java
│       │   ├── InvoiceDto.java
│       │   ├── FieldType.java             ← enum loại sân, đặt ở tầng DTO
│       │   └── DtoMapper.java
│       ├── service/
│       │   ├── BookingService.java      ← nghiệp vụ ghi: đặt/hủy/đổi/hoàn thành/thu tiền
│       │   └── BusinessException.java   ← ánh xạ lỗi nghiệp vụ → mã HTTP
│       ├── db/
│       │   ├── Database.java            ← kết nối JDBC + SQLite, schema, WAL
│       │   └── Repository.java          ← đọc/ghi 5 bảng, giao dịch, chặn trùng lịch
│       └── api/
│           ├── SmartSportHubApi.java    ← server HTTP + endpoint đọc
│           └── WriteApi.java            ← endpoint ghi POST/PATCH
├── test/
│   ├── FieldDomainTest.java             15 phép thử
│   ├── BookingDomainTest.java           11 phép thử
│   ├── SeedDataValidationTest.java      18 phép thử
│   ├── JsonContractTest.java            9 phép thử
│   ├── ApiServerTest.java               11 phép thử
│   ├── DatabaseTest.java                9 phép thử
│   ├── BookingServiceTest.java          22 phép thử
│   └── WriteApiTest.java                18 phép thử
├── build.sh / build.ps1                ← biên dịch + chạy toàn bộ kiểm thử
├── docs/
│   ├── erd.drawio
│   ├── DAC_TA_THIET_KE.md
│   └── tools/
│       ├── gen_erd.py
│       └── check_erd.py
├── data/
│   └── dataset.json
└── web/
    └── SmartSportHub.Web/               ← phần Developer B
```

## 5.3 Bảng ánh xạ 9 bảng ER → 11 lớp Java

| Bảng ER | Lớp Java | Ghi chú |
|---|---|---|
| **CUSTOMER** | `Customer` (trừu tượng) · `StandardCustomer` (kế thừa) · `VipCustomer` (kế thừa) | Một bảng sinh ra **ba lớp** vì có hai nhóm khách khác nhau chính sách |
| **SPORTFIELD** | `SportField` (trừu tượng) | Chỉ lớp trừu tượng, lớp con nằm ở bốn bảng dưới |
| **FOOTBALL_FIELD** | `FootballField` | Ghi đè `calculateRentalFee()`, giữ `nightLightFee` |
| **BADMINTON_FIELD** | `BadmintonField` | Ghi đè `calculateRentalFee()`, giữ `racketFee`, `matFee` |
| **TENNIS_FIELD** | `TennisField` | Ghi đè `calculateRentalFee()`, giữ `courtEquipmentFee` |
| **PICKLEBALL_FIELD** | `PickleballField` | Ghi đè `calculateRentalFee()`, giữ `equipmentFeePerHour` |
| **SERVICE** | `Service` | Bảng đơn giản không phân loại, chỉ cần một lớp |
| **BOOKING** | `Booking` | Giữ ba tham chiếu: `Customer`, `SportField`, `Service` |
| **INVOICE** | `Invoice` | Giữ tham chiếu `Booking`, chụp lại khách, sân và tiền |

**Tổng: 9 bảng ER → 11 lớp thực thể.** Ngoài ra có 4 kiểu enum trong `model/`, 1 enum `FieldType` trong `dto/`, 2 interface và 5 lớp quản lý.

## 5.4 Quan hệ trừu tượng và kế thừa trong Java

**Nhánh khách hàng — trừu tượng + kế thừa + interface:**
`Customer` là abstract class, có `StandardCustomer` (ưu đãi = 0) và `VipCustomer` (giảm giá theo tỷ lệ) kế thừa; cả hai implement `IPromotional`.

**Nhánh sân — trừu tượng + kế thừa:**
`SportField` là abstract class, có 4 lớp con `FootballField`, `BadmintonField`, `TennisField`, `PickleballField` cùng implement `IPeakHourCalculable`.

**Bảng đối chiếu hai lối tổ chức dữ liệu:**

| Khía cạnh | ERD (khái niệm) | Java (hiện thực) |
|---|---|---|
| Loại sân | 4 bảng chuyên biệt | 4 lớp con kế thừa `SportField` |
| Khách hàng | 1 bảng | 1 lớp trừu tượng + 2 lớp con |
| Quan hệ giữa bảng | Khóa ngoại | Thuộc tính tham chiếu (reference) |
| Phí riêng | Cột trong bảng con | Thuộc tính private của lớp con |

## 5.5 Tách phần mở rộng bằng interface

- `IPeakHourCalculable` — khả năng kiểm tra giờ cao điểm và tính phụ phí tương ứng; có phương thức default `isPeakHour(LocalTime)` trả về `true` nếu `17:00 ≤ time < 20:00`.
- `IPromotional` — khả năng áp dụng chương trình ưu đãi lên một khoản tiền; có `calculatePromotionalDiscount(amount)` và `getPromotionPolicy()`.

## 5.6 Ví dụ code — đa hình trong `Booking`

```java
// Booking giữ reference kiểu SportField nhưng gọi đúng công thức của từng loại sân
public double getRentalFee() {
    return field.calculateRentalFee(getHours());   // đa hình
}

// Phí giờ cao điểm chỉ áp dụng nếu sân hỗ trợ và giờ bắt đầu trong khung
public double getPeakSurcharge() {
    if (field instanceof IPeakHourCalculable peak
            && peak.isPeakHour(startTime.toLocalTime())) {
        return peak.calculatePeakHourSurcharge(getHours());
    }
    return 0;
}

// Giảm giá theo chính sách của khách (đa hình)
public double getDiscount() {
    return customer.calculatePromotionalDiscount(getSubtotal());
}
```

---

# 6. DỮ LIỆU MẪU VÀ ENUM

## 6.1 Sáu kiểu enum

| Enum | Giá trị | Ý nghĩa |
|---|---|---|
| `CustomerType` | `STANDARD`, `VIP` | Loại khách |
| `FieldType` | `FOOTBALL`, `BADMINTON`, `TENNIS`, `PICKLEBALL` | Loại sân |
| `FieldStatus` | `AVAILABLE`, `MAINTENANCE`, `OCCUPIED` | Trạng thái sân |
| `BookingStatus` | `COMPLETED`, `CONFIRMED`, `CANCELLED` | Trạng thái lượt đặt |
| `InvoiceStatus` | `PAID`, `UNPAID`, `REFUNDED` | Trạng thái hóa đơn |
| `ServiceCategory` | `NUOC`, `THIET_BI`, `DICH_VU`, `TIEC_CU_TRANG`, `TIEN_ICH` | Nhóm dịch vụ |

## 6.2 Dữ liệu mẫu

- **CUSTOMER:** 6 khách — 3 `STANDARD`, 3 `VIP` (giảm giá 10%, 12%, 15%).
- **SPORTFIELD:** 6 sân — F001, F005 `AVAILABLE`; F003, F004 `OCCUPIED`; F002, F006 `MAINTENANCE`.
- **SERVICE:** 6 dịch vụ (SV006 `active = false` để kiểm chứng Q05).
- **BOOKING:** 12 lượt đặt, bao phủ cả booking peak và non-peak, cả có và không có dịch vụ, cả 3 trạng thái.
- **INVOICE:** 12 hóa đơn, liên kết 1–1 với booking.

## 6.3 Ba điểm khác biệt giữa thiết kế và mã hiện tại

| # | Điểm | ERD thiết kế | Mã Java hiện tại | Trạng thái |
|---|---|---|---|---|
| 1 | `SERVICE.category` | Enum `ServiceCategory` | Kiểu `String` | Chưa hiện thực enum, đang dùng chuỗi tiếng Việt |
| 2 | `fieldType` trong `SPORTFIELD` | Cột thật | Suy ra bằng `instanceof` trong enum `FieldType` | Có hiện thực ở tầng DTO (`BookingDto`, `FieldDto`) |
| 3 | `tierLevel` | Thuộc tính khách VIP | Có trong `VipCustomer` | Có hiện thực, dữ liệu mẫu chưa gán giá trị |
| 4 | Lược đồ vật lý | 9 bảng ER | SQLite gộp còn **5 bảng** | Có hiện thực, cột `field_type` + 4 cột phí trong `fields` |

---

# 7. GIAO DIỆN DỮ LIỆU CHO DEVELOPER B

## 7.1 Hợp đồng dữ liệu

Developer B không viết lại nghiệp vụ. Phần C# chỉ gồm:

| Loại lớp C# | Ví dụ | Trách nhiệm |
|---|---|---|
| **DTO** | `CustomerDto`, `FieldDto`, `BookingDto`, `ServiceDto`, `InvoiceDto` | Nhận JSON từ Java |
| **API client** | `SmartSportHubApiClient` | Gọi Java, không gọi HTTP trực tiếp trong Controller |
| **ViewModel** | Các model cho từng trang Razor | Chuẩn bị dữ liệu cho giao diện |
| **Query service** | Chứa 20 truy vấn LINQ | Tính toán bằng LINQ |

**Ba điều cấm:**
1. Không viết lại lớp Java
2. Không tạo bản sao nghiệp vụ bằng C#
3. Không hard-code kết quả truy vấn

## 7.2 Ánh xạ ERD → truy vấn LINQ

Mỗi truy vấn cần những cột nào trong ERD — đây là bảng chứng minh ERD đã đủ trường:

| Nhóm truy vấn | Cột ERD cần dùng |
|---|---|
| Lọc và sắp xếp sân | `fieldStatus`, `basePricePerHour`, `fieldType` |
| Thống kê theo loại sân | `fieldType` |
| Thống kê theo khách | `customerId`, `customerType`, `fullName` |
| Thống kê theo dịch vụ | `serviceCategory`, `active`, `unitPrice` |
| Thống kê theo thời gian | `startTime`, `endTime`, `hours`, `peakHour` |
| Phân tích doanh thu | `rentalFee`, `peakSurcharge`, `serviceFee`, `discount`, `totalAmount` |
| Theo dõi thanh toán | `invoiceStatus`, `issueDate`, `amount` |

## 7.3 Bảng truy vấn đầy đủ 20 truy vấn

| Mã | Tên truy vấn | Dữ liệu cần | Toán tử LINQ chính |
|---|---|---|---|
| Q01 | Sân đang sẵn sàng, sắp xếp giá tăng dần | `status`, `basePricePerHour` | `Where`, `OrderBy`, `Select` |
| Q02 | Sân có giá cao hơn giá trung bình | `basePricePerHour` | `Average`, `Where` |
| Q03 | Đếm số sân theo loại | `fieldType` | `GroupBy`, `Count` |
| Q04 | Sân có doanh thu cao nhất | `totalAmount`, `fieldType` | `GroupBy`, `Max` |
| Q05 | Dịch vụ đang cung cấp | `active` | `Where`, `Select` |
| Q06 | Sân chưa từng được đặt | `fieldId`, `bookingId` | `Except`, `Any` |
| Q07 | Lượt đặt không kèm dịch vụ | `serviceId` | `Where`, `Any` |
| Q08 | Doanh thu theo từng loại sân | `fieldType`, `totalAmount` | `GroupBy`, `Sum` |
| Q09 | Lượt đặt trong một tháng cụ thể | `startTime` | `Where`, `OrderBy` |
| Q10 | Xếp hạng khách theo tổng thời gian đã dùng | `customerId`, `hours` | `GroupBy`, `Sum` |
| Q11 | Chi tiêu từng khách hàng | `customerId`, `totalAmount` | `GroupJoin`, `DefaultIfEmpty`, `Sum` |
| Q12 | Khách có nhiều giờ thuê nhất | `customerId`, `hours` | `GroupBy`, `Sum`, `Max`, `Join` |
| Q13 | Khách thuê nhiều loại sân | `customerId`, `fieldType` | `GroupBy`, `Distinct`, `Count` |
| Q14 | Thuê dụng cụ nhưng chưa đặt tennis | `serviceCategory`, `fieldType` | `Where`, `Any`, `All` |
| Q15 | Số lượt đặt theo giờ bắt đầu | `startTime` | `GroupBy`, `Count` |
| Q16 | Ngày bận nhất | `startTime` | `GroupBy`, `Count`, `Max`, `Min` |
| Q17 | Dịch vụ phổ biến nhất | `serviceId` | `SelectMany`, `GroupBy`, `Count`, `Max` |
| Q18 | Booking vượt mức của chính khách | `customerId`, `totalAmount` | `GroupBy`, `Average`, `Where`, `SelectMany` |
| Q19 | Phân trang booking hoàn thành | `startTime`, `status` | `Where`, `OrderBy`, `Skip`, `Take` |
| Q20 | Khách đã đặt nhưng chưa hoàn thành | `customerId`, `status` | `Any`, `Where` |

---

# 8. KIẾN TRÚC TỔNG THỂ VÀ LUỒNG CHẠY

## 8.1 Luồng dữ liệu

1. **Khởi động Java:** `SmartSportHubApi.main()` mở SQLite, tạo schema nếu cần, nạp seed nếu DB trống, khởi động HTTP server ở cổng 18083.
2. **Khởi động C#:** ASP.NET Core MVC chạy ở cổng 5080, cấu hình `SmartSportHubApi:BaseUrl = http://localhost:18083/`.
3. **Người dùng** mở trình duyệt → C# controller nhận request → gọi `SmartSportHubApiClient` → nhận JSON → map sang DTO → chạy LINQ → render Razor view.
4. **Thao tác ghi** (đặt sân, hủy, hoàn thành, đổi lịch, thu/hoàn tiền, đổi trạng thái sân): form POST → C# controller → Java API endpoint ghi → `BookingService` kiểm tra nghiệp vụ + lưu SQLite → trả JSON → redirect với flash message.

## 8.2 Các endpoint API

**Đọc (GET):**

| Method | URL | Mô tả |
|---|---|---|
| GET | `/api/health` | Kiểm tra server sống + đếm entity |
| GET | `/api/customers?page=1&pageSize=20` | Danh sách khách hàng |
| GET | `/api/fields?page=1&pageSize=20` | Danh sách sân |
| GET | `/api/services?page=1&pageSize=20` | Danh sách dịch vụ |
| GET | `/api/bookings?page=1&pageSize=20` | Danh sách booking (đã tính tiền) |
| GET | `/api/invoices?page=1&pageSize=20` | Danh sách hóa đơn |
| GET | `/api/data` | Snapshot toàn bộ 5 tập (không phân trang) |
| GET | `/api/overview` | Tổng quan nhanh: đếm entity + doanh thu |

**Ghi (POST / PATCH):**

| Method | URL | Mô tả |
|---|---|---|
| POST | `/api/bookings` | Tạo booking mới |
| PATCH | `/api/bookings/{id}/reschedule` | Đổi lịch |
| POST | `/api/bookings/{id}/cancel` | Hủy booking |
| POST | `/api/bookings/{id}/complete` | Hoàn thành booking |
| POST | `/api/invoices/{id}/pay` | Thu tiền |
| POST | `/api/invoices/{id}/refund` | Hoàn tiền |
| PATCH | `/api/fields/{id}/status` | Đổi trạng thái sân |

---

# 9. MÃ NGUỒN VÀ CÁC THÀNH PHẦN CHÍNH

## 9.1 Java Core

**`model/`** — 11 lớp thực thể:
- `Customer` (abstract) + `StandardCustomer`, `VipCustomer`
- `SportField` (abstract) + `FootballField`, `BadmintonField`, `TennisField`, `PickleballField`
- `Service`, `Booking`, `Invoice`
- 4 enum trạng thái + `FieldPricing` (hằng số phí)

**`interfaces/`** — `IPromotional`, `IPeakHourCalculable`.

**`manager/`** — `CustomerManager`, `FieldManager`, `ServiceManager`, `BookingManager`, `InvoiceManager` (thêm/tìm/cập nhật trên `List`).

**`data/`** — `SeedData`, `SmartSportHubData`.

**`dto/`** — `JsonDto` (lớp cơ sở), `CustomerDto`, `FieldDto`, `ServiceDto`, `BookingDto`, `InvoiceDto`, `FieldType`, `DtoMapper`.

**`service/`** — `BookingService` (nghiệp vụ ghi), `BusinessException` (ánh xạ lỗi → mã HTTP), `DataExportService`.

**`db/`** — `Database` (JDBC + SQLite, WAL), `Repository` (đọc/ghi, giao dịch, chặn trùng lịch).

**`api/`** — `SmartSportHubApi` (server HTTP + endpoint đọc), `WriteApi` (endpoint ghi).

## 9.2 C# Web

- `Program.cs` — khởi tạo ASP.NET Core MVC.
- `Controllers/HomeController.cs` — nhận request, gọi API, trả view; 7 action ghi.
- `Services/SmartSportHubApiClient.cs` — gọi Java API bằng HTTP.
- `Services/SmartSportHubQueryService.cs` — 20 câu LINQ.
- `Dtos/ApiData.cs` — DTO nhận JSON.
- `ViewModels/PageModels.cs` — dữ liệu cho từng trang.
- `Views/Home/` — 9 trang: Index, Fields, Customers, Bookings, Invoices, Services, Reports, Queries, Unavailable.
- `Views/Shared/_Layout.cshtml`, `_QueryTable.cshtml`.
- `wwwroot/css/site.css` — giao diện.
- `SmartSportHub.Web.Tests/` — 43 test C#.

---

# 10. CÁC BƯỚC THỰC HIỆN ĐỂ RA PROJECT

1. **Phân tích bài toán và hiện trạng** — đọc mô tả nghiệp vụ, gạch chân danh từ chính → xác định 5 thực thể gốc.
2. **Xác định thực thể, thuộc tính, khóa** — PK/FK, ràng buộc toàn vẹn (RB1–RB7).
3. **Xác định quan hệ và vẽ ERD** — 9 bảng, 8 quan hệ L1–L8, công cụ draw.io.
4. **Chuyển ERD sang lớp Java** theo 6 quy tắc Q1–Q6 (bảng → lớp, quan hệ → tham chiếu, khóa → `final`, enum → kiểu enum, kế thừa → abstract + override, CRUD → manager).
5. **Viết các lớp quản lý và dữ liệu mẫu** — `SeedData` với 6 khách, 6 sân, 6 dịch vụ, 12 booking, 12 invoice.
6. **Dựng DTO và API JSON** — `JsonDto`, `DtoMapper`, `SmartSportHubApi` (JDK thuần, không framework).
7. **Thêm lưu trữ SQLite** qua JDBC — `Database`, `Repository`, WAL, FK, transaction.
8. **Developer B làm giao diện web** — ASP.NET Core MVC, `SmartSportHubApiClient`, 20 câu LINQ, Razor views.
9. **Kiểm thử cả hai phía** — Java 113 phép thử, C# 43 phép thử, test live 2 server.

Kiến trúc cuối: `Browser → C# ASP.NET Core → Java API → Java Core + SQLite`. C# không mở DB, không tự tính tiền; chỉ nhận JSON rồi chạy LINQ trên collection trong bộ nhớ.

---

# 11. TRÌNH BÀY MAIN VÀ CÁC CÂU TRUY VẤN

## 11.1 Chạy Main

`SmartSportHubApi.main()` thực hiện:
1. Mở SQLite (`Database.open(dbPath)`), tạo schema nếu chưa có.
2. Nạp dữ liệu mẫu nếu DB trống (`repository.loadInto(...)` trả false → `repository.saveAll(...)`).
3. Tạo `HttpServer` ở cổng chỉ định (mặc định 8080, repo dùng 18083).
4. In ra: `SmartSportHub API đang chạy tại http://localhost:18083/api/data` và `Dữ liệu: customers=6, fields=6, bookings=12, services=6, invoices=12`.

C# Web chạy ở cổng 5080, cấu hình `SmartSportHubApi:BaseUrl = http://localhost:18083/`.

## 11.2 Các câu truy vấn Q01–Q10 (Developer A)

Mỗi câu nói theo khuôn: **câu hỏi → dùng LINQ gì → kết quả → ý nghĩa quản lý**.

- **Q01 — Sân AVAILABLE rẻ nhất** (`Where · OrderBy · Select`): tìm sân đang mở có giá thấp nhất → nhân viên gợi ý sân rẻ còn trống.
- **Q02 — Giá cao hơn trung bình** (`Average · Where`): sân nào đắt hơn mặt bằng chung → phân khúc giá.
- **Q03 — Số sân theo loại** (`GroupBy · Count`): 2 bóng đá, 2 cầu lông, 1 tennis, 1 pickleball → cơ cấu tài sản.
- **Q04 — Loại sân đắt nhất** (`GroupBy · Average · Max`): loại có giá TB cao nhất, không chỉ nhìn 1 sân.
- **Q05 — Sân đã được thuê** (`GroupJoin · Count`): đếm booking COMPLETED theo sân → sân thực sự sinh doanh thu.
- **Q06 — Sân chưa có lượt hoàn thành** (`Where · Any`): sân chưa từng có booking COMPLETED → sân ít khai thác.
- **Q07 — Sân nhiều giờ thuê nhất** (`GroupBy · Sum · Max · Join`): tổng giờ COMPLETED cao nhất → mức độ sử dụng theo thời lượng.
- **Q08 — Doanh thu theo loại sân** (`Where · GroupBy · Sum`): loại nào đóng góp nhiều doanh thu nhất → báo cáo quản trị.
- **Q09 — Sân vượt doanh thu trung bình** (`GroupJoin · Sum · Average · Where`): kể cả sân doanh thu 0 trong phép tính TB → mặt bằng toàn bộ sân.
- **Q10 — Sân có cả giờ thường và cao điểm** (`GroupBy · Any · All`): từng có cả 2 khung giờ → nhu cầu phân bố theo giờ trong ngày.

## 11.3 Các câu truy vấn Q11–Q20 (Developer B)

- **Q11 — Chi tiêu từng khách** (`GroupJoin · DefaultIfEmpty · Count · Sum`): kể cả khách chưa có booking COMPLETED.
- **Q12 — Khách nhiều giờ thuê nhất** (`GroupBy · Sum · Max · Join`): khách sử dụng dịch vụ nhiều nhất → nhận diện khách thân thiết.
- **Q13 — Khách thuê ≥ 2 loại sân** (`GroupBy · Distinct · Count · Join`): cơ sở chính sách chăm sóc khách dùng nhiều nhóm dịch vụ.
- **Q14 — Thuê dụng cụ nhưng chưa đặt tennis** (`Where · Any · All`): nhóm khách tiềm năng để giới thiệu sân tennis.
- **Q15 — Số booking theo giờ bắt đầu** (`Where · GroupBy · Count`): khung giờ cao điểm nhu cầu → điều chỉnh giá/bố trí nhân sự.
- **Q16 — Ngày bận nhất** (`GroupBy · Count · Max · Min`): nhiều ngày cùng số booking cao nhất thì chọn ngày sớm nhất (Min) → kết quả luôn xác định duy nhất.
- **Q17 — Dịch vụ phổ biến nhất** (`SelectMany · GroupBy · Count · Max`): dịch vụ dùng nhiều nhất trong booking COMPLETED.
- **Q18 — Booking vượt mức của chính khách** (`GroupBy · Average · Where · SelectMany`): so với TB của chính khách đó, không phải TB toàn hệ thống.
- **Q19 — Trang 2 booking hoàn thành** (`Where · OrderBy · Skip · Take`): Skip(3).Take(3), mỗi trang 3 dòng → phân trang trên collection sau khi nhận từ API.
- **Q20 — Đã đặt nhưng chưa hoàn thành** (`Any · Where`): khách có booking nhưng chưa có booking COMPLETED → nhắc lịch/chăm sóc.

---

# 12. KIỂM THỬ VÀ KẾT QUẢ

## 12.1 Java — 113 phép thử (8 suite)

| Suite | Số test | Nội dung |
|---|---|---|
| `FieldDomainTest` | 15 | Tạo 4 loại sân, đa hình, phí cao điểm, validation, FieldManager |
| `BookingDomainTest` | 11 | Tạo khách Standard/VIP, tính tổng booking, validation, Invoice, BookingManager |
| `SeedDataValidationTest` | 18 | Số lượng entity, đủ 4 loại sân, trạng thái booking/hóa đơn, doanh thu khớp |
| `JsonContractTest` | 9 | Snapshot 5 tập dữ liệu, thứ tự key DTO |
| `ApiServerTest` | 11 | Endpoint đọc, phân trang, mã lỗi 400/404/405 |
| `DatabaseTest` | 9 | Schema, lưu/đọc, FK, WAL |
| `BookingServiceTest` | 22 | Đặt/hủy/đổi/hoàn thành/thu tiền/hoàn tiền, chặn trùng lịch |
| `WriteApiTest` | 18 | Endpoint ghi, mã HTTP, validation |

Chạy: `.\run-video.ps1 -Role Build` → `==> All Java tests passed: 113/113`.

## 12.2 C# — 43 phép thử

Chạy: `.\run-video.ps1 -Role Test` → `Passed! 43/43`.

## 12.3 Kiểm chứng live

- Đặt sân trên web → Java kiểm tra sân/thời gian, tính phí, tạo invoice, lưu SQLite; số booking tăng từ 12 → 13.
- Hủy booking → trạng thái `CANCELLED`, invoice theo nghiệp vụ.
- Đặt vào sân đang bảo trì → Java chặn với mã 409 (`Sân ... đang bảo trì, chưa thể đặt.`).
- Tổng doanh thu booking `PAID` = 1.887.200 đồng (seed ban đầu).

---

# 13. PHỤ LỤC

## 13.1 Công thức tính phí

**Khung giờ cao điểm:** `[17:00, 20:00)` — tỷ lệ phụ phí 20%.

**Phí riêng theo loại sân:**

| Hằng số | Giá trị |
|---|---|
| `FOOTBALL_NIGHT_LIGHT_FEE` | 50.000 |
| `BADMINTON_RACKET_FEE` | 20.000 |
| `BADMINTON_MAT_FEE` | 10.000 |
| `TENNIS_EQUIPMENT_FEE` | 40.000 |
| `PICKLEBALL_EQUIPMENT_FEE_PER_HOUR` | 15.000 |

**Công thức ghi đè trong từng lớp con:**

| Lớp | Công thức phí thuê sân |
|---|---|
| `FootballField` | `basePricePerHour × giờ + 50.000` |
| `BadmintonField` | `basePricePerHour × giờ + 20.000 + 10.000` |
| `TennisField` | `basePricePerHour × giờ + 40.000` |
| `PickleballField` | `(basePricePerHour + 15.000) × giờ` |

**Tổng tiền một lượt đặt:**

```text
rentalFee       = phí thuê sân theo công thức của lớp con
peakSurcharge   = basePricePerHour × giờ × 0.20   (nếu 17:00 ≤ giờ bắt đầu < 20:00)
serviceFee      = unitPrice của dịch vụ           (bằng 0 nếu không kèm dịch vụ)
discount        = calculatePromotionalDiscount(rentalFee + peakSurcharge + serviceFee)
totalAmount     = rentalFee + peakSurcharge + serviceFee − discount
```

## 13.2 Danh mục file trong dự án

| Đường dẫn | Nội dung |
|---|---|
| `docs/erd.drawio` | File ERD draw.io, 2 trang |
| `docs/DAC_TA_THIET_KE.md` | Đặc tả bản thiết kế |
| `docs/KICH_BAN_VIDEO.md` | Kịch bản quay video báo cáo |
| `docs/tools/gen_erd.py` | Script sinh file ERD |
| `docs/tools/check_erd.py` | Script kiểm tra bố cục ERD |
| `data/dataset.json` | Dữ liệu mẫu xuất ra JSON |
| `API_CONTRACT.md` | Hợp đồng API cho Developer B |
| `src/smartsporthub/model/` | 11 lớp thực thể, 4 kiểu enum, `FieldPricing` |
| `src/smartsporthub/dto/` | 5 lớp DTO, `JsonDto`, `DtoMapper`, enum `FieldType` |
| `src/smartsporthub/interfaces/` | 2 interface |
| `src/smartsporthub/manager/` | 5 lớp quản lý |
| `src/smartsporthub/service/` | `BookingService`, `BusinessException`, `DataExportService` |
| `src/smartsporthub/db/` | `Database`, `Repository` |
| `src/smartsporthub/api/` | `SmartSportHubApi`, `WriteApi` |
| `lib/sqlite-jdbc-3.46.1.3.jar` | Driver JDBC SQLite (có local, không commit) |
| `build.sh` / `build.ps1` | Biên dịch + chạy toàn bộ kiểm thử |
| `run-video.ps1` | Script chạy Java API / C# Web / test trên Windows PowerShell |
| `web/` | Phần Developer B — ASP.NET Core MVC |

## 13.3 Cách chạy trên Windows PowerShell

```powershell
cd "D:\New folder\OOP_GK"

# Terminal 1 — Java API
.\run-video.ps1 -Role Api

# Terminal 2 — C# Web
.\run-video.ps1 -Role Web

# Terminal 3 — test
.\run-video.ps1 -Role Build
.\run-video.ps1 -Role Test
.\run-video.ps1 -Role Open
```

Script tự tìm `dotnet.exe` tại `D:\dev\dotnet` khi dotnet không có trong PATH, tự tìm `java`/`javac`, tự tải driver SQLite nếu thiếu.

## 13.4 Kết quả kiểm chứng phần Java

| Hạng mục | Kết quả |
|---|---|
| Biên dịch với `-Xlint:all` | 0 cảnh báo |
| Phép thử tự động | 113/113 đạt (8 suite) |
| Bộ dữ liệu JSON | 6 khách · 6 sân · 6 dịch vụ · 12 lượt đặt · 12 hóa đơn |
| Kiểm tra API | `/api/health`, phân trang, mã lỗi 400/404/405/409 đều đúng |
| Tổng tiền của các hóa đơn `PAID` | 1.887.200 đồng |
| Lưu trữ bền vững | SQLite qua JDBC, dữ liệu còn sau khi đóng ứng dụng |

---

**Kết luận:** SmartSportHub là một hệ thống thông tin quản lý hoàn chỉnh ở mức bài tập lớn — Java đảm nhiệm mô hình hóa, nghiệp vụ, lưu trữ và API; C# đảm nhiệm giao diện và 20 truy vấn LINQ phục vụ báo cáo. Sản phẩm không chỉ quản lý việc đặt sân mà còn cung cấp thông tin ra quyết định: doanh thu theo loại sân, mức độ sử dụng sân, hành vi khách hàng.
