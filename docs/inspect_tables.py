# -*- coding: utf-8 -*-
import sys
sys.path.insert(0, r'C:/Users/MINH KHOI/AppData/Local/hermes/tools')
from docx import Document
from docx.oxml.ns import qn
p = r'C:/Users/MINH KHOI/AppData/Local/hermes/attachments/DeCuongChiTiet_Nhom5_23162026_23162046_NoiDung_v4_CoHinhAnh_APA.docx'
d = Document(p)
for ti, tb in enumerate(d.tables):
    print('TABLE', ti, 'style=', tb.style.name, 'rows', len(tb.rows), 'cols', len(tb.columns))
    # print first row text
    print('  header:', [c.text.strip()[:30] for c in tb.rows[0].cells])
    # table grid properties
    tblPr = tb._tbl.tblPr
    print('  tblPr:', tblPr.xml[:400])