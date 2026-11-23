// Google Play Games Services (Android only) + Persistent Global/Daily Leaderboard engine.
// Silent sign-in runs at startup; interactive UI calls prompt sign-in only on explicit tap.

import { Capacitor } from '@capacitor/core';
import { PlayGames } from 'capacitor-play-games';
import { ACHIEVEMENTS } from '../data/themes';
import {
  AchievementDef,
  DailyLeaderboardEntry,
  LeaderboardEntry,
  PlayerProgress,
} from '../types/game';

const STORAGE_KEY = 'slidepulse_player_progress_v1';
const GLOBAL_BOARD_KEY = 'slidepulse_global_leaderboard_v1';
const DAILY_BOARD_KEY = 'slidepulse_daily_leaderboard_v1';

export const PLAY_LEADERBOARD_ID = 'CgkI_SlidePulse_Global';
export const PLAY_DAILY_LEADERBOARD_ID = 'CgkI_SlidePulse_Daily';

export const PLAY_ACHIEVEMENT_IDS: Record<string, string> = {
  'first-slide': 'ach_first-slide',
  'par-master': 'ach_par-master',
  'grid-4x4': 'ach_grid-4x4',
  'grid-5x5': 'ach_grid-5x5',
  'daily-runner': 'ach_daily-runner',
  'theme-collector': 'ach_theme-collector',
  'star-hoarder': 'ach_star-hoarder',
  'pure-solve': 'ach_pure-solve',
};

const native = () => Capacitor.isNativePlatform();
let lastIssue = '';
const errText = (e: unknown) =>
  (e instanceof Error ? e.message : typeof e === 'string' ? e : JSON.stringify(e)) ||
  'unknown error';

let ready: Promise<void> | null = null;

// Called once at startup. Silent sign-in only: never shows a prompt by itself.
export function initPlayGames(): Promise<void> {
  if (!native()) return Promise.resolve();
  if (!ready) {
    ready = (async () => {
      try {
        await PlayGames.initialize();
        const r = await PlayGames.signIn({ silent: true });
        if (!r.signedIn) lastIssue = `Not signed in (${r.reason ?? 'unknown'})`;
      } catch (e) {
        lastIssue = errText(e);
      }
    })();
  }
  return ready;
}

async function signedIn(): Promise<boolean> {
  if (!native()) return false;
  try {
    await initPlayGames();
    return (await PlayGames.isSignedIn()).signedIn;
  } catch {
    return false;
  }
}

export async function signInInteractivePlayGames(): Promise<{
  signedIn: boolean;
  playerId?: string;
  displayName?: string;
}> {
  if (!native()) {
    return { signedIn: true };
  }
  try {
    await initPlayGames();
    const res = await PlayGames.signIn({ silent: false });
    if (!res.signedIn) {
      lastIssue = `Sign-in not completed (${res.reason ?? 'unknown'})`;
      return { signedIn: false };
    }
    return {
      signedIn: true,
      playerId: res.player?.playerId,
      displayName: res.player?.displayName,
    };
  } catch (e) {
    lastIssue = errText(e);
    return { signedIn: false };
  }
}

export async function unlockAchievement(localId: string): Promise<void> {
  const id = PLAY_ACHIEVEMENT_IDS[localId];
  if (!id || !(await signedIn())) return;
  try {
    await PlayGames.unlockAchievement({ id });
  } catch {
    // ignore
  }
}

export async function submitScore(
  score: number,
  leaderboardId = PLAY_LEADERBOARD_ID
): Promise<void> {
  if (!(await signedIn())) return;
  try {
    await PlayGames.submitScore({ leaderboardId, score });
  } catch {
    // ignore
  }
}

export async function showLeaderboard(): Promise<boolean> {
  if (!native()) return false;
  try {
    await initPlayGames();
    if (!(await PlayGames.isSignedIn()).signedIn) {
      const r = await PlayGames.signIn({ silent: false });
      if (!r.signedIn) {
        lastIssue = `Sign-in not completed (${r.reason ?? 'unknown'})`;
        return false;
      }
    }
    await PlayGames.showLeaderboard({ leaderboardId: PLAY_LEADERBOARD_ID });
    return true;
  } catch (e) {
    lastIssue = errText(e);
    return false;
  }
}

