import axios from 'axios';
import { Anime, AnimeDetail, Episode, WatchData, ApiResponse } from '@/types/anime';
import { ANOBOY_CONFIG, MOCK_ANIME_DATA, ScrapedAnime } from '@/utils/anoboyParser';

// Backend proxy URL (kamu perlu setup backend untuk scraping)
// Untuk development, kita gunakan mock data dulu
const USE_MOCK_DATA = true;
const PROXY_URL = 'http://localhost:5000/api'; // Backend proxy kamu nanti

class AnoboyScraperService {
  /**
   * Convert scraped data ke format Anime
   */
  private convertToAnime(scraped: ScrapedAnime): Anime {
    return {
      id: scraped.slug,
      title: scraped.title,
      slug: scraped.slug,
      thumbnail: scraped.thumbnail,
      status: scraped.status,
      type: scraped.type,
      totalEpisodes: scraped.episode ? parseInt(scraped.episode.match(/\d+/)?.[0] || '0') : undefined,
    };
  }

  /**
   * Get latest anime releases
   */
  async getLatestAnime(): Promise<ApiResponse<Anime[]>> {
    try {
      if (USE_MOCK_DATA) {
        // Gunakan mock data untuk development
        const animeList = MOCK_ANIME_DATA
          .filter(a => a.status === 'Ongoing')
          .map(a => this.convertToAnime(a));
        
        return {
          success: true,
          data: animeList,
        };
      }

      // Ketika backend ready, gunakan proxy
      const response = await axios.get(`${PROXY_URL}/latest`);
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: 'Gagal memuat anime terbaru',
      };
    }
  }

  /**
   * Get ongoing anime
   */
  async getOngoingAnime(page: number = 1): Promise<ApiResponse<Anime[]>> {
    try {
      if (USE_MOCK_DATA) {
        const animeList = MOCK_ANIME_DATA
          .filter(a => a.status === 'Ongoing')
          .map(a => this.convertToAnime(a))
          .slice((page - 1) * 20, page * 20);
        
        return {
          success: true,
          data: animeList,
        };
      }

      const response = await axios.get(`${PROXY_URL}/ongoing?page=${page}`);
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: 'Gagal memuat anime ongoing',
      };
    }
  }

  /**
   * Get completed anime
   */
  async getCompletedAnime(page: number = 1): Promise<ApiResponse<Anime[]>> {
    try {
      if (USE_MOCK_DATA) {
        const animeList = MOCK_ANIME_DATA
          .filter(a => a.status === 'Completed')
          .map(a => this.convertToAnime(a))
          .slice((page - 1) * 20, page * 20);
        
        return {
          success: true,
          data: animeList,
        };
      }

      const response = await axios.get(`${PROXY_URL}/completed?page=${page}`);
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: 'Gagal memuat anime completed',
      };
    }
  }

  /**
   * Get popular anime (dari recommendation section)
   */
  async getPopularAnime(): Promise<ApiResponse<Anime[]>> {
    try {
      if (USE_MOCK_DATA) {
        // Ambil semua anime dan sort by random untuk simulasi popular
        const animeList = [...MOCK_ANIME_DATA]
          .sort(() => Math.random() - 0.5)
          .slice(0, 12)
          .map(a => this.convertToAnime(a));
        
        return {
          success: true,
          data: animeList,
        };
      }

      const response = await axios.get(`${PROXY_URL}/popular`);
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: 'Gagal memuat anime populer',
      };
    }
  }

  /**
   * Get anime detail
   */
  async getAnimeDetail(slug: string): Promise<ApiResponse<AnimeDetail>> {
    try {
      if (USE_MOCK_DATA) {
        const scraped = MOCK_ANIME_DATA.find(a => a.slug === slug);
        if (!scraped) {
          return {
            success: false,
            error: 'Anime tidak ditemukan',
          };
        }

        const anime: AnimeDetail = {
          ...this.convertToAnime(scraped),
          description: `Sinopsis untuk ${scraped.title}. Anime ini berkisah tentang petualangan yang menarik dan penuh aksi.`,
          genres: ['Action', 'Adventure', 'Fantasy'],
          rating: '8.5',
          studio: 'Unknown Studio',
          episodeList: this.generateMockEpisodes(scraped),
        };

        return {
          success: true,
          data: anime,
        };
      }

      const response = await axios.get(`${PROXY_URL}/anime/${slug}`);
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: 'Gagal memuat detail anime',
      };
    }
  }

  /**
   * Get episode/watch data
   */
  async getEpisodeData(slug: string): Promise<ApiResponse<WatchData>> {
    try {
      if (USE_MOCK_DATA) {
        const scraped = MOCK_ANIME_DATA.find(a => a.slug === slug);
        if (!scraped) {
          return {
            success: false,
            error: 'Episode tidak ditemukan',
          };
        }

        const episodeNum = parseInt(scraped.episode?.match(/\d+/)?.[0] || '1');
        
        const watchData: WatchData = {
          anime: this.convertToAnime(scraped),
          episode: {
            id: slug,
            number: episodeNum,
            title: scraped.episode || '',
            slug: slug,
          },
          streamLinks: [
            {
              quality: '720p',
              url: `https://www.youtube.com/embed/dQw4w9WgXcQ`, // Demo URL
              provider: 'Server 1',
            },
            {
              quality: '480p',
              url: `https://www.youtube.com/embed/dQw4w9WgXcQ`,
              provider: 'Server 2',
            },
          ],
          prevEpisode: episodeNum > 1 ? {
            id: `${slug}-${episodeNum - 1}`,
            number: episodeNum - 1,
            title: `Episode ${episodeNum - 1}`,
            slug: slug.replace(`episode-${episodeNum}`, `episode-${episodeNum - 1}`),
          } : undefined,
          nextEpisode: {
            id: `${slug}-${episodeNum + 1}`,
            number: episodeNum + 1,
            title: `Episode ${episodeNum + 1}`,
            slug: slug.replace(`episode-${episodeNum}`, `episode-${episodeNum + 1}`),
          },
        };

        return {
          success: true,
          data: watchData,
        };
      }

      const response = await axios.get(`${PROXY_URL}/episode/${slug}`);
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: 'Gagal memuat episode',
      };
    }
  }

  /**
   * Search anime
   */
  async searchAnime(query: string, page: number = 1): Promise<ApiResponse<Anime[]>> {
    try {
      if (USE_MOCK_DATA) {
        const results = MOCK_ANIME_DATA
          .filter(a => a.title.toLowerCase().includes(query.toLowerCase()))
          .map(a => this.convertToAnime(a))
          .slice((page - 1) * 20, page * 20);

        return {
          success: true,
          data: results,
        };
      }

      const response = await axios.get(`${PROXY_URL}/search?q=${encodeURIComponent(query)}&page=${page}`);
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: 'Gagal melakukan pencarian',
      };
    }
  }

  /**
   * Generate mock episodes untuk testing
   */
  private generateMockEpisodes(scraped: ScrapedAnime): Episode[] {
    const totalEps = parseInt(scraped.episode?.match(/\d+/)?.[0] || '12');
    const episodes: Episode[] = [];

    for (let i = 1; i <= totalEps; i++) {
      episodes.push({
        id: `${scraped.slug}-episode-${i}`,
        number: i,
        title: `Episode ${i}`,
        slug: scraped.slug.replace(/episode-\d+/, `episode-${i}`),
      });
    }

    return episodes;
  }
}

// Export singleton
export const anoboyApi = new AnoboyScraperService();
export default anoboyApi;
