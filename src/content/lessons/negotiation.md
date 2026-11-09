---
topic: negotiation
subtitle: Read every part of a tech offer, know what equity and CTC really mean, and ask for more honestly, with data and without fear.
status: published
illustrative_story: true
sources:
  - id: sec-startup-securities
    title: "U.S. SEC, Office of the Advocate for Small Business Capital Formation, “What different types of securities are issued to startup investors?”"
    url: https://www.sec.gov/files/startup-securities-building-blocks.pdf
    note: A two-page SEC staff explainer that defines a stock option, a strike price and an RSU in plain words.
  - id: sec-exits
    title: "U.S. SEC, Office of the Advocate for Small Business Capital Formation, “How do startups “exit” or provide liquidity to investors?”"
    url: https://www.sec.gov/files/exits-building-blocks.pdf
    note: Why private-company shares are often hard to sell, and the exits (IPO, acquisition, liquidation) that turn them into money.
  - id: investor-options
    title: "Investor.gov (U.S. SEC), “An Introduction to Options – Investor Bulletin” (March 18, 2015, updated July 16, 2026)"
    url: https://www.investor.gov/introduction-investing/general-resources/news-alerts/alerts-bulletins/investor-bulletins-63
    note: The basics of strike price and "out-of-the-money", explained for traded options.
  - id: gitlab-equity
    title: "The GitLab Handbook, “Equity Compensation”"
    url: https://handbook.gitlab.com/handbook/total-rewards/stock-options/
    note: One company's public rules for options and RSUs, including the four-year schedule, the one-year cliff and what happens when you leave.
  - id: epfo-scheme
    title: "Employees' Provident Fund Organisation, “EPF Scheme 1952 – EPFO Benefits”"
    url: https://pmvbry.epfindia.gov.in/epf-scheme/
    note: EPFO's own summary of who is covered and the 12% contribution from employee and employer.
  - id: mole-faq
    title: "Ministry of Labour & Employment (India), “Additional FAQs on Labour Codes (As on 16.03.2026)”"
    url: https://www.labour.gov.in/static/uploads/2026/03/a4ccf4c6d97c4f1f36a6d83f8c64213d.pdf
    note: Official answers on gratuity and the new meaning of wages under the labour codes, in force from 21 November 2025.
  - id: coss-2020
    title: "The Code on Social Security, 2020 (No. 36 of 2020), Gazette of India, 29 September 2020"
    url: https://www.labour.gov.in/static/uploads/2025/07/b0620548445580767b5c0d18c95c26f7.pdf
    note: "Section 53: who gets gratuity, the exceptions to the five-year rule, and the 15/26 formula."
  - id: pon-principled
    title: "Katie Shonk, “Principled Negotiation: Focus on Interests to Create Value”, PON – Program on Negotiation at Harvard Law School (3 November 2026)"
    url: https://www.pon.harvard.edu/daily/negotiation-skills-daily/principled-negotiation-focus-interests-create-value/
    note: Interests versus positions, objective criteria and BATNA, with a job-offer example.
  - id: pon-salary
    title: "PON Staff, “Meet Your Salary Expectations”, PON – Program on Negotiation at Harvard Law School (31 August 2026)"
    url: https://www.pon.harvard.edu/daily/business-negotiations/meet-your-salary-expectations/
    note: The anchoring effect in salary talks, and how to research a fair range before you counter.
  - id: colorado-epewa
    title: "Colorado Department of Labor and Employment, “Equal Pay for Equal Work Act”"
    url: https://cdle.colorado.gov/dlss/labor-laws-by-topic/equal-pay-for-equal-work-act
    note: Colorado's rules on pay in job postings and on asking about pay history.
  - id: nyc-salary
    title: "NYC Commission on Human Rights, “Salary Transparency in Job Advertisements” (Pay Transparency page)"
    url: https://www.nyc.gov/site/cchr/media/pay-transparency.page
    note: New York City's rule that job ads must show a good faith pay range.
