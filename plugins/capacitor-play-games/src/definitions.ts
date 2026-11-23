export interface PlayerInfo {
  playerId: string;
  displayName: string;
  avatarUrl?: string;
}

export interface SignInResult {
  signedIn: boolean;
  player?: PlayerInfo;
  reason?: 'cancelled' | 'unavailable' | 'not_authenticated';
}

export interface PlayGamesPlugin {
  initialize(): Promise<void>;
  signIn(opts?: { silent?: boolean }): Promise<SignInResult>;
  isSignedIn(): Promise<{ signedIn: boolean }>;
  getPlayer(): Promise<PlayerInfo>;
  unlockAchievement(opts: { id: string }): Promise<void>;
  incrementAchievement(opts: { id: string; steps: number }): Promise<void>;
  showAchievements(): Promise<void>;
  submitScore(opts: { leaderboardId: string; score: number }): Promise<void>;
  showLeaderboard(opts: { leaderboardId: string }): Promise<void>;
  showAllLeaderboards(): Promise<void>;
}
