---
topic: rag
subtitle: Ground answers in your own documents, check who may read them, and measure retrieval before you blame the model.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
review_by: 2027-01-19
illustrative_story: false
sources:
  - id: moffatt
    title: "Civil Resolution Tribunal (British Columbia), Moffatt v. Air Canada, 2024 BCCRT 149, decision of 14 February 2024"
    url: https://decisions.civilresolutionbc.ca/crt/crtd/en/525448/1/document.do
    note: The full decision (a short PDF). Read paragraphs 13 to 29 for what the chatbot said and why the airline was responsible, and paragraph 44 for the amount.
  - id: lewis2020
    title: "Lewis et al., “Retrieval-Augmented Generation for Knowledge-Intensive NLP Tasks”, NeurIPS 2020 (arXiv:2005.11401)"
    url: https://arxiv.org/abs/2005.11401
    note: The paper that named RAG. The abstract explains why knowledge stored only in model weights is hard to update and to trace.
  - id: contextual
    title: "Anthropic, “Introducing Contextual Retrieval” (September 2024)"
    url: https://www.anthropic.com/engineering/contextual-retrieval
    note: Chunking, BM25 plus embeddings, re-ranking, and measured retrieval failure rates. Also when to skip RAG and put everything in the prompt.
  - id: openai-emb
    title: "OpenAI, “Vector embeddings”, API documentation"
    url: https://developers.openai.com/api/docs/guides/embeddings
    note: A neutral definition of an embedding and how distance between vectors measures relatedness.
  - id: pgvector
    title: "pgvector, README (GitHub)"
    url: https://github.com/pgvector/pgvector
    note: Vector search inside Postgres. Index types, filtering with WHERE, and hybrid search with full-text search.
  - id: es-rrf
    title: "Elastic, “Reciprocal rank fusion”, Elasticsearch reference"
    url: https://www.elastic.co/docs/reference/elasticsearch/rest-apis/reciprocal-rank-fusion
    note: How to merge several result lists with one simple formula, and the default value of its constant k.
  - id: claude-evals
    title: "Anthropic, “Define success criteria and build evaluations”, Claude documentation"
    url: https://platform.claude.com/docs/en/test-and-evaluate/develop-tests
    note: How to write measurable success criteria and build a test set that you can grade automatically.
  - id: claude-halluc
    title: "Anthropic, “Reduce hallucinations”, Claude documentation"
    url: https://platform.claude.com/docs/en/test-and-evaluate/strengthen-guardrails/reduce-hallucinations
    note: Let the model say "I don't know", ground it in quotes, and make it cite.
  - id: claude-models
    title: "Anthropic, “Models overview”, Claude documentation"
    url: https://platform.claude.com/docs/en/models/overview
    note: Each model's "reliable knowledge cutoff" date and what that date means.
  - id: owasp-llm01
    title: "OWASP, “LLM01:2025 Prompt Injection”, OWASP Top 10 for LLM Applications 2025"
    url: https://genai.owasp.org/llmrisk/llm01-prompt-injection/
    note: Direct and indirect prompt injection, and why RAG does not fully fix it.
  - id: owasp-llm08
    title: "OWASP, “LLM08:2025 Vector and Embedding Weaknesses”, OWASP Top 10 for LLM Applications 2025"
    url: https://genai.owasp.org/llmrisk/llm082025-vector-and-embedding-weaknesses/
    note: Data leaks through vector stores, and the advice to make retrieval permission-aware.
