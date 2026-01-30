// Phi (?) symbol as an SVG component - the 21st letter of the Greek alphabet
// Represents the golden ratio (?1.618) in mathematics

export default function PhiLogo({ className = '', size = 'md' }) {
  const sizes = {
    sm: 'h-8 w-8',
    md: 'h-10 w-10',
    lg: 'h-14 w-14',
  };

  const sizeClass = sizes[size] || sizes.md;

  return (
    <div className={`${sizeClass} ${className}`}>
      <svg viewBox="0 0 64 64" className="w-full h-full">
        <defs>
          <linearGradient id="phiGradient" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor="#FBBF24"/>
            <stop offset="100%" stopColor="#D97706"/>
          </linearGradient>
        </defs>
        <rect x="2" y="2" width="60" height="60" rx="12" fill="url(#phiGradient)"/>
        {/* Phi symbol: vertical line through an ellipse */}
        <ellipse cx="32" cy="32" rx="14" ry="18" fill="none" stroke="white" strokeWidth="5"/>
        <line x1="32" y1="6" x2="32" y2="58" stroke="white" strokeWidth="5" strokeLinecap="round"/>
      </svg>
    </div>
  );
}
