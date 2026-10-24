import { useState, useMemo } from "react";
import { useParams, Link } from "react-router-dom";
import {
  ArrowLeft, Music, Sparkles, BookOpen, Send, Lightbulb, PenTool, Headphones,
} from "lucide-react";
import { units } from "../data/topics";
import { songMapping } from "../data/songMapping";
import { generateLyrics } from "../data/lyricsGenerator";
import AudioPlayer from "../components/AudioPlayer";

const langOptions = [
  { code: "en", label: "English" },
  { code: "hi", label: "हिंदी" },
  { code: "mr", label: "मराठी" },
];

export default function LearningPage({ language, setLanguage }) {
  const { topicId } = useParams();
  const [showLyrics, setShowLyrics] = useState(false);
  const [lyricsLoading, setLyricsLoading] = useState(false);
  const [showSong, setShowSong] = useState(false);
  const [songLoading, setSongLoading] = useState(false);
  const [chatInput, setChatInput] = useState("");
  const [chatMessages, setChatMessages] = useState([]);

  const { topic, unit } = useMemo(() => {
    for (const u of units) {
      const st = u.subtopics.find((s) => s.id === topicId);
      if (st) return { topic: st, unit: u };
    }
    return { topic: null, unit: null };
  }, [topicId]);

  const mapping = songMapping.find((m) => m.topicId === topicId);

  if (!topic) {
    return (
      <div className="learning-page">
        <div className="not-found">
          <h2>Topic not found</h2>
          <Link to="/topics" className="btn btn-primary">Browse Topics</Link>
        </div>
      </div>
    );
  }

  const songLang = language === "en" ? "english" : "hindi";
  const matchedSongs = mapping ? mapping[songLang] : [];
  const primarySong = matchedSongs[0];

  const handleGenerateLyrics = () => {
    setLyricsLoading(true);
    setTimeout(() => {
      setLyricsLoading(false);
      setShowLyrics(true);
    }, 2000);
  };

  const handleGenerateSong = () => {
    setSongLoading(true);
    setTimeout(() => {
      setSongLoading(false);
      setShowSong(true);
    }, 2500);
  };

  const mockResponses = [
    {
      match: (q) => q.includes("price") || q.includes("increase") || q.includes("decrease"),
      text: `When price increases for a normal good, the quantity demanded typically decreases. This is the law of demand. Consumers either buy less, switch to substitutes, or postpone their purchase.`,
    },
    {
      match: (q) => q.includes("simple") || q.includes("easy"),
      text: `In simple terms: ${topic.description.en.split(".")[0]}. Think of it like everyday choices you make — whether to buy something or save your money for later.`,
    },
    {
      match: (q) => q.includes("example") || q.includes("real"),
      text: `Here's a real-world example: Consider the market for smartphones. When new models launch at high prices, few people buy them. But as prices drop over time, more and more people purchase them — that's ${topic.keywords.en[0]} in action.`,
    },
  ];

  const handleChat = () => {
    if (!chatInput.trim()) return;
    const q = chatInput.toLowerCase();
    setChatMessages((prev) => [...prev, { role: "user", text: chatInput }]);
    setChatInput("");

    setTimeout(() => {
      const found = mockResponses.find((r) => r.match(q));
      const response = found
        ? found.text
        : `Great question about ${topic.name.en}! ${topic.description.en} This is a key concept that helps us understand how markets and economies work.`;
      setChatMessages((prev) => [...prev, { role: "ai", text: response }]);
    }, 1200);
  };

  const lyrics = generateLyrics(topicId, topic.name.en, topic.keywords.en);

  return (
    <div className="learning-page">
      <Link to="/topics" className="back-link">
        <ArrowLeft size={18} /> Back to Topics
      </Link>

      <div className="learning-header">
        <span className="unit-badge">
          Unit {unit.unitNo}: {unit.name[language]}
        </span>
        <h1>{topic.name[language]}</h1>
        <div className="lang-tabs">
          {langOptions.map((opt) => (
            <button
              key={opt.code}
              className={`lang-tab ${language === opt.code ? "active" : ""}`}
              onClick={() => setLanguage(opt.code)}
            >
              {opt.label}
            </button>
          ))}
        </div>
      </div>

      <div className="learning-grid">
        {/* Concept Card */}
        <div className="learn-card concept-card">
          <div className="card-header">
            <BookOpen size={20} />
            <h2>Concept</h2>
          </div>
          <p className="concept-text">{topic.description[language]}</p>
          <div className="key-ideas">
            <h3>
              <Lightbulb size={16} /> Key Ideas
            </h3>
            <div className="keyword-list">
              {topic.keywords[language].map((kw) => (
                <span key={kw} className="keyword-pill">{kw}</span>
              ))}
            </div>
          </div>
        </div>

        {/* Song Match Card */}
        {primarySong && (
          <div className="learn-card song-match-card">
            <div className="card-header">
              <Music size={20} />
              <h2>Your Learning Song</h2>
            </div>
            <div className="song-info">
              <div className="song-cover">
                <Music size={32} />
              </div>
              <div className="song-details">
                <h3>{primarySong.title}</h3>
                <p className="song-artist">{primarySong.artist}</p>
                <div className="song-meta">
                  <span>{primarySong.year}</span>
                  <span>{songLang === "hindi" ? "Hindi" : "English"}</span>
                </div>
              </div>
            </div>
            {mapping && (
              <div className="song-reason">
                <Sparkles size={14} />
                <p>{mapping.reason}</p>
              </div>
            )}
            {matchedSongs.length > 1 && (
              <div className="alt-song">
                <span>Also recommended:</span> {matchedSongs[1].title} &mdash;{" "}
                {matchedSongs[1].artist}
              </div>
            )}
            <span className="prototype-badge">Song mapping from research dataset</span>
          </div>
        )}

        {/* AI Chat Card */}
        <div className="learn-card chat-card">
          <div className="card-header">
            <Sparkles size={20} />
            <h2>AI Learning Assistant</h2>
            <span className="prototype-badge">Prototype AI</span>
          </div>
          <div className="chat-messages">
            {chatMessages.length === 0 && (
              <div className="chat-empty">
                <p>Ask about this topic</p>
                <div className="chat-suggestions">
                  {[
                    "What happens when price increases?",
                    "Explain in simple words",
                    "Give me a real-world example",
                  ].map((q) => (
                    <button key={q} onClick={() => setChatInput(q)}>
                      {q}
                    </button>
                  ))}
                </div>
              </div>
            )}
            {chatMessages.map((msg, i) => (
              <div key={i} className={`chat-msg ${msg.role}`}>
                {msg.role === "ai" && <Sparkles size={14} />}
                <p>{msg.text}</p>
              </div>
            ))}
          </div>
          <div className="chat-input-wrap">
            <input
              type="text"
              value={chatInput}
              onChange={(e) => setChatInput(e.target.value)}
              placeholder={`Ask about ${topic.name.en}...`}
              onKeyDown={(e) => e.key === "Enter" && handleChat()}
            />
            <button onClick={handleChat}>
              <Send size={18} />
            </button>
          </div>
        </div>

        {/* Lyrics Card */}
        <div className="learn-card lyrics-card">
          <div className="card-header">
            <PenTool size={20} />
            <h2>Learn It Through Lyrics</h2>
          </div>
          {!showLyrics && !lyricsLoading && (
            <div className="lyrics-cta">
              <p>Generate educational lyrics based on this topic&apos;s concepts</p>
              <button className="btn btn-primary" onClick={handleGenerateLyrics}>
                <Sparkles size={16} /> Generate Lyrics
              </button>
            </div>
          )}
          {lyricsLoading && (
            <div className="loading-state">
              <div className="loading-dots">
                <span />
                <span />
                <span />
              </div>
              <p>Creating educational lyrics...</p>
            </div>
          )}
          {showLyrics && (
            <div className="lyrics-display">
              <pre>{lyrics}</pre>
              <span className="prototype-badge">Template-based prototype lyrics</span>
            </div>
          )}
        </div>

        {/* Song Generation Card */}
        <div className="learn-card generate-card">
          <div className="card-header">
            <Headphones size={20} />
            <h2>Create Your Learning Song</h2>
          </div>
          {!showSong && !songLoading && (
            <div className="generate-cta">
              <div className="generate-info">
                <div className="generate-row">
                  <span>Topic:</span> {topic.name[language]}
                </div>
                <div className="generate-row">
                  <span>Language:</span>{" "}
                  {langOptions.find((l) => l.code === language)?.label}
                </div>
                <div className="generate-row">
                  <span>Style:</span> Instrumental
                </div>
              </div>
              <button
                className="btn btn-primary"
                onClick={handleGenerateSong}
                disabled={!showLyrics}
              >
                <Music size={16} /> Generate Song
              </button>
              {!showLyrics && <p className="hint">Generate lyrics first</p>}
            </div>
          )}
          {songLoading && (
            <div className="loading-state">
              <div className="loading-dots">
                <span />
                <span />
                <span />
              </div>
              <p>Turning your lesson into music...</p>
              <div className="loading-steps">
                <div className="loading-step done">&#10003; Lyrics prepared</div>
                <div className="loading-step done">&#10003; Style selected</div>
                <div className="loading-step active">&#9676; Generating melody...</div>
              </div>
            </div>
          )}
          {showSong && (
            <div className="song-player-section">
              <div className="loading-steps complete">
                <div className="loading-step done">&#10003; Lyrics prepared</div>
                <div className="loading-step done">&#10003; Style selected</div>
                <div className="loading-step done">&#10003; Melody created</div>
                <div className="loading-step done">&#10003; Song ready</div>
              </div>
              <AudioPlayer />
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
