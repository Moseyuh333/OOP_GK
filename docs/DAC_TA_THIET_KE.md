# ĐẶC TẢ BẢN THIẾT KẾ DỰ ÁN NHÓM — SMARTSPORTHUB

**Môn học:** OOPR240279 — Lập trình hướng đối tượng
**Bài tập lớn:** Đồ án nhóm — Phần MIS (Hệ thống thông tin quản lý)
**Vai trò trong nhóm:** Developer A — phần Java Core
**Môi trường phát triển:** Java 22 · IntelliJ IDEA · draw.io 31.7.0

---

## MỤC LỤC

1. [Tổng quan hệ thống](#1-tổng-quan-hệ-thống)
2. [Phát biểu vấn đề](#2-phát-biểu-vấn-đề)
   - [2.1 Bối cảnh](#21-bối-cảnh)
   - [2.2 Hạn chế của hệ thống quản lý thủ công](#22-hạn-chế-của-hệ-thống-quản-lý-thủ-công)
   - [2.3 Mục tiêu của hệ thống mới](#23-mục-tiêu-của-hệ-thống-mới)
   - [2.4 Phạm vi và giới hạn](#24-phạm-vi-và-giới-hạn)
3. [Xác định các thực thể và thuộc tính](#3-xác-định-các-thực-thể-và-thuộc-tính)
   - [3.1 Danh mục thực thể](#31-danh-mục-thực-thể)
   - [3.2 Từ khoá thuộc tính trong tài liệu](#32-từ-khoá-thuộc-tính-trong-tài-liệu)
   - [3.3 CUSTOMER — Bảng Khách hàng](#33-customer--bảng-khách-hàng)
   - [3.4 SPORTFIELD và 4 bảng loại sân](#34-sportfield-và-4-bảng-loại-sân)
   - [3.5 SERVICE — Bảng Dịch vụ](#35-service--bảng-dịch-vụ)
   - [3.6 BOOKING — Bảng Lượt đặt](#36-booking--bảng-lượt-đặt)
   - [3.7 INVOICE — Bảng Hóa đơn](#37-invoice--bảng-hóa-đơn)
   - [3.8 Ràng buộc toàn vẹn](#38-ràng-buộc-toàn-vẹn)
4. [Xác định mối liên kết và xây dựng ERD](#4-xác-định-mối-liên-kết-và-xây-dựng-erd)
   - [4.1 Danh mục liên kết](#41-danh-mục-liên-kết)
   - [4.2 Sơ đồ ERD](#42-sơ-đồ-erd)
   - [4.3 Lược đồ quan hệ dạng chữ](#43-lược-đồ-quan-hệ-dạng-chữ)
5. [Chuyển quan hệ ER/RR thành các lớp Java](#5-chuyển-quan-hệ-errr-thành-các-lớp-java)
   - [5.1 Sáu quy tắc chuyển đổi](#51-sáu-quy-tắc-chuyển-đổi)
   - [5.2 Cấu trúc thư mục project IntelliJ](#52-cấu-trúc-thư-mục-project-intellij)
   - [5.3 Bảng ánh xạ 9 bảng ER → 11 lớp Java](#53-bảng-ánh-xạ-9-bảng-er--11-lớp-java)
   - [5.4 Quan hệ trừu tượng và kế thừa trong Java](#54-quan-hệ-trừu-tượng-và-kế-thừa-trong-java)
   - [5.5 Tách phần mở rộng bằng interface](#55-tách-phần-mở-rộng-bằng-interface)
   - [5.6 Biểu đồ lớp](#56-biểu-đồ-lớp)
6. [Tạo dữ liệu cho các thực thể và enum](#6-tạo-dữ-liệu-cho-các-thực-thể-và-enum)
   - [6.1 Sáu kiểu enum](#61-sáu-kiểu-enum)
   - [6.2 Dữ liệu mẫu — CUSTOMER](#62-dữ-liệu-mẫu--customer)
   - [6.3 Dữ liệu mẫu — SPORTFIELD](#63-dữ-liệu-mẫu--sportfield)
   - [6.4 Dữ liệu mẫu — SERVICE](#64-dữ-liệu-mẫu--service)
   - [6.5 Dữ liệu mẫu — BOOKING](#65-dữ-liệu-mẫu--booking)
   - [6.6 Dữ liệu mẫu — INVOICE](#66-dữ-liệu-mẫu--invoice)
   - [6.7 Thống kê kiểm chứng](#67-thống-kê-kiểm-chứng)
   - [6.8 Ba điểm khác biệt giữa thiết kế và mã hiện tại](#68-ba-điểm-khác-biệt-giữa-thiết-kế-và-mã-hiện-tại)
7. [Giao diện dữ liệu cho Developer B](#7-giao-diện-dữ-liệu-cho-developer-b)
   - [7.1 Hợp đồng dữ liệu và vì sao không sửa Java](#71-hợp-ồng-dữ-liệu-và-vì-sao-không-sửa-java)
   - [7.2 Ánh xạ ERD → truy vấn LINQ](#72-ánh-xạ-erd--truy-vấn-linq)
   - [7.3 Bảng truy vấn đầy đủ 20 truy vấn](#73-bảng-truy-vấn-đầy-đủ-20-truy-vấn)
8. [Phụ lục](#8-phụ-lục)

---

# 1. TỔNG QUAN HỆ THỐNG

SmartSportHub là hệ thống quản lý việc đặt sân thể thao và tính tiền cho một nhà thi đấu nhỏ. Hệ thống gồm **hai phần** do hai thành viên cùng phát triển trong **một repository duy nhất**:

| Phần | Ngôn ngữ | Trách nhiệm |
|---|---|---|
| **Java Core** — Developer A | Java 22 | Toàn bộ nghiệp vụ: mô hình thực thể, tính phí, quản lý dữ liệu, xuất JSON, phục vụ REST API |
| **Web UI** — Developer B | C# ASP.NET Core | Giao diện web, bảng LINQ 20 truy vấn thống kê, báo cáo và biểu đồ |

Hai phần không phải hai hệ thống riêng lẻ. Chúng là **một hệ thống duy nhất** với hai tầng:

```text
        ┌─────────────────────────────────────────────┐
        │              TRÌNH DUYỆT                   │
        └────────────────────┬────────────────────────┘
                             │
        ┌────────────────────▼────────────────────────┐
        │   C# ASP.NET CORE  (Developer B)            │
        │   Razor Pages · ViewModel · Bootstrap        │
        └────────────────────┬────────────────────────┘
                             │ gọi qua SmartSportHubApiClient
        ┌────────────────────▼────────────────────────┐
        │   C# DTO + Query Service  →  LINQ            │
        │   (DTO là bản sao dữ liệu, KHÔNG phải        │
        │    nghiệp vụ)                                 │
        └────────────────────┬────────────────────────┘
                             │ HTTP / JSON
        ┌────────────────────▼────────────────────────┐
        │   JAVA CORE  (Developer A)                  │
        │   model · manager · dto · service · api      │
        └─────────────────────────────────────────────┘
```

**Nguyên tắc bất di bất dịch:** chỉ có một hệ nghiệp vụ duy nhất, và nó nằm ở tầng Java. Lớp C# chỉ nhận dữ liệu dạng JSON để hiển thị và thống kê. Vì vậy khi có thay đổi về nghiệp vụ, chỉ có một chỗ cần sửa.

---

# 2. PHÁT BIỂU VẤN ĐỀ

## 2.1 Bối cảnh

Nhà thi đấu Minh Phát đang vận hành 6 sân thể thao: 2 sân bóng đá, 2 sân cầu lông, 1 sân tennis, 1 sân pickleball. Trong tháng 2/2026 có 12 lượt khách đặt sân, kèm theo dịch vụ phát sinh như thuê vợt, thuê dụng cụ, nước uống, thuê huấn luyện viên.

## 2.2 Hạn chế của hệ thống quản lý thủ công

Lãnh đạp nhà thi đấu đang ghi sổ tay. Cách làm này gặp bốn vấn đề cụ thể:

| Mã | Vấn đề | Hậu quả |
|---|---|---|
| **V1** | Không biết chính xác sân nào đang trống | Khách đến nơi mới biết sân có trống hay không; nhiều khách phải chờ |
| **V2** | Tính tiền bằng tay, dễ tính nhầm phí giờ cao điểm | Bất đồng giữa tiền sân và tiền khách phải trả; mất uy tín |
| **V3** | Lịch sử giao dịch nằm rải rác trong sổ, không tra cứu được | Không lập được báo cáo doanh thu theo khách, theo loại sân, theo giờ |
| **V4** | Không lưu hồ sơ khách hàng | Không biết khách nào là VIP, không áp dụng được chính sách ưu đãi |

Điểm cốt lõi của V2: phí giờ cao điểm chỉ áp dụng trong khung **17:00 ≤ giờ bắt đầu < 20:00**, và mỗi loại sân có phí riêng khác nhau. Với sáu loại thuộc tính tính tiền cộng dồn, tính tay rất dễ quên bớt một khoản.

## 2.3 Mục tiêu của hệ thống mới

Hệ thống mới phải giải quyết trọn vẹn V1 đến V4, tức là phải thực hiện được sáu việc:

| Mã | Mục tiêu | Cách đạt |
|---|---|---|
| **M1** | Tra cứu sân trống theo trạng thái | Bảng `SPORTFIELD` với trường `status` |
| **M2** | Tính tiền chính xác, không thiếu khoản nào | Phương thức tính phí trong lớp Java, kiểm chứng bằng 64 phép thử |
| **M3** | Lập được báo cáo theo nhiều góc nhìn | Lưu `startTime`, `status` và các khoản phí đã tách riêng |
| **M4** | Quản lý hồ sơ khách và chính sách ưu đãi | Bảng `CUSTOMER` với `customerType` và `discountRate` |
| **M5** | Theo dõi thanh toán | Bảng `INVOICE` liên kết 1–1 với `BOOKING` |
| **M6** | Chuyển dữ liệu sang phần web để thống kê | Xuất JSON và phục vụ REST API |

## 2.4 Phạm vi và giới hạn

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

**Vì sao tách bốn bảng loại sân?**

Bốn loại sân có bốn bộ phí riêng biệt, không loại nào giống loại nào:

| Loại sân | Phí riêng |
|---|---|
| Sân bóng đá | phí đèn 50.000 đồng mỗi lượt |
| Sân cầu lông | phí vợt 20.000 đồng + phí thảm 10.000 đồng mỗi lượt |
| Sân tennis | phí dụng cụ 40.000 đồng mỗi lượt |
| Sân pickleball | phí dụng cụ 15.000 đồng **mỗi giờ** thuê |

Nếu gộp tất cả vào bảng `SPORTFIELD`, mỗi dòng sẽ có 4 cột chứa giá trị rỗng không dùng đến. Tách ra thành bốn bảng riêng vừa đúng nguyên tắc chuẩn hoá, vừa tạo ra **đối xứng hoàn hảo** với bốn lớp con trong Java — xem [mục 5.3](#53-bảng-ánh-xạ-9-bảng-er--15-lớp-java).

## 3.2 Từ khoá thuộc tính trong tài liệu

| Ký hiệu | Ý nghĩa |
|---|---|
| **PK** | Thuộc tính khoá chính — giá trị duy nhất, không trùng lặp, không rỗng |
| **FK** | Thuộc tính khoá ngoại — tham chiếu tới khoá chính của bảng khác |
| **PK, FK** | Vừa là khoá chính vừa là khoá ngoại — dùng cho quan hệ kế thừa |
| *(không ký hiệu)* | Thuộc tính mô tả |

## 3.3 CUSTOMER — Bảng Khách hàng

**Ý nghĩa:** Lưu hồ sơ khách hàng và chính sách ưu đãi áp dụng cho họ.

| # | Ký hiệu | Tên thuộc tính | Kiểu | Ý nghĩa | Ví dụ |
|---|---|---|---|---|---|
| 1 | **PK** | `customerId` | String | Mã khách, do người thiết kế cấp | `C001` |
| 2 | | `fullName` | String | Họ và tên khách | `Nguyễn Văn An` |
| 3 | | `phoneNumber` | String | Số điện thoại liên hệ | `0901234567` |
| 4 | | `email` | String | Email gửi thông báo | `an.nguyen@gmail.com` |
| 5 | | `customerType` | Enum `CustomerType` | Loại khách: `STANDARD` hoặc `VIP` | `VIP` |
| 6 | | `discountRate` | double | Tỷ lệ giảm của khách VIP: 0.10, 0.12, 0.15 | `0.1` |
| 7 | | `tierLevel` | int | Cấp thành viên VIP: 1, 2, 3 | `1` |

**Ghi chú về cột `tierLevel`:** thuộc tính này *chỉ có nghĩa khi `customerType` = `VIP`*. Với khách `STANDARD`, cả `discountRate` lẫn `tierLevel` đều rỗng. Đây là đặc tả cho mô hình quan hệ; trong Java, điều này được đảm bảo bằng cách đặt hai thuộc tính này vào riêng lớp `VipCustomer` — xem [mục 5.4](#54-quan-hệ-trừu-tượng-và-kế-thừa-trong-java).

## 3.4 SPORTFIELD và 4 bảng loại sân

### 3.4.1 SPORTFIELD — bảng gốc

| # | Ký hiệu | Tên thuộc tính | Kiểu | Ý nghĩa | Ví dụ |
|---|---|---|---|---|---|
| 1 | **PK** | `fieldId` | String | Mã sân | `F001` |
| 2 | | `fieldName` | String | Tên sân | `Sân bóng A` |
| 3 | | `fieldType` | Enum `FieldType` | `FOOTBALL`, `BADMINTON`, `TENNIS`, `PICKLEBALL` | `FOOTBALL` |
| 4 | | `basePricePerHour` | double | Giá thuê một giờ | `100000` |
| 5 | | `status` | Enum `FieldStatus` | `AVAILABLE`, `MAINTENANCE`, `OCCUPIED` | `AVAILABLE` |

**Vì sao `fieldType` phải là một cột thật?**

Trong Java, loại sân được biết bằng toán tử `instanceof` — lấy đối tượng là `FootballField` thì chắc chắn là sân bóng đá, không cần lưu. **Nhưng trong mô hình quan hệ thì bắt buộc phải có cột này**, vì Developer B cần thống kê theo loại sân:

- Q03: đếm số sân theo từng loại
- Q08: doanh thu theo từng loại sân
- Q10: xếp hạng khách theo tổng thời gian đã dùng từng loại sân
- Q13: tìm khách đã dùng **từ hai loại sân trở lên**

Không có cột `fieldType`, bốn truy vấn này buộc phải đoán bằng cách kiểm tra chuỗi tên sân có bắt đầu bằng "Sân bóng" hay không — cách làm dễ sai và không phản ánh đúng thiết kế.

### 3.4.2 FOOTBALL_FIELD

| # | Ký hiệu | Tên thuộc tính | Kiểu | Ý nghĩa | Ví dụ |
|---|---|---|---|---|---|
| 1 | **PK, FK** | `fieldId` | String → SPORTFIELD | Liên kết tới sân gốc | `F001` |
| 2 | | `nightLightFee` | double | Phí đèn mỗi lượt | `50000` |

### 3.4.3 BADMINTON_FIELD

| # | Ký hiệu | Tên thuộc tính | Kiểu | Ý nghĩa | Ví dụ |
|---|---|---|---|---|---|
| 1 | **PK, FK** | `fieldId` | String → SPORTFIELD | Liên kết tới sân gốc | `F003` |
| 2 | | `racketFee` | double | Phí thuê vợt mỗi lượt | `20000` |
| 3 | | `matFee` | double | Phí thảm mỗi lượt | `10000` |

### 3.4.4 TENNIS_FIELD

| # | Ký hiệu | Tên thuộc tính | Kiểu | Ý nghĩa | Ví dụ |
|---|---|---|---|---|---|
| 1 | **PK, FK** | `fieldId` | String → SPORTFIELD | Liên kết tới sân gốc | `F005` |
| 2 | | `courtEquipmentFee` | double | Phí dụng cụ mỗi lượt | `40000` |

### 3.4.5 PICKLEBALL_FIELD

| # | Ký hiệu | Tên thuộc tính | Kiểu | Ý nghĩa | Ví dụ |
|---|---|---|---|---|---|
| 1 | **PK, FK** | `fieldId` | String → SPORTFIELD | Liên kết tới sân gốc | `F006` |
| 2 | | `equipmentFeePerHour` | double | Phí dụng cụ **mỗi giờ** | `15000` |

**Điểm cần lưu ý:** `equipmentFeePerHour` của pickleball tính theo **giờ**, còn `nightLightFee` của bóng đá, `racketFee` và `matFee` của cầu lông, `courtEquipmentFee` của tennis đều tính theo **lượt thuê**. Sự khác biệt này là lý do phí pickleball phải nằm trong hàm tính riêng chứ không thể dùng chung một công thức.

## 3.5 SERVICE — Bảng Dịch vụ

| # | Ký hiệu | Tên thuộc tính | Kiểu | Ý nghĩa | Ví dụ |
|---|---|---|---|---|---|
| 1 | **PK** | `serviceId` | String | Mã dịch vụ | `SV001` |
| 2 | | `name` | String | Tên dịch vụ | `Dụng cụ thể thao` |
| 3 | | `category` | Enum `ServiceCategory` | Nhóm dịch vụ | `Thiết bị` |
| 4 | | `unitPrice` | double | Đơn giá một lần dùng | `150000` |
| 5 | | `active` | boolean | Đang cung cấp hay tạm ngưng | `true` |

**Vì sao `category` phải là enum chứ không phải chuỗi tự do?**

Truy vấn Q14 của Developer B cần tìm những khách **"đã dùng dịch vụ liên quan vợt nhưng chưa từng đặt sân tennis"**. Nếu `category` là chuỗi tự do thì truy vấn phải viết `Contains("vợt")` — một thay đổi nhỏ trong cách đặt tên là hỏng ngay kết quả. Đặt thành enum `ServiceCategory` thì lọc chính xác bằng `ServiceCategory.THIET_BI`.

## 3.6 BOOKING — Bảng Lượt đặt

**Ý nghĩa:** Ghi lại mỗi lần khách đặt sân, kèm toàn bộ chi tiết tính tiền.

| # | Ký hiệu | Tên thuộc tính | Kiểu | Ý nghĩa | Ví dụ |
|---|---|---|---|---|---|
| 1 | **PK** | `bookingId` | String | Mã lượt đặt | `B001` |
| 2 | **FK** | `customerId` | String → CUSTOMER | Khách đặt sân | `C001` |
| 3 | **FK** | `fieldId` | String → SPORTFIELD | Sân được đặt | `F001` |
| 4 | **FK** | `serviceId` | String → SERVICE | Dịch vụ kèm theo, có thể rỗng | `SV001` |
| 5 | | `startTime` | LocalDateTime | Thời điểm bắt đầu | `2026-02-15T18:00` |
| 6 | | `endTime` | LocalDateTime | Thời điểm kết thúc | `2026-02-15T20:00` |
| 7 | | `status` | Enum `BookingStatus` | `COMPLETED`, `CONFIRMED`, `CANCELLED` | `COMPLETED` |
| 8 | *(tính toán)* | `hours` | double | Số giờ thuê, tính từ `endTime − startTime` | `2` |
| 9 | *(tính toán)* | `rentalFee` | double | Tiền thuê sân | `200000` |
| 10 | *(tính toán)* | `peakSurcharge` | double | Phí giờ cao điểm | `40000` |
| 11 | *(tính toán)* | `serviceFee` | double | Tiền dịch vụ | `30000` |
| 12 | *(tính toán)* | `discount` | double | Tiền giảm | `27000` |
| 13 | *(tính toán)* | `totalAmount` | double | Tổng tiền phải trả | `243000` |

**Bảy thuộc tính cuối không lưu trong cơ sở dữ liệu.** Chúng là kết quả tính theo công thức nghiệp vụ. Đã có MIS hoàn chỉnh thì tách thành bảng phụ hoặc tính bằng view. Ở đồ án này, công thức được giữ trong lớp Java và kiểm chứng bằng 64 phép thử tự động — đúng nguyên tắc tính theo nguồn sự kiện.

**Sáu khoản tiền được lưu tách riêng là có chủ ý.** Nếu chỉ lưu `totalAmount`, thì Q02 (bảng xếp hạng doanh thu từng khoản phí) và Q20 (phân tích ảnh hưởng giờ cao điểm) sẽ không có dữ liệu để tính.

## 3.7 INVOICE — Bảng Hóa đơn

| # | Ký hiệu | Tên thuộc tính | Kiểu | Ý nghĩa | Ví dụ |
|---|---|---|---|---|---|
| 1 | **PK** | `invoiceId` | String | Mã hóa đơn | `I001` |
| 2 | **FK** | `bookingId` | String → BOOKING | Lượt đặt sinh hóa đơn | `B001` |
| 3 | **FK** | `customerId` | String → CUSTOMER | Chụp lại khách lúc phát hành | `C001` |
| 4 | **FK** | `fieldId` | String → SPORTFIELD | Chụp lại sân lúc phát hành | `F001` |
| 5 | | `issueDate` | LocalDate | Ngày phát hành | `2026-02-15` |
| 6 | | `status` | Enum `InvoiceStatus` | `PAID`, `UNPAID`, `REFUNDED` | `PAID` |
| 7 | | `amount` | double | Số tiền chụp lại lúc phát hành | `243000` |

**Vì sao chụp lại `customerId`, `fieldId` và `amount`?**

Đây là kỹ thuật **snapshot**. Ba thuộc tính này có thể truy ra từ `BOOKING`, nhưng lưu lại giúp:

- Lập báo cáo doanh thu theo khách hoặc theo sân mà không cần nối bảng
- Kết quả báo cáo **không bị thay đổi** khi lượt đặt được sửa về sau

## 3.8 Ràng buộc toàn vẹn

| Mã | Ràng buộc | Nơi thực thi |
|---|---|---|
| **RB1** | `customerId`, `fieldId`, `serviceId`, `bookingId`, `invoiceId` không trùng lặp | Ràng buộc khoá chính |
| **RB2** | `serviceId` cho phép rỗng | Không đặt ràng buộc bắt buộc |
| **RB3** | Một sân không thể xuất hiện ở hai bảng loại sân cùng lúc | Cùng dùng `fieldId` làm PK |
| **RB4** | `endTime` phải lớn hơn `startTime` | Kiểm tra nghiệp vụ |
| **RB5** | Hai lượt đặt của cùng một sân không được trùng khung giờ | Kiểm tra nghiệp vụ |
| **RB6** | Không cho đặt sân khi `status` = `MAINTENANCE` | Kiểm tra nghiệp vụ |
| **RB7** | `discountRate` và `tierLevel` chỉ có nghĩa khi `customerType` = `VIP` | Đảm bảo bằng lớp con `VipCustomer` |
| **RB8** | Một lượt đặt sinh tối đa một hóa đơn | Quan hệ 1 : 0..1 |

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

**Ghi chú về L5 và L6 — quan hệ nhiều – nhiều.**

Một khách có thể đặt nhiều sân khác nhau, và một sân có thể phục vụ nhiều khách. Về mặt toán học đây là quan hệ nhiều – nhiều. Quan hệ nhiều – nhiều **không thể đặt thẳng trong ERD** — phải tách ra một thực thể liên kết. `BOOKING` chính là thực thể liên kết đó, vì nó vừa nối hai thực thể, vừa chứa thuộc tính riêng (`startTime`, `status`).

**Ghi chú về L7 — quan hệ 0..1.**

Không phải lượt đặt nào cũng kèm dịch vụ. Khách chỉ thuê sân thì `serviceId` rỗng. Vì vậy đây là quan hệ tuỳ chọn, không phải bắt buộc.

## 4.2 Sơ đồ ERD

Sơ đồ được vẽ bằng **draw.io** và lưu tại `docs/erd.drawio`, gồm hai trang:

| Trang | Nội dung |
|---|---|
| Trang 1 | ERD — 9 bảng, đầy đủ thuộc tính khoá, thuộc tính mô tả, quan hệ, chú giải và quy mô dữ liệu |
| Trang 2 | Quy tắc chuyển quan hệ ER/RR thành lớp Java, bảng ánh xạ, enum, interface và công thức tính phí |

```text
┌──────────────────┐
│   SPORTFIELD     │  fieldId (PK)
│   fieldName      │  fieldType (FieldType)
│   fieldType      │  basePricePerHour
│   basePricePerHour│ status (FieldStatus)
│   status         │
└────────┬─────────┘
         │ 1 : 0..1   (quan hệ kế thừa, fieldId vừa PK vừa FK)
    ┌────┼────┬────────────┐
    │    │    │            │
┌───▼──┐┌──▼───┐┌────────▼┐┌─────────────┐
│FOOTBALL││BADMIN││ TENNIS  ││ PICKLEBALL  │
│_FIELD ││TON   ││ _FIELD  ││ _FIELD      │
│fieldId││_FIELD││ fieldId ││ fieldId     │
│night  ││fieldId││courtEqui││equipmentFee │
│LightFee││racket││ pmentFee││ PerHour     │
│ 50.000││ matFee││ 40.000  ││  15.000/giờ │
└───────┘└──────┘└─────────┘└─────────────┘
         │                                              ┌─────────────────┐
         │            1 : 0..N                           │    CUSTOMER     │
         │  ┌────────────┴────────────┐                 │ customerId (PK) │
         │  │                         │  1 : 0..N       │ fullName        │
         │  │     ┌───────────────┐   │  ◄──────────────│ phoneNumber     │
         └─►│     │   BOOKING     │   │                 │ email           │
            │     │ bookingId(PK) │   │                 │ customerType    │
            └────►│ customerId(FK)│◄──┘                 │ discountRate    │
                  │ fieldId (FK) │                       │ tierLevel       │
                  │ serviceId(FK)│───────┐               └─────────────────┘
                  │ startTime     │       │
                  │ endTime       │  ┌────▼──────────┐
                  │ status        │  │   SERVICE     │
                  │ hours         │  │ serviceId (PK)│
                  │ rentalFee     │  │ name          │
                  │ peakSurcharge │  │ category      │
                  │ serviceFee    │  │ unitPrice     │
                  │ discount      │  │ active        │
                  │ totalAmount   │  └───────────────┘
                  └───────┬───────┘
                          │ 1 : 0..1
                  ┌───────▼────────┐
                  │    INVOICE     │
                  │ invoiceId (PK) │
                  │ bookingId (FK) │
                  │ customerId(FK) │
                  │ fieldId (FK)   │
                  │ issueDate      │
                  │ status         │
                  │ amount         │
                  └────────────────┘
```

**Ký hiệu dùng trong ERD:**

| Ký hiệu | Ý nghĩa |
|---|---|
| **PK** | Thuộc tính khoá chính |
| **FK** | Thuộc tính khoá ngoại |
| **PK, FK** | Vừa là khoá chính vừa là khoá ngoại |
| `1 : 0..N` | Mỗi thực thể bên phải thuộc đúng một thực thể bên trái |
| `1 : 0..1` | Quan hệ tuỳ chọn — có thể không có |
| đường nét đứt màu cam | Quan hệ kế thừa |

## 4.3 Lược đồ quan hệ dạng chữ

Bảng dưới đây là hình thức rút gọn của ERD, thuận tiện để in và trình bày:

```text
CUSTOMER (1) ──────< (N) BOOKING (1) ──────< (0..1) INVOICE
    │                      │
    │                      │ (N)
    │                      └──────< (1) SPORTFIELD
    │                                │
    │                                └── (0..1) FOOTBALL_FIELD
    │                                ├── (0..1) BADMINTON_FIELD
    │                                ├── (0..1) TENNIS_FIELD
    │                                └── (0..1) PICKLEBALL_FIELD
    │
    │        SERVICE (1) ──────< (0..N) BOOKING
    │            └── name, category, unitPrice, active
    │
    └── fullName, phoneNumber, email, customerType,
        discountRate, tierLevel
```

---

# 5. CHUYỂN QUAN HỆ ER/RR THÀNH CÁC LỚP JAVA

## 5.1 Sáu quy tắc chuyển đổi

Trước khi chuyển đổi, cần chốt sáu quy tắc. Đây là phần cốt lõi của yêu cầu "chuyển các quan hệ ERs/RRs thành các lớp".

| Mã | Quan hệ / đặc điểm ER | Cách biểu diễn trong Java | Lý do |
|---|---|---|---|
| **Q1** | Bảng ER | Một lớp, tên viết hoa chữ cái đầu, không dấu gạch | Giữ đúng tên bảng để đối chiếu hai chiều |
| **Q2** | Quan hệ RR | Không tạo bảng liên kết — lớp giữ **thuộc tính tham chiếu** tới lớp kia | Tránh dư thừa; quan hệ trở thành quan hệ đối tượng |
| **Q3** | Thuộc tính khoá | Khai báo `private final`, không có hàm thiết lập | Thuộc tính khoá bất biến suốt vòng đời đối tượng |
| **Q4** | Thuộc tính enum | Tách thành **kiểu enum** riêng | Kiểm tra giá trị hợp lệ lúc biên dịch, không phải lúc chạy |
| **Q5** | Quan hệ kế thừa | Bảng gốc → lớp trừu tượng, mỗi bảng con → một lớp con ghi đè phương thức trừu tượng | Tận dụng đặc tính đa hình của Java |
| **Q6** | Nhóm thao tác CRUD | Mỗi thực thể gốc có một **lớp quản lý** riêng giữ `List` | Tách trách nhiệm, dễ kiểm thử |

## 5.2 Cấu trúc thư mục project IntelliJ

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
│       └── api/
├── test/
├── docs/
│   ├── erd.drawio
│   ├── DAC_TA_THIET_KE.md
│   └── tools/
│       ├── gen_erd.py
│       └── check_erd.py
└── data/
    └── dataset.json
```

## 5.3 Bảng ánh xạ 9 bảng ER → 11 lớp Java

| Bảng ER | Lớp Java | Ghi chú |
|---|---|---|
| **CUSTOMER** | `Customer` (trừu tượng)<br/>`StandardCustomer` (kế thừa)<br/>`VipCustomer` (kế thừa) | Một bảng sinh ra **ba lớp** vì có hai nhóm khách khác nhau chính sách |
| **SPORTFIELD** | `SportField` (trừu tượng) | Chỉ lớp trừu tượng, lớp con nằm ở bốn bảng dưới |
| **FOOTBALL_FIELD** | `FootballField` | Ghi đè `calculateRentalFee()`, giữ `nightLightFee` |
| **BADMINTON_FIELD** | `BadmintonField` | Ghi đè `calculateRentalFee()`, giữ `racketFee`, `matFee` |
| **TENNIS_FIELD** | `TennisField` | Ghi đè `calculateRentalFee()`, giữ `courtEquipmentFee` |
| **PICKLEBALL_FIELD** | `PickleballField` | Ghi đè `calculateRentalFee()`, giữ `equipmentFeePerHour` |
| **SERVICE** | `Service` | Bảng đơn giản không phân loại, chỉ cần một lớp |
| **BOOKING** | `Booking` | Giữ ba tham chiếu: `Customer`, `SportField`, `Service` |
| **INVOICE** | `Invoice` | Giữ tham chiếu `Booking`, chụp lại khách, sân và tiền |

**Tổng: 9 bảng ER → 11 lớp thực thể.** Ngoài ra có 4 kiểu enum trong `model/`, 1 enum `FieldType` trong `dto/`, 2 interface và 5 lớp quản lý — tất cả đều là hệ quả trực tiếp của các quy tắc Q1 đến Q6 ở [mục 5.1](#51-sáu-quy-tắc-chuyển-đổi).

## 5.4 Quan hệ trừu tượng và kế thừa trong Java

### 5.4.1 Nhánh khách hàng — trừu tượng + kế thừa + interface

```text
                  ┌──────────────────────────────┐
                  │  Customer  (abstract)        │
                  │  - customerId : final String  │
                  │  - fullName, phoneNumber     │
                  │  - email                      │
                  │  + getCustomerType() abstract │
                  │  + calculatePromotional      │
                  │      Discount(...) : abstract │◄── IPromotional
                  └───────┬──────────────┬───────┘
                          │              │
              ┌───────────▼───┐   ┌──────▼─────────────┐
              │ Standard      │   │ VipCustomer        │
              │ Customer      │   │  - discountRate    │
              │               │   │  - tierLevel       │
              │ ưu đãi = 0    │   │                   │
              └───────────────┘   └────────────────────┘
```

Điểm then chốt: **ràng buộc RB7** ở [mục 3.8](#38-ràng-buộc-toàn-vẹn) — `discountRate` và `tierLevel` chỉ có nghĩa với khách VIP — được Java đảm bảo bằng cách đặt hai thuộc tính đó vào riêng lớp `VipCustomer`. Người dùng nhìn vào đối tượng `Customer` sẽ **không bao giờ** thấy thuộc tính này trên một khách thường.

### 5.4.2 Nhánh sân — trừu tượng + kế thừa

```text
                  ┌──────────────────────────────────┐
                  │  SportField  (abstract)           │
                  │  - fieldId : final String         │
                  │  - fieldName, basePricePerHour   │
                  │  - status : FieldStatus           │
                  │  + getFieldType() abstract        │
                  │  + calculateRentalFee(h) abstract │◄── IPeakHourCalculable
                  │  + isPeakHour(startTime)          │
                  └─┬───────┬─────────┬─────────┬────┘
        ┌──────────▼┐ ┌────▼──────┐ ┌▼────────┐ ┌▼─────────────┐
        │ Football  │ │ Badminton │ │ Tennis  │ │ Pickleball   │
        │ Field     │ │ Field     │ │ Field   │ │ Field        │
        │           │ │           │ │         │ │              │
        │ base×h    │ │ base×h    │ │ base×h  │ │ (base+15000) │
        │ + 50.000  │ │ +20.000   │ │ +40.000 │ │      ×h      │
        │           │ │ +10.000   │ │         │ │              │
        └───────────┘ └───────────┘ └─────────┘ └──────────────┘
```

### 5.4.3 Bảng đối chiếu hai lối tổ chức dữ liệu

Đây là bảng quan trọng nhất của chương này. Cùng một nghiệp vụ, nhưng hai cách tổ chức:

| Khía cạnh | Mô hình quan hệ (ERD) | Lập trình hướng đối tượng (Java) |
|---|---|---|
| Cách tổ chức loại sân | Tách thành 4 bảng chuyên biệt hoá | 4 lớp con kế thừa từ lớp trừu tượng |
| Cách nối bảng với bảng | Khoá ngoại | Tham chiếu đối tượng |
| Nơi lưu phí riêng của loại sân | Cột trong bảng loại sân | Thuộc tính trong lớp con |
| Nơi kiểm tra giá trị hợp lệ | Ràng buộc `CHECK` hoặc `ENUM` | Kiểu enum, kiểm tra lúc biên dịch |
| Ưu điểm | Truy vấn SQL gọn, dễ lập chỉ mục | Tái sử dụng mã, thêm loại mới chỉ viết một lớp |
| Nhược điểm | Thêm loại sân mới phải thêm bảng | Phải viết lớp mới, không sinh tự động |

## 5.5 Tách phần mở rộng bằng interface

Hai interface tách phần biến động khỏi phần cố định:

| Interface | Thực hiện bởi | Phương thức |
|---|---|---|
| `IPromotional` | `Customer`, `StandardCustomer`, `VipCustomer` | `calculatePromotionalDiscount(double amount)` → double<br/>`getPromotionPolicy()` → String |
| `IPeakHourCalculable` | 4 lớp con của `SportField` | `isPeakHour(LocalDateTime startTime)` → boolean |

Lớp `Booking` chỉ cộng phí cao điểm khi kiểm tra `field instanceof IPeakHourCalculable`. Nhờ vậy thêm một loại sân mới hoặc một hạng khách mới **chỉ cần viết thêm một lớp con**, không phải sửa những lớp đang chạy tốt.

## 5.6 Biểu đồ lớp

```text
                    ┌──────────────┐
                    │  BookingStatus│
                    ├──────────────┤
                    │ CONFIRMED    │
                    │ COMPLETED    │
                    │ CANCELLED    │
                    └──────────────┘

                    ┌──────────────┐
                    │  InvoiceStatus│
                    ├──────────────┤
                    │ UNPAID       │
                    │ PAID         │
                    │ REFUNDED     │
                    └──────────────┘

┌────────────────────────┐        ┌──────────────────────────┐
│       Booking          │        │        Invoice          │
├────────────────────────┤        ├──────────────────────────┤
│ - bookingId : final    │        │ - invoiceId : final      │
│ - customer : Customer  │───────►│ - booking : Booking      │
│ - field : SportField   │        │ - customerId : final     │
│ - service : Service?   │        │ - fieldId : final        │
│ - startTime, endTime   │        │ - issueDate              │
│ - status : BookingStat.│        │ - status : InvoiceStatus │
│ - rentalFee            │        │ - amount                 │
│ - peakSurcharge        │        └──────────────────────────┘
│ - serviceFee           │
│ - discount             │
│ - totalAmount          │
│ + getHours()           │
│ + calculateTotal()     │
└────────────────────────┘
```

---

# 6. TẠO DỮ LIỆU CHO CÁC THỰC THỂ VÀ ENUM

## 6.1 Sáu kiểu enum

### 6.1.1 CustomerType

| Giá trị | Ý nghĩa |
|---|---|
| `STANDARD` | Khách thường — không áp dụng ưu đãi |
| `VIP` | Khách VIP — áp dụng giảm giá theo hạng |

### 6.1.2 FieldType

| Giá trị | Ý nghĩa |
|---|---|
| `FOOTBALL` | Sân bóng đá |
| `BADMINTON` | Sân cầu lông |
| `TENNIS` | Sân tennis |
| `PICKLEBALL` | Sân pickleball |

### 6.1.3 FieldStatus

| Giá trị | Ý nghĩa |
|---|---|
| `AVAILABLE` | Sẵn sàng cho thuê |
| `MAINTENANCE` | Đang bảo trì, không đặt được |
| `OCCUPIED` | Đang có người dùng |

### 6.1.4 BookingStatus

| Giá trị | Ý nghĩa | Tính vào doanh thu |
|---|---|---|
| `COMPLETED` | Lượt đặt đã hoàn thành | Có |
| `CONFIRMED` | Đã đặt, chưa thực hiện | Không — nếu lọc `COMPLETED` |
| `CANCELLED` | Đã huỷ | Không |

### 6.1.5 InvoiceStatus

| Giá trị | Ý nghĩa |
|---|---|
| `PAID` | Đã thanh toán |
| `UNPAID` | Chưa thanh toán |
| `REFUNDED` | Đã hoàn tiền |

### 6.1.6 ServiceCategory

| Tên hằng số trong Java | Giá trị hiển thị | Ý nghĩa |
|---|---|---|
| `NUOC` | Nước | Nước uống |
| `THIET_BI` | Thiết bị | Dụng cụ thể thao, thiết bị thuê |
| `DICH_VU` | Dịch vụ | Huấn luyện viên, dịch vụ hỗ trợ |
| `TIEC_CU_TRANG` | Tiệc cư trang | Tiệc cư trang |
| `TIEN_ICH` | Tiện ích | Wifi, dọn sân |

Kiểu enum này là **thiết kế**, chưa hiện thực trong mã Java hiện tại — xem [mục 6.8](#68-ba-điểm-khác-biệt-giữa-thiết-kế-và-mã-hiện-tại). Bảng dữ liệu mẫu ở [mục 6.4](#64-dữ-liệu-mẫu--service) đang dùng giá trị hiển thị.

## 6.2 Dữ liệu mẫu — CUSTOMER

Bộ dữ liệu gồm 6 khách, trong đó 3 khách `STANDARD` và 3 khách `VIP`:

| customerId | fullName | phoneNumber | email | customerType | discountRate |
|---|---|---|---|---|---|
| C001 | Nguyễn Văn An | 0901234567 | an.nguyen@gmail.com | VIP | 0.10 |
| C002 | Trần Thị Bình | 0912345678 | binh.tran@gmail.com | STANDARD | 0 |
| C003 | Lê Văn Chiến | 0923456789 | chien.le@gmail.com | VIP | 0.12 |
| C004 | Phạm Thu Dung | 0934567890 | dung.pham@gmail.com | STANDARD | 0 |
| C005 | Võ Hải Đăng | 0945678901 | dang.vo@gmail.com | VIP | 0.15 |
| C006 | Đặng Thu Hà | 0956789012 | ha.dang@gmail.com | STANDARD | 0 |

**Ý nghĩa thiết kế:** chỉ có 3 tỷ lệ giảm khác nhau (10%, 12%, 15%) để khi trình diễn, một câu hỏi kiểm tra "khách VIP giảm nhiều nhất là bao nhiêu?" có đáp án thật, và mỗi VIP thuộc một hạng khác nhau.

**Về `tierLevel`:** thuộc tính này tồn tại trong lớp `VipCustomer` của Java và được mô tả trong ERD, nhưng bộ dữ liệu mẫu hiện tại **chưa gán giá trị** cho nó (mặc định 0). Chính sách ưu đãi thực tế được mô tả bằng thuộc tính `promotionPolicy` trong dữ liệu JSON, ví dụ `"VIP hạng 1 giảm 10%"`. Nếu sau này cần truy vấn "khách VIP hạng nào chi nhiều nhất" thì chỉ cần gán thêm `tierLevel` vào dữ liệu mẫu — cấu trúc đã sẵn sàng.

## 6.3 Dữ liệu mẫu — SPORTFIELD

| fieldId | fieldName | fieldType | basePricePerHour | status | Bảng loại sân chứa nó |
|---|---|---|---|---|---|
| F001 | Sân bóng A | FOOTBALL | 100000 | AVAILABLE | `FOOTBALL_FIELD` (nightLightFee = 50.000) |
| F002 | Sân bóng B | FOOTBALL | 110000 | MAINTENANCE | `FOOTBALL_FIELD` (nightLightFee = 50.000) |
| F003 | Sân cầu lông C | BADMINTON | 60000 | AVAILABLE | `BADMINTON_FIELD` (racketFee = 20.000, matFee = 10.000) |
| F004 | Sân tennis D | TENNIS | 150000 | OCCUPIED | `TENNIS_FIELD` (courtEquipmentFee = 40.000) |
| F005 | Sân pickleball E | PICKLEBALL | 90000 | AVAILABLE | `PICKLEBALL_FIELD` (equipmentFeePerHour = 15.000) |
| F006 | Sân cầu lông F | BADMINTON | 65000 | MAINTENANCE | `BADMINTON_FIELD` (racketFee = 20.000, matFee = 10.000) |

**Phân bổ dữ liệu mẫu:**

| Bảng loại sân | Số bản ghi | Mã sân tương ứng |
|---|---|---|
| `FOOTBALL_FIELD` | 2 | F001, F002 |
| `BADMINTON_FIELD` | 2 | F003, F006 |
| `TENNIS_FIELD` | 1 | F004 |
| `PICKLEBALL_FIELD` | 1 | F005 |

**Ý nghĩa thiết kế:** sân đang có trạng thái `OCCUPIED` (F004) và `MAINTENANCE` (F002, F006) là cố ý. Nhờ vậy truy vấn "sân đang sẵn sàng" của Developer B trả về **3 sân** thay vì 6 — chứng minh điều kiện lọc thực sự hoạt động, thay vì lọc xong vẫn ra đủ 6 dòng.

## 6.4 Dữ liệu mẫu — SERVICE

| serviceId | name | category | unitPrice | active |
|---|---|---|---|---|
| SV001 | Nước uống | Nước | 30000 | true |
| SV002 | Dụng cụ thể thao | Thiết bị | 150000 | true |
| SV003 | Tiệc cư trang | Tiệc cư trang | 200000 | true |
| SV004 | Huấn luyện viên | Dịch vụ | 250000 | true |
| SV005 | Wifi premium | Tiện ích | 50000 | true |
| SV006 | Dọn sân sau | Tiện ích | 40000 | false |

Dịch vụ `SV006` có `active = false` để truy vấn Q05 "danh sách dịch vụ đang cung cấp" trả về 5 dịch vụ.

## 6.5 Dữ liệu mẫu — BOOKING

Bộ dữ liệu gồm 12 lượt đặt, bao phủ đủ các tình huống tính tiền khác nhau:

| bookingId | customerId | fieldId | serviceId | startTime | hours | status | peakHour | rentalFee | peakSurcharge | serviceFee | discount | totalAmount |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| B001 | C001 | F001 | SV001 | 2026-02-15 18:00 | 2 | COMPLETED | ✓ | 200000 | 40000 | 30000 | 27000 | **243000** |
| B002 | C001 | F003 | SV003 | 2026-02-16 08:00 | 2 | COMPLETED | ✗ | 150000 | 0 | 200000 | 35000 | **315000** |
| B003 | C001 | F004 | — | 2026-02-17 19:00 | 2 | COMPLETED | ✓ | 340000 | 60000 | 0 | 40000 | **360000** |
| B004 | C002 | F001 | SV004 | 2026-02-18 06:00 | 2 | COMPLETED | ✗ | 200000 | 0 | 250000 | 0 | **450000** |
| B005 | C002 | F003 | — | 2026-02-19 07:00 | 2 | COMPLETED | ✗ | 150000 | 0 | 0 | 0 | **150000** |
| B006 | C003 | F003 | SV002 | 2026-02-20 17:30 | 2.5 | COMPLETED | ✓ | 180000 | 30000 | 150000 | 43200 | **316800** |
| B007 | C003 | F001 | SV001 | 2026-02-21 09:00 | 2 | COMPLETED | ✗ | 200000 | 0 | 30000 | 27600 | **202400** |
| B008 | C004 | F002 | SV001 | 2026-02-22 18:00 | 2 | CONFIRMED | ✓ | 220000 | 44000 | 30000 | 0 | **294000** |
| B009 | C004 | F004 | — | 2026-02-23 10:00 | 2 | CONFIRMED | ✗ | 340000 | 0 | 0 | 0 | **340000** |
| B010 | C005 | F005 | SV003 | 2026-02-24 19:00 | 2 | CONFIRMED | ✓ | 210000 | 36000 | 200000 | 66900 | **379100** |
| B011 | C002 | F006 | SV001 | 2026-02-25 20:30 | 1.5 | CANCELLED | ✗ | 127500 | 0 | 30000 | 0 | **157500** |
| B012 | C005 | F005 | SV004 | 2026-02-26 08:00 | 2 | CANCELLED | ✗ | 210000 | 0 | 250000 | 69000 | **391000** |

**Bộ dữ liệu cố tình chọn để bao phủ các tình huống khó:**

| Tình huống | Lượt đặt minh hoạ |
|---|---|
| Có phí cao điểm | B001, B003, B006, B008, B010 |
| Không có phí cao điểm | B002, B004, B005, B007, B009, B011, B012 |
| Bắt đầu đúng mốc 20:00 — **không** tính cao điểm | B011 lúc 20:30 |
| Không kèm dịch vụ | B003, B005, B009 |
| Thời gian thuê là số lẻ giờ | B006 (2,5 giờ), B011 (1,5 giờ) |
| Cả ba trạng thái BOOKING | 7 COMPLETED, 3 CONFIRMED, 2 CANCELLED |

**Kiểm chứng công thức bằng dữ liệu thật:**

Lượt đặt `B001` — sân bóng đá F001 (100.000 đồng/giờ), 2 giờ, bắt đầu 18:00 (thuộc giờ cao điểm), kèm nước uống 30.000 đồng, khách C001 là VIP giảm 10%:

```text
Phí thuê sân    = 100.000 × 2 giờ                    = 200.000
Phí cao điểm    = 100.000 × 2 giờ × 20%              =  40.000
Phí dịch vụ     = 30.000                              =  30.000
Chiết khấu VIP  = (200.000 + 40.000 + 30.000) × 10%  =  27.000
──────────────────────────────────────────────────────────────────────
Tổng tiền                                             = 243.000 đồng
```

Lượt đặt `B006` — sân cầu lông F003 (60.000 đồng/giờ), 2,5 giờ, bắt đầu 17:30 (thuộc giờ cao điểm), kèm dụng cụ 150.000 đồng, khách C003 là VIP giảm 12%:

```text
Phí thuê sân    = 60.000 × 2,5 giờ + 20.000 + 10.000  = 180.000
Phí cao điểm    = 60.000 × 2,5 giờ × 20%              =  30.000
Phí dịch vụ     = 150.000                             = 150.000
Chiết khấu VIP  = (180.000 + 30.000 + 150.000) × 12%  =  43.200
──────────────────────────────────────────────────────────────────────
Tổng tiền                                             = 316.800 đồng
```

Lượt đặt `B011` — sân cầu lông F006 (65.000 đồng/giờ), 1,5 giờ, bắt đầu 20:30 (sau khung cao điểm), kèm nước uống, khách thường:

```text
Phí thuê sân    = 65.000 × 1,5 giờ + 20.000 + 10.000  = 127.500
Phí cao điểm    = 0   ← 20:30 nằm ngoài khung [17:00, 20:00)
Phí dịch vụ     = 30.000                              =  30.000
Chiết khấu      = 0   (khách STANDARD)
──────────────────────────────────────────────────────────────────────
Tổng tiền                                             = 157.500 đồng
```

## 6.6 Dữ liệu mẫu — INVOICE

| invoiceId | bookingId | customerId | fieldId | issueDate | status | amount |
|---|---|---|---|---|---|---|
| I001 | B001 | C001 | F001 | 2026-02-15 | PAID | 243000 |
| I002 | B002 | C001 | F003 | 2026-02-16 | PAID | 315000 |
| I003 | B003 | C001 | F004 | 2026-02-17 | PAID | 360000 |
| I004 | B004 | C002 | F001 | 2026-02-18 | PAID | 450000 |
| I005 | B005 | C002 | F003 | 2026-02-19 | UNPAID | 150000 |
| I006 | B006 | C003 | F003 | 2026-02-20 | PAID | 316800 |
| I007 | B007 | C003 | F001 | 2026-02-21 | PAID | 202400 |
| I008 | B008 | C004 | F002 | 2026-02-22 | UNPAID | 294000 |
| I009 | B009 | C004 | F004 | 2026-02-23 | UNPAID | 340000 |
| I010 | B010 | C005 | F005 | 2026-02-24 | UNPAID | 379100 |
| I011 | B011 | C002 | F006 | 2026-02-25 | REFUNDED | 157500 |
| I012 | B012 | C005 | F005 | 2026-02-26 | REFUNDED | 391000 |

**Quy tắc sinh hóa đơn trong bộ dữ liệu mẫu:**

| Trạng thái hóa đơn | Quy tắc | Số lượng |
|---|---|---|
| `PAID` | Lượt đặt đã `COMPLETED` | 6 (I001–I004, I006, I007) |
| `UNPAID` | Lượt đặt `COMPLETED` nhưng chưa thu tiền, và cả lượt `CONFIRMED` | 4 (I005, I008, I009, I010) |
| `REFUNDED` | Lượt đặt đã `CANCELLED` — khách huỷ nên hoàn tiền | 2 (I011, I012) |

Hai hóa đơn `REFUNDED` ứng với hai lượt `CANCELLED` là một trong những chi tiết cố ý của bộ dữ liệu: nó cho phép kiểm chứng rằng lượt đã huỷ không cộng vào doanh thu, đồng thời có dữ liệu để lập báo cáo "số tiền đã hoàn trả".

## 6.7 Thống kê kiểm chứng

| Chỉ số | Giá trị |
|---|---|
| Số khách hàng | 6 — 3 `STANDARD`, 3 `VIP` |
| Số sân | 6 — 2 bóng đá, 2 cầu lông, 1 tennis, 1 pickleball |
| Trạng thái sân | 3 `AVAILABLE`, 2 `MAINTENANCE`, 1 `OCCUPIED` |
| Số dịch vụ | 6 — 5 đang cung cấp, 1 tạm ngưng |
| Số lượt đặt | 12 — 7 `COMPLETED`, 3 `CONFIRMED`, 2 `CANCELLED` |
| Số hóa đơn | 12 — 6 `PAID`, 4 `UNPAID`, 2 `REFUNDED` |
| Tổng tiền của các lượt `COMPLETED` | 2.037.200 đồng |
| Tổng tiền của các hóa đơn `PAID` | 1.887.200 đồng |

**Đối chiếu chéo hai tổng tiền:**

| Cách tính | Kết quả | Ý nghĩa |
|---|---|---|
| Tổng `totalAmount` của 7 lượt `COMPLETED` | 2.037.200 đồng | Doanh thu thực tế phát sinh trong tháng |
| Tổng `amount` của 6 hóa đơn `PAID` | 1.887.200 đồng | Tiền đã thu vào — thấp hơn vì 2 lượt `COMPLETED` chưa thu tiền |

Chênh lệch 150.000 đồng chính là lượt `B005` — đã hoàn thành nhưng hóa đơn `I005` còn ở trạng thái `UNPAID`. Sự khác biệt này là dữ liệu thật, không phải lỗi: nó cho thấy hệ thống phân biệt được **"đã sử dụng dịch vụ"** với **"đã thu tiền"**, đúng theo mục tiêu M5 ở [mục 2.3](#23-mục-tiêu-của-hệ-thống-mới).

## 6.8 Ba điểm khác biệt giữa thiết kế và mã hiện tại

Phần này ghi rõ chỗ nào trong ERD là **thiết kế**, chỗ nào đã **hiện thực** trong Java. Cần nói rõ để khi bảo vệ đồ án, thầy giáo hỏi thì trả lời đúng, và để Developer B biết phần nào có thể dựa vào ngay.

| # | Điểm | ERD thiết kế | Mã Java hiện tại | Trạng thái |
|---|---|---|---|---|
| 1 | Kiểu của `SERVICE.category` | Enum `ServiceCategory` | Kiểu `String` | **Chưa hiện thực.** Giá trị đang dùng là chuỗi tiếng Việt: `"Nước"`, `"Thiết bị"`, `"Dịch vụ"`, `"Tiệc cư trang"`, `"Tiện ích"` |
| 2 | Cột `fieldType` trong `SPORTFIELD` | Cột thật, lưu trong bảng | Suy ra bằng `instanceof` trong enum `FieldType` | **Có hiện thực ở tầng DTO** — `BookingDto` và `FieldDto` đều xuất ra trường `fieldType`, Developer B dùng được ngay |
| 3 | Cột `tierLevel` | Thuộc tính của khách VIP | Có trong lớp `VipCustomer` | **Có hiện thực**, nhưng dữ liệu mẫu chưa gán giá trị |

**Về điểm 1 — vì sao nên bổ sung enum `ServiceCategory`:**

Trong Java, tên hằng số của enum không chứa khoảng trắng và không dùng dấu tiếng Việt có dấu, nên phải viết:

```java
public enum ServiceCategory {
    NUOC("Nước"),
    THIET_BI("Thiết bị"),
    DICH_VU("Dịch vụ"),
    TIEC_CU_TRANG("Tiệc cư trang"),
    TIEN_ICH("Tiện ích");

    private final String displayName;

    ServiceCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
```

Mỗi hằng số giữ một giá trị hiển thị tiếng Việt, nên bảng trong [mục 6.1.6](#616-servicategory) ghi tên hiển thị còn mã nguồn dùng tên không dấu. Lợi ích trực tiếp cho truy vấn Q14: lọc chính xác dịch vụ thiết bị bằng `ServiceCategory.THIET_BI` thay vì so chuỗi `Contains("thiết bị")` — cách sau rất dễ hỏng khi ai đó đổi cách viết.

**Về điểm 2 — vì sao thiết kế và cài đặt khác nhau mà vẫn đúng:**

ERD mô tả cách **lưu trữ** dữ liệu trong quan hệ; Java chọn cách **tận dụng đa hình** để không phải ghi `fieldType` thủ công mỗi lần tạo sân. Hai cách này cùng một nghiệp vụ, và việc lớp DTO đưa `fieldType` ra JSON là chỗ nối giữa hai bên. Đây chính là nội dung muốn trình bày ở [mục 5.4.3](#543-bảng-đối-chiếu-hai-lối-tổ-chức-dữ-liệu).

---

# 7. GIAO DIỆN DỮ LIỆU CHO DEVELOPER B

## 7.1 Hợp ồng dữ liệu và vì sao không sửa Java

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
| Q04 | Trường có doanh thu cao nhất | `totalAmount`, `fieldType` | `GroupBy`, `Max` |
| Q05 | Dịch vụ đang cung cấp | `active` | `Where`, `Select` |
| Q06 | Khách VIP có tỷ lệ giảm cao nhất | `customerType`, `discountRate` | `Where`, `OrderByDescending` |
| Q07 | Khách đặt nhiều lượt nhất | `customerId`, `bookingId` | `GroupBy`, `Count`, `OrderByDescending` |
| Q08 | Doanh thu theo từng loại sân | `fieldType`, `totalAmount` | `GroupBy`, `Sum` |
| Q09 | Lượt đặt trong một tháng cụ thể | `startTime` | `Where`, `OrderBy` |
| Q10 | Xếp hạng khách theo tổng thời gian đã dùng | `customerId`, `hours` | `GroupBy`, `Sum` |
| Q11 | Sân chưa từng được đặt | `fieldId`, `bookingId` | `Except`, `Any` |
| Q12 | Lượt đặt không kèm dịch vụ | `serviceId` | `Where`, `Any` |
| Q13 | Khách đã dùng từ 2 loại sân trở lên | `customerId`, `fieldType` | `GroupBy`, `Distinct`, `Count` |
| Q14 | Khách dùng dịch vụ vợt nhưng chưa đặt sân tennis | `serviceCategory`, `fieldType` | `Where`, `Any`, `GroupBy` |
| Q15 | Số lượt đặt theo giờ bắt đầu | `startTime` | `GroupBy`, `Count` |
| Q16 | Ngày có nhiều lượt đặt nhất | `startTime` | `GroupBy`, `OrderByDescending` |
| Q17 | Hóa đơn chưa thanh toán | `invoiceStatus` | `Where`, `OrderBy` |
| Q18 | Tổng doanh thu từ hóa đơn đã thanh toán | `invoiceStatus`, `amount` | `Where`, `Sum` |
| Q19 | Phân trang danh sách lượt đặt | `startTime` | `Skip`, `Take` |
| Q20 | So sánh doanh thu giờ cao điểm và ngoài giờ cao điểm | `peakHour`, `totalAmount` | `Where`, `GroupBy`, `Sum` |

**Hai truy vấn chứng minh ERD đã đủ trường:**

| Truy vấn | Nếu ERD thiếu trường | Bằng chứng ERD đã đủ |
|---|---|---|
| **Q13** — khách dùng ≥ 2 loại sân | Phải đoán loại sân từ chuỗi tên sân | Có cột `fieldType` trong `SPORTFIELD` |
| **Q20** — so sánh cao điểm và ngoài cao điểm | Phải tính lại từ `startTime` | Có cột `peakSurcharge` và `peakHour` |

---

# 8. PHỤ LỤC

## 8.1 Công thức tính phí

**Khung giờ cao điểm:**

| Hằng số | Giá trị | Ý nghĩa |
|---|---|---|
| `PEAK_START` | 17:00 | Bắt đầu khung cao điểm |
| `PEAK_END` | 20:00 | Kết thúc khung cao điểm |
| `PEAK_SURCHARGE_RATE` | 0.20 | Tỷ lệ phí cao điểm: 20% |

Khung là **[17:00, 20:00)** — có nghĩa lúc 20:00 đúng không còn thuộc giờ cao điểm.

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

## 8.2 Danh mục file trong dự án

| Đường dẫn | Nội dung |
|---|---|
| `docs/erd.drawio` | File ERD draw.io, 2 trang |
| `docs/DAC_TA_THIET_KE.md` | File đặc tả này |
| `docs/tools/gen_erd.py` | Script sinh file ERD |
| `docs/tools/check_erd.py` | Script kiểm tra bố cục ERD |
| `data/dataset.json` | Dữ liệu mẫu xuất ra JSON |
| `API_CONTRACT.md` | Hợp đồng API cho Developer B |
| `src/smartsporthub/model/` | 11 lớp thực thể, 4 kiểu enum, lớp hằng số `FieldPricing` |
| `src/smartsporthub/dto/` | 5 lớp DTO, lớp cơ sở `JsonDto`, bộ chuyển đổi `DtoMapper`, enum `FieldType` |
| `src/smartsporthub/interfaces/` | 2 interface |
| `src/smartsporthub/manager/` | 5 lớp quản lý |

## 8.3 Cách tái tạo ERD

```bash
python docs/tools/gen_erd.py docs/erd.drawio
python docs/tools/check_erd.py docs/erd.drawio
```

Script `gen_erd.py` sinh file `.drawio` từ dữ liệu khai báo trong code. Script `check_erd.py` đọc hình học từ file vừa sinh và báo cáo các ô chồng lấn hoặc tràn trang.

## 8.4 Kết quả kiểm chứng phần Java

| Hạng mục | Kết quả |
|---|---|
| Biên dịch với `-Xlint:all` | 0 cảnh báo |
| Phép thử tự động | 64/64 đạt |
| Bộ dữ liệu JSON | 6 khách · 6 sân · 6 dịch vụ · 12 lượt đặt · 12 hóa đơn |
| Kiểm tra API | `/api/health`, phân trang, mã lỗi 404 và 405 đều đúng |
| Tổng tiền của các hóa đơn `PAID` | 1.887.200 đồng |

---

*Tài liệu được biên soạn bởi Developer A. ERD vẽ bằng draw.io 31.7.0. Mã nguồn Java phát triển trên IntelliJ IDEA với Java 22.*