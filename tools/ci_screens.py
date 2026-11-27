#!/usr/bin/env python3
"""Drives the debug APK on a CI emulator, saving screenshots and the crash log into ./screens."""
import os
import re
import subprocess
import time
import xml.etree.ElementTree as ET

PACKAGE = "com.dpjha.slidingnumbersplus"
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
    for _ in range(4):
        adb("shell", "uiautomator", "dump", "/sdcard/ui.xml", check=False)
        xml = adb("shell", "cat", "/sdcard/ui.xml", check=False)
        if xml.startswith("<?xml"):
            return list(ET.fromstring(xml).iter("node"))
        time.sleep(2)
    return []


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


def main():
    os.makedirs(OUT, exist_ok=True)
    adb("install", "-r", "app/build/outputs/apk/debug/app-debug.apk")
    adb("shell", "settings", "put", "secure", "immersive_mode_confirmations", "confirmed", check=False)
    adb("logcat", "-c")
    adb("shell", "am", "start", "-W", "-n", f"{PACKAGE}/.MainActivity")
    shot("01-home", 15)
    # the consent form or a system dialog may cover the first launch
    for label in ("Consent", "Wait", "OK"):
        tap_text(label)
    if tap_text("Play"):
        shot("02-game", 5)
        # use hints until level 1 is solved or they run out
        for _ in range(3):
            tap_text("Hint")
            time.sleep(1)
        shot("03-hint", 0)
        back()
    for number, label in enumerate(["All levels", "Rewards", "Themes", "Shop", "Passport", "Stats"], start=4):
        if tap_text(label):
            shot(f"{number:02d}-{label.split()[-1].lower()}", 4)
            back()
    if tap_text("Play"):
        # second tile row/column taps: enough to see tiles move
        shot("10-daily-or-game", 4)
        back()
    with open(f"{OUT}/logcat.txt", "w") as f:
        f.write(adb("logcat", "-d", "-v", "brief", "AndroidRuntime:E", "*:S"))
    print("crash log:")
    print(open(f"{OUT}/logcat.txt").read()[-6000:])
    print("running:", adb("shell", "pidof", PACKAGE, check=False).strip() or "NO")


if __name__ == "__main__":
    main()
