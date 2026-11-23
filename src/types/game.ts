export type ThemeId =
  | 'city-skyline'
  | 'neon-cyberpunk'
  | 'alpine-timber'
  | 'arctic-marble'
  | 'obsidian-gold';

export type TileSkinId = 'classic' | 'prism' | 'brass';

export type PowerUpType = 'hint' | 'undo' | 'swap' | 'doubleCoins';

export type NavTab = 'play' | 'daily' | 'levels' | 'store' | 'leaderboard';

export interface LevelDefinition {
  level: number;
  title: string;
  size: number;
  scrambleSteps: number;
  targetMoves: number;
  rewardCoins: number;
  rewardDiamonds: number;
  tierLabel: string;
}

export interface ThemeConfig {
  id: ThemeId;
  name: string;
  subtitle: string;
  priceDiamonds: number;
  requiredStars?: number;
  imageUrl: string | null;
  fallbackGradient: string;
  canvasBg: string;
  boardFrame: string;
  boardWell: string;
  tileBase: string;
  tileSolved: string;
  tileText: string;
  tileSolvedText: string;
  accentColor: string;
  accentBg: string;
  accentText: string;
  glowStyle: string;
  soundProfile: 'warm' | 'synth' | 'wood' | 'crystal' | 'metallic';
}

export interface TileSkinConfig {
  id: TileSkinId;
  name: string;
  description: string;
  priceDiamonds: number;
  priceCoins: number;
  tileClassModifier: string;
}

export interface PowerUpItem {
  id: PowerUpType;
  name: string;
  description: string;
  priceCoins: number;
  priceDiamonds: number;
  packAmount: number;
}

export interface AchievementDef {
  id: string;
  title: string;
  description: string;
  xp: number;
  rewardDiamonds: number;
}

export interface PlayerProgress {
  gamerId: string;
  gamerTag: string;
  country: string;
  avatarColor: string;
  playGamesSignedIn: boolean;
  xp: number;
  highestLevelUnlocked: number;
  levelStars: Record<number, number>;
  levelBestMoves: Record<number, number>;
  totalScore: number;
  currencies: {
    stars: number;
    diamonds: number;
    coins: number;
  };
  inventory: {
    hint: number;
    undo: number;
    swap: number;
    doubleCoinsTurns: number;
  };
  unlockedThemes: ThemeId[];
  activeTheme: ThemeId;
  unlockedSkins: TileSkinId[];
  activeSkin: TileSkinId;
  adsRemoved: boolean;
  unlockedAchievements: string[];
  dailyStreak: number;
  completedDailyDates: string[];
  soundEnabled: boolean;
  hapticsEnabled: boolean;
  lastSyncedAt: string | null;
}

export interface LeaderboardEntry {
  id: string;
  gamerTag: string;
  country: string;
  avatarColor: string;
  totalStars: number;
  highestLevel: number;
  bestScore: number;
  dailyClears: number;
  bestDailyMoves: number | null;
  playGamesVerified: boolean;
  updatedAt: string;
}

export interface DailyLeaderboardEntry {
  id: string;
  gamerTag: string;
  country: string;
  moves: number;
  timeSec: number;
  stars: number;
  updatedAt: string;
}

export interface LevelCompletionResult {
  level: number;
  isDaily: boolean;
  moves: number;
  targetMoves: number;
  timeSec: number;
  starsEarned: number;
  newBestStars: boolean;
  coinsEarned: number;
  diamondsEarned: number;
  bonusStarsEarned: number;
  scoreEarned: number;
  newlyUnlockedAchievements: AchievementDef[];
}