claims_checked:
  - claim: "Moffatt v. Air Canada: on 11 November 2022 the chatbot said a passenger who had already travelled could ask for the bereavement rate within 90 days of the ticket date; the linked page said the policy does not apply after travel; a representative admitted 'misleading words'; Air Canada was ordered to pay $812.02."
    source: moffatt
    note: "Paragraphs 13-22 and 44 of the decision PDF. $812.02 = $650.88 damages + $36.14 interest + $125 CRT fees, so the lesson now says so. Para 14: Air Canada gave no information about the nature of its chatbot. First refund request was 17 November, before the return flight, so 'flew, and applied' was changed to 'booked, and applied'."
  - claim: "The tribunal called Air Canada's argument 'a remarkable submission', said the airline is responsible for all information on its website, static page or chatbot, and asked why customers should double-check one part of the site against another."
    source: moffatt
    note: "Paragraphs 27-28. 'Remarkable submission' refers to the airline in effect treating the chatbot as a separate legal entity; the lesson now says this."
  - claim: "Contextual Embeddings plus Contextual BM25 cut the top-20 retrieval failure rate by 49% (5.7% to 2.9%); adding re-ranking cut it by 67% (to 1.9%)."
    source: contextual
    note: "Exact numbers. Metric is 1 minus recall@20, averaged over codebases, fiction, ArXiv and science papers, with their best embedding setup. Scope added to the lesson."
  - claim: "A knowledge base under 200,000 tokens (about 500 pages) can go in the prompt without RAG, with prompt caching; chunks are usually no more than a few hundred tokens; embeddings can miss exact matches such as 'TS-999'."
    source: contextual
    note: "All stated in the post (September 2024). '(about 500 pages)' added."
  - claim: "RRF scores each document by 1 / (k + rank) summed over the lists; it needs no tuning and the relevance indicators need not be related; Elasticsearch's rank_constant defaults to 60."
    source: es-rrf
    note: "The draft said RRF needs no tuning 'because' the scores need not be related. The source states two separate facts, so the causal link was removed. Formula and default k added."
  - claim: "With approximate indexes, pgvector applies a WHERE filter after the index scan, so a query can return fewer results; pgvector suggests Postgres full-text search plus RRF or a cross-encoder for hybrid search."
    source: pgvector
    note: "README 'Filtering', 'Iterative Index Scans' and 'Hybrid Search' sections."
  - claim: "Indirect prompt injection comes from external content such as websites or files; RAG and fine-tuning do not fully mitigate it; mark untrusted content and give the model least privilege. OWASP LLM08 advises permission-aware vector stores."
    source: owasp-llm01
    note: "LLM01:2025 wording confirmed. 'Only the tools it needs' changed to 'only the access and tools it needs' to match 'least privilege'. LLM08:2025 confirms 'permission-aware vector and embedding stores'."
  - claim: "Hallucination means text that is false or does not match the given context; Anthropic's guide lists letting the model say 'I don't know', grounding in quotes, citing sources, and restricting it to the given documents."
    source: claude-halluc
    note: "The draft said hallucinated text 'sounds sure'; the guide does not say this, so it was changed to the guide's own definition."
quiz:
  - q: Your support bot gives a wrong answer about the refund window. What should you check first?
    options:
      - Which model you use. Switch to a bigger one.
      - Which chunks retrieval returned for that question, and whether the right policy text was among them.
      - The temperature setting.
    answer: 1
    why: Check retrieval first. If the right text never reached the prompt, no model can answer from it.
  - q: Your index holds HR documents. Some are for managers only. Where must you enforce this?
    options:
      - In the prompt, by telling the model not to reveal manager-only content.
      - At retrieval time, so chunks the user may not read never enter the prompt.
      - In the user interface, by hiding the answer when it looks sensitive.
    answer: 1
    why: A prompt rule is advice the model may ignore. A filter at retrieval time means the model never sees what the user may not read.
  - q: Users search for error codes like "TS-999", and vector search keeps missing them. What helps most?
    options:
      - Add keyword search (BM25) and merge both result lists.
      - Make the chunks larger.
      - Ask the model to guess the error code.
    answer: 0
    why: Embeddings capture meaning but can miss exact strings. Keyword search finds exact terms, so hybrid search covers both.
  - q: Your whole knowledge base is about 50 pages and changes once a month. What is the simplest good design?
    options:
      - A vector database, a re-ranker and a chunking pipeline.
      - Fine-tune a model on the 50 pages.
      - Put all the pages in the prompt, with prompt caching.
    answer: 2
    why: Anthropic notes that a knowledge base under 200,000 tokens can go straight into the prompt. Our advice is to add RAG when it no longer fits.
  - q: You changed the chunk size. How do you know if it helped?
    options:
      - Ask three colleagues if the answers feel better.
      - Run your test set and compare recall@k and answer scores before and after.
      - Check that the build still passes.
    answer: 1
    why: Measure, don't guess. A fixed test set shows whether the right chunks are now found and whether answers improved.
incidents: []
related: [prompt-engineering, vector-search, search-systems, llm-evals, llm-guardrails]
---

## What broke

In November 2022, Jake Moffatt's grandmother died. The same day, Moffatt went to Air Canada's website to book a flight at its lower bereavement fares. Moffatt asked the website's support chatbot about these fares. [@moffatt]

The chatbot said a passenger who had already travelled could ask for a reduced bereavement rate within 90 days of the ticket date. The words "bereavement fares" in its answer linked to the airline's own policy page. That page said the policy does not apply to requests made after travel is completed. [@moffatt]

Moffatt trusted the chatbot, booked two flights, and applied for the lower rate within the 90 days. Air Canada staff then said the airline did not allow requests after travel. In an email, an airline representative admitted the chatbot had used "misleading words". [@moffatt]

