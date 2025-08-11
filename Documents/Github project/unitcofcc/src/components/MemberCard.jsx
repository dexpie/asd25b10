// MemberCard: Individual member card with animation and modal
import React, { useState } from 'react';

/**
 * MemberCard displays a member with avatar, name, and role.
 * On click, shows modal with more details.
 * Accessibility: focus, keyboard, alt text.
 */
const MemberCard = ({ member, delay }) => {
    const [showModal, setShowModal] = useState(false);

    return (
        <div
            className="bg-gradient-to-br from-[#F5E6D3] via-[#FFD700]/20 to-[#FFF8F0] rounded-xl shadow-lg hover:shadow-2xl transition-transform duration-300 hover:-translate-y-1 flex flex-col items-center p-6 cursor-pointer"
            style={{ transitionDelay: `${delay}ms` }}
            tabIndex={0}
            role="button"
            aria-label={`View details for ${member.name}`}
            onClick={() => setShowModal(true)}
            onKeyDown={e => e.key === 'Enter' && setShowModal(true)}
        >
            {/* Avatar placeholder */}
            <div className="w-20 h-20 rounded-full bg-[#8B4513]/30 flex items-center justify-center mb-4">
                <span className="text-3xl text-[#8B4513]">👤</span>
            </div>
            {/* Name and role */}
            <div className="text-lg font-bold text-[#3C1810] truncate w-40 text-center">{member.name}</div>
            {member.role && <div className="text-sm text-[#8B7355] mt-1">{member.role}</div>}
            {/* Modal for details */}
            {showModal && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50" onClick={() => setShowModal(false)}>
                    <div className="bg-[#FFF8F0] rounded-2xl shadow-2xl p-8 max-w-sm w-full relative" onClick={e => e.stopPropagation()}>
                        <button className="absolute top-2 right-2 text-[#3C1810] text-xl" onClick={() => setShowModal(false)} aria-label="Close">×</button>
                        <div className="w-24 h-24 rounded-full bg-[#8B4513]/30 flex items-center justify-center mb-4 mx-auto">
                            <span className="text-4xl text-[#8B4513]">👤</span>
                        </div>
                        <div className="text-2xl font-bold text-[#3C1810] mb-2 text-center">{member.name}</div>
                        {member.role && <div className="text-base text-[#8B7355] mb-2 text-center">{member.role}</div>}
                        <div className="text-sm text-[#3C1810] text-center">Unit Coffee ITS Camp 2025 Member</div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default MemberCard;
