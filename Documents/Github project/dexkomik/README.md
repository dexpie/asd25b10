# 🎬 DexAnime - Website Streaming Anime

Website streaming anime modern dengan React, TypeScript, dan Tailwind CSS yang menggunakan Anoboy API.

## ✨ Fitur

- 🎥 **Streaming Anime** - Nonton anime dengan subtitle Indonesia
- 🔥 **Anime Ongoing** - Update anime yang sedang tayang
- ✅ **Anime Completed** - Koleksi anime yang sudah selesai
- ⭐ **Anime Populer** - Daftar anime populer
- 🔍 **Pencarian** - Cari anime berdasarkan judul
- 📱 **Responsive Design** - Tampilan optimal di semua perangkat
- 🎨 **Modern UI/UX** - Interface yang clean dan mudah digunakan
- ⚡ **Fast Loading** - Performance optimal dengan Vite
- 🎯 **TypeScript** - Type-safe development

## 🛠️ Teknologi

- **React 18** - UI Library
- **TypeScript** - Type Safety
- **Vite** - Build Tool & Dev Server
- **Tailwind CSS** - Styling
- **React Router** - Routing
- **Axios** - HTTP Client
- **Anoboy API** - Data Source

## 📦 Instalasi

1. **Clone repository**
   ```bash
   git clone https://github.com/yourusername/dexanime.git
   cd dexanime
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Jalankan development server**
   ```bash
   npm run dev
   ```

4. **Buka browser**
   ```
   http://localhost:3000
   ```

## 🚀 Build Production

```bash
npm run build
```

Preview build:
```bash
npm run preview
```

## 📁 Struktur Folder

```
dexanime/
├── src/
│   ├── components/         # Reusable components
│   │   ├── AnimeCard.tsx
│   │   ├── Header.tsx
│   │   ├── Footer.tsx
│   │   ├── Loading.tsx
│   │   └── ErrorMessage.tsx
│   ├── pages/             # Page components
│   │   ├── HomePage.tsx
│   │   ├── AnimeDetailPage.tsx
│   │   ├── WatchPage.tsx
│   │   └── SearchPage.tsx
│   ├── services/          # API services
│   │   └── animeApi.ts
│   ├── hooks/             # Custom hooks
│   │   └── useApi.ts
│   ├── types/             # TypeScript types
│   │   └── anime.ts
│   ├── App.tsx            # Main app component
│   ├── main.tsx           # Entry point
│   └── index.css          # Global styles
├── public/                # Static assets
├── index.html
├── package.json
├── tsconfig.json
├── tailwind.config.js
└── vite.config.ts
```

## 🎯 Fitur Utama

### 1. **Halaman Beranda**
- Daftar anime ongoing, completed, dan populer
- Tab navigation untuk switching konten
- Grid layout responsif

### 2. **Detail Anime**
- Informasi lengkap anime
- Daftar episode
- Synopsis dan metadata

### 3. **Halaman Watch**
- Video player untuk streaming
- Multiple server options
- Navigation ke episode sebelumnya/selanjutnya

### 4. **Pencarian**
- Search anime berdasarkan judul
- Real-time search results
- Filter dan sorting

## 🔧 Konfigurasi

### API Configuration

File: `src/services/animeApi.ts`

```typescript
const API_BASE_URL = 'https://api-anoboy.vercel.app/api';
const API_TIMEOUT = 10000; // 10 seconds
```

### Tailwind Configuration

File: `tailwind.config.js`

Custom colors, animations, dan utilities sudah dikonfigurasi untuk tema anime.

## 📱 Responsive Breakpoints

- **Mobile**: < 640px
- **Tablet**: 640px - 1024px
- **Desktop**: > 1024px

## 🎨 Color Palette

- **Primary**: Blue tones (#0ea5e9)
- **Background**: Dark gray (#111827, #1f2937)
- **Text**: White & Gray variants

## 🌟 Custom Hooks

### `useApi`

Custom hook untuk API calls dengan loading dan error states:

```typescript
const { data, loading, error, refetch } = useApi(() => animeApi.getOngoingAnime(1));
```

## 📝 Scripts

- `npm run dev` - Start development server
- `npm run build` - Build production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is open source and available under the MIT License.

## 🙏 Credits

- **Anoboy API** - Data source
- **React Team** - React library
- **Tailwind Labs** - Tailwind CSS
- **Vite Team** - Build tool

## 📞 Support

Jika ada pertanyaan atau masalah, silakan buat issue di repository ini.

---

**Made with ❤️ for anime lovers in Indonesia**
