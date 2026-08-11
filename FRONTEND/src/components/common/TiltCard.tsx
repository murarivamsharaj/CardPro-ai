import React, { useRef } from 'react';

interface TiltCardProps {
  children: React.ReactNode;
  className?: string;
  /** Max tilt angle in degrees on each axis. Default 10. */
  maxTilt?: number;
  /** Show a moving glare highlight. Default true. */
  glare?: boolean;
  /** Disable tilt while loading (keeps skeleton previews still). */
  disabled?: boolean;
}

/**
 * Custom 3D tilt effect — tilts on the X/Y axes toward the cursor with a
 * subtle glare, mimicking a physical card held in the hand. No library
 * required: it drives CSS custom properties from pointer events.
 */
export const TiltCard: React.FC<TiltCardProps> = ({
  children,
  className = '',
  maxTilt = 10,
  glare = true,
  disabled = false,
}) => {
  const ref = useRef<HTMLDivElement>(null);

  const handleMouseMove = (e: React.MouseEvent<HTMLDivElement>) => {
    if (disabled) return;
    const el = ref.current;
    if (!el) return;

    const rect = el.getBoundingClientRect();
    const px = (e.clientX - rect.left) / rect.width; // 0..1
    const py = (e.clientY - rect.top) / rect.height; // 0..1

    // Tilt away from the cursor: rotateY positive on the right side,
    // rotateX negative toward the bottom (like lifting the card edge).
    const rotateY = (px - 0.5) * 2 * maxTilt;
    const rotateX = (0.5 - py) * 2 * maxTilt;

    el.style.transform = `perspective(900px) rotateX(${rotateX.toFixed(2)}deg) rotateY(${rotateY.toFixed(2)}deg) scale3d(1.02, 1.02, 1.02)`;
    el.style.setProperty('--glare-x', `${(px * 100).toFixed(1)}%`);
    el.style.setProperty('--glare-y', `${(py * 100).toFixed(1)}%`);
  };

  const handleMouseLeave = () => {
    const el = ref.current;
    if (!el) return;
    el.style.transform =
      'perspective(900px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)';
    el.style.setProperty('--glare-x', '50%');
    el.style.setProperty('--glare-y', '50%');
  };

  return (
    <div
      ref={ref}
      onMouseMove={handleMouseMove}
      onMouseLeave={handleMouseLeave}
      className={`tilt-card ${className}`}
    >
      {children}
      {glare && <div className="tilt-glare" aria-hidden="true" />}
    </div>
  );
};

export default TiltCard;