At the tribunal, Air Canada argued that it could not be held liable for what its chatbot said. The tribunal said this treats the chatbot as a separate legal entity, and called it "a remarkable submission". It said the airline is responsible for all the information on its website, whether it comes from a static page or a chatbot. It also asked why a customer should have to double-check one part of a website against another. The airline had to pay $812.02: $650.88 in damages, plus interest and tribunal fees. [@moffatt]

Air Canada gave no information about how its chatbot worked, and the decision does not say how it was built. [@moffatt] But the lesson for anyone who ships one is clear. The answer and the policy page disagreed. Our advice: your system must answer from the real policy text, and show the user where the answer came from.

## The idea in simple words

A language model alone is not enough for questions about *your* data. There are three reasons.

- **It has a knowledge cutoff.** Every model has a date after which its knowledge is not reliable. Anthropic lists this date for each Claude model. [@claude-models] Your price change from last week is not in it.
- **It has never seen your private data.** Your policies, tickets and contracts were not in its training data.
- **It can hallucinate.** Models sometimes write text that is false, or that does not match the text you gave them. [@claude-halluc]

The RAG paper of 2020 described the core problem. When knowledge is stored only in model weights, it is hard to update, and hard to show where a fact came from. [@lewis2020] Its answer was to add a second memory: a vector index of Wikipedia that the model reads from at answer time. [@lewis2020]

So RAG works like an open-book exam. Your app searches your documents, puts the few passages that matter in the prompt, and the model answers from them with citations.

> [!key] **The key idea.** In RAG, the model can only be as good as the text you give it. When an answer is wrong, first check whether retrieval found the right text. So treat retrieval as the main part of the system, and measure it.

## How it works

Step through one request with Next and Back.

[[diagram:rag-permission-filter]]

### Before any question: index your documents

1. **Chunk.** Split each document into small pieces. Anthropic says chunks are usually no more than a few hundred tokens. [@contextual]
2. **Embed.** An embedding model turns each chunk into a vector, a list of numbers. The distance between two vectors measures how related the texts are. [@openai-emb]
3. **Store.** Save each vector with its text, its source link, and who may read it. A vector database does this. So does Postgres with pgvector, which supports exact and approximate nearest-neighbour search. [@pgvector]

### At question time

4. **Retrieve.** Embed the question the same way, and find the top k chunks with the nearest vectors. For how search indexes work in general, see [Search systems and inverted indexes](/learn/search-and-inverted-indexes).
5. **Filter.** Drop every chunk the user may not read.
6. **Prompt.** Put the allowed chunks into the prompt, each with a number. Tell the model to use only these sources and to cite them. Let it say "I don't know" when the sources do not answer. Anthropic's guide lists all three methods to reduce hallucinations. [@claude-halluc]
7. **Answer.** Show the answer with links to the cited chunks, so the user can check.

### Hybrid search and re-ranking

Vector search finds text with a similar *meaning*. But embeddings can miss exact matches, such as an error code like "TS-999". [@contextual] BM25 is a keyword ranking function that finds exact words and phrases. [@contextual] Hybrid search runs both and merges the two lists.

One way to merge them is reciprocal rank fusion (RRF). Each result gets 1 / (k + rank) from each list, and the points are added up. Elasticsearch uses k = 60 by default. [@es-rrf] RRF needs no tuning, and the scores from the two lists do not need to be related. [@es-rrf] For hybrid search, pgvector suggests Postgres full-text search, merged with RRF or a re-ranking model. [@pgvector]

Re-ranking adds one more step. A re-ranking model scores the top results again, and only the best ones go into the prompt. It adds some latency, and you trade more re-ranked chunks against speed and cost. [@contextual]

### Chunking choices

Chunk size, chunk borders and overlap all change what retrieval finds. [@contextual] A small chunk can lose its meaning. Anthropic's example is a chunk that says revenue grew by 3%, without saying which company or which quarter. [@contextual] Its fix, "contextual retrieval", adds a short note about the whole document to each chunk before indexing. [@contextual]

Anthropic measured this on codebases, fiction, ArXiv papers and science papers. The failure rate is the share of relevant chunks missing from the top 20, averaged over these sets. Contextual embeddings plus contextual BM25 cut the top-20 retrieval failure rate by 49%, from 5.7% to 2.9%. Adding re-ranking cut it by 67%, to 1.9%. [@contextual] These are their numbers on their data. Your data will differ, so measure your own.

### Evaluation: measure, don't guess

Test the two halves apart.

