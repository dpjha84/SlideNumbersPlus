/**
 * Native Google AdMob integration (Capacitor).
 * Runs real AdMob SDK calls inside the Android app; in web preview renders graceful
 * interactive banner/interstitial/rewarded UI fallbacks.
 *
 * Design rules:
 *  - Ads are PRELOADED in the background. Tapping Next Level never blocks on network.
 *  - Persistent bottom Banner Ad is shown across every level.
 *  - Interstitials on Next Level are strictly gated by a 2-minute (120,000ms) cooldown.
 *  - Ads default to Google's official TEST units unless production VITE_ADMOB_*_UNIT_ID
 *    values are provided at build time.
 */
import { Capacitor } from '@capacitor/core';
import {
  AdMob,
  AdmobConsentStatus,
  BannerAdPluginEvents,
  BannerAdPosition,
  BannerAdSize,
  InterstitialAdPluginEvents,
  RewardAdPluginEvents,
} from '@capacitor-community/admob';

// Google's official sample test ad units (always safe to click).
const TEST = {
  banner: 'ca-app-pub-3940256099942544/6300978111',
  interstitial: 'ca-app-pub-3940256099942544/1033173712',
  rewarded: 'ca-app-pub-3940256099942544/5224354917',
};

const IDS = {
  banner: import.meta.env.VITE_ADMOB_BANNER_UNIT_ID || TEST.banner,
  interstitial:
    import.meta.env.VITE_ADMOB_INTERSTITIAL_UNIT_ID || TEST.interstitial,
  rewarded: import.meta.env.VITE_ADMOB_REWARDED_UNIT_ID || TEST.rewarded,
};

const isTesting =
  IDS.banner === TEST.banner &&
  IDS.interstitial === TEST.interstitial &&
  IDS.rewarded === TEST.rewarded;

export const BANNER_HEIGHT_PX = 52;
export const INTERSTITIAL_COOLDOWN_MS = 2 * 60 * 1000; // Strictly 2 minutes
const LAST_INTERSTITIAL_KEY = 'slidepulse_last_interstitial_at';
const RETRY_MS = 45_000;
const LOG_MAX = 80;

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

export function getLastInterstitialTimestamp(): number {
  if (typeof window === 'undefined') return 0;
  const raw = window.localStorage.getItem(LAST_INTERSTITIAL_KEY);
  const parsed = raw ? Number(raw) : 0;
  return Number.isFinite(parsed) ? parsed : 0;
}

export function getRemainingCooldownMs(now = Date.now()): number {
  const last = getLastInterstitialTimestamp();
  if (last === 0) return 0;
  return Math.max(0, INTERSTITIAL_COOLDOWN_MS - (now - last));
}

