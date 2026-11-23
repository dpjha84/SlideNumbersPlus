import {
  AchievementDef,
  LevelDefinition,
  PowerUpItem,
  ThemeConfig,
  ThemeId,
  TileSkinConfig,
} from '../types/game';

export const THEMES: Record<ThemeId, ThemeConfig> = {
  'city-skyline': {
    id: 'city-skyline',
    name: 'City Skyline',
    subtitle: 'Golden-hour steel & amber metropolis',
    priceDiamonds: 0,
    imageUrl: '/src/assets/images/theme_city_skyline_1795426782102.jpg',
    fallbackGradient:
      'linear-gradient(160deg, #0f172a 0%, #1e293b 52%, #78350f 100%)',
    canvasBg: 'bg-slate-950',
    boardFrame:
      'bg-slate-900/65 backdrop-blur-md border border-amber-400/35 shadow-2xl shadow-amber-950/50',
    boardWell: 'bg-slate-950/35 border border-white/15',
    tileBase:
      'bg-gradient-to-b from-slate-700/95 to-slate-800/95 border border-slate-400/45 shadow-[0_5px_0_0_rgba(15,23,42,0.9),0_8px_16px_rgba(0,0,0,0.45)]',
    tileSolved:
      'bg-gradient-to-b from-amber-400 to-amber-500 border border-amber-200/80 shadow-[0_5px_0_0_rgba(180,83,9,0.95),0_8px_18px_rgba(245,158,11,0.35)]',
    tileText: 'text-slate-100',
    tileSolvedText: 'text-slate-950',
    accentColor: '#f59e0b',
    accentBg: 'bg-amber-500 hover:bg-amber-400 text-slate-950',
    accentText: 'text-amber-400',
    glowStyle: 'shadow-amber-500/20',
    soundProfile: 'warm',
  },
  'neon-cyberpunk': {
    id: 'neon-cyberpunk',
    name: 'Neon Cyberpunk',
    subtitle: 'Midnight synth rain & electric cyan edges',
    priceDiamonds: 35,
    imageUrl: '/src/assets/images/theme_neon_cyberpunk_1795426782692.jpg',
    fallbackGradient:
      'linear-gradient(155deg, #050816 0%, #0f172a 48%, #3b0764 100%)',
    canvasBg: 'bg-[#050816]',
    boardFrame:
      'bg-[#090d22]/65 backdrop-blur-md border border-cyan-400/45 shadow-2xl shadow-cyan-950/60',
    boardWell: 'bg-[#040712]/35 border border-fuchsia-500/25',
    tileBase:
      'bg-gradient-to-b from-[#131b3a]/95 to-[#0b1024]/95 border border-cyan-400/45 shadow-[0_5px_0_0_rgba(6,182,212,0.35),0_8px_18px_rgba(0,0,0,0.65)]',
    tileSolved:
      'bg-gradient-to-b from-cyan-400 to-teal-400 border border-cyan-100/90 shadow-[0_5px_0_0_rgba(8,145,178,0.95),0_0_20px_rgba(34,211,238,0.45)]',
    tileText: 'text-cyan-100',
    tileSolvedText: 'text-slate-950',
    accentColor: '#22d3ee',
    accentBg: 'bg-cyan-400 hover:bg-cyan-300 text-slate-950',
    accentText: 'text-cyan-400',
    glowStyle: 'shadow-cyan-500/30',
    soundProfile: 'synth',
  },
  'alpine-timber': {
    id: 'alpine-timber',
    name: 'Alpine Timber',
    subtitle: 'Hand-carved walnut & brushed brass inlay',
    priceDiamonds: 60,
    imageUrl: '/src/assets/images/theme_alpine_timber_1795426781602.jpg',
    fallbackGradient:
      'linear-gradient(160deg, #27170e 0%, #422515 50%, #1c100a 100%)',
    canvasBg: 'bg-[#1a110b]',
    boardFrame:
      'bg-[#2a1a10]/70 backdrop-blur-md border border-amber-600/40 shadow-2xl shadow-black/60',
    boardWell: 'bg-[#170e09]/35 border border-amber-700/30',
    tileBase:
      'bg-gradient-to-b from-[#e7d6be] to-[#cbb18e] border border-[#f5ebd9]/80 shadow-[0_5px_0_0_rgba(120,83,48,0.95),0_8px_14px_rgba(0,0,0,0.45)]',
    tileSolved:
      'bg-gradient-to-b from-[#d97706] to-[#b45309] border border-amber-300/80 shadow-[0_5px_0_0_rgba(120,53,15,0.95),0_8px_16px_rgba(217,119,6,0.3)]',
    tileText: 'text-[#2b190d]',
    tileSolvedText: 'text-amber-50',
    accentColor: '#d97706',
    accentBg: 'bg-amber-600 hover:bg-amber-500 text-white',
    accentText: 'text-amber-400',
    glowStyle: 'shadow-amber-700/25',
    soundProfile: 'wood',
  },
  'arctic-marble': {
    id: 'arctic-marble',
    name: 'Arctic Marble',
    subtitle: 'Carrara stone & glacial cobalt precision',
    priceDiamonds: 85,
    imageUrl: '/src/assets/images/theme_arctic_marble_1795426781800.jpg',
    fallbackGradient:
      'linear-gradient(160deg, #0f172a 0%, #334155 55%, #475569 100%)',
    canvasBg: 'bg-slate-900',
    boardFrame:
      'bg-slate-800/65 backdrop-blur-md border border-sky-300/35 shadow-2xl shadow-slate-950/60',
    boardWell: 'bg-slate-950/35 border border-sky-200/20',
    tileBase:
      'bg-gradient-to-b from-slate-100 to-slate-200 border border-white shadow-[0_5px_0_0_rgba(100,116,139,0.9),0_8px_14px_rgba(0,0,0,0.35)]',
    tileSolved:
      'bg-gradient-to-b from-sky-500 to-blue-600 border border-sky-200/80 shadow-[0_5px_0_0_rgba(29,78,216,0.95),0_8px_18px_rgba(56,189,248,0.35)]',
    tileText: 'text-slate-900',
    tileSolvedText: 'text-white',
    accentColor: '#38bdf8',
    accentBg: 'bg-sky-400 hover:bg-sky-300 text-slate-950',
    accentText: 'text-sky-400',
    glowStyle: 'shadow-sky-500/20',
    soundProfile: 'crystal',
  },
  'obsidian-gold': {
    id: 'obsidian-gold',
    name: 'Obsidian Gold',
    subtitle: 'Grandmaster matte carbon & 24k trim',
    priceDiamonds: 140,
    requiredStars: 18,
    imageUrl: null,
    fallbackGradient:
      'radial-gradient(circle at 25% 20%, rgba(234,179,8,0.16) 0%, transparent 45%), linear-gradient(160deg, #09090b 0%, #18181b 55%, #27272a 100%)',
    canvasBg: 'bg-zinc-950',
    boardFrame:
      'bg-zinc-900/85 backdrop-blur-md border border-yellow-500/45 shadow-2xl shadow-yellow-950/30',
    boardWell: 'bg-black/60 border border-yellow-500/25',
    tileBase:
      'bg-gradient-to-b from-zinc-800 to-zinc-900 border border-yellow-500/40 shadow-[0_5px_0_0_rgba(9,9,11,0.98),0_8px_16px_rgba(0,0,0,0.6)]',
    tileSolved:
      'bg-gradient-to-b from-yellow-300 via-amber-400 to-yellow-500 border border-yellow-100 shadow-[0_5px_0_0_rgba(161,98,7,0.98),0_0_18px_rgba(234,179,8,0.4)]',
    tileText: 'text-yellow-100',
    tileSolvedText: 'text-zinc-950',
    accentColor: '#eab308',
    accentBg: 'bg-yellow-400 hover:bg-yellow-300 text-zinc-950',
    accentText: 'text-yellow-400',
    glowStyle: 'shadow-yellow-500/25',
    soundProfile: 'metallic',
  },
};

