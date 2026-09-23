# SmartSportHub — Field Domain API Contract

Branch: `feature/field-domain`
Package root: `smartsporthub`

## API dùng chung

```java
SportField field = fieldManager.findFieldById("F001");
if (field == null) {
    // Không tìm thấy sân
}

double baseAndTypeFee = field.calculateRentalFee(hours);
FieldStatus status = field.getStatus();
field.setStatus(FieldStatus.MAINTENANCE);
```

`calculateRentalFee(hours)` đã bao gồm công thức riêng của từng loại sân nhưng **không tự cộng phí giờ cao điểm**.

## Phí giờ cao điểm

```java
LocalTime bookingStart = ...;

if (field instanceof IPeakHourCalculable peak
        && peak.isPeakHour(bookingStart)) {
    double peakSurcharge = peak.calculatePeakHourSurcharge(hours);
    // Developer B cộng peakSurcharge vào Booking/Invoice.
}
```

Khung giờ cao điểm: `[17:00, 20:00)`.
Tỷ lệ phí giờ cao điểm: `20%` trên `basePricePerHour × hours`.

## Phí riêng theo loại sân

| Loại sân | `calculateRentalFee(hours)` |
|---|---|
| Football | `basePricePerHour × hours` |
| Badminton | `basePricePerHour × hours + racketFee + matFee` |
| Tennis | `basePricePerHour × hours + courtEquipmentFee` |
| Pickleball | `basePricePerHour × hours + equipmentFee` |

Riêng football có API tính phụ thu để Booking gọi khi thực sự sử dụng đèn:

```java
if (field instanceof FootballField football) {
    double total = football.calculateRentalFee(hours, useNightLight);
}
```

## FieldManager

```java
FieldManager manager = new FieldManager();

manager.addField(field);                 // false nếu null hoặc trùng ID
manager.findFieldById("F001");           // null nếu không tìm thấy
manager.findAvailableFields();
manager.updateFieldStatus("F001", FieldStatus.MAINTENANCE);
manager.removeField("F001");
manager.searchByType(FootballField.class);
manager.displayAllFields();
```

## Compile và chạy test

Từ thư mục gốc repository trong Git Bash:

```bash
rm -rf out && mkdir -p out
javac -encoding UTF-8 -Xlint:all -d out $(find src test -name '*.java')
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 \
  -cp out smartsporthub.test.FieldDomainTest
```

## Quy tắc phối hợp

- Không sửa `SportField`, 4 subclass, `IPeakHourCalculable` hoặc `FieldManager` khi chưa thống nhất breaking change.
- Mã sân (`fieldId`) là bất biến sau khi tạo để `FieldManager` luôn duy nhất.
- Mọi validation thất bại dùng `IllegalArgumentException`.
- `findFieldById()` trả về `null` nếu không tìm thấy.
- `getAllFields()` trả về danh sách unmodifiable.
