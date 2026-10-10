# API Contract — Java Core ↔ Developer B (C# ASP.NET Core)

Tài liệu này là hợp đồng tích hợp duy nhất giữa **Dev A (Java)** và
**Dev B (C# ASP.NET Core)**. Dev B chỉ cần đọc file này + file
`data/dataset.json` là đủ để dựng controller, không cần đọc code Java.

## 1. Cách lấy dữ liệu

| Cách | Lệnh / URL | Khi nào dùng |
|---|---|---|
| File JSON tĩnh | `data/dataset.json` (đã commit kèm repo) | Dev B seed database, viết test, chạy offline |
| API động (JDK thuần, không framework) | `java -cp out smartsporthub.api.SmartSportHubApi` → `http://localhost:8080` | Dev B muốn dữ liệu live, hoặc demo |

Xuất lại file JSON sau khi đổi seed (Git Bash):

```bash
javac -encoding UTF-8 -d out $(find src -name '*.java')
java -Dfile.encoding=UTF-8 -cp out smartsporthub.service.DataExportService
# ghi ra data/dataset.json
```

## 2. Endpoints

### 2.1. Đọc (GET)

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

### 2.2. Ghi (POST / PATCH)

| Method | URL | Mô tả |
|---|---|---|
| POST | `/api/bookings` | Đặt sân mới |
| POST | `/api/bookings/{id}/cancel` | Hủy lượt đặt |
| POST | `/api/bookings/{id}/complete` | Đánh dấu đã dùng xong |
| POST | `/api/bookings/{id}/reschedule` | Đổi khung giờ |
| POST | `/api/invoices/{id}/pay` | Thu tiền hóa đơn |
| POST | `/api/invoices/{id}/refund` | Hoàn tiền hóa đơn |
| PATCH | `/api/fields/{id}/status` | Đổi trạng thái sân |

Body ghi luôn là JSON phẳng với giá trị chuỗi.

`POST /api/bookings` — `fieldId`, `customerId`, `startTime`, `endTime` bắt buộc,
`serviceId` tuỳ chọn. Thời gian theo định dạng `2026-04-01T08:00` (không có
múi giờ). Phản hồi `201` kèm JSON booking đầy đủ; server tự sinh hóa đơn ở
trạng thái `UNPAID`.

```
POST /api/bookings
{"customerId":"C001","fieldId":"F001",
 "startTime":"2026-04-01T08:00","endTime":"2026-04-01T10:00",
 "serviceId":"SV001"}
```

`POST /api/bookings/{id}/reschedule` — `newStartTime`, `newEndTime` bắt buộc.

`PATCH /api/fields/{id}/status` — `status` là `AVAILABLE`, `OCCUPIED` hoặc
`MAINTENANCE`. Sân còn lượt đặt hiệu lực thì không chuyển sang `MAINTENANCE`.

### 2.3. Mã lỗi

| Mã | Khi nào |
|---|---|
| `400` | Thiếu trường bắt buộc, sai định dạng thời gian, giờ kết thúc trước giờ bắt đầu |
| `404` | Không tìm thấy khách / sân / dịch vụ / booking / hóa đơn, hoặc action lạ |
| `405` | Endpoint chỉ nhận GET mà gọi bằng phương thức khác |
| `409` | Trùng khung giờ, sân đang bảo trì, dịch vụ đã ngừng, trạng thái không hợp lệ (hủy booking đã hủy, thu hóa đơn đã thu…) |
| `500` | Lỗi đọc/ghi cơ sở dữ liệu |

Mọi lỗi trả `{"error": "thông điệp tiếng Việt"}`.

Quy ước chung:

- `Content-Type` luôn `application/json; charset=UTF-8`.
- Phân trang (chỉ áp dụng cho GET): `page` bắt đầu từ 1, `pageSize` mặc định 20.
- Tất cả số tiền là `number` (VND, không format dấu phẩy).

Ví dụ phân trang: `GET /api/bookings?page=2&pageSize=5` → 5 booking thứ 6–10
trong tổng 12 booking.

### 2.4. Dữ liệu bền vững

Server dùng SQLite tại `db/smartsporthub.db` (đổi được qua tham số dòng lệnh
thứ hai). Lần chạy đầu nạp seed; các lần sau nạp lại từ file nên dữ liệu còn
sau khi tắt ứng dụng.

Khởi chạy:

```
java -cp "out;lib/sqlite-jdbc-3.46.1.3.jar" smartsporthub.api.SmartSportHubApi [port] [dbPath]
```

## 3. Schema JSON

### 3.1. Customer — `/api/customers`

```json
{
  "id": "C001",
  "fullName": "Nguyễn Văn An",
  "phoneNumber": "0901234567",
  "email": "an.nguyen@gmail.com",
  "customerType": "VIP",
  "discountRate": 0.1,
  "promotionPolicy": "VIP hạng 1 giảm 10%"
}
```

| Key | Kiểu | Ghi chú |
|---|---|---|
| `customerType` | `"STANDARD"` \| `"VIP"` | Dev B map sang enum |
| `discountRate` | number 0–1 | 0.1 = giảm 10%, 0 = không giảm |
| `promotionPolicy` | string | Mô tả tiếng Việt, hiển thị trên hóa đơn |

### 3.2. Field — `/api/fields`

```json
{
  "id": "F001",
  "name": "Sân bóng đá mini A",
  "type": "FOOTBALL",
  "status": "AVAILABLE",
  "basePricePerHour": 300000
}
```

| Key | Kiểu | Ghi chú |
|---|---|---|
| `type` | `"FOOTBALL"` \| `"BADMINTON"` \| `"TENNIS"` \| `"PICKLEBALL"` | Đủ 4 loại sân |
| `status` | `"AVAILABLE"` \| `"MAINTENANCE"` | Seed có cả 2 trạng thái |
| `basePricePerHour` | number | Giá gốc/giờ, chưa gồm phí cao điểm |

### 3.3. Service — `/api/services`

```json
{
  "id": "S001",
  "name": "Thuê vợt cầu lông",
  "category": "EQUIPMENT",
  "unitPrice": 30000,
  "active": true
}
```

| Key | Kiểu | Ghi chú |
|---|---|---|
| `active` | boolean | `false` = dịch vụ ngừng cung cấp (seed có 1 service inactive để Dev B test lọc) |

### 3.4. Booking — `/api/bookings`

```json
{
  "id": "B001",
  "customerId": "C001",
  "customerName": "Nguyễn Văn An",
  "customerType": "VIP",
  "fieldId": "F001",
  "fieldType": "FOOTBALL",
  "fieldName": "Sân bóng A",
  "serviceId": "SV001",
  "serviceName": "Nước uống",
  "startTime": "2026-02-15T18:00:00",
  "endTime": "2026-02-15T20:00:00",
  "hours": 2.0,
  "status": "COMPLETED",
  "peakHour": true,
  "rentalFee": 200000,
  "peakSurcharge": 40000,
  "serviceFee": 30000,
  "discount": 27000,
  "totalAmount": 243000
}
```

| Key | Kiểu | Ghi chú |
|---|---|---|
| `serviceId` / `serviceName` | string \| null | Booking không dùng dịch vụ → cả 2 đều `null` (seed có case này) |
| `startTime` / `endTime` | string ISO `yyyy-MM-dd'T'HH:mm:ss` | Dev B parse bằng `DateTime.Parse` |
| `hours` | number | Số giờ thuê, cho phép lẻ (ví dụ 1.5) |
| `status` | `"COMPLETED"` \| `"CONFIRMED"` \| `"CANCELLED"` | `CANCELLED` không tính doanh thu |
| `peakHour` | boolean | Bắt đầu trong `[17:00, 20:00)` → true |
| `rentalFee` | number | `calculateRentalFee(hours)`, đã tách phí cao điểm |
| `peakSurcharge` | number | `basePricePerHour × hours × 0.20`, = 0 nếu ngoài giờ cao điểm |
| `serviceFee` | number | Tiền dịch vụ, = 0 nếu không dùng |
| `discount` | number | `VIP 10% × (rentalFee + peakSurcharge)`, Standard = 0 |
| `totalAmount` | number | `rentalFee + peakSurcharge + serviceFee − discount` |

Công thức tổng quát Dev B dựng lại ở C#:

```
rentalFee    = field.calculateRentalFee(hours)          // Java đa hình theo loại sân
peakSurcharge = peakHour ? basePricePerHour × hours × 0.20 : 0
serviceFee   = service == null ? 0 : service.unitPrice
discount     = customerType == VIP ? (rentalFee + peakSurcharge) × discountRate : 0
totalAmount  = rentalFee + peakSurcharge + serviceFee − discount
```

### 3.5. Invoice — `/api/invoices`

```json
{
  "id": "I001",
  "bookingId": "B001",
  "customerId": "C001",
  "fieldId": "F001",
  "issueDate": "2026-02-15",
  "status": "PAID",
  "amount": 243000
}
```

| Key | Kiểu | Ghi chú |
|---|---|---|
| `status` | `"PAID"` \| `"UNPAID"` \| `"REFUNDED"` | Booking `CANCELLED` ↔ invoice `REFUNDED` |
| `amount` | number | Luôn bằng `totalAmount` của booking tương ứng |
| `paid` | suy ra từ status | `PAID` = đã thu, `UNPAID`/`REFUNDED` = chưa thu |

## 4. Seed data đi kèm (đủ điều kiện kiểm thử)

- 6 field (đủ 4 loại, có `AVAILABLE` + `MAINTENANCE`)
- 6 customer (Standard + VIP, có 1 khách chưa từng booking)
- 6 service (có 1 service inactive chưa ai dùng)
- 12 booking (đủ `COMPLETED`/`CONFIRMED`/`CANCELLED`, peak + off-peak,
  1 booking không service, có khách nhiều booking, có sân chưa từng COMPLETED)
- 12 invoice (đủ `PAID`/`UNPAID`/`REFUNDED`, mỗi booking đúng 1 hóa đơn)

## 5. Checklist cho Dev B

1. Đọc `data/dataset.json` để lấy mẫu thật.
2. Dựng 5 entity C# theo đúng key mục 3 (`System.Text.Json`, `JsonPropertyName` giữ nguyên tên).
3. Viết lại công thức mục 3.4, test với booking `B001` phải ra `243000`.
4. Xử lý `serviceId = null`, `status = CANCELLED` (doanh thu = 0).
5. Gọi API live theo bảng mục 2 để kiểm tra phân trang + 404/405.
6. Phía C# **không mở file SQLite**: mọi thao tác ghi đi qua endpoint mục 2.2.
7. Bắt đầu server bằng `build.sh` rồi `java -cp "out;lib/sqlite-jdbc-3.46.1.3.jar" smartsporthub.api.SmartSportHubApi`.
