#!/usr/bin/env python3
"""A 20 s upbeat arrangement of the game's London theme on the game's own instruments, timed to the reel.

Usage: make_promo_music.py OUT.wav SAMPLE_DIR
SAMPLE_DIR holds the unpacked npm packages tonejs-instrument-{piano,guitar-nylon,harp,flute}-mp3-* (CC BY 3.0).
120 BPM: a tile move on every eighth note, the board solves on beat 8, scene cuts on beats 11, 16, 22, 27, 32."""
import sys, os, re, glob, subprocess, wave
import numpy as np

SR, BEAT, TOTAL = 44100, 0.5, 20.0
out_path, root = sys.argv[1], sys.argv[2]
NAMES = {"C": 0, "D": 2, "E": 4, "F": 5, "G": 7, "A": 9, "B": 11}
rng = np.random.default_rng(5)

def midi(n):
    m = re.fullmatch(r"([A-G])(#|s)?(\d)", n)
    return 12 * (int(m[3]) + 1) + NAMES[m[1]] + (1 if m[2] else 0)

BANK = {}
for inst in ("piano", "guitar-nylon", "harp", "flute"):
    d = glob.glob(os.path.join(root, f"tonejs-instrument-{inst}-mp3-*[0-9]", ""))[0]
    BANK[inst] = {}
    for f in glob.glob(os.path.join(d, "**", "*.mp3"), recursive=True):
        stem = os.path.basename(f)[:-4]
        if re.fullmatch(r"[A-G]s?\d", stem): BANK[inst][midi(stem)] = f
CACHE = {}

def sample(inst, m):
    base = min(BANK[inst], key=lambda k: abs(k - m))
    path = BANK[inst][base]
    if path not in CACHE:
        raw = subprocess.run(["ffmpeg", "-v", "error", "-i", path, "-f", "f32le", "-ac", "1", "-ar", str(SR), "-"], capture_output=True, check=True).stdout
        a = np.frombuffer(raw, np.float32).copy()
        a = a[np.argmax(np.abs(a) > 0.02 * np.abs(a).max()):]  # drop leading silence so notes land on time
        CACHE[path] = a / max(1e-6, np.abs(a).max())
    a, rate = CACHE[path], 2 ** ((m - base) / 12)
    return np.interp(np.arange(0, len(a) - 1, rate), np.arange(len(a)), a)

mix = np.zeros((int(SR * (TOTAL + 2)), 2))

def add(sig, t, vol, pan=0.0):
    i = int(t * SR)
    sig = sig[: len(mix) - i]
    mix[i:i + len(sig), 0] += sig * vol * (1 - pan) ** 0.5
    mix[i:i + len(sig), 1] += sig * vol * (1 + pan) ** 0.5

def note(inst, name, t, dur, vol, pan=0.0, release=0.12):
    s = sample(inst, midi(name) if isinstance(name, str) else name)[: int((dur + release) * SR)].copy()
    r = min(len(s), int(release * SR))
    if r: s[-r:] *= np.linspace(1, 0, r)
    add(s, t, vol, pan)

def t_(n): return np.arange(int(n * SR)) / SR

def kick(t, v):
    x = t_(0.22)
    add(np.sin(2 * np.pi * (52 * x + 70 * 0.03 * (1 - np.exp(-x / 0.03)))) * np.exp(-x / 0.08), t, v)

def noise(n, hp):
    a = rng.standard_normal(int(n * SR))
    for _ in range(hp): a = np.diff(a, prepend=0)
    return a / np.abs(a).max()

def clap(t, v):
    x = t_(0.16)
    env = sum(np.where(x >= d, np.exp(-(x - d) / 0.022), 0) for d in (0, 0.011, 0.023))
    add(noise(0.16, 1) * env, t, v * 0.6)

def shaker(t, v, pan=0.25):
    x = t_(0.07)
    add(noise(0.07, 2) * np.minimum(1, x / 0.012) * np.exp(-x / 0.022), t, v, pan)

