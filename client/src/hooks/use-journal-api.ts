import { useAuth } from '@clerk/nextjs';
import { useMutation, useQuery } from '@tanstack/react-query';

import { QUERY_CONFIG, QUERY_KEYS } from '@/lib/query-config';

const SERVER_URL =
  process.env.NEXT_PUBLIC_SERVER_URL || 'http://localhost:8000';

interface AIAnalysis {
  data: {
    mood: string;
    summary: string;
    reason: string;
  };
}

interface AnalyzeJournalRequest {
  text: string;
}

interface SaveJournalRequest {
  text: string;
  mood?: string;
  summary?: string;
  reason?: string;
}

interface SaveJournalResponse {
  id: string;
  title: string;
  mood: string;
  createdAt: string;
}

interface PDFExtractResponse {
  text: string;
}

type TimeRange = 'week' | 'month' | 'quarter' | 'year';

// Hook for analyzing journal with AI
export function useAnalyzeJournal() {
  const { getToken } = useAuth();

  return useMutation<AIAnalysis, Error, AnalyzeJournalRequest>({
    mutationFn: async ({ text }: AnalyzeJournalRequest) => {
      const token = await getToken();

      const response = await fetch(`${SERVER_URL}/api/analyze-journal`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ text }),
      });

      if (!response.ok) {
        throw new Error('Failed to analyze journal');
      }

      return response.json();
    },
  });
}

// Hook for saving journal
export function useSaveJournal() {
  const { getToken } = useAuth();

  return useMutation<SaveJournalResponse, Error, SaveJournalRequest>({
    mutationFn: async ({
      text,
      mood = '',
      summary = '',
      reason = '',
    }: SaveJournalRequest) => {
      const token = await getToken();

      const response = await fetch(`${SERVER_URL}/api/save-journal`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          text,
          mood,
          summary,
          reason,
        }),
      });

      if (!response.ok) {
        throw new Error('Failed to save journal');
      }

      const data = await response.json();
      return data.data;
    },
  });
}

export function useGetUserJournals(selectedMonth?: string) {
  const { getToken, isSignedIn } = useAuth();

  return useQuery({
    queryKey: [...QUERY_KEYS.userJournals, selectedMonth],
    queryFn: async () => {
      const token = await getToken();

      // Build URL with optional month parameter
      const url = new URL(`${SERVER_URL}/api/get-user-journal`);
      if (selectedMonth) {
        url.searchParams.append('month', selectedMonth);
      }

      const response = await fetch(url.toString(), {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        throw new Error('Failed to fetch journals');
      }

      const result = await response.json();
      return result.data || result; // Handle both wrapped and unwrapped responses
    },
    enabled: !!isSignedIn, // Only run query when user is signed in
    staleTime: QUERY_CONFIG.staleTime,
    gcTime: QUERY_CONFIG.gcTime,
    retry: QUERY_CONFIG.retry,
    refetchOnWindowFocus: QUERY_CONFIG.refetchOnWindowFocus,
  });
}

export function useGetJournalById(journalId: string | null) {
  const { getToken, isSignedIn } = useAuth();

  return useQuery({
    queryKey: QUERY_KEYS.journalById(journalId || ''),
    queryFn: async () => {
      const token = await getToken();

      const response = await fetch(`${SERVER_URL}/api/journal/${journalId}`, {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        throw new Error('Failed to fetch journal');
      }

      const result = await response.json();
      return result.data || result;
    },
    enabled: !!isSignedIn && !!journalId, // Only run when user is signed in and journalId exists
    staleTime: QUERY_CONFIG.staleTime,
    gcTime: QUERY_CONFIG.gcTime,
    retry: QUERY_CONFIG.retry,
    refetchOnWindowFocus: QUERY_CONFIG.refetchOnWindowFocus,
  });
}

// Hook for updating journal
export function useUpdateJournal() {
  const { getToken } = useAuth();

  return useMutation<
    void,
    Error,
    {
      id: string;
      text: string;
      mood?: string;
      summary?: string;
      reason?: string;
    }
  >({
    mutationFn: async ({ id, text, mood = '', summary = '', reason = '' }) => {
      const token = await getToken();

      const response = await fetch(`${SERVER_URL}/api/update-journal/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          text,
          mood,
          summary,
          reason,
        }),
      });

      if (!response.ok) {
        throw new Error('Failed to update journal');
      }
    },
  });
}

// Hook for deleting journal
export function useDeleteJournal() {
  const { getToken } = useAuth();

  return useMutation<void, Error, string>({
    mutationFn: async (journalId: string) => {
      const token = await getToken();

      const response = await fetch(
        `${SERVER_URL}/api/delete-journal/${journalId}`,
        {
          method: 'DELETE',
          headers: {
            Authorization: `Bearer ${token}`,
          },
        },
      );

      if (!response.ok) {
        throw new Error('Failed to delete journal');
      }
    },
  });
}

// Hook for fetching journal insights

export function useGetJournalInsights(
  timeRange: TimeRange = 'month',
  moodFilter?: string,
) {
  const { getToken, isSignedIn } = useAuth();

  return useQuery({
    queryKey: [...QUERY_KEYS.journalInsights, timeRange, moodFilter],
    queryFn: async () => {
      const token = await getToken();

      // Build URL with query parameters
      const url = new URL(`${SERVER_URL}/api/journals/insights`);
      url.searchParams.append('range', timeRange);
      if (moodFilter) {
        url.searchParams.append('mood', moodFilter);
      }

      const response = await fetch(url.toString(), {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        throw new Error('Failed to fetch insights');
      }

      const result = await response.json();
      return result.data || result; // Handle both wrapped and unwrapped responses
    },
    enabled: !!isSignedIn, // Only run query when user is signed in
    staleTime: QUERY_CONFIG.staleTime,
    gcTime: QUERY_CONFIG.gcTime,
    retry: QUERY_CONFIG.retry,
    refetchOnWindowFocus: QUERY_CONFIG.refetchOnWindowFocus,
  });
}
