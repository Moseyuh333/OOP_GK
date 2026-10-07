#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Kiem tra bo cuc file .drawio: o chong lan nhau va o tran ra ngoai trang.

Doc thang hinh hoc tu file do gen_erd.py sinh ra, nen khong phai chep lai toa do.
File do chinh generator tao ra (khong phai tai ve tu mang), nen dung minidoc
mac dinh; ma dich chay cuc bo, khong dua du lieu tin tuong vao.

    python docs/tools/check_erd.py docs/erd.drawio
"""
import sys
import xml.dom.minidom as minidom


def load(path):
    doc = minidom.parse(path)
    pages = []
    for dg in doc.getElementsByTagName("diagram"):
        name = dg.getAttribute("name")
        model = dg.getElementsByTagName("mxGraphModel")[0]
        pw = int(model.getAttribute("pageWidth"))
        ph = int(model.getAttribute("pageHeight"))
        boxes = []
        for cell in model.getElementsByTagName("mxCell"):
            # Chi xet o dinh vi tri: bo qua duong ke va dong thuoc tinh
            # nam ben trong khung bang.
            if cell.getAttribute("vertex") != "1":
                continue
            if cell.getAttribute("parent") != "1":
                continue
            geo = cell.getElementsByTagName("mxGeometry")
            if not geo or not geo[0].hasAttribute("width"):
                continue
            g = geo[0]
            x = int(float(g.getAttribute("x") or 0))
            y = int(float(g.getAttribute("y") or 0))
            w = int(float(g.getAttribute("width")))
            h = int(float(g.getAttribute("height")))
            label = (cell.getAttribute("value") or cell.getAttribute("id"))
            label = label.replace("<br>", " ").replace("<b>", "")
            label = label.replace("</b>", "").replace("&nbsp;", " ")
            label = " ".join(label.split())[:26]
            boxes.append((cell.getAttribute("id"), label, x, y, w, h))
        pages.append((name, pw, ph, boxes))
    return pages


def check(page):
    name, pw, ph, boxes = page
    bad = 0
    for i in range(len(boxes)):
        id1, lab1, x1, y1, w1, h1 = boxes[i]
        r1, b1 = x1 + w1, y1 + h1
        if x1 < 0 or y1 < 0 or r1 > pw or b1 > ph:
            bad += 1
            print(f"  [TRAN ] {id1:9} {lab1:26} ({x1},{y1})-({r1},{b1})"
                  f" vuot trang {pw}x{ph}")
        for j in range(i + 1, len(boxes)):
            id2, lab2, x2, y2, w2, h2 = boxes[j]
            r2, b2 = x2 + w2, y2 + h2
            ox = min(r1, r2) - max(x1, x2)
            oy = min(b1, b2) - max(y1, y2)
            if ox > 0 and oy > 0:
                bad += 1
                print(f"  [CHONG] {id1:9} {lab1:24} x {id2:9} {lab2:24}"
                      f" ({ox}x{oy}px)")
    print(f"{name}: {len(boxes)} o · trang {pw}x{ph} · {bad} loi")
    return bad


def main():
    path = sys.argv[1] if len(sys.argv) > 1 else "docs/erd.drawio"
    total = 0
    for page in load(path):
        total += check(page)
    if total:
        print(f"\n=> {total} loi can sua")
        sys.exit(1)
    print("\n=> Bo cuc sach: khong co o chong lan, khong co o tran trang.")


if __name__ == "__main__":
    main()