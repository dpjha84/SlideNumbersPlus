import React, { useState } from 'react';
import { Globe, RefreshCw, ShieldCheck, Star, Trophy } from 'lucide-react';
import { DailyLeaderboardEntry, LeaderboardEntry } from '../types/game';

interface LeaderboardViewProps {
  globalEntries: LeaderboardEntry[];
  dailyDateKey: string;
  dailyEntries: DailyLeaderboardEntry[];
  currentGamerId: string;
  loading: boolean;
  onRefresh: () => void;
}

export const LeaderboardView: React.FC<LeaderboardViewProps> = ({
  globalEntries,
  dailyDateKey,
  dailyEntries,
  currentGamerId,
  loading,
  onRefresh,
}) => {
  const [boardTab, setBoardTab] = useState<'global' | 'daily'>('global');

  const playerGlobalIdx = globalEntries.findIndex((e) => e.id === currentGamerId);

  return (
    <section className="space-y-5">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-display font-bold text-white flex items-center gap-2">
            <Globe className="w-6 h-6 text-amber-400" />
            <span>Global Leaderboards</span>
          </h2>
          <p className="text-sm text-slate-400">
            Play Games verified standings across worldwide players.
          </p>
        </div>

        <div className="flex items-center gap-2 self-start">
          <div className="flex items-center gap-1 p-1 rounded-xl bg-slate-900 border border-slate-800">
            <button
              type="button"
              onClick={() => setBoardTab('global')}
              className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-colors whitespace-nowrap cursor-pointer ${
                boardTab === 'global'
                  ? 'bg-slate-800 text-white'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              Global Top Scores
            </button>
            <button
              type="button"
              onClick={() => setBoardTab('daily')}
              className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-colors whitespace-nowrap cursor-pointer ${
                boardTab === 'daily'
                  ? 'bg-slate-800 text-white'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              Daily Grid
            </button>
          </div>

          <button
            type="button"
            onClick={onRefresh}
            aria-label="Refresh leaderboards"
            className="p-2.5 rounded-xl bg-slate-900 hover:bg-slate-800 border border-slate-800 text-slate-300 transition-colors cursor-pointer"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      {/* Player Standing Summary */}
      {boardTab === 'global' && playerGlobalIdx >= 0 && (
        <div className="rounded-2xl bg-amber-500/10 border border-amber-400/30 p-4 flex items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <span className="w-9 h-9 rounded-xl bg-amber-400 text-slate-950 font-display font-bold text-sm flex items-center justify-center tabular-nums">
              #{playerGlobalIdx + 1}
            </span>
            <div>
              <p className="text-sm font-semibold text-white">
                Your Global Rank
              </p>
              <p className="text-xs text-amber-200/80 tabular-nums">
                {globalEntries[playerGlobalIdx].bestScore.toLocaleString()} pts ·{' '}
                {globalEntries[playerGlobalIdx].totalStars} ★ · Level{' '}
                {globalEntries[playerGlobalIdx].highestLevel}
              </p>
            </div>
          </div>
          <Trophy className="w-5 h-5 text-amber-400 shrink-0" />
        </div>
      )}

      {boardTab === 'global' ? (
        <div className="rounded-2xl bg-slate-900/80 border border-slate-800 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-800 text-[11px] font-medium text-slate-400">
                  <th className="py-3 px-4">Rank</th>
                  <th className="py-3 px-4">Player</th>
                  <th className="py-3 px-4 text-right">Stars</th>
                  <th className="py-3 px-4 text-right">Level</th>
                  <th className="py-3 px-4 text-right">Score</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/70 text-sm">
                {globalEntries.map((entry, idx) => {
                  const isYou = entry.id === currentGamerId;
                  return (
                    <tr
                      key={entry.id}
                      className={
                        isYou
                          ? 'bg-amber-500/10'
                          : 'hover:bg-slate-800/35 transition-colors'
                      }
                    >
                      <td className="py-3 px-4 font-display font-bold tabular-nums">
                        <span
                          className={
                            idx === 0
                              ? 'text-amber-400'
                              : idx === 1
                                ? 'text-slate-200'
                                : idx === 2
                                  ? 'text-orange-400'
                                  : 'text-slate-400'
                          }
                        >
                          #{idx + 1}
                        </span>
                      </td>
                      <td className="py-3 px-4">
                        <div className="flex items-center gap-2.5 min-w-0">
                          <span
                            className="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold text-slate-950 shrink-0"
                            style={{ backgroundColor: entry.avatarColor }}
                          >
                            {entry.gamerTag.slice(0, 2).toUpperCase()}
                          </span>
                          <div className="min-w-0">
                            <div className="flex items-center gap-1.5">
                              <span className="font-semibold text-white truncate">
                                {entry.gamerTag}
                              </span>
                              {isYou && (
                                <span className="text-[11px] text-amber-400 font-medium">
                                  (You)
                                </span>
                              )}
                              {entry.playGamesVerified && (
                                <ShieldCheck
                                  className="w-3.5 h-3.5 text-emerald-400 shrink-0"
                                  aria-label="Play Games Verified"
                                />
                              )}
                            </div>
                            <p className="text-xs text-slate-400">
                              {entry.country} · {entry.dailyClears} daily clears
                            </p>
                          </div>
                        </div>
                      </td>
                      <td className="py-3 px-4 text-right font-mono tabular-nums text-amber-300">
                        <span className="inline-flex items-center gap-1">
                          {entry.totalStars}
                          <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
                        </span>
                      </td>
                      <td className="py-3 px-4 text-right font-mono tabular-nums text-slate-300">
                        Lv.{entry.highestLevel}
                      </td>
                      <td className="py-3 px-4 text-right font-display font-bold tabular-nums text-white">
                        {entry.bestScore.toLocaleString()}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <div className="rounded-2xl bg-slate-900/80 border border-slate-800 overflow-hidden">
          <div className="px-4 py-3 border-b border-slate-800 flex items-center justify-between text-xs text-slate-400">
            <span>Daily Seed · {dailyDateKey}</span>
            <span>Ranked by fewest moves, then fastest time</span>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-800 text-[11px] font-medium text-slate-400">
                  <th className="py-3 px-4">Rank</th>
                  <th className="py-3 px-4">Player</th>
                  <th className="py-3 px-4 text-right">Moves</th>
                  <th className="py-3 px-4 text-right">Time</th>
                  <th className="py-3 px-4 text-right">Stars</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/70 text-sm">
                {dailyEntries.length === 0 ? (
                  <tr>
                    <td
                      colSpan={5}
                      className="py-8 text-center text-sm text-slate-400"
                    >
                      No daily scores posted yet today. Claim #1!
                    </td>
                  </tr>
                ) : (
                  dailyEntries.map((entry, idx) => {
                    const isYou = entry.id === currentGamerId;
                    return (
                      <tr
                        key={entry.id}
                        className={isYou ? 'bg-cyan-500/10' : 'hover:bg-slate-800/35'}
                      >
                        <td className="py-3 px-4 font-display font-bold tabular-nums text-slate-300">
                          #{idx + 1}
                        </td>
                        <td className="py-3 px-4 font-semibold text-white">
                          {entry.gamerTag}{' '}
                          <span className="text-xs font-normal text-slate-400">
                            · {entry.country}
                          </span>
                        </td>
                        <td className="py-3 px-4 text-right font-mono tabular-nums text-cyan-300">
                          {entry.moves}
                        </td>
                        <td className="py-3 px-4 text-right font-mono tabular-nums text-slate-300">
                          {entry.timeSec}s
                        </td>
                        <td className="py-3 px-4 text-right font-mono tabular-nums text-amber-400">
                          {entry.stars}★
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </section>
  );
};
