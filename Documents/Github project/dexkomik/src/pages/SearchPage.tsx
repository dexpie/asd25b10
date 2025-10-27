import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { animeApi } from '@/services/animeApi';
import { Anime } from '@/types/anime';
import AnimeCard from '@/components/AnimeCard';
import Loading, { LoadingGrid } from '@/components/Loading';
import ErrorMessage from '@/components/ErrorMessage';

export default function SearchPage() {
  const [searchParams] = useSearchParams();
  const query = searchParams.get('q') || '';
  
  const [searchResults, setSearchResults] = useState<Anime[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState(query);

  useEffect(() => {
    if (query) {
      performSearch(query);
    }
  }, [query]);

  const performSearch = async (searchTerm: string) => {
    if (!searchTerm.trim()) return;
    
    setLoading(true);
    setError(null);
    
    const response = await animeApi.searchAnime(searchTerm);
    
    if (response.success && response.data) {
      setSearchResults(response.data.results);
    } else {
      setError(response.error || 'Gagal melakukan pencarian');
    }
    
    setLoading(false);
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      window.history.pushState({}, '', `/search?q=${encodeURIComponent(searchQuery.trim())}`);
      performSearch(searchQuery.trim());
    }
  };

  return (
    <div className="min-h-screen">
      <div className="container mx-auto px-4 py-8">
        {/* Search Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-white mb-6">Pencarian Anime</h1>
          
          <form onSubmit={handleSearch} className="max-w-2xl">
            <div className="relative">
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Cari anime berdasarkan judul..."
                className="w-full bg-gray-800 text-white pl-12 pr-4 py-4 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 text-lg"
                autoFocus
              />
              <svg 
                className="absolute left-4 top-1/2 -translate-y-1/2 w-6 h-6 text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <button
                type="submit"
                className="absolute right-2 top-1/2 -translate-y-1/2 bg-primary-600 hover:bg-primary-700 text-white px-6 py-2 rounded-lg transition-colors font-medium"
              >
                Cari
              </button>
            </div>
          </form>
        </div>

        {/* Search Results */}
        {query && (
          <div className="mb-6">
            <p className="text-gray-400">
              Hasil pencarian untuk: <span className="text-white font-semibold">"{query}"</span>
              {searchResults.length > 0 && !loading && (
                <span className="text-primary-500 ml-2">
                  ({searchResults.length} anime ditemukan)
                </span>
              )}
            </p>
          </div>
        )}

        {/* Loading State */}
        {loading && <LoadingGrid />}

        {/* Error State */}
        {error && !loading && (
          <ErrorMessage message={error} onRetry={() => performSearch(query)} />
        )}

        {/* Results */}
        {!loading && !error && searchResults.length > 0 && (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
            {searchResults.map((anime) => (
              <AnimeCard key={anime.id} anime={anime} />
            ))}
          </div>
        )}

        {/* No Results */}
        {!loading && !error && searchResults.length === 0 && query && (
          <div className="text-center py-12">
            <div className="w-24 h-24 bg-gray-800 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg className="w-12 h-12 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
            <h3 className="text-white text-xl font-semibold mb-2">Tidak Ada Hasil</h3>
            <p className="text-gray-400">
              Tidak ditemukan anime dengan kata kunci "{query}". <br />
              Coba kata kunci lain atau periksa ejaan Anda.
            </p>
          </div>
        )}

        {/* Initial State */}
        {!loading && !error && !query && (
          <div className="text-center py-12">
            <div className="w-24 h-24 bg-gray-800 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg className="w-12 h-12 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
            <h3 className="text-white text-xl font-semibold mb-2">Mulai Pencarian</h3>
            <p className="text-gray-400">
              Masukkan judul anime yang ingin Anda cari
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
