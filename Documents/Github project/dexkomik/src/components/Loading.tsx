export default function Loading() {
  return (
    <div className="flex items-center justify-center min-h-[400px]">
      <div className="text-center">
        <div className="inline-block relative">
          {/* Spinner */}
          <div className="w-16 h-16 border-4 border-gray-700 border-t-primary-500 rounded-full animate-spin"></div>
          
          {/* Inner circle */}
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2">
            <div className="w-8 h-8 bg-gradient-to-br from-primary-500 to-primary-700 rounded-full opacity-20 animate-pulse"></div>
          </div>
        </div>
        
        <p className="mt-4 text-gray-400 text-sm animate-pulse">Memuat data...</p>
      </div>
    </div>
  );
}

export function LoadingGrid() {
  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
      {[...Array(12)].map((_, index) => (
        <div key={index} className="animate-pulse">
          <div className="bg-gray-800 rounded-lg overflow-hidden">
            <div className="aspect-[3/4] bg-gray-700"></div>
            <div className="p-4 space-y-2">
              <div className="h-4 bg-gray-700 rounded w-3/4"></div>
              <div className="h-3 bg-gray-700 rounded w-1/2"></div>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