claims_checked:
  - claim: "An RSU is a right to receive shares once vesting conditions are met, and you own no shares on the grant date; a stock option is the right but not the obligation to buy at a strike price after a vesting period."
    source: sec-startup-securities
    note: "Two-page SEC OASB PDF: 'An RSU is a right to receive shares of stock once certain vesting conditions are met. The recipient does not own any shares of stock on the date of the grant.'"
  - claim: "GitLab new-hire options vest over four years with a one-year cliff ('at most companies, including GitLab'); RSUs vest over two to four years from the grant date; unvested RSUs and options are forfeited on leaving; vested options must be exercised within 90 days."
    source: gitlab-equity
    note: "Handbook last modified 21 September 2026. Phrases found under Option Vesting, RSU Vesting & Grant Cadence, and the opening summary."
  - claim: "Employee and employer each contribute 12% of basic wages plus DA; contributions are mandatory up to ₹15,000 a month."
    source: epfo-scheme
    note: "EPFO page last updated 6 November 2026; the ₹15,000 test is on basic wages plus DA (page FAQ)."
  - claim: "Gratuity needs five years of continuous service except on death, disablement or end of fixed-term employment; it is 15 days' wages per completed year, and for monthly-rated employees monthly wages ÷ 26 × 15."
    source: coss-2020
    note: "Section 53(1) second proviso, 53(2) and Explanation 3 in the Gazette copy (No. 36 of 2020, 29 September 2020). Worked example re-computed: 1,00,000 ÷ 26 × 15 = 57,692."
  - claim: "Gratuity under the revised meaning of wages applies from 21 November 2025; fixed-term employees qualify after one year; excluded pay over 50% of remuneration is added back to wages."
    source: mole-faq
    note: "Labour Ministry FAQs as on 16.03.2026, Q1, Q6, Q11 and Q14."
  - claim: "Since 1 November 2022, NYC requires a good faith pay range in all ads for jobs performed in the city; Colorado requires pay in all postings, internal and public, and bans asking about pay history."
    source: nyc-salary
    note: "NYC CCHR page 'Effective November 1, 2022…'; Colorado text confirmed on the CDLE EPEWA page (colorado-epewa)."
reviewed_by: ai-factcheck
reviewed_on: 2026-11-09
glossary_optout: [postings, join, deadline]
quiz:
  - q: An offer has 4,800 stock options over 4 years with a 1-year cliff and monthly vesting after it. You leave after 18 months. About how many units have vested?
    options: ["0", "1,800", "2,400"]
    answer: 1
    why: "The cliff gives 1,200 at month 12. Then 100 a month for 6 months adds 600. 1,200 + 600 = 1,800."
  - q: A startup offers you stock options with a strike price of ₹100 a share. Which statement is true?
    options:
      - The options are worth at least ₹100 a share.
      - They are worth something only if the share price rises above ₹100 and you can sell the shares.
      - They turn into cash every year, like a bonus.
    answer: 1
    why: An option lets you buy at the strike price. It has value only above that price, and private shares are often hard to sell until an exit.
  - q: Your Indian offer says "CTC ₹24 lakh". What should you ask first?
    options:
      - Nothing. CTC is the money paid into your bank account.
      - How much is fixed, how much is variable, and what is inside it, such as PF and gratuity.
      - Whether the CTC can be paid in cash.
    answer: 1
    why: CTC usually includes the variable bonus and employer costs such as PF. Your monthly in-hand pay is lower.
  - q: You have no other offer. The recruiter asks if you have one. What do you do?
    options:
      - Say you have one, to get a better number.
      - Say no, and use market data and your interview results to make your case.
      - Refuse to answer and end the call.
    answer: 1
    why: Never invent an offer. Use honest data and your interests instead. A made-up offer can cost you the real one.
  - q: In negotiation terms, what is your BATNA during a job offer?
    options:
      - The highest number you plan to ask for.
      - What you will do if this deal does not happen, such as staying in your job or taking another offer.
      - The recruiter's budget limit.
    answer: 1
    why: PON explains BATNA as your best alternative, what you will do if this deal fails. A stronger alternative lets you ask with more confidence.
incidents: []
related: [interview-loops, behavioural-interviews, what-senior-means]
---

## What broke

This story is made up. Asha and Vikram, backend engineers with three years of experience, pass the same loop at the same company. Each gets the same offer by phone: base salary, a bonus and "some stock".

Vikram is relieved and says yes on the call. He sees nothing in writing until the joining date.

Asha thanks them and asks for the offer by email. Then she asks: which level is this? What kind of stock is it, and how does it vest? Is there a sign-on bonus?

The stock vests over four years, with nothing in the first year. The level is one below the role she applied for. Asha asks what it was based on, and points to the design round she led. The level stays, but the company adds a sign-on bonus and more stock.

