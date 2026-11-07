---
topic: fan-out
subtitle: Learn the two ways a post reaches millions of feeds, what each one costs, and how big apps mix them for accounts with millions of followers.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-08
illustrative_story: false
sources:
  - id: hs-twitter
    title: "High Scalability, “The Architecture Twitter Uses to Deal with 150M Active Users, 300K QPS, a 22 MB/S Firehose, and Send Tweets in Under 5 Seconds”, July 2013"
    url: https://highscalability.com/the-architecture-twitter-uses-to-deal-with-150m-active-users/
    note: A detailed summary of Raffi Krikorian's QCon San Francisco 2012 talk, and the source of every Twitter number in this lesson. Redis timelines capped at 800 entries, active users, reconstruction, and the trouble with very large accounts.
  - id: infoq-timelines
    title: "Raffi Krikorian, “Timelines at Scale”, QCon San Francisco 2012, published on InfoQ April 2013"
    url: https://www.infoq.com/presentations/Twitter-Timeline-Scalability/
    note: The original talk (video and slides) by Raffi Krikorian, then Senior Director of Twitter's Applications Services group, on how tweets, social graph changes and direct messages flow through the system.
  - id: x-home-mixer
    title: "X (Twitter), “Home Mixer” README, the-algorithm repository on GitHub, 2023"
    url: https://github.com/twitter/the-algorithm/blob/main/home-mixer/README.md
    note: How the For You and Following timelines were built at read time in the code X published in 2023.
  - id: fb-multifeed
    title: "Yufei Zhu, Facebook Engineering, “Serving Facebook Multifeed: Efficiency, performance gains through redesign”, March 2015"
    url: https://engineering.fb.com/2015/03/10/production-engineering/serving-facebook-multifeed-efficiency-performance-gains-through-redesign/
    note: News Feed as aggregators and leaves. Leaves keep recent actions in memory, and aggregators query them and rank the results.
  - id: linkedin-followfeed
    title: "Ankit Gupta, Swapnil Ghike and Shubham Gupta, “FollowFeed: LinkedIn's Feed Made Faster and Smarter”, LinkedIn Engineering, March 2016"
    url: https://www.linkedin.com/blog/engineering/feed/followfeed-linkedin-s-feed-made-faster-and-smarter
    note: Why LinkedIn chose fan-out on read, with a measured storage difference and the cost of testing new ranking models.
  - id: mastodon-fanout
    title: "Mastodon source code, FanOutOnWriteService (app/services/fan_out_on_write_service.rb), November 2026"
    url: https://github.com/mastodon/mastodon/blob/4dff13d48694e302d1a161b3d0795284a512b482/app/services/fan_out_on_write_service.rb
    note: A real fan-out on write. One background job per follower, queued in batches, only for recently active local followers.
  - id: mastodon-feed
    title: "Mastodon source code, FeedManager (app/lib/feed_manager.rb), November 2026"
    url: https://github.com/mastodon/mastodon/blob/4dff13d48694e302d1a161b3d0795284a512b482/app/lib/feed_manager.rb
    note: Home feeds as Redis sorted sets capped at 800 items, merging a new account's posts on follow, and rebuilding a feed.
  - id: mastodon-active
    title: "Mastodon source code, User::Activity (app/models/concerns/user/activity.rb), November 2026"
    url: https://github.com/mastodon/mastodon/blob/4dff13d48694e302d1a161b3d0795284a512b482/app/models/concerns/user/activity.rb
    note: The 7-day "active" window that decides who gets fan-out and whose feed is kept, with the reasons in a code comment.
  - id: mastodon-user
    title: "Mastodon source code, User model (app/models/user.rb), November 2026"
    url: https://github.com/mastodon/mastodon/blob/4dff13d48694e302d1a161b3d0795284a512b482/app/models/user.rb
    note: On sign-in after the active window, a returning user gets a background job that rebuilds the home feed.
  - id: mastodon-scaling
    title: "Mastodon documentation, “Scaling up your server”"
    url: https://docs.joinmastodon.org/admin/scaling/
    note: The Sidekiq queues behind posting, in priority order, and why remote work runs below local work.
  - id: fcm-topics
    title: "Firebase documentation, “Topic messaging on Android”"
    url: https://firebase.google.com/docs/cloud-messaging/android/topic-messaging
    note: Push notifications to many devices through topics, built for throughput, with a limit on fan-outs running at once.
