import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.slidepulse.puzzle',
  appName: 'SlidePulse',
  webDir: 'dist',
  server: {
    androidScheme: 'https',
  },
  plugins: {
    AdMob: {
      initializeForTesting: true,
    },
  },
  android: {
    backgroundColor: '#020617',
    allowMixedContent: false,
  },
};

export default config;