export const THEME_LIST: ThemeConfig[] = Object.values(THEMES);

export const TILE_SKINS: TileSkinConfig[] = [
  {
    id: 'classic',
    name: 'Architectural Bevel',
    description: 'Clean chamfered edges with tactile elevation.',
    priceDiamonds: 0,
    priceCoins: 0,
    tileClassModifier: 'rounded-xl',
  },
  {
    id: 'prism',
    name: 'Frosted Prism',
    description: 'Translucent inner specular rim and razor corners.',
    priceDiamonds: 25,
    priceCoins: 350,
    tileClassModifier: 'rounded-lg ring-1 ring-inset ring-white/30',
  },
  {
    id: 'brass',
    name: 'Monolith Pill',
    description: 'Soft pebble curvature with high-precision corner dot.',
    priceDiamonds: 45,
    priceCoins: 600,
    tileClassModifier: 'rounded-2xl ring-2 ring-inset ring-white/15',
  },
];

export const POWER_UPS: PowerUpItem[] = [
  {
    id: 'hint',
    name: 'Hint Magnet',
    description: 'Highlights the optimal next tile to slide toward solving.',
    priceCoins: 80,
    priceDiamonds: 4,
    packAmount: 2,
  },
  {
    id: 'undo',
    name: 'Step Rewind',
    description: 'Reverts your last move and restores move count cleanly.',
    priceCoins: 60,
    priceDiamonds: 3,
    packAmount: 3,
  },
  {
    id: 'swap',
    name: 'Tile Swap',
    description: 'Select any two tiles on the board to swap their positions.',
    priceCoins: 140,
    priceDiamonds: 7,
    packAmount: 1,
  },
  {
    id: 'doubleCoins',
    name: '2× Coin Booster',
    description: 'Doubles coin payouts for your next 3 completed puzzles.',
    priceCoins: 180,
    priceDiamonds: 8,
    packAmount: 3,
  },
];

