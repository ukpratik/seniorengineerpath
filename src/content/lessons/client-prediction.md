---
topic: client-prediction
subtitle: The server owns the truth, but players cannot wait for it. Learn how games predict your moves, fix them quietly, and rewind time to judge shots fairly.
status: published
illustrative_story: true
sources:
  - id: gambetta-arch
    title: "Gabriel Gambetta, “Fast-Paced Multiplayer (Part I): Client-Server Game Architecture”"
    url: https://www.gabrielgambetta.com/client-server-game-architecture.html
    note: Why the server must be the authority, and why waiting for the server makes a game feel slow.
  - id: gambetta-predict
    title: "Gabriel Gambetta, “Fast-Paced Multiplayer (Part II): Client-Side Prediction and Server Reconciliation”"
    url: https://www.gabrielgambetta.com/client-side-prediction-server-reconciliation.html
    note: The step-by-step example with input numbers that this lesson's diagram follows.
  - id: gambetta-interp
    title: "Gabriel Gambetta, “Fast-Paced Multiplayer (Part III): Entity Interpolation”"
    url: https://www.gabrielgambetta.com/entity-interpolation.html
    note: Why you cannot predict other players, dead reckoning, and showing others slightly in the past.
  - id: gambetta-lag
    title: "Gabriel Gambetta, “Fast-Paced Multiplayer (Part IV): Lag Compensation”"
    url: https://www.gabrielgambetta.com/lag-compensation.html
    note: How the server rebuilds the past to judge a shot, and the fairness cost.
  - id: valve
    title: "Valve Developer Community, “Source Multiplayer Networking” (archived copy, January 2026)"
    url: https://web.archive.org/web/20260105011842/https://developer.valvesoftware.com/wiki/Source_Multiplayer_Networking
    note: Real defaults from the Source engine. Tick rate, snapshots, interpolation, prediction and the lag compensation formula.
  - id: riot-peek
    title: "Matt deWet and David Straily, “Peeking into VALORANT's Netcode”, Riot Games Technology (2020, archived copy)"
    url: https://web.archive.org/web/20250104022431/https://technology.riotgames.com/news/peeking-valorants-netcode
    note: How Riot measured peeker's advantage in milliseconds and cut it, plus fixed time steps and rewind limits.
  - id: ggpo
    title: "GGPO, “Rollback Networking SDK for Peer-to-Peer Games”"
    url: https://www.ggpo.net/
    note: A short, plain explanation of rollback netcode from the library that made it popular.
