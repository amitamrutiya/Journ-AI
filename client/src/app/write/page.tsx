'use client';

import { SignInButton, useAuth } from '@clerk/nextjs';
import { Brain, Loader2, LogIn, Save } from 'lucide-react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Suspense, useEffect, useState } from 'react';

import { AIAnalysisCard } from '@/components/journal/ai-analysis-card';
import { JournalEditor } from '@/components/journal/journal-editor';
import SaveJournalButton from '@/components/journal/save-journal-button';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import {
  useAnalyzeJournal,
  useGetJournalById,
  useSaveJournal,
  useUpdateJournal,
} from '@/hooks/use-journal-api';
import { content } from '@/lib/content';

interface AIAnalysis {
  mood: string;
  summary: string;
  reason: string;
}

function WritePageContent() {
  const { isSignedIn } = useAuth();
  const router = useRouter();
  const searchParams = useSearchParams();
  const editJournalId = searchParams.get('edit');

  const [journalText, setJournalText] = useState(editJournalId ? '' : content);

  const [saveSuccess, setSaveSuccess] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [isSaved, setIsSaved] = useState(false);
  const [currentJournalId, setCurrentJournalId] = useState<string>();
  const [existingAnalysis, setExistingAnalysis] = useState<AIAnalysis>();
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);
  const [editorKey, setEditorKey] = useState(0);

  const analyzeJournalMutation = useAnalyzeJournal();
  const saveJournalMutation = useSaveJournal();
  const updateJournalMutation = useUpdateJournal();

  const existingJournal = useGetJournalById(editJournalId);

  useEffect(() => {
    if (editJournalId && existingJournal.data && !isEditMode) {
      setJournalText(existingJournal.data.content || '');
      setIsEditMode(true);
      setIsSaved(true);
      setCurrentJournalId(editJournalId);
      setEditorKey(previous => previous + 1);
      // Pre-populate analysis with existing data instead of calling API
      if (existingJournal.data.mood && existingJournal.data.summary) {
        const analysisData = {
          mood: existingJournal.data.mood,
          summary: existingJournal.data.summary,
          reason:
            existingJournal.data.reason || 'Previously analyzed journal entry',
        };

        setExistingAnalysis(analysisData);
      }
    }
  }, [editJournalId, existingJournal.data, isEditMode]);

  useEffect(() => {
    const handleBeforeUnload = (event: BeforeUnloadEvent) => {
      if (hasUnsavedChanges && journalText.trim()) {
        event.preventDefault();
        event.returnValue =
          'You have unsaved changes. Are you sure you want to leave?';
      }
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => window.removeEventListener('beforeunload', handleBeforeUnload);
  }, [hasUnsavedChanges, journalText]);

  const handleAnalyzeWithAI = async () => {
    if (!journalText.trim()) return;

    try {
      const result = await analyzeJournalMutation.mutateAsync({
        text: journalText,
      });
      // Clear existing analysis when new analysis is completed
      if (result && isEditMode) {
        setExistingAnalysis(undefined);
      }
    } catch (error) {
      console.error('Error analyzing journal:', error);
    }
  };

  const handleSaveJournal = async () => {
    // Prioritize new analysis over existing analysis
    const analysisData = analyzeJournalMutation.data?.data || existingAnalysis;

    if (!isSignedIn || !journalText.trim()) {
      return;
    }

    try {
      if (currentJournalId || editJournalId) {
        // Update existing journal (either from URL param or from previous save)
        const journalIdToUpdate = editJournalId || currentJournalId;
        await updateJournalMutation.mutateAsync({
          id: journalIdToUpdate!,
          text: journalText,
          mood: analysisData?.mood || '',
          summary: analysisData?.summary || '',
          reason: analysisData?.reason || '',
        });
      } else {
        // Save new journal and mark as saved
        const result = await saveJournalMutation.mutateAsync({
          text: journalText,
          mood: analysisData?.mood || '',
          summary: analysisData?.summary || '',
          reason: analysisData?.reason || '',
        });

        // After successful save, mark as saved and store the journal ID
        setIsSaved(true);
        setCurrentJournalId(result.id);
      }

      setSaveSuccess(true);
      setHasUnsavedChanges(false);
      setTimeout(() => setSaveSuccess(false), 3000);
    } catch (error) {
      console.error('Error saving journal:', error);
      // TODO: Add proper error handling/toast
    }
  };

  const handleResetJournal = () => {
    // Reset all states to create a new journal
    setIsEditMode(false);
    setIsSaved(false);
    setCurrentJournalId(undefined);
    setExistingAnalysis(undefined);
    setHasUnsavedChanges(false);
    setSaveSuccess(false);

    analyzeJournalMutation.reset();

    setJournalText(content);
    setEditorKey(previous => previous + 1);

    if (editJournalId) {
      router.push('/write');
    }
  };

  // Show loading state when fetching existing journal
  if (editJournalId && existingJournal.isPending) {
    return (
      <div className="container mx-auto max-w-7xl px-4 py-8">
        <div className="flex h-96 items-center justify-center">
          <div className="space-y-4 text-center">
            <Loader2 className="text-muted-foreground mx-auto h-8 w-8 animate-spin" />
            <p className="text-muted-foreground">Loading journal entry...</p>
          </div>
        </div>
      </div>
    );
  }

  // Show error state if journal not found
  if (editJournalId && existingJournal.isError) {
    return (
      <div className="container mx-auto max-w-7xl px-4 py-8">
        <Card>
          <CardContent className="space-y-4 pt-6 text-center">
            <div>
              <h2 className="text-xl font-semibold">Journal not found</h2>
              <p className="text-muted-foreground">
                The journal entry you&apos;re looking for doesn&apos;t exist or
                you don&apos;t have access to it.
              </p>
            </div>
            <Button onClick={() => router.push('/calendar')}>
              Back to Calendar
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="container mx-auto max-w-7xl px-4 py-8">
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between px-6">
          {/* Back to Calendar button */}
          <div className="flex items-center">
            {isEditMode && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  if (hasUnsavedChanges && journalText.trim()) {
                    const confirmed = globalThis.confirm(
                      'You have unsaved changes. Are you sure you want to leave without saving?',
                    );
                    if (confirmed) {
                      router.push('/calendar');
                    }
                  } else {
                    router.push('/calendar');
                  }
                }}
                className="mr-4"
              >
                ← Back to Calendar
              </Button>
            )}
          </div>

          {/* Title */}
          <div className="flex-1">
            <h1 className="text-center text-xl font-bold tracking-tight">
              {isEditMode
                ? 'Update your thoughts and analysis'
                : 'Express your thoughts and let AI help you understand your emotions'}
            </h1>
          </div>

          {/* Reset Journal Button */}
          <div className="flex items-center">
            <Button
              variant="outline"
              size="sm"
              onClick={handleResetJournal}
              className="ml-4"
              disabled={!isSaved}
            >
              🆕 New Journal
            </Button>
          </div>
        </div>

        <CardContent>
          {/* Only render editor when content is ready */}
          {(!editJournalId || (editJournalId && existingJournal.data)) && (
            <JournalEditor
              key={editorKey}
              content={journalText}
              onChange={newContent => {
                setJournalText(newContent);
                if (newContent !== content && newContent.trim()) {
                  setHasUnsavedChanges(true);
                }
              }}
            />
          )}

          {/* Show loading state for editor when fetching journal data */}
          {editJournalId && existingJournal.isPending && (
            <div className="flex h-96 items-center justify-center">
              <div className="space-y-4 text-center">
                <Loader2 className="text-muted-foreground mx-auto h-4 w-4 animate-spin" />
                <p className="text-muted-foreground text-sm">
                  Loading journal content...
                </p>
              </div>
            </div>
          )}

          {/* Action Buttons Row */}
          {journalText.trim() && (
            <div className="mt-6 space-y-4">
              <div className="flex flex-col justify-center gap-3 sm:flex-row">
                {/* Analyze Button */}
                <Button
                  onClick={handleAnalyzeWithAI}
                  disabled={
                    !journalText.trim() || analyzeJournalMutation.isPending
                  }
                  variant={
                    analyzeJournalMutation.data || existingAnalysis
                      ? 'outline'
                      : 'default'
                  }
                  className="max-w-xs flex-1"
                >
                  {analyzeJournalMutation.isPending ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      {analyzeJournalMutation.data || existingAnalysis
                        ? 'Re-analyzing...'
                        : 'Analyzing...'}
                    </>
                  ) : (
                    <>
                      <Brain className="mr-2 h-4 w-4" />
                      {analyzeJournalMutation.data || existingAnalysis
                        ? '🔄 Re-analyze with AI'
                        : '💡 Analyze with AI'}
                    </>
                  )}
                </Button>

                {/* Save Button */}
                {isSignedIn ? (
                  <SaveJournalButton
                    handleSaveJournal={handleSaveJournal}
                    isSaving={
                      saveJournalMutation.isPending ||
                      updateJournalMutation.isPending
                    }
                    saveSuccess={saveSuccess}
                    isUpdateMode={isEditMode || isSaved}
                  />
                ) : (
                  <SignInButton>
                    <Button className="max-w-xs flex-1">
                      <LogIn className="mr-2 h-4 w-4" />
                      🔐 Login to Save
                    </Button>
                  </SignInButton>
                )}
              </div>

              {/* Status Messages */}
              <div className="space-y-2 text-center">
                {saveSuccess && (
                  <p className="text-sm text-green-600 dark:text-green-400">
                    ✅ Your journal has been saved to your account.
                  </p>
                )}
                {!isSignedIn && (
                  <p className="text-muted-foreground text-sm">
                    Login to save this journal and track your mood over time.
                  </p>
                )}
                {isSignedIn &&
                  !(analyzeJournalMutation.data || existingAnalysis) && (
                    <p className="text-muted-foreground text-sm">
                      � Tip: Use &ldquo;Analyze with AI&rdquo; to get mood
                      insights before saving
                    </p>
                  )}
              </div>
            </div>
          )}
        </CardContent>

        {/* AI Analysis Results */}
        {(analyzeJournalMutation.data || existingAnalysis) && (
          <AIAnalysisCard
            mood={
              analyzeJournalMutation.data?.data.mood ||
              existingAnalysis?.mood ||
              ''
            }
            summary={
              analyzeJournalMutation.data?.data.summary ||
              existingAnalysis?.summary ||
              ''
            }
            reason={
              analyzeJournalMutation.data?.data.reason ||
              existingAnalysis?.reason ||
              ''
            }
          />
        )}
      </div>
    </div>
  );
}

export default function WritePage() {
  return (
    <Suspense
      fallback={
        <div className="container mx-auto max-w-7xl px-4 py-8">
          <div className="flex h-96 items-center justify-center">
            <div className="space-y-4 text-center">
              <Loader2 className="text-muted-foreground mx-auto h-8 w-8 animate-spin" />
              <p className="text-muted-foreground">Loading...</p>
            </div>
          </div>
        </div>
      }
    >
      <WritePageContent />
    </Suspense>
  );
}