export async function showAchievements(): Promise<boolean> {
  if (!native()) return false;
  try {
    await initPlayGames();
    if (!(await PlayGames.isSignedIn()).signedIn) {
      const r = await PlayGames.signIn({ silent: false });
      if (!r.signedIn) {
        lastIssue = `Sign-in not completed (${r.reason ?? 'unknown'})`;
        return false;
      }
    }
    await PlayGames.showAchievements();
    return true;
  } catch (e) {
    lastIssue = errText(e);
    return false;
  }
}

export function getLastIssue(): string {
  return lastIssue;
}

export async function playGamesStatus(): Promise<string> {
  if (!native()) return 'Play Games: only available in the Android app';
  try {
    await initPlayGames();
    const { signedIn: ok } = await PlayGames.isSignedIn();
    if (!ok) {
      return `Play Games: not signed in${lastIssue ? ` - ${lastIssue}` : ''}`;
    }
    const p = await PlayGames.getPlayer();
    return `Play Games: signed in as ${p.displayName || p.playerId}`;
  } catch (e) {
    return `Play Games: error - ${errText(e)}`;
  }
}

export async function syncProgress(
  unlockedIds: string[],
  score: number
): Promise<void> {
  if (!(await signedIn())) return;
  for (const id of unlockedIds) await unlockAchievement(id);
  if (score > 0) await submitScore(score);
}

// ---------- Local Progress & Competitive Standings ----------

export const COUNTRY_OPTIONS = [
  { code: 'US', flag: '🇺🇸', name: 'United States' },
  { code: 'GB', flag: '🇬🇧', name: 'United Kingdom' },
  { code: 'JP', flag: '🇯🇵', name: 'Japan' },
  { code: 'DE', flag: '🇩🇪', name: 'Germany' },
  { code: 'BR', flag: '🇧🇷', name: 'Brazil' },
  { code: 'KR', flag: '🇰🇷', name: 'South Korea' },
  { code: 'CA', flag: '🇨🇦', name: 'Canada' },
  { code: 'FR', flag: '🇫🇷', name: 'France' },
  { code: 'IN', flag: '🇮🇳', name: 'India' },
  { code: 'AU', flag: '🇦🇺', name: 'Australia' },
];

const AVATAR_COLORS = [
  '#f59e0b',
  '#06b6d4',
  '#10b981',
  '#ec4899',
  '#6366f1',
  '#f97316',
];

function getTodayKey(): string {
  return new Date().toISOString().slice(0, 10);
}

