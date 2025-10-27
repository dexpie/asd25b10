// Backend Server untuk Scrape Anoboy.be
// File: server.js (Node.js + Express)

import express from 'express';
import axios from 'axios';
import * as cheerio from 'cheerio';
import cors from 'cors';

const app = express();
const PORT = process.env.PORT || 5000;
const ANOBOY_BASE_URL = 'https://anoboy.be';

// Middleware
app.use(cors());
app.use(express.json());

// Helper: Delay untuk rate limiting
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

// Helper: Fetch HTML dari Anoboy
async function fetchHTML(url) {
  try {
    const response = await axios.get(url, {
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
      },
      timeout: 10000,
    });
    return response.data;
  } catch (error) {
    console.error(`Error fetching ${url}:`, error.message);
    throw error;
  }
}

// Helper: Parse anime card dari HTML
function parseAnimeCard($, element) {
  const $card = $(element);
  
  return {
    title: $card.find('h2, h3, .title').text().trim(),
    slug: $card.find('a').attr('href')?.split('/').filter(Boolean).pop() || '',
    thumbnail: $card.find('img').attr('src') || $card.find('img').attr('data-src') || '',
    episode: $card.find('.episode, .eps').text().trim(),
    type: $card.find('.type').text().trim() || 'TV',
    status: $card.find('.status').text().includes('Completed') ? 'Completed' : 'Ongoing',
    url: $card.find('a').attr('href') || '',
  };
}

// API Routes

// GET /api/latest - Latest anime releases
app.get('/api/latest', async (req, res) => {
  try {
    const html = await fetchHTML(ANOBOY_BASE_URL);
    const $ = cheerio.load(html);
    
    const animeList = [];
    
    // Parse anime cards (adjust selector based on actual HTML)
    $('.post, .anime-item, article').each((i, elem) => {
      if (i < 20) { // Limit 20 items
        try {
          const anime = parseAnimeCard($, elem);
          if (anime.title && anime.slug) {
            animeList.push(anime);
          }
        } catch (e) {
          console.error('Parse error:', e);
        }
      }
    });
    
    res.json({
      success: true,
      data: animeList,
      count: animeList.length,
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
    });
  }
});

// GET /api/ongoing - Ongoing anime
app.get('/api/ongoing', async (req, res) => {
  try {
    const page = req.query.page || 1;
    const url = `${ANOBOY_BASE_URL}/anime/?status=ongoing&page=${page}`;
    
    const html = await fetchHTML(url);
    const $ = cheerio.load(html);
    
    const animeList = [];
    $('.post, .anime-item, article').each((i, elem) => {
      try {
        const anime = parseAnimeCard($, elem);
        if (anime.title && anime.slug) {
          animeList.push(anime);
        }
      } catch (e) {
        console.error('Parse error:', e);
      }
    });
    
    res.json({
      success: true,
      data: animeList,
      page: parseInt(page),
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
    });
  }
});

// GET /api/completed - Completed anime
app.get('/api/completed', async (req, res) => {
  try {
    const page = req.query.page || 1;
    const url = `${ANOBOY_BASE_URL}/anime/?status=completed&page=${page}`;
    
    const html = await fetchHTML(url);
    const $ = cheerio.load(html);
    
    const animeList = [];
    $('.post, .anime-item, article').each((i, elem) => {
      try {
        const anime = parseAnimeCard($, elem);
        if (anime.title && anime.slug) {
          animeList.push(anime);
        }
      } catch (e) {
        console.error('Parse error:', e);
      }
    });
    
    res.json({
      success: true,
      data: animeList,
      page: parseInt(page),
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
    });
  }
});

// GET /api/anime/:slug - Anime detail
app.get('/api/anime/:slug', async (req, res) => {
  try {
    const { slug } = req.params;
    const url = `${ANOBOY_BASE_URL}/anime/${slug}/`;
    
    const html = await fetchHTML(url);
    const $ = cheerio.load(html);
    
    const anime = {
      title: $('h1.entry-title, .anime-title').text().trim(),
      slug: slug,
      thumbnail: $('.anime-poster img, .poster img').attr('src') || '',
      description: $('.synopsis, .description, .entry-content p').first().text().trim(),
      genres: [],
      status: $('.status').text().trim(),
      type: $('.type').text().trim(),
      rating: $('.rating').text().trim(),
      episodeList: [],
    };
    
    // Parse genres
    $('.genre a, .genres a').each((i, elem) => {
      anime.genres.push($(elem).text().trim());
    });
    
    // Parse episode list
    $('.episode-list a, .eps-list a').each((i, elem) => {
      const $ep = $(elem);
      anime.episodeList.push({
        number: i + 1,
        title: $ep.text().trim(),
        slug: $ep.attr('href')?.split('/').filter(Boolean).pop() || '',
        url: $ep.attr('href') || '',
      });
    });
    
    res.json({
      success: true,
      data: anime,
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
    });
  }
});

// GET /api/episode/:slug - Episode/watch data
app.get('/api/episode/:slug', async (req, res) => {
  try {
    const { slug } = req.params;
    const url = `${ANOBOY_BASE_URL}/${slug}/`;
    
    const html = await fetchHTML(url);
    const $ = cheerio.load(html);
    
    const streamLinks = [];
    
    // Parse video sources
    $('.video-source, .player-option, iframe').each((i, elem) => {
      const src = $(elem).attr('src') || $(elem).attr('data-src');
      if (src) {
        streamLinks.push({
          quality: '720p',
          url: src,
          provider: `Server ${i + 1}`,
        });
      }
    });
    
    const watchData = {
      anime: {
        title: $('h1.entry-title').text().trim(),
        slug: slug,
      },
      episode: {
        number: parseInt(slug.match(/episode-(\d+)/)?.[1] || '1'),
        title: $('h1.entry-title').text().trim(),
        slug: slug,
      },
      streamLinks: streamLinks,
    };
    
    res.json({
      success: true,
      data: watchData,
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
    });
  }
});

// GET /api/search - Search anime
app.get('/api/search', async (req, res) => {
  try {
    const { q, page = 1 } = req.query;
    const url = `${ANOBOY_BASE_URL}/?s=${encodeURIComponent(q)}&page=${page}`;
    
    const html = await fetchHTML(url);
    const $ = cheerio.load(html);
    
    const results = [];
    $('.post, .anime-item, article').each((i, elem) => {
      try {
        const anime = parseAnimeCard($, elem);
        if (anime.title && anime.slug) {
          results.push(anime);
        }
      } catch (e) {
        console.error('Parse error:', e);
      }
    });
    
    res.json({
      success: true,
      data: results,
      query: q,
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
    });
  }
});

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'OK', message: 'Anoboy Scraper API Running' });
});

// Start server
app.listen(PORT, () => {
  console.log(`🚀 Anoboy Scraper API running on http://localhost:${PORT}`);
  console.log(`📡 Scraping from: ${ANOBOY_BASE_URL}`);
  console.log(`\nAvailable endpoints:`);
  console.log(`  GET /api/latest`);
  console.log(`  GET /api/ongoing?page=1`);
  console.log(`  GET /api/completed?page=1`);
  console.log(`  GET /api/anime/:slug`);
  console.log(`  GET /api/episode/:slug`);
  console.log(`  GET /api/search?q=query`);
});

export default app;
