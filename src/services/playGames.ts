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

// Replace with your Play Console Leaderboard & Achievement IDs before Play Store publishing
export const PLAY_GAMES_IDS = {
  globalLeaderboard: 'CgkI_SlidePulse_Global',
  dailyLeaderboard: 'CgkI_SlidePulse_Daily',
};

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

export async function signInPlayGames(): Promise<{
  playerId: string;
  displayName: string;
  isSignedIn: boolean;
} | null> {
  try {
    if (Capacitor.isNativePlatform()) {
      return await PlayGames.signIn();
    }
    return await PlayGames.getPlayerProfile();
  } catch {
    return null;
  }
}

export async function openNativePlayGamesLeaderboard(): Promise<void> {
  if (!Capacitor.isNativePlatform()) return;
  try {
    await PlayGames.showLeaderboard({
      leaderboardId: PLAY_GAMES_IDS.globalLeaderboard,
    });
  } catch {
    // Fallback handled by in-app LeaderboardView
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
      if (Capacitor.isNativePlatform()) {
        void PlayGames.unlockAchievement({ achievementId: `ach_${id}` });
      }
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
  if (Capacitor.isNativePlatform()) {
    try {
      await PlayGames.submitScore({
        leaderboardId: PLAY_GAMES_IDS.globalLeaderboard,
        score: progress.totalScore,
      });
    } catch {
      // Continue
    }
  }
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

  if (Capacitor.isNativePlatform() && progress.playGamesSignedIn) {
    try {
      await PlayGames.submitScore({
        leaderboardId: PLAY_GAMES_IDS.globalLeaderboard,
        score: progress.totalScore,
      });
    } catch {
      // Ignore native bridge error in offline tests
    }
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

  const rank = sorted.findIndex((e) => e.id === progress.gamerId) + 1;
  return { rank, daily: { dateKey: payload.dateKey, entries: sorted.slice(0, 50) } };
}
