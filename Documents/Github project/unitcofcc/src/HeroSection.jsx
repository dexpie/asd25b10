// Hero Section: Cinematic opening for Our Journey Together
import React from 'react';

const HeroSection = () => {
    return (
        <section className="relative min-h-screen w-full flex items-center justify-center bg-gradient-to-br from-[#8B4513] via-[#D2691E] to-[#F4A460] overflow-hidden">
            <div className="w-full max-w-2xl flex flex-col items-center justify-center px-4 py-10 md:py-20">
                <h1 className="text-5xl md:text-7xl font-extrabold text-[#FFF8F0] mb-6 text-center drop-shadow-xl animate-fade-in-scale">Our Journey Together</h1>
                <h2 className="text-xl md:text-3xl font-semibold tracking-widest text-[#F5E6D3] mb-4 text-center animate-fade-in-delay">Unit Coffee - ITS Camp 2025</h2>
                <p className="max-w-2xl text-base md:text-xl text-[#3C1810] text-center leading-relaxed mb-10 animate-fade-in-stagger">
                    This website documents Unit Coffee’s journey during ITS Camp 2025,<br className="hidden md:block" /> filled with warm memories and unforgettable moments of togetherness.
                </p>
                {/* Scroll indicator */}
                <div className="mt-2 flex flex-col items-center">
                    <span className="animate-bounce text-3xl text-[#FFD700]">↓</span>
                </div>
            </div>
            {/* Floating coffee beans (decorative) */}
            <div className="absolute inset-0 pointer-events-none">
                {/* Example bean icons, can be replaced with SVGs */}
                <span className="absolute left-10 top-20 w-8 h-8 bg-[#8B4513] rounded-full opacity-70 animate-rotate-slow" />
                <span className="absolute right-16 bottom-24 w-6 h-6 bg-[#D2691E] rounded-full opacity-60 animate-rotate-slow" />
            </div>
            {/* Parallax effect and music note indicator can be added here */}
        </section>
    );
};

export default HeroSection;
