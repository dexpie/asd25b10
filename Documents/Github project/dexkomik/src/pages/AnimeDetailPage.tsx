import { useParams, Link } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { animeApi } from '@/services/animeApi';
import { AnimeDetail } from '@/types/anime';
import Loading from '@/components/Loading';
import ErrorMessage from '@/components/ErrorMessage';

export default function AnimeDetailPage() {
  const { slug } = useParams<{ slug: string }>();
  const [anime, setAnime] = useState<AnimeDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchAnimeDetail = async () => {
      if (!slug) return;
      
      setLoading(true);
      setError(null);
      
      const response = await animeApi.getAnimeDetail(slug);
      
      if (response.success && response.data) {
        setAnime(response.data);
      } else {
        setError(response.error || 'Gagal memuat detail anime');
      }
      
      setLoading(false);
    };

    fetchAnimeDetail();
  }, [slug]);

  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} />;
  if (!anime) return <ErrorMessage message="Anime tidak ditemukan" />;

  return (
    <div className="min-h-screen">
      {/* Header with backdrop */}
      <div className="relative h-[400px] md:h-[500px]">
        <div 
          className="absolute inset-0 bg-cover bg-center"
          style={{ backgroundImage: `url(${anime.thumbnail})` }}
        >
          <div className="absolute inset-0 bg-gradient-to-t from-gray-900 via-gray-900/80 to-gray-900/40"></div>
        </div>
        
        <div className="relative container mx-auto px-4 h-full flex items-end pb-8">
          <div className="flex flex-col md:flex-row gap-6 w-full">
            {/* Poster */}
            <img
              src={anime.thumbnail}
              alt={anime.title}
              className="w-48 h-72 object-cover rounded-lg shadow-2xl"
            />
            
            {/* Info */}
            <div className="flex-1 text-white">
              <h1 className="text-3xl md:text-5xl font-bold mb-4">{anime.title}</h1>
              
              <div className="flex flex-wrap gap-2 mb-4">
                {anime.genres?.map((genre, index) => (
                  <span 
                    key={index}
                    className="bg-primary-600 px-3 py-1 rounded-full text-sm font-medium"
                  >
                    {genre}
                  </span>
                ))}
              </div>
              
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                {anime.status && (
                  <div>
                    <p className="text-gray-400">Status</p>
                    <p className="font-semibold">{anime.status}</p>
                  </div>
                )}
                {anime.type && (
                  <div>
                    <p className="text-gray-400">Type</p>
                    <p className="font-semibold">{anime.type}</p>
                  </div>
                )}
                {anime.rating && (
                  <div>
                    <p className="text-gray-400">Rating</p>
                    <p className="font-semibold">⭐ {anime.rating}</p>
                  </div>
                )}
                {anime.studio && (
                  <div>
                    <p className="text-gray-400">Studio</p>
                    <p className="font-semibold">{anime.studio}</p>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="container mx-auto px-4 py-8">
        {/* Synopsis */}
        <section className="mb-8">
          <h2 className="text-2xl font-bold text-white mb-4">Sinopsis</h2>
          <p className="text-gray-300 leading-relaxed">
            {anime.description || anime.synopsis || 'Tidak ada sinopsis tersedia.'}
          </p>
        </section>

        {/* Episode List */}
        <section>
          <h2 className="text-2xl font-bold text-white mb-4">
            Daftar Episode ({anime.episodeList?.length || 0})
          </h2>
          
          {anime.episodeList && anime.episodeList.length > 0 ? (
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3">
              {anime.episodeList.map((episode) => (
                <Link
                  key={episode.id}
                  to={`/watch/${episode.slug}`}
                  className="bg-gray-800 hover:bg-primary-600 transition-colors rounded-lg p-4 text-center group"
                >
                  <p className="text-white font-semibold group-hover:scale-110 transition-transform">
                    Episode {episode.number}
                  </p>
                  {episode.releaseDate && (
                    <p className="text-gray-400 text-xs mt-1">{episode.releaseDate}</p>
                  )}
                </Link>
              ))}
            </div>
          ) : (
            <div className="bg-gray-800 rounded-lg p-8 text-center">
              <p className="text-gray-400">Belum ada episode tersedia</p>
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
