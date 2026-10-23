---
topic: web-performance
subtitle: Measure how fast your pages feel to real users with LCP, INP and CLS, and fix the few things that matter most.
status: published
illustrative_story: false
sources:
  - id: vodafone
    title: "web.dev, Vodafone case study: LCP improvement and sales (2021)"
    url: https://web.dev/case-studies/vodafone
    note: An A/B test of two landing pages that differed only in Web Vitals work, with the changes and the business results.
  - id: redbus
    title: "Amit Kumar and Saurabh Rajpal, “How redBus improved their website's Interaction to Next Paint (INP)”, web.dev (case study, 2023)"
    url: https://web.dev/case-studies/redbus-inp
    note: How redBus found slow interactions with field data and fixed them, and the sales result.
  - id: vitals
    title: "web.dev, “Web Vitals”"
    url: https://web.dev/articles/vitals
    note: The three Core Web Vitals, their good thresholds, the 75th percentile rule, and which tools give lab or field data.
  - id: thresholds
    title: "web.dev, “How the Core Web Vitals metrics thresholds were defined”"
    url: https://web.dev/articles/defining-core-web-vitals-thresholds
    note: Why Google chose the 75th percentile, and how the 2.5 s and 0.1 thresholds were picked.
  - id: lcp
    title: "web.dev, “Largest Contentful Paint (LCP)”"
    url: https://web.dev/articles/lcp
    note: What counts as the largest element, and the good and poor values.
  - id: optimize-lcp
    title: "web.dev, “Optimize Largest Contentful Paint”"
    url: https://web.dev/articles/optimize-lcp
    note: The four parts of LCP and how to shorten each one.
  - id: inp
    title: "web.dev, “Interaction to Next Paint (INP)”"
    url: https://web.dev/articles/inp
    note: Which interactions count, the three phases of an interaction, and the thresholds.
  - id: inp-launch
    title: "web.dev blog, announcement that INP is a Core Web Vital (March 2024)"
    url: https://web.dev/blog/inp-cwv-launch
    note: The announcement that INP replaced First Input Delay as a Core Web Vital.
  - id: long-tasks
    title: "web.dev, “Optimize long tasks”"
    url: https://web.dev/articles/optimize-long-tasks
    note: What a long task is, why it blocks input, and how to yield to the main thread.
  - id: cls
    title: "web.dev, “Cumulative Layout Shift (CLS)”"
    url: https://web.dev/articles/cls
    note: How layout shifts are scored, which shifts count, and the common causes.
  - id: optimize-cls
    title: "web.dev, “Optimize Cumulative Layout Shift”"
    url: https://web.dev/articles/optimize-cls
    note: Image sizes, space for ads, font settings and safe animations.
  - id: ttfb
    title: "web.dev, “Time to First Byte (TTFB)”"
    url: https://web.dev/articles/ttfb
    note: What TTFB includes and the rough 0.8 s guide.
  - id: mdn-crp
    title: "MDN Web Docs, “Critical rendering path”"
    url: https://developer.mozilla.org/en-US/docs/Web/Performance/Guides/Critical_rendering_path
    note: DOM, CSSOM, render tree, layout and paint, and why CSS blocks rendering.
  - id: lab-field
    title: "web.dev, “Why lab and field data can be different (and what to do about it)”"
    url: https://web.dev/articles/lab-and-field-data-differences
    note: Why a Lighthouse run and your real users often disagree.
  - id: lighthouse
    title: "Chrome for Developers, “Introduction to Lighthouse”"
    url: https://developer.chrome.com/docs/lighthouse/overview
    note: What Lighthouse is and the ways to run it.
  - id: crux
    title: "Chrome for Developers, “Overview of CrUX”"
    url: https://developer.chrome.com/docs/crux
    note: What the Chrome UX Report is.
  - id: crux-method
    title: "Chrome for Developers, “CrUX methodology”"
    url: https://developer.chrome.com/docs/crux/methodology
    note: Which Chrome users and platforms are included, and which are not.
  - id: web-vitals-js
    title: "GoogleChrome, “web-vitals” library (GitHub)"
    url: https://github.com/GoogleChrome/web-vitals
    note: A small library to measure LCP, INP and CLS on real users and send them to your analytics.
  - id: code-splitting
    title: "web.dev, “Reduce JavaScript payloads with code splitting”"
    url: https://web.dev/articles/reduce-javascript-payloads-with-code-splitting
    note: Why less JavaScript at startup helps INP, and how dynamic import() splits a bundle.
  - id: lazy
    title: "web.dev, “Browser-level image lazy loading for the web”"
    url: https://web.dev/articles/browser-level-image-lazy-loading
    note: How loading="lazy" works, and why images in the first view should not use it.
  - id: preconnect
    title: "web.dev, “Establish network connections early to improve perceived page speed”"
    url: https://web.dev/articles/preconnect-and-dns-prefetch
    note: What preconnect does, the savings, and why too many preconnects hurt.
  - id: rendering
    title: "web.dev, “Rendering on the Web”"
    url: https://web.dev/articles/rendering-on-the-web
    note: Server-side, static and client-side rendering and hydration, and how each affects TTFB and INP.
  - id: budgets
    title: "web.dev, “Performance budgets 101”"
    url: https://web.dev/articles/performance-budgets-101
    note: Types of budgets and tools that enforce them in the build.
  - id: tbt
    title: "web.dev, “Total Blocking Time (TBT)”"
    url: https://web.dev/articles/tbt
    note: How TBT adds up main-thread blocking after First Contentful Paint, and why it is only a lab proxy for INP.
  - id: lhci
    title: "GoogleChrome, “Lighthouse CI” (GitHub)"
    url: https://github.com/GoogleChrome/lighthouse-ci
    note: Run Lighthouse on every commit and fail on regressions.
