# ---------- build stage ----------
FROM node:18-alpine AS builder

# 1. 建立工作目錄
WORKDIR /app

# 2. 安裝依賴
COPY package*.json ./
RUN npm ci

# 3. 複製專案原始碼並進行編譯
COPY . .
COPY env/.env.production ./.env
RUN npm run build

# ---------- production stage ----------
FROM nginx:stable-alpine AS runner

# 4. 將編譯好的靜態檔案放入 nginx 預設網站資料夾
COPY --from=builder /app/dist /usr/share/nginx/html

# 5. 若需要自定義 SPA fallback，可放置 nginx.conf
COPY nginx/nginx.conf /etc/nginx/conf.d/default.conf

# 6. 對外暴露 80 端口
EXPOSE 80

# 7. 啟動 Nginx
CMD ["nginx", "-g", "daemon off;"]