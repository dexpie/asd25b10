// PhotoSlideshow: Main story slideshow for Our Journey Together
import React, { useState, useEffect, useRef } from 'react';
import ProgressBar from './components/ProgressBar';
import PhotoCard from './components/PhotoCard';

const SLIDE_DURATION = 4000;

const PhotoSlideshow = ({ photos, currentIndex, setCurrentIndex, isAutoplay, setIsAutoplay }) => {
    const timerRef = useRef(null);

    // Autoplay logic
    useEffect(() => {
        if (isAutoplay) {
            timerRef.current = setTimeout(() => {
                setCurrentIndex((prev) => (prev + 1) % photos.length);
            }, SLIDE_DURATION);
        }
        return () => clearTimeout(timerRef.current);
    }, [currentIndex, isAutoplay, photos.length, setCurrentIndex]);

    // Pause on hover
    const handleMouseEnter = () => setIsAutoplay(false);
    const handleMouseLeave = () => setIsAutoplay(true);

    // Manual navigation
    const goToPrev = () => setCurrentIndex((prev) => (prev === 0 ? photos.length - 1 : prev - 1));
    const goToNext = () => setCurrentIndex((prev) => (prev + 1) % photos.length);

    return (
        <section className="w-full flex flex-col items-center justify-center py-12 bg-[#F5E6D3]">
            <div className="relative w-full max-w-2xl aspect-video flex flex-col items-center justify-center rounded-3xl shadow-2xl overflow-hidden bg-[#fff8f0] border-4 border-[#8B4513]/10 mx-auto">
                {/* Progress bar */}
                <ProgressBar duration={SLIDE_DURATION} isActive={isAutoplay} key={currentIndex} />
                {/* Photo card */}
                <div className="w-full h-full flex items-center justify-center" onMouseEnter={handleMouseEnter} onMouseLeave={handleMouseLeave}>
                    <PhotoCard photo={photos[currentIndex]} />
                </div>
            </div>
            {/* Controls and navigation below the slideshow */}
            <div className="w-full max-w-2xl flex flex-col items-center justify-center gap-2 mt-6 mb-2">
                <div className="flex justify-center items-center gap-4">
                    <button onClick={goToPrev} className="p-3 bg-[#8B4513] rounded-full text-[#FFF8F0] shadow-lg hover:bg-[#D2691E] focus:outline-none focus:ring-2 focus:ring-[#FFD700]" aria-label="Previous photo">←</button>
                    <button onClick={goToNext} className="p-3 bg-[#8B4513] rounded-full text-[#FFF8F0] shadow-lg hover:bg-[#D2691E] focus:outline-none focus:ring-2 focus:ring-[#FFD700]" aria-label="Next photo">→</button>
                    <button onClick={() => setIsAutoplay((a) => !a)} className="p-3 bg-[#FFD700] rounded-lg text-[#3C1810] shadow hover:bg-[#FFD700]/80 focus:outline-none focus:ring-2 focus:ring-[#FFD700]" aria-label={isAutoplay ? 'Pause slideshow' : 'Play slideshow'}>
                        {isAutoplay ? 'Pause' : 'Play'}
                    </button>
                    <button className="p-3 bg-[#FFD700] rounded-lg text-[#3C1810] shadow hover:bg-[#FFD700]/80 focus:outline-none focus:ring-2 focus:ring-[#FFD700]" aria-label={`Share ${photos[currentIndex]?.caption}`}>Share</button>
                </div>
                <div className="flex justify-center gap-2 mt-2">
                    {photos.map((_, idx) => (
                        <button key={idx} onClick={() => setCurrentIndex(idx)} className={`w-3 h-3 rounded-full border-2 border-[#8B4513]/30 ${idx === currentIndex ? 'bg-[#FFD700] border-[#FFD700]' : 'bg-[#8B4513]/30'} transition focus:outline-none focus:ring-2 focus:ring-[#FFD700]`} aria-label={`Go to photo ${idx + 1}`} />
                    ))}
                </div>
            </div>
        </section>
    );
};

export default PhotoSlideshow;