# the London theme from the game: (bass, chord), melody as (note, beats)
BARS = [
    ("F2", "C4 E4 A4", "A4:1.5 C5:.5 E5:2"), ("A2", "C4 E4 G4", "D5:1 C5:1 G4:2"),
    ("D3", "A3 C4 F4", "A4:1.5 D5:.5 F5:2"), ("C3", "G3 C4 E4", "E5:1 D5:1 C5:2"),
]
SOLVE_BEAT, END_BEAT = 8, 36

for bar in range(10):
    start = bar * 4
    if start >= END_BEAT: break
    bass, chord, mel = BARS[bar % 4]
    chord = chord.split()
    big = start >= SOLVE_BEAT
    for e in range(8):                       # eighth notes
        t = (start + e / 2) * BEAT
        note("piano", (chord + [chord[1]])[[0, 1, 2, 3, 2, 1, 2, 1][e]], t, 0.22, 0.13 if big else 0.10, -0.25)
        shaker(t, (0.06 if e % 2 else 0.035) * (1 if big else 0.6))
        if e % 2:                            # bright off-beat strums
            for j, n in enumerate(chord):
                note("guitar-nylon", midi(n) + 12, t + j * 0.008, 0.16, 0.17 if big else 0.11, 0.3, 0.08)
    for b in range(4):
        t = (start + b) * BEAT
        note("piano", midi(bass) + (12 if b % 2 else 0), t, 0.3, 0.30 if big else 0.2)
        if big:
            kick(t, 0.55)
            if b % 2: clap(t, 0.30)
        elif b % 2 == 0:
            kick(t, 0.35)
    if big:                                  # the tune, doubled by flute from the second half
        at = 0.0
        for item in mel.split():
            n, length = item.split(":")
            note("piano", n, (start + at) * BEAT, float(length) * BEAT, 0.34, 0.05, 0.25)
            note("harp", midi(n) + 12, (start + at) * BEAT, float(length) * BEAT, 0.16, -0.1, 0.3)
            if start >= 20: note("flute", n, (start + at) * BEAT, float(length) * BEAT * 0.9, 0.17, 0.15)
            at += float(length)

# one rising harp note per tile move
SCALE = ["C4", "D4", "E4", "G4", "A4", "C5", "D5", "E5", "G5", "A5", "C6", "D6", "E6", "G6", "A6"]
for i, n in enumerate(SCALE):
    note("harp", n, 0.25 + i * 0.25, 0.3, 0.20 + i * 0.012, 0.0, 0.2)
# lift into the solve, then a harp sweep and chord
for k in range(8):
    clap((6 + k / 4) * BEAT, 0.07 + k * 0.025)
sweep = t_(2 * BEAT)
add(noise(2 * BEAT, 1) * (sweep / sweep[-1]) ** 2.5, 6 * BEAT, 0.09)
for i, n in enumerate(["C5", "E5", "G5", "C6", "E6", "G6", "C7"]):
    note("harp", n, SOLVE_BEAT * BEAT + i * 0.045, 1.2, 0.22, -0.4 + i * 0.13, 0.6)
add(noise(1.2, 1) * np.exp(-t_(1.2) / 0.35), SOLVE_BEAT * BEAT, 0.08)
# final chord
end = END_BEAT * BEAT
kick(end, 0.6); clap(end, 0.3)
for i, n in enumerate(["C3", "G3", "C4", "E4", "G4", "C5", "E5"]):
    note("piano", n, end + i * 0.018, 2.0, 0.22, 0.0, 1.0)
    note("harp", midi(n) + 12, end + i * 0.05, 2.0, 0.14, 0.2, 1.0)

# a little room, then level it
wet = mix.copy()
for delay, gain in ((0.029, 0.16), (0.047, 0.12), (0.089, 0.09), (0.131, 0.06)):
    d = int(delay * SR)
    wet[d:] += mix[:-d, ::-1] * gain
wet = wet[: int(TOTAL * SR)]
wet[-int(0.6 * SR):] *= np.linspace(1, 0, int(0.6 * SR))[:, None]
wet /= np.percentile(np.abs(wet), 99.7)
wet = np.tanh(wet * 0.7) * 0.92
with wave.open(out_path, "wb") as w:
    w.setnchannels(2); w.setsampwidth(2); w.setframerate(SR)
    w.writeframes((wet * 32767).astype("<i2").tobytes())
