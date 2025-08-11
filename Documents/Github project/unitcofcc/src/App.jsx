
import React, { useState, useEffect, useMemo } from 'react';

import HeroSection from './HeroSection';
import PhotoSlideshow from './PhotoSlideshow';
import VideoSection from './VideoSection';
import TransitionSection from './TransitionSection';
import MembersShowcase from './MembersShowcase';
import Footer from './Footer';

// Main App component for Our Journey Together
function App() {
  // State for loading, slideshow, members, etc.
  const [isLoading, setIsLoading] = useState(true);
  const [currentPhotoIndex, setCurrentPhotoIndex] = useState(0);
  const [isAutoplay, setIsAutoplay] = useState(true);
  const [photosData, setPhotosData] = useState([]);
  const [membersData, setMembersData] = useState([]);

  // Simulate async loading
  useEffect(() => {
    setTimeout(() => setIsLoading(false), 1200);
  }, []);

  // Memoized data for performance
  const photoList = useMemo(() => [
    { src: '/IMG-20250806-WA0186.jpg', caption: 'Mentor as opening', filename: 'IMG-20250806-WA0186.jpg', timestamp: 'Day 1', location: 'ITS Camp' },
    { src: '/WhatsApp Image 2025-08-10 at 22.48.47_0e2dcefd (1).jpg', caption: 'Group arrival', filename: 'WhatsApp Image 2025-08-10 at 22.48.47_0e2dcefd (1).jpg', timestamp: 'Day 1', location: 'ITS Camp' },
    { src: 'setup-camp.jpg', caption: 'Setting up camp', timestamp: 'Day 1', location: 'Camp Area' },
    { src: 'morning-coffee.jpg', caption: 'Morning coffee ritual', timestamp: 'Day 2', location: 'Campfire' },
    { src: 'team-building-1.jpg', caption: 'Team building activity', timestamp: 'Day 2', location: 'Field' },
    { src: 'candid-moments-1.jpg', caption: 'Natural candid shots', timestamp: 'Day 2', location: 'Camp Area' },
    { src: 'group-discussion.jpg', caption: 'Group discussion', timestamp: 'Day 2', location: 'Circle' },
    { src: 'adventure-activity.jpg', caption: 'Adventure activity', timestamp: 'Day 3', location: 'Forest' },
    { src: 'meal-together.jpg', caption: 'Having a meal together', timestamp: 'Day 3', location: 'Dining Area' },
    { src: 'night-gathering.jpg', caption: 'Evening gathering', timestamp: 'Day 3', location: 'Campfire' },
    { src: 'sunrise-moment.jpg', caption: 'Watching sunrise', timestamp: 'Day 4', location: 'Hilltop' },
    { src: 'individual-growth.jpg', caption: 'Individual growth moments', timestamp: 'Day 4', location: 'Camp Area' },
    { src: 'group-challenge.jpg', caption: 'Group challenge', timestamp: 'Day 4', location: 'Field' },
    { src: 'reflection-time.jpg', caption: 'Reflection time', timestamp: 'Day 4', location: 'Camp Area' },
    { src: 'final-group-photo.jpg', caption: 'Closing group photo', timestamp: 'Day 4', location: 'ITS Camp' },
  ], []);

  const memberList = useMemo(() => [
    'Safratul Ulyaa Zahari', 'Yusuf Adji Pamungkas', 'Fathy Exa Assamy', 'Mohammad Daffa Balthazar Rusbini',
    'Ahza Ahnaf Adrymansyah', 'Danindra Dimitri Prabasatya', 'Sabrina Firly Safira Putri', 'Fayyadh Satria Utama',
    'Famya Lituhayu', 'Yehezkiel Christian Mc Kenzie Sihombing', 'Henry Tristan', 'Desanda Verdely Yulisar',
    'Muhammad Haekal Pashahudin', 'Aira Navela Islamy', 'Syafirah Destiah Dinawati', 'Princessa Ofelyn Christandria',
    'Wahyu Adriel Christoval', 'Caraka Taqwa Progresta', 'Lunetta Kirana Sari Hantoro', 'Muhammad Faqih Yusuf Al Banna',
    'Muhammad Razan Abid Baswedan'
  ], []);

  useEffect(() => {
    setPhotosData(photoList);
    setMembersData(memberList.map((name, idx) => ({
      id: idx + 1,
      name,
      avatar: '', // Placeholder
      role: '', // Optional
    })));
  }, [photoList, memberList]);

  // Loading state
  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-screen bg-gradient-to-br from-[#8B4513] via-[#D2691E] to-[#F4A460]">
        <span className="text-4xl font-bold text-[#FFF8F0] animate-pulse">Loading Memories...</span>
      </div>
    );
  }

  return (
    <div className="w-full min-h-screen overflow-x-hidden bg-gradient-to-br from-[#8B4513] via-[#D2691E] to-[#F4A460] flex flex-col">
      {/* Hero Section (fullscreen, no parent bg) */}
      <HeroSection />
      {/* Photo Slideshow */}
      <PhotoSlideshow photos={photosData} currentIndex={currentPhotoIndex} setCurrentIndex={setCurrentPhotoIndex} isAutoplay={isAutoplay} setIsAutoplay={setIsAutoplay} />
      {/* Video Section */}
      <VideoSection />
      {/* Transition Section */}
      <TransitionSection membersCount={membersData.length} />
      {/* Members Showcase */}
      <MembersShowcase members={membersData} />
      {/* Footer */}
      <Footer />
    </div>
  );
}

export default App;
