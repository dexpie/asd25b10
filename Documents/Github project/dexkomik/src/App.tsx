import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import HomePage from '@/pages/HomePage';
import AnimeDetailPage from '@/pages/AnimeDetailPage';
import WatchPage from '@/pages/WatchPage';
import SearchPage from '@/pages/SearchPage';

function App() {
  return (
    <Router>
      <div className="flex flex-col min-h-screen bg-gray-900">
        <Header />
        <main className="flex-1">
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/ongoing" element={<HomePage />} />
            <Route path="/completed" element={<HomePage />} />
            <Route path="/anime/:slug" element={<AnimeDetailPage />} />
            <Route path="/watch/:slug" element={<WatchPage />} />
            <Route path="/search" element={<SearchPage />} />
          </Routes>
        </main>
        <Footer />
      </div>
    </Router>
  );
}

export default App;
