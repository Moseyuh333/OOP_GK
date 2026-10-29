# -*- coding: utf-8 -*-
import sys
sys.path.insert(0, r'C:/Users/MINH KHOI/AppData/Local/hermes/tools')
from docx import Document
p = r'C:/Users/MINH KHOI/AppData/Local/hermes/attachments/DeCuongChiTiet_Nhom5_23162026_23162046_NoiDung_v4_CoHinhAnh_APA.docx'
d = Document(p)
# find first headings
cnt=0
for pg in d.paragraphs:
    if pg.style.name.startswith('Heading'):
        for run in pg.runs:
            print(pg.style.name, '|', repr(run.text[:60]), 'size=', run.font.size, 'bold=', run.bold, 'italic=', run.italic, 'color=', run.font.color.rgb if run.font.color.type else None, 'name=', run.font.name)
        cnt+=1
        if cnt>8: break