function getSeededGlobalLeaderboard(): LeaderboardEntry[] {
  return [
    {
      id: 'gpgs-kael-01',
      gamerTag: 'KaelVortex',
      country: 'JP',
      avatarColor: '#f59e0b',
      totalStars: 64,
      highestLevel: 22,
      bestScore: 18420,
      dailyClears: 19,
      bestDailyMoves: 26,
      playGamesVerified: true,
      updatedAt: new Date(Date.now() - 3600_000 * 2).toISOString(),
    },
    {
      id: 'gpgs-mira-02',
      gamerTag: 'MiraGrid',
      country: 'DE',
      avatarColor: '#06b6d4',
      totalStars: 58,
      highestLevel: 20,
      bestScore: 16890,
      dailyClears: 15,
      bestDailyMoves: 28,
      playGamesVerified: true,
      updatedAt: new Date(Date.now() - 3600_000 * 5).toISOString(),
    },
    {
      id: 'gpgs-arjun-03',
      gamerTag: 'ArjunSlide',
      country: 'IN',
      avatarColor: '#10b981',
      totalStars: 53,
      highestLevel: 18,
      bestScore: 15240,
      dailyClears: 14,
      bestDailyMoves: 29,
      playGamesVerified: true,
      updatedAt: new Date(Date.now() - 3600_000 * 7).toISOString(),
    },
    {
      id: 'gpgs-chloe-04',
      gamerTag: 'ChloeHex',
      country: 'CA',
      avatarColor: '#ec4899',
      totalStars: 47,
      highestLevel: 16,
      bestScore: 13910,
      dailyClears: 11,
      bestDailyMoves: 31,
      playGamesVerified: true,
      updatedAt: new Date(Date.now() - 3600_000 * 9).toISOString(),
    },
    {
      id: 'gpgs-lucas-05',
      gamerTag: 'LucasPrime',
      country: 'BR',
      avatarColor: '#6366f1',
      totalStars: 41,
      highestLevel: 14,
      bestScore: 12150,
      dailyClears: 9,
      bestDailyMoves: 33,
      playGamesVerified: true,
      updatedAt: new Date(Date.now() - 3600_000 * 12).toISOString(),
    },
    {
      id: 'gpgs-hana-06',
      gamerTag: 'HanaZero',
      country: 'KR',
      avatarColor: '#f97316',
      totalStars: 36,
      highestLevel: 13,
      bestScore: 10840,
      dailyClears: 8,
      bestDailyMoves: 34,
      playGamesVerified: true,
      updatedAt: new Date(Date.now() - 3600_000 * 15).toISOString(),
    },
    {
      id: 'gpgs-oliver-07',
      gamerTag: 'OliverShift',
      country: 'GB',
      avatarColor: '#14b8a6',
      totalStars: 29,
      highestLevel: 11,
      bestScore: 8920,
      dailyClears: 6,
      bestDailyMoves: 36,
      playGamesVerified: true,
      updatedAt: new Date(Date.now() - 3600_000 * 19).toISOString(),
    },
    {
      id: 'gpgs-sara-08',
      gamerTag: 'SaraPulse',
      country: 'US',
      avatarColor: '#8b5cf6',
      totalStars: 22,
      highestLevel: 9,
      bestScore: 7150,
      dailyClears: 4,
      bestDailyMoves: 39,
      playGamesVerified: false,
      updatedAt: new Date(Date.now() - 3600_000 * 24).toISOString(),
    },
  ];
}

function getSeededDailyLeaderboard(dateKey: string): {
  dateKey: string;
  entries: DailyLeaderboardEntry[];
} {
  return {
    dateKey,
    entries: [
      {
        id: 'gpgs-kael-01',
        gamerTag: 'KaelVortex',
        country: 'JP',
        moves: 26,
        timeSec: 38,
        stars: 3,
        updatedAt: new Date(Date.now() - 3600_000 * 1).toISOString(),
      },
      {
        id: 'gpgs-mira-02',
        gamerTag: 'MiraGrid',
        country: 'DE',
        moves: 28,
        timeSec: 44,
        stars: 3,
        updatedAt: new Date(Date.now() - 3600_000 * 2).toISOString(),
      },
      {
        id: 'gpgs-arjun-03',
        gamerTag: 'ArjunSlide',
        country: 'IN',
        moves: 29,
        timeSec: 49,
        stars: 3,
        updatedAt: new Date(Date.now() - 3600_000 * 4).toISOString(),
      },
      {
        id: 'gpgs-chloe-04',
        gamerTag: 'ChloeHex',
        country: 'CA',
        moves: 31,
        timeSec: 53,
        stars: 2,
        updatedAt: new Date(Date.now() - 3600_000 * 5).toISOString(),
      },
    ],
  };
}

export function createDefaultProgress(): PlayerProgress {
  const randomSuffix = Math.floor(1000 + Math.random() * 9000);
  return {
    gamerId: `gpgs-player-${randomSuffix}`,
    gamerTag: `MetroSlider${randomSuffix}`,
    country: 'US',
    avatarColor: AVATAR_COLORS[randomSuffix % AVATAR_COLORS.length],
    playGamesSignedIn: true,
    xp: 120,
    highestLevelUnlocked: 1,
    levelStars: {},
    levelBestMoves: {},
    totalScore: 0,
    currencies: {
      stars: 0,
      diamonds: 25,
      coins: 150,
    },
    inventory: {
      hint: 3,
      undo: 4,
      swap: 1,
      doubleCoinsTurns: 0,
    },
    unlockedThemes: ['city-skyline'],
    activeTheme: 'city-skyline',
    unlockedSkins: ['classic'],
    activeSkin: 'classic',
    adsRemoved: false,
    unlockedAchievements: [],
    dailyStreak: 0,
    completedDailyDates: [],
    soundEnabled: true,
    hapticsEnabled: true,
    lastSyncedAt: null,
  };
}

