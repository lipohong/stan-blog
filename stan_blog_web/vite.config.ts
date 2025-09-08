import path from 'node:path';

import react from '@vitejs/plugin-react-swc';
import { defineConfig, loadEnv } from 'vite';
import { createHtmlPlugin } from 'vite-plugin-html';

import config from './customization.json';

function buildInjectedScript(): string {
  return `${buildGoogleTagScript()}`;
}

function buildGoogleTagScript(): string {
  return `<meta name="og:title" content="${config.slogan} ｜ ${config.title}" />
    <meta name="og:site_name" content="${config.title}" />
    <meta name="og:type" content="article" />
    <meta name="og:description" content="${config.description}" />
    <meta name="description" content="${config.description}" />
    <meta name="og:image" content="${config.image}" />
    <meta name="og:url" content="/" />
    <meta name="keywords" content="${config.keywords}" />
    <script async src="https://www.googletagmanager.com/gtag/js?id=${config.googleAnalyticsId}"></script>
    <script>
      window.dataLayer = window.dataLayer || [];
      function gtag(){dataLayer.push(arguments);}
      gtag('js', new Date());
      gtag('config', '${config.googleAnalyticsId}');
    </script>`;
}

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  // load from custom env dir
  const env = loadEnv(mode, path.resolve(process.cwd(), 'env'), 'VITE_');

  // fallback if env not set; ensure trailing slash
  const rawBase = env.VITE_BASE ?? '/';
  const base = rawBase.endsWith('/') ? rawBase : `${rawBase}/`;

  return {
    base,
    envDir: './env',
    plugins: [
      react(),
      createHtmlPlugin({
        minify: true,
        entry: 'src/main.tsx',
        template: 'index.html',
        inject: {
          data: {
            title: config.title,
            injectScript: buildInjectedScript(),
          },
          tags: [{ injectTo: 'body-prepend', tag: 'div', attrs: { id: 'tag' } }],
        },
      }),
    ],
    server: { port: 3000, strictPort: true, open: true },
    build: {
      manifest: true,
      rollupOptions: {
        output: {
          manualChunks: {
            'react-vendor': ['react', 'react-dom', 'react-router-dom'],
            'mui-vendor': ['@mui/material', '@mui/icons-material', '@mui/x-date-pickers', '@mui/x-tree-view', '@emotion/react', '@emotion/styled'],
            'editor-vendor': ['@wangeditor/editor', '@wangeditor/editor-for-react'],
            'utils-vendor': ['axios', 'moment', 'i18next', 'i18next-browser-languagedetector', 'i18next-http-backend', 'react-i18next', 'notistack'],
          },
          assetFileNames: assetInfo => {
            const name = assetInfo.name ?? '';
            if (/\.(png|jpe?g|gif|svg|webp|ico)$/.test(name)) return 'assets/images/[name].[hash].[ext]';
            if (/\.(woff2?|eot|ttf|otf)$/.test(name)) return 'assets/fonts/[name].[hash].[ext]';
            return 'assets/[name].[hash].[ext]';
          },
          chunkFileNames: 'assets/js/[name].[hash].js',
          entryFileNames: 'assets/js/[name].[hash].js',
        },
      },
      chunkSizeWarningLimit: 1000,
      sourcemap: false,
      minify: 'terser',
      terserOptions: {
        compress: { drop_console: true, drop_debugger: true },
      },
    },
  };
});
