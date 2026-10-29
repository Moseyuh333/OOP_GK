# -*- coding: utf-8 -*-
import sys
sys.path.insert(0, r'C:/Users/MINH KHOI/AppData/Local/hermes/tools')
from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
p = r'C:/Users/MINH KHOI/AppData/Local/hermes/attachments/DeCuongChiTiet_Nhom5_23162026_23162046_NoiDung_v4_CoHinhAnh_APA.docx'
d = Document(p)
for pg in d.paragraphs[:12]:
    print(repr(pg.text[:60]), 'align=', pg.alignment, 'style=', pg.style.name)