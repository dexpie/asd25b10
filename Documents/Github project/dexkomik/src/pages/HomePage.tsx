import { useState } from 'react';
import { useApi } from '@/hooks/useApi';
import { animeApi } from '@/services/animeApi';
import AnimeCard from '@/components/AnimeCard';
import Loading, { LoadingGrid } from '@/components/Loading';
import ErrorMessage from '@/components/ErrorMessage';
import { Anime } from '@/types/anime';

export default function HomePage() {
  const [activeTab, setActiveTab] = useState<'ongoing' | 'completed' | 'popular'>('ongoing');
  
  const { 
    data: ongoingAnime, 
    loading: loadingOngoing, 
    error: errorOngoing,
    refetch: refetchOngoing 
  } = useApi(() => animeApi.getOngoingAnime(1), { autoFetch: activeTab === 'ongoing' });

  const { 
    data: completedAnime, 
    loading: loadingCompleted, 
    error: errorCompleted,
    refetch: refetchCompleted 
  } = useApi(() => animeApi.getCompletedAnime(1), { autoFetch: activeTab === 'completed' });

  const { 
    data: popularAnime, 
    loading: loadingPopular, 
    error: errorPopular,
    refetch: refetchPopular 
  } = useApi(() => animeApi.getPopularAnime(), { autoFetch: activeTab === 'popular' });

  const getCurrentData = (): Anime[] | null => {
    switch (activeTab) {
      case 'ongoing':
        return ongoingAnime;
      case 'completed':
        return completedAnime;
      case 'popular':
        return popularAnime;
      default:
        return null;
    }
  };

  const getCurrentLoading = (): boolean => {
    switch (activeTab) {
      case 'ongoing':
        return loadingOngoing;
      case 'completed':
        return loadingCompleted;
      case 'popular':
        return loadingPopular;
      default:
        return false;
    }
  };

  const getCurrentError = (): string | null => {
    switch (activeTab) {
      case 'ongoing':
        return errorOngoing;
      case 'completed':
        return errorCompleted;
      case 'popular':
        return errorPopular;
      default:
        return null;
    }
  };

  const handleRefetch = () => {
    switch (activeTab) {
      case 'ongoing':
        refetchOngoing();
        break;
      case 'completed':
        refetchCompleted();
        break;
      case 'popular':
        refetchPopular();
        break;
    }
  };

  const handleTabChange = (tab: 'ongoing' | 'completed' | 'popular') => {
    setActiveTab(tab);
  };

  const currentData = getCurrentData();
  const currentLoading = getCurrentLoading();
  const currentError = getCurrentError();

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section className="bg-gradient-to-r from-primary-900 to-primary-700 py-12 md:py-20">
        <div className="container mx-auto px-4">
          <div className="text-center">
            <h1 className="text-4xl md:text-6xl font-bold text-white mb-4 animate-fade-in">
              Selamat Datang di <span className="text-primary-300">DexAnime</span>
            </h1>
            <p className="text-xl text-primary-100 mb-8 animate-slide-up">
              Streaming anime favoritmu dengan subtitle Indonesia berkualitas HD
            </p>
            <div className="flex flex-wrap gap-4 justify-center">
              <div className="bg-white/10 backdrop-blur-sm px-6 py-3 rounded-lg">
                <p className="text-white font-semibold">✨ Update Setiap Hari</p>
              </div>
              <div className="bg-white/10 backdrop-blur-sm px-6 py-3 rounded-lg">
                <p className="text-white font-semibold">🎬 Kualitas HD</p>
              </div>
              <div className="bg-white/10 backdrop-blur-sm px-6 py-3 rounded-lg">
                <p className="text-white font-semibold">📱 Mobile Friendly</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Tabs Section */}
      <section className="container mx-auto px-4 py-8">
        <div className="flex gap-4 mb-8 border-b border-gray-800">
          <button
            onClick={() => handleTabChange('ongoing')}
            className={`px-6 py-3 font-semibold transition-all ${
              activeTab === 'ongoing'
                ? 'text-primary-500 border-b-2 border-primary-500'
                : 'text-gray-400 hover:text-white'
            }`}
          >
            🔥 Sedang Tayang
          </button>
          <button
            onClick={() => handleTabChange('completed')}
            className={`px-6 py-3 font-semibold transition-all ${
              activeTab === 'completed'
                ? 'text-primary-500 border-b-2 border-primary-500'
                : 'text-gray-400 hover:text-white'
            }`}
          >
            ✅ Completed
          </button>
          <button
            onClick={() => handleTabChange('popular')}
            className={`px-6 py-3 font-semibold transition-all ${
              activeTab === 'popular'
                ? 'text-primary-500 border-b-2 border-primary-500'
                : 'text-gray-400 hover:text-white'
            }`}
          >
            ⭐ Populer
          </button>
        </div>

        {/* Content */}
        {currentLoading && <LoadingGrid />}
        
        {currentError && !currentLoading && (
          <ErrorMessage message={currentError} onRetry={handleRefetch} />
        )}
        
        {currentData && !currentLoading && !currentError && (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
            {currentData.map((anime) => (
              <AnimeCard key={anime.id} anime={anime} />
            ))}
          </div>
        )}

        {currentData && currentData.length === 0 && !currentLoading && (
          <div className="text-center py-12">
            <p className="text-gray-400 text-lg">Tidak ada anime yang ditemukan</p>
          </div>
        )}
      </section>
    </div>
  );
}
