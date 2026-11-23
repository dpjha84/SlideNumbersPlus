import React, { useEffect, useState } from 'react';
import {
  Award,
  Check,
  Cloud,
  Gamepad2,
  RefreshCw,
  ShieldCheck,
  Smartphone,
  Volume2,
  VolumeX,
  X,
} from 'lucide-react';
import { ACHIEVEMENTS } from '../data/themes';
import { formatCooldown, getRemainingCooldownMs } from '../services/admob';
import { getPlayerLevelFromXp } from '../services/playGames';
import { PlayerProgress } from '../types/game';

interface PlayGamesModalProps {
  open: boolean;
  progress: PlayerProgress;
  syncing: boolean;
  onClose: () => void;
  onUpdateProfile: (patch: Partial<PlayerProgress>) => void;
  onManualSync: () => void;
}

const AVATAR_SWATCHES = [
  '#f59e0b',
  '#06b6d4',
  '#10b981',
  '#ec4899',
  '#6366f1',
  '#f97316',
];

export const PlayGamesModal: React.FC<PlayGamesModalProps> = ({
  open,
  progress,
  syncing,
  onClose,
  onUpdateProfile,
  onManualSync,
}) => {
  const [tagDraft, setTagDraft] = useState(progress.gamerTag);
  const [countryDraft, setCountryDraft] = useState(progress.country);
  const [cooldownMs, setCooldownMs] = useState(getRemainingCooldownMs());

  useEffect(() => {
    setTagDraft(progress.gamerTag);
    setCountryDraft(progress.country);
  }, [progress.gamerTag, progress.country, open]);

  useEffect(() => {
    if (!open) return;
    setCooldownMs(getRemainingCooldownMs());
    const id = window.setInterval(() => {
      setCooldownMs(getRemainingCooldownMs());
    }, 1000);
    return () => window.clearInterval(id);
  }, [open]);

  if (!open) return null;

  const xpStats = getPlayerLevelFromXp(progress.xp);
  const xpPercent = Math.round(
    (xpStats.currentXpInLevel / xpStats.xpForNextLevel) * 100
  );

  const handleSaveIdentity = (e: React.FormEvent) => {
    e.preventDefault();
    const cleanedTag = tagDraft.trim().slice(0, 20) || progress.gamerTag;
    const cleanedCountry =
      countryDraft.trim().toUpperCase().slice(0, 2) || 'US';
    onUpdateProfile({
      gamerTag: cleanedTag,
      country: cleanedCountry,
    });
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label="Google Play Games Profile and Settings"
      className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4"
    >
      <div className="max-w-lg w-full max-h-[88vh] overflow-y-auto rounded-3xl bg-slate-900 border border-slate-700/80 p-5 sm:p-6 shadow-2xl space-y-5">
        {/* Header */}
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-emerald-500/15 border border-emerald-400/30 flex items-center justify-center">
              <Gamepad2 className="w-5 h-5 text-emerald-400" />
            </div>
            <div>
              <h2 className="text-lg font-display font-bold text-white">
                Play Games & Settings
              </h2>
              <p className="text-xs text-slate-400">
                {progress.playGamesSignedIn
                  ? 'Connected · Cloud Save & Leaderboards active'
                  : 'Signed out · Local progress only'}
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={onClose}
            aria-label="Close Play Games modal"
            className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 cursor-pointer"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Gamer Card & XP */}
        <div className="rounded-2xl bg-slate-950/80 border border-slate-800 p-4 space-y-4">
          <div className="flex items-center justify-between gap-3">
            <div className="flex items-center gap-3">
              <div
                className="w-12 h-12 rounded-2xl flex items-center justify-center text-base font-display font-bold text-slate-950 shrink-0"
                style={{ backgroundColor: progress.avatarColor }}
              >
                {progress.gamerTag.slice(0, 2).toUpperCase()}
              </div>
              <div>
                <div className="flex items-center gap-1.5">
                  <p className="text-base font-display font-bold text-white">
                    {progress.gamerTag}
                  </p>
                  {progress.playGamesSignedIn && (
                    <ShieldCheck className="w-4 h-4 text-emerald-400" />
                  )}
                </div>
                <p className="text-xs text-slate-400 tabular-nums">
                  Play Level {xpStats.level} · {progress.xp} XP
                </p>
              </div>
            </div>

            <button
              type="button"
              onClick={() =>
                onUpdateProfile({
                  playGamesSignedIn: !progress.playGamesSignedIn,
                })
              }
              className={`px-3 py-1.5 rounded-xl text-xs font-semibold cursor-pointer transition-colors ${
                progress.playGamesSignedIn
                  ? 'bg-emerald-500/15 text-emerald-300 border border-emerald-400/35 hover:bg-emerald-500/25'
                  : 'bg-slate-800 text-slate-200 hover:bg-slate-700'
              }`}
            >
              {progress.playGamesSignedIn ? 'Signed In' : 'Sign In'}
            </button>
          </div>

          {/* XP Progress Bar */}
          <div>
            <div className="flex justify-between text-[11px] text-slate-400 mb-1 tabular-nums">
              <span>Level {xpStats.level} Progress</span>
              <span>
                {xpStats.currentXpInLevel} / {xpStats.xpForNextLevel} XP
              </span>
            </div>
            <div className="h-2 rounded-full bg-slate-800 overflow-hidden">
              <div
                className="h-full bg-emerald-400 rounded-full transition-all"
                style={{ width: `${xpPercent}%` }}
              />
            </div>
          </div>

          {/* Gamer Tag Form */}
          <form onSubmit={handleSaveIdentity} className="grid grid-cols-12 gap-2 pt-1">
            <input
              type="text"
              value={tagDraft}
              onChange={(e) => setTagDraft(e.target.value)}
              maxLength={20}
              placeholder="Gamer Tag"
              aria-label="Gamer Tag"
              className="col-span-7 px-3 py-2 rounded-xl bg-slate-900 border border-slate-700 text-xs text-white focus:outline-none focus:border-emerald-400"
            />
            <input
              type="text"
              value={countryDraft}
              onChange={(e) => setCountryDraft(e.target.value.toUpperCase())}
              maxLength={2}
              placeholder="US"
              aria-label="Country code"
              className="col-span-2 px-2 py-2 rounded-xl bg-slate-900 border border-slate-700 text-xs text-center font-mono text-white focus:outline-none focus:border-emerald-400"
            />
            <button
              type="submit"
              className="col-span-3 py-2 rounded-xl bg-emerald-400 hover:bg-emerald-300 text-slate-950 text-xs font-bold cursor-pointer"
            >
              Save
            </button>
          </form>

          {/* Avatar Swatches & Cloud Sync */}
          <div className="flex items-center justify-between gap-2 pt-1">
            <div className="flex items-center gap-1.5">
              {AVATAR_SWATCHES.map((color) => (
                <button
                  key={color}
                  type="button"
                  aria-label={`Select avatar color ${color}`}
                  onClick={() => onUpdateProfile({ avatarColor: color })}
                  className={`w-6 h-6 rounded-full flex items-center justify-center cursor-pointer transition-transform ${
                    progress.avatarColor === color ? 'scale-110 ring-2 ring-white' : ''
                  }`}
                  style={{ backgroundColor: color }}
                >
                  {progress.avatarColor === color && (
                    <Check className="w-3 h-3 text-slate-950" />
                  )}
                </button>
              ))}
            </div>

            <button
              type="button"
              disabled={!progress.playGamesSignedIn || syncing}
              onClick={onManualSync}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 disabled:opacity-50 text-xs font-medium text-slate-200 cursor-pointer"
            >
              {syncing ? (
                <RefreshCw className="w-3.5 h-3.5 animate-spin" />
              ) : (
                <Cloud className="w-3.5 h-3.5 text-emerald-400" />
              )}
              <span>Cloud Sync</span>
            </button>
          </div>
        </div>

        {/* Device Feedback & AdMob Cadence */}
        <div className="grid grid-cols-2 gap-3">
          <button
            type="button"
            onClick={() =>
              onUpdateProfile({ soundEnabled: !progress.soundEnabled })
            }
            className="rounded-2xl bg-slate-950/75 border border-slate-800 p-3 flex items-center justify-between text-xs font-medium text-slate-200 cursor-pointer hover:border-slate-700"
          >
            <span className="flex items-center gap-2">
              {progress.soundEnabled ? (
                <Volume2 className="w-4 h-4 text-amber-400" />
              ) : (
                <VolumeX className="w-4 h-4 text-slate-500" />
              )}
              <span>Audio FX</span>
            </span>
            <span className="text-slate-400">
              {progress.soundEnabled ? 'On' : 'Muted'}
            </span>
          </button>

          <button
            type="button"
            onClick={() =>
              onUpdateProfile({ hapticsEnabled: !progress.hapticsEnabled })
            }
            className="rounded-2xl bg-slate-950/75 border border-slate-800 p-3 flex items-center justify-between text-xs font-medium text-slate-200 cursor-pointer hover:border-slate-700"
          >
            <span className="flex items-center gap-2">
              <Smartphone
                className={`w-4 h-4 ${
                  progress.hapticsEnabled ? 'text-cyan-400' : 'text-slate-500'
                }`}
              />
              <span>Haptics</span>
            </span>
            <span className="text-slate-400">
              {progress.hapticsEnabled ? 'On' : 'Off'}
            </span>
          </button>
        </div>

        <div className="rounded-xl bg-slate-950/60 border border-slate-800 px-3.5 py-2.5 flex items-center justify-between text-xs text-slate-400 tabular-nums">
          <span>AdMob Interstitial 2-Min Gate</span>
          <span className="font-mono text-slate-200">
            {cooldownMs === 0 ? 'Eligible Now' : `Cooldown ${formatCooldown(cooldownMs)}`}
          </span>
        </div>

        {/* Achievements List */}
        <div className="space-y-2.5">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-display font-bold text-white flex items-center gap-1.5">
              <Award className="w-4 h-4 text-amber-400" />
              <span>Play Games Achievements</span>
            </h3>
            <span className="text-xs text-slate-400 tabular-nums">
              {progress.unlockedAchievements.length} / {ACHIEVEMENTS.length}
            </span>
          </div>

          <div className="space-y-2">
            {ACHIEVEMENTS.map((ach) => {
              const unlocked = progress.unlockedAchievements.includes(ach.id);
              return (
                <div
                  key={ach.id}
                  className={`rounded-xl p-3 border flex items-center justify-between gap-3 ${
                    unlocked
                      ? 'bg-emerald-500/10 border-emerald-500/30'
                      : 'bg-slate-950/60 border-slate-800/80 opacity-75'
                  }`}
                >
                  <div>
                    <p className="text-xs font-semibold text-white">
                      {ach.title}
                    </p>
                    <p className="text-[11px] text-slate-400 mt-0.5">
                      {ach.description}
                    </p>
                  </div>
                  <div className="text-right shrink-0 tabular-nums">
                    <p className="text-xs font-semibold text-emerald-300">
                      +{ach.xp} XP
                    </p>
                    <p className="text-[11px] text-cyan-300">
                      +{ach.rewardDiamonds} ◆
                    </p>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};
