import { i as WebPlugin } from "./index-D3H8pOpB.js";
// node_modules/capacitor-play-games/src/web.ts
var PlayGamesWeb = class extends WebPlugin {
	signedIn = true;
	player = {
		playerId: "gpgs-web-preview",
		displayName: "MetroSlider"
	};
	async initialize() {}
	async signIn(_opts) {
		this.signedIn = true;
		return {
			signedIn: true,
			player: this.player
		};
	}
	async isSignedIn() {
		return { signedIn: this.signedIn };
	}
	async getPlayer() {
		if (!this.signedIn) return {
			playerId: "",
			displayName: ""
		};
		return this.player;
	}
	async unlockAchievement(_opts) {}
	async incrementAchievement(_opts) {}
	async showAchievements() {}
	async submitScore(_opts) {}
	async showLeaderboard(_opts) {}
	async showAllLeaderboards() {}
};
export { PlayGamesWeb };