claims_checked:
  - claim: 'Around 2012 Twitter served about 300,000 timeline reads a second and about 6,000 write requests a second; home timelines lived in Redis, capped at 800 entries of tweet ID, author ID and 4 bytes of flags.'
    source: hs-twitter
    note: 'Quotes: "300K QPS are spent reading timelines and only 6000 requests per second are spent on writes"; "a maximum of 800 entries". Talk is QCon SF 2012 per InfoQ; HS summary dated 8 July 2013.'
  - claim: 'A Lady Gaga tweet could take up to 5 minutes to reach her 31 million followers, and merging very large accounts at read time was listed as future work.'
    source: hs-twitter
    note: 'Under "The Future": "Not fanning out the high value users anymore"; "Trying to figure out how to merge the read and write paths". Lesson frames it as a plan.'
  - claim: 'Mastodon fans out only to local followers who signed in within USER_ACTIVE_DAYS (default 7), one FeedInsertWorker job per follower, queued in batches.'
    source: mastodon-fanout
    note: 'followers_for_local_distribution = followers.local.joins(:user).merge(User.signed_in_recently); ACTIVE_DURATION in user/activity.rb (mastodon-active). Commit 4dff13d.'
  - claim: 'Mastodon home feeds are capped at 800 items; following an account merges up to 200 of its recent posts, filtered by the oldest item once the feed has 200 or more; returning inactive users get a feed rebuild job.'
    source: mastodon-feed
    note: 'MAX_ITEMS = 800, merge_into_home limit MAX_ITEMS / 4; regenerate_feed! on sign-in in user.rb (mastodon-user).'
  - claim: 'LinkedIn found fan-out-on-write data was 62 times larger than its pull design, and pre-built feeds stored in ranked order make testing new ranking models IO-heavy.'
    source: linkedin-followfeed
    note: '"data size with this model was 62 times greater than pull based architecture". Post dated 31 March 2016.'
  - claim: 'FCM topic messages are optimized for throughput rather than latency, with at most 1,000 concurrent fan-outs per project.'
    source: fcm-topics
    note: '"We limit the number of concurrent message fanouts per project to 1,000."'
quiz:
  - q: An account with 2,000 followers posts 10 times a day. With pure fan-out on write, about how many timeline writes does that make each day?
    options:
      - "10"
      - "2,000"
      - "20,000"
    answer: 2
    why: Each post is written once per follower, so 10 posts × 2,000 followers = 20,000 writes. The number of followers multiplies every post.
  - q: Twitter's numbers from 2012–13 showed about 300,000 timeline reads a second and about 6,000 write requests a second. Why does this favour fan-out on write for most accounts?
    options:
      - Reads are about 50 times more common, so it pays to do the work once at write time and keep reads cheap.
      - Writes are more common, so writes must be cheap.
      - It does not matter, because both use Redis.
    answer: 0
    why: When reads far outnumber writes, building each timeline in advance moves work from the busy path to the quiet one. The cost grows with audience size, so it becomes a problem for accounts with huge audiences.
  - q: Your app pushes posts into followers' cached lists. A user with 30 million followers starts posting. What is the usual fix?
    options:
      - Add more Redis memory.
      - Stop fanning out that account's posts, and merge them into each reader's timeline at read time.
      - Send the post only to the first million followers.
    answer: 1
    why: This is the hybrid. Most posts are pushed, but very large accounts are pulled and merged when a reader opens the app. Twitter described this plan in its 2012 talk, as reported in a 2013 summary.
  - q: In Mastodon's code, what happens to a user who has not signed in for more than 7 days (the default)?
    options:
      - Their account is deleted.
      - New posts are no longer pushed to their home feed, and the feed is rebuilt when they come back.
      - They get every missed post by email.
    answer: 1
    why: Fan-out only goes to followers who signed in recently, and a user returning after the active window gets a background job that rebuilds the feed.
  - q: LinkedIn chose fan-out on read for its feed in 2016. Which reason did it give?
    options:
      - Pre-built feeds needed about 62 times more data, and made testing new ranking models slow.
      - Fan-out on read is always faster for the reader.
      - LinkedIn has no members with many followers.
    answer: 0
    why: Their experiments found the pre-built model's data was 62 times larger. Pre-built feeds are also stored in ranked order, so each new ranking model means re-sorting every feed.
incidents: []
cases: [discord-elixir-fanout]
related: [scaling-reads, queues-vs-logs, presence-fanout, caching-stampedes, sharding]
glossary_optout: [fan-out, follower]
---

## What broke

Around 2012, Twitter had about 150 million active users. The Twitter numbers in this lesson come from a detailed 2013 summary of a talk by Raffi Krikorian. [@hs-twitter] Your home timeline was a ready-made list in a large Redis cluster. When someone tweeted, Twitter copied the tweet's ID into the list of every follower. [@hs-twitter]

