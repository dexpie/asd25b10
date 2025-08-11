// ProgressBar: Linear progress indicator for slideshow
import React, { useEffect, useRef } from 'react';

/**
 * ProgressBar shows slideshow progress with smooth animation.
 * Accessibility: aria attributes for progress.
 */
const ProgressBar = ({ duration, isActive }) => {
    const barRef = useRef(null);

    useEffect(() => {
        if (isActive && barRef.current) {
            barRef.current.style.transition = `width ${duration}ms linear`;
            barRef.current.style.width = '100%';
        } else if (barRef.current) {
            barRef.current.style.transition = 'none';
            barRef.current.style.width = '0%';
        }
    }, [duration, isActive]);

    return (
        <div className="absolute top-0 left-0 w-full h-2 bg-[#8B4513]/20">
            <div
                ref={barRef}
                className="h-full bg-[#FFD700] rounded-full"
                style={{ width: isActive ? '100%' : '0%' }}
                aria-valuenow={isActive ? 100 : 0}
                aria-valuemax={100}
                aria-valuemin={0}
                role="progressbar"
            />
        </div>
    );
};

export default ProgressBar;