export const ACHIEVEMENTS: AchievementDef[] = [
  {
    id: 'first-slide',
    title: 'First Metro Clear',
    description: 'Complete Level 1 and earn your first stars.',
    xp: 100,
    rewardDiamonds: 5,
  },
  {
    id: 'par-master',
    title: 'Precision Tactician',
    description: 'Beat the Target Move par to earn a 3-Star rating.',
    xp: 200,
    rewardDiamonds: 10,
  },
  {
    id: 'grid-4x4',
    title: 'Sixteen-Tile Pro',
    description: 'Unlock and solve your first 4×4 grid puzzle.',
    xp: 300,
    rewardDiamonds: 15,
  },
  {
    id: 'grid-5x5',
    title: 'Twenty-Four Master',
    description: 'Unlock and solve your first 5×5 grid puzzle.',
    xp: 500,
    rewardDiamonds: 25,
  },
  {
    id: 'daily-runner',
    title: 'Daily Challenger',
    description: 'Complete today’s global Daily Challenge.',
    xp: 250,
    rewardDiamonds: 15,
  },
  {
    id: 'theme-collector',
    title: 'Aesthetic Architect',
    description: 'Unlock an alternate board theme in the Store.',
    xp: 250,
    rewardDiamonds: 12,
  },
  {
    id: 'star-hoarder',
    title: 'Constellation',
    description: 'Collect 15 total Stars across campaign levels.',
    xp: 400,
    rewardDiamonds: 20,
  },
  {
    id: 'pure-solve',
    title: 'Zero-Assist Clear',
    description: 'Win a 4×4 or larger level without using any power-ups.',
    xp: 350,
    rewardDiamonds: 18,
  },
];

const LEVEL_TITLES = [
  'Harbor Gate',
  'Midtown Grid',
  'Transit Loop',
  'Skybridge',
  'Clocktower Plaza',
  'Signal Tower',
  'Viaduct Crossing',
  'Neon District',
  'Observatory Deck',
  'Central Terminal',
  'Monorail Core',
  'Citadel Spire',
  'Apex Junction',
  'Substation Nine',
  'Quantum Plaza',
  'Orbital Spire',
  'Zenith Vault',
  'Chrono Matrix',
  'Eclipse Tower',
  'Grandmaster Core',
];

export function getLevelDefinition(level: number): LevelDefinition {
  const safeLevel = Math.max(1, level);
  let size = 3;
  let scrambleSteps = 10;
  let targetMoves = 12;
  let tierLabel = '3×3 Warm-Up';

  if (safeLevel <= 3) {
    size = 3;
    scrambleSteps = 8 + safeLevel * 4; // 12, 16, 20
    targetMoves = scrambleSteps + 2;
    tierLabel = '3×3 Warm-Up';
  } else if (safeLevel <= 8) {
    size = 4;
    const offset = safeLevel - 3;
    scrambleSteps = 18 + offset * 6; // 24, 30, 36, 42, 48
    targetMoves = scrambleSteps + 4;
    tierLabel = '4×4 Tactical';
  } else if (safeLevel <= 14) {
    size = 5;
    const offset = safeLevel - 8;
    scrambleSteps = 34 + offset * 8; // 42..82
    targetMoves = scrambleSteps + 8;
    tierLabel = '5×5 Master';
  } else {
    size = 6;
    const offset = safeLevel - 14;
    scrambleSteps = 56 + offset * 10;
    targetMoves = scrambleSteps + 14;
    tierLabel = '6×6 Grandmaster';
  }

  const title =
    LEVEL_TITLES[safeLevel - 1] ?? `Sector ${safeLevel}`;

  return {
    level: safeLevel,
    title,
    size,
    scrambleSteps,
    targetMoves,
    rewardCoins: 30 + safeLevel * 12,
    rewardDiamonds: 3 + Math.floor(safeLevel / 3) * 2,
    tierLabel,
  };
}

export function getDailyChallengeDefinition(dateKey: string): LevelDefinition {
  // Deterministic difficulty between 4x4 and 5x5 depending on day of month
  const dayNum = parseInt(dateKey.slice(-2), 10) || 15;
  const isFive = dayNum % 4 === 0;
  const size = isFive ? 5 : 4;
  const scrambleSteps = isFive ? 46 : 32;
  const targetMoves = isFive ? 54 : 36;

  return {
    level: 0,
    title: `Daily Grid · ${dateKey}`,
    size,
    scrambleSteps,
    targetMoves,
    rewardCoins: 140,
    rewardDiamonds: 18,
    tierLabel: isFive ? 'Daily 5×5' : 'Daily 4×4',
  };
}
