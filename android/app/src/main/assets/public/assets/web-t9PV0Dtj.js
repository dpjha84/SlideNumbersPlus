import { i as WebPlugin } from "./index-CAeBZV6Y.js";
// node_modules/capacitor-play-games/src/web.ts
var PlayGamesWeb = class extends WebPlugin {
	signedIn = true;
	playerId = "gpgs-web-preview";
	displayName = "MetroSlider";
	async signIn() {
		this.signedIn = true;
		return {
			playerId: this.playerId,
			displayName: this.displayName,
			isSignedIn: this.signedIn
		};
	}
	async getPlayerProfile() {
		return {
			playerId: this.playerId,
			displayName: this.displayName,
			isSignedIn: this.signedIn
		};
	}
	async submitScore(_options) {}
	async showLeaderboard(_options) {}
	async unlockAchievement(_options) {}
	async showAchievements() {}
};
export { PlayGamesWeb };
