import { Link } from 'react-router-dom';
import { Anime } from '@/types/anime';

interface AnimeCardProps {
  anime: Anime;
}

export default function AnimeCard({ anime }: AnimeCardProps) {
  return (
    <Link 
      to={`/anime/${anime.slug}`}
      className="group bg-gray-800 rounded-lg overflow-hidden shadow-lg hover:shadow-2xl transition-all duration-300 hover:-translate-y-2 animate-fade-in"
    >
      <div className="relative overflow-hidden aspect-[3/4]">
        <img
          src={anime.thumbnail || '/placeholder-anime.jpg'}
          alt={anime.title}
          className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-300"
          loading="lazy"
        />
        {anime.status && (
          <div className="absolute top-2 right-2 bg-primary-600 text-white px-2 py-1 rounded text-xs font-semibold">
            {anime.status}
          </div>
        )}
        {anime.type && (
          <div className="absolute top-2 left-2 bg-black/70 text-white px-2 py-1 rounded text-xs">
            {anime.type}
          </div>
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
      </div>
      
      <div className="p-4">
        <h3 className="text-white font-semibold text-sm line-clamp-2 mb-2 group-hover:text-primary-400 transition-colors">
          {anime.title}
        </h3>
        
        {anime.genres && anime.genres.length > 0 && (
          <div className="flex flex-wrap gap-1 mb-2">
            {anime.genres.slice(0, 2).map((genre, index) => (
              <span 
                key={index}
                className="text-xs bg-gray-700 text-gray-300 px-2 py-1 rounded"
              >
                {genre}
              </span>
            ))}
          </div>
        )}
        
        <div className="flex items-center justify-between text-xs text-gray-400">
          {anime.rating && (
            <div className="flex items-center gap-1">
              <svg className="w-4 h-4 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
              </svg>
              <span>{anime.rating}</span>
            </div>
          )}
          {anime.totalEpisodes && (
            <span>{anime.totalEpisodes} Episode</span>
          )}
        </div>
      </div>
    </Link>
  );
}
