#!/usr/bin/env python3
"""Accessibility audit of one screen, from a `uiautomator dump` of a phone.

Checks every tappable element for what a screen reader needs and what a thumb needs:
  NO-LABEL     tappable but nothing to read (no text or content description, own or inside it)
  SMALL-TARGET tappable but smaller than 48dp in width or height
  EDIT-LABEL   a text box with no content description (the screen reader cannot say what to type)
Usage:  python3 tools/a11y_audit.py dump.xml [--density 440]
Exit code 1 when there is any finding. See tools/a11y_audit.sh to run it against a connected phone.
"""
import re
import sys
import xml.etree.ElementTree as ET

BOUNDS = re.compile(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]")


def bounds(node):
    m = BOUNDS.match(node.get("bounds", ""))
    return tuple(int(x) for x in m.groups()) if m else None


def label_of(node):
    own = (node.get("text") or node.get("content-desc") or "").strip()
    if own:
        return own
    parts = [label_of(c) for c in node]
    return " / ".join(p for p in parts if p)


def audit(root, density):
    min_px = 48 * density / 160.0
    findings = []
    for node in root.iter("node"):
        b = bounds(node)
        if b is None:
            continue
        w, h = b[2] - b[0], b[3] - b[1]
        if w <= 0 or h <= 0:
            continue  # off-screen list item: not on screen, nothing to touch
        label = label_of(node)
        where = f"{node.get('class', '?').split('.')[-1]} {b} '{label[:40]}'"
        if node.get("clickable") == "true":
            if not label:
                findings.append(f"NO-LABEL     {where}")
            if w < min_px - 1 or h < min_px - 1:
                findings.append(f"SMALL-TARGET {where} is {w / density * 160:.0f}x{h / density * 160:.0f}dp")
        if node.get("class", "").endswith("EditText") and not (node.get("content-desc") or "").strip():
            findings.append(f"EDIT-LABEL    {where}")
    return findings


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    density = 440.0
    if "--density" in sys.argv:
        density = float(sys.argv[sys.argv.index("--density") + 1])
        args = [a for a in args if a != str(int(density)) and a != str(density)]
    if not args:
        print(__doc__)
        return 2
    root = ET.parse(args[0]).getroot()
    findings = audit(root, density)
    taps = sum(1 for n in root.iter("node") if n.get("clickable") == "true")
    print(f"{taps} tappable elements checked, {len(findings)} finding(s)")
    for f in findings:
        print("  " + f)
    return 1 if findings else 0


if __name__ == "__main__":
    sys.exit(main())
