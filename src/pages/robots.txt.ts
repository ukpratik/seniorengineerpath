// robots.txt built from SITE.url, so the sitemap line and paths follow the site's address.
// Note: crawlers read robots.txt only at a host's root. Under a GitHub Pages project path this file
// is informational; submit the sitemap in Search Console / Bing Webmaster Tools instead.
import { BASE_PATH, absUrl } from '../config.ts';

export function GET() {
  const body = `User-agent: *\nAllow: /\nDisallow: ${BASE_PATH}/data/\nSitemap: ${absUrl('/sitemap.xml')}\n`;
  return new Response(body, { headers: { 'content-type': 'text/plain; charset=utf-8' } });
}