export function loadLocalProgress(): PlayerProgress {
  if (typeof window === 'undefined') return createDefaultProgress();
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      const fresh = createDefaultProgress();
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(fresh));
      return fresh;
    }
    const parsed = JSON.parse(raw) as Partial<PlayerProgress>;
    const defaults = createDefaultProgress();
    return {
      ...defaults,
      ...parsed,
      currencies: { ...defaults.currencies, ...(parsed.currencies || {}) },
      inventory: { ...defaults.inventory, ...(parsed.inventory || {}) },
      unlockedThemes: parsed.unlockedThemes?.length
        ? parsed.unlockedThemes
        : ['city-skyline'],
      unlockedSkins: parsed.unlockedSkins?.length
        ? parsed.unlockedSkins
        : ['classic'],
    };
  } catch {
    return createDefaultProgress();
  }
}

export function saveLocalProgress(progress: PlayerProgress): void {
  if (typeof window === 'undefined') return;
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(progress));
  } catch {
    // Ignore storage quota errors
  }
}

export function evaluateAchievements(
  progress: PlayerProgress,
  context: {
    level: number;
    size: number;
    starsEarned: number;
    isDaily: boolean;
    usedPowerUp: boolean;
  }
): AchievementDef[] {
  const unlocked = new Set(progress.unlockedAchievements);
  const newlyUnlocked: AchievementDef[] = [];

  const grant = (id: string) => {
    if (unlocked.has(id)) return;
    const def = ACHIEVEMENTS.find((a) => a.id === id);
    if (def) {
      unlocked.add(id);
      newlyUnlocked.push(def);
      void unlockAchievement(id);
    }
  };

  if (context.level >= 1) grant('first-slide');
  if (context.starsEarned === 3) grant('par-master');
  if (context.size >= 4) grant('grid-4x4');
  if (context.size >= 5) grant('grid-5x5');
  if (context.isDaily) grant('daily-runner');
  if (progress.unlockedThemes.length > 1) grant('theme-collector');
  if (progress.currencies.stars >= 15) grant('star-hoarder');
  if (context.size >= 4 && !context.usedPowerUp) grant('pure-solve');

  return newlyUnlocked;
}

export function getPlayerLevelFromXp(xp: number): {
  level: number;
  currentXpInLevel: number;
  xpForNextLevel: number;
} {
  const xpPerLevel = 450;
  const level = Math.floor(xp / xpPerLevel) + 1;
  return {
    level,
    currentXpInLevel: xp % xpPerLevel,
    xpForNextLevel: xpPerLevel,
  };
}

export async function syncProfileToCloud(
  progress: PlayerProgress
): Promise<string | null> {
  saveLocalProgress(progress);
  if (!progress.playGamesSignedIn) return null;
  await syncProgress(progress.unlockedAchievements, progress.totalScore);
  return new Date().toISOString();
}

export async function fetchLeaderboards(): Promise<{
  global: LeaderboardEntry[];
  daily: { dateKey: string; entries: DailyLeaderboardEntry[] };
}> {
  const today = getTodayKey();
  let globalList: LeaderboardEntry[] = [];
  let dailyBoard = getSeededDailyLeaderboard(today);

  if (typeof window !== 'undefined') {
    try {
      const rawGlobal = window.localStorage.getItem(GLOBAL_BOARD_KEY);
      if (rawGlobal) {
        globalList = JSON.parse(rawGlobal) as LeaderboardEntry[];
      } else {
        globalList = getSeededGlobalLeaderboard();
        window.localStorage.setItem(
          GLOBAL_BOARD_KEY,
          JSON.stringify(globalList)
        );
      }

      const rawDaily = window.localStorage.getItem(DAILY_BOARD_KEY);
      if (rawDaily) {
        const parsedDaily = JSON.parse(rawDaily) as {
          dateKey: string;
          entries: DailyLeaderboardEntry[];
        };
        if (parsedDaily.dateKey === today) {
          dailyBoard = parsedDaily;
        } else {
          window.localStorage.setItem(
            DAILY_BOARD_KEY,
            JSON.stringify(dailyBoard)
          );
        }
      } else {
        window.localStorage.setItem(
          DAILY_BOARD_KEY,
          JSON.stringify(dailyBoard)
        );
      }
    } catch {
      globalList = getSeededGlobalLeaderboard();
    }
  } else {
    globalList = getSeededGlobalLeaderboard();
  }

  const sortedGlobal = [...globalList].sort((a, b) => {
    if (b.bestScore !== a.bestScore) return b.bestScore - a.bestScore;
    if (b.totalStars !== a.totalStars) return b.totalStars - a.totalStars;
    return b.highestLevel - a.highestLevel;
  });

  const sortedDaily = [...dailyBoard.entries].sort((a, b) => {
    if (a.moves !== b.moves) return a.moves - b.moves;
    return a.timeSec - b.timeSec;
  });

  return {
    global: sortedGlobal.slice(0, 50),
    daily: {
      dateKey: dailyBoard.dateKey,
      entries: sortedDaily.slice(0, 50),
    },
  };
}

