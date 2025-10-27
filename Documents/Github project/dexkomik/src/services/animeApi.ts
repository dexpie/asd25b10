import axios, { AxiosError, AxiosInstance } from 'axios';
import { 
  Anime, 
  AnimeDetail, 
  SearchResult, 
  WatchData, 
  ApiResponse,
  ApiError 
} from '@/types/anime';
// Import scraper service sebagai fallback
import { anoboyApi as scraperApi } from './anoboyScraperApi';

// Base API Configuration
// Backend deployed on Vercel
const USE_SCRAPER = false; // Use Vercel backend API
const API_BASE_URL = 'https://dexkomikbackend-p26w.vercel.app/api'; // DexAnime Backend API
const API_TIMEOUT = 15000; // 15 seconds for scraping

class AnimeApiService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      timeout: API_TIMEOUT,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Response interceptor untuk error handling
    this.api.interceptors.response.use(
      (response) => response,
      (error: AxiosError) => {
        return Promise.reject(this.handleError(error));
      }
    );
  }

  // Error handler
  private handleError(error: AxiosError): ApiError {
    if (error.response) {
      // Server responded with error
      const data = error.response.data as { message?: string };
      return {
        message: data?.message || 'Terjadi kesalahan pada server',
        status: error.response.status,
        code: error.code,
      };
    } else if (error.request) {
      // Request made but no response
      return {
        message: 'Tidak dapat terhubung ke server. Periksa koneksi internet Anda.',
        code: error.code,
      };
    } else {
      // Something else happened
      return {
        message: error.message || 'Terjadi kesalahan yang tidak diketahui',
        code: error.code,
      };
    }
  }

  // Get ongoing anime (anime yang sedang tayang)
  async getOngoingAnime(page: number = 1): Promise<ApiResponse<Anime[]>> {
    // Gunakan scraper jika enabled
    if (USE_SCRAPER) {
      return scraperApi.getOngoingAnime(page);
    }

    try {
      const response = await this.api.get(`/ongoing?page=${page}`);
      return {
        success: true,
        data: response.data.data || response.data.results || response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: (error as ApiError).message,
      };
    }
  }

  // Get completed anime
  async getCompletedAnime(page: number = 1): Promise<ApiResponse<Anime[]>> {
    // Gunakan scraper jika enabled
    if (USE_SCRAPER) {
      return scraperApi.getCompletedAnime(page);
    }

    try {
      const response = await this.api.get(`/completed?page=${page}`);
      return {
        success: true,
        data: response.data.results || response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: (error as ApiError).message,
      };
    }
  }

  // Get anime detail
  async getAnimeDetail(slug: string): Promise<ApiResponse<AnimeDetail>> {
    // Gunakan scraper jika enabled
    if (USE_SCRAPER) {
      return scraperApi.getAnimeDetail(slug);
    }

    try {
      const response = await this.api.get(`/anime/${slug}`);
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: (error as ApiError).message,
      };
    }
  }

  // Get episode/watch data
  async getEpisodeData(slug: string): Promise<ApiResponse<WatchData>> {
    // Gunakan scraper jika enabled
    if (USE_SCRAPER) {
      return scraperApi.getEpisodeData(slug);
    }

    try {
      const response = await this.api.get(`/episode/${slug}`);
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: (error as ApiError).message,
      };
    }
  }

  // Search anime
  async searchAnime(query: string, page: number = 1): Promise<ApiResponse<SearchResult>> {
    // Gunakan scraper jika enabled
    if (USE_SCRAPER) {
      const result = await scraperApi.searchAnime(query, page);
      if (result.success && result.data) {
        return {
          success: true,
          data: {
            results: result.data,
            currentPage: page,
            totalPages: 1,
            totalResults: result.data.length,
          },
        };
      }
      return {
        success: false,
        error: result.error,
      };
    }

    try {
      const response = await this.api.get(`/search?q=${encodeURIComponent(query)}&page=${page}`);
      return {
        success: true,
        data: {
          results: response.data.results || response.data,
          currentPage: page,
          totalPages: response.data.totalPages || 1,
          totalResults: response.data.total || response.data.results?.length || 0,
        },
      };
    } catch (error) {
      return {
        success: false,
        error: (error as ApiError).message,
      };
    }
  }

  // Get popular/trending anime
  async getPopularAnime(): Promise<ApiResponse<Anime[]>> {
    // Use latest endpoint as popular
    if (USE_SCRAPER) {
      return scraperApi.getPopularAnime();
    }

    try {
      const response = await this.api.get('/latest');
      return {
        success: true,
        data: response.data.data || response.data.results || response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: (error as ApiError).message,
      };
    }
  }

  // Get anime by genre
  async getAnimeByGenre(genre: string, page: number = 1): Promise<ApiResponse<Anime[]>> {
    try {
      const response = await this.api.get(`/genre/${genre}?page=${page}`);
      return {
        success: true,
        data: response.data.results || response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: (error as ApiError).message,
      };
    }
  }
}

// Export singleton instance
export const animeApi = new AnimeApiService();
export default animeApi;
