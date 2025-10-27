# 🚀 Deploy Backend ke Railway - Step by Step

## 📋 Prerequisites

- ✅ Account Railway.app (gratis)
- ✅ Account GitHub (gratis)
- ✅ Git installed

---

## 🎯 Method 1: Deploy via Railway Dashboard (PALING MUDAH)

### Step 1: Push Backend ke GitHub

```bash
# Masuk ke folder backend
cd backend

# Initialize git
git init

# Add semua files
git add .

# Commit
git commit -m "Initial backend setup for DexAnime"

# Create repo di GitHub (via web)
# Nama: dexanime-backend

# Add remote
git remote add origin https://github.com/USERNAME/dexanime-backend.git

# Push
git push -u origin main
```

### Step 2: Deploy di Railway

1. **Login ke Railway:**
   - Go to https://railway.app
   - Sign in with GitHub

2. **New Project:**
   - Click "New Project"
   - Select "Deploy from GitHub repo"
   - Pilih repository `dexanime-backend`

3. **Railway Auto-Detect:**
   - Railway akan detect `package.json`
   - Auto-set build command: `npm run build`
   - Auto-set start command: `npm start`

4. **Set Environment Variables:**
   - Go to "Variables" tab
   - Add variables:
   ```
   NODE_ENV=production
   ANOBOY_BASE_URL=https://anoboy.be
   CACHE_TTL=300
   MAX_REQUESTS_PER_MINUTE=30
   ```
   
   **PENTING:** Tambahkan frontend URL setelah deploy frontend:
   ```
   ALLOWED_ORIGINS=https://your-frontend.vercel.app
   ```

5. **Deploy!**
   - Railway akan auto-deploy
   - Tunggu sampai selesai (~2-3 menit)
   - Dapatkan URL publik (e.g., `https://dexanime-backend.up.railway.app`)

---

## 🎯 Method 2: Deploy via Railway CLI

### Step 1: Install Railway CLI

```bash
npm install -g @railway/cli
```

### Step 2: Login

```bash
railway login
```

### Step 3: Initialize Project

```bash
cd backend
railway init
```

Pilih:
- Create new project? **Yes**
- Project name: **dexanime-backend**

### Step 4: Link to GitHub (Optional)

```bash
railway link
```

### Step 5: Set Environment Variables

```bash
railway variables set NODE_ENV=production
railway variables set ANOBOY_BASE_URL=https://anoboy.be
railway variables set CACHE_TTL=300
railway variables set MAX_REQUESTS_PER_MINUTE=30
```

### Step 6: Deploy

```bash
railway up
```

Railway akan:
1. Upload code
2. Install dependencies
3. Build TypeScript
4. Start server

### Step 7: Get URL

```bash
railway domain
```

Atau buat custom domain:
```bash
railway domain create
```

---

## 🔧 Update Environment Variables Setelah Deploy Frontend

Setelah frontend deploy ke Vercel, tambahkan URL-nya:

**Via Dashboard:**
1. Go to Railway project
2. Variables tab
3. Edit `ALLOWED_ORIGINS`
4. Add: `https://your-frontend.vercel.app`

**Via CLI:**
```bash
railway variables set ALLOWED_ORIGINS="http://localhost:3000,https://your-frontend.vercel.app"
```

---

## ✅ Verify Deployment

### Test API Endpoint

```bash
# Health check
curl https://your-backend.up.railway.app/api/health

# Latest anime
curl https://your-backend.up.railway.app/api/latest

# Search
curl https://your-backend.up.railway.app/api/search?q=one+punch+man
```

Response sukses:
```json
{
  "success": true,
  "data": [...],
  "count": 20
}
```

---

## 🌐 Update Frontend untuk Gunakan Backend Railway

### File: `src/services/anoboyScraperApi.ts`

```typescript
const USE_MOCK_DATA = false; // Ganti jadi false
const PROXY_URL = 'https://your-backend.up.railway.app/api'; // URL Railway kamu
```

---

## 📊 Monitor Backend

### Via Railway Dashboard:

1. **Logs:**
   - Railway Dashboard → Deployments → Logs
   - Real-time logs

2. **Metrics:**
   - CPU usage
   - Memory usage
   - Request count

3. **Restarts:**
   - Auto-restart on crash
   - Manual restart available

### Via CLI:

```bash
# View logs
railway logs

# View status
railway status

# Restart
railway restart
```

---

## 🐛 Troubleshooting

### Build Failed

**Error:** TypeScript compilation error

**Fix:**
```bash
# Local test
cd backend
npm run build

# Fix errors, then redeploy
git add .
git commit -m "Fix build errors"
git push
```

### Server Crashed

**Check logs:**
```bash
railway logs
```

**Common issues:**
- Missing environment variables
- Port binding (Railway auto-assigns port)
- Memory limit exceeded

### CORS Error

**Error:** "Not allowed by CORS"

**Fix:** Update `ALLOWED_ORIGINS`:
```bash
railway variables set ALLOWED_ORIGINS="https://your-frontend.vercel.app,http://localhost:3000"
```

### Rate Limit Too Low

**Increase limit:**
```bash
railway variables set MAX_REQUESTS_PER_MINUTE=60
railway restart
```

---

## 💰 Railway Pricing

**Free Tier:**
- ✅ $5 credit per month
- ✅ Auto-sleep after 7 days inactive
- ✅ 512 MB RAM
- ✅ 1 GB disk
- ✅ Good for small projects

**Paid Plans:**
- Developer: $5/month
- Team: $20/month

---

## 🔄 Auto-Deploy on Push

Railway auto-deploys saat kamu push ke GitHub:

```bash
# Make changes
git add .
git commit -m "Update scraper"
git push

# Railway auto-deploys in ~2 minutes
```

---

## 🔒 Security Checklist

- ✅ Set `NODE_ENV=production`
- ✅ Configure `ALLOWED_ORIGINS`
- ✅ Enable rate limiting
- ✅ Set reasonable cache TTL
- ✅ Don't commit `.env` file
- ✅ Use environment variables for secrets

---

## 📈 Performance Tips

1. **Increase Cache TTL:**
   ```bash
   railway variables set CACHE_TTL=600  # 10 minutes
   ```

2. **Optimize Rate Limit:**
   ```bash
   railway variables set MAX_REQUESTS_PER_MINUTE=60
   ```

3. **Monitor Memory:**
   - Check Railway metrics
   - Optimize if needed

---

## 🎉 Success Checklist

- ✅ Backend deployed to Railway
- ✅ Environment variables configured
- ✅ API endpoints working
- ✅ CORS configured for frontend
- ✅ Frontend connected to backend
- ✅ Rate limiting active
- ✅ Caching working
- ✅ Logs monitored

---

## 📝 Next Steps

1. **Deploy Frontend to Vercel:**
   - See main README.md
   - Update `ALLOWED_ORIGINS` with Vercel URL

2. **Test Integration:**
   - Frontend → Backend → Anoboy.be
   - Check all pages working

3. **Monitor:**
   - Railway logs
   - Frontend console
   - API response times

---

## 🆘 Support

**Railway Issues:**
- Discord: https://discord.gg/railway
- Docs: https://docs.railway.app

**Backend Issues:**
- Check logs: `railway logs`
- Test locally first: `npm run dev`
- Verify env variables

---

**Your Backend URL:**
```
https://dexanime-backend.up.railway.app
```

**API Base:**
```
https://dexanime-backend.up.railway.app/api
```

**Update this in frontend config!** 🚀
