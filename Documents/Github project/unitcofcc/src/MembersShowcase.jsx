// MembersShowcase: Grid of 21 members with animations and interactions
import React, { useState } from 'react';
import MemberCard from './components/MemberCard';

const MembersShowcase = ({ members }) => {
    const [search, setSearch] = useState('');
    const [filtered, setFiltered] = useState(members);

    // Search/filter logic
    const handleSearch = (e) => {
        const val = e.target.value;
        setSearch(val);
        setFiltered(members.filter(m => m.name.toLowerCase().includes(val.toLowerCase())));
    };

    // Shuffle logic
    const shuffleMembers = () => {
        setFiltered([...filtered].sort(() => Math.random() - 0.5));
    };

    return (
        <section className="w-full py-16 bg-[#FFF8F0] flex flex-col items-center">
            <h2 className="text-4xl font-bold text-[#3C1810] mb-8">Meet the Members</h2>
            <div className="mb-6 flex gap-4">
                <input type="text" value={search} onChange={handleSearch} placeholder="Search members..." className="p-2 rounded-lg border border-[#8B4513] focus:outline-none focus:ring-2 focus:ring-[#FFD700]" />
                <button onClick={shuffleMembers} className="px-4 py-2 bg-[#FFD700] rounded-lg text-[#3C1810] shadow hover:bg-[#FFD700]/80">Shuffle</button>
            </div>
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-6 w-full max-w-5xl">
                {filtered.map((member, idx) => (
                    <MemberCard key={member.id} member={member} delay={idx * 100} />
                ))}
            </div>
        </section>
    );
};

export default MembersShowcase;