For most accounts this worked well. A tweet to 1 million followers took a couple of seconds to reach them all. The goal was under 5 seconds. [@hs-twitter]

Then a few accounts had tens of millions of followers. A tweet from Lady Gaga could take up to 5 minutes to reach her 31 million followers. Queues for these large fan-outs backed up all the time. [@hs-twitter]

The delay caused strange bugs. A fan who got her tweet early could reply at once. Other fans saw the reply before the tweet it answered. [@hs-twitter] This is likely because the reply had a small audience, so it was delivered fast.

Twitter's plan was to stop pushing tweets from the biggest accounts. Instead, it would merge their tweets into your timeline when you read it. [@hs-twitter] Raffi Krikorian described Twitter's timelines in a talk at QCon San Francisco in late 2012. [@infoq-timelines]

## The idea in simple words

A feed shows posts from many people you follow. You can do the gathering work at one of two times.

**Fan-out on write (push).** When someone posts, copy the post's ID into a list for each follower. Opening your feed is then one cheap read of your own list.

**Fan-out on read (pull).** Store each post once, with its author. When you open your feed, fetch recent posts from everyone you follow and merge them. Writing is cheap. Every read does the work.

Neither one wins every time. Push hurts when one author has millions of followers. This is the celebrity problem. Pull hurts when one reader follows thousands of accounts, and readers open the app all day.

> [!key] **The key idea.** Push pays per follower, at write time. Pull pays per followed account, at read time. Most big feeds push for normal accounts and pull for the few huge ones.

## How it works

Step through one normal post and one post from a huge account. Use Next and Back, or the ← and → keys.

[[diagram:fanout-hybrid-timeline]]

### Do the cost maths first

Write cost with push is posts per second × average followers. Read cost with pull is feed opens per second × accounts followed. Compare the two before you choose.

Example with made-up numbers: an app gets 1,000 posts a second, and the average author has 200 followers. Push makes 200,000 list writes a second. Each write is tiny, and reads become one lookup.

Now one author has 30 million followers. One post from her is 30 million writes. That is 150 seconds of the whole app's normal push work, for one post.

Real apps read much more than they write. Around 2012, Twitter served about 300,000 timeline reads a second, but only about 6,000 write requests a second. [@hs-twitter] With that ratio, doing the work once at write time is usually the better deal.

### Store timelines as capped lists of IDs

Keep only IDs in the list, not the full post. Twitter stored the tweet ID, the author's ID and 4 bytes of flags, such as "this is a retweet". [@hs-twitter] Each home timeline was capped at 800 entries, and each timeline entry was kept on 3 machines in the Redis cluster. [@hs-twitter]

When a reader opens the app, the server loads full posts for the IDs it shows. Short IDs keep the list small, and an edited post changes in one place only.

Mastodon, the open-source social server, works the same way today. Its home feeds live in Redis. Each feed keeps at most 800 items, and a trim step removes anything older. [@mastodon-feed]

### Skip inactive users

Most people do not open the app every day. Pushing posts into their lists wastes memory and work.

Twitter kept only active users in Redis, meaning people who had logged in within 30 days. Others got no entries. When they came back, Twitter rebuilt their timeline. It looked up who they follow, read those posts from disk and put the list back in Redis. [@hs-twitter]

Mastodon pushes posts only to local followers who signed in within the last 7 days by default. [@mastodon-fanout] [@mastodon-active] Feeds are kept for the same 7 days. [@mastodon-active] When such a user signs in again, Mastodon queues a job that rebuilds the feed from the accounts they follow. [@mastodon-user] [@mastodon-feed] A code comment states the trade-off. A shorter window saves work when many people sign up but rarely visit. A longer window means less catch-up work when people come back. [@mastodon-active]

The cost is a slow first load for a returning user. Show a "loading your feed" state, not an empty screen.

### Fan out through a queue

Never fan out inside the request that saves the post. Save the post, put one job on a queue, and answer the user. Workers do the copying later.

Mastodon's fan-out code reads followers in batches. It then queues one small job per follower, and each job adds the post to that follower's feed. [@mastodon-fanout] Its queues run in priority order. Posts from other servers go to a lower-priority queue, "so that local users still see their posts when the server is under load". [@mastodon-scaling]

Notifications use the same path. At Twitter, delivery reached in-memory timelines, push notifications and emails. [@hs-twitter] Your workers rarely send to each phone one by one. They call a push service. Firebase Cloud Messaging can send one message to every device subscribed to a topic. Its docs say topic messages are built for throughput, not low latency. They also limit each project to 1,000 fan-outs running at once. [@fcm-topics] For urgent alerts to a few people, Firebase advises sending to device tokens instead. [@fcm-topics]

