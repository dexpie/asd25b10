# 📂 DexAnime - Project Structure

## Overview
Website streaming anime dengan React, TypeScript, dan Tailwind CSS menggunakan Anoboy API.

## 🌳 Full Directory Tree

```
dexanime/
│
├── 📁 public/                  # Static assets
│   └── vite.svg               # App icon/logo
│
├── 📁 src/                     # Source code
│   │
│   ├── 📁 components/          # Reusable React components
│   │   ├── AnimeCard.tsx      # Card component untuk menampilkan anime
│   │   ├── Header.tsx         # Navigation header dengan search
│   │   ├── Footer.tsx         # Footer dengan informasi
│   │   ├── Loading.tsx        # Loading states & skeletons
│   │   └── ErrorMessage.tsx   # Error handling component
│   │
│   ├── 📁 pages/               # Page components (routes)
│   │   ├── HomePage.tsx       # Halaman beranda (ongoing/completed/popular)
│   │   ├── AnimeDetailPage.tsx # Detail anime dengan episode list
│   │   ├── WatchPage.tsx      # Video player untuk streaming
│   │   └── SearchPage.tsx     # Search results page
│   │
│   ├── 📁 services/            # API & External services
│   │   └── animeApi.ts        # Anoboy API service dengan error handling
│   │
│   ├── 📁 hooks/               # Custom React hooks
│   │   └── useApi.ts          # Hook untuk API calls dengan state management
│   │
│   ├── 📁 types/               # TypeScript type definitions
│   │   └── anime.ts           # Types untuk anime data structures
│   │
│   ├── App.tsx                # Main app component dengan routing
│   ├── main.tsx               # React entry point
│   ├── index.css              # Global CSS & Tailwind imports
│   └── vite-env.d.ts          # Vite type definitions
│
├── .env.example               # Environment variables template
├── .eslintrc.cjs              # ESLint configuration
├── .gitignore                 # Git ignore rules
├── index.html                 # HTML entry point
├── package.json               # Dependencies & scripts
├── postcss.config.js          # PostCSS configuration
├── README.md                  # Main documentation
├── QUICKSTART.md              # Quick start guide
├── tailwind.config.js         # Tailwind CSS configuration
├── tsconfig.json              # TypeScript configuration
├── tsconfig.node.json         # TypeScript config for Node
└── vite.config.ts             # Vite build configuration
```

## 📄 File Descriptions

### Core Files

#### `src/App.tsx`
- Main application component
- Configures React Router
- Layout wrapper (Header + Content + Footer)

#### `src/main.tsx`
- Application entry point
- React root rendering
- Imports global CSS

#### `src/index.css`
- Tailwind CSS directives
- Global styles
- Custom scrollbar & utilities

### Components (`src/components/`)

#### `AnimeCard.tsx`
- Props: `{ anime: Anime }`
- Features:
  - Thumbnail dengan hover effects
  - Status badge (ongoing/completed)
  - Rating & genre tags
  - Link ke detail page

#### `Header.tsx`
- Features:
  - Logo & branding
  - Navigation menu (Desktop & Mobile)
  - Search bar dengan autocomplete
  - Responsive hamburger menu

#### `Footer.tsx`
- Features:
  - Brand info
  - Quick links
  - Copyright & disclaimer

#### `Loading.tsx`
- Exports: `Loading`, `LoadingGrid`
- Features:
  - Animated spinner
  - Skeleton cards untuk grid

#### `ErrorMessage.tsx`
- Props: `{ message?: string, onRetry?: () => void }`
- Features:
  - Error icon & message
  - Retry button

### Pages (`src/pages/`)

#### `HomePage.tsx`
- Route: `/`
- Features:
  - Hero section
  - Tabs: Ongoing, Completed, Popular
  - Grid anime cards
  - Auto-fetch on tab change

#### `AnimeDetailPage.tsx`
- Route: `/anime/:slug`
- Features:
  - Large backdrop image
  - Anime information (genres, status, rating, etc)
  - Synopsis
  - Episode list grid

#### `WatchPage.tsx`
- Route: `/watch/:slug`
- Features:
  - Video player (iframe)
  - Multiple server selection
  - Previous/Next episode navigation
  - Breadcrumb navigation

#### `SearchPage.tsx`
- Route: `/search?q=query`
- Features:
  - Search input
  - Search results grid
  - Empty state handling
  - Query from URL params

