import {
  AdMob,
  BannerAdPosition,
  BannerAdSize,
} from '@capacitor-community/admob';
import { Capacitor } from '@capacitor/core';

const INTERSTITIAL_COOLDOWN_MS = 120_000; // Strictly 2 minutes
const LAST_INTERSTITIAL_KEY = 'slidepulse_last_interstitial_at';

// Official Google AdMob Android Test Ad Unit IDs (swap with production IDs before Play Store release)
export const ADMOB_ANDROID_IDS = {
  banner: 'ca-app-pub-3940256099942544/6300978111',
  interstitial: 'ca-app-pub-3940256099942544/1033173712',
  rewarded: 'ca-app-pub-3940256099942544/5224354917',
};

let admobInitialized = false;

export interface BannerCreative {
  id: string;
  sponsor: string;
  headline: string;
  cta: string;
  accent: string;
}

export const BANNER_CREATIVES: BannerCreative[] = [
  {
    id: 'ad-metro-architect',
    sponsor: 'Sponsored · Studio Craft',
    headline: 'City Builder Pro — Design transit networks offline',
    cta: 'Install',
    accent: 'bg-amber-500 text-slate-950',
  },
  {
    id: 'ad-focus-audio',
    sponsor: 'Sponsored · SoundLab',
    headline: 'FocusWave — Spatial lo-fi & binaural study beats',
    cta: 'Try Free',
    accent: 'bg-cyan-400 text-slate-950',
  },
  {
    id: 'ad-cloud-vault',
    sponsor: 'Sponsored · VaultSync',
    headline: 'Encrypted photo vault with zero-latency backup',
    cta: 'Learn More',
    accent: 'bg-emerald-400 text-slate-950',
  },
];

export async function initializeNativeAdMob(): Promise<void> {
  if (!Capacitor.isNativePlatform() || admobInitialized) return;
  try {
    await AdMob.initialize({
      initializeForTesting: true,
    });
    admobInitialized = true;
    await AdMob.showBanner({
      adId: ADMOB_ANDROID_IDS.banner,
      adSize: BannerAdSize.ADAPTIVE_BANNER,
      position: BannerAdPosition.BOTTOM_CENTER,
      margin: 0,
      isTesting: true,
    });
  } catch {
    // Graceful fallback to in-app AdMob Banner UI
  }
}

export async function showNativeInterstitialIfReady(): Promise<boolean> {
  if (!Capacitor.isNativePlatform()) return false;
  try {
    await AdMob.prepareInterstitial({
      adId: ADMOB_ANDROID_IDS.interstitial,
      isTesting: true,
    });
    await AdMob.showInterstitial();
    recordInterstitialShown();
    return true;
  } catch {
    return false;
  }
}

export async function showNativeRewardedAd(): Promise<boolean> {
  if (!Capacitor.isNativePlatform()) return false;
  try {
    await AdMob.prepareRewardVideoAd({
      adId: ADMOB_ANDROID_IDS.rewarded,
      isTesting: true,
    });
    await AdMob.showRewardVideoAd();
    return true;
  } catch {
    return false;
  }
}

export function getLastInterstitialTimestamp(): number {
  if (typeof window === 'undefined') return 0;
  const raw = window.localStorage.getItem(LAST_INTERSTITIAL_KEY);
  const parsed = raw ? Number(raw) : 0;
  return Number.isFinite(parsed) ? parsed : 0;
}

export function getRemainingCooldownMs(now = Date.now()): number {
  const last = getLastInterstitialTimestamp();
  if (last === 0) return 0;
  const elapsed = now - last;
  return Math.max(0, INTERSTITIAL_COOLDOWN_MS - elapsed);
}

export function canShowInterstitial(adsRemoved: boolean, now = Date.now()): boolean {
  if (adsRemoved) return false;
  return getRemainingCooldownMs(now) === 0;
}

export function recordInterstitialShown(now = Date.now()): void {
  if (typeof window === 'undefined') return;
  window.localStorage.setItem(LAST_INTERSTITIAL_KEY, String(now));
}

export function formatCooldown(ms: number): string {
  const totalSec = Math.ceil(ms / 1000);
  const mins = Math.floor(totalSec / 60);
  const secs = totalSec % 60;
  return `${mins}:${secs.toString().padStart(2, '0')}`;
}
