// Deterministic PRNG (Mulberry32) so levels & daily challenges are reproducible
export function createSeededRng(seedStr: string): () => number {
  let h = 2166136261 >>> 0;
  for (let i = 0; i < seedStr.length; i++) {
    h ^= seedStr.charCodeAt(i);
    h = Math.imul(h, 16777619);
  }
  let a = h >>> 0;
  return function () {
    a |= 0;
    a = (a + 0x6d2b79f5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

export function createSolvedBoard(size: number): number[] {
  const total = size * size;
  const arr: number[] = [];
  for (let i = 1; i < total; i++) {
    arr.push(i);
  }
  arr.push(0);
  return arr;
}

export function isSolved(board: number[]): boolean {
  for (let i = 0; i < board.length - 1; i++) {
    if (board[i] !== i + 1) return false;
  }
  return board[board.length - 1] === 0;
}

export function countCorrectTiles(board: number[]): number {
  let count = 0;
  for (let i = 0; i < board.length - 1; i++) {
    if (board[i] === i + 1) count++;
  }
  return count;
}

export function getManhattanDistance(board: number[], size: number): number {
  let dist = 0;
  for (let idx = 0; idx < board.length; idx++) {
    const val = board[idx];
    if (val === 0) continue;
    const targetIdx = val - 1;
    const curR = Math.floor(idx / size);
    const curC = idx % size;
    const tgtR = Math.floor(targetIdx / size);
    const tgtC = targetIdx % size;
    dist += Math.abs(curR - tgtR) + Math.abs(curC - tgtC);
  }
  return dist;
}

export function getLinearConflictHeuristic(board: number[], size: number): number {
  let h = getManhattanDistance(board, size);
  // Row conflicts
  for (let r = 0; r < size; r++) {
    for (let c1 = 0; c1 < size; c1++) {
      const v1 = board[r * size + c1];
      if (v1 === 0 || Math.floor((v1 - 1) / size) !== r) continue;
      for (let c2 = c1 + 1; c2 < size; c2++) {
        const v2 = board[r * size + c2];
        if (v2 === 0 || Math.floor((v2 - 1) / size) !== r) continue;
        if (v1 > v2) h += 2;
      }
    }
  }
  // Column conflicts
  for (let c = 0; c < size; c++) {
    for (let r1 = 0; r1 < size; r1++) {
      const v1 = board[r1 * size + c];
      if (v1 === 0 || (v1 - 1) % size !== c) continue;
      for (let r2 = r1 + 1; r2 < size; r2++) {
        const v2 = board[r2 * size + c];
        if (v2 === 0 || (v2 - 1) % size !== c) continue;
        if (v1 > v2) h += 2;
      }
    }
  }
  return h;
}

export function generateSolvableBoard(
  size: number,
  scrambleSteps: number,
  seed: string
): number[] {
  const rng = createSeededRng(seed);
  const board = createSolvedBoard(size);
  let blankIdx = board.length - 1;
  let prevBlankIdx = -1;

  for (let step = 0; step < scrambleSteps; step++) {
    const r = Math.floor(blankIdx / size);
    const c = blankIdx % size;
    const neighbors: number[] = [];
    if (r > 0) neighbors.push((r - 1) * size + c);
    if (r < size - 1) neighbors.push((r + 1) * size + c);
    if (c > 0) neighbors.push(r * size + (c - 1));
    if (c < size - 1) neighbors.push(r * size + (c + 1));

    const filtered = neighbors.filter((n) => n !== prevBlankIdx);
    const pickPool = filtered.length > 0 ? filtered : neighbors;
    const chosen = pickPool[Math.floor(rng() * pickPool.length)];

    board[blankIdx] = board[chosen];
    board[chosen] = 0;
    prevBlankIdx = blankIdx;
    blankIdx = chosen;
  }

  // Ensure it's never trivially solved
  if (isSolved(board)) {
    const swapIdx = board.length - 2;
    board[board.length - 1] = board[swapIdx];
    board[swapIdx] = 0;
  }

  return board;
}

export interface SlideResult {
  nextBoard: number[];
  stepsMoved: number;
  movedTileValues: number[];
  newlyLockedCount: number;
}

/**
 * Supports single-tile adjacent slides AND multi-tile line slides along the same row or column.
 */
export function slideTileAt(
  board: number[],
  size: number,
  clickedIdx: number
): SlideResult | null {
  const blankIdx = board.indexOf(0);
  if (clickedIdx === blankIdx || clickedIdx < 0 || clickedIdx >= board.length) {
    return null;
  }

  const clickR = Math.floor(clickedIdx / size);
  const clickC = clickedIdx % size;
  const blankR = Math.floor(blankIdx / size);
  const blankC = blankIdx % size;

  if (clickR !== blankR && clickC !== blankC) {
    return null;
  }

  const nextBoard = [...board];
  const movedTileValues: number[] = [];
  let stepsMoved = 0;

  if (clickR === blankR) {
    const dir = clickC < blankC ? -1 : 1;
    let cur = blankC;
    while (cur !== clickC) {
      const nextC = cur + dir;
      const targetPos = blankR * size + cur;
      const sourcePos = blankR * size + nextC;
      movedTileValues.push(nextBoard[sourcePos]);
      nextBoard[targetPos] = nextBoard[sourcePos];
      cur = nextC;
      stepsMoved++;
    }
    nextBoard[clickedIdx] = 0;
  } else {
    const dir = clickR < blankR ? -1 : 1;
    let cur = blankR;
    while (cur !== clickR) {
      const nextR = cur + dir;
      const targetPos = cur * size + blankC;
      const sourcePos = nextR * size + blankC;
      movedTileValues.push(nextBoard[sourcePos]);
      nextBoard[targetPos] = nextBoard[sourcePos];
      cur = nextR;
      stepsMoved++;
    }
    nextBoard[clickedIdx] = 0;
  }

  const prevCorrect = countCorrectTiles(board);
  const nextCorrect = countCorrectTiles(nextBoard);
  const newlyLockedCount = Math.max(0, nextCorrect - prevCorrect);

  return {
    nextBoard,
    stepsMoved,
    movedTileValues,
    newlyLockedCount,
  };
}

export function slideByDirection(
  board: number[],
  size: number,
  direction: 'UP' | 'DOWN' | 'LEFT' | 'RIGHT'
): SlideResult | null {
  const blankIdx = board.indexOf(0);
  const blankR = Math.floor(blankIdx / size);
  const blankC = blankIdx % size;

  // Moving a tile UP means the tile BELOW the blank slides into the blank
  let targetR = blankR;
  let targetC = blankC;
  if (direction === 'UP') targetR = blankR + 1;
  if (direction === 'DOWN') targetR = blankR - 1;
  if (direction === 'LEFT') targetC = blankC + 1;
  if (direction === 'RIGHT') targetC = blankC - 1;

  if (targetR < 0 || targetR >= size || targetC < 0 || targetC >= size) {
    return null;
  }
  return slideTileAt(board, size, targetR * size + targetC);
}

/**
 * Calculates Star Rating (1 to 3) based on moves taken vs targetMoves.
 */
export function calculateStars(moves: number, targetMoves: number): number {
  if (moves <= targetMoves) return 3;
  if (moves <= Math.ceil(targetMoves * 1.35)) return 2;
  return 1;
}

export function calculateScore(
  level: number,
  size: number,
  moves: number,
  targetMoves: number,
  timeSec: number,
  stars: number
): number {
  const base = size * size * 45 + Math.max(1, level) * 60;
  const parEfficiency = Math.max(0, (targetMoves - moves) * 35);
  const timeBonus = Math.max(0, Math.round(300 - timeSec * 1.8));
  const starMult = stars === 3 ? 1.5 : stars === 2 ? 1.2 : 1.0;
  return Math.round((base + parEfficiency + timeBonus) * starMult);
}

/**
 * Bounded lookahead / Weighted A* hint finder returning the index of the best tile to click next.
 */
export function findBestNextTileIndex(
  board: number[],
  size: number,
  lastBlankIdx: number | null = null
): number | null {
  if (isSolved(board)) return null;

  const blankIdx = board.indexOf(0);
  const blankR = Math.floor(blankIdx / size);
  const blankC = blankIdx % size;

  const getNeighbors = (bIdx: number): number[] => {
    const r = Math.floor(bIdx / size);
    const c = bIdx % size;
    const list: number[] = [];
    if (r > 0) list.push((r - 1) * size + c);
    if (r < size - 1) list.push((r + 1) * size + c);
    if (c > 0) list.push(r * size + (c - 1));
    if (c < size - 1) list.push(r * size + (c + 1));
    return list;
  };

  // Weighted A* with bounded node budget (up to 3500 expansions for instant <15ms UI response)
  interface SearchNode {
    state: number[];
    blank: number;
    g: number;
    f: number;
    firstMoveTileIdx: number;
  }

  const encode = (arr: number[]) => arr.join(',');
  const visited = new Map<string, number>();
  const weight = size === 3 ? 1.25 : 2.2;

  const initialNeighbors = getNeighbors(blankIdx);
  const queue: SearchNode[] = [];

  for (const nIdx of initialNeighbors) {
    const next = [...board];
    next[blankIdx] = next[nIdx];
    next[nIdx] = 0;
    if (isSolved(next)) return nIdx;
    const h = getLinearConflictHeuristic(next, size);
    const avoidBacktrack = nIdx === lastBlankIdx ? 2 : 0;
    queue.push({
      state: next,
      blank: nIdx,
      g: 1,
      f: 1 + weight * h + avoidBacktrack,
      firstMoveTileIdx: nIdx,
    });
    visited.set(encode(next), 1);
  }

  let bestNode = queue[0];
  let bestHeuristic = Infinity;
  let expansions = 0;
  const MAX_EXPANSIONS = size <= 3 ? 4000 : 2800;

  while (queue.length > 0 && expansions < MAX_EXPANSIONS) {
    // Pick smallest f
    let minI = 0;
    for (let i = 1; i < queue.length; i++) {
      if (queue[i].f < queue[minI].f) minI = i;
    }
    const curr = queue.splice(minI, 1)[0];
    expansions++;

    const currH = getLinearConflictHeuristic(curr.state, size);
    if (currH === 0) {
      return curr.firstMoveTileIdx;
    }
    if (currH < bestHeuristic) {
      bestHeuristic = currH;
      bestNode = curr;
    }

    const neighbors = getNeighbors(curr.blank);
    for (const nIdx of neighbors) {
      const next = [...curr.state];
      next[curr.blank] = next[nIdx];
      next[nIdx] = 0;
      const key = encode(next);
      const nextG = curr.g + 1;
      const prevG = visited.get(key);
      if (prevG !== undefined && prevG <= nextG) continue;
      visited.set(key, nextG);
      const h = getLinearConflictHeuristic(next, size);
      if (h === 0) return curr.firstMoveTileIdx;
      queue.push({
        state: next,
        blank: nIdx,
        g: nextG,
        f: nextG + weight * h,
        firstMoveTileIdx: curr.firstMoveTileIdx,
      });
    }
  }

  if (bestNode) return bestNode.firstMoveTileIdx;

  // Fallback: immediate best Manhattan neighbor
  const r = blankR;
  const c = blankC;
  const fallbackNeighbors: number[] = [];
  if (r > 0) fallbackNeighbors.push((r - 1) * size + c);
  if (r < size - 1) fallbackNeighbors.push((r + 1) * size + c);
  if (c > 0) fallbackNeighbors.push(r * size + (c - 1));
  if (c < size - 1) fallbackNeighbors.push(r * size + (c + 1));

  return fallbackNeighbors[0] ?? null;
}
