// Scraper untuk Anoboy.be
// Karena ini website scraping, kita perlu backend/proxy untuk handle CORS

export interface ScrapedAnime {
  title: string;
  slug: string;
  thumbnail: string;
  episode?: string;
  type?: string;
  status?: string;
  url: string;
}

export interface ScraperConfig {
  baseUrl: string;
  endpoints: {
    home: string;
    ongoing: string;
    completed: string;
    search: string;
    anime: string;
  };
}

// Konfigurasi base URL
export const ANOBOY_CONFIG: ScraperConfig = {
  baseUrl: 'https://anoboy.be',
  endpoints: {
    home: '/',
    ongoing: '/anime/?status=&type=&order=update',
    completed: '/anime/?status=completed',
    search: '/?s=',
    anime: '/anime/',
  },
};

// Parser HTML helper functions
export class AnoboyParser {
  /**
   * Parse HTML dari Latest Release section
   */
  static parseLatestRelease(html: string): ScrapedAnime[] {
    const animeList: ScrapedAnime[] = [];
    
    // Regex untuk extract data dari HTML
    // Format: [TV Ep X Sub![Title](thumbnail)Title\n---](url)
    const animeRegex = /\[([^\]]+)\s+Ep\s+(\d+)\s+Sub!\[(.*?)\]\((.*?)\s+"(.*?)"\)(.*?)\n-+\]\((.*?)\s+"/g;
    
    let match;
    while ((match = animeRegex.exec(html)) !== null) {
      const [, type, episode, title, thumbnail, , fullTitle, url] = match;
      
      animeList.push({
        title: fullTitle.trim() || title.trim(),
        slug: url.split('/').filter(Boolean).pop() || '',
        thumbnail: thumbnail,
        episode: `Episode ${episode}`,
        type: type.trim(),
        status: 'Ongoing',
        url: url,
      });
    }
    
    return animeList;
  }

  /**
   * Parse HTML dari Recommendation section
   */
  static parseRecommendation(html: string): ScrapedAnime[] {
    const animeList: ScrapedAnime[] = [];
    
    // Similar parsing untuk recommendation section
    const animeRegex = /\[([^\]]*)\s*(Ongoing|Completed)?\s*Sub!\[\]\((.*?)\s+"(.*?)"\)(.*?)\n-+\]\((.*?)\s+"/g;
    
    let match;
    while ((match = animeRegex.exec(html)) !== null) {
      const [, type, status, thumbnail, title, fullTitle, url] = match;
      
      animeList.push({
        title: fullTitle.trim() || title.trim(),
        slug: url.split('/').filter(Boolean).pop() || '',
        thumbnail: thumbnail,
        type: type.trim() || 'TV',
        status: status || 'Ongoing',
        url: url,
      });
    }
    
    return animeList;
  }

  /**
   * Extract slug dari URL
   */
  static extractSlug(url: string): string {
    const parts = url.split('/').filter(Boolean);
    return parts[parts.length - 1] || '';
  }

  /**
   * Clean title dari subtitle Indonesia
   */
  static cleanTitle(title: string): string {
    return title
      .replace(/\s+Subtitle\s+Indonesia$/i, '')
      .replace(/\s+Sub\s+Indo$/i, '')
      .replace(/Episode\s+\d+\s+/i, '')
      .trim();
  }
}

