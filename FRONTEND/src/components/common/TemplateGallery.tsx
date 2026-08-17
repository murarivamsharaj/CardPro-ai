import React from 'react';
import { TEMPLATES } from '../../utils/templateConstants';
import { notifyInfo } from '../../store/useNotificationStore';

interface TemplateGalleryProps {
  selectedTemplateId: string;
  premiumTemplatesUnlocked: boolean;
  onSelect: (templateId: string) => void;
}

/**
 * Grid of template swatches shared by the Create Card editor and the Edit
 * Card modal. Premium templates render locked (padlock, grayscale,
 * not-allowed cursor) until premiumTemplatesUnlocked is true; clicking a
 * locked template fires the unlock toast instead of selecting it.
 */
export const TemplateGallery: React.FC<TemplateGalleryProps> = ({
  selectedTemplateId,
  premiumTemplatesUnlocked,
  onSelect,
}) => {
  return (
    <>
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
        {TEMPLATES.map((t) => {
          const locked = t.premium && !premiumTemplatesUnlocked;
          const selected = selectedTemplateId === t.id;
          return (
            <button
              key={t.id}
              type="button"
              aria-disabled={locked}
              onClick={() => {
                if (locked) {
                  notifyInfo('Premium Template', 'Unlock this template in the Store to use it.');
                  return;
                }
                onSelect(t.id);
              }}
              className={`group relative overflow-hidden rounded-xl border p-3 text-left transition-all duration-300 ${
                selected
                  ? 'border-fuchsia-500/60 bg-fuchsia-500/10 shadow-lg shadow-fuchsia-900/30'
                  : 'border-white/10 bg-white/5 hover:border-white/25'
              } ${locked ? 'cursor-not-allowed opacity-60 grayscale' : ''}`}
            >
              <div className="flex h-14 items-end justify-between rounded-lg p-2" style={{ background: t.gradient }}>
                <span className="text-[10px] font-bold text-white/90 drop-shadow">{t.name}</span>
                {locked ? (
                  <span className="flex h-6 w-6 items-center justify-center rounded-full bg-black/50 text-white backdrop-blur-sm">
                    <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 10-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 002.25-2.25v-6.75a2.25 2.25 0 00-2.25-2.25H6.75a2.25 2.25 0 00-2.25 2.25v6.75a2.25 2.25 0 002.25 2.25z" />
                    </svg>
                  </span>
                ) : selected ? (
                  <span className="flex h-6 w-6 items-center justify-center rounded-full bg-fuchsia-500 text-white">
                    <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 12.75l6 6 9-13.5" />
                    </svg>
                  </span>
                ) : null}
              </div>
              <p className="mt-2 text-xs font-semibold text-white">{t.name}</p>
              <p className="text-[11px] text-white/45">{t.description}</p>
            </button>
          );
        })}
      </div>
      {!premiumTemplatesUnlocked && (
        <p className="mt-2 text-xs text-white/40">
          🔒 Premium templates unlock with the Premium Templates pack in the Store.
        </p>
      )}
    </>
  );
};

export default TemplateGallery;
