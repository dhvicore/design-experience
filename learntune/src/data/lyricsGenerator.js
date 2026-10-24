const lyricsTemplates = {
  "1-1": `[Verse 1]
Open up the books, let the learning start,
Economics lives in every market and every cart,
Micro looks at firms and the choices that we make,
Macro sees the whole — the nation's path to take.

[Chorus]
Economics, economics — it's how the world decides,
Where the money flows, where the value hides,
From the smallest trade to the grandest plan,
Understanding choices is where it all began.

[Verse 2]
Scarcity is real, wants are never done,
Resources have their limits under every sun,
But with the right knowledge, we can find our way,
Economics lights the path, every single day.`,

  "1-2": `[Verse 1]
Three roads diverge in how nations run,
Capitalism lets the free market have its fun,
Socialism says the state should hold the key,
Mixed economy blends them both — that's what we see.

[Chorus]
Systems, systems, shaping every land,
Private hands or public plans, help us understand,
Markets move and governments guide,
In this economic world, we all decide.

[Verse 2]
India chose the middle ground — a mixed design,
Private enterprise with a public lifeline,
Every system has its strengths and its cost,
Learning how they work means nothing's lost.`,

  "1-3": `[Verse 1]
Unlimited wants but the resources are few,
Every single choice means something we can't do,
That's the opportunity cost we need to weigh,
Scarcity's the reason economics saves the day.

[Chorus]
Choose wisely, choose well,
Every trade-off has a story it can tell,
When you pick one path, another fades away,
That's the price of choice we all must pay.

[Verse 2]
Land and labor, capital in hand,
These are scarce resources spread across the land,
Allocating wisely, that's the central theme,
Economics turns the scarcity into a stream.`,

  "1-4": `[Verse 1]
When the price goes high, buyers step away,
When the price comes down, more demand comes into play,
That's the law of demand — simple, clean, and clear,
Higher price means less is what consumers want to hear.

[Chorus]
Demand and supply, supply and demand,
Together they shape the price across the land,
When they meet in the middle, the market finds its beat,
Equilibrium is where the two compete.

[Verse 2]
Suppliers want more when the price is right,
Higher prices mean more goods come into sight,
Law of supply says produce when returns are high,
Together with demand, they tell us where prices lie.`,

  "1-5": `[Verse 1]
Draw a line that slopes from high to low,
That's the demand curve — as price drops, purchases grow,
Now draw another rising from the left to right,
Supply curve shows producers ready for the fight.

[Chorus]
Curves that cross, curves that meet,
Where demand meets supply is the market's heartbeat,
Equilibrium point — the balance we all need,
Where buyers and sellers silently agree.

[Verse 2]
Below the crossing point, demand exceeds the store,
Above it, surplus grows as supply creates much more,
The curves reveal the truth behind every marketplace,
Economics in a graph — elegant and full of grace.`,

  "2-1": `[Verse 1]
Perfect competition — many sellers, same product line,
Monopoly means one firm — all the power is mine,
Oligopoly — just a few control the game,
Monopolistic competition — similar but not the same.

[Chorus]
Competition drives the market, shapes the price we pay,
From perfect to imperfect, in every single way,
Understanding structures helps us see the light,
How market power determines what is wrong or right.

[Verse 2]
Barriers to entry, product differentiation,
Price-making power across every station,
The spectrum of competition tells a market's tale,
From free and open trading to a single scale.`,

  "2-4": `[Verse 1]
GDP counts everything produced within our land,
Goods and services combined — isn't that grand?
GNP goes further, adds income from abroad,
Measuring a nation's wealth, these metrics are the rod.

[Chorus]
National income, tell me how we grow,
GDP and GNP — the numbers that we know,
Counting every factory, every service, every trade,
This is how a country's economic health is weighed.

[Verse 2]
Nominal GDP shows the current price in play,
Real GDP adjusts for inflation's sway,
Per capita tells us what each person earns,
National income accounting — a concept every student learns.`,

  "2-5": `[Verse 1]
Prices climbing higher, month by month they grow,
That's inflation knocking at everybody's door, you know,
Cost-push means production costs are on the rise,
Demand-pull means too much money chasing limited supplies.

[Chorus]
Inflation, inflation — rising every day,
CPI tracks the changes in what consumers pay,
Too much is a problem, a little keeps things right,
Balancing the pressure is the central banker's fight.

[Verse 2]
The consumer price index measures what we spend,
On food and fuel and housing — watching every trend,
Moderate inflation can signal a growing land,
But hyperinflation spirals out of hand.`,

  "3-1": `[Verse 1]
Reserve Bank of India, standing tall and strong,
Regulating money, making sure nothing goes wrong,
Banker to the banks, and to the government too,
Issuing currency — that's what central banks do.

[Chorus]
RBI, RBI — the guardian of the land,
Monetary policy shaped by a steady hand,
Controlling inflation, keeping growth on track,
The central bank ensures the economy won't crack.

[Verse 2]
Lender of last resort when banks are in distress,
Managing foreign exchange, cleaning up the mess,
From repo rates to CRR, the tools are in their hands,
The central bank conducts — and the economy expands.`,

  "3-6": `[Verse 1]
Nineteen ninety-one, the winds of change arrived,
Liberalization — Indian economy revived,
Privatization said let businesses run free,
Globalization opened doors for you and me.

[Chorus]
LPG — liberalize, privatize, go global wide,
Breaking down the barriers, opening up with pride,
India stepped into the world marketplace,
New Economic Policy gave the nation grace.

[Verse 2]
Fewer licenses, more freedom to compete,
Foreign investment flowing down every street,
The world became a market, borders started to fade,
India's new economic journey was finally made.`,

  "5-1": `[Verse 1]
Planning, organizing, leading every day,
Controlling all the resources in a systematic way,
That's management — the art of getting things done right,
Through people and processes, keeping goals in sight.

[Chorus]
Manage well, manage smart,
Every function plays its part,
From the vision to the action on the ground,
Management is where success is found.

[Verse 2]
Efficiency means doing things the right way,
Effectiveness means doing the right things every day,
Combine them both and watch the organization grow,
Management's the engine — now you're in the know.`,

  "6-5": `[Verse 1]
Maslow built a pyramid of human needs,
From food and shelter up to where achievement leads,
Physiological, safety, love, esteem, and more,
Self-actualization sits right at the top floor.

[Chorus]
What motivates us? What makes us strive?
Herzberg said two factors keep the workplace alive,
Hygiene keeps dissatisfaction at bay,
Motivators are what make us love the working day.

[Verse 2]
McClelland spoke of achievement, power, and affiliation,
Three needs that drive us in every situation,
Understanding motivation helps a leader see,
What inspires their team to be the best they can be.`
};

export function generateLyrics(topicId, topicName, keywords) {
  if (lyricsTemplates[topicId]) {
    return lyricsTemplates[topicId];
  }

  const kw = keywords.slice(0, 4);
  const kwList = kw.join(", ");

  return `[Verse 1]
Let me tell you about ${topicName},
A concept that helps us understand the game,
${kw[0] ? `${kw[0]} is the key to what we learn today,` : "Knowledge is the key to what we learn today,"}
${kw[1] ? `With ${kw[1]} guiding us along the way.` : "With concepts guiding us along the way."}

[Chorus]
${topicName} — remember it well,
Every keyword is a story we can tell,
${kwList || "Economics and management combined"},
Learning through music, expanding your mind.

[Verse 2]
${kw[2] ? `${kw[2]} connects to everything we see,` : "Theory connects to everything we see,"}
${kw[3] ? `And ${kw[3]} shapes our economy.` : "And practice shapes our economy."}
From textbook pages to a catchy rhyme,
Economics and music — partners in time.

[Bridge]
Study smart, learn it right,
Let the rhythm make the concepts bright,
${topicName} is here to stay,
LearnTune helps you learn and play.`;
}
