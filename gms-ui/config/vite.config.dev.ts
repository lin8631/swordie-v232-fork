import { mergeConfig } from 'vite';
import baseConfig from './vite.config.base';

export default mergeConfig(baseConfig, {
  mode: 'development',
  server: {
    open: true,
    fs: {
      strict: true,
    },
    proxy: {
      '/api': {
        target: 'http://localhost:3000',
        changeOrigin: true,
      },
    },
  },
  plugins: [],
});
