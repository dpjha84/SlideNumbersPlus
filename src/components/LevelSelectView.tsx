import React from 'react';
import { Lock, Play, Star } from 'lucide-react';
import { getLevelDefinition } from '../data/themes';
import { PlayerProgress } from '../types/game';

interface LevelSelectViewProps {
  progress: PlayerProgress;
  activeLevel: number;
  onSelectLevel: (level: number) => void;
}

export const LevelSelectView: React.FC<LevelSelectViewProps> = ({
  progress,
  activeLevel,
  onSelectLevel,
}) => {
  const maxShown = Math.max(20, progress.highestLevelUnlocked + 2);
  const levels = Array.from({ length: maxShown }, (_, i) =>
    getLevelDefinition(i + 1)
  );

  const tiers = [
    { label: '3×3 Warm-Up', filter: (s: number) => s === 3 },
    { label: '4×4 Tactical', filter: (s: number) => s === 4 },
    { label: '5×5 Master', filter: (s: number) => s === 5 },
    { label: '6×6 Grandmaster', filter: (s: number) => s >= 6 },
  ];

  return (
    <section className="space-y-7">
      <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-3">
        <div>
          <h2 className="text-2xl font-display font-bold text-white">
            Campaign Stages
          </h2>
          <p className="text-sm text-slate-400">
            Grid dimensions and scramble depths scale as you advance. Beat Target Moves for 3 Stars.
          </p>
        </div>
        <div className="text-xs text-slate-400 tabular-nums">
          Total Earned: <span className="text-amber-400 font-semibold">{progress.currencies.stars} ★</span>
        </div>
      </div>

      {tiers.map((tier) => {
        const tierLevels = levels.filter((l) => tier.filter(l.size));
        if (tierLevels.length === 0) return null;

        return (
          <div key={tier.label} className="space-y-3">
            <h3 className="text-sm font-display font-semibold text-slate-300 border-b border-slate-800 pb-2">
              {tier.label}
            </h3>
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3">
              {tierLevels.map((lvl) => {
                const isUnlocked = lvl.level <= progress.highestLevelUnlocked;
                const stars = progress.levelStars[lvl.level] || 0;
                const bestMoves = progress.levelBestMoves[lvl.level];
                const isCurrent = lvl.level === activeLevel;

                return (
                  <button
                    key={lvl.level}
                    type="button"
                    disabled={!isUnlocked}
                    onClick={() => onSelectLevel(lvl.level)}
                    className={`rounded-2xl p-3.5 border text-left transition-all flex flex-col justify-between gap-2.5 ${
                      !isUnlocked
                        ? 'bg-slate-900/40 border-slate-800/70 text-slate-500 cursor-not-allowed'
                        : isCurrent
                          ? 'bg-slate-800 border-amber-400/60 shadow-lg shadow-amber-500/10 cursor-pointer'
                          : 'bg-slate-900/80 border-slate-800 hover:border-slate-600 cursor-pointer'
                    }`}
                  >
                    <div className="flex items-center justify-between gap-2">
                      <span className="text-lg font-display font-bold text-white tabular-nums">
                        {lvl.level}
                      </span>
                      {!isUnlocked ? (
                        <Lock className="w-4 h-4 text-slate-600" />
                      ) : isCurrent ? (
                        <Play className="w-3.5 h-3.5 text-amber-400 fill-amber-400" />
                      ) : null}
                    </div>

                    <div>
                      <p className="text-xs font-medium text-slate-300 truncate">
                        {lvl.title}
                      </p>
                      <p className="text-[11px] text-slate-500 tabular-nums mt-0.5">
                        Par {lvl.targetMoves} moves
                        {bestMoves ? ` · Best ${bestMoves}` : ''}
                      </p>
                    </div>

                    <div className="flex items-center gap-1 pt-1">
                      {[1, 2, 3].map((s) => (
                        <Star
                          key={s}
                          className={`w-3.5 h-3.5 ${
                            s <= stars
                              ? 'fill-amber-400 text-amber-400'
                              : 'text-slate-700'
                          }`}
                        />
                      ))}
                    </div>
                  </button>
                );
              })}
            </div>
          </div>
        );
      })}
    </section>
  );
};
