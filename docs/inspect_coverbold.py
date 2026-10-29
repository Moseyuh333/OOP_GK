# -*- coding: utf-8 -*-
import sys
sys.path.insert(0, r'C:/Users/MINH KHOI/AppData/Local/hermes/tools')
from docx import Document
p = r'C:/Users/MINH KHOI/AppData/Local/hermes/attachments/DeCuongChiTiet_Nhom5_23162026_23162046_NoiDung_v4_CoHinhAnh_APA.docx'
d = Document(p)
for pg in d.paragraphs[:14]:
    for run in pg.runs:
        print(repr(run.text[:40]), 'bold=', run.bold, 'size=', run.font.size)
    print('---')