- **Retrieval quality.** For each test question, write down which chunks *should* come back. Then measure recall@k: the share of those chunks that appear in the top k. Anthropic's study uses exactly this kind of metric. [@contextual]
- **Answer quality.** Is the answer correct, and is every claim backed by a cited chunk? Anthropic's eval guide says to mirror your real task mix, include edge cases, and grade automatically where you can. [@claude-evals] If a model grades the answers, it suggests using a different model from the one that wrote them. [@claude-evals]

Our advice: start with 30 to 50 real questions from users or support tickets. Add questions where the right answer is "I don't know". Run the set after every change to chunking, the embedding model, top-k or the prompt.

### Common failures

- **Wrong chunk retrieved.** The answer sounds right but cites the wrong policy. Look at the retrieved chunks first.
- **Stale index.** The document changed, but the old chunks are still in the index. Re-index on every change, and delete chunks of deleted documents.
- **Prompt injection from documents.** A retrieved page can contain hidden instructions. OWASP calls this indirect prompt injection, and says RAG does not fully remove the risk. [@owasp-llm01] Mark retrieved text clearly as untrusted data, and give the model only the access and tools it needs. [@owasp-llm01]
- **Leaking documents.** If the index holds documents for different users, weak access control can leak them. OWASP advises permission-aware vector stores. [@owasp-llm08] Filter at retrieval time, not in the prompt. One trap: with approximate indexes in pgvector, a WHERE filter runs *after* the index scan, so you may get fewer results than you asked for. [@pgvector]

### Cost and latency

Each request adds steps: embed the question, search, maybe re-rank, then a longer prompt. More chunks mean more input tokens. Re-ranking costs time but can lower model cost, because the model reads less text. [@contextual] If your whole knowledge base is under 200,000 tokens (about 500 pages), Anthropic suggests you may not need RAG at all. Put it all in the prompt and use prompt caching. [@contextual]

## Trade-offs

This table is our own summary, except where a source is cited.

| Approach | Fresh data | Can cite its source | Cost and latency | Use it when |
|---|---|---|---|---|
| Whole knowledge base in the prompt | Yes. Edit the text and it is live. | Yes. | More tokens per call. Caching helps. | The data is small, under about 200,000 tokens. |
| RAG with vector search | Yes, if you re-index on change. | Yes. | Embedding and a search per question. | Users ask in their own words, and data is large. |
| RAG with keyword search (BM25) | Yes. | Yes. | Cheap and fast. | Users search for exact codes, names or IDs. |
| Hybrid search plus re-ranking | Yes. | Yes. | The most steps and the slowest. | Both kinds of question, and quality matters most. |
| Fine-tuning | No. Facts in model weights are hard to update. [@lewis2020] | No. The paper calls tracing facts in weights an open problem. [@lewis2020] | Our view: a training run for every change. | Our view: to change style or format, not to add facts. |

**Simple rule:** start with the simplest design that passes your test set. Add hybrid search or re-ranking only when the numbers show you need it.

## At work: how a senior uses this

- When an answer is wrong, first ask: **"Was the right chunk retrieved?"** Log the retrieved chunk IDs for every request so you can answer this.
- Build the test set before you tune anything. Keep it in the repo and run it in CI.
- Treat permissions as part of retrieval. Store the access rules next to each chunk, and filter in the query.
- When a policy changes, re-index it the same day and add a test question for it.
- Show citations in the user interface. If the model cannot cite a source, it should say it does not know.
- Ask your team who owns the bot's answers. The tribunal's view was clear: the company does. [@moffatt]

## With AI

### What to ask the agent

> "Here are 20 real user questions and our docs folder. For each question, list the document sections that should be retrieved. Mark any question our docs cannot answer."

> "Write a script that runs our RAG pipeline on this test set and prints recall@5 and the retrieved chunk IDs for each question that failed."

> "Review this retrieval code. Show me every path where a chunk can reach the prompt without the user's permission check."

### What to check yourself

- The expected chunks in the test set are right. Check a sample by hand, because the agent may guess.
- The permission filter runs inside the search query, before the prompt is built, and a test proves it.
- The test set includes questions that should get "I don't know".
- Retrieved text is marked as data in the prompt, and the model has no tools it does not need.
- You compared numbers before and after each change, not only a few examples.

## Check yourself

## Go deeper

Related lessons: [Vector databases and ANN search](/learn/vector-databases-ann-search), [Search systems and inverted indexes](/learn/search-and-inverted-indexes), [Evals for LLM systems](/learn/evals-for-llm-systems), and [Guardrails, cost and latency for LLM features](/learn/llm-guardrails-cost-latency).
