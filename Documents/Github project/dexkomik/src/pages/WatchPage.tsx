import { useParams, Link } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { animeApi } from '@/services/animeApi';
import { WatchData } from '@/types/anime';
import Loading from '@/components/Loading';
import ErrorMessage from '@/components/ErrorMessage';

export default function WatchPage() {
  const { slug } = useParams<{ slug: string }>();
  const [watchData, setWatchData] = useState<WatchData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedServer, setSelectedServer] = useState(0);

  useEffect(() => {
    const fetchEpisodeData = async () => {
      if (!slug) return;
      
      setLoading(true);
      setError(null);
      
      const response = await animeApi.getEpisodeData(slug);
      
      if (response.success && response.data) {
        setWatchData(response.data);
      } else {
        setError(response.error || 'Gagal memuat episode');
      }
      
      setLoading(false);
    };

    fetchEpisodeData();
  }, [slug]);

  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} />;
  if (!watchData) return <ErrorMessage message="Episode tidak ditemukan" />;

  return (
    <div className="min-h-screen">
      <div className="container mx-auto px-4 py-6">
        {/* Breadcrumb */}
        <div className="mb-4">
          <Link 
            to={`/anime/${watchData.anime.slug}`}
            className="text-primary-500 hover:text-primary-400 transition-colors flex items-center gap-2"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
            Kembali ke {watchData.anime.title}
          </Link>
        </div>

        {/* Video Player */}
        <div className="bg-black rounded-lg overflow-hidden mb-6">
          <div className="aspect-video">
            {watchData.streamLinks && watchData.streamLinks.length > 0 ? (
              <iframe
                src={watchData.streamLinks[selectedServer]?.url}
                className="w-full h-full"
                allowFullScreen
                title={`${watchData.anime.title} - Episode ${watchData.episode.number}`}
              />
            ) : (
              <div className="w-full h-full flex items-center justify-center bg-gray-900">
                <p className="text-white">Video tidak tersedia</p>
              </div>
            )}
          </div>
        </div>

        {/* Episode Info */}
        <div className="bg-gray-800 rounded-lg p-6 mb-6">
          <h1 className="text-2xl md:text-3xl font-bold text-white mb-2">
            {watchData.anime.title}
          </h1>
          <p className="text-primary-500 font-semibold mb-4">
            Episode {watchData.episode.number}
            {watchData.episode.title && ` - ${watchData.episode.title}`}
          </p>
          
          {/* Server Selection */}
          {watchData.streamLinks && watchData.streamLinks.length > 1 && (
            <div className="mb-4">
              <p className="text-gray-400 mb-2">Pilih Server:</p>
              <div className="flex flex-wrap gap-2">
                {watchData.streamLinks.map((link, index) => (
                  <button
                    key={index}
                    onClick={() => setSelectedServer(index)}
                    className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                      selectedServer === index
                        ? 'bg-primary-600 text-white'
                        : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
                    }`}
                  >
                    {link.provider || `Server ${index + 1}`} 
                    {link.quality && ` (${link.quality})`}
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Navigation */}
        <div className="flex gap-4 mb-6">
          {watchData.prevEpisode && (
            <Link
              to={`/watch/${watchData.prevEpisode.slug}`}
              className="flex-1 bg-gray-800 hover:bg-gray-700 text-white px-6 py-3 rounded-lg transition-colors flex items-center justify-center gap-2 font-medium"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
              Episode Sebelumnya
            </Link>
          )}
          {watchData.nextEpisode && (
            <Link
              to={`/watch/${watchData.nextEpisode.slug}`}
              className="flex-1 bg-primary-600 hover:bg-primary-700 text-white px-6 py-3 rounded-lg transition-colors flex items-center justify-center gap-2 font-medium"
            >
              Episode Selanjutnya
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </Link>
          )}
        </div>

        {/* Anime Info */}
        {watchData.anime.synopsis && (
          <div className="bg-gray-800 rounded-lg p-6">
            <h2 className="text-xl font-bold text-white mb-3">Sinopsis</h2>
            <p className="text-gray-300 leading-relaxed">
              {watchData.anime.synopsis}
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
