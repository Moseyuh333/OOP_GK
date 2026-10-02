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

| Method | URL | Mô tả |
|---|---|---|
| GET | `/api/health` | Kiểm tra server sống + đếm entity |
| GET | `/api/customers?page=1&pageSize=20` | Danh sách khách hàng |
| GET | `/api/fields?page=1&pageSize=20` | Danh sách sân |
| GET | `/api/services?page=1&pageSize=20` | Danh sách dịch vụ |
| GET | `/api/bookings?page=1&pageSize=20` | Danh sách booking (đã tính tiền) |
| GET | `/api/invoices?page=1&pageSize=20` | Danh sách hóa đơn |
| GET | `/api/data` | Snapshot toàn bộ 5 tập (không phân trang) |

Quy ước chung:

- Chỉ hỗ trợ `GET`. Method khác trả `405 {"error": "..."}`.
- URL không khớp trả `404 {"error": "Không tìm thấy ..."}`.
- `Content-Type` luôn `application/json; charset=UTF-8`.
- Phân trang: `page` bắt đầu từ 1, `pageSize` mặc định 20, tối đa 200.
- Tất cả số tiền là `number` (VND, không format dấu phẩy).

Ví dụ phân trang: `GET /api/bookings?page=2&pageSize=5` → 5 booking thứ 6–10
trong tổng 12 booking.

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
