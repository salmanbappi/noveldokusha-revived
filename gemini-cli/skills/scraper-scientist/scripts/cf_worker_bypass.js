/**
 * NovelDokusha Cloudflare Bypass Proxy
 * To be deployed as a Cloudflare Worker with 'Browser Rendering' enabled.
 */
import puppeteer from '@cloudflare/puppeteer';

export default {
  async fetch(request, env) {
    const url = new URL(request.url).searchParams.get('url');
    if (!url) return new Response('Missing url parameter', { status: 400 });

    try {
      // Launch a headless browser instance on Cloudflare's network
      const browser = await puppeteer.launch(env.MYBROWSER);
      const page = await browser.newPage();

      // Set a realistic User-Agent
      await page.setUserAgent('Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36');

      // Navigate to the target site
      await page.goto(url, { waitUntil: 'networkidle2', timeout: 30000 });

      // If Cloudflare challenge is present, wait for it to be solved automatically by browser rendering
      // We can also inject small delays if needed: await page.waitForTimeout(2000);

      const html = await page.content();
      await browser.close();

      return new Response(html, {
        headers: { 'Content-Type': 'text/html', 'Access-Control-Allow-Origin': '*' }
      });
    } catch (e) {
      return new Response(e.message, { status: 500 });
    }
  }
};