### Services (`src/services/`)

#### `animeApi.ts`
- Axios instance dengan interceptors
- Methods:
  - `getOngoingAnime(page)` - Anime ongoing
  - `getCompletedAnime(page)` - Anime completed
  - `getPopularAnime()` - Popular anime
  - `getAnimeDetail(slug)` - Detail anime
  - `getEpisodeData(slug)` - Episode streaming data
  - `searchAnime(query, page)` - Search results
  - `getAnimeByGenre(genre, page)` - Filter by genre
- Error handling otomatis
- Response standardization

### Hooks (`src/hooks/`)

#### `useApi.ts`
- Generic hook untuk API calls
- Returns: `{ data, loading, error, refetch }`
- Auto-fetch on mount (optional)
- Error handling built-in

### Types (`src/types/`)

#### `anime.ts`
Type definitions:
- `Anime` - Basic anime info
- `AnimeDetail` - Extended anime data
- `Episode` - Episode info
- `WatchData` - Streaming data
- `SearchResult` - Search response
- `StreamLink` - Video source
- `ApiResponse<T>` - Generic API response
- `ApiError` - Error structure

## 🎨 Styling System

### Tailwind Configuration
- Custom colors (primary blue palette)
- Custom animations (fade-in, slide-up)
- Responsive breakpoints
- Extended utilities

### Color Scheme
- **Primary**: Blue (#0ea5e9 - #0c4a6e)
- **Background**: Dark gray (#111827, #1f2937, #374151)
- **Text**: White, Gray variants
- **Accent**: Primary for CTAs

## 🔧 Configuration Files

### `vite.config.ts`
- React plugin
- Path alias: `@/*` → `./src/*`
- Dev server: port 3000, auto-open

### `tailwind.config.js`
- Content paths for PurgeCSS
- Theme extensions
- Custom animations

### `tsconfig.json`
- Strict mode enabled
- Path mappings
- ES2020 target
- React JSX transform

## 📦 Dependencies

### Production
- `react` & `react-dom` - UI library
- `react-router-dom` - Routing
- `axios` - HTTP client

### Development
- `vite` - Build tool
- `typescript` - Type checking
- `tailwindcss` - CSS framework
- `@vitejs/plugin-react` - Vite React plugin
- `eslint` - Code linting
- `autoprefixer` & `postcss` - CSS processing

## 🚀 Scripts

```json
{
  "dev": "vite",              // Start dev server
  "build": "tsc && vite build", // Build for production
  "preview": "vite preview",   // Preview production build
  "lint": "eslint ..."         // Lint code
}
```

## 🌐 Routes Map

| Route | Component | Description |
|-------|-----------|-------------|
| `/` | HomePage | Beranda dengan tabs |
| `/ongoing` | HomePage | Anime ongoing |
| `/completed` | HomePage | Anime completed |
| `/anime/:slug` | AnimeDetailPage | Detail anime |
| `/watch/:slug` | WatchPage | Streaming page |
| `/search?q=...` | SearchPage | Search results |

## 🔑 Key Features Implementation

### 1. **API Integration**
- Centralized service layer
- Automatic error handling
- Response type safety
- Request interceptors

### 2. **State Management**
- Custom hooks pattern
- Local component state
- No external state library needed

### 3. **Responsive Design**
- Mobile-first approach
- Breakpoint-based layouts
- Touch-friendly UI

### 4. **Performance**
- Lazy loading images
- Code splitting by route
- Optimized bundle size

### 5. **Developer Experience**
- TypeScript for type safety
- ESLint for code quality
- Hot Module Replacement (HMR)
- Path aliases for clean imports

## 📝 Coding Conventions

1. **File Naming**: PascalCase for components (`AnimeCard.tsx`)
2. **Function Components**: Use arrow functions
3. **Props Typing**: Always define interfaces
4. **Imports**: Group by external → internal → local
5. **Styling**: Tailwind utility classes, no CSS modules

## 🎯 Future Improvements

- [ ] Add unit tests (Vitest)
- [ ] Implement PWA (Service Worker)
- [ ] Add skeleton loading
- [ ] Cache API responses
- [ ] Infinite scroll pagination
- [ ] Bookmark/Favorite system
- [ ] User preferences (theme, quality)
- [ ] SEO optimization
- [ ] Analytics integration

---

**Last Updated**: October 26, 2025
**Version**: 1.0.0
