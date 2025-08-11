// Smooth scroll untuk navigasi
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        document.querySelector(this.getAttribute('href')).scrollIntoView({
            behavior: 'smooth'
        });
    });
});

// Menambahkan efek fade-in saat elemen muncul
document.addEventListener("DOMContentLoaded", function () {
    const elements = document.querySelectorAll('.fade-in-up');
    elements.forEach(element => {
        element.classList.add('aos-animate');
    });

    // Inisialisasi AOS (Animate On Scroll)
    AOS.init();
});

// Mengubah teks quote setiap 10 detik dengan kutipan dari Frieren dan Himmel
const quotes = [
    '"Orang-orang hidup untuk dikenang." - Frieren',
    '"Aku ingin tahu apa yang akan aku lakukan jika aku bisa merasakan waktu yang lebih lama." - Frieren',
    '"Hidup ini adalah perjalanan, bukan tentang mencapai tujuan." - Frieren',
    '"Terkadang, hal terbaik yang bisa kita lakukan adalah menikmati perjalanan itu, bukan hanya fokus pada akhir." - Himmel',
    '"Kekuatan sejati bukanlah tentang berapa banyak yang kita miliki, tapi bagaimana kita menghadapinya." - Frieren',
    '"Kenangan adalah apa yang tetap hidup setelah kita pergi." - Himmel',
    '"Apa yang lebih penting daripada pencapaian adalah siapa yang kita temui di sepanjang perjalanan." - Frieren',
    '"Hidup adalah sesuatu yang berharga, tetapi kita harus menyadari bahwa itu bisa cepat berlalu." - Frieren',
    '"Keindahan hidup adalah dalam hubungan yang kita bangun, bukan dalam waktu yang kita miliki." - Himmel',
    '"Aku mungkin tidak akan bisa menyelesaikan semua hal, tapi aku ingin mencoba." - Frieren'
];

let currentQuoteIndex = 0;
const quoteElement = document.querySelector('.quote-section h1');

// Fungsi untuk mengubah quote
function changeQuote() {
    currentQuoteIndex = (currentQuoteIndex + 1) % quotes.length; // Menghitung index berikutnya
    quoteElement.textContent = quotes[currentQuoteIndex];  // Mengubah teks pada elemen
}

// Mengubah quote setiap 10 detik
setInterval(changeQuote, 10000); // 10000ms = 10 detik

// Mengubah quote saat halaman dimuat pertama kali
changeQuote();

// Menambahkan event listener pada tombol untuk switch dark mode
const toggleButton = document.getElementById('dark-mode-toggle');

// Mengecek apakah pengguna sebelumnya memilih dark mode
if (localStorage.getItem('dark-mode') === 'enabled') {
    document.body.classList.add('dark-mode');
}

// Fungsi untuk toggle dark mode
if (toggleButton) {
    toggleButton.addEventListener('click', () => {
        document.body.classList.toggle('dark-mode');

        // Menyimpan preferensi pengguna di localStorage
        if (document.body.classList.contains('dark-mode')) {
            localStorage.setItem('dark-mode', 'enabled');
        } else {
            localStorage.setItem('dark-mode', 'disabled');
        }
    });
}

// Menambahkan tooltip untuk setiap item di Tech Stack
const techItems = document.querySelectorAll('.tech-item');

techItems.forEach(item => {
    item.addEventListener('mouseenter', function () {
        const techName = item.querySelector('p').textContent;
        const tooltip = document.createElement('span');
        tooltip.classList.add('tooltip');
        tooltip.textContent = `Teknologi: ${techName}`;
        item.appendChild(tooltip);
    });

    item.addEventListener('mouseleave', function () {
        const tooltip = item.querySelector('.tooltip');
        if (tooltip) tooltip.remove();
    });
});

// Filter Projects
const filterButtons = document.querySelectorAll('.filter-btn');
const portfolioItems = document.querySelectorAll('.item');

filterButtons.forEach(button => {
    button.addEventListener('click', () => {
        const filter = button.getAttribute('data-filter');

        // Toggle active class for buttons
        filterButtons.forEach(btn => btn.classList.remove('active'));
        button.classList.add('active');

        portfolioItems.forEach(item => {
            if (filter === 'all') {
                item.style.display = 'block';
            } else if (item.classList.contains(filter)) {
                item.style.display = 'block';
            } else {
                item.style.display = 'none';
            }
        });
    });
});

// loading.js - Mengatur tampilan loading screen

