import React, { useEffect, useState } from 'react';
import { ExternalLink, ShieldCheck, Sparkles, X } from 'lucide-react';
import { BANNER_CREATIVES } from '../services/admob';

interface AdMobBannerProps {
  levelContext: number;
  onOpenStoreRemoveAds?: () => void;
}

export const AdMobBanner: React.FC<AdMobBannerProps> = ({
  levelContext,
  onOpenStoreRemoveAds,
}) => {
  const [creativeIdx, setCreativeIdx] = useState(0);
  const [clickedNotice, setClickedNotice] = useState(false);

  useEffect(() => {
    setCreativeIdx(levelContext % BANNER_CREATIVES.length);
  }, [levelContext]);

  useEffect(() => {
    const timer = window.setInterval(() => {
      setCreativeIdx((prev) => (prev + 1) % BANNER_CREATIVES.length);
    }, 28000);
    return () => window.clearInterval(timer);
  }, []);

  const creative = BANNER_CREATIVES[creativeIdx];

  const handleBannerAction = () => {
    setClickedNotice(true);
    window.setTimeout(() => setClickedNotice(false), 2200);
  };

  return (
    <aside
      aria-label="Advertisement banner"
      className="w-full bg-slate-900/95 border-t border-slate-800 backdrop-blur-md"
    >
      <div className="max-w-6xl mx-auto px-3 sm:px-4 h-[52px] flex items-center justify-between gap-2.5">
        <div className="flex items-center gap-2.5 min-w-0">
          <span className="shrink-0 text-[10px] font-mono font-semibold uppercase tracking-wider px-1.5 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700">
            Ad
          </span>
          <div className="min-w-0">
            <p className="text-xs font-medium text-slate-100 truncate">
              {clickedNotice
                ? 'AdMob test click recorded'
                : creative.headline}
            </p>
            <p className="text-[11px] text-slate-400 truncate">
              {creative.sponsor} · Google AdMob
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2 shrink-0">
          {onOpenStoreRemoveAds && (
            <button
              type="button"
              onClick={onOpenStoreRemoveAds}
              className="hidden sm:inline-flex items-center text-[11px] font-medium text-slate-400 hover:text-slate-200 transition-colors whitespace-nowrap px-2 py-1"
            >
              Ad Preferences
            </button>
          )}
          <button
            type="button"
            onClick={handleBannerAction}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap shrink-0 flex items-center gap-1 transition-transform active:scale-95 ${creative.accent}`}
          >
            <span>{creative.cta}</span>
            <ExternalLink className="w-3 h-3" />
          </button>
        </div>
      </div>
    </aside>
  );
};

interface AdMobInterstitialModalProps {
  open: boolean;
  nextLevelLabel: string;
  onComplete: () => void;
}

export const AdMobInterstitialModal: React.FC<AdMobInterstitialModalProps> = ({
  open,
  nextLevelLabel,
  onComplete,
}) => {
  const [secondsLeft, setSecondsLeft] = useState(5);

  useEffect(() => {
    if (!open) return;
    setSecondsLeft(5);
    const id = window.setInterval(() => {
      setSecondsLeft((prev) => (prev <= 1 ? 0 : prev - 1));
    }, 1000);
    return () => window.clearInterval(id);
  }, [open]);

  if (!open) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label="AdMob Interstitial Advertisement"
      className="fixed inset-0 z-50 bg-slate-950/95 backdrop-blur-md flex flex-col justify-between p-4 sm:p-6"
    >
      {/* Top AdMob Bar */}
      <div className="max-w-lg w-full mx-auto flex items-center justify-between">
        <div className="flex items-center gap-2 text-xs text-slate-400">
          <span className="px-1.5 py-0.5 rounded bg-slate-800 border border-slate-700 text-slate-200 font-mono text-[10px]">
            AdMob Interstitial
          </span>
          <span>2-min cooldown active</span>
        </div>

        {secondsLeft > 0 ? (
          <span className="text-xs font-mono tabular-nums text-slate-300 px-3 py-1.5 rounded-lg bg-slate-900 border border-slate-800">
            Continue in {secondsLeft}s
          </span>
        ) : (
          <button
            type="button"
            onClick={onComplete}
            className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-lg bg-white text-slate-950 text-xs font-semibold hover:bg-slate-200 transition-colors cursor-pointer"
          >
            <span>Continue</span>
            <X className="w-3.5 h-3.5" />
          </button>
        )}
      </div>

      {/* Sponsored Card */}
      <div className="max-w-md w-full mx-auto rounded-3xl bg-gradient-to-br from-slate-900 via-slate-900 to-indigo-950 border border-slate-700/80 p-6 sm:p-8 text-center shadow-2xl">
        <div className="w-14 h-14 rounded-2xl bg-amber-500/15 border border-amber-400/30 flex items-center justify-center mx-auto mb-5">
          <Sparkles className="w-7 h-7 text-amber-400" />
        </div>
        <p className="text-xs text-slate-400 mb-2">Google Play Editor’s Pick</p>
        <h3 className="text-2xl font-display font-bold text-white mb-2 text-balance">
          Architect Logic Suites
        </h3>
        <p className="text-sm text-slate-300 leading-relaxed mb-6">
          Challenge spatial memory with 400+ handcrafted minimalist puzzles. Offline play included.
        </p>
        <div className="flex items-center justify-center gap-4 text-xs text-slate-400 mb-6 tabular-nums">
          <span>4.9 ★ Rating</span>
          <span aria-hidden="true">·</span>
          <span>Free Download</span>
          <span aria-hidden="true">·</span>
          <span>Puzzle</span>
        </div>
        <button
          type="button"
          onClick={secondsLeft === 0 ? onComplete : undefined}
          disabled={secondsLeft > 0}
          className={`w-full py-3 px-4 rounded-xl text-sm font-semibold transition-all ${
            secondsLeft === 0
              ? 'bg-amber-400 hover:bg-amber-300 text-slate-950 cursor-pointer'
              : 'bg-slate-800 text-slate-400 cursor-not-allowed'
          }`}
        >
          {secondsLeft > 0
            ? `Loading ${nextLevelLabel} (${secondsLeft}s)`
            : `Proceed to ${nextLevelLabel}`}
        </button>
      </div>

      {/* Footer note */}
      <div className="max-w-lg w-full mx-auto flex items-center justify-center gap-2 text-xs text-slate-500">
        <ShieldCheck className="w-4 h-4 text-slate-400" />
        <span>Next interstitial gated for at least 2 minutes after dismissal</span>
      </div>
    </div>
  );
};

interface AdMobRewardedModalProps {
  open: boolean;
  rewardLabel: string;
  onClaim: () => void;
  onCancel: () => void;
}

export const AdMobRewardedModal: React.FC<AdMobRewardedModalProps> = ({
  open,
  rewardLabel,
  onClaim,
  onCancel,
}) => {
  const [secondsLeft, setSecondsLeft] = useState(3);

  useEffect(() => {
    if (!open) return;
    setSecondsLeft(3);
    const id = window.setInterval(() => {
      setSecondsLeft((prev) => (prev <= 1 ? 0 : prev - 1));
    }, 1000);
    return () => window.clearInterval(id);
  }, [open]);

  if (!open) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label="AdMob Rewarded Video"
      className="fixed inset-0 z-50 bg-slate-950/95 backdrop-blur-md flex items-center justify-center p-4"
    >
      <div className="max-w-md w-full rounded-3xl bg-slate-900 border border-slate-700 p-6 text-center shadow-2xl">
        <div className="flex items-center justify-between mb-4">
          <span className="px-2 py-0.5 rounded bg-slate-800 text-slate-300 font-mono text-[10px]">
            AdMob Rewarded
          </span>
          <button
            type="button"
            onClick={onCancel}
            className="text-xs text-slate-400 hover:text-white px-2 py-1"
          >
            Close
          </button>
        </div>

        <div className="h-36 rounded-2xl bg-gradient-to-br from-cyan-950 via-slate-900 to-amber-950 border border-slate-700/70 flex flex-col items-center justify-center mb-5 px-4">
          <p className="text-xs text-cyan-300 font-medium mb-1">Sponsored Video Preview</p>
          <p className="text-base font-display font-bold text-white">
            Cloud Studio Mobile Suite
          </p>
          <p className="text-xs text-slate-400 mt-1 tabular-nums">
            {secondsLeft > 0
              ? `Reward unlocks in ${secondsLeft}s`
              : 'Reward ready to claim'}
          </p>
        </div>

        <button
          type="button"
          disabled={secondsLeft > 0}
          onClick={onClaim}
          className={`w-full py-3 px-4 rounded-xl text-sm font-semibold transition-all ${
            secondsLeft === 0
              ? 'bg-cyan-400 hover:bg-cyan-300 text-slate-950 cursor-pointer'
              : 'bg-slate-800 text-slate-400 cursor-not-allowed'
          }`}
        >
          {secondsLeft > 0 ? `Watching (${secondsLeft}s)...` : `Claim ${rewardLabel}`}
        </button>
      </div>
    </div>
  );
};
