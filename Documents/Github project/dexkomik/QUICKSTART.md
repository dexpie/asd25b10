# 🚀 Quick Start Guide - DexAnime

## Langkah-langkah untuk Memulai

### 1. Install Dependencies
```bash
npm install
```

### 2. Jalankan Development Server
```bash
npm run dev
```

Server akan berjalan di: **http://localhost:3000**

### 3. Build untuk Production
```bash
npm run build
```

### 4. Preview Production Build
```bash
npm run preview
```

## 📋 Checklist Fitur

✅ **Halaman Beranda** - Tampilan anime ongoing, completed, dan populer
✅ **Detail Anime** - Informasi lengkap dan daftar episode
✅ **Watch Page** - Streaming episode dengan multiple server
✅ **Search** - Pencarian anime
✅ **Responsive Design** - Mobile, tablet, dan desktop
✅ **Error Handling** - Penanganan error yang baik
✅ **Loading States** - Loading indicators yang smooth
✅ **TypeScript** - Full type safety
✅ **Tailwind CSS** - Modern styling
✅ **React Router** - Client-side routing

## 🎨 Customization

### Ganti Warna Tema
Edit file `tailwind.config.js`:
```javascript
colors: {
  primary: {
    500: '#your-color',
    // ... dst
  }
}
```

### Ganti API Endpoint
Edit file `src/services/animeApi.ts`:
```typescript
const API_BASE_URL = 'your-api-url';
```

## 📱 Page Routes

- `/` - Halaman beranda
- `/ongoing` - Anime ongoing
- `/completed` - Anime completed  
- `/anime/:slug` - Detail anime
- `/watch/:slug` - Watch episode
- `/search?q=query` - Search results

## 🛠️ Tech Stack

- **React 18** + **TypeScript**
- **Vite** (Build tool)
- **Tailwind CSS** (Styling)
- **React Router** (Routing)
- **Axios** (HTTP Client)

## 💡 Tips

1. Server dev akan auto-reload saat ada perubahan code
2. Check console browser untuk debug
3. Tailwind IntelliSense extension recommended untuk VS Code
4. ESLint akan check code quality

## 🎯 Next Steps

- [ ] Tambahkan bookmark/favorite feature
- [ ] Implementasi infinite scroll
- [ ] Tambahkan dark/light mode toggle
- [ ] Cache API responses
- [ ] Add PWA support
- [ ] Improve SEO

---

**Happy Coding! 🎉**
