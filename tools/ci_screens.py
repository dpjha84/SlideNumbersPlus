#!/usr/bin/env python3
"""Drives the debug APK on a CI emulator, saving screenshots and the crash log into ./screens."""
import os
import re
import subprocess
import time
import xml.etree.ElementTree as ET

PACKAGE = "com.dpjha.slidingnumbersplus"
# push a change to this file to run the check on the branch
OUT = "screens"


def adb(*args, check=True, binary=False):
    result = subprocess.run(["adb", *args], capture_output=True, check=check, timeout=180)
    return result.stdout if binary else result.stdout.decode(errors="replace")


def shot(name, wait=3):
    time.sleep(wait)
    with open(f"{OUT}/{name}.png", "wb") as f:
        f.write(adb("exec-out", "screencap", "-p", binary=True))
    print("shot", name, flush=True)


def nodes():
    # uiautomator gives up while something on screen is animating, so try several times
    for _ in range(8):
        adb("shell", "rm", "-f", "/sdcard/ui.xml", check=False)
        adb("shell", "uiautomator", "dump", "/sdcard/ui.xml", check=False)
        xml = adb("shell", "cat", "/sdcard/ui.xml", check=False)
        if xml.startswith("<?xml"):
            return list(ET.fromstring(xml).iter("node"))
        time.sleep(2)
    return []


def report_hangs():
    """Prints why the system thought the app hung, if it did."""
    log = adb("logcat", "-d", "-v", "brief", "ActivityManager:E", "*:S", check=False)
    if "ANR in" not in log:
        print("no ANR", flush=True)
        return
    print(log[log.index("ANR in"):][:3000], flush=True)
    trace = adb("shell", "dumpsys", "activity", "lastanr-traces", check=False)
    start = trace.find(f"Cmd line: {PACKAGE}")
    main_at = trace.find('"main"', start)
    print("main thread at the hang:")
    print(trace[main_at:main_at + 3500], flush=True)


def tap_text(label):
    for node in nodes():
        if node.get("text") == label or node.get("content-desc") == label:
            x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds")))
            adb("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))
            return True
    print("not found:", label, flush=True)
    return False


def back():
    adb("shell", "input", "keyevent", "BACK")
    time.sleep(1)


def solve(start, rows, cols):
    """Shortest list of cells to tap (chain slides allowed)."""
    goal = tuple(range(1, rows * cols)) + (0,)
    seen = {tuple(start)}
    queue = [(tuple(start), [])]
    for state, path in queue:
        if state == goal:
            return path
        gap = state.index(0)
        for cell in range(rows * cols):
            same_row = cell // cols == gap // cols
            if cell == gap or not (same_row or cell % cols == gap % cols):
                continue
            step = (1 if cell > gap else -1) * (1 if same_row else cols)
            board = list(state)
            for at in range(gap, cell, step):
                board[at] = board[at + step]
            board[cell] = 0
            if tuple(board) not in seen:
                seen.add(tuple(board))
                queue.append((tuple(board), path + [cell]))
    raise ValueError("unsolvable")


def board_cells(rows, cols):
    for node in nodes():
        if node.get("content-desc", "").startswith("Puzzle board"):
            x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds")))
            return [(x1 + (x2 - x1) * (2 * (i % cols) + 1) // (2 * cols),
                     y1 + (y2 - y1) * (2 * (i // cols) + 1) // (2 * rows)) for i in range(rows * cols)]
    raise ValueError("no board on screen")


def tap(point):
    adb("shell", "input", "tap", str(point[0]), str(point[1]))


def launch():
    adb("shell", "am", "start", "-W", "-n", f"{PACKAGE}/.MainActivity")


def seed_save(**values):
    """Overwrites the save of the debug build."""
    entries = "".join(f'<string name="{k}">{v}</string>' for k, v in dict(v="1", **values).items())
    with open("seed.xml", "w") as f:
        f.write(f"<?xml version='1.0' encoding='utf-8'?><map>{entries}</map>")
    adb("shell", "am", "force-stop", PACKAGE)
    adb("push", "seed.xml", "/data/local/tmp/seed.xml")
    adb("shell", f"run-as {PACKAGE} cp /data/local/tmp/seed.xml shared_prefs/snp3.xml")


def main():
    os.makedirs(OUT, exist_ok=True)
    adb("install", "-r", "app/build/outputs/apk/debug/app-debug.apk")
    adb("shell", "settings", "put", "secure", "immersive_mode_confirmations", "confirmed", check=False)
    # a freshly booted emulator is still busy for a minute or two
    time.sleep(90)
    adb("logcat", "-c")
    launch()
    shot("01-home", 15)
    # the consent form or a system dialog may cover the first launch
    for label in ("Consent", "Wait", "OK"):
        tap_text(label)

    report_hangs()
    print(adb("shell", "dumpsys", "gfxinfo", PACKAGE, check=False)[:1500], flush=True)

    # level 1 is the same for everyone: play it to the end
    tap_text("Play")
    time.sleep(4)
    path = solve([3, 1, 2, 4, 7, 0, 8, 5, 6], 3, 3)
    cells = board_cells(3, 3)
    print("solution", path, flush=True)
    tap(cells[path[0]])
    # leave and come back: the board must still be there
    time.sleep(1)
    adb("shell", "am", "force-stop", PACKAGE)
    launch()
    shot("02-resume-home", 8)
    if not tap_text("Resume"):
        raise SystemExit("the saved board was not offered")
    time.sleep(3)
    shot("03-resumed-board", 0)
    for cell in path[1:]:
        tap(cells[cell])
        time.sleep(0.4)
    shot("04-wave", 0)
    shot("04b-fireworks", 0.3)
    shot("04c-fireworks", 0.3)
    shot("05-win", 4)
    tap_text("Next level")
    shot("06-level2", 4)
    back()

    for number, label in enumerate(["All levels", "Themes"], start=7):
        if tap_text(label):
            shot(f"{number:02d}-{label.split()[-1].lower()}", 4)
            back()

    # later skies and bigger boards
    for name, level in (("09-london-night-snow", 5), ("10-paris-rain", 9), ("11-tokyo-snow", 13), ("12-dubai", 29),
                        ("13-rome-6x6", 35), ("14-mumbai", 36)):
        seed_save(level=level, reached=36, coins=500)
        launch()
        time.sleep(7)
        tap_text("Continue") or tap_text("Resume")
        shot(name, 4)

    with open(f"{OUT}/logcat.txt", "w") as f:
        f.write(adb("logcat", "-d", "-v", "brief", "AndroidRuntime:E", "*:S"))
    report_hangs()
    print("crash log:")
    print(open(f"{OUT}/logcat.txt").read()[-6000:])
    print("running:", adb("shell", "pidof", PACKAGE, check=False).strip() or "NO")


if __name__ == "__main__":
    main()
