import React from 'react';

/**
 * Ambient aurora background: deep space gradient with drifting
 * color orbs. Render once per full-screen surface (auth pages, app shell).
 */
export const BackgroundFX: React.FC = () => {
  return (
    <>
      <div className="app-backdrop" aria-hidden="true" />
      <div
        aria-hidden="true"
        className="pointer-events-none fixed inset-0 -z-10 opacity-[0.04]"
        style={{
          backgroundImage:
            'linear-gradient(rgba(255,255,255,.6) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,.6) 1px, transparent 1px)',
          backgroundSize: '56px 56px',
          maskImage: 'radial-gradient(ellipse at center, black 30%, transparent 75%)',
        }}
      />
    </>
  );
};

export default BackgroundFX;