For delivery guarantees and retries, see [Queues vs logs](/learn/queues-vs-logs-delivery-guarantees).

### Fill in old posts when someone follows

Push only adds new posts. If you follow Asha today, her older posts are not in your list. You must merge them in once.

Mastodon's feed code has a merge step for this. It copies up to 200 recent posts from the new account into your feed. It does this only for users who signed in recently. If your feed already has 200 or more items, it skips posts older than the oldest one in it. Then it trims the feed back to 800. [@mastodon-feed] A reverse step removes an account's posts from your feed after an unfollow. [@mastodon-feed]

### Merge and rank at read time

The hybrid reads two things. It reads your pushed list, and it reads the latest posts of the few huge accounts you follow. Then it merges them by time, or by a score.

Ranking pushes many apps further toward pull. In 2015, Facebook described its News Feed backend this way. "Leaf" servers kept recent actions in memory. An aggregator sent each query to the leaves, then ranked what came back. [@fb-multifeed] In other words, Facebook built the feed when you asked for it.

LinkedIn chose pull in 2016. Its tests found that pre-built feeds needed 62 times more data. [@linkedin-followfeed] Pre-built feeds are also stored in ranked order. So testing a new ranking model means re-sorting every stored feed. [@linkedin-followfeed]

By 2023, X's published code built the For You and Following tabs at read time. The Following tab fetched posts from the Earlybird search index. The For You tab gathered candidates from several sources, then scored them with a model using about 6,000 features. [@x-home-mixer]

## Trade-offs

| Option | Write cost | Read cost | Best for |
|---|---|---|---|
| Fan-out on write | High. One write per active follower. Very high for huge accounts. | Low. One list read, then load the posts. | Read-heavy apps where most accounts have a modest audience. |
| Fan-out on read | Low. One write per post. | High. One lookup per followed account, then a merge. | Ranked feeds, users who follow few accounts, and early products. |
| Hybrid | Push for normal accounts, nothing extra for huge ones. | One list read plus a few pulls. | Big social apps with a few accounts that have millions of followers. |
| Push to active users only | Lower. Inactive users cost nothing. | Slow first load when a user returns. | Apps where many users visit rarely. |

**Simple rule:** start with pull while you are small. Add push when reads hurt. Add the hybrid when one account's post takes minutes to deliver.

## At work: how a senior uses this

- Multiply first. Ask: **"Posts per second × average followers, and feed opens per second × accounts followed. Which is bigger?"**
- Ask for the follower count at p99 and the maximum, not the average. The largest account decides your worst case.
- Pick a clear threshold for "huge account", and store it as a setting. Watch how many accounts cross it.
- Watch fan-out lag: the time from "post saved" to "last follower's list updated". Alert on it.
- Keep timelines rebuildable. The database is the truth, and the cache list is a copy you can throw away.
- Decide what happens on block, delete and unfollow. Each one must remove posts from lists that are already built.

## With AI

### What to ask the agent

> "We have 2 million users. The average user follows 150 accounts and opens the app 8 times a day. Users make 500,000 posts a day, and the largest account has 4 million followers. Estimate writes per second for fan-out on write and reads per second for fan-out on read. Show the maths."

> "Write a fan-out worker in Node with BullMQ and Redis. It reads followers in batches of 1,000, skips users not seen in 14 days, adds the post ID with ZADD (score = post ID), and trims each feed to 800 with ZREMRANGEBYRANK. Make a retried job safe."

> "Add read-time merge for accounts with more than 1 million followers. Merge their latest posts into the cached list by post ID, and remove duplicates."

### What to check yourself

- The worker does not load all followers into memory at once. Agents often write `getAllFollowers()` and loop.
- A retried job does not add the same post twice. Check for duplicates or use a sorted set keyed by post ID.
- The list is trimmed after every insert, so memory stays bounded.
- Deletes, blocks and unfollows remove posts from lists that are already built.
- The merge sorts by a stable key, such as a time-ordered post ID, so pages do not repeat posts.

## Check yourself

## Go deeper

- [Scaling reads](/learn/scaling-reads): caches, replicas and read models, the tools under any timeline.
- [Presence and real-time fan-out](/learn/presence-and-real-time-fan-out): the same push-or-pull choice for live status and chat.
- [Caching and cache stampedes](/learn/caching-and-cache-stampedes): what happens when many timelines rebuild at once.
- [Sharding and consistent hashing](/learn/sharding-and-consistent-hashing): how timelines are spread over many Redis servers.
