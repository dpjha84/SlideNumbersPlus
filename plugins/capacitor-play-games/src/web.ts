import { WebPlugin } from '@capacitor/core';
import type {
  PlayerInfo,
  PlayGamesPlugin,
  SignInResult,
} from './definitions';

export class PlayGamesWeb extends WebPlugin implements PlayGamesPlugin {
  private signedIn = true;
  private readonly player: PlayerInfo = {
    playerId: 'gpgs-web-preview',
    displayName: 'MetroSlider',
  };

  async initialize(): Promise<void> {
    // Web preview fallback — no native SDK initialization needed
  }

  async signIn(_opts?: { silent?: boolean }): Promise<SignInResult> {
    this.signedIn = true;
    return {
      signedIn: true,
      player: this.player,
    };
  }

  async isSignedIn(): Promise<{ signedIn: boolean }> {
    return { signedIn: this.signedIn };
  }

  async getPlayer(): Promise<PlayerInfo> {
    if (!this.signedIn) {
      return { playerId: '', displayName: '' };
    }
    return this.player;
  }

  async unlockAchievement(_opts: { id: string }): Promise<void> {
    // Handled via local Play Games achievement tracker in web preview
  }

  async incrementAchievement(_opts: { id: string; steps: number }): Promise<void> {
    // Web preview no-op
  }

  async showAchievements(): Promise<void> {
    // Native Play Games UI opens on Android device
  }

  async submitScore(_opts: {
    leaderboardId: string;
    score: number;
  }): Promise<void> {
    // Handled via persistent local leaderboard in web preview
  }

  async showLeaderboard(_opts: { leaderboardId: string }): Promise<void> {
    // Native Play Games UI opens on Android device
  }

  async showAllLeaderboards(): Promise<void> {
    // Native Play Games UI opens on Android device
  }
}
