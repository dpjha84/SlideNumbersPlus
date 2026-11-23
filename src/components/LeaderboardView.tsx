import React, { useEffect, useState } from 'react';
import { Award, Flame, Globe, RefreshCw, ShieldCheck, Star, Trophy } from 'lucide-react';
import { ACHIEVEMENTS } from '../data/themes';
import {
  DailyLeaderboardEntry,
  LeaderboardEntry,
  PlayerProgress,
} from '../types/game';
import {
  getLastIssue,
  playGamesStatus,
  showAchievements,
  showLeaderboard,
} from '../utils/playGames';

interface LeaderboardViewProps {
  globalEntries: LeaderboardEntry[];
  dailyDateKey: string;
  dailyEntries: DailyLeaderboardEntry[];
  progress: PlayerProgress;
  loading: boolean;
  onRefresh: () => void;
}

export const LeaderboardView: React.FC<LeaderboardViewProps> = ({
  globalEntries,
  dailyDateKey,
  dailyEntries,
  progress,
  loading,
  onRefresh,
}) => {
  const [boardTab, setBoardTab] = useState<'global' | 'daily'>('global');
  const [pgMsg, setPgMsg] = useState<string | null>(null);
  const [pgStatus, setPgStatus] = useState('Play Games: checking...');

  useEffect(() => {
    void playGamesStatus().then(setPgStatus);
  }, [pgMsg]);

  const playerGlobalIdx = globalEntries.findIndex(
    (e) => e.id === progress.gamerId
  );
  const clearedLevelsCount = Object.keys(progress.levelStars).length;

  const handleOpenPlayGamesLeaderboard = async () => {
    const ok = await showLeaderboard();
    setPgMsg(
      ok ? null : `Could not open Google Play Games. ${getLastIssue()}`.trim()
    );
  };

  const handleOpenPlayGamesAchievements = async () => {
    const ok = await showAchievements();
    setPgMsg(
      ok ? null : `Could not open Google Play Games. ${getLastIssue()}`.trim()
    );
  };

  return (
    <section className="space-y-5">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-display font-bold text-white flex items-center gap-2">
            <Globe className="w-6 h-6 text-amber-400" />
            <span>Global Leaderboards & Stats</span>
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

      {/* Player Stats & Google Play Games Card (color-sort-puzzle pattern) */}
      <div className="rounded-2xl bg-slate-900/85 border border-slate-800 p-4 space-y-3.5">
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
          <div className="rounded-xl bg-slate-950/80 border border-slate-800 p-3">
            <p className="text-[11px] text-slate-400">Levels Cleared</p>
            <p className="text-lg font-display font-bold text-white tabular-nums">
              {clearedLevelsCount}
            </p>
          </div>
          <div className="rounded-xl bg-slate-950/80 border border-slate-800 p-3">
            <p className="text-[11px] text-slate-400">Total Stars</p>
            <p className="text-lg font-display font-bold text-amber-300 flex items-center gap-1 tabular-nums">
              <Star className="w-4 h-4 fill-amber-400 text-amber-400" />
              <span>{progress.currencies.stars}</span>
            </p>
          </div>
          <div className="rounded-xl bg-slate-950/80 border border-slate-800 p-3">
            <p className="text-[11px] text-slate-400">Total Score</p>
            <p className="text-lg font-display font-bold text-emerald-300 tabular-nums">
              {progress.totalScore.toLocaleString()}
            </p>
          </div>
          <div className="rounded-xl bg-slate-950/80 border border-slate-800 p-3">
            <p className="text-[11px] text-slate-400">Daily & Trophies</p>
            <p className="text-lg font-display font-bold text-cyan-300 flex items-center gap-1.5 tabular-nums">
              <Flame className="w-4 h-4 text-orange-400" />
              <span>{progress.dailyStreak}d</span>
              <span className="text-xs text-slate-500">·</span>
              <span className="text-sm text-slate-200">
                {progress.unlockedAchievements.length}/{ACHIEVEMENTS.length}
              </span>
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
          <button
            type="button"
            onClick={handleOpenPlayGamesLeaderboard}
            className="w-full py-2.5 px-4 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-500 hover:brightness-105 text-slate-950 font-bold text-xs sm:text-sm flex items-center justify-center gap-2 transition-all active:scale-98 cursor-pointer"
          >
            <Globe className="w-4 h-4" />
            <span>Global Leaderboard (Play Games)</span>
          </button>
          <button
            type="button"
            onClick={handleOpenPlayGamesAchievements}
            className="w-full py-2.5 px-4 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-white font-bold text-xs sm:text-sm flex items-center justify-center gap-2 transition-all active:scale-98 cursor-pointer"
          >
            <Award className="w-4 h-4 text-amber-400" />
            <span>Play Games Achievements</span>
          </button>
        </div>

        {pgMsg && (
          <p className="text-center text-xs text-amber-300 break-words">
            {pgMsg}
          </p>
        )}
        <p className="text-center text-[11px] text-slate-400 break-words">
          {pgStatus}
        </p>
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
                Your Global Standing
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
                  const isYou = entry.id === progress.gamerId;
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
                    const isYou = entry.id === progress.gamerId;
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
