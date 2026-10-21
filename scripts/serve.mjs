// Serves dist/ like Cloudflare Pages does: /learn/x → /learn/x.html, /dir → /dir/index.html.
// Used by the e2e tests and for local checks. Usage: node scripts/serve.mjs [port]
import { createServer } from 'node:http';
import { readFile, stat } from 'node:fs/promises';
import { join, extname, normalize } from 'node:path';

const root = join(process.cwd(), 'dist');
const port = Number(process.argv[2] ?? 4322);
const types = { '.html': 'text/html; charset=utf-8', '.css': 'text/css', '.js': 'text/javascript', '.svg': 'image/svg+xml', '.woff2': 'font/woff2', '.json': 'application/json', '.png': 'image/png', '.xml': 'application/xml', '.txt': 'text/plain' };

async function resolve(path) {
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
