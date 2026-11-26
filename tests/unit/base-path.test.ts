import { describe, it, expect } from 'vitest';
import { prefixBase, addMetaCsp, redirectPage } from '../../src/content-tools/integration.ts';

describe('serving under a base path (ADR-008)', () => {
  it('prefixes root-relative URLs in attributes, once', () => {
    const html = '<a href="/learn/x">x</a><use href="/icons.svg#i-map"/><img src="/og/a.png"><a href="/seniorengineerpath/map">m</a><a href="//cdn.example/x">c</a><a href="https://e.com/a">e</a><a href="#s1">s</a>';
    const out = prefixBase(html, '/seniorengineerpath', 'html');
    expect(out).toContain('href="/seniorengineerpath/learn/x"');
    expect(out).toContain('href="/seniorengineerpath/icons.svg#i-map"');
    expect(out).toContain('src="/seniorengineerpath/og/a.png"');
    expect(out).not.toContain('/seniorengineerpath/seniorengineerpath');
    expect(out).toContain('href="//cdn.example/x"');
    expect(out).toContain('href="#s1"');
  });

  it('leaves inline script bodies alone but fixes script src', () => {
    const html = '<script>var a = \' href="/x"\';</script><script type="module" src="/_astro/a.js"></script>';
    const out = prefixBase(html, '/b', 'html');
    expect(out).toContain('var a = \' href="/x"\';');
    expect(out).toContain('src="/b/_astro/a.js"');
  });

  it('prefixes CSS url() and is a no-op at the root', () => {
    expect(prefixBase('src:url(/fonts/a.woff2)', '/b', 'css')).toBe('src:url(/b/fonts/a.woff2)');
    expect(prefixBase('<a href="/map">', '', 'html')).toBe('<a href="/map">');
  });

  it('adds a meta CSP that hashes inline scripts but not JSON-LD', () => {
    const out = addMetaCsp('<head><meta charset="utf-8"><script>go()</script><script type="application/ld+json">{"a":1}</script></head>');
    const csp = out.match(/http-equiv="Content-Security-Policy" content="([^"]+)"/)![1];
    expect(csp.match(/sha256-/g)).toHaveLength(1);
    expect(csp).not.toContain('frame-ancestors');
  });

  it('writes noindex redirect stubs', () => {
    const page = redirectPage('/b/about#privacy');
    expect(page).toContain('url=/b/about#privacy');
    expect(page).toContain('noindex');
  });
});