A year later, Vikram learns the difference. Same loop, same skills, different pay. He just never asked.

## The idea in simple words

An offer is a set of parts, not one number. Each part has its own rules, risks and room to move.

Negotiation is a normal, polite talk about those parts.

> [!key] **The key idea.** Get the offer in writing, understand each part, and ask for what you need with real data. Never lie about other offers.

This lesson explains how offers work. It is not financial or tax advice. For big decisions, ask a qualified advisor.

## How it works

### The parts of a tech offer

- **Base salary.** Fixed pay, every month. It is the safest part.
- **Bonus.** Often a yearly target percentage of base. It depends on results, so it may not be paid in full.
- **Equity.** A share in the company, as RSUs or stock options. See below.
- **Sign-on bonus.** A one-time payment for joining. Ask if you must pay it back if you leave early.
- **Benefits.** Health insurance, leave and retirement plans.

### RSUs and stock options

An RSU is a right to receive shares once vesting conditions are met. You own no shares on the grant date. [@sec-startup-securities]

A stock option is the right, but not the duty, to buy shares at a fixed price, the strike price, after a vesting period. Startups often give options to employees as part of pay. [@sec-startup-securities]

Both usually vest over time. GitLab's handbook describes a four-year schedule for new-hire stock options, with a one-year cliff. It says that "at most companies, including GitLab", the cliff is generally one year. [@gitlab-equity] There, RSUs vest over two to four years from the grant date, and refresh grants have no cliff. [@gitlab-equity] Step through a made-up option grant below.

[[diagram:option-vesting-cliff]]

At GitLab, unvested RSUs and options are lost when you leave. Vested options must be bought within 90 days of leaving. [@gitlab-equity] Read your own grant agreement.

**Why options can be worth nothing.** For traded options, Investor.gov explains that a call option is "out-of-the-money" when the strike price is above the share price. [@investor-options] Employee options are not traded, but in our reading the same sums apply. If the share price stays at or below your strike price, buying at that price gains you nothing.

Private shares add a second risk. An SEC staff explainer says startup shares are "often illiquid": you cannot easily sell them. Money usually comes at an exit, such as an IPO or an acquisition. [@sec-exits] If a startup winds down, it pays its debts first. What is left goes to shareholders in order of liquidation preference. [@sec-exits] So in your sums, count startup options as uncertain and possibly worth zero, not as salary. This is our advice.

### In India: CTC versus in-hand pay

Indian offers often show CTC, the cost to company: the total the employer spends on you, not what reaches your bank.

It often includes:

- **Variable pay.** A bonus target that may be paid in part or not at all.
- **Employer PF.** EPFO says both employee and employer contribute 12% of basic wages plus dearness allowance. [@epfo-scheme] In our experience, most offers put the employer's 12% inside CTC. Your own 12% comes out of your salary.
- **Gratuity.** A lump sum paid when you leave. Most permanent staff qualify only after five years of continuous service. The Code on Social Security, 2020 waives this for death, disablement or the end of a fixed-term contract. [@coss-2020] Fixed-term staff qualify after one year. [@mole-faq] The amount is 15 days' wages per year of service. For monthly pay, that is monthly wages ÷ 26 × 15. [@coss-2020] These rules apply from 21 November 2025. [@mole-faq] It may sit in CTC each year, but you get it only if you qualify.

Here is a made-up example. "Wages" here means basic pay plus dearness allowance. Under the new codes, if other pay is more than half the total, the excess counts as wages. [@mole-faq] So assume wages of ₹12,00,000 a year, about half the pay.

- CTC: ₹24,00,000 a year.
- Variable pay: ₹2,40,000.
- Employer PF: 12% × ₹12,00,000 = ₹1,44,000.
- Gratuity: ₹1,00,000 ÷ 26 × 15 ≈ ₹57,700.
- Insurance: ₹12,000.
- Fixed pay: 24,00,000 − 2,40,000 − 1,44,000 − 57,700 − 12,000 = ₹19,46,300.
- After your own PF of ₹1,44,000: ₹18,02,300, about ₹1,50,000 a month before income tax.

EPFO makes contributions mandatory when basic pay plus DA is up to ₹15,000 a month, and voluntary above. [@epfo-scheme] So employers calculate PF in different ways. Ask for a full salary breakup.

### Where to find market data

As of 2026, these are common sites. In our experience:

