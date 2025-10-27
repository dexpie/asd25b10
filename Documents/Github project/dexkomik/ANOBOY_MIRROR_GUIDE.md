# 🎯 Cara Membuat Mirror Anoboy

## ⚠️ DISCLAIMER
Mirroring website tanpa izin bisa melanggar hak cipta dan terms of service. Dokumentasi ini hanya untuk tujuan edukasi. Gunakan dengan tanggung jawab sendiri.

## 📋 Opsi untuk Menggunakan Data Anoboy

### **Opsi 1: Scraping Real-time (CORS Issue)** ❌
**Masalah:** Browser akan block karena CORS policy
```
https://anoboy.be → Blocked by CORS
```

### **Opsi 2: Backend Proxy/Scraper (RECOMMENDED)** ✅
Buat backend sederhana yang scrape Anoboy.be dan expose sebagai API

**Flow:**
```
React App → Your Backend API → Scrape Anoboy.be → Return JSON
```

### **Opsi 3: Browser Extension/Tampermonkey** ⚠️
Bypass CORS dengan extension, tapi tidak recommended untuk production

### **Opsi 4: RSS/Feed Parser** 📡
Gunakan RSS feed Anoboy jika tersedia

---

## 🚀 Implementasi Backend Scraper (Node.js + Express)

Saya akan buatkan backend sederhana untuk scrape Anoboy.be:

### 1. Setup Backend Project

```bash
# Di folder terpisah (misalnya: dexanime-backend)
mkdir dexanime-backend
cd dexanime-backend
npm init -y
npm install express axios cheerio cors dotenv
npm install -D nodemon typescript @types/node @types/express @types/cors
```

### 2. Structure Backend
```
dexanime-backend/
├── src/
│   ├── scrapers/
│   │   └── anoboyScaper.ts
│   ├── routes/
│   │   └── anime.ts
│   ├── utils/
│   │   └── parser.ts
│   └── server.ts
├── package.json
└── tsconfig.json
```

### 3. Environment Variables
```env
PORT=5000
ANOBOY_BASE_URL=https://anoboy.be
ALLOWED_ORIGINS=http://localhost:3000
```

---

## 📡 Base URL Anoboy.be

Berdasarkan scraping kamu, ini endpoint yang bisa digunakan:

### **Base URL**
```
https://anoboy.be
```

### **Endpoints:**

1. **Latest Release (Homepage)**
   ```
   GET https://anoboy.be/
   ```

2. **All Anime**
   ```
   GET https://anoboy.be/anime/?status=&type=&order=update
   ```

3. **Ongoing Anime**
   ```
   GET https://anoboy.be/anime/?status=ongoing
   ```

4. **Completed Anime**
   ```
   GET https://anoboy.be/anime/?status=completed
   ```

5. **Anime Detail**
   ```
   GET https://anoboy.be/anime/[anime-slug]/
   Contoh: https://anoboy.be/anime/one-punch-man-3/
   ```

6. **Episode/Watch**
   ```
   GET https://anoboy.be/[episode-slug]/
   Contoh: https://anoboy.be/one-punch-man-3-episode-3-subtitle-indonesia/
   ```

7. **Search**
   ```
   GET https://anoboy.be/?s=[query]
   Contoh: https://anoboy.be/?s=one+punch+man
   ```

8. **Pagination**
   ```
   GET https://anoboy.be/page/[number]/
   Contoh: https://anoboy.be/page/2/
   ```

9. **Genre/Tag**
   ```
   GET https://anoboy.be/#series-[id]
   Contoh: https://anoboy.be/#series-81 (Romance)
   ```

---

## 🛠️ Cara Kerja Scraping

### **HTML Structure yang Perlu Di-parse:**

Dari hasil scraping kamu, struktur HTML Anoboy:

```markdown
[TV Ep 4 Sub![Title](thumbnail)Title
---](url)
```

Pattern ini bisa di-parse dengan:
- **Regex** untuk extract data
- **Cheerio** (jQuery-like) untuk DOM parsing
- **Puppeteer** untuk JavaScript-rendered content

### **Data yang Bisa Diambil:**

1. **Anime Card:**
   - Title
   - Thumbnail URL
   - Episode number
   - Type (TV/ONA/Movie)
   - Status (Ongoing/Completed)
   - URL/Slug

2. **Anime Detail:**
   - Synopsis
   - Genres
   - Studio
   - Release date
   - Episode list

3. **Streaming Links:**
   - Video URLs
   - Quality options
   - Multiple servers

---

## 💻 Alternatif: Gunakan Mock Data Dulu

Untuk development cepat, gunakan mock data yang sudah saya buat di:
- `src/utils/anoboyParser.ts`
- `src/services/anoboyScraperApi.ts`

Mock data sudah include anime dari scraping kamu:
- Sanda
- One Punch Man 3
- Saigo ni Hitotsu dake...
- Nohara Hiroshi
- Dan lainnya

---

## ⚖️ Legal & Ethical Considerations

### **Yang BOLEH:**
✅ Scraping untuk personal use/learning
✅ Caching data dengan proper attribution
✅ Link back ke source asli
✅ Respect robots.txt

### **Yang TIDAK BOLEH:**
❌ Claim content sebagai milik sendiri
❌ Remove watermark/branding
❌ Monetize tanpa izin
❌ Overload server dengan excessive requests
❌ Bypass paywall/premium content

### **Best Practices:**
1. **Rate Limiting:** Max 1-2 request per second
2. **User-Agent:** Identifikasi scraper kamu
3. **Cache:** Simpan hasil scraping, jangan request berulang
4. **Attribution:** Sertakan "Data from Anoboy.be"
5. **Disclaimer:** Jelaskan ini adalah mirror/fan site

---

## 🔧 Setup di Project Kamu

Saat ini project DexAnime sudah setup dengan:

1. **Mock Data Mode:** 
   ```typescript
   const USE_MOCK_DATA = true; // di anoboyScraperApi.ts
   ```

2. **Real API Mode:**
   ```typescript
   const USE_SCRAPER = true; // di animeApi.ts
   ```

### **Untuk Development:**
- Gunakan mock data (sudah include 5 anime dari scraping kamu)
- Tidak perlu backend

### **Untuk Production:**
- Buat backend scraper (tutorial di atas)
- Set `USE_MOCK_DATA = false`
- Point `PROXY_URL` ke backend kamu

---

## 📚 Next Steps

1. **Test dengan Mock Data:**
   - Web sudah berfungsi dengan mock data
   - Cek di http://localhost:3000

2. **Buat Backend (Optional):**
   - Ikuti tutorial di atas
   - Deploy ke Heroku/Railway/Vercel

3. **Connect Backend ke Frontend:**
   - Update `PROXY_URL` di `anoboyScraperApi.ts`
   - Set `USE_MOCK_DATA = false`

4. **Deploy:**
   - Frontend: Vercel/Netlify
   - Backend: Railway/Heroku

---

## 🎓 Resources

- [Cheerio Documentation](https://cheerio.js.org/)
- [Puppeteer for Scraping](https://pptr.dev/)
- [Express.js Guide](https://expressjs.com/)
- [Web Scraping Ethics](https://www.scrapingbee.com/blog/web-scraping-ethics/)

---

**Catatan:** Mock data di project kamu sudah cukup untuk development dan demo. Untuk production dengan data real-time, kamu perlu setup backend scraper.
