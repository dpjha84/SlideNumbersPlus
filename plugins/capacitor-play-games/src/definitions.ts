export interface PlayGamesPlayer {
  playerId: string;
  displayName: string;
  isSignedIn: boolean;
}

export interface PlayGamesPlugin {
  signIn(): Promise<PlayGamesPlayer>;
  getPlayerProfile(): Promise<PlayGamesPlayer>;
  submitScore(options: { leaderboardId: string; score: number }): Promise<void>;
  showLeaderboard(options: { leaderboardId: string }): Promise<void>;
  unlockAchievement(options: { achievementId: string }): Promise<void>;
  showAchievements(): Promise<void>;
}
