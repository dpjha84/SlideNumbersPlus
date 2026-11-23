import React, { useRef, useState } from 'react';
import { motion } from 'motion/react';
import { ThemeConfig, TileSkinConfig } from '../types/game';

interface PuzzleBoardProps {
  board: number[];
  size: number;
  theme: ThemeConfig;
  skin: TileSkinConfig;
  hintTileIdx: number | null;
  swapMode: boolean;
  swapFirstIdx: number | null;
  invalidShakeIdx: number | null;
  disabled?: boolean;
  onTileClick: (idx: number) => void;
  onSwipeDirection: (dir: 'UP' | 'DOWN' | 'LEFT' | 'RIGHT') => void;
}

export const PuzzleBoard: React.FC<PuzzleBoardProps> = ({
  board,
  size,
  theme,
  skin,
  hintTileIdx,
  swapMode,
  swapFirstIdx,
  invalidShakeIdx,
  disabled = false,
  onTileClick,
  onSwipeDirection,
}) => {
  const pointerStart = useRef<{ x: number; y: number } | null>(null);
  const [imgFailed, setImgFailed] = useState(false);

  const handlePointerDown = (e: React.PointerEvent) => {
    if (disabled) return;
    pointerStart.current = { x: e.clientX, y: e.clientY };
  };

  const handlePointerUp = (e: React.PointerEvent) => {
    if (disabled || !pointerStart.current) return;
    const dx = e.clientX - pointerStart.current.x;
    const dy = e.clientY - pointerStart.current.y;
    pointerStart.current = null;

    const absX = Math.abs(dx);
    const absY = Math.abs(dy);
    const SWIPE_THRESHOLD = 32;

    if (Math.max(absX, absY) >= SWIPE_THRESHOLD) {
      if (absX > absY) {
        onSwipeDirection(dx > 0 ? 'RIGHT' : 'LEFT');
      } else {
        onSwipeDirection(dy > 0 ? 'DOWN' : 'UP');
      }
    }
  };

  const blankIdx = board.indexOf(0);
  const blankR = Math.floor(blankIdx / size);
  const blankC = blankIdx % size;

  const fontSizeClass =
    size === 3
      ? 'text-3xl sm:text-4xl'
      : size === 4
        ? 'text-2xl sm:text-3xl'
        : size === 5
          ? 'text-lg sm:text-xl'
          : 'text-base sm:text-lg';

  const gapClass =
    size <= 4 ? 'gap-2 sm:gap-2.5' : size === 5 ? 'gap-1.5 sm:gap-2' : 'gap-1 sm:gap-1.5';

  return (
    <div
      className={`relative w-full max-w-[420px] mx-auto rounded-3xl p-3 sm:p-4 overflow-hidden select-none ${theme.boardFrame}`}
      onPointerDown={handlePointerDown}
      onPointerUp={handlePointerUp}
      style={{ touchAction: 'none' }}
    >
      {/* Theme Artwork Backdrop inside Board Frame */}
      <div
        className="absolute inset-0 pointer-events-none"
        style={{ background: theme.fallbackGradient }}
      >
        {theme.imageUrl && !imgFailed && (
          <img
            src={theme.imageUrl}
            alt={`${theme.name} puzzle surface`}
            referrerPolicy="no-referrer"
            onError={() => setImgFailed(true)}
            className="w-full h-full object-cover opacity-80"
          />
        )}
        <div className="absolute inset-0 bg-gradient-to-b from-black/20 via-black/25 to-black/55" />
      </div>

      {/* Inner Tactile Well */}
      <div
        className={`relative z-10 w-full aspect-square rounded-2xl p-2 sm:p-2.5 ${theme.boardWell}`}
      >
        <div
          className={`w-full h-full grid ${gapClass}`}
          style={{
            gridTemplateColumns: `repeat(${size}, minmax(0, 1fr))`,
            gridTemplateRows: `repeat(${size}, minmax(0, 1fr))`,
          }}
        >
          {board.map((val, idx) => {
            if (val === 0) {
              return (
                <div
                  key="blank-slot"
                  aria-hidden="true"
                  className="rounded-xl bg-black/25 border border-white/5 shadow-inner"
                />
              );
            }

            const r = Math.floor(idx / size);
            const c = idx % size;
            const isMovable = r === blankR || c === blankC;
            const isCorrect = val === idx + 1;
            const isHint = hintTileIdx === idx;
            const isSwapSelected = swapFirstIdx === idx;
            const isShaking = invalidShakeIdx === idx;

            return (
              <motion.button
                key={`tile-${val}`}
                layout
                type="button"
                disabled={disabled}
                onClick={() => onTileClick(idx)}
                transition={{
                  type: 'spring',
                  stiffness: 560,
                  damping: 30,
                  mass: 0.65,
                }}
                whileTap={isMovable || swapMode ? { scale: 0.92 } : { scale: 0.97 }}
                className={`relative flex items-center justify-center font-display font-bold tabular-nums select-none cursor-pointer transition-shadow focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-amber-400 ${
                  skin.tileClassModifier
                } ${fontSizeClass} ${
                  isCorrect
                    ? `${theme.tileSolved} ${theme.tileSolvedText}`
                    : `${theme.tileBase} ${theme.tileText}`
                } ${
                  isHint
                    ? 'ring-3 ring-amber-300 animate-hint-ring z-20'
                    : isSwapSelected
                      ? 'ring-3 ring-emerald-400 scale-[0.96] z-20'
                      : swapMode
                        ? 'hover:ring-2 hover:ring-emerald-300/70'
                        : isMovable
                          ? 'hover:brightness-110'
                          : 'opacity-95'
                } ${isShaking ? 'animate-shake-gentle' : ''}`}
                aria-label={`Tile ${val}${isCorrect ? ', in correct position' : ''}`}
              >
                {/* Top specular highlight */}
                <span
                  aria-hidden="true"
                  className="pointer-events-none absolute inset-x-1.5 top-1 h-1/4 rounded-t-lg bg-gradient-to-b from-white/25 to-transparent"
                />
                <span className="relative z-10 drop-shadow-sm">{val}</span>

                {/* Subtle solved lock dot */}
                {isCorrect && (
                  <span
                    aria-hidden="true"
                    className="absolute bottom-1.5 right-1.5 w-1.5 h-1.5 rounded-full bg-current opacity-60"
                  />
                )}

                {/* Hint badge indicator */}
                {isHint && (
                  <span
                    aria-hidden="true"
                    className="absolute -top-1.5 -right-1.5 w-4 h-4 rounded-full bg-amber-400 text-slate-950 text-[10px] font-bold flex items-center justify-center shadow"
                  >
                    !
                  </span>
                )}
              </motion.button>
            );
          })}
        </div>
      </div>
    </div>
  );
};
