import { WebPlugin } from '@capacitor/core';
import type { PlayGamesPlayer, PlayGamesPlugin } from './definitions';

export class PlayGamesWeb extends WebPlugin implements PlayGamesPlugin {
  private signedIn = true;
  private playerId = 'gpgs-web-preview';
  private displayName = 'MetroSlider';

  async signIn(): Promise<PlayGamesPlayer> {
    this.signedIn = true;
    return {
      playerId: this.playerId,
      displayName: this.displayName,
      isSignedIn: this.signedIn,
    };
  }

  async getPlayerProfile(): Promise<PlayGamesPlayer> {
    return {
      playerId: this.playerId,
      displayName: this.displayName,
      isSignedIn: this.signedIn,
    };
  }

  async submitScore(_options: {
    leaderboardId: string;
    score: number;
  }): Promise<void> {
    // Handled via local persistent leaderboard in WebView preview
  }

  async showLeaderboard(_options: { leaderboardId: string }): Promise<void> {
    // Native Play Games UI opens on Android device
  }

  async unlockAchievement(_options: { achievementId: string }): Promise<void> {
    // Handled via Play Games achievement tracker in WebView preview
  }

  async showAchievements(): Promise<void> {
    // Native Play Games UI opens on Android device
  }
}
