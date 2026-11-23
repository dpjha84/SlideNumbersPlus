import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  Calendar,
  Coins,
  Flame,
  Gamepad2,
  Gem,
  Globe,
  Grid3X3,
  Lightbulb,
  Palette,
  RotateCcw,
  ShieldCheck,
  Shuffle,
  Sparkles,
  Star,
  Store,
  Trophy,
  Undo2,
  Volume2,
  VolumeX,
} from 'lucide-react';
import {
  AdMobBanner,
  AdMobInterstitialModal,
  AdMobRewardedModal,
} from './components/AdMobOverlays';
import { DailyChallengeView } from './components/DailyChallengeView';
import { LeaderboardView } from './components/LeaderboardView';
import { LevelSelectView } from './components/LevelSelectView';
import { PlayGamesModal } from './components/PlayGamesModal';
import { PuzzleBoard } from './components/PuzzleBoard';
import { StoreView } from './components/StoreView';
import { VictoryModal } from './components/VictoryModal';
import {
  POWER_UPS,
  THEMES,
  TILE_SKINS,
  getDailyChallengeDefinition,
  getLevelDefinition,
} from './data/themes';
import {
  calculateScore,
  calculateStars,
  countCorrectTiles,
  findBestNextTileIndex,
  generateSolvableBoard,
  isSolved,
  slideByDirection,
  slideTileAt,
} from './engine/puzzle';
import {
  adMobManager,
  canShowInterstitial,
  recordInterstitialShown,
} from './utils/admob';
import { playGameSound, triggerHaptic } from './utils/feedback';
import {
  evaluateAchievements,
  fetchLeaderboards,
  getPlayerLevelFromXp,
  initPlayGames,
  loadLocalProgress,
  saveLocalProgress,
  submitDailyScore,
  submitGlobalScore,
  syncProfileToCloud,
  syncProgress,
  unlockAchievement,
} from './utils/playGames';
import {
  DailyLeaderboardEntry,
  LeaderboardEntry,
  LevelCompletionResult,
  NavTab,
  PlayerProgress,
  PowerUpType,
  ThemeId,
  TileSkinId,
} from './types/game';

function getTodayDateKey(): string {
  return new Date().toISOString().slice(0, 10);
}

interface HistorySnapshot {
  board: number[];
  moves: number;
}

