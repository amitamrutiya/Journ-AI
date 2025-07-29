'use client';

import { type Editor } from '@tiptap/react';
import { BubbleMenu } from '@tiptap/react/menus';
import { useEffect } from 'react';

import { ScrollArea, ScrollBar } from '@/components/ui/scroll-area';
import { Separator } from '@/components/ui/separator';
import { TooltipProvider } from '@/components/ui/tooltip';
import { useMediaQuery } from '@/hooks/use-media-querry';

import { AlignmentTooolbar } from '../toolbars/alignment';
import { BlockquoteToolbar } from '../toolbars/blockquote';
import { BoldToolbar } from '../toolbars/bold';
import { BulletListToolbar } from '../toolbars/bullet-list';
import { ColorHighlightToolbar } from '../toolbars/color-and-highlight';
import { HeadingsToolbar } from '../toolbars/headings';
import { ImagePlaceholderToolbar } from '../toolbars/image-placeholder-toolbar';
import { ItalicToolbar } from '../toolbars/italic';
import { LinkToolbar } from '../toolbars/link';
import { OrderedListToolbar } from '../toolbars/ordered-list';
import { ToolbarProvider } from '../toolbars/toolbar-provider';
import { UnderlineToolbar } from '../toolbars/underline';

export function FloatingToolbar({ editor }: { editor: Editor | null }) {
  const isMobile = useMediaQuery('(max-width: 640px)');

  // Prevent default context menu on mobile
  useEffect(() => {
    if (!editor?.options.element || !isMobile) return;

    const handleContextMenu = (event_: Event) => {
      event_.preventDefault();
    };

    const element = editor.options.element;
    element.addEventListener('contextmenu', handleContextMenu);

    return () => element.removeEventListener('contextmenu', handleContextMenu);
  }, [editor, isMobile]);

  if (!editor) return;

  if (isMobile) {
    return (
      <TooltipProvider>
        <BubbleMenu
          options={{
            strategy: 'absolute',
            placement: 'bottom',
            offset: 8,
            flip: true,
            shift: true,
          }}
          shouldShow={() => {
            // Show toolbar when editor is focused and has selection
            return editor.isEditable && editor.isFocused;
          }}
          editor={editor}
          className="bg-background mx-0 w-full min-w-full rounded-sm border shadow-sm"
        >
          <ToolbarProvider editor={editor}>
            <ScrollArea className="h-fit w-full py-0.5">
              <div className="flex items-center gap-0.5 px-2">
                <div className="flex items-center gap-0.5 p-1">
                  {/* Primary formatting */}
                  <BoldToolbar />
                  <ItalicToolbar />
                  <UnderlineToolbar />
                  <Separator orientation="vertical" className="mx-1 h-6" />

                  {/* Structure controls */}
                  <HeadingsToolbar />
                  <BulletListToolbar />
                  <OrderedListToolbar />
                  <Separator orientation="vertical" className="mx-1 h-6" />

                  {/* Rich formatting */}
                  <ColorHighlightToolbar />
                  <LinkToolbar />
                  <ImagePlaceholderToolbar />
                  <Separator orientation="vertical" className="mx-1 h-6" />

                  {/* Additional controls */}
                  <AlignmentTooolbar />
                  <BlockquoteToolbar />
                </div>
              </div>
              <ScrollBar className="h-0.5" orientation="horizontal" />
            </ScrollArea>
          </ToolbarProvider>
        </BubbleMenu>
      </TooltipProvider>
    );
  }

  return;
}