claims_checked:
  - claim: "Vodafone A/B test: 50/50 split, about 34K visits a day per version, only difference was Web Vitals work; LCP 8.3 s to 5.7 s (31% better), +8% sales, +15% lead-to-visit, +11% cart-to-visit, DCL 15% worse."
    source: vodafone
    note: All numbers match the case study (March 2021). It does not say whether 8.3 s and 5.7 s are averages, so "average" was removed. Traffic channels corrected to display, iOS/Android, search and social.
  - claim: "Core Web Vitals good thresholds LCP ≤ 2.5 s, INP ≤ 200 ms, CLS ≤ 0.1; poor above 4.0 s, 500 ms and 0.25; judged at the 75th percentile, split by mobile and desktop; a page passes only if all three are good."
    source: vitals
    note: Good values and the 75th percentile rule from the Web Vitals page. Poor values confirmed on the lcp, inp and cls pages. The 75th percentile reasoning matches the thresholds article.
  - claim: INP replaced First Input Delay as a Core Web Vital in March 2024.
    source: inp-launch
    note: 'Post dated 2024-03-12: "Interaction to Next Paint is now a stable Core Web Vital metric, replacing First Input Delay."'
  - claim: "LCP has four parts (TTFB, load delay, load duration, render delay), about 40/10/40/10; never lazy-load the LCP image; start loading it with the first resource."
    source: optimize-lcp
    note: Matches the article, which calls the split "guidelines, not strict rules". Lazy loading makes the image start later, not always load later, so the trade-off table was reworded.
  - claim: "A long task is over 50 ms; the main thread does one task at a time and blocks interactions while it runs; yield with scheduler.yield() or setTimeout."
    source: long-tasks
    note: Quoted closely from the article.
  - claim: "Lab tests use one device and network and usually a cold cache, and cannot predict taps; TBT helps diagnose INP but is not a substitute."
    source: lab-field
    note: TBT wording softened from "the lab metric for INP". The web.dev TBT page says it "may be a reasonable proxy metric for INP for the lab but it's not a substitute for INP", now cited as tbt.
  - claim: "CrUX excludes Chrome on iOS, Android WebView apps and other Chromium browsers such as Microsoft Edge; the web-vitals library is about 3 KB brotli."
    source: crux-method
    note: Exclusions quoted from the methodology page; size (~3K, brotli'd) from the web-vitals README.
  - claim: "redBus cut search-page INP from 870–900 to 350–370 by loading 10 results per batch instead of 30, and reported a 7% overall increase in sales."
    source: redbus
    note: The case study gives no unit for these values. The lesson now says so, and cites the INP page for INP being in milliseconds.
  - claim: "CSS is render-blocking because later rules can override earlier ones; some requests halt HTML parsing; defer, async or remove non-critical resources."
    source: mdn-crp
    note: MDN says "some requests are blocking", not specifically scripts, so the sentence was reworded.
  - claim: "Static rendering gives fast TTFB; SSR can raise TTFB; hydration can hurt INP and the page can look ready but not respond; CSR JavaScript grows and can hurt INP."
    source: rendering
    note: Each cell matches a sentence in the article. The CSR first-content cell is plain mechanism.
  - claim: "Budget example \"less than 170 KB of JavaScript on mobile\" for a product page; Lighthouse CI runs on every commit to prevent regressions; preconnect saves 100–500 ms but too many compete for bandwidth."
    source: budgets
    note: Budget quote exact. Lighthouse CI README says "running Lighthouse for every commit ... preventing regressions" (it does not say it fails builds, so that wording was dropped). Preconnect numbers from the preconnect article.
reviewed_by: ai-factcheck
reviewed_on: 2026-10-22
quiz:
  - q: "Which three metrics are the Core Web Vitals today?"
    options:
      - "LCP, FID and CLS"
      - "LCP, INP and CLS"
      - "TTFB, LCP and CLS"
    answer: 1
    why: "INP replaced FID as a Core Web Vital in March 2024. TTFB is useful, but it is not a Core Web Vital."
  - q: "Your field data shows LCP of 2.1 s at the median and 3.4 s at the 75th percentile, on phones. Does the page pass?"
    options:
      - "Yes, because the median is under 2.5 s."
      - "No, because the 75th percentile is over 2.5 s."
      - "It cannot be known without a Lighthouse score."
    answer: 1
    why: "Google judges each metric at the 75th percentile of page loads. 3.4 s is above the 2.5 s good limit."
  - q: "A Lighthouse run in your CI gives a perfect score, but real users report a slow page. What is the most likely reason?"
    options:
      - "Lighthouse is broken."
      - "Lab data uses one device and network and a cold cache, so it misses what real users get, including their taps."
      - "Real users always have slower devices than any test can model, so field data cannot be trusted."
    answer: 1
    why: "Lab data is repeatable but limited. Field data covers real devices, networks and interactions. Use both."
  - q: "Users tap a filter button and the page freezes for half a second. Which fix targets the cause?"
    options:
      - "Add loading=\"lazy\" to the hero image."
      - "Break the long JavaScript task into smaller pieces and yield to the main thread."
      - "Add a preconnect to the image CDN."
    answer: 1
    why: "A long task on the main thread delays the response to input, which raises INP. Splitting it lets the browser handle the tap sooner."
  - q: "Which change most directly lowers CLS?"
    options:
      - "Set width and height on images and reserve space for ads."
      - "Move the site to HTTP/3."
      - "Lazy-load every image, including the hero."
    answer: 0
    why: "Images and ads with unknown sizes push content down when they load. Reserving their space stops the move."
incidents: []
related: [cdn-http-caching, http-versions, ci-cd, observability-slos, latency-estimation]
---

## What broke

Vodafone had a landing page that people reached from paid ads: display, search, social, and iOS and Android. The team wanted to know if performance work alone would change sales. [@vodafone]

So they ran an A/B test. Half the traffic went to the old page and half to a faster one. The only difference was the Web Vitals work on the faster page. Each version got about 34,000 visits a day. [@vodafone]

The faster page did three things. It moved a widget's rendering from the browser to the server, so less JavaScript blocked rendering. It rendered the important HTML on the server. And it resized the hero image, made SVGs and PNGs smaller, and did not load images outside the screen. [@vodafone]

They measured real users with the browser's PerformanceObserver API. Largest Contentful Paint dropped from 8.3 seconds to 5.7 seconds, 31% better. Total sales rose by 8%. The lead-to-visit rate rose by 15%, and the cart-to-visit rate by 11%. [@vodafone]

Note one detail. An older metric, DOMContentLoaded, got 15% *worse* on the faster page. [@vodafone] If the team had watched only that number, they could have rolled back a change that made money. You must measure what users feel.

## The idea in simple words

A page can be slow in three different ways. It can be slow to show its main content. It can be slow to react when you tap. Or it can jump around while you read. Google's Core Web Vitals give one number for each. [@vitals]

| Metric | What it measures | Good | Poor |
|---|---|---|---|
| LCP | When the largest image, text block or video in view is shown [@lcp] | ≤ 2.5 s | > 4.0 s [@lcp] |
| INP | How fast the page shows a response to clicks, taps and key presses [@inp] | ≤ 200 ms | > 500 ms [@inp] |
| CLS | How much content moves without the user asking [@cls] | ≤ 0.1 | > 0.25 [@cls] |

INP replaced First Input Delay (FID) as a Core Web Vital in March 2024. [@inp-launch] If an old dashboard still shows FID, it is out of date.

You judge each metric at the 75th percentile of page loads, for phones and desktops separately. A page passes only if all three are good at that point. [@vitals] Google chose the 75th percentile so that most visits get the target experience, while a few outliers do not move the number much. [@thresholds]

> [!key] **The key idea.** Speed is three separate problems: loading, responding and staying still. Measure all three on real users, at the 75th percentile, and fix the biggest one first.

## How it works

Step through one page load, from the first request to the first tap. Use Next and Back, or the ← and → keys.

[[diagram:page-load-vitals]]

### The critical rendering path

The browser turns your files into pixels in a fixed order. It parses HTML into the DOM and CSS into the CSSOM. It combines them into a render tree, works out the layout, then paints. [@mdn-crp]

CSS is render-blocking: the browser waits for all the CSS before it paints, because later rules can change earlier ones. Some requests, such as scripts, can also stop the HTML parser until the file is handled. [@mdn-crp] So stylesheets, and scripts without `defer` or `async`, in the head can delay the first paint. MDN's advice: defer what is not critical, mark scripts async, or remove them. [@mdn-crp]

### LCP: four parts

LCP has four parts. First comes the time to first byte. Then there is a delay before the LCP resource starts loading, its load time, and a delay before it is painted. The web.dev guide suggests about 40% for TTFB, about 40% for the load, and under 10% for each delay. [@optimize-lcp]

TTFB covers redirects, DNS, the connection, TLS and the server's work. As a rough guide, most sites should aim for 0.8 seconds or less. TTFB is not a Core Web Vital [@ttfb], but it is the first part of LCP. [@optimize-lcp] The [latency lesson](/learn/latency-numbers-and-estimation) shows why far-away servers cost so much.

### INP: the main thread

Almost all of your JavaScript runs on one main thread, which does one task at a time. A task longer than 50 ms is a long task. While it runs, the browser cannot handle a tap. [@long-tasks]

An interaction has three phases: the input delay, the time your event handlers run, and the time until the next frame is shown. [@inp] A long task that is already running makes the first phase long. A slow handler makes the second phase long.

### CLS: things that move

A layout shift counts when it is unexpected. Shifts within 500 ms of a tap, click or key press do not count. [@cls] The common causes are images and videos without a size, web fonts that render larger or smaller than the fallback font, and ads or widgets that resize themselves. [@cls]

### The biggest fixes

**Send less JavaScript.** Large bundles cost time to parse, compile and run, and that work blocks the main thread. Split the bundle with dynamic `import()` and send only what the first view needs. [@code-splitting]

**Break up long tasks.** Yield to the main thread between pieces of work, for example with `scheduler.yield()` or `setTimeout`. [@long-tasks]

**Make the LCP image early and small.** Start loading it at the same time as the first resource on the page. If it is hidden in CSS or JavaScript, preload it with `fetchpriority="high"`. Never lazy-load it. [@optimize-lcp] Use `loading="lazy"` only for images below the first view. [@lazy]

**Reserve space.** Set `width` and `height` on every image and video, or use CSS `aspect-ratio`. Reserve space for ads with `min-height`. For fonts, use `font-display: optional`, or use `size-adjust` so the fallback font takes up about the same space. Animate with `transform`, not `top` or `left`. [@optimize-cls]

**Connect early.** `rel="preconnect"` lets the browser open a connection to an important third-party origin early. The web.dev guide reports 100–500 ms saved, but too many preconnects compete for bandwidth. [@preconnect]

**Cut TTFB and cache.** Serve static files and cacheable HTML from a CDN close to users. The [CDN and caching lesson](/learn/cdn-and-http-caching) shows how. Fewer round trips also help, see the [HTTP versions lesson](/learn/http-1-2-3-and-quic).

### Lab data and field data

Lab data comes from loading a page in a controlled setup. Field data comes from all the real users who visit it. [@lab-field] Lighthouse is a lab tool. [@vitals] You can run it in Chrome DevTools, from the command line, as a Node module or in PageSpeed Insights. [@lighthouse]

The two often disagree. Lab tests use one device, one network and usually a cold cache. They cannot predict when a user will tap, so they cannot measure INP directly. Total Blocking Time is a lab metric meant to help diagnose INP problems. [@lab-field] But it is not a substitute for INP, so a good TBT does not promise a good INP. [@tbt]

For field data, the Chrome UX Report (CrUX) shows how real Chrome users experience popular sites. [@crux] But it leaves out Chrome on iOS, Android WebViews and other Chromium browsers such as Microsoft Edge. [@crux-method] So also collect your own real user monitoring. The `web-vitals` library, about 3 KB compressed, reports LCP, INP and CLS from each visit to your own endpoint. [@web-vitals-js]

redBus did this. Its real user data showed slow interactions on the search page. Loading 10 results per batch instead of 30 cut INP there from about 870–900 to 350–370. [@redbus] The case study gives no unit, but INP is measured in milliseconds. [@inp] Overall, redBus reported 7% more sales. [@redbus]

## Trade-offs

| Option | Fast first content? | Good INP? | Use it when |
|---|---|---|---|
| Static rendering | Yes. HTML is ready, so TTFB stays low. [@rendering] | Yes, if you ship little JavaScript. [@rendering] | Pages that are the same for everyone: blogs, docs, landing pages. |
| Server-side rendering | Yes, but building HTML per request can raise TTFB. [@rendering] | Depends. Hydration can hurt INP, because the page looks ready before it can respond. [@rendering] | Personal or fast-changing pages that still need content in the first HTML. |
| Client-side rendering | No. The browser must download and run JavaScript first. | Gets worse as the bundle grows. [@rendering] | Apps behind a login, used for a long time per visit. |
| Lazy-load every image | No. A lazy LCP image starts loading later. [@optimize-lcp] | No effect. | Never. Lazy-load only below the first view. |
| Lazy-load below the first view | Yes. Fewer bytes compete with the LCP image. | No effect. | Long pages, lists and feeds. Set image sizes too. [@lazy] |

## At work: how a senior uses this

- Report LCP, INP and CLS at the 75th percentile, split by phone and desktop. Treat them like SLOs, as in the [observability lesson](/learn/observability-slos-error-budgets).
- Ask "which metric is bad, for which pages, on which devices?" before anyone writes a fix.
- Add a performance budget to CI. The web.dev example is "less than 170 KB of JavaScript on mobile" for a product page. Tools like Lighthouse CI run checks on every commit to catch regressions. [@budgets] [@lhci] See the [CI/CD lesson](/learn/ci-cd-trunk-based-development).
- Review every new third-party script like a dependency. Ask who owns it and what it costs on the main thread.
- Check that the LCP image is in the HTML, not lazy, and sized for the screen.
- When you show speed work to the business, use a real A/B test, like Vodafone did. [@vodafone]

## With AI

### What to ask the agent

> "Find the LCP element on our product page. Check that it is in the initial HTML, not lazy-loaded, has width and height, and uses fetchpriority=high. List what to change."

> "Find event handlers in this component that do more than 50 ms of work. Propose how to split them and yield to the main thread, without changing behaviour."

> "Add the web-vitals library to send LCP, INP and CLS to our /analytics endpoint, with the page type and device type on each event."

### What to check yourself

- The agent did not add `loading="lazy"` to every image, including the hero.
- Any new `preload` or `preconnect` is for a resource the page truly needs early. Too many slow other requests down.
- Code splitting did not move needed code behind an `import()` that runs on the first tap.
- You measured the change on real users, not only in one Lighthouse run.

## Check yourself

## Go deeper

Related lessons: [CDN and HTTP caching](/learn/cdn-and-http-caching), [HTTP/1.1, HTTP/2 and HTTP/3](/learn/http-1-2-3-and-quic), [CI/CD and trunk-based development](/learn/ci-cd-trunk-based-development) and [Observability, SLOs and error budgets](/learn/observability-slos-error-budgets).