export default function App() {
  const [progress, setProgress] = useState<PlayerProgress>(() =>
    loadLocalProgress()
  );
  const progressRef = useRef(progress);
  progressRef.current = progress;
  const [activeTab, setActiveTab] = useState<NavTab>('play');
  const [activeLevel, setActiveLevel] = useState<number>(() =>
    loadLocalProgress().highestLevelUnlocked
  );
  const [isDailyMode, setIsDailyMode] = useState(false);
  const [dailyDateKey, setDailyDateKey] = useState<string>(() =>
    getTodayDateKey()
  );

  // Active Puzzle Board State
  const currentLevelDef = useMemo(
    () =>
      isDailyMode
        ? getDailyChallengeDefinition(dailyDateKey)
        : getLevelDefinition(activeLevel),
    [isDailyMode, dailyDateKey, activeLevel]
  );

  const [board, setBoard] = useState<number[]>(() =>
    generateSolvableBoard(
      currentLevelDef.size,
      currentLevelDef.scrambleSteps,
      isDailyMode
        ? `daily-${dailyDateKey}`
        : `campaign-level-${activeLevel}`
    )
  );
  const [history, setHistory] = useState<HistorySnapshot[]>([]);
  const [moves, setMoves] = useState(0);
  const [elapsedSec, setElapsedSec] = useState(0);
  const [hasStartedMoving, setHasStartedMoving] = useState(false);
  const [usedPowerUpThisRun, setUsedPowerUpThisRun] = useState(false);
  const [hintTileIdx, setHintTileIdx] = useState<number | null>(null);
  const [swapMode, setSwapMode] = useState(false);
  const [swapFirstIdx, setSwapFirstIdx] = useState<number | null>(null);
  const [invalidShakeIdx, setInvalidShakeIdx] = useState<number | null>(null);
  const lastBlankRef = useRef<number | null>(null);

  // Modals & Overlays
  const [victoryResult, setVictoryResult] =
    useState<LevelCompletionResult | null>(null);
  const [interstitialOpen, setInterstitialOpen] = useState(false);
  const [pendingNextLevel, setPendingNextLevel] = useState<number | null>(null);
  const [rewardedAdContext, setRewardedAdContext] = useState<
    null | { type: 'store' } | { type: 'victory'; amount: number }
  >(null);
  const rewardedResolveRef = useRef<(() => void) | null>(null);
  const [playGamesModalOpen, setPlayGamesModalOpen] = useState(false);
  const [syncingProfile, setSyncingProfile] = useState(false);
  const [toastMsg, setToastMsg] = useState<string | null>(null);

  // Leaderboards
  const [globalEntries, setGlobalEntries] = useState<LeaderboardEntry[]>([]);
  const [dailyEntries, setDailyEntries] = useState<DailyLeaderboardEntry[]>([]);
  const [loadingBoards, setLoadingBoards] = useState(false);

  const activeTheme = THEMES[progress.activeTheme] || THEMES['city-skyline'];
  const activeSkin =
    TILE_SKINS.find((s) => s.id === progress.activeSkin) || TILE_SKINS[0];

  const showToast = useCallback((msg: string) => {
    setToastMsg(msg);
  }, []);

  useEffect(() => {
    if (!toastMsg) return;
    const t = window.setTimeout(() => setToastMsg(null), 2600);
    return () => window.clearTimeout(t);
  }, [toastMsg]);

  // Persist progress locally whenever it changes
  useEffect(() => {
    saveLocalProgress(progress);
  }, [progress]);

  // Load leaderboards on boot
  const refreshLeaderboards = useCallback(async () => {
    setLoadingBoards(true);
    const res = await fetchLeaderboards();
    if (res) {
      setGlobalEntries(res.global);
      setDailyDateKey(res.daily.dateKey);
      setDailyEntries(res.daily.entries);
    }
    setLoadingBoards(false);
  }, []);

  useEffect(() => {
    refreshLeaderboards();
    void adMobManager.init();
    void initPlayGames();
    // Push any offline-earned achievements and score to Play Games on launch
    void syncProgress(
      progressRef.current.unlockedAchievements,
      progressRef.current.totalScore
    );
  }, [refreshLeaderboards]);

  // Timer tick while playing
  useEffect(() => {
    if (!hasStartedMoving || victoryResult || activeTab !== 'play') return;
    const timer = window.setInterval(() => {
      setElapsedSec((prev) => prev + 1);
    }, 1000);
    return () => window.clearInterval(timer);
  }, [hasStartedMoving, victoryResult, activeTab]);

  // Start / reset a puzzle board
  const startBoard = useCallback(
    (levelNum: number, daily: boolean, customDateKey?: string) => {
      const dateStr = customDateKey || dailyDateKey;
      const def = daily
        ? getDailyChallengeDefinition(dateStr)
        : getLevelDefinition(levelNum);
      const seed = daily ? `daily-${dateStr}` : `campaign-level-${levelNum}`;

      setIsDailyMode(daily);
      if (!daily) setActiveLevel(levelNum);
      setBoard(generateSolvableBoard(def.size, def.scrambleSteps, seed));
      setHistory([]);
      setMoves(0);
      setElapsedSec(0);
      setHasStartedMoving(false);
      setUsedPowerUpThisRun(false);
      setHintTileIdx(null);
      setSwapMode(false);
      setSwapFirstIdx(null);
      setVictoryResult(null);
      lastBlankRef.current = null;
      setActiveTab('play');
    },
    [dailyDateKey]
  );

  // Handle completing a puzzle
  const handlePuzzleSolved = useCallback(
    async (finalMoves: number, finalSec: number) => {
      triggerHaptic('win', progress.hapticsEnabled);
      playGameSound('win', progress.soundEnabled, activeTheme.soundProfile);

      const def = currentLevelDef;
      const starsEarned = calculateStars(finalMoves, def.targetMoves);
      const scoreEarned = calculateScore(
        def.level,
        def.size,
        finalMoves,
        def.targetMoves,
        finalSec,
        starsEarned
      );

      const prevStarsOnLevel = isDailyMode
        ? 0
        : progress.levelStars[def.level] || 0;
      const bonusStarsEarned = isDailyMode
        ? starsEarned
        : Math.max(0, starsEarned - prevStarsOnLevel);

      const hasDoubleCoins = progress.inventory.doubleCoinsTurns > 0;
      const coinsEarned = def.rewardCoins * (hasDoubleCoins ? 2 : 1);

      const isFirstClear =
        !isDailyMode && !progress.levelStars[def.level];
      const diamondsEarned =
        (isDailyMode
          ? def.rewardDiamonds
          : isFirstClear
            ? def.rewardDiamonds
            : starsEarned === 3 && prevStarsOnLevel < 3
              ? 3
              : 1) + (starsEarned === 3 ? 2 : 0);

      // Evaluate achievements
      const prospectiveProgress: PlayerProgress = {
        ...progress,
        currencies: {
          ...progress.currencies,
          stars: progress.currencies.stars + bonusStarsEarned,
        },
      };
      const newlyUnlockedAchievements = evaluateAchievements(
        prospectiveProgress,
        {
          level: isDailyMode ? 1 : def.level,
          size: def.size,
          starsEarned,
          isDaily: isDailyMode,
          usedPowerUp: usedPowerUpThisRun,
        }
      );

      const achievementDiamonds = newlyUnlockedAchievements.reduce(
        (acc, a) => acc + a.rewardDiamonds,
        0
      );
      const achievementXp = newlyUnlockedAchievements.reduce(
        (acc, a) => acc + a.xp,
        0
      );

      const prevSnapshot = progressRef.current;
      const nextLevelStars = { ...prevSnapshot.levelStars };
      const nextBestMoves = { ...prevSnapshot.levelBestMoves };

      if (!isDailyMode) {
        nextLevelStars[def.level] = Math.max(
          prevSnapshot.levelStars[def.level] || 0,
          starsEarned
        );
        const prevBest = prevSnapshot.levelBestMoves[def.level];
        nextBestMoves[def.level] =
          prevBest !== undefined ? Math.min(prevBest, finalMoves) : finalMoves;
      }

      const completedDailies = new Set(prevSnapshot.completedDailyDates);
      const isNewDailyToday =
        isDailyMode && !completedDailies.has(dailyDateKey);
      if (isDailyMode) completedDailies.add(dailyDateKey);

      const nextProgress: PlayerProgress = {
        ...prevSnapshot,
        xp: prevSnapshot.xp + 75 + starsEarned * 30 + achievementXp,
        highestLevelUnlocked: isDailyMode
          ? prevSnapshot.highestLevelUnlocked
          : Math.max(prevSnapshot.highestLevelUnlocked, def.level + 1),
        levelStars: nextLevelStars,
        levelBestMoves: nextBestMoves,
        totalScore: prevSnapshot.totalScore + scoreEarned,
        currencies: {
          stars: prevSnapshot.currencies.stars + bonusStarsEarned,
          coins: prevSnapshot.currencies.coins + coinsEarned,
          diamonds:
            prevSnapshot.currencies.diamonds +
            diamondsEarned +
            achievementDiamonds,
        },
        inventory: {
          ...prevSnapshot.inventory,
          doubleCoinsTurns: Math.max(
            0,
            prevSnapshot.inventory.doubleCoinsTurns - 1
          ),
        },
        unlockedAchievements: [
          ...new Set([
            ...prevSnapshot.unlockedAchievements,
            ...newlyUnlockedAchievements.map((a) => a.id),
          ]),
        ],
        dailyStreak: isNewDailyToday
          ? prevSnapshot.dailyStreak + 1
          : prevSnapshot.dailyStreak,
        completedDailyDates: Array.from(completedDailies),
      };

      progressRef.current = nextProgress;
      setProgress(nextProgress);

      setVictoryResult({
        level: def.level,
        isDaily: isDailyMode,
        moves: finalMoves,
        targetMoves: def.targetMoves,
        timeSec: finalSec,
        starsEarned,
        newBestStars: !isDailyMode && starsEarned > prevStarsOnLevel,
        coinsEarned,
        diamondsEarned,
        bonusStarsEarned,
        scoreEarned,
        newlyUnlockedAchievements,
      });

      // Background sync to Play Games & Global Leaderboard
      window.setTimeout(async () => {
        const globalRes = await submitGlobalScore(
          nextProgress,
          isDailyMode ? finalMoves : null
        );
        if (globalRes) {
          setGlobalEntries(globalRes.global);
        }
        if (isDailyMode) {
          const dailyRes = await submitDailyScore(
            nextProgress,
            finalMoves,
            finalSec,
            starsEarned
          );
          if (dailyRes) {
            setDailyEntries(dailyRes.daily.entries);
          }
        }
        const syncedAt = await syncProfileToCloud(nextProgress);
        if (syncedAt) {
          setProgress((p) => ({ ...p, lastSyncedAt: syncedAt }));
        }
      }, 80);
    },
    [
      progress,
      activeTheme.soundProfile,
      currentLevelDef,
      isDailyMode,
      usedPowerUpThisRun,
      dailyDateKey,
    ]
  );

  // Execute a validated slide result
  const applySlideResult = useCallback(
    (
      result: NonNullable<ReturnType<typeof slideTileAt>>,
      prevBoard: number[],
      prevMoves: number
    ) => {
      lastBlankRef.current = prevBoard.indexOf(0);
      setHistory((h) => [...h.slice(-30), { board: prevBoard, moves: prevMoves }]);
      const nextMoves = prevMoves + result.stepsMoved;
      setBoard(result.nextBoard);
      setMoves(nextMoves);
      setHasStartedMoving(true);
      setHintTileIdx(null);

      if (isSolved(result.nextBoard)) {
        handlePuzzleSolved(nextMoves, Math.max(1, elapsedSec));
      } else if (result.newlyLockedCount > 0) {
        triggerHaptic('lock', progress.hapticsEnabled);
        playGameSound('lock', progress.soundEnabled, activeTheme.soundProfile);
      } else {
        triggerHaptic('move', progress.hapticsEnabled);
        playGameSound('move', progress.soundEnabled, activeTheme.soundProfile);
      }
    },
    [
      elapsedSec,
      progress.hapticsEnabled,
      progress.soundEnabled,
      activeTheme.soundProfile,
      handlePuzzleSolved,
    ]
  );

  // Tile click handler (handles normal slide + Tile Swap power-up)
  const handleTileClick = useCallback(
    (idx: number) => {
      if (victoryResult) return;

      if (swapMode) {
        if (board[idx] === 0) return;
        if (swapFirstIdx === null) {
          setSwapFirstIdx(idx);
          triggerHaptic('button', progress.hapticsEnabled);
          return;
        }
        if (swapFirstIdx === idx) {
          setSwapFirstIdx(null);
          return;
        }
        // Perform swap
        const nextBoard = [...board];
        const temp = nextBoard[swapFirstIdx];
        nextBoard[swapFirstIdx] = nextBoard[idx];
        nextBoard[idx] = temp;

        setHistory((h) => [...h.slice(-30), { board, moves }]);
        setBoard(nextBoard);
        setSwapMode(false);
        setSwapFirstIdx(null);
        setUsedPowerUpThisRun(true);
        setHasStartedMoving(true);
        setProgress((prev) => ({
          ...prev,
          inventory: {
            ...prev.inventory,
            swap: Math.max(0, prev.inventory.swap - 1),
          },
        }));
        triggerHaptic('lock', progress.hapticsEnabled);
        playGameSound('unlock', progress.soundEnabled, activeTheme.soundProfile);

        if (isSolved(nextBoard)) {
          handlePuzzleSolved(moves, Math.max(1, elapsedSec));
        }
        return;
      }

      const result = slideTileAt(board, currentLevelDef.size, idx);
      if (!result) {
        setInvalidShakeIdx(idx);
        window.setTimeout(() => setInvalidShakeIdx(null), 240);
        triggerHaptic('error', progress.hapticsEnabled);
        playGameSound('error', progress.soundEnabled, activeTheme.soundProfile);
        return;
      }

      applySlideResult(result, board, moves);
    },
    [
      victoryResult,
      swapMode,
      board,
      swapFirstIdx,
      progress.hapticsEnabled,
      progress.soundEnabled,
      activeTheme.soundProfile,
      moves,
      elapsedSec,
      currentLevelDef.size,
      applySlideResult,
      handlePuzzleSolved,
    ]
  );

  const handleSwipeDirection = useCallback(
    (dir: 'UP' | 'DOWN' | 'LEFT' | 'RIGHT') => {
      if (victoryResult || swapMode) return;
      const result = slideByDirection(board, currentLevelDef.size, dir);
      if (!result) {
        triggerHaptic('error', progress.hapticsEnabled);
        return;
      }
      applySlideResult(result, board, moves);
    },
    [
      victoryResult,
      swapMode,
      board,
      currentLevelDef.size,
      progress.hapticsEnabled,
      applySlideResult,
      moves,
    ]
  );

  // Keyboard support (Arrow keys & WASD)
  useEffect(() => {
    if (activeTab !== 'play' || victoryResult || interstitialOpen || playGamesModalOpen) {
      return;
    }
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.target instanceof HTMLInputElement || e.target instanceof HTMLTextAreaElement) {
        return;
      }
      if (e.key === 'ArrowUp' || e.key === 'w' || e.key === 'W') {
        e.preventDefault();
        handleSwipeDirection('UP');
      } else if (e.key === 'ArrowDown' || e.key === 's' || e.key === 'S') {
        e.preventDefault();
        handleSwipeDirection('DOWN');
      } else if (e.key === 'ArrowLeft' || e.key === 'a' || e.key === 'A') {
        e.preventDefault();
        handleSwipeDirection('LEFT');
      } else if (e.key === 'ArrowRight' || e.key === 'd' || e.key === 'D') {
        e.preventDefault();
        handleSwipeDirection('RIGHT');
      }
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [
    activeTab,
    victoryResult,
    interstitialOpen,
    playGamesModalOpen,
    handleSwipeDirection,
  ]);

  // Power-Up Triggers
  const handleUseUndo = () => {
    if (history.length === 0 || victoryResult) return;
    if (progress.inventory.undo <= 0) {
      showToast('Out of Step Rewinds — visit the Store');
      return;
    }
    const last = history[history.length - 1];
    setHistory((h) => h.slice(0, -1));
    setBoard(last.board);
    setMoves(last.moves);
    setHintTileIdx(null);
    setUsedPowerUpThisRun(true);
    setProgress((prev) => ({
      ...prev,
      inventory: { ...prev.inventory, undo: prev.inventory.undo - 1 },
    }));
    triggerHaptic('button', progress.hapticsEnabled);
    playGameSound('move', progress.soundEnabled, activeTheme.soundProfile);
  };

  const handleUseHint = () => {
    if (victoryResult) return;
    if (progress.inventory.hint <= 0) {
      showToast('Out of Hint Magnets — visit the Store');
      return;
    }
    const bestIdx = findBestNextTileIndex(
      board,
      currentLevelDef.size,
      lastBlankRef.current
    );
    if (bestIdx === null) return;
    setHintTileIdx(bestIdx);
    setUsedPowerUpThisRun(true);
    setProgress((prev) => ({
      ...prev,
      inventory: { ...prev.inventory, hint: prev.inventory.hint - 1 },
    }));
    triggerHaptic('lock', progress.hapticsEnabled);
    playGameSound('coin', progress.soundEnabled, activeTheme.soundProfile);
  };

  const handleToggleSwap = () => {
    if (victoryResult) return;
    if (!swapMode && progress.inventory.swap <= 0) {
      showToast('Out of Tile Swaps — visit the Store');
      return;
    }
    setSwapMode((prev) => !prev);
    setSwapFirstIdx(null);
    triggerHaptic('button', progress.hapticsEnabled);
  };

  // Victory Next Level + AdMob 2-minute Interstitial Gate
  const handleVictoryNextLevel = async () => {
    const wasDaily = victoryResult?.isDaily;
    setVictoryResult(null);

    if (wasDaily) {
      startBoard(progress.highestLevelUnlocked, false);
      return;
    }

    const nextLvl = activeLevel + 1;
    if (canShowInterstitial(progress.adsRemoved)) {
      if (adMobManager.isNative()) {
        await adMobManager.showInterstitial(progress.adsRemoved, () => {
          startBoard(nextLvl, false);
        });
      } else {
        setPendingNextLevel(nextLvl);
        setInterstitialOpen(true);
      }
    } else {
      startBoard(nextLvl, false);
    }
  };

  const handleInterstitialComplete = () => {
    recordInterstitialShown();
    setInterstitialOpen(false);
    const targetLvl = pendingNextLevel ?? activeLevel + 1;
    setPendingNextLevel(null);
    startBoard(targetLvl, false);
  };

  // Store Actions
  const handleBuyPowerUp = (
    type: PowerUpType,
    currency: 'coins' | 'diamonds'
  ) => {
    const item = POWER_UPS.find((p) => p.id === type);
    if (!item) return;

    const cost = currency === 'coins' ? item.priceCoins : item.priceDiamonds;
    if (progress.currencies[currency] < cost) {
      showToast(`Not enough ${currency}`);
      return;
    }

    setProgress((prev) => ({
      ...prev,
      currencies: {
        ...prev.currencies,
        [currency]: prev.currencies[currency] - cost,
      },
      inventory: {
        ...prev.inventory,
        hint:
          type === 'hint'
            ? prev.inventory.hint + item.packAmount
            : prev.inventory.hint,
        undo:
          type === 'undo'
            ? prev.inventory.undo + item.packAmount
            : prev.inventory.undo,
        swap:
          type === 'swap'
            ? prev.inventory.swap + item.packAmount
            : prev.inventory.swap,
        doubleCoinsTurns:
          type === 'doubleCoins'
            ? prev.inventory.doubleCoinsTurns + item.packAmount
            : prev.inventory.doubleCoinsTurns,
      },
    }));
    triggerHaptic('lock', progress.hapticsEnabled);
    playGameSound('coin', progress.soundEnabled, activeTheme.soundProfile);
    showToast(`Added +${item.packAmount} ${item.name}`);
  };

  const handleUnlockOrEquipTheme = (themeId: ThemeId) => {
    const theme = THEMES[themeId];
    if (progress.unlockedThemes.includes(themeId)) {
      setProgress((prev) => ({ ...prev, activeTheme: themeId }));
      triggerHaptic('button', progress.hapticsEnabled);
      playGameSound('unlock', progress.soundEnabled, theme.soundProfile);
      showToast(`${theme.name} equipped`);
      return;
    }

    if (
      theme.requiredStars !== undefined &&
      progress.currencies.stars < theme.requiredStars
    ) {
      showToast(`Requires ${theme.requiredStars} Stars to unlock`);
      return;
    }
    if (progress.currencies.diamonds < theme.priceDiamonds) {
      showToast('Not enough Diamonds');
      return;
    }

    setProgress((prev) => {
      const nextUnlocked = [...prev.unlockedThemes, themeId];
      const shouldGrantThemeAch =
        nextUnlocked.length > 1 &&
        !prev.unlockedAchievements.includes('theme-collector');
      if (shouldGrantThemeAch) {
        void unlockAchievement('theme-collector');
      }
      return {
        ...prev,
        currencies: {
          ...prev.currencies,
          diamonds: prev.currencies.diamonds - theme.priceDiamonds,
        },
        unlockedThemes: nextUnlocked,
        activeTheme: themeId,
        unlockedAchievements: shouldGrantThemeAch
          ? [...prev.unlockedAchievements, 'theme-collector']
          : prev.unlockedAchievements,
      };
    });
    triggerHaptic('win', progress.hapticsEnabled);
    playGameSound('unlock', progress.soundEnabled, theme.soundProfile);
    showToast(`Unlocked & equipped ${theme.name}`);
  };

  const handleUnlockOrEquipSkin = (skinId: TileSkinId) => {
    const skin = TILE_SKINS.find((s) => s.id === skinId);
    if (!skin) return;

    if (progress.unlockedSkins.includes(skinId)) {
      setProgress((prev) => ({ ...prev, activeSkin: skinId }));
      triggerHaptic('button', progress.hapticsEnabled);
      showToast(`${skin.name} equipped`);
      return;
    }

    if (progress.currencies.diamonds < skin.priceDiamonds) {
      showToast('Not enough Diamonds');
      return;
    }

    setProgress((prev) => ({
      ...prev,
      currencies: {
        ...prev.currencies,
        diamonds: prev.currencies.diamonds - skin.priceDiamonds,
      },
      unlockedSkins: [...prev.unlockedSkins, skinId],
      activeSkin: skinId,
    }));
    triggerHaptic('lock', progress.hapticsEnabled);
    playGameSound('unlock', progress.soundEnabled, activeTheme.soundProfile);
    showToast(`Unlocked ${skin.name}`);
  };

  // Rewarded Ad Handlers
  const grantDiamonds = useCallback(
    (amount: number) => {
      setProgress((prev) => ({
        ...prev,
        currencies: {
          ...prev.currencies,
          diamonds: prev.currencies.diamonds + amount,
        },
      }));
      playGameSound('coin', progress.soundEnabled, activeTheme.soundProfile);
      showToast(`+${amount} Diamonds claimed`);
    },
    [progress.soundEnabled, activeTheme.soundProfile, showToast]
  );

  const handleRewardedDoubleVictory = (): Promise<void> => {
    return new Promise((resolve) => {
      if (!victoryResult) {
        resolve();
        return;
      }
      const bonus = victoryResult.diamondsEarned;
      if (adMobManager.isNative()) {
        void adMobManager.showRewarded(
          () => {
            grantDiamonds(bonus);
            resolve();
          },
          () => {
            showToast('Rewarded video still loading — try again in a moment');
            resolve();
          }
        );
        return;
      }
      rewardedResolveRef.current = resolve;
      setRewardedAdContext({
        type: 'victory',
        amount: bonus,
      });
    });
  };

  const handleStoreRewardedDiamonds = () => {
    if (adMobManager.isNative()) {
      void adMobManager.showRewarded(
        () => grantDiamonds(15),
        () => showToast('Rewarded video still loading — try again in a moment')
      );
      return;
    }
    setRewardedAdContext({ type: 'store' });
  };

  const handleClaimRewardedAd = () => {
    if (!rewardedAdContext) return;
    const amount =
      rewardedAdContext.type === 'store' ? 15 : rewardedAdContext.amount;

    grantDiamonds(amount);
    setRewardedAdContext(null);
    if (rewardedResolveRef.current) {
      rewardedResolveRef.current();
      rewardedResolveRef.current = null;
    }
  };

  // Play Games Manual Cloud Sync
  const handleManualSync = async () => {
    setSyncingProfile(true);
    const syncedAt = await syncProfileToCloud(progress);
    await submitGlobalScore(progress);
    await refreshLeaderboards();
    if (syncedAt) {
      setProgress((p) => ({ ...p, lastSyncedAt: syncedAt }));
      showToast('Play Games profile synced');
    }
    setSyncingProfile(false);
  };

  // Live Pace Metrics
  const twoStarCap = Math.ceil(currentLevelDef.targetMoves * 1.35);
  const currentPaceStars = calculateStars(moves, currentLevelDef.targetMoves);
  const totalPlayableTiles = currentLevelDef.size * currentLevelDef.size - 1;
  const lockedTiles = countCorrectTiles(board);
  const pacePercent = Math.min(
    100,
    Math.round((moves / Math.max(1, twoStarCap + 6)) * 100)
  );
  const playerRankIdx = globalEntries.findIndex(
    (e) => e.id === progress.gamerId
  );
  const xpStats = getPlayerLevelFromXp(progress.xp);

  const navItems: { id: NavTab; label: string; icon: React.ReactNode }[] = [
    { id: 'play', label: 'Play', icon: <Grid3X3 className="w-4 h-4" /> },
    { id: 'daily', label: 'Daily', icon: <Calendar className="w-4 h-4" /> },
    { id: 'levels', label: 'Levels', icon: <LayersIcon /> },
    { id: 'store', label: 'Store', icon: <Store className="w-4 h-4" /> },
    { id: 'leaderboard', label: 'Ranks', icon: <Trophy className="w-4 h-4" /> },
  ];

  return (
    <div
      className={`relative min-h-screen flex flex-col transition-colors duration-300 ${activeTheme.canvasBg} text-slate-100`}
    >
      {/* Full-Bleed Active Theme Backdrop (City Skyline / Neon Cyberpunk / etc.) */}
      <div
        aria-hidden="true"
        className="fixed inset-0 pointer-events-none z-0 overflow-hidden"
        style={{ background: activeTheme.fallbackGradient }}
      >
        {activeTheme.imageUrl && (
          <img
            src={activeTheme.imageUrl}
            alt=""
            referrerPolicy="no-referrer"
            className="w-full h-full object-cover opacity-45"
          />
        )}
        <div className="absolute inset-0 bg-gradient-to-b from-slate-950/65 via-slate-950/50 to-slate-950/90" />
      </div>

      {/* 3-Zone Top Bar (Compact 52px sticky header) */}
      <header className="sticky top-0 z-30 h-[52px] bg-slate-950/80 backdrop-blur-md border-b border-slate-800/80">
        <div className="max-w-6xl mx-auto h-full px-3 sm:px-6 flex items-center justify-between gap-4 sm:gap-8">
          {/* Zone 1: Single-line Brand Wordmark */}
          <button
            type="button"
            onClick={() => setActiveTab('play')}
            className="text-lg font-display font-extrabold tracking-tight text-white whitespace-nowrap shrink-0 cursor-pointer"
          >
            Slide<span className={activeTheme.accentText}>Pulse</span>
          </button>

          {/* Zone 2: Desktop Single-Line Navigation */}
          <nav
            aria-label="Primary navigation"
            className="hidden md:flex items-center gap-6 text-sm font-medium"
          >
            {navItems.map((item) => {
              const active = activeTab === item.id;
              return (
                <button
                  key={item.id}
                  type="button"
                  onClick={() => setActiveTab(item.id)}
                  className={`py-1 whitespace-nowrap shrink-0 transition-colors cursor-pointer border-b-2 ${
                    active
                      ? 'text-white border-amber-400'
                      : 'text-slate-400 border-transparent hover:text-slate-200'
                  }`}
                >
                  {item.label}
                </button>
              );
            })}
          </nav>

          {/* Zone 3: Wallet + Play Games Profile Action */}
          <div className="flex items-center gap-3 shrink-0">
            <div className="hidden sm:flex items-center gap-3 text-xs font-mono font-semibold tabular-nums text-slate-200">
              <span className="flex items-center gap-1 text-amber-300">
                <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
                {progress.currencies.stars}
              </span>
              <span className="flex items-center gap-1 text-cyan-300">
                <Gem className="w-3.5 h-3.5" />
                {progress.currencies.diamonds}
              </span>
              <span className="flex items-center gap-1 text-yellow-300">
                <Coins className="w-3.5 h-3.5" />
                {progress.currencies.coins}
              </span>
            </div>

            <button
              type="button"
              onClick={() => setPlayGamesModalOpen(true)}
              className="inline-flex items-center gap-2 px-3 py-1.5 rounded-xl bg-slate-900 hover:bg-slate-800 border border-slate-700/80 text-xs font-semibold text-white whitespace-nowrap shrink-0 cursor-pointer"
            >
              <Gamepad2 className="w-4 h-4 text-emerald-400" />
              <span className="max-w-[92px] truncate">{progress.gamerTag}</span>
            </button>
          </div>
        </div>
      </header>

      {/* Mobile Wallet + Inline Nav Strip (Not sticky, keeps sticky chrome <= 13%!) */}
      <div className="md:hidden border-b border-slate-800/80 bg-slate-950/75 px-3 py-2 space-y-2">
        <div className="flex items-center justify-between text-xs font-mono font-semibold tabular-nums">
          <div className="flex items-center gap-4">
            <span className="flex items-center gap-1 text-amber-300">
              <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
              {progress.currencies.stars}
            </span>
            <span className="flex items-center gap-1 text-cyan-300">
              <Gem className="w-3.5 h-3.5" />
              {progress.currencies.diamonds}
            </span>
            <span className="flex items-center gap-1 text-yellow-300">
              <Coins className="w-3.5 h-3.5" />
              {progress.currencies.coins}
            </span>
          </div>
          <span className="text-[11px] font-sans text-slate-400">
            {activeTheme.name}
          </span>
        </div>

        <nav
          aria-label="Mobile navigation"
          className="grid grid-cols-5 gap-1 bg-slate-900/90 p-1 rounded-xl border border-slate-800"
        >
          {navItems.map((item) => {
            const active = activeTab === item.id;
            return (
              <button
                key={item.id}
                type="button"
                onClick={() => setActiveTab(item.id)}
                className={`py-2 px-1 rounded-lg text-[11px] font-semibold flex flex-col items-center justify-center gap-0.5 transition-colors cursor-pointer whitespace-nowrap ${
                  active
                    ? 'bg-slate-800 text-white'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                {item.icon}
                <span>{item.label}</span>
              </button>
            );
          })}
        </nav>
      </div>

      {/* Main Content Container (with bottom clearance for always-on AdMob Banner) */}
      <main className="relative z-10 flex-1 max-w-6xl w-full mx-auto px-3 sm:px-6 pt-4 sm:pt-6 pb-20">
        {activeTab === 'play' && (
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 lg:gap-8 items-start">
            {/* Primary Left/Center Puzzle Arena */}
            <div className="lg:col-span-7 space-y-4">
              {/* Level Header & Target Moves HUD */}
              <div className="rounded-2xl bg-slate-900/80 border border-slate-800 p-3.5 sm:p-4 space-y-3">
                <div className="flex items-center justify-between gap-3">
                  <div className="min-w-0">
                    <div className="flex items-center gap-2 text-xs text-slate-400">
                      <span>
                        {isDailyMode
                          ? 'Daily Challenge'
                          : `Level ${currentLevelDef.level}`}
                      </span>
                      <span aria-hidden="true">·</span>
                      <span>{currentLevelDef.tierLabel}</span>
                    </div>
                    <h1 className="text-lg sm:text-xl font-display font-bold text-white truncate">
                      {currentLevelDef.title}
                    </h1>
                  </div>

                  <div className="flex items-center gap-2 shrink-0">
                    <button
                      type="button"
                      aria-label={
                        progress.soundEnabled ? 'Mute sound' : 'Enable sound'
                      }
                      onClick={() =>
                        setProgress((p) => ({
                          ...p,
                          soundEnabled: !p.soundEnabled,
                        }))
                      }
                      className="w-10 h-10 rounded-xl bg-slate-800/80 hover:bg-slate-700 border border-slate-700/70 flex items-center justify-center text-slate-300 cursor-pointer"
                    >
                      {progress.soundEnabled ? (
                        <Volume2 className="w-4 h-4" />
                      ) : (
                        <VolumeX className="w-4 h-4 text-slate-500" />
                      )}
                    </button>
                    <div className="px-3 py-1.5 rounded-xl bg-slate-950/90 border border-slate-800 text-right tabular-nums">
                      <p className="text-[10px] text-slate-400">Time</p>
                      <p className="text-sm font-mono font-bold text-white">
                        {Math.floor(elapsedSec / 60)}:
                        {(elapsedSec % 60).toString().padStart(2, '0')}
                      </p>
                    </div>
                  </div>
                </div>

                {/* Target Moves & Live 3-Star Pace Bar */}
                <div className="space-y-1.5">
                  <div className="flex items-center justify-between text-xs tabular-nums">
                    <div className="flex items-center gap-2">
                      <span className="font-mono font-bold text-white">
                        Moves: {moves}
                      </span>
                      <span className="text-slate-500">/</span>
                      <span className="text-slate-300">
                        Target: {currentLevelDef.targetMoves}
                      </span>
                    </div>
                    <div className="flex items-center gap-1.5">
                      {[1, 2, 3].map((s) => (
                        <Star
                          key={s}
                          className={`w-3.5 h-3.5 ${
                            s <= currentPaceStars
                              ? 'fill-amber-400 text-amber-400'
                              : 'text-slate-700'
                          }`}
                        />
                      ))}
                      <span className="text-slate-300 font-medium ml-1">
                        {currentPaceStars}★ Pace
                      </span>
                    </div>
                  </div>

                  <div className="h-1.5 rounded-full bg-slate-950 overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all duration-200 ${
                        currentPaceStars === 3
                          ? 'bg-emerald-400'
                          : currentPaceStars === 2
                            ? 'bg-amber-400'
                            : 'bg-rose-400'
                      }`}
                      style={{ width: `${Math.max(4, pacePercent)}%` }}
                    />
                  </div>
                </div>
              </div>

              {/* Swap Mode Guidance Strip */}
              {swapMode && (
                <div className="rounded-xl bg-emerald-500/15 border border-emerald-400/40 px-3.5 py-2 flex items-center justify-between text-xs text-emerald-200">
                  <span>
                    {swapFirstIdx === null
                      ? 'Tile Swap Active: Tap the first tile to swap'
                      : 'Now tap the second tile to complete the swap'}
                  </span>
                  <button
                    type="button"
                    onClick={() => {
                      setSwapMode(false);
                      setSwapFirstIdx(null);
                    }}
                    className="font-semibold underline ml-2 cursor-pointer"
                  >
                    Cancel
                  </button>
                </div>
              )}

              {/* Tactile Animated Puzzle Board */}
              <PuzzleBoard
                board={board}
                size={currentLevelDef.size}
                theme={activeTheme}
                skin={activeSkin}
                hintTileIdx={hintTileIdx}
                swapMode={swapMode}
                swapFirstIdx={swapFirstIdx}
                invalidShakeIdx={invalidShakeIdx}
                disabled={Boolean(victoryResult)}
                onTileClick={handleTileClick}
                onSwipeDirection={handleSwipeDirection}
              />

              {/* Ergonomic Thumb-Zone Action Dock */}
              <div className="max-w-[420px] mx-auto grid grid-cols-4 gap-2">
                <button
                  type="button"
                  onClick={handleUseUndo}
                  disabled={history.length === 0}
                  className="min-h-[48px] rounded-2xl bg-slate-900 hover:bg-slate-800 disabled:opacity-45 border border-slate-800 px-2 py-2 flex flex-col items-center justify-center gap-0.5 text-slate-200 transition-transform active:scale-95 cursor-pointer"
                >
                  <Undo2 className="w-4 h-4 text-slate-300" />
                  <span className="text-[11px] font-semibold tabular-nums">
                    Undo ({progress.inventory.undo})
                  </span>
                </button>

                <button
                  type="button"
                  onClick={handleUseHint}
                  className="min-h-[48px] rounded-2xl bg-slate-900 hover:bg-slate-800 border border-slate-800 px-2 py-2 flex flex-col items-center justify-center gap-0.5 text-slate-200 transition-transform active:scale-95 cursor-pointer"
                >
                  <Lightbulb className="w-4 h-4 text-amber-400" />
                  <span className="text-[11px] font-semibold tabular-nums">
                    Hint ({progress.inventory.hint})
                  </span>
                </button>

                <button
                  type="button"
                  onClick={handleToggleSwap}
                  className={`min-h-[48px] rounded-2xl border px-2 py-2 flex flex-col items-center justify-center gap-0.5 transition-transform active:scale-95 cursor-pointer ${
                    swapMode
                      ? 'bg-emerald-500/20 border-emerald-400 text-emerald-200'
                      : 'bg-slate-900 hover:bg-slate-800 border-slate-800 text-slate-200'
                  }`}
                >
                  <Shuffle className="w-4 h-4 text-cyan-400" />
                  <span className="text-[11px] font-semibold tabular-nums">
                    Swap ({progress.inventory.swap})
                  </span>
                </button>

                <button
                  type="button"
                  onClick={() => startBoard(activeLevel, isDailyMode)}
                  className="min-h-[48px] rounded-2xl bg-slate-900 hover:bg-slate-800 border border-slate-800 px-2 py-2 flex flex-col items-center justify-center gap-0.5 text-slate-200 transition-transform active:scale-95 cursor-pointer"
                >
                  <RotateCcw className="w-4 h-4 text-slate-300" />
                  <span className="text-[11px] font-semibold">Reset</span>
                </button>
              </div>

              <p className="text-center text-xs text-slate-500 tabular-nums">
                {lockedTiles} / {totalPlayableTiles} tiles locked · Tap any tile in the blank’s row/column or swipe
              </p>
            </div>

            {/* Right Desktop Companion Panel (Visible cleanly below board on mobile, right column on desktop) */}
            <aside className="lg:col-span-5 space-y-4">
              {/* Active Theme & Quick Theme Switcher */}
              <div className="rounded-2xl bg-slate-900/80 border border-slate-800 p-4 space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Palette className="w-4 h-4 text-amber-400" />
                    <h2 className="text-sm font-display font-bold text-white">
                      Active Board Theme
                    </h2>
                  </div>
                  <button
                    type="button"
                    onClick={() => setActiveTab('store')}
                    className="text-xs font-semibold text-cyan-300 hover:text-cyan-200 cursor-pointer"
                  >
                    Browse Store
                  </button>
                </div>

                <div className="flex items-center gap-3">
                  {activeTheme.imageUrl && (
                    <div
                      className="w-16 h-12 rounded-xl overflow-hidden border border-white/15 shrink-0"
                      style={{ background: activeTheme.fallbackGradient }}
                    >
                      <img
                        src={activeTheme.imageUrl}
                        alt={activeTheme.name}
                        referrerPolicy="no-referrer"
                        className="w-full h-full object-cover"
                      />
                    </div>
                  )}
                  <div className="min-w-0">
                    <p className="text-sm font-semibold text-white truncate">
                      {activeTheme.name}
                    </p>
                    <p className="text-xs text-slate-400 truncate">
                      {activeTheme.subtitle} · {activeSkin.name}
                    </p>
                  </div>
                </div>

                {progress.unlockedThemes.length > 1 && (
                  <div className="flex flex-wrap gap-1.5 pt-1">
                    {progress.unlockedThemes.map((tId) => {
                      const t = THEMES[tId];
                      const selected = tId === progress.activeTheme;
                      return (
                        <button
                          key={tId}
                          type="button"
                          onClick={() => handleUnlockOrEquipTheme(tId)}
                          className={`px-2.5 py-1 rounded-lg text-xs font-medium cursor-pointer transition-colors ${
                            selected
                              ? 'bg-amber-400 text-slate-950 font-semibold'
                              : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
                          }`}
                        >
                          {t.name}
                        </button>
                      );
                    })}
                  </div>
                )}

                {progress.inventory.doubleCoinsTurns > 0 && (
                  <div className="rounded-xl bg-amber-500/10 border border-amber-400/25 px-3 py-2 flex items-center gap-2 text-xs text-amber-300 tabular-nums">
                    <Sparkles className="w-3.5 h-3.5 shrink-0" />
                    <span>
                      2× Coin Booster active for {progress.inventory.doubleCoinsTurns} more level(s)
                    </span>
                  </div>
                )}
              </div>

              {/* Daily Challenge Quick Launch */}
              <div className="rounded-2xl bg-slate-900/80 border border-slate-800 p-4 flex items-center justify-between gap-3">
                <div className="space-y-1">
                  <div className="flex items-center gap-1.5 text-xs text-cyan-300 font-medium">
                    <Flame className="w-3.5 h-3.5 text-orange-400" />
                    <span>Daily Challenge · {progress.dailyStreak}d streak</span>
                  </div>
                  <p className="text-sm font-display font-bold text-white">
                    {dailyDateKey} Global Seed
                  </p>
                  <p className="text-xs text-slate-400">
                    {progress.completedDailyDates.includes(dailyDateKey)
                      ? 'Cleared today · Replay for fewer moves'
                      : 'Earn +18 Diamonds & global daily rank'}
                  </p>
                </div>
                <button
                  type="button"
                  onClick={() => startBoard(0, true, dailyDateKey)}
                  className="px-3.5 py-2 rounded-xl bg-cyan-400 hover:bg-cyan-300 text-slate-950 text-xs font-bold whitespace-nowrap shrink-0 cursor-pointer"
                >
                  {isDailyMode ? 'Restart' : 'Play Daily'}
                </button>
              </div>

              {/* Play Games & Global Top 5 Snapshot */}
              <div className="rounded-2xl bg-slate-900/80 border border-slate-800 p-4 space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Globe className="w-4 h-4 text-emerald-400" />
                    <h2 className="text-sm font-display font-bold text-white">
                      Global Top 5
                    </h2>
                  </div>
                  <button
                    type="button"
                    onClick={() => setActiveTab('leaderboard')}
                    className="text-xs font-semibold text-amber-300 hover:text-amber-200 cursor-pointer"
                  >
                    Full Ranks
                  </button>
                </div>

                <div className="divide-y divide-slate-800/70">
                  {globalEntries.slice(0, 5).map((entry, idx) => (
                    <div
                      key={entry.id}
                      className="py-2 flex items-center justify-between text-xs"
                    >
                      <div className="flex items-center gap-2 min-w-0">
                        <span className="w-5 font-mono font-bold text-slate-400 tabular-nums">
                          #{idx + 1}
                        </span>
                        <span className="font-medium text-white truncate">
                          {entry.gamerTag}
                        </span>
                        {entry.playGamesVerified && (
                          <ShieldCheck className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
                        )}
                      </div>
                      <div className="flex items-center gap-3 font-mono tabular-nums shrink-0">
                        <span className="text-amber-300">{entry.totalStars}★</span>
                        <span className="text-slate-200 font-semibold">
                          {entry.bestScore.toLocaleString()}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>

                <div className="pt-1 flex items-center justify-between text-[11px] text-slate-400 border-t border-slate-800/80 tabular-nums">
                  <span>
                    Play Games Lv.{xpStats.level} ·{' '}
                    {playerRankIdx >= 0
                      ? `Global Rank #${playerRankIdx + 1}`
                      : 'Complete a level to rank'}
                  </span>
                  <button
                    type="button"
                    onClick={() => setPlayGamesModalOpen(true)}
                    className="text-emerald-300 hover:text-emerald-200 font-medium cursor-pointer"
                  >
                    Profile
                  </button>
                </div>
              </div>
            </aside>
          </div>
        )}

        {activeTab === 'daily' && (
          <DailyChallengeView
            dateKey={dailyDateKey}
            progress={progress}
            dailyEntries={dailyEntries}
            onStartDaily={() => startBoard(0, true, dailyDateKey)}
          />
        )}

        {activeTab === 'levels' && (
          <LevelSelectView
            progress={progress}
            activeLevel={activeLevel}
            onSelectLevel={(lvl) => startBoard(lvl, false)}
          />
        )}

        {activeTab === 'store' && (
          <StoreView
            progress={progress}
            onBuyPowerUp={handleBuyPowerUp}
            onUnlockOrEquipTheme={handleUnlockOrEquipTheme}
            onUnlockOrEquipSkin={handleUnlockOrEquipSkin}
            onWatchRewardedDiamonds={handleStoreRewardedDiamonds}
          />
        )}

        {activeTab === 'leaderboard' && (
          <LeaderboardView
            globalEntries={globalEntries}
            dailyDateKey={dailyDateKey}
            dailyEntries={dailyEntries}
            progress={progress}
            loading={loadingBoards}
            onRefresh={refreshLeaderboards}
          />
        )}
      </main>

      {/* Always-Visible AdMob Banner on Every Level & View (native SDK banner on Android, web creative bar in preview) */}
      {!adMobManager.isNative() && (
        <div className="fixed bottom-0 inset-x-0 z-30">
          <AdMobBanner
            levelContext={activeLevel}
            onOpenStoreRemoveAds={() => setPlayGamesModalOpen(true)}
          />
        </div>
      )}

      {/* Transient Non-Blocking Toast */}
      {toastMsg && (
        <div
          role="status"
          className="fixed bottom-16 left-1/2 -translate-x-1/2 z-50 px-4 py-2 rounded-xl bg-slate-900/95 border border-slate-700 text-xs font-semibold text-white shadow-xl backdrop-blur-md whitespace-nowrap"
        >
          {toastMsg}
        </div>
      )}

      {/* Celebratory Level Complete Modal */}
      <VictoryModal
        result={victoryResult}
        theme={activeTheme}
        interstitialPending={canShowInterstitial(progress.adsRemoved)}
        onNextLevel={handleVictoryNextLevel}
        onReplay={() => startBoard(activeLevel, isDailyMode)}
        onWatchRewardedDouble={handleRewardedDoubleVictory}
      />

      {/* AdMob Interstitial (2-Minute Cooldown Enforced) */}
      <AdMobInterstitialModal
        open={interstitialOpen}
        nextLevelLabel={`Level ${pendingNextLevel ?? activeLevel + 1}`}
        onComplete={handleInterstitialComplete}
      />

      {/* AdMob Rewarded Video */}
      <AdMobRewardedModal
        open={Boolean(rewardedAdContext)}
        rewardLabel={
          rewardedAdContext?.type === 'store'
            ? '+15 Diamonds'
            : `+${rewardedAdContext?.amount ?? 0} Bonus Diamonds`
        }
        onClaim={handleClaimRewardedAd}
        onCancel={() => {
          setRewardedAdContext(null);
          if (rewardedResolveRef.current) {
            rewardedResolveRef.current();
            rewardedResolveRef.current = null;
          }
        }}
      />

      {/* Google Play Games Profile, Achievements & Settings */}
      <PlayGamesModal
        open={playGamesModalOpen}
        progress={progress}
        syncing={syncingProfile}
        onClose={() => setPlayGamesModalOpen(false)}
        onUpdateProfile={(patch) => {
          const updated = { ...progressRef.current, ...patch };
          progressRef.current = updated;
          setProgress(updated);
          if (updated.totalScore > 0) {
            void submitGlobalScore(updated).then((res) => {
              if (res) setGlobalEntries(res.global);
            });
          }
        }}
        onManualSync={handleManualSync}
      />
    </div>
  );
}

function LayersIcon() {
  return (
    <svg
      className="w-4 h-4"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <polygon points="12 2 2 7 12 12 22 7 12 2" />
      <polyline points="2 17 12 22 22 17" />
      <polyline points="2 12 12 17 22 12" />
    </svg>
  );
}
