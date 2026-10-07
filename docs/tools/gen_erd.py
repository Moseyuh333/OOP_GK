#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Sinh file .drawio (mxGraph XML) cho đặc tả MIS SmartSportHub — OOPR240279.

Trang 1: ERD — 9 bảng (5 thực thể gốc + 4 bảng loại sân theo kế thừa), đầy đủ
        thuộc tính khóa / thuộc tính mô tả / quan hệ, ký hiệu ER chuẩn.
Trang 2: Quy tắc chuyển quan hệ ER / RR thành lớp Java OOP, bảng ánh xạ
        9 bảng ER <-> 11 lớp thực thể, 5 enum, interface và công thức tính phí.

Cách chạy:
    python docs/tools/gen_erd.py docs/erd.drawio
"""
import sys
import xml.sax.saxutils as su

# ---------------------------------------------------------------- hằng số layout
HEADER_H = 34          # chiều cao dải tiêu đề của bảng
ROW_MIN = 42          # dòng thuộc tính, ghi chú ngắn (2 dòng chữ)
ROW_LONG = 58         # dòng thuộc tính, ghi chú vừa (3 dòng chữ)
ROW_XLONG = 74        # dòng thuộc tính, ghi chú dài hoặc công thức (4 dòng chữ)
WRAP_LIMIT = 44
WRAP_LIMIT_2 = 88

FONT_SIZE = 11
NOTE_SIZE = 9

ATTR_STYLE = (
    "shape=partialRectangle;collapsible=0;dropTarget=0;pointerEvents=0;"
    "fillColor=none;top=0;left=0;bottom=0;right=0;fontColor=default;align=left;"
    "verticalAlign=middle;spacingLeft=12;spacingRight=12;overflow=hidden;"
    "points=[[0,0.5],[1,0.5]];portConstraint=eastwest;rounded=0;html=1;"
    f"whiteSpace=wrap;fontSize={FONT_SIZE};"
)

TITLE_STYLE = (
    "text;html=1;align=left;verticalAlign=middle;fontSize=22;fontStyle=1;"
    "strokeColor=none;fillColor=none;fontColor=#1a1a1a;"
)
SUB_STYLE = (
    "text;html=1;align=left;verticalAlign=top;whiteSpace=wrap;fontSize=12;"
    "strokeColor=none;fillColor=none;fontColor=#444444;lineHeight=1.35;"
)
BOX_NOTE = (
    "rounded=1;whiteSpace=wrap;html=1;align=left;verticalAlign=top;"
    "fillColor=#f5f5f5;strokeColor=#b3b3b3;fontSize=12;spacing=12;"
    "fontColor=#1a1a1a;lineHeight=1.4;"
)

BLUE = ("#dae8fc", "#6c8ebf")
GREEN = ("#d5e8d4", "#82b366")
ORANGE = ("#ffe6cc", "#d79b00")
PURPLE = ("#e1d5e7", "#9673a6")
RED = ("#f8cecc", "#b85450")
YELLOW = ("#fff2cc", "#d6b656")
GREY = ("#f5f5f5", "#999999")

# ---------------------------------------------------------------- bảng ER
TABLES = [
    {
        "id": "E_SPORTFIELD",
        "name": "SPORTFIELD",
        "kind": "gốc",
        "fill": BLUE[0], "stroke": BLUE[1],
        "x": 60, "y": 300, "w": 430,
        "attrs": [
            ("PK", "fieldId", "String", "Khóa chính, do người thiết kế cấp: F001…F006"),
            ("", "fieldName", "String", "Tên sân, ví dụ: Sân bóng A"),
            ("", "fieldType", "Enum FieldType",
             "FOOTBALL · BADMINTON · TENNIS · PICKLEBALL — cột bắt buộc để thống kê theo loại"),
            ("", "basePricePerHour", "double", "Giá thuê một giờ: 60.000 – 150.000 đồng"),
            ("", "status", "Enum FieldStatus",
             "AVAILABLE (sẵn sàng) · MAINTENANCE (bảo trì) · OCCUPIED (đang dùng)"),
        ],
    },
    {
        "id": "E_FOOTBALL",
        "name": "FOOTBALL_FIELD",
        "kind": "kế thừa",
        "fill": ORANGE[0], "stroke": ORANGE[1],
        "x": 545, "y": 300, "w": 360,
        "attrs": [
            ("PK, FK", "fieldId", "String → SPORTFIELD",
             "Khóa chính đồng thời là khóa ngoại — quan hệ kế thừa 0..1"),
            ("", "nightLightFee", "double", "Phí đèn 50.000 đồng cho mỗi lượt thuê"),
        ],
    },
    {
        "id": "E_BADMINTON",
        "name": "BADMINTON_FIELD",
        "kind": "kế thừa",
        "fill": ORANGE[0], "stroke": ORANGE[1],
        "x": 960, "y": 300, "w": 380,
        "attrs": [
            ("PK, FK", "fieldId", "String → SPORTFIELD", "Khóa chính và khóa ngoại — kế thừa 0..1"),
            ("", "racketFee", "double", "Phí thuê vợt 20.000 đồng mỗi lượt"),
            ("", "matFee", "double", "Phí thảm 10.000 đồng mỗi lượt"),
        ],
    },
    {
        "id": "E_TENNIS",
        "name": "TENNIS_FIELD",
        "kind": "kế thừa",
        "fill": ORANGE[0], "stroke": ORANGE[1],
        "x": 545, "y": 560, "w": 360,
        "attrs": [
            ("PK, FK", "fieldId", "String → SPORTFIELD", "Khóa chính và khóa ngoại — kế thừa 0..1"),
            ("", "courtEquipmentFee", "double", "Phí dụng cụ 40.000 đồng mỗi lượt"),
        ],
    },
    {
        "id": "E_PICKLEBALL",
        "name": "PICKLEBALL_FIELD",
        "kind": "kế thừa",
        "fill": ORANGE[0], "stroke": ORANGE[1],
        "x": 960, "y": 560, "w": 390,
        "attrs": [
            ("PK, FK", "fieldId", "String → SPORTFIELD", "Khóa chính và khóa ngoại — kế thừa 0..1"),
            ("", "equipmentFeePerHour", "double", "Phí dụng cụ 15.000 đồng cho mỗi giờ thuê"),
        ],
    },
    {
        "id": "E_CUSTOMER",
        "name": "CUSTOMER",
        "kind": "gốc",
        "fill": BLUE[0], "stroke": BLUE[1],
        "x": 60, "y": 900, "w": 470,
        "attrs": [
            ("PK", "customerId", "String", "Khóa chính, do người thiết kế cấp: C001…C006"),
            ("", "fullName", "String", "Họ và tên khách hàng"),
            ("", "phoneNumber", "String", "Số điện thoại liên hệ"),
            ("", "email", "String", "Email dùng để gửi thông báo"),
            ("", "customerType", "Enum CustomerType", "STANDARD hoặc VIP"),
            ("", "discountRate", "double", "Tỷ lệ giảm của VIP: 0.10 / 0.12 / 0.15 — null nếu Standard"),
            ("", "tierLevel", "int", "Cấp thành viên VIP: 1, 2, 3 — null nếu Standard"),
        ],
    },
    {
        "id": "E_SERVICE",
        "name": "SERVICE",
        "kind": "gốc",
        "fill": GREEN[0], "stroke": GREEN[1],
        "x": 60, "y": 1340, "w": 470,
        "attrs": [
            ("PK", "serviceId", "String", "Khóa chính, do người thiết kế cấp: SV001…SV006"),
            ("", "name", "String", "Tên dịch vụ, ví dụ: Dụng cụ thể thao"),
            ("", "category", "Enum ServiceCategory", "Nhóm dịch vụ dùng để lọc — xem trang 2"),
            ("", "unitPrice", "double", "Đơn giá một lần dùng: 30.000 – 250.000 đồng"),
            ("", "active", "boolean", "false nghĩa là dịch vụ đang tạm ngưng cung cấp"),
        ],
    },
    {
        "id": "E_BOOKING",
        "name": "BOOKING",
        "kind": "gốc",
        "fill": PURPLE[0], "stroke": PURPLE[1],
        "x": 620, "y": 900, "w": 560,
        "attrs": [
            ("PK", "bookingId", "String", "Khóa chính, do người thiết kế cấp: B001…B012"),
            ("FK", "customerId", "String → CUSTOMER", "Bắt buộc — mỗi lượt đặt luôn có một khách"),
            ("FK", "fieldId", "String → SPORTFIELD", "Bắt buộc — mỗi lượt đặt luôn có một sân"),
            ("FK", "serviceId", "String → SERVICE", "Có thể null — khách thuê sân không kèm dịch vụ"),
            ("", "startTime", "LocalDateTime", "Thời điểm bắt đầu, ví dụ: 15/02/2026 18:00"),
            ("", "endTime", "LocalDateTime", "Thời điểm kết thúc, ví dụ: 15/02/2026 20:00"),
            ("", "status", "Enum BookingStatus",
             "COMPLETED (đã xong) · CONFIRMED (đã đặt) · CANCELLED (đã hủy)"),
            ("calc", "hours", "double", "Tự tính từ endTime − startTime, không lưu riêng"),
            ("calc", "peakHour", "boolean",
             "true khi 17:00 ≤ giờ bắt đầu &lt; 20:00 — cột này phục vụ truy vấn thống kê"),
            ("calc", "rentalFee", "double", "Tính theo bảng loại sân tương ứng của sân đã đặt"),
            ("calc", "peakSurcharge", "double",
             "basePricePerHour × hours × 0.20 khi 17:00 ≤ giờ bắt đầu &lt; 20:00"),
            ("calc", "serviceFee", "double", "unitPrice của dịch vụ — bằng 0 nếu không kèm dịch vụ"),
            ("calc", "discount", "double", "Theo chính sách ưu đãi — VIP giảm 10%, 12% hoặc 15%"),
            ("calc", "totalAmount", "double",
             "rentalFee + peakSurcharge + serviceFee − discount"),
        ],
    },
    {
        "id": "E_INVOICE",
        "name": "INVOICE",
        "kind": "gốc",
        "fill": RED[0], "stroke": RED[1],
        "x": 620, "y": 1720, "w": 500,
        "attrs": [
            ("PK", "invoiceId", "String", "Khóa chính, do người thiết kế cấp: I001…I012"),
            ("FK", "bookingId", "String → BOOKING", "Một lượt đặt sinh ra tối đa một hóa đơn"),
            ("FK", "customerId", "String → CUSTOMER", "Lưu trùng khách để lập báo cáo mà không cần join"),
            ("FK", "fieldId", "String → SPORTFIELD", "Lưu trùng sân để lập báo cáo mà không cần join"),
            ("", "issueDate", "LocalDate", "Ngày phát hành hóa đơn, ví dụ: 15/02/2026"),
            ("", "status", "Enum InvoiceStatus",
             "PAID (đã trả) · UNPAID (chưa trả) · REFUNDED (đã hoàn tiền)"),
            ("", "amount", "double", "Chụp lại totalAmount của lượt đặt tại lúc phát hành"),
        ],
    },
]

# Quan hệ: (id, nguồn, đích, tỷ lệ, nhãn, loại)
EDGES = [
    ("H1", "E_SPORTFIELD", "E_FOOTBALL", "1 : 0..1", "Kế thừa — sân bóng đá", "inherit"),
    ("H2", "E_SPORTFIELD", "E_BADMINTON", "1 : 0..1", "Kế thừa — sân cầu lông", "inherit"),
    ("H3", "E_SPORTFIELD", "E_TENNIS", "1 : 0..1", "Kế thừa — sân tennis", "inherit"),
    ("H4", "E_SPORTFIELD", "E_PICKLEBALL", "1 : 0..1", "Kế thừa — sân pickleball", "inherit"),
    ("R1", "E_CUSTOMER", "E_BOOKING", "1 : 0..N", "Khách đặt sân", "normal"),
    ("R2", "E_SPORTFIELD", "E_BOOKING", "1 : 0..N", "Sân được đặt", "normal"),
    ("R3", "E_SERVICE", "E_BOOKING", "0..1 : 0..N", "Dịch vụ kèm theo (có thể không có)", "optional"),
    ("R4", "E_BOOKING", "E_INVOICE", "1 : 0..1", "Sinh hóa đơn thanh toán", "single"),
    ("R5", "E_BADMINTON", "E_SERVICE", "—", "Dụng cụ vợt cho thuê tại sân cầu lông", "note"),
]

# Toạ độ đặt ô: (x, y, bề rộng). Tách riêng khỏi TABLES để chỉnh bố cục
# không đụng vào phần khai báo thuộc tính.
_LAYOUT = {
    "E_SPORTFIELD": (60, 300, 430),
    "E_FOOTBALL": (540, 300, 350),
    "E_BADMINTON": (940, 300, 390),
    "E_TENNIS": (540, 480, 350),
    "E_PICKLEBALL": (940, 490, 390),
    "E_CUSTOMER": (60, 800, 460),
    "E_SERVICE": (60, 1200, 460),
    "E_BOOKING": (600, 800, 560),
    "E_INVOICE": (600, 1610, 500),
}
for _t in TABLES:
    _t["x"], _t["y"], _t["w"] = _LAYOUT[_t["id"]]

BY_ID = {t["id"]: t for t in TABLES}


def esc(text):
    return su.escape(str(text))


def row_height(note):
    n = len(note)
    if n <= WRAP_LIMIT:
        return ROW_MIN
    if n <= WRAP_LIMIT_2:
        return ROW_LONG
    return ROW_XLONG


def table_height(tab):
    return HEADER_H + sum(row_height(a[3]) for a in tab["attrs"])


def attr_value(tag, name, dtype, note):
    tag_html = f"<b>{esc(tag)}</b> " if tag else ""
    head = f"{tag_html}<b>{esc(name)}</b> : {esc(dtype)}"
    if not note:
        return head
    return f"{head}<br/><font style='font-size:{NOTE_SIZE}px' color='#666666'>{esc(note)}</font>"


def table_cells(tab):
    rows = [row_height(a[3]) for a in tab["attrs"]]
    total = table_height(tab)
    style = (
        f"swimlane;fontStyle=1;childLayout=tableLayout;startSize={HEADER_H};"
        f"horizontal=1;rounded=1;arcSize=6;collapsible=0;whiteSpace=wrap;html=1;"
        f"align=left;spacingLeft=14;fontSize=14;fillColor={tab['fill']};"
        f"strokeColor={tab['stroke']};fontColor=#1a1a1a;swimlaneFillColor={tab['fill']};"
    )
    parent = (
        f'<mxCell id="{tab["id"]}" value="{esc(tab["name"])}" style="{style}" '
        f'vertex="1" parent="1">'
        f'<mxGeometry x="{tab["x"]}" y="{tab["y"]}" width="{tab["w"]}" '
        f'height="{total}" as="geometry"/></mxCell>'
    )
    children = []
    y = HEADER_H
    for i, (tag, name, dtype, note) in enumerate(tab["attrs"]):
        children.append(
            f'<mxCell id="{tab["id"]}_a{i}" '
            f'value="{esc(attr_value(tag, name, dtype, note))}" '
            f'style="{ATTR_STYLE}" vertex="1" connectable="0" parent="{tab["id"]}">'
            f'<mxGeometry y="{y}" width="{tab["w"]}" height="{rows[i]}" as="geometry"/>'
            f"</mxCell>"
        )
        y += rows[i]
    return parent, children


def edge_cells():
    out = []
    for eid, src, dst, ratio, label, kind in EDGES:
        if kind == "inherit":
            style = ("edgeStyle=orthogonalEdgeStyle;rounded=1;html=1;strokeWidth=2;"
                     "strokeColor=#d79b00;dashed=1;startArrow=ERone;startFill=0;"
                     "endArrow=ERzeroToOne;endFill=0;fontSize=10;fontColor=#b37400;"
                     "labelBackgroundColor=#fff2cc;")
        elif kind == "note":
            style = ("edgeStyle=orthogonalEdgeStyle;rounded=1;html=1;strokeWidth=1;"
                     "strokeColor=#82b366;dashed=1;endArrow=none;fontSize=10;"
                     "fontColor=#5b7c3a;labelBackgroundColor=#ffffff;")
        else:
            style = ("edgeStyle=orthogonalEdgeStyle;rounded=1;html=1;strokeWidth=1.5;"
                     "strokeColor=#4d4d4d;startArrow=ERone;startFill=0;"
                     "endArrow=ERmany;endFill=0;fontSize=11;labelBackgroundColor=#ffffff;")
            if kind == "optional":
                style = style.replace("startArrow=ERone;", "startArrow=ERzeroToOne;")
            if kind == "single":
                style = style.replace("endArrow=ERmany;", "endArrow=ERzeroToOne;")
        out.append(
            f'<mxCell id="{eid}" value="{esc(label + "   —   " + ratio)}" style="{style}" '
            f'edge="1" parent="1" source="{src}" target="{dst}">'
            f'<mxGeometry relative="1" as="geometry"/></mxCell>'
        )
    return out


def cell(cid, x, y, w, h, value, style):
    return (
        f'<mxCell id="{cid}" value="{esc(value)}" style="{style}" vertex="1" parent="1">'
        f'<mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>'
    )


def heading(cid, x, y, w, text, size=16):
    return cell(cid, x, y, w, 32, text,
                f"text;html=1;align=left;verticalAlign=middle;fontSize={size};fontStyle=1;"
                "strokeColor=none;fillColor=none;fontColor=#1a1a1a;")


def page1():
    cells = [
        cell("P1_TITLE", 60, 55, 1800, 46,
             "SMARTSPORTHUB — SƠ ĐỒ QUAN HỆ THỰC THỂ (ERD)", TITLE_STYLE),
        cell("P1_SUB", 60, 105, 1800, 110,
             "Môn OOPR240279 — Lập trình hướng đối tượng. Đặc tả dự án nhóm gồm: phát biểu "
             "vấn đề, xác định các thực thể, xác định thuộc tính khóa và thuộc tính mô tả, "
             "xác định mối liên kết, rồi xây dựng ERD.<br/>"
             "<b>Cấu trúc ERD:</b> 5 bảng thực thể gốc (SPORTFIELD, CUSTOMER, SERVICE, "
             "BOOKING, INVOICE) và 4 bảng loại sân nối bằng quan hệ kế thừa — mỗi bảng loại "
             "sân ứng với đúng một lớp con trong Java.<br/>"
             "<b>Ký hiệu:</b> &nbsp; <b>PK</b> thuộc tính khóa chính &nbsp;·&nbsp; <b>FK</b> "
             "khóa ngoại &nbsp;·&nbsp; <b>PK, FK</b> vừa là khóa chính vừa là khóa ngoại "
             "&nbsp;·&nbsp; <b>calc</b> thuộc tính tính toán, không lưu trong bảng &nbsp;·&nbsp; "
             "cặp số <b>1 : 0..N</b> cho biết số lượng tối thiểu và tối đa ở mỗi vế quan hệ "
             "&nbsp;·&nbsp; đường nét đứt màu cam là quan hệ kế thừa.", SUB_STYLE),
    ]
    for tab in TABLES:
        parent, kids = table_cells(tab)
        cells.append(parent)
        cells.extend(kids)
    cells.extend(edge_cells())

    cells.append(cell(
        "P1_NOTE", 1380, 300, 480, 560,
        "<b>GHI CHÚ THIẾT KẾ</b><br/><br/>"
        "<b>1. BOOKING là thực thể liên kết.</b><br/>"
        "Nó giải quyết quan hệ nhiều – nhiều giữa CUSTOMER và SPORTFIELD: một khách đặt được "
        "nhiều sân khác nhau, một sân phục vụ nhiều khách. Thiếu thực thể này thì quan hệ "
        "nhiều – nhiều sẽ phải tách thêm bảng trung gian.<br/><br/>"
        "<b>2. SERVICE là quan hệ 0..1 tương đối.</b><br/>"
        "Một lượt đặt <i>có thể</i> kèm hoặc không kèm dịch vụ, nên khóa ngoại serviceId cho "
        "phép null, không đặt ràng buộc bắt buộc.<br/><br/>"
        "<b>3. INVOICE lưu giá trị chụp (snapshot).</b><br/>"
        "customerId, fieldId và amount được sao chép sang lúc phát hành. Nhờ vậy báo cáo doanh "
        "thu theo khách hay theo sân không phải join ngược lại BOOKING, và kết quả không bị "
        "đổi khi lượt đặt sửa về sau.<br/><br/>"
        "<b>4. Bốn bảng loại sân không có quan hệ với nhau.</b><br/>"
        "Một sân chỉ thuộc đúng một loại, nên tồn tại nhiều nhất một bản ghi trong một trong bốn "
        "bảng này. Đây chính là cách biểu diễn quan hệ <b>kế thừa</b> trong mô hình quan hệ, "
        "đối xứng với thừa kế lớp trong Java.<br/><br/>"
        "<b>5. Các cột calc không tồn tại trong cơ sở dữ liệu.</b><br/>"
        "MIS hoàn chỉnh sẽ tách thành bảng phụ hoặc tính bằng view. Ở đồ án này công thức được "
        "giữ trong lớp Java và kiểm chứng bằng 64 phép thử tự động — đúng nguyên tắc tính theo "
        "nguồn sự kiện.", BOX_NOTE))

    cells.append(cell(
        "P1_DATA", 1380, 890, 480, 380,
        "<b>QUY MÔ DỮ LIỆU MẪU CHO CÁC THỰC THỂ</b><br/><br/>"
        "<b>CUSTOMER — 6 khách</b><br/>"
        "&nbsp;&nbsp;3 khách STANDARD và 3 khách VIP<br/><br/>"
        "<b>SPORTFIELD — 6 sân</b><br/>"
        "&nbsp;&nbsp;2 sân bóng đá, 2 sân cầu lông,<br/>"
        "&nbsp;&nbsp;1 sân tennis, 1 sân pickleball<br/>"
        "&nbsp;&nbsp;→ chia đều vào 4 bảng loại sân<br/><br/>"
        "<b>SERVICE — 6 dịch vụ</b><br/>"
        "&nbsp;&nbsp;có 1 dịch vụ đang tạm ngưng<br/><br/>"
        "<b>BOOKING — 12 lượt đặt</b><br/>"
        "&nbsp;&nbsp;đủ ba trạng thái, có lượt vào<br/>"
        "&nbsp;&nbsp;giờ cao điểm lẫn ngoài giờ cao<br/>"
        "&nbsp;&nbsp;điểm, có lượt không kèm dịch vụ<br/><br/>"
        "<b>INVOICE — 12 hóa đơn</b><br/>"
        "&nbsp;&nbsp;6 đã trả, 4 chưa trả, 2 đã hoàn", BOX_NOTE))

    cells.append(cell(
        "P1_RULE", 1380, 1300, 480, 380,
        "<b>QUY TẮC XÁC ĐỊNH KHÓA</b><br/><br/>"
        "• Khóa chính của cả chín bảng đều là <b>khóa tự nhiên do người thiết kế cấp</b>, theo "
        "quy ước: chữ cái viết tắt của thực thể ghép ba chữ số — C001, F001, SV001, B001, I001.<br/><br/>"
        "• Khóa <b>không</b> sinh tự động bởi hệ thống, để khi trình diễn chỉ cần chạy chương "
        "trình là có ngay bộ dữ liệu.<br/><br/>"
        "• Bốn bảng loại sân dùng chung <b>fieldId</b> làm khóa chính lẫn khóa ngoại trỏ về "
        "SPORTFIELD. Nhờ vậy một sân không thể xuất hiện ở hai bảng loại sân cùng lúc.<br/><br/>"
        "• Khóa ngoại không lặp lại thuộc tính mô tả: BOOKING không lưu customerName mà chỉ lưu "
        "customerId, nên không bao giờ xảy ra chuyện tên khách trong lượt đặt khác tên trong hồ sơ.",
        BOX_NOTE))

    cells.append(cell(
        "P1_IDX", 1380, 1710, 480, 400,
        "<b>GỢI Ý THỰC HIỆN KHI MỞ RỘNG HỆ THỐNG</b><br/><br/>"
        "Đây là các khuyến nghị vận hành, <b>không</b> đưa vào ERD vì không phải thuộc tính "
        "của thực thể:<br/><br/>"
        "<b>1. Chỉ mục (index)</b><br/>"
        "&nbsp;&nbsp;• BOOKING(status, startTime) — phục vụ các truy vấn thống kê theo trạng "
        "thái và theo ngày.<br/>"
        "&nbsp;&nbsp;• BOOKING(customerId) và BOOKING(fieldId) — phục vụ truy vấn theo khách "
        "và theo sân.<br/><br/>"
        "<b>2. Ràng buộc nghiệp vụ</b><br/>"
        "&nbsp;&nbsp;• endTime phải lớn hơn startTime.<br/>"
        "&nbsp;&nbsp;• Hai lượt đặt của cùng một sân không được trùng khung giờ.<br/>"
        "&nbsp;&nbsp;• Không cho đặt sân khi status = MAINTENANCE.<br/><br/>"
        "<b>3. Bảo mật dữ liệu</b><br/>"
        "&nbsp;&nbsp;• Không nhập trùng customerId, fieldId, serviceId, bookingId, invoiceId.<br/>"
        "&nbsp;&nbsp;• Khi xóa một lượt đặt đã phát hành hóa đơn thì phải từ chối thao tác xóa.",
        BOX_NOTE))

    return cells


def rule_box(cid, x, y, w, h, title, body):
    return cell(cid, x, y, w, h, f"<b>{esc(title)}</b><br/><br/>{body}",
                "rounded=1;whiteSpace=wrap;html=1;align=left;verticalAlign=top;fillColor=#eef4ff;"
                "strokeColor=#6c8ebf;fontSize=12;spacing=12;fontColor=#1a1a1a;lineHeight=1.4;")


def enum_box(cid, x, y, w, h, name, values, note):
    return cell(
        cid, x, y, w, h,
        f"<b>{esc(name)}</b><br/>"
        f"<font color='#b85450'><b>{esc(values)}</b></font><br/>"
        f"<font style='font-size:10px' color='#666666'>{esc(note)}</font>",
        "rounded=1;whiteSpace=wrap;html=1;align=left;verticalAlign=top;fillColor=#f8cecc;"
        "strokeColor=#b85450;fontSize=12;spacing=10;fontColor=#1a1a1a;lineHeight=1.4;")


def mapping_row(cid, y, ent, cls, note):
    return [
        cell(cid + "a", 60, y, 300, 78, f"<b>{esc(ent)}</b>",
             "rounded=1;whiteSpace=wrap;html=1;align=center;verticalAlign=middle;"
             "fillColor=#ffe6cc;strokeColor=#d79b00;fontSize=13;fontStyle=1;fontColor=#1a1a1a;"),
        cell(cid + "b", 375, y, 60, 78, "→",
             "text;html=1;align=center;verticalAlign=middle;fontSize=24;strokeColor=none;"
             "fillColor=none;fontColor=#4d4d4d;"),
        cell(cid + "c", 450, y, 420, 78, cls,
             "rounded=1;whiteSpace=wrap;html=1;align=left;verticalAlign=middle;fillColor=#d5e8d4;"
             "strokeColor=#82b366;fontSize=11;spacing=10;fontColor=#1a1a1a;lineHeight=1.3;"),
        cell(cid + "d", 890, y, 780, 78, note,
             "text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;strokeColor=none;"
             "fillColor=none;fontSize=11;fontColor=#444444;lineHeight=1.3;"),
    ]


def page2():
    cells = [
        cell("P2_TITLE", 60, 45, 1600, 46,
             "CHUYỂN CÁC QUAN HỆ ER / RR THÀNH CÁC LỚP TRONG PROJECT JAVA", TITLE_STYLE),
        cell("P2_SUB", 60, 95, 1600, 60,
             "Bước 2 của đặc tả. Chín bảng trong ERD trở thành mười một lớp thực thể trong thư mục "
             "<b>src/smartsporthub/model</b> của project IntelliJ IDEA. Bảng ánh xạ bên dưới là "
             "bản đối chiếu giữa hai hình thức tổ chức dữ liệu của cùng một nghiệp vụ.", SUB_STYLE),
    ]

    rules = [
        ("T1", "Bảng ER → một lớp",
         "Tên lớp viết hoa chữ cái đầu, không dấu gạch.<br/>Đặt trong thư mục <b>model/</b>."),
        ("T2", "Quan hệ RR → tham chiếu",
         "Không tạo bảng liên kết riêng.<br/>Lớp giữ <b>thuộc tính tham chiếu</b> tới lớp kia."),
        ("T3", "Thuộc tính khóa → bất biến",
         "Khai báo <b>private final</b>.<br/>Không có hàm thiết lập cho thuộc tính khóa."),
        ("T4", "Thuộc tính enum → java enum",
         "Tách thành <b>kiểu enum</b> trong model/.<br/>Dùng để kiểm tra giá trị hợp lệ."),
        ("T5", "Quan hệ kế thừa → lớp trừu tượng",
         "Bảng gốc thành <b>lớp trừu tượng</b>, mỗi bảng con thành <b>một lớp con</b> ghi đè "
         "phương thức trừu tượng."),
        ("T6", "Nhóm thao tác CRUD → lớp quản lý",
         "Mỗi thực thể gốc có một <b>manager</b> riêng trong manager/, giữ danh sách kiểu List."),
    ]
    for i, (cid, title, body) in enumerate(rules):
        cells.append(rule_box(cid, 60 + (i % 3) * 590,
                              165 + (i // 3) * 165, 560, 150, title, body))

    cells.append(heading("P2_M_TITLE", 60, 520, 1600,
                         "BẢNG ÁN XẠC: 9 BẢNG ER  →  15 LỚP JAVA"))
    mapping = [
        ("M1", "CUSTOMER", "Customer (trừu tượng)<br/>StandardCustomer · VipCustomer",
         "Một bảng sinh ra <b>một lớp trừu tượng và hai lớp con</b>, vì khách hàng chia thành "
         "hai nhóm có chính sách ưu đãi khác nhau."),
        ("M2", "SPORTFIELD", "SportField (trừu tượng)",
         "Chỉ một lớp trừu tượng, <b>không có lớp con trực tiếp</b> — các lớp con nằm ở bốn "
         "bảng loại sân bên dưới."),
        ("M3", "FOOTBALL_FIELD", "FootballField",
         "Ghi đè calculateRentalFee() và giữ thuộc tính nightLightFee."),
        ("M4", "BADMINTON_FIELD", "BadmintonField",
         "Ghi đè calculateRentalFee() và giữ hai thuộc tính racketFee, matFee."),
        ("M5", "TENNIS_FIELD", "TennisField",
         "Ghi đè calculateRentalFee() và giữ thuộc tính courtEquipmentFee."),
        ("M6", "PICKLEBALL_FIELD", "PickleballField",
         "Ghi đè calculateRentalFee() và giữ thuộc tính equipmentFeePerHour."),
        ("M7", "SERVICE", "Service",
         "Là bảng đơn giản không phân loại nên chỉ cần <b>một lớp đơn</b>, không có lớp con."),
        ("M8", "BOOKING", "Booking",
         "Lớp của thực thể liên kết, giữ <b>ba thuộc tính tham chiếu</b>: Customer, "
         "SportField và Service — trong đó Service cho phép null."),
        ("M9", "INVOICE", "Invoice",
         "Giữ <b>một tham chiếu Booking</b>, đồng thời chụp lại customerId, fieldId và amount "
         "để lập báo cáo."),
    ]
    y = 570
    for cid, ent, cls, note in mapping:
        cells.extend(mapping_row(cid, y, ent, cls, note))
        y += 86

    cells.append(heading("P2_E_TITLE", 60, y + 25, 1600,
                         "ENUM — CÁC TẬP GIÁ TRỊ RÀNG BUỘC CỦA THỰC THỂ"))
    enums = [
        ("EN1", "FieldType", "FOOTBALL, BADMINTON, TENNIS, PICKLEBALL",
         "Cột fieldType trong bảng SPORTFIELD. Trong Java được suy ra từ kiểu lớp con, nhưng "
         "trong ERD phải lưu thành cột để thống kê được theo loại."),
        ("EN2", "CustomerType", "STANDARD, VIP",
         "Lớp StandardCustomer trả về STANDARD, lớp VipCustomer trả về VIP."),
        ("EN3", "FieldStatus", "AVAILABLE, MAINTENANCE, OCCUPIED",
         "Trạng thái của sân: sẵn sàng, đang bảo trì, đang có người dùng."),
        ("EN4", "BookingStatus", "COMPLETED, CONFIRMED, CANCELLED",
         "Trạng thái lượt đặt. CANCELLED không tính vào doanh thu."),
        ("EN5", "InvoiceStatus", "PAID, UNPAID, REFUNDED",
         "Trạng thái thanh toán: đã trả, chưa trả, đã hoàn tiền."),
        ("EN6", "ServiceCategory", "NƯỚC, THIẾT_BỊ, DỊCH_VỤ, TIỆC_CƯ_TRANG, TIỆN_ÍCH",
         "Nhóm dịch vụ dùng để lọc, ví dụ nhóm THIẾT_BỊ phục vụ truy vấn tìm khách dùng vợt "
         "nhưng chưa từng đặt sân tennis."),
    ]
    for i, (cid, name, values, note) in enumerate(enums):
        cells.append(enum_box(cid, 60 + (i % 3) * 590,
                              y + 70 + (i // 3) * 170, 560, 155, name, values, note))

    bottom = y + 70 + 170 + 175
    cells.append(cell(
        "P2_I", 60, bottom, 800, 350,
        "<b>INTERFACE — TÁCH PHẦN MỞ RỘNG CHO LẬP TRÌNH HƯỚNG ĐỐI TƯỢNG</b><br/><br/>"
        "<b>IPromotional</b> — do lớp Customer hiện thực<br/>"
        "&nbsp;&nbsp;• calculatePromotionalDiscount(double amount) : double<br/>"
        "&nbsp;&nbsp;• getPromotionPolicy() : String<br/>"
        "&nbsp;&nbsp;→ Lớp StandardCustomer luôn trả về 0.0; lớp VipCustomer trả về tỷ lệ giảm "
        "lấy từ thuộc tính của chính mình.<br/><br/>"
        "<b>IPeakHourCalculable</b> — do bốn lớp con của SportField hiện thực<br/>"
        "&nbsp;&nbsp;→ Lớp Booking chỉ cộng phí cao điểm khi kiểm tra "
        "<i>field instanceof IPeakHourCalculable</i>.<br/><br/>"
        "<b>Ý nghĩa thiết kế:</b> tách phần biến động (khách VIP giảm giá, loại sân tính phí cao "
        "điểm) ra khỏi phần cố định. Thêm một loại sân mới hoặc một hạng khách mới chỉ cần viết "
        "thêm một lớp con, không phải sửa lại những lớp đang chạy tốt.",
        "rounded=1;whiteSpace=wrap;html=1;align=left;verticalAlign=top;fillColor=#e1d5e7;"
        "strokeColor=#9673a6;fontSize=12;spacing=12;fontColor=#1a1a1a;lineHeight=1.4;"))

    cells.append(cell(
        "P2_C", 900, bottom, 770, 350,
        "<b>CÔNG THỨC TÍNH PHÍ — Lớp FieldPricing (final, hàm khởi tạo private)</b><br/><br/>"
        "<b>Khung giờ cao điểm:</b> PEAK_START = 17:00 &nbsp;|&nbsp; PEAK_END = 20:00 &nbsp;|&nbsp; "
        "PEAK_SURCHARGE_RATE = 0.20<br/><br/>"
        "<b>Phí riêng theo loại sân:</b><br/>"
        "FOOTBALL_NIGHT_LIGHT_FEE = 50.000<br/>"
        "BADMINTON_RACKET_FEE = 20.000 &nbsp;|&nbsp; BADMINTON_MAT_FEE = 10.000<br/>"
        "TENNIS_EQUIPMENT_FEE = 40.000 &nbsp;|&nbsp; PICKLEBALL_EQUIPMENT_FEE_PER_HOUR = 15.000"
        "<br/><br/>"
        "<b>Công thức ghi đè trong từng lớp con:</b><br/>"
        "Sân bóng đá &nbsp;&nbsp;&nbsp;&nbsp;= basePricePerHour × giờ + 50.000<br/>"
        "Sân cầu lông = basePricePerHour × giờ + 20.000 + 10.000<br/>"
        "Sân tennis &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;= basePricePerHour × giờ + 40.000<br/>"
        "Sân pickleball = (basePricePerHour + 15.000) × giờ<br/><br/>"
        "<b>Phí cao điểm</b> = basePricePerHour × giờ × 0.20, chỉ áp dụng khi "
        "17:00 ≤ giờ bắt đầu &lt; 20:00<br/>"
        "<b>Tổng tiền</b> = phí thuê sân + phí cao điểm + phí dịch vụ − ưu đãi<br/><br/>"
        "<b>Ví dụ kiểm chứng</b> (lượt đặt B001, khách VIP giảm 10%):<br/>"
        "200.000 + 40.000 + 30.000 − 27.000 = <b>243.000 đồng</b>",
        "rounded=1;whiteSpace=wrap;html=1;align=left;verticalAlign=top;fillColor=#fff2cc;"
        "strokeColor=#d6b656;fontSize=12;spacing=12;fontColor=#1a1a1a;lineHeight=1.4;"))

    return cells


def diagram(did, name, cells, w, h):
    return (
        f'<diagram id="{did}" name="{esc(name)}">'
        f'<mxGraphModel dx="1500" dy="1000" grid="1" gridSize="10" guides="1" tooltips="1" '
        f'connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="{w}" '
        f'pageHeight="{h}" math="0" shadow="0">'
        f'<root><mxCell id="0"/><mxCell id="1" parent="0"/>{"".join(cells)}</root>'
        f"</mxGraphModel></diagram>"
    )


def main():
    out = sys.argv[1] if len(sys.argv) > 1 else "erd.drawio"
    xml = (
        '<?xml version="1.0" encoding="UTF-8"?>\n'
        '<mxfile host="app.diagrams.net" agent="hermes-smartsporthub-spec" '
        'version="24.7.17" type="device">'
        + diagram("d1", "1. ERD", page1(), 1950, 2150)
        + diagram("d2", "2. ER → Lớp Java OOP", page2(), 1850, 2200)
        + "</mxfile>"
    )
    with open(out, "w", encoding="utf-8") as fh:
        fh.write(xml)
    print(f"Đã ghi {out} — {len(xml)} ký tự — {len(TABLES)} bảng ER")
    for t in TABLES:
        print(f"  {t['name']:17} {len(t['attrs'])} thuộc tính  "
              f"cao {table_height(t):4}px  rộng {t['w']}px  ({t['kind']})")


if __name__ == "__main__":
    main()