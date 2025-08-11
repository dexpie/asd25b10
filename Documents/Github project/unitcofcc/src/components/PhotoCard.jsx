// PhotoCard: Individual photo with overlay, caption, and animation
import React from 'react';

/**
 * PhotoCard displays a single photo in the slideshow with overlay, caption, and Ken Burns effect.
 * Accessibility: alt text, keyboard navigation, high contrast overlay.
 */
const PhotoCard = ({ photo }) => {
    return (
        <div className="relative w-full h-full flex items-center justify-center overflow-hidden">
            {/* Responsive image with gradient overlay and Ken Burns effect */}
            <img
                src={photo.src}
                alt={photo.caption}
                className="w-full h-full object-contain transition-transform duration-4000 ease-in-out scale-100 hover:scale-105"
                loading="lazy"
                style={{ filter: 'blur(0px)' }}
            />
            {/* Gradient overlay */}
            <div className="absolute bottom-0 left-0 right-0 h-1/3 bg-gradient-to-t from-[#3C1810]/70 to-transparent pointer-events-none" />
            {/* Caption and info */}
            <div className="absolute bottom-6 left-8 text-[#FFF8F0] animate-fade-in drop-shadow-xl">
                <div className="text-xl font-bold mb-1">{photo.caption}</div>
                <div className="text-sm font-medium opacity-90">{photo.timestamp} | {photo.location}</div>
            </div>
            {/* Share button removed from photo card; now placed in slideshow controls */}
        </div>
    );
};

export default PhotoCard;
