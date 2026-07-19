FROM node:22.23.1-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM node:22.23.1-alpine

WORKDIR /app
ENV NODE_ENV=production

COPY --from=build /app/dist/ssr ./
RUN npm install --package-lock-only --ignore-scripts --no-audit --no-fund
RUN npm ci --omit=dev --ignore-scripts --no-audit --no-fund

EXPOSE 3000

CMD ["npm", "start"]