export async function submitGlobalScore(
  progress: PlayerProgress,
  bestDailyMoves: number | null = null
): Promise<{ rank: number; global: LeaderboardEntry[] }> {
  const current = await fetchLeaderboards();
  const list = [...current.global];
  const idx = list.findIndex((e) => e.id === progress.gamerId);

  const entry: LeaderboardEntry = {
    id: progress.gamerId,
    gamerTag: progress.gamerTag,
    country: progress.country,
    avatarColor: progress.avatarColor,
    totalStars: progress.currencies.stars,
    highestLevel: progress.highestLevelUnlocked,
    bestScore: progress.totalScore,
    dailyClears: progress.completedDailyDates.length,
    bestDailyMoves:
      bestDailyMoves ?? (idx >= 0 ? list[idx].bestDailyMoves : null),
    playGamesVerified: progress.playGamesSignedIn,
    updatedAt: new Date().toISOString(),
  };

  if (idx >= 0) {
    list[idx] = {
      ...list[idx],
      ...entry,
      bestScore: Math.max(list[idx].bestScore, entry.bestScore),
      totalStars: Math.max(list[idx].totalStars, entry.totalStars),
      highestLevel: Math.max(list[idx].highestLevel, entry.highestLevel),
    };
  } else {
    list.push(entry);
  }

  const sorted = list.sort((a, b) => b.bestScore - a.bestScore);
  if (typeof window !== 'undefined') {
    window.localStorage.setItem(GLOBAL_BOARD_KEY, JSON.stringify(sorted));
  }

  if (progress.playGamesSignedIn) {
    await submitScore(progress.totalScore, PLAY_LEADERBOARD_ID);
  }

  const rank = sorted.findIndex((e) => e.id === progress.gamerId) + 1;
  return { rank, global: sorted.slice(0, 50) };
}

export async function submitDailyScore(
  progress: PlayerProgress,
  moves: number,
  timeSec: number,
  stars: number
): Promise<{
  rank: number;
  daily: { dateKey: string; entries: DailyLeaderboardEntry[] };
}> {
  const current = await fetchLeaderboards();
  const entries = [...current.daily.entries];
  const idx = entries.findIndex((e) => e.id === progress.gamerId);

  const entry: DailyLeaderboardEntry = {
    id: progress.gamerId,
    gamerTag: progress.gamerTag,
    country: progress.country,
    moves,
    timeSec,
    stars,
    updatedAt: new Date().toISOString(),
  };

  if (idx >= 0) {
    const prev = entries[idx];
    if (moves < prev.moves || (moves === prev.moves && timeSec < prev.timeSec)) {
      entries[idx] = entry;
    }
  } else {
    entries.push(entry);
  }

  const sorted = entries.sort((a, b) => {
    if (a.moves !== b.moves) return a.moves - b.moves;
    return a.timeSec - b.timeSec;
  });

  const payload = { dateKey: current.daily.dateKey, entries: sorted };
  if (typeof window !== 'undefined') {
    window.localStorage.setItem(DAILY_BOARD_KEY, JSON.stringify(payload));
  }

  if (progress.playGamesSignedIn) {
    await submitScore(moves, PLAY_DAILY_LEADERBOARD_ID);
  }

  const rank = sorted.findIndex((e) => e.id === progress.gamerId) + 1;
  return { rank, daily: { dateKey: payload.dateKey, entries: sorted.slice(0, 50) } };
}
