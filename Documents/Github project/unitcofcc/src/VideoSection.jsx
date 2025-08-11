// VideoSection: Cinematic video display for memories website
import React from 'react';

/**
 * VideoSection displays a video from a given URL in a cinematic, responsive container.
 * Accessibility: controls, alt text, responsive layout.
 */
const VideoSection = () => {
    return (
        <section className="w-full flex flex-col items-center justify-center py-12 bg-[#F5E6D3]">
            <h2 className="text-3xl md:text-4xl font-bold text-[#3C1810] mb-6 text-center">Memories Video</h2>
            <div className="w-full max-w-md md:max-w-2xl lg:max-w-3xl rounded-2xl shadow-2xl overflow-hidden bg-black flex justify-center items-center mx-auto" style={{ aspectRatio: '9/16', height: '60vw', maxHeight: '80vh' }}>
                <iframe
                    src="https://drive.google.com/file/d/1-GeXgQ6F5PBL1--zJTp-oEB4MJL1IEAT/preview"
                    allow="autoplay"
                    allowFullScreen
                    className="w-full h-full object-cover"
                    title="Memories Video"
                    style={{ minHeight: '100%', minWidth: '100%' }}
                ></iframe>
            </div>
        </section>
    );
};

export default VideoSection;
