import { useState, useEffect } from 'react';
import { ApiResponse, ApiError } from '@/types/anime';

interface UseApiOptions {
  autoFetch?: boolean;
}

export function useApi<T>(
  apiCall: () => Promise<ApiResponse<T>>,
  options: UseApiOptions = { autoFetch: true }
) {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState<boolean>(options.autoFetch ?? true);
  const [error, setError] = useState<string | null>(null);

  const fetchData = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await apiCall();
      
      if (response.success && response.data) {
        setData(response.data);
      } else {
        setError(response.error || 'Terjadi kesalahan');
      }
    } catch (err) {
      setError((err as ApiError).message || 'Terjadi kesalahan yang tidak diketahui');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (options.autoFetch) {
      fetchData();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const refetch = () => {
    fetchData();
  };

  return { data, loading, error, refetch };
}
