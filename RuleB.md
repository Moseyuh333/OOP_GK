# Quy tắc làm việc — Developer B

Chỉ làm đúng phần việc của Developer B.

Trước khi code:
- Kiểm tra cấu trúc dự án hiện tại và các file source có liên quan.
- Đọc `FIELD_DOMAIN_API.md` trước và xem đây là hợp đồng API cố định của Developer A.

Quy tắc bắt buộc:
- Làm đúng theo cấu trúc dự án hiện tại.
- Giữ nguyên cách đặt tên, package và style code hiện có.
- Chỉ thực hiện những thay đổi tối thiểu cần thiết cho task hiện tại.
- Không tự ý sửa, xóa, đổi tên, di chuyển hoặc refactor code không liên quan.
- Không sửa các file thuộc Field Domain của Developer A.
- Không tạo class, API hoặc logic trùng với phần Developer A đã làm.
- Không tự ý thêm hoặc cài đặt thư viện, framework, SDK, plugin, build tool hoặc phần mềm mới.
- Không thay đổi cấu hình project hoặc cấu hình build.
- Không tự tạo Maven, Gradle hoặc hệ thống build mới nếu project hiện tại không dùng.
- Không sửa `Main.java` nếu chưa được yêu cầu cụ thể.
- Không tự ý mở rộng chức năng ngoài yêu cầu bài tập.
- Không tự “cải thiện” các phần code đang hoạt động nếu không liên quan đến task hiện tại.

Nếu task yêu cầu phải:
- sửa API của Developer A,
- sửa code ngoài phạm vi phần B,
- đổi cấu trúc project,
- thêm dependency,
- cài phần mềm,
- hoặc thay đổi build configuration,

thì KHÔNG được tự ý thực hiện.

Hãy dừng lại và báo rõ:
- vấn đề đang chặn task,
- file/API nào gây ra vấn đề,
- thay đổi tối thiểu cần thiết là gì.

Sau mỗi task:
- compile/test bằng cách mà project hiện tại đang sử dụng;
- chỉ sửa các lỗi phát sinh từ phần code vừa thay đổi;
- báo rõ các file đã tạo;
- báo rõ các file đã sửa;
- xác nhận các file không liên quan không bị thay đổi.