export default function Footer() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="bg-gray-900 border-t border-gray-800 mt-12">
      <div className="container mx-auto px-4 py-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {/* Brand */}
          <div>
            <div className="flex items-center gap-2 mb-4">
              <div className="w-10 h-10 bg-gradient-to-br from-primary-500 to-primary-700 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-xl">D</span>
              </div>
              <span className="text-white font-bold text-xl">
                Dex<span className="text-primary-500">Anime</span>
              </span>
            </div>
            <p className="text-gray-400 text-sm">
              Streaming anime terbaik dengan subtitle Indonesia. Nonton anime favoritmu kapan saja, dimana saja.
            </p>
          </div>

          {/* Quick Links */}
          <div>
            <h3 className="text-white font-semibold mb-4">Menu Cepat</h3>
            <ul className="space-y-2 text-gray-400 text-sm">
              <li>
                <a href="/" className="hover:text-primary-500 transition-colors">Beranda</a>
              </li>
              <li>
                <a href="/ongoing" className="hover:text-primary-500 transition-colors">Anime Ongoing</a>
              </li>
              <li>
                <a href="/completed" className="hover:text-primary-500 transition-colors">Anime Completed</a>
              </li>
              <li>
                <a href="/search" className="hover:text-primary-500 transition-colors">Pencarian</a>
              </li>
            </ul>
          </div>

          {/* Info */}
          <div>
            <h3 className="text-white font-semibold mb-4">Informasi</h3>
            <ul className="space-y-2 text-gray-400 text-sm">
              <li>Website streaming anime gratis</li>
              <li>Subtitle Indonesia berkualitas</li>
              <li>Update episode terbaru setiap hari</li>
              <li>Tersedia dalam berbagai resolusi</li>
            </ul>
          </div>
        </div>

        <div className="border-t border-gray-800 mt-8 pt-6 text-center">
          <p className="text-gray-400 text-sm">
            &copy; {currentYear} <span className="text-primary-500 font-semibold">DexAnime</span>. 
            All rights reserved. Made with ❤️ for anime lovers.
          </p>
          <p className="text-gray-500 text-xs mt-2">
            Disclaimer: Semua konten anime diperoleh dari sumber publik. Kami tidak meng-host file video apapun.
          </p>
        </div>
      </div>
    </footer>
  );
}
