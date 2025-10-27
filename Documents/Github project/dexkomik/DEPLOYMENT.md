# 🚀 Deployment Guide - DexAnime

## Deployment Options

### 1. Vercel (Recommended) ⚡

**Steps:**
1. Push code ke GitHub repository
2. Login ke [Vercel](https://vercel.com)
3. Import repository
4. Configure:
   - Framework Preset: `Vite`
   - Build Command: `npm run build`
   - Output Directory: `dist`
5. Deploy!

**CLI Method:**
```bash
npm install -g vercel
vercel
```

### 2. Netlify 🎯

**Steps:**
1. Push code ke GitHub
2. Login ke [Netlify](https://netlify.com)
3. New site from Git
4. Build settings:
   - Build command: `npm run build`
   - Publish directory: `dist`
5. Deploy!

**CLI Method:**
```bash
npm install -g netlify-cli
netlify deploy --prod
```

### 3. GitHub Pages 📄

**Setup:**
1. Install gh-pages:
```bash
npm install -g gh-pages
```

2. Update `vite.config.ts`:
```typescript
export default defineConfig({
  base: '/dexanime/', // nama repository
  // ... rest of config
})
```

3. Add script to `package.json`:
```json
{
  "scripts": {
    "deploy": "vite build && gh-pages -d dist"
  }
}
```

4. Deploy:
```bash
npm run deploy
```

### 4. Firebase Hosting 🔥

**Setup:**
1. Install Firebase CLI:
```bash
npm install -g firebase-tools
firebase login
firebase init hosting
```

2. Configure `firebase.json`:
```json
{
  "hosting": {
    "public": "dist",
    "ignore": ["firebase.json", "**/.*", "**/node_modules/**"],
    "rewrites": [
      {
        "source": "**",
        "destination": "/index.html"
      }
    ]
  }
}
```

3. Build & Deploy:
```bash
npm run build
firebase deploy
```

### 5. Docker 🐳

**Dockerfile:**
```dockerfile
# Build stage
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Production stage
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

**nginx.conf:**
```nginx
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # Cache static assets
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

**Build & Run:**
```bash
docker build -t dexanime .
docker run -p 8080:80 dexanime
```

## 🔧 Pre-Deployment Checklist

- [ ] Run production build locally: `npm run build`
- [ ] Test production build: `npm run preview`
- [ ] Check for console errors
- [ ] Verify all routes work
- [ ] Test responsive design
- [ ] Optimize images (if any)
- [ ] Update API URLs (if needed)
- [ ] Set environment variables
- [ ] Check build size
- [ ] Run linter: `npm run lint`
- [ ] Update README with live URL

## 📊 Build Optimization

### Analyze Bundle Size
```bash
npm run build
npx vite-bundle-visualizer
```

### Reduce Bundle Size
1. **Code Splitting**: Already done by Vite
2. **Tree Shaking**: Enabled by default
3. **Compression**: Enable on server
4. **Image Optimization**: Use WebP format

### Performance Tips
- Enable gzip/brotli compression
- Use CDN for static assets
- Set proper cache headers
- Enable HTTP/2
- Add service worker for PWA

## 🌍 Environment Variables

Create `.env.production`:
```env
VITE_API_BASE_URL=https://api-anoboy.vercel.app/api
VITE_API_TIMEOUT=10000
```

For Vercel/Netlify, set via dashboard.

## 🔒 Security Headers

Add to deployment platform:

```
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Referrer-Policy: no-referrer
Permissions-Policy: geolocation=(), microphone=(), camera=()
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';
```

## 📈 Monitoring

### Add Analytics
1. Google Analytics
2. Vercel Analytics
3. Cloudflare Analytics

### Error Tracking
1. Sentry
2. LogRocket
3. Bugsnag

## 🎯 Post-Deployment

1. **Test Live Site:**
   - All pages load
   - API calls work
   - Responsive on mobile
   - Search functionality
   - Video player works

2. **Update URLs:**
   - Update README
   - Update documentation
   - Share with users

3. **Monitor Performance:**
   - Check loading times
   - Monitor API errors
   - Check user feedback

## 📝 Custom Domain

### Vercel
```bash
vercel domains add yourdomain.com
```

### Netlify
Dashboard → Domain Settings → Add custom domain

### Configure DNS
```
Type: CNAME
Name: www
Value: your-deployment-url
```

## 🔄 CI/CD Pipeline

### GitHub Actions Example
Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'
      - run: npm ci
      - run: npm run build
      - run: npm run test # if you have tests
      # Deploy step depends on your platform
```

## 💡 Tips

1. **Always test locally first** with `npm run preview`
2. **Use environment variables** for API URLs
3. **Enable compression** on your server
4. **Set up monitoring** from day one
5. **Keep dependencies updated**
6. **Use semantic versioning** for releases

---

**Ready to deploy? Pick your platform and follow the steps above!** 🚀