export function canShowInterstitial(
  adsRemoved: boolean,
  now = Date.now()
): boolean {
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

type Listener = () => void;

class AdMobManager {
  private initPromise: Promise<void> | null = null;
  private ready = false;
  private bannerShown = false;
  private bannerRetry: ReturnType<typeof setTimeout> | null = null;

  private interstitialLoaded = false;
  private interstitialLoading = false;
  private interstitialBusy = false;
  private interstitialRetry: ReturnType<typeof setTimeout> | null = null;

  private rewardedLoaded = false;
  private rewardedLoading = false;
  private rewardedBusy = false;
  private rewardedRetry: ReturnType<typeof setTimeout> | null = null;

  private logLines: string[] = [];
  private logListeners = new Set<Listener>();

  public isNative(): boolean {
    return Capacitor.isNativePlatform();
  }

  private log(msg: string) {
    const t = new Date();
    const stamp = `${t.getMinutes().toString().padStart(2, '0')}:${t
      .getSeconds()
      .toString()
      .padStart(2, '0')}.${t.getMilliseconds().toString().padStart(3, '0')}`;
    this.logLines.push(`${stamp} ${msg}`);
    if (this.logLines.length > LOG_MAX) this.logLines.shift();
    this.logListeners.forEach((l) => l());
  }

  public getLog(): string[] {
    return [...this.logLines];
  }

  public subscribeLog(l: Listener): () => void {
    this.logListeners.add(l);
    return () => this.logListeners.delete(l);
  }

  private online(): boolean {
    return typeof navigator === 'undefined' || navigator.onLine !== false;
  }

  public init(): Promise<void> {
    if (!this.isNative()) return Promise.resolve();
    if (this.initPromise) return this.initPromise;

    this.initPromise = (async () => {
      try {
        const evs: string[] = [
          ...Object.values(InterstitialAdPluginEvents),
          ...Object.values(RewardAdPluginEvents),
          BannerAdPluginEvents.Loaded,
          BannerAdPluginEvents.FailedToLoad,
          BannerAdPluginEvents.Opened,
          BannerAdPluginEvents.Closed,
        ];
        for (const ev of evs) {
          try {
            await AdMob.addListener(ev as never, () => this.log(`event ${ev}`));
          } catch {
            // ignore unsupported event registration
          }
        }

        await AdMob.addListener(BannerAdPluginEvents.FailedToLoad, () => {
          this.bannerShown = false;
          if (this.bannerRetry) clearTimeout(this.bannerRetry);
          this.bannerRetry = setTimeout(() => void this.showBanner(), RETRY_MS);
        });

        try {
          const info = await AdMob.requestConsentInfo();
          if (
            info.isConsentFormAvailable &&
            info.status === AdmobConsentStatus.REQUIRED
          ) {
            await AdMob.showConsentForm();
          }
        } catch (e) {
          this.log(`consent skipped: ${String(e).slice(0, 80)}`);
        }

        await AdMob.initialize({ initializeForTesting: isTesting });
        this.ready = true;
        this.log(`init ok (testing=${isTesting})`);
        void this.showBanner();
        this.preloadInterstitial();
        this.preloadRewarded();
      } catch (e) {
        this.log(`init failed: ${String(e).slice(0, 80)}`);
      }
    })();

    return this.initPromise;
  }

  public async showBanner(): Promise<void> {
    if (!this.isNative() || this.bannerShown) return;
    await this.init();
    if (!this.ready || !this.online()) return;
    try {
      this.bannerShown = true;
      await AdMob.showBanner({
        adId: IDS.banner,
        adSize: BannerAdSize.ADAPTIVE_BANNER,
        position: BannerAdPosition.BOTTOM_CENTER,
        margin: 0,
        isTesting,
      });
      this.log('banner requested');
    } catch (e) {
      this.bannerShown = false;
      this.log(`banner failed: ${String(e).slice(0, 80)}`);
    }
  }

  public preloadInterstitial(): void {
    if (
      !this.isNative() ||
      !this.ready ||
      this.interstitialLoaded ||
      this.interstitialLoading
    ) {
      return;
    }
    if (!this.online()) return;
    this.interstitialLoading = true;
    AdMob.prepareInterstitial({ adId: IDS.interstitial, isTesting })
      .then(() => {
        this.interstitialLoaded = true;
        this.log('interstitial preloaded');
      })
      .catch((e) => {
        this.log(`interstitial preload failed: ${String(e).slice(0, 60)}`);
        if (this.interstitialRetry) clearTimeout(this.interstitialRetry);
        this.interstitialRetry = setTimeout(
          () => this.preloadInterstitial(),
          RETRY_MS
        );
      })
      .finally(() => {
        this.interstitialLoading = false;
      });
  }

  public async showInterstitial(
    adsRemoved: boolean,
    onDone: () => void
  ): Promise<boolean> {
    if (!this.isNative()) return false;
    if (this.interstitialBusy) {
      onDone();
      return true;
    }
    if (!canShowInterstitial(adsRemoved)) {
      onDone();
      return true;
    }
    if (!this.interstitialLoaded) {
      this.log('interstitial not ready -> skip');
      this.preloadInterstitial();
      onDone();
      return true;
    }

    this.interstitialBusy = true;
    this.interstitialLoaded = false;
    recordInterstitialShown();

    let finished = false;
    const handles: { remove: () => void }[] = [];
    const watchdog = setTimeout(() => done(), 60_000);
    const done = () => {
      if (finished) return;
      finished = true;
      clearTimeout(watchdog);
      handles.forEach((h) => h.remove());
      this.interstitialBusy = false;
      this.preloadInterstitial();
      onDone();
    };

    try {
      handles.push(
        await AdMob.addListener(InterstitialAdPluginEvents.Dismissed, done),
        await AdMob.addListener(InterstitialAdPluginEvents.FailedToShow, done)
      );
      await AdMob.showInterstitial();
    } catch (e) {
      this.log(`interstitial show failed: ${String(e).slice(0, 60)}`);
      done();
    }
    return true;
  }

  public preloadRewarded(): void {
    if (
      !this.isNative() ||
      !this.ready ||
      this.rewardedLoaded ||
      this.rewardedLoading
    ) {
      return;
    }
    if (!this.online()) return;
    this.rewardedLoading = true;
    AdMob.prepareRewardVideoAd({ adId: IDS.rewarded, isTesting })
      .then(() => {
        this.rewardedLoaded = true;
        this.log('rewarded preloaded');
      })
      .catch((e) => {
        this.log(`rewarded preload failed: ${String(e).slice(0, 60)}`);
        if (this.rewardedRetry) clearTimeout(this.rewardedRetry);
        this.rewardedRetry = setTimeout(() => this.preloadRewarded(), RETRY_MS);
      })
      .finally(() => {
        this.rewardedLoading = false;
      });
  }

  public isRewardedReady(): boolean {
    return !this.isNative() || this.rewardedLoaded;
  }

  public async showRewarded(
    onReward: () => void,
    onUnavailable: () => void
  ): Promise<void> {
    if (!this.isNative()) return onReward();
    if (this.rewardedBusy) return;
    if (!this.rewardedLoaded) {
      this.log('rewarded not ready');
      this.preloadRewarded();
      return onUnavailable();
    }

    this.rewardedBusy = true;
    this.rewardedLoaded = false;
    recordInterstitialShown();

    let rewarded = false;
    let settled = false;
    const handles: { remove: () => void }[] = [];
    const watchdog = setTimeout(() => finish(false), 120_000);
    const finish = (ok: boolean) => {
      if (settled) return;
      settled = true;
      clearTimeout(watchdog);
      handles.forEach((h) => h.remove());
      this.rewardedBusy = false;
      this.preloadRewarded();
      if (ok) onReward();
      else onUnavailable();
    };

    try {
      handles.push(
        await AdMob.addListener(RewardAdPluginEvents.Rewarded, () => {
          rewarded = true;
        }),
        await AdMob.addListener(RewardAdPluginEvents.Dismissed, () =>
          finish(rewarded)
        ),
        await AdMob.addListener(RewardAdPluginEvents.FailedToShow, () =>
          finish(false)
        )
      );
      await AdMob.showRewardVideoAd();
    } catch (e) {
      this.log(`rewarded show failed: ${String(e).slice(0, 60)}`);
      finish(false);
    }
  }
}

export const adMobManager = new AdMobManager();
