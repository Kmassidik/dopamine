#!/usr/bin/env python3
"""Rebuild the small sample of lessons that ships inside the app (the first-launch seed).

The seed is a trimmed copy of the content repo in the same layout: manifest.json at the top and everything else
under content/. It holds the first lesson of a few units so the app works before the first Update.

Usage (from the app repo root):  python3 tools/sync_seed.py ../dopamine-content
"""
import json
import shutil
import sys
from pathlib import Path

SEED = Path("app/src/main/assets/content")
# The units in the sample (folder of the unit, relative to the content repo root) and which lesson to keep.
UNITS = [
    "content/math/algebra-1/linear-eq",
    "content/science/basics/matter",
    "content/swe/programming-basics/variables",
    "content/swe/programming-basics/decisions",
    "content/electronics/electronics-basics/ohms-law",
    "content/electronics/electronics-basics/circuits",
]
KEEP_LESSON = "lesson-01.json"


def main(repo: Path) -> None:
    m = json.loads((repo / "manifest.json").read_text(encoding="utf-8"))
    files, images = [], set()
    courses = []
    for course in m["courses"]:
        units = []
        for unit in course["units"]:
            lessons = [l for l in unit["lessons"]
                       if l["path"].rsplit("/", 1)[0] in UNITS and l["path"].endswith("/" + KEEP_LESSON)]
            if not lessons:
                continue
            u = dict(unit)
            u["lessons"] = lessons
            files += [l["path"] for l in lessons]
            if unit.get("notes"):
                files.append(unit["notes"]["path"])
            else:
                u.pop("notes", None)
            for l in lessons:
                lesson = json.loads((repo / l["path"]).read_text(encoding="utf-8"))
                for page in lesson.get("learn", []):
                    if "image" in page:
                        images.add(page["image"])
                for q in lesson.get("questions", []):
                    if "image" in q:
                        images.add(q["image"])
            units.append(u)
        if units:
            c = dict(course)
            c["units"] = units
            courses.append(c)
    wanted = {i for i in images}
    seed_images = [i for i in m["images"] if i["path"] in wanted]
    missing = wanted - {i["path"] for i in seed_images}
    if missing:
        sys.exit(f"images referenced by sample lessons are not in the manifest: {sorted(missing)}")
    used_cats = {c["categoryId"] for c in courses}
    out = {
        "schemaVersion": m["schemaVersion"],
        "contentVersion": m["contentVersion"].split("+")[0] + "+0",
        "minAppVersion": m["minAppVersion"],
        "categories": [c for c in m["categories"] if c["id"] in used_cats],
        "courses": courses,
        "images": seed_images,
    }
    if SEED.exists():
        shutil.rmtree(SEED)
    SEED.mkdir(parents=True)
    for rel in files + [i["path"] for i in seed_images]:
        dest = SEED / rel
        dest.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(repo / rel, dest)
    (SEED / "manifest.json").write_text(json.dumps(out, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"seed: {len(files)} files, {len(seed_images)} images, {sum(len(u['lessons']) for c in courses for u in c['units'])} lessons")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    main(Path(sys.argv[1]).resolve())
