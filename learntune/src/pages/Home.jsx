import { Link } from "react-router-dom";
import { BookOpen, Music, Sparkles, Headphones, PenTool, ArrowRight } from "lucide-react";

const steps = [
  { icon: BookOpen, title: "Choose a Topic", desc: "Browse Economics units and subtopics from the syllabus" },
  { icon: Sparkles, title: "Understand the Concept", desc: "Read clear explanations in English, Hindi, or Marathi" },
  { icon: Music, title: "Discover a Song", desc: "Get a song recommendation mapped to the concept" },
  { icon: PenTool, title: "Learn Through Lyrics", desc: "Generate educational lyrics based on topic keywords" },
  { icon: Headphones, title: "Play a Learning Tune", desc: "Listen to a procedurally generated background melody" },
];

export default function Home() {
  return (
    <div className="home">
      <section className="hero">
        <div className="hero-badge">AI-Assisted Learning Prototype</div>
        <h1>
          Turn Economics into
          <br />
          something you can <span className="gradient-text">remember</span>
        </h1>
        <p className="hero-sub">
          Explore academic concepts through explanations, music recommendations, and AI-generated
          learning content.
        </p>
        <div className="hero-actions">
          <Link to="/topics" className="btn btn-primary">
            Start Learning <ArrowRight size={18} />
          </Link>
          <Link to="/songs" className="btn btn-secondary">
            Explore Songs
          </Link>
        </div>
        <div className="hero-visual">
          <div className="hero-icon-group">
            <div className="hero-float-icon icon-book">
              <BookOpen size={32} />
            </div>
            <div className="hero-float-icon icon-music">
              <Music size={32} />
            </div>
            <div className="hero-float-icon icon-ai">
              <Sparkles size={32} />
            </div>
          </div>
        </div>
      </section>

      <section className="how-it-works">
        <h2>How LearnTune Works</h2>
        <p className="section-sub">Five simple steps from textbook to tune</p>
        <div className="steps-grid">
          {steps.map((step, i) => (
            <div key={i} className="step-card" style={{ animationDelay: `${i * 0.1}s` }}>
              <div className="step-number">{i + 1}</div>
              <div className="step-icon">
                <step.icon size={24} />
              </div>
              <h3>{step.title}</h3>
              <p>{step.desc}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="home-cta">
        <h2>Ready to learn differently?</h2>
        <p>Connect Economics concepts with music and make studying memorable.</p>
        <Link to="/topics" className="btn btn-primary">
          Browse Topics <ArrowRight size={18} />
        </Link>
      </section>
    </div>
  );
}
