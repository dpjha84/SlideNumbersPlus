import React from 'react';
import { Calendar, CheckCircle2, Flame, Gem, Play, Trophy } from 'lucide-react';
import { getDailyChallengeDefinition } from '../data/themes';
import { DailyLeaderboardEntry, PlayerProgress } from '../types/game';

interface DailyChallengeViewProps {
  dateKey: string;
  progress: PlayerProgress;
  dailyEntries: DailyLeaderboardEntry[];
  onStartDaily: () => void;
}

export const DailyChallengeView: React.FC<DailyChallengeViewProps> = ({
  dateKey,
  progress,
  dailyEntries,
  onStartDaily,
}) => {
  const dailyDef = getDailyChallengeDefinition(dateKey);
  const completedToday = progress.completedDailyDates.includes(dateKey);

  return (
    <section className="space-y-6">
      <div className="rounded-3xl bg-gradient-to-br from-slate-900 via-slate-900 to-cyan-950/60 border border-cyan-500/30 p-6 sm:p-8 flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div className="space-y-3 max-w-xl">
          <div className="flex items-center gap-2 text-xs text-cyan-300 font-medium">
            <Calendar className="w-4 h-4" />
            <span>Global Seed · {dateKey}</span>
            <span aria-hidden="true">·</span>
            <span>{dailyDef.tierLabel}</span>
          </div>

          <h2 className="text-2xl sm:text-3xl font-display font-bold text-white text-balance">
            Today’s Competitive Daily Grid
          </h2>
          <p className="text-sm text-slate-300 leading-relaxed">
            Every player worldwide faces the exact same scramble. Finish at or under{' '}
            <strong className="text-white tabular-nums">{dailyDef.targetMoves} moves</strong>{' '}
            to claim a 3-Star clear, earn bonus Diamonds, and extend your daily streak.
          </p>

          <div className="flex flex-wrap items-center gap-5 pt-1 text-xs text-slate-300 tabular-nums">
            <div className="flex items-center gap-1.5">
              <Flame className="w-4 h-4 text-orange-400" />
              <span>Streak: {progress.dailyStreak} days</span>
            </div>
            <div className="flex items-center gap-1.5">
              <Gem className="w-4 h-4 text-cyan-300" />
              <span>Reward: +{dailyDef.rewardDiamonds} ◆ & +{dailyDef.rewardCoins} Coins</span>
            </div>
          </div>
        </div>

        <div className="flex flex-col items-stretch sm:items-end gap-2.5 shrink-0">
          {completedToday && (
            <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-emerald-300">
              <CheckCircle2 className="w-4 h-4" />
              <span>Completed today · Replay to beat your moves</span>
            </div>
          )}
          <button
            type="button"
            onClick={onStartDaily}
            className="px-6 py-3.5 rounded-2xl bg-cyan-400 hover:bg-cyan-300 text-slate-950 font-display font-bold text-sm flex items-center justify-center gap-2 shadow-lg shadow-cyan-500/20 transition-transform active:scale-95 cursor-pointer"
          >
            <Play className="w-4 h-4 fill-current" />
            <span>{completedToday ? 'Replay Daily Grid' : 'Play Daily Challenge'}</span>
          </button>
        </div>
      </div>

      {/* Today's Top Finishers */}
      <div className="rounded-2xl bg-slate-900/80 border border-slate-800 p-5">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-base font-display font-bold text-white flex items-center gap-2">
            <Trophy className="w-4 h-4 text-amber-400" />
            <span>Today’s Fastest Solvers</span>
          </h3>
          <span className="text-xs text-slate-400 tabular-nums">
            {dailyEntries.length} ranked entries
          </span>
        </div>

        <div className="divide-y divide-slate-800/70">
          {dailyEntries.slice(0, 6).map((entry, idx) => (
            <div
              key={entry.id}
              className="py-2.5 flex items-center justify-between text-sm"
            >
              <div className="flex items-center gap-3">
                <span className="w-6 text-xs font-mono font-bold text-slate-400 tabular-nums">
                  #{idx + 1}
                </span>
                <span className="font-medium text-white">{entry.gamerTag}</span>
                <span className="text-xs text-slate-500">{entry.country}</span>
              </div>
              <div className="flex items-center gap-4 text-xs font-mono tabular-nums">
                <span className="text-cyan-300 font-semibold">{entry.moves} moves</span>
                <span className="text-slate-400">{entry.timeSec}s</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};
