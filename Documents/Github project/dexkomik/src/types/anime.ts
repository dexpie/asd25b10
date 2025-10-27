// Types untuk data anime dari Anoboy API
export interface Anime {
  id: string;
  title: string;
  slug: string;
  thumbnail: string;
  synopsis?: string;
  status?: string;
  rating?: string;
  genres?: string[];
  released?: string;
  type?: string;
  episodes?: Episode[];
  totalEpisodes?: number;
}

export interface Episode {
  id: string;
  number: number;
  title: string;
  slug: string;
  releaseDate?: string;
  thumbnail?: string;
}

export interface AnimeDetail extends Anime {
  description: string;
  studio?: string;
  duration?: string;
  score?: number;
  episodeList: Episode[];
}

export interface StreamLink {
  quality: string;
  url: string;
  provider: string;
}

export interface WatchData {
  anime: Anime;
  episode: Episode;
  streamLinks: StreamLink[];
  prevEpisode?: Episode;
  nextEpisode?: Episode;
}

export interface SearchResult {
  results: Anime[];
  currentPage: number;
  totalPages: number;
  totalResults: number;
}

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
}

export interface ApiError {
  message: string;
  status?: number;
  code?: string;
}