- **levels.fyi** shows pay by company and level, shared by users.
- **Glassdoor** has salaries and company reviews posted by employees and job seekers.
- **Blind** is an anonymous forum where tech workers discuss pay and offers.

All three are mostly self-reported. We think people with high or unusual pay may post more often. Data can be old. Use a range from several sources.

### Three ideas from negotiation research

**BATNA.** Your Best Alternative to a Negotiated Agreement is what you will do if this deal fails. PON says a deal should beat your BATNA, and interviewing at several firms makes it stronger. [@pon-principled]

**Anchoring.** PON describes the anchoring effect: the first number tends to pull later offers towards it. Employers often name the first number, so plan for it. [@pon-salary] Research your range before the call.

**Interests, not positions.** A position is a fixed demand; an interest is the need behind it. In PON's job example, the company cares about its budget. A signing bonus instead of a raise, or a salary review tied to milestones, can still meet your need. [@pon-principled]

### The script

Our suggested script:

1. **Thank them and get it in writing.** "Thank you, I'm excited about this team. Could you email me the full offer?"
2. **Ask for time.** "I'd like a few days to review it. Can I reply by Friday?"
3. **Ask about every part.** Level, base, bonus target, equity type and vesting, sign-on, benefits, and in India the CTC breakup.
4. **Ask about the level.** Level sets your pay band. The [interview loops lesson](/learn/big-tech-interview-loops) explains how it is set.
5. **Make one clear request.** "Based on market data and how my design round went, I'd ask for ₹X base. If base is fixed, could we look at the sign-on bonus?"
6. **Use competing offers honestly.** Name a real offer only if you have it. Be ready to show it.

Never lie about another offer. Companies may ask for the letter. A lie can end a real offer, and it is wrong.

### What is usually negotiable

In our experience, sign-on bonus and equity move most often. Base moves within the band for your level. The level itself moves only with evidence. Benefits and bonus percentages are usually fixed by policy.

### Exploding offers

An exploding offer has a very short deadline, such as 24 hours. Our advice: politely ask for a few more days. If they refuse, still read every term before you decide.

### Pay transparency laws

As of 2026, Colorado's Equal Pay for Equal Work Act requires employers to disclose pay in all job postings, internal and public. It also bans asking about your pay history. [@colorado-epewa] Since 1 November 2022, New York City has required a "good faith pay range" in all job ads for work done in the city. [@nyc-salary] A posted range is useful data. But PON warns that an advertised range can anchor talks in the employer's favour, so do your own research too. [@pon-salary]

### Raises and promotions inside your company

Internal pay follows your company's levels and review cycle. [Career ladders and level expectations](/learn/career-ladders-level-expectations) covers it.

## Trade-offs

| Approach | Honest? | Risk | Use it when |
|---|---|---|---|
| Accept on the call | Yes. | Less pay or a lower level than you could get. | Almost never. |
| Counter with market data | Yes. | Low. The answer may be no. | Usually. |
| Counter with a real competing offer | Yes. | They may decline to match. | You would really take the other offer. |
| Bluff about an offer you do not have | No. | You can lose the offer and your name. | Never. |

**Simple rule:** understand every part, know your alternative, ask once with evidence, and stay honest.

## At work: how a senior uses this

- Before a job search, write your BATNA in one line. "If this fails, I stay and aim for promotion in March."
- Compare offers in the same terms. Turn equity into a yearly value, and mark private equity as uncertain.
- As an interviewer, write clear level evidence. It helps candidates get the right level.

## With AI

### What to ask the agent

> "Act as a polite but firm recruiter. Role-play the call while I ask about the level, vesting and a sign-on bonus. Then tell me where I was unclear."

> "Here is my offer letter with names removed. List every pay component, the vesting schedule, any cliff, and any clause about paying back a bonus. Quote the exact lines."

### What to check yourself

- Every number the agent reads from your letter, against the letter itself.
- Tax and legal claims. Confirm them with official sources or an advisor.
- Your script. Never ask the agent to invent a competing offer or fake data.
- Privacy. Remove names, IDs and salary account details before you paste.

## Check yourself

## Go deeper

Before this: [big-tech interview loops](/learn/big-tech-interview-loops) and [behavioural interviews](/learn/behavioural-interviews-star). Also: [what senior really means](/learn/what-senior-really-means). Next: [career ladders](/learn/career-ladders-level-expectations).