// Mock data berdasarkan scraping kamu dari Anoboy.be
// Data ini diambil dari: https://anoboy.be/ (26 Oktober 2025)
export const MOCK_ANIME_DATA: ScrapedAnime[] = [
  {
    title: 'Sanda',
    slug: 'sanda-episode-4-subtitle-indonesia',
    thumbnail: 'https://anoboy.be/wp-content/uploads/2025/10/1759515456-8096-151767.jpg',
    episode: 'Episode 4',
    type: 'TV',
    status: 'Ongoing',
    url: 'https://anoboy.be/sanda-episode-4-subtitle-indonesia/',
  },
  {
    title: 'One Punch Man 3',
    slug: 'one-punch-man-3-episode-3-subtitle-indonesia',
    thumbnail: 'https://anoboy.be/wp-content/uploads/2025/10/1759685040-9198-148347.jpg',
    episode: 'Episode 3',
    type: 'TV',
    status: 'Ongoing',
    url: 'https://anoboy.be/one-punch-man-3-episode-3-subtitle-indonesia/',
  },
  {
    title: 'Saigo ni Hitotsu dake Onegai shitemo Yoroshii deshou ka',
    slug: 'saigo-ni-hitotsu-dake-onegai-shitemo-yoroshii-deshou-ka-episode-5-subtitle-indonesia',
    thumbnail: 'https://anoboy.be/wp-content/uploads/2025/10/1759510108-4674-151754.jpg',
    episode: 'Episode 5',
    type: 'TV',
    status: 'Ongoing',
    url: 'https://anoboy.be/saigo-ni-hitotsu-dake-onegai-shitemo-yoroshii-deshou-ka-episode-5-subtitle-indonesia/',
  },
  {
    title: 'Nohara Hiroshi: Hiru Meshi no Ryuugi',
    slug: 'nohara-hiroshi-hiru-meshi-no-ryuugi-episode-4-subtitle-indonesia',
    thumbnail: 'https://anoboy.be/wp-content/uploads/2025/10/1759508420-8843-148429.jpg',
    episode: 'Episode 4',
    type: 'TV',
    status: 'Ongoing',
    url: 'https://anoboy.be/nohara-hiroshi-hiru-meshi-no-ryuugi-episode-4-subtitle-indonesia/',
  },
  {
    title: 'Watari-kun no xx ga Houkai Sunzen',
    slug: 'watari-kun-no-xx-ga-houkai-sunzen-episode-17-subtitle-indonesia',
    thumbnail: 'https://anoboy.be/wp-content/uploads/2025/07/1751628561-3722-150545.jpg',
    episode: 'Episode 17',
    type: 'TV',
    status: 'Completed',
    url: 'https://anoboy.be/watari-kun-no-xx-ga-houkai-sunzen-episode-17-subtitle-indonesia/',
  },
  {
    title: 'Shabake',
    slug: 'shabake-episode-4-subtitle-indonesia',
    thumbnail: 'https://anoboy.be/wp-content/uploads/2025/10/1759509122-7179-152179.jpg',
    episode: 'Episode 4',
    type: 'TV',
    status: 'Ongoing',
    url: 'https://anoboy.be/shabake-episode-4-subtitle-indonesia/',
  },
  {
    title: 'Tougen Anki',
    slug: 'tougen-anki-episode-15-subtitle-indonesia',
    thumbnail: 'https://anoboy.be/wp-content/uploads/2025/07/1752251634-4249-150666.jpg',
    episode: 'Episode 15',
    type: 'TV',
    status: 'Ongoing',
    url: 'https://anoboy.be/tougen-anki-episode-15-subtitle-indonesia/',
  },
  {
    title: 'Mugen Gacha LV9999',
    slug: 'mugen-gacha-lv9999-episode-4-subtitle-indonesia',
    thumbnail: 'https://anoboy.be/wp-content/uploads/2025/10/1759505124-3628-151246.jpg',
    episode: 'Episode 4',
    type: 'TV',
    status: 'Ongoing',
    url: 'https://anoboy.be/mugen-gacha-lv9999-episode-4-subtitle-indonesia/',
  },
  {
    title: 'Potion, Wagami wo Tasukeru',
    slug: 'potion-wagami-wo-tasukeru-episode-4-subtitle-indonesia',
    thumbnail: 'https://anoboy.be/wp-content/uploads/2025/10/1759427584-6195-151227.jpg',
    episode: 'Episode 4',
    type: 'TV',
    status: 'Ongoing',
    url: 'https://anoboy.be/potion-wagami-wo-tasukeru-episode-4-subtitle-indonesia/',
  },
  {
    title: 'Futari Solo Camp',
    slug: 'futari-solo-camp-episode-16-subtitle-indonesia',
    thumbnail: 'https://anoboy.be/wp-content/uploads/2025/07/1752169609-7976-150649.jpg',
    episode: 'Episode 16',
    type: 'TV',
    status: 'Ongoing',
    url: 'https://anoboy.be/futari-solo-camp-episode-16-subtitle-indonesia/',
  },
  {
    title: 'Akujiki Reijou to Kyouketsu Koushaku',
    slug: 'akujiki-reijou-to-kyouketsu-koushaku-episode-4-subtitle-indonesia',
    thumbnail: 'https://anoboy.be/wp-content/uploads/2025/10/1759425631-1528-152012.jpg',
    episode: 'Episode 4',
    type: 'TV',
    status: 'Ongoing',
    url: 'https://anoboy.be/akujiki-reijou-to-kyouketsu-koushaku-episode-4-subtitle-indonesia/',
  },
  {
    title: 'Taiyou yori mo Mabushii Hoshi',
    slug: 'taiyou-yori-mo-mabushii-hoshi-episode-4-subtitle-indonesia',
    thumbnail: 'https://anoboy.be/wp-content/uploads/2025/10/1759424008-1236-149410.jpg',
    episode: 'Episode 4',
    type: 'TV',
    status: 'Ongoing',
    url: 'https://anoboy.be/taiyou-yori-mo-mabushii-hoshi-episode-4-subtitle-indonesia/',
  },
  {
    title: 'Towa no Yuugure',
    slug: 'towa-no-yuugure-episode-4-subtitle-indonesia',
    thumbnail: 'https://anoboy.be/wp-content/uploads/2025/09/1758825269-3719-151734.jpg',
    episode: 'Episode 4',
    type: 'TV',
    status: 'Ongoing',
    url: 'https://anoboy.be/towa-no-yuugure-episode-4-subtitle-indonesia/',
  },
  {
    title: 'Watashi wo Tabetai, Hitodenashi',
    slug: 'watashi-wo-tabetai-hitodenashi-episode-4-subtitle-indonesia',
    thumbnail: 'https://anoboy.be/wp-content/uploads/2025/10/1759417683-6393-151406.jpg',
    episode: 'Episode 4',
    type: 'TV',
    status: 'Ongoing',
    url: 'https://anoboy.be/watashi-wo-tabetai-hitodenashi-episode-4-subtitle-indonesia/',
  },
  {
    title: 'Bukiyou na Senpai',
    slug: 'bukiyou-na-senpai-episode-4-subtitle-indonesia',
    thumbnail: 'https://anoboy.be/wp-content/uploads/2025/10/1759416129-2408-152233.jpg',
    episode: 'Episode 4',
    type: 'TV',
    status: 'Ongoing',
    url: 'https://anoboy.be/bukiyou-na-senpai-episode-4-subtitle-indonesia/',
  },
  {
    title: 'Heika Watashi wo Wasurete Kudasai',
    slug: 'heika-watashi-wo-wasurete-kudasai-episode-3-subtitle-indonesia',
    thumbnail: 'https://anoboy.be/wp-content/uploads/2025/10/1760020075-9183-151926.jpg',
    episode: 'Episode 3',
    type: 'TV',
    status: 'Ongoing',
    url: 'https://anoboy.be/heika-watashi-wo-wasurete-kudasai-episode-3-subtitle-indonesia/',
  },
  {
    title: 'Wandance',
    slug: 'wandance-episode-3-subtitle-indonesia',
    thumbnail: 'https://anoboy.be/wp-content/uploads/2025/10/1759940433-8031-151524.jpg',
    episode: 'Episode 3',
    type: 'TV',
    status: 'Ongoing',
    url: 'https://anoboy.be/wandance-episode-3-subtitle-indonesia/',
  },
  {
    title: 'Ninja to Gokudou',
    slug: 'ninja-to-gokudou-episode-3-subtitle-indonesia',
    thumbnail: 'https://anoboy.be/wp-content/uploads/2025/10/1759860456-2679-152147.jpg',
    episode: 'Episode 3',
    type: 'TV',
    status: 'Ongoing',
    url: 'https://anoboy.be/ninja-to-gokudou-episode-3-subtitle-indonesia/',
  },
  {
    title: 'Kaijuu 8-gou 2nd Season',
    slug: 'kaijuu-8-gou-2nd-season-episode-11-subtitle-indonesia',
    thumbnail: 'https://anoboy.be/wp-content/uploads/2025/07/1751729907-6007-150344.jpg',
    episode: 'Episode 11',
    type: 'TV',
    status: 'Completed',
    url: 'https://anoboy.be/kaijuu-8-gou-2nd-season-episode-11-subtitle-indonesia/',
  },
  {
    title: 'Yofukashi no Uta Season 2',
    slug: 'yofukashi-no-uta-season-2-episode-12-subtitle-indonesia',
    thumbnail: 'https://anoboy.be/wp-content/uploads/2025/07/1751628497-6071-148453.jpg',
    episode: 'Episode 12',
    type: 'TV',
    status: 'Completed',
    url: 'https://anoboy.be/yofukashi-no-uta-season-2-episode-12-subtitle-indonesia/',
  },
];
