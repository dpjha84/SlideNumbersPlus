import React, { useState } from 'react';
import { motion } from 'motion/react';
import {
  ArrowRight,
  Award,
  Coins,
  Gem,
  Play,
  RotateCcw,
  Star,
} from 'lucide-react';
import { LevelCompletionResult, ThemeConfig } from '../types/game';

interface VictoryModalProps {
  result: LevelCompletionResult | null;
  theme: ThemeConfig;
  interstitialPending: boolean;
  onNextLevel: () => void;
  onReplay: () => void;
  onWatchRewardedDouble: () => Promise<void>;
}

export const VictoryModal: React.FC<VictoryModalProps> = ({
  result,
  theme,
  interstitialPending,
  onNextLevel,
  onReplay,
  onWatchRewardedDouble,
}) => {
  const [doubledClaimed, setDoubledClaimed] = useState(false);

  if (!result) return null;

  const handleDouble = async () => {
    if (doubledClaimed) return;
    await onWatchRewardedDouble();
    setDoubledClaimed(true);
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label="Level Complete"
      className="fixed inset-0 z-40 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4"
    >
      <motion.div
        initial={{ opacity: 0, scale: 0.9, y: 18 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        transition={{ type: 'spring', stiffness: 360, damping: 24 }}
        className="relative max-w-md w-full rounded-3xl bg-slate-900 border border-slate-700/80 p-6 sm:p-7 shadow-2xl overflow-hidden"
      >
        {/* Subtle top celebratory gradient */}
        <div
          aria-hidden="true"
          className="absolute inset-x-0 top-0 h-28 bg-gradient-to-b from-amber-500/15 to-transparent pointer-events-none"
        />

        <div className="relative z-10 text-center">
          <p className="text-xs font-medium text-slate-400 mb-1">
            {result.isDaily ? 'Daily Challenge Cleared' : `Level ${result.level} Cleared`}
          </p>
          <h2 className="text-2xl sm:text-3xl font-display font-bold text-white mb-5">
            {result.starsEarned === 3
              ? 'Masterful Solve'
              : result.starsEarned === 2
                ? 'Solid Precision'
                : 'Board Cleared'}
          </h2>

          {/* Animated 3-Star Showcase */}
          <div className="flex items-center justify-center gap-3 mb-6">
            {[1, 2, 3].map((starIdx) => {
              const earned = starIdx <= result.starsEarned;
              return (
                <motion.div
                  key={starIdx}
                  initial={{ scale: 0, rotate: -25 }}
                  animate={{ scale: 1, rotate: 0 }}
                  transition={{
                    type: 'spring',
                    stiffness: 420,
                    damping: 16,
                    delay: starIdx * 0.12,
                  }}
                  className={`w-14 h-14 rounded-2xl flex items-center justify-center border ${
                    earned
                      ? 'bg-amber-400/15 border-amber-400/50 text-amber-400 shadow-lg shadow-amber-500/15'
                      : 'bg-slate-800/70 border-slate-700 text-slate-600'
                  } ${starIdx === 2 ? '-translate-y-2' : ''}`}
                >
                  <Star
                    className={`w-7 h-7 ${earned ? 'fill-amber-400 text-amber-400' : ''}`}
                  />
                </motion.div>
              );
            })}
          </div>

          {/* Stats Row */}
          <div className="grid grid-cols-3 gap-2.5 mb-5">
            <div className="rounded-xl bg-slate-950/80 border border-slate-800 py-2.5 px-2">
              <p className="text-[11px] text-slate-400">Moves</p>
              <p className="text-base font-display font-bold text-white tabular-nums">
                {result.moves}
                <span className="text-xs font-normal text-slate-500">
                  /{result.targetMoves}
                </span>
              </p>
            </div>
            <div className="rounded-xl bg-slate-950/80 border border-slate-800 py-2.5 px-2">
              <p className="text-[11px] text-slate-400">Time</p>
              <p className="text-base font-display font-bold text-white tabular-nums">
                {result.timeSec}s
              </p>
            </div>
            <div className="rounded-xl bg-slate-950/80 border border-slate-800 py-2.5 px-2">
              <p className="text-[11px] text-slate-400">Score</p>
              <p className="text-base font-display font-bold text-emerald-400 tabular-nums">
                +{result.scoreEarned}
              </p>
            </div>
          </div>

          {/* Earned Currencies */}
          <div className="rounded-2xl bg-slate-950/70 border border-slate-800 p-3.5 mb-5">
            <p className="text-xs text-slate-400 mb-2.5">Rewards Collected</p>
            <div className="flex items-center justify-center gap-5 tabular-nums">
              <div className="flex items-center gap-1.5 text-sm font-semibold text-amber-400">
                <Star className="w-4 h-4 fill-amber-400" />
                <span>+{result.bonusStarsEarned} Stars</span>
              </div>
              <div className="flex items-center gap-1.5 text-sm font-semibold text-yellow-300">
                <Coins className="w-4 h-4" />
                <span>+{result.coinsEarned}</span>
              </div>
              <div className="flex items-center gap-1.5 text-sm font-semibold text-cyan-300">
                <Gem className="w-4 h-4" />
                <span>
                  +{doubledClaimed ? result.diamondsEarned * 2 : result.diamondsEarned}
                </span>
              </div>
            </div>
          </div>

          {/* Newly Unlocked Play Games Achievements */}
          {result.newlyUnlockedAchievements.length > 0 && (
            <div className="rounded-xl bg-emerald-500/10 border border-emerald-500/30 p-3 mb-5 text-left">
              <div className="flex items-center gap-2 text-xs font-semibold text-emerald-300 mb-1">
                <Award className="w-4 h-4" />
                <span>Play Games Achievement Unlocked</span>
              </div>
              {result.newlyUnlockedAchievements.map((ach) => (
                <p key={ach.id} className="text-xs text-slate-200">
                  {ach.title} · +{ach.xp} XP · +{ach.rewardDiamonds} ◆
                </p>
              ))}
            </div>
          )}

          {/* Optional AdMob Rewarded 2x Diamonds */}
          {!doubledClaimed && (
            <button
              type="button"
              onClick={handleDouble}
              className="w-full mb-3 py-2.5 px-4 rounded-xl bg-slate-800 hover:bg-slate-700 border border-cyan-400/30 text-cyan-300 text-xs font-semibold flex items-center justify-center gap-2 transition-colors cursor-pointer"
            >
              <Play className="w-3.5 h-3.5 fill-current" />
              <span>
                Watch Ad for 2× Diamonds (+{result.diamondsEarned} ◆)
              </span>
            </button>
          )}

          {/* Primary Actions */}
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={() => {
                setDoubledClaimed(false);
                onReplay();
              }}
              className="px-4 py-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-sm font-semibold flex items-center justify-center gap-1.5 transition-colors cursor-pointer"
            >
              <RotateCcw className="w-4 h-4" />
              <span>Replay</span>
            </button>
            <button
              type="button"
              onClick={() => {
                setDoubledClaimed(false);
                onNextLevel();
              }}
              className={`flex-1 py-3 px-4 rounded-xl text-sm font-semibold flex items-center justify-center gap-2 transition-transform active:scale-95 cursor-pointer ${theme.accentBg}`}
            >
              <span>{result.isDaily ? 'Back to Campaign' : 'Next Level'}</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
          {interstitialPending && !result.isDaily && (
            <p className="text-[11px] text-slate-500 mt-2.5">
              Short sponsor break before next level
            </p>
          )}
        </div>
      </motion.div>
    </div>
  );
};
