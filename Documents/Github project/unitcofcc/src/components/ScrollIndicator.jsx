// ScrollIndicator: Smooth scroll navigation helper
import React from 'react';

/**
 * ScrollIndicator shows a bouncing arrow to guide users to scroll down.
 * Accessibility: aria-label, keyboard focus.
 */
const ScrollIndicator = () => (
    <div className="flex flex-col items-center mt-4" tabIndex={0} aria-label="Scroll down">
        <span className="animate-bounce text-3xl text-[#FFD700]">↓</span>
    </div>
);

export default ScrollIndicator;