claims_checked:
  - claim: "Source simulates in 15 ms ticks (about 66 per second), clients receive about 20 snapshots per second, and the default interpolation delay is 100 ms (cl_interp 0.1)."
    source: valve
    note: Archived page, sections on tick rate and entity interpolation. One lost snapshot still leaves two to interpolate between.
  - claim: "Lag compensation keeps one second of player positions and uses Command Execution Time = Current Server Time − Packet Latency − Client View Interpolation."
    source: valve
    note: Exact formula and the one-second history are on the archived page. Also the cheat-proxy reason and "can't be solved in general".
  - claim: "In Gambetta's example with 250 ms lag, the server answers x = 11, last processed #1; the client resets and replays #2, staying at x = 12."
    source: gambetta-predict
    note: Responses arrive at t = 250 ms (#1) and t = 350 ms (#2). The start position x = 10 is implied by the arithmetic.
  - claim: "Riot's model gives the peeker ~141 ms more; 35 ms ping and 128-tick servers cut ~40 ms; at 144 FPS it drops to ~71 ms; skilled players noticed ~10 ms changes and 20 ms felt very impactful."
    source: riot-peek
    note: All numbers match the 2020 article (archived copy). 128 Hz fixed timestep on client and server, "move #408", 500 ms rewind example also confirmed.
  - claim: "Rollback predicts remote inputs and re-simulates from the point of divergence; it needs a fully deterministic peer-to-peer engine."
    source: ggpo
    note: GGPO home page. Traditional method waits for all inputs before each frame.
  - claim: "Gambetta calls lag compensation \"somewhat unfair\" but \"the most agreeable solution for everyone involved\"."
    source: gambetta-lag
    note: Exact quotes. Earlier wording "best choice overall" was not in the source and was replaced.
  - claim: "Prediction works well only if the game world is deterministic enough."
    source: gambetta-predict
    note: "QC1-16: Gambetta says 'If the game world is deterministic enough'. Lesson no longer says 'only if ... deterministic'."
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
quiz:
  - q: Why do most competitive shooters let the server, not the client, decide where players are and who got hit?
    options:
      - The server has a faster CPU than players' computers.
      - A player can change their own client, so the game cannot trust what it says.
      - It uses less network bandwidth.
    answer: 1
    why: Gambetta's rule is "don't trust the player". A hacked client can claim anything, so only the server's version counts.
  - q: Your game predicted you at x = 12. The server's update says x = 11, last input processed #1. You have sent input #2 as well. What should the client do?
    options:
      - Move the player to x = 11 and stay there.
      - Ignore the server, because the client is more up to date.
      - Reset to x = 11, drop input #1, and replay input #2 on top.
    answer: 2
    why: This is server reconciliation. The server's state is true but old, so the client replays the inputs the server has not seen yet.
  - q: Why do clients show other players about 100 ms in the past instead of guessing where they are now?
    options:
      - Other players can change direction at any moment, so guesses would often be wrong and jump around.
      - It saves CPU time on the client.
      - The server forbids showing the present.
    answer: 0
    why: You cannot know another player's next key press. Interpolating between two real updates is smooth and uses real positions, just a little late.
  - q: A player complains they were shot after they reached cover. What is the most likely cause?
    options:
      - A bug in the physics code.
      - Lag compensation. The server judged the shot at the moment the shooter saw them, before they reached cover.
      - The server's tick rate was too high.
    answer: 1
    why: Valve and Gambetta both describe this side effect. Rewinding time favours the shooter, so the target can be hit after reaching cover.
  - q: Riot found that skilled players could notice a change in peeker's advantage of about how much?
    options:
      - About 10 ms.
      - About 1 second.
      - Players cannot notice anything under 200 ms.
    answer: 0
    why: In Riot's blind tests, skilled players noticed changes of about 10 ms, and 20 ms felt very large to them.
incidents: []
cases: [riot-valorant-netcode, riot-valorant-128-tick]
related: [tick-rate, anti-cheat, rollback-netcode, game-loop, latency-estimation]
glossary_optout: [reconciliation, rollback]
---

## What broke

This story is made up, but each part of it happens in real games.

A small studio ships an online shooter. Each player's game decides what happened and tells the server: "I moved here. I hit player 7." The server passes the news on. It feels fast.

In the first week, cheaters arrive. A changed client reports a headshot on every enemy. The server believes it. Honest players quit.

The team moves the truth to the server. Now your game sends only "I pressed forward" and waits to hear where you are. Cheating gets much harder. But now every step starts a fraction of a second after you press the key. The game feels slow.

The team needs both: a server that cannot be fooled, and a game that reacts at once. This lesson shows how real games get both.

## The idea in simple words

**The server is the authority.** Gabriel Gambetta's rule is short: "don't trust the player." A hacked client may claim 10000% health. The server still knows the real value, so the player still dies. [@gambetta-arch] VALORANT is built the same way. Riot writes that its server "must never trust a client's view of the world". [@riot-peek]

**But the network is slow.** Light alone would need about 13 ms to cross the 4,000 km from San Francisco to New York. Real internet paths are slower. Gambetta says delays of 100, 200 or even 500 ms are not unusual. A half-second delay makes a game unplayable. [@gambetta-arch] The time for a message to go to the server and come back is called the round-trip time, or ping. [@valve]

So games use three tricks, one for each kind of thing on your screen:

1. **Your own character: predict.** Move at once, as a guess. Fix the guess when the server answers.
2. **Other players: interpolate.** Show them a little in the past, between two real updates.
3. **Shots: lag compensation.** The server rewinds time to what the shooter saw, then checks the hit.

> [!key] **The key idea.** The server decides what is true. The client decides what you see right now. Prediction, interpolation and lag compensation are ways to hide the gap between the two, and each one has a price.

## How it works

### Tick rate: how often the server updates

The server runs the game in small, fixed steps called ticks. In each tick it reads player inputs, moves everything and checks the rules. In Valve's Source engine, the default tick is 15 ms, about 66 ticks per second. [@valve] A higher tick rate is more exact, but it needs more CPU and bandwidth. [@valve] VALORANT runs movement at exactly 128 steps per second, on both client and server. [@riot-peek]

The server does not send every tick to every player. It sends snapshots of the world at a set rate. In Source, a client receives about 20 snapshots per second by default. [@valve]

### Client-side prediction

Without prediction, you press forward and see nothing until the server answers. Valve's example: with 150 ms of latency, you see your own move 150 ms late. [@valve]

With prediction, your game runs the same movement code as the server, at once. You move now. The server still has the final word. [@valve] This works well only if the game is deterministic enough: the same state plus the same inputs should give the same result. [@gambetta-predict]

### Server reconciliation

Prediction has a trap. The server's answer is always old by the time it arrives. If you copy it, you jump backwards. Step through the fix below. It follows Gambetta's example: 250 ms of lag, and two presses of the right arrow. [@gambetta-predict] Use Next and Back, or the ← and → keys.

[[diagram:client-reconcile]]

The fix needs two small things. [@gambetta-predict]

- **Numbered inputs.** Each input gets a sequence number: #1, #2, #3.
- **Acknowledgement.** Each server update says, "this is your state after input #N."

The client throws away inputs up to #N. It resets to the server's state and replays the inputs the server has not seen yet. Riot uses numbered, fixed-size moves in the same way. When a client says "I simulated move #408", the server knows exactly which move it means. [@riot-peek]

Sometimes the guess is truly wrong. Maybe another player blocked your path. Then the client must move you to the server's position. Source can spread this fix over a short time, so the view does not jump. [@valve] In VALORANT, only the player with the bad network sees the fix. The other nine players keep smooth movement. [@riot-peek]

### Entity interpolation for other players

You can predict yourself because you know your own key presses. You cannot know another player's. [@valve] Gambetta describes dead reckoning: assume the player keeps the same speed and direction. It works for cars or ships. It fails for players, who run, stop and turn very quickly. [@gambetta-interp]

So games show other players in the past. They draw smooth movement between the last two real updates. [@gambetta-interp] Source does this with a 100 ms delay by default. With 20 snapshots per second, one lost snapshot still leaves two real ones to draw between. [@valve] The result: you see yourself in the present, and everyone else about 100 ms ago.

### Lag compensation: the server rewinds time

Now you aim at an enemy and fire. You aimed at where they were on your screen, which is in the past. By the time your shot reaches the server, they have moved. Without help, you would have to aim ahead of moving targets. [@riot-peek]

Lag compensation fixes this on the server. Source keeps one second of recent player positions. When your shot arrives, the server estimates when you fired [@valve]:

```
fire time = server time − packet latency − your interpolation delay
```

It moves the other players back to that moment, checks the hit, and puts them back. [@valve]

Why not let the client say "I hit him"? Valve's answer: a server cannot trust clients with such decisions. Even a clean client's packets could be changed on the way by a "cheat proxy". [@valve]

### The price: shot behind a wall

Rewinding time favours the shooter. You can reach cover on your screen and still die, because the server checked the shot at an earlier moment, when you were still in the open. Valve says this "can't be solved in general". [@valve] Gambetta calls it "somewhat unfair", but "the most agreeable solution for everyone involved". [@gambetta-lag]

So games limit the rewind. Riot writes that without limits, a player with 500 ms latency could kill you half a second after you reached cover. [@riot-peek]

### Peeker's advantage

The same delays help the attacker. When you step around a corner, you see the defender at once, thanks to prediction. The defender sees you only after your move goes to the server and on to them, plus buffering. [@riot-peek]

Riot added up these delays. With its model's assumptions, the peeker had about 141 ms longer to react. Lower network latency, with a target of 35 ms ping, and 128-tick servers cut about 40 ms of that. With those changes, a client running at 144 frames per second (FPS) brings it down to about 71 ms. [@riot-peek] Small numbers matter. In Riot's blind tests, skilled players noticed changes of about 10 ms. [@riot-peek]

### A glimpse of rollback netcode

Some fast games are peer-to-peer, with no central server. Each player's machine runs the full game. The old way was to wait for the other player's inputs, which feels slow. [@ggpo] Rollback netcode predicts the other player's inputs and keeps going. When the real inputs arrive and differ, it re-runs the game from the point where they differed. Like prediction, this needs a fully deterministic game. [@ggpo] See [lockstep and rollback netcode](/learn/lockstep-and-rollback-netcode).

## Trade-offs

| Technique | What you gain | What you pay | Use it for |
|---|---|---|---|
| Wait for the server | Yes. Always correct and simple. | No. Every action is late by the round-trip time. | Slow games, like turn-based games. |
| Client-side prediction | Yes. Your own moves feel instant. | Shared, deterministic code. Small fixes when guesses are wrong. | Your own character's movement and actions. |
| Dead reckoning | Others look current. | No. Big jumps when a player turns suddenly. | Things that move in a steady way, like cars or ships. |
| Entity interpolation | Yes. Smooth, and uses only real positions. | Others are shown about 100 ms in the past. | Other players in fast games. |
| Lag compensation | Yes. Shots hit where you aimed. | Targets sometimes die behind cover. Needs a rewind limit. | Hit checks in shooters. |
| Rollback netcode | Yes. No input delay for local inputs. | Must save, load and re-run game states fast. Needs full determinism. | Fast peer-to-peer games, which GGPO was built for. |

## At work: how a senior uses this

- Ask first: **"Who is the authority for this piece of state?"** Health, money, hits and inventory belong to the server.
- Write a latency budget. List network, buffering, tick and frame time in milliseconds, like Riot's peeker's advantage sum.
- Test with fake lag and packet loss (network data that never arrives) from day one. Prediction bugs hide on a fast office network.
- Keep movement code shared and deterministic, with a fixed time step.
- Log every correction. A rise in corrections after a release means client and server code have drifted apart.
- Set a maximum rewind time for lag compensation, and write down who it is unfair to.
- Notice the pattern outside games. An app that shows a "like" before the server confirms it is predicting, and needs a plan for when the server says no.

## With AI

### What to ask the agent

> "Here is our movement code for client and server. List every place where the two could give different results for the same input, such as floats, random numbers, frame time or order of updates."

> "Write a test that runs 1,000 random inputs through the client predictor and the server, with 150 ms of fake latency, and checks that the final positions match after server reconciliation."

> "Explain our lag compensation code step by step. What is the maximum rewind time, and where is it set?"

### What to check yourself

- The agent did not move any authority from the server to the client "to make it faster".
- Inputs carry sequence numbers, and acknowledged inputs are dropped. Otherwise the replay list grows forever.
- The tests use real lag, jitter and packet loss, not only a perfect local network.
- Any number in a latency budget comes from your own measurements or a cited source.
- The rewind limit is a deliberate choice that you can explain to players.

## Check yourself

## Go deeper

Start with [tick rate, snapshots and bandwidth](/learn/tick-rate-snapshots-bandwidth). Next, read [authoritative servers and anti-cheat](/learn/authoritative-servers-anti-cheat) and [lockstep and rollback netcode](/learn/lockstep-and-rollback-netcode).
