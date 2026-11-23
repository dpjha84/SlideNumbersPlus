import React, { useState } from 'react';
import {
  Check,
  Coins,
  Gem,
  Lock,
  Play,
  Sparkles,
  Star,
  Zap,
} from 'lucide-react';
import { POWER_UPS, THEME_LIST, TILE_SKINS } from '../data/themes';
import {
  PlayerProgress,
  PowerUpType,
  ThemeId,
  TileSkinId,
} from '../types/game';

interface StoreViewProps {
  progress: PlayerProgress;
  onBuyPowerUp: (type: PowerUpType, currency: 'coins' | 'diamonds') => void;
  onUnlockOrEquipTheme: (themeId: ThemeId) => void;
  onUnlockOrEquipSkin: (skinId: TileSkinId) => void;
  onWatchRewardedDiamonds: () => void;
}

export const StoreView: React.FC<StoreViewProps> = ({
  progress,
  onBuyPowerUp,
  onUnlockOrEquipTheme,
  onUnlockOrEquipSkin,
  onWatchRewardedDiamonds,
}) => {
  const [subTab, setSubTab] = useState<'themes' | 'powerups'>('themes');
  const [failedImages, setFailedImages] = useState<Record<string, boolean>>({});

  return (
    <section className="space-y-6">
      {/* Store Header & Segmented Switch */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-display font-bold text-white">
            Workshop Store
          </h2>
          <p className="text-sm text-slate-400">
            Customize your board aesthetic or stock tactical power-ups.
          </p>
        </div>

        <div className="flex items-center gap-1 p-1 rounded-xl bg-slate-900 border border-slate-800 self-start">
          <button
            type="button"
            onClick={() => setSubTab('themes')}
            className={`px-4 py-2 rounded-lg text-xs font-semibold transition-colors whitespace-nowrap cursor-pointer ${
              subTab === 'themes'
                ? 'bg-slate-800 text-white shadow-sm'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            Themes & Skins
          </button>
          <button
            type="button"
            onClick={() => setSubTab('powerups')}
            className={`px-4 py-2 rounded-lg text-xs font-semibold transition-colors whitespace-nowrap cursor-pointer ${
              subTab === 'powerups'
                ? 'bg-slate-800 text-white shadow-sm'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            Power-Ups
          </button>
        </div>
      </div>

      {/* Rewarded Ad Diamond Top-Up Strip */}
      <div className="rounded-2xl bg-slate-900/90 border border-cyan-500/25 p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-cyan-500/15 border border-cyan-400/30 flex items-center justify-center shrink-0">
            <Gem className="w-5 h-5 text-cyan-300" />
          </div>
          <div>
            <p className="text-sm font-semibold text-white">
              Need more Diamonds for Themes?
            </p>
            <p className="text-xs text-slate-400">
              Watch a short AdMob sponsor clip for +15 Diamonds instantly.
            </p>
          </div>
        </div>
        <button
          type="button"
          onClick={onWatchRewardedDiamonds}
          className="px-4 py-2.5 rounded-xl bg-cyan-400 hover:bg-cyan-300 text-slate-950 text-xs font-bold flex items-center justify-center gap-1.5 whitespace-nowrap shrink-0 cursor-pointer transition-transform active:scale-95"
        >
          <Play className="w-3.5 h-3.5 fill-current" />
          <span>Watch Ad · +15 ◆</span>
        </button>
      </div>

      {subTab === 'themes' ? (
        <div className="space-y-8">
          {/* Board Themes */}
          <div>
            <h3 className="text-base font-display font-semibold text-slate-200 mb-3">
              Board Themes
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {THEME_LIST.map((theme) => {
                const isUnlocked = progress.unlockedThemes.includes(theme.id);
                const isActive = progress.activeTheme === theme.id;
                const lacksStars =
                  theme.requiredStars !== undefined &&
                  progress.currencies.stars < theme.requiredStars &&
                  !isUnlocked;
                const canAfford =
                  progress.currencies.diamonds >= theme.priceDiamonds && !lacksStars;

                return (
                  <div
                    key={theme.id}
                    className={`rounded-2xl overflow-hidden border transition-all flex flex-col ${
                      isActive
                        ? 'bg-slate-900 border-amber-400/60 shadow-lg shadow-amber-500/10'
                        : 'bg-slate-900/75 border-slate-800 hover:border-slate-700'
                    }`}
                  >
                    {/* Theme Preview Banner */}
                    <div
                      className="relative h-36 w-full overflow-hidden"
                      style={{ background: theme.fallbackGradient }}
                    >
                      {theme.imageUrl && !failedImages[theme.id] && (
                        <img
                          src={theme.imageUrl}
                          alt={`${theme.name} theme preview`}
                          referrerPolicy="no-referrer"
                          onError={() =>
                            setFailedImages((prev) => ({ ...prev, [theme.id]: true }))
                          }
                          className="w-full h-full object-cover"
                        />
                      )}
                      <div className="absolute inset-0 bg-gradient-to-t from-slate-950/90 via-slate-950/35 to-transparent" />

                      {/* Mini 2x2 Tile Preview */}
                      <div className="absolute bottom-3 right-3 grid grid-cols-2 gap-1.5 p-1.5 rounded-xl bg-black/55 backdrop-blur-sm border border-white/10">
                        <div
                          className={`w-7 h-7 rounded-md flex items-center justify-center text-xs font-display font-bold ${theme.tileSolved} ${theme.tileSolvedText}`}
                        >
                          1
                        </div>
                        <div
                          className={`w-7 h-7 rounded-md flex items-center justify-center text-xs font-display font-bold ${theme.tileBase} ${theme.tileText}`}
                        >
                          2
                        </div>
                        <div
                          className={`w-7 h-7 rounded-md flex items-center justify-center text-xs font-display font-bold ${theme.tileBase} ${theme.tileText}`}
                        >
                          3
                        </div>
                        <div className="w-7 h-7 rounded-md bg-black/40 border border-white/10" />
                      </div>

                      <div className="absolute bottom-3 left-3.5 right-24">
                        <p className="text-base font-display font-bold text-white truncate">
                          {theme.name}
                        </p>
                        <p className="text-xs text-slate-300 truncate">
                          {theme.subtitle}
                        </p>
                      </div>
                    </div>

                    {/* Action Bar */}
                    <div className="p-3.5 flex items-center justify-between gap-2 mt-auto">
                      <div className="text-xs text-slate-400 tabular-nums">
                        {isActive ? (
                          <span className="text-amber-400 font-semibold">
                            Equipped
                          </span>
                        ) : isUnlocked ? (
                          <span>Unlocked</span>
                        ) : theme.requiredStars ? (
                          <span>
                            {theme.priceDiamonds} ◆ · {theme.requiredStars} ★ req
                          </span>
                        ) : (
                          <span>{theme.priceDiamonds} Diamonds</span>
                        )}
                      </div>

                      <button
                        type="button"
                        disabled={!isUnlocked && !canAfford}
                        onClick={() => onUnlockOrEquipTheme(theme.id)}
                        className={`px-3.5 py-2 rounded-xl text-xs font-semibold flex items-center gap-1.5 transition-all whitespace-nowrap cursor-pointer ${
                          isActive
                            ? 'bg-amber-500/15 text-amber-300 border border-amber-400/40'
                            : isUnlocked
                              ? 'bg-slate-800 hover:bg-slate-700 text-white'
                              : canAfford
                                ? 'bg-cyan-400 hover:bg-cyan-300 text-slate-950'
                                : 'bg-slate-800/70 text-slate-500 cursor-not-allowed'
                        }`}
                      >
                        {isActive ? (
                          <>
                            <Check className="w-3.5 h-3.5" />
                            <span>Active</span>
                          </>
                        ) : isUnlocked ? (
                          <span>Equip</span>
                        ) : lacksStars ? (
                          <>
                            <Star className="w-3.5 h-3.5" />
                            <span>Need {theme.requiredStars}★</span>
                          </>
                        ) : (
                          <>
                            {!canAfford ? (
                              <Lock className="w-3.5 h-3.5" />
                            ) : (
                              <Gem className="w-3.5 h-3.5" />
                            )}
                            <span>Unlock · {theme.priceDiamonds}</span>
                          </>
                        )}
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Tile Geometry Skins */}
          <div>
            <h3 className="text-base font-display font-semibold text-slate-200 mb-3">
              Tile Edge Skins
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {TILE_SKINS.map((skin) => {
                const isUnlocked = progress.unlockedSkins.includes(skin.id);
                const isActive = progress.activeSkin === skin.id;
                const canAfford =
                  progress.currencies.diamonds >= skin.priceDiamonds;

                return (
                  <div
                    key={skin.id}
                    className={`rounded-2xl p-4 border flex flex-col justify-between gap-4 ${
                      isActive
                        ? 'bg-slate-900 border-amber-400/50'
                        : 'bg-slate-900/70 border-slate-800'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <p className="text-sm font-display font-bold text-white">
                          {skin.name}
                        </p>
                        <p className="text-xs text-slate-400 mt-1">
                          {skin.description}
                        </p>
                      </div>
                      <div
                        className={`w-10 h-10 shrink-0 bg-gradient-to-b from-slate-700 to-slate-800 border border-slate-500/50 flex items-center justify-center font-display font-bold text-sm text-white ${skin.tileClassModifier}`}
                      >
                        8
                      </div>
                    </div>

                    <button
                      type="button"
                      disabled={!isUnlocked && !canAfford}
                      onClick={() => onUnlockOrEquipSkin(skin.id)}
                      className={`w-full py-2.5 px-3 rounded-xl text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors cursor-pointer ${
                        isActive
                          ? 'bg-amber-500/15 text-amber-300 border border-amber-400/40'
                          : isUnlocked
                            ? 'bg-slate-800 hover:bg-slate-700 text-white'
                            : canAfford
                              ? 'bg-cyan-400 hover:bg-cyan-300 text-slate-950'
                              : 'bg-slate-800/70 text-slate-500 cursor-not-allowed'
                      }`}
                    >
                      {isActive ? (
                        <>
                          <Check className="w-3.5 h-3.5" />
                          <span>Equipped</span>
                        </>
                      ) : isUnlocked ? (
                        <span>Equip Skin</span>
                      ) : (
                        <>
                          <Gem className="w-3.5 h-3.5" />
                          <span>Unlock · {skin.priceDiamonds} ◆</span>
                        </>
                      )}
                    </button>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      ) : (
        /* Power-Ups Grid */
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {POWER_UPS.map((item) => {
            const ownedCount =
              item.id === 'doubleCoins'
                ? progress.inventory.doubleCoinsTurns
                : progress.inventory[item.id];
            const canAffordCoins = progress.currencies.coins >= item.priceCoins;
            const canAffordDiamonds =
              progress.currencies.diamonds >= item.priceDiamonds;

            return (
              <div
                key={item.id}
                className="rounded-2xl bg-slate-900/80 border border-slate-800 p-4 sm:p-5 flex flex-col justify-between gap-4"
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-start gap-3">
                    <div className="w-10 h-10 rounded-xl bg-amber-500/15 border border-amber-400/30 flex items-center justify-center shrink-0">
                      {item.id === 'doubleCoins' ? (
                        <Sparkles className="w-5 h-5 text-amber-400" />
                      ) : (
                        <Zap className="w-5 h-5 text-amber-400" />
                      )}
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <h4 className="text-sm font-display font-bold text-white">
                          {item.name}
                        </h4>
                        <span className="text-xs text-slate-400 tabular-nums">
                          · +{item.packAmount} pack
                        </span>
                      </div>
                      <p className="text-xs text-slate-400 mt-1 leading-relaxed">
                        {item.description}
                      </p>
                    </div>
                  </div>
                  <span className="text-xs font-mono font-semibold text-slate-300 tabular-nums shrink-0">
                    Owned: {ownedCount}
                  </span>
                </div>

                <div className="grid grid-cols-2 gap-2.5">
                  <button
                    type="button"
                    disabled={!canAffordCoins}
                    onClick={() => onBuyPowerUp(item.id, 'coins')}
                    className={`py-2.5 px-3 rounded-xl text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors tabular-nums cursor-pointer ${
                      canAffordCoins
                        ? 'bg-yellow-400/15 hover:bg-yellow-400/25 text-yellow-300 border border-yellow-400/35'
                        : 'bg-slate-800/60 text-slate-500 border border-slate-800 cursor-not-allowed'
                    }`}
                  >
                    <Coins className="w-3.5 h-3.5" />
                    <span>Buy · {item.priceCoins}</span>
                  </button>
                  <button
                    type="button"
                    disabled={!canAffordDiamonds}
                    onClick={() => onBuyPowerUp(item.id, 'diamonds')}
                    className={`py-2.5 px-3 rounded-xl text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors tabular-nums cursor-pointer ${
                      canAffordDiamonds
                        ? 'bg-cyan-400/15 hover:bg-cyan-400/25 text-cyan-300 border border-cyan-400/35'
                        : 'bg-slate-800/60 text-slate-500 border border-slate-800 cursor-not-allowed'
                    }`}
                  >
                    <Gem className="w-3.5 h-3.5" />
                    <span>Buy · {item.priceDiamonds} ◆</span>
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </section>
  );
};
