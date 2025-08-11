// TransitionSection: Bridge to members showcase
import React, { useEffect, useState } from 'react';

const TransitionSection = ({ membersCount }) => {
    const [count, setCount] = useState(0);

    useEffect(() => {
        let frame;
        if (count < membersCount) {
            frame = setTimeout(() => setCount(count + 1), 40);
        }
        return () => clearTimeout(frame);
    }, [count, membersCount]);

    return (
        <section className="w-full py-16 bg-[#F5E6D3] flex flex-col items-center justify-center relative">
            <div className="text-3xl md:text-4xl font-semibold text-[#3C1810] mb-4 animate-typewriter">
                Together with <span className="text-[#FFD700] font-bold">{count}</span> incredible souls, we created memories that will be cherished forever
            </div>
            <div className="absolute inset-0 bg-[url('/pattern.png')] opacity-10 pointer-events-none" />
        </section>
    );
};

export default TransitionSection;
