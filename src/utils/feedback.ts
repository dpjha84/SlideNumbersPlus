import { Capacitor } from '@capacitor/core';
import { Haptics, ImpactStyle, NotificationType } from '@capacitor/haptics';
import { ThemeConfig } from '../types/game';

export type HapticPattern = 'move' | 'lock' | 'win' | 'error' | 'button';

let audioCtx: AudioContext | null = null;

function getAudioContext(): AudioContext | null {
  if (typeof window === 'undefined') return null;
  if (!audioCtx) {
    const Ctx =
      window.AudioContext ||
      (window as unknown as { webkitAudioContext?: typeof AudioContext })
        .webkitAudioContext;
    if (Ctx) {
      audioCtx = new Ctx();
    }
  }
  if (audioCtx && audioCtx.state === 'suspended') {
    audioCtx.resume().catch(() => {});
  }
  return audioCtx;
}

export function triggerHaptic(pattern: HapticPattern, enabled: boolean): void {
  if (!enabled) return;

  if (Capacitor.isNativePlatform()) {
    try {
      switch (pattern) {
        case 'move':
          void Haptics.impact({ style: ImpactStyle.Light });
          return;
        case 'lock':
          void Haptics.impact({ style: ImpactStyle.Medium });
          return;
        case 'win':
          void Haptics.notification({ type: NotificationType.Success });
          return;
        case 'error':
          void Haptics.notification({ type: NotificationType.Error });
          return;
        case 'button':
          void Haptics.selectionStart();
          return;
      }
    } catch {
      // Fallback to navigator.vibrate below
    }
  }

  if (typeof navigator === 'undefined' || !('vibrate' in navigator)) {
    return;
  }
  try {
    switch (pattern) {
      case 'move':
        navigator.vibrate(14);
        break;
      case 'lock':
        navigator.vibrate([10, 24, 18]);
        break;
      case 'win':
        navigator.vibrate([25, 45, 30, 45, 70]);
        break;
      case 'error':
        navigator.vibrate([8, 25, 8]);
        break;
      case 'button':
        navigator.vibrate(8);
        break;
    }
  } catch {
    // Ignore unsupported browser vibration blocks
  }
}

export function playGameSound(
  type: 'move' | 'lock' | 'win' | 'error' | 'coin' | 'unlock',
  enabled: boolean,
  soundProfile: ThemeConfig['soundProfile'] = 'warm'
): void {
  if (!enabled) return;
  const ctx = getAudioContext();
  if (!ctx) return;

  const now = ctx.currentTime;

  const playTone = (
    freq: number,
    duration: number,
    wave: OscillatorType,
    gainPeak: number,
    delay = 0,
    freqEnd?: number
  ) => {
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = wave;
    osc.frequency.setValueAtTime(freq, now + delay);
    if (freqEnd) {
      osc.frequency.exponentialRampToValueAtTime(freqEnd, now + delay + duration);
    }
    gain.gain.setValueAtTime(0.0001, now + delay);
    gain.gain.linearRampToValueAtTime(gainPeak, now + delay + 0.012);
    gain.gain.exponentialRampToValueAtTime(0.0001, now + delay + duration);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start(now + delay);
    osc.stop(now + delay + duration + 0.02);
  };

  if (type === 'move') {
    if (soundProfile === 'synth') {
      playTone(460, 0.065, 'triangle', 0.07, 0, 620);
    } else if (soundProfile === 'wood') {
      playTone(290, 0.055, 'sine', 0.09, 0, 220);
    } else if (soundProfile === 'crystal') {
      playTone(880, 0.07, 'sine', 0.05, 0, 1020);
    } else if (soundProfile === 'metallic') {
      playTone(540, 0.06, 'triangle', 0.06, 0, 480);
    } else {
      playTone(360, 0.06, 'sine', 0.08, 0, 460);
    }
  } else if (type === 'lock') {
    const base =
      soundProfile === 'synth' ? 660 : soundProfile === 'crystal' ? 987 : 587;
    playTone(base, 0.1, 'sine', 0.08, 0);
    playTone(base * 1.25, 0.14, 'sine', 0.07, 0.05);
  } else if (type === 'error') {
    playTone(170, 0.09, 'sawtooth', 0.04, 0, 130);
  } else if (type === 'coin') {
    playTone(987, 0.08, 'sine', 0.06, 0);
    playTone(1318, 0.16, 'sine', 0.07, 0.06);
  } else if (type === 'unlock') {
    playTone(523, 0.1, 'triangle', 0.07, 0);
    playTone(659, 0.1, 'triangle', 0.07, 0.07);
    playTone(784, 0.18, 'triangle', 0.08, 0.14);
  } else if (type === 'win') {
    const notes = [523.25, 659.25, 783.99, 1046.5];
    notes.forEach((n, idx) => {
      playTone(
        n,
        0.24,
        soundProfile === 'synth' ? 'triangle' : 'sine',
        0.09,
        idx * 0.085
      );
    });
  }
}
