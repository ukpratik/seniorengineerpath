// Serves dist/ like GitHub Pages does: /learn/x → /learn/x.html, /dir → /dir/index.html.
// The build prefixes links with the base path from SITE.url (src/config.ts); it is served here at
// that path and, so tests can use plain routes like '/map', at the root too.
// Used by the e2e tests and Lighthouse. Usage: node scripts/serve.mjs [port]
import { createServer } from 'node:http';
import { readFile, stat } from 'node:fs/promises';
import { join, extname, normalize } from 'node:path';

const root = join(process.cwd(), 'dist');
const port = Number(process.argv[2] ?? 4322);
const siteUrl = (await readFile(join(process.cwd(), 'src', 'config.ts'), 'utf8')).match(/\burl: '([^']+)'/)?.[1] ?? '/';
const base = new URL(siteUrl).pathname.replace(/\/+$/, '');
const types = { '.html': 'text/html; charset=utf-8', '.css': 'text/css', '.js': 'text/javascript', '.svg': 'image/svg+xml', '.woff2': 'font/woff2', '.json': 'application/json', '.png': 'image/png', '.xml': 'application/xml', '.txt': 'text/plain' };

async function resolve(path) {
  if (base && (path === base || path.startsWith(`${base}/`) || path.startsWith(`${base}?`))) path = path.slice(base.length) || '/';
  const p = normalize(decodeURIComponent(path.split('?')[0])).replace(/^(\.\.[/\\])+/, '');
  for (const c of [p, `${p}.html`, join(p, 'index.html')]) {
    const f = join(root, c);
    try {
      if ((await stat(f)).isFile()) return f;
    } catch {}
  }
  return null;
}

createServer(async (req, res) => {
  const f = await resolve(req.url ?? '/');
  if (!f) {
    res.writeHead(404, { 'content-type': 'text/plain' }).end('Not found');
    return;
  }
  res.writeHead(200, { 'content-type': types[extname(f)] ?? 'application/octet-stream' }).end(await readFile(f));
}).listen(port, '127.0.0.1', () => console.log(`serving dist on http://127.0.0.1:${port}`));
