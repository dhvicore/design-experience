import { useState, useMemo } from "react";
import { Link } from "react-router-dom";
import { Search, BookOpen, ChevronRight } from "lucide-react";
import { units } from "../data/topics";

export default function Topics({ language }) {
  const [search, setSearch] = useState("");
  const [selectedUnit, setSelectedUnit] = useState(null);

  const filtered = useMemo(() => {
    if (!search.trim() && !selectedUnit) return units;
    const q = search.toLowerCase();
    return units
      .filter((u) => !selectedUnit || u.unitNo === selectedUnit)
      .map((unit) => ({
        ...unit,
        subtopics: unit.subtopics.filter(
          (st) =>
            !q ||
            st.name[language].toLowerCase().includes(q) ||
            st.name.en.toLowerCase().includes(q) ||
            st.keywords[language].some((k) => k.toLowerCase().includes(q)) ||
            st.keywords.en.some((k) => k.toLowerCase().includes(q))
        ),
      }))
      .filter((u) => u.subtopics.length > 0);
  }, [search, selectedUnit, language]);

  return (
    <div className="topics-page">
      <div className="topics-header">
        <h1>Explore Economics</h1>
        <p>Choose a topic and learn it through music</p>
      </div>

      <div className="topics-search">
        <Search size={20} />
        <input
          type="text"
          placeholder="Search topics, keywords..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      <div className="topics-filters">
        {units.map((u) => (
          <button
            key={u.unitNo}
            className={`filter-chip ${selectedUnit === u.unitNo ? "active" : ""}`}
            onClick={() => setSelectedUnit(selectedUnit === u.unitNo ? null : u.unitNo)}
          >
            Unit {u.unitNo}
          </button>
        ))}
        {selectedUnit && (
          <button className="filter-chip clear" onClick={() => setSelectedUnit(null)}>
            Clear
          </button>
        )}
      </div>

      <div className="units-list">
        {filtered.map((unit) => (
          <div key={unit.unitNo} className="unit-section">
            <div className="unit-header">
              <BookOpen size={20} />
              <h2>
                Unit {unit.unitNo}: {unit.name[language]}
              </h2>
              <span className="topic-count">{unit.subtopics.length} topics</span>
            </div>
            <div className="topic-cards">
              {unit.subtopics.map((st) => (
                <Link key={st.id} to={`/learn/${st.id}`} className="topic-card">
                  <div className="topic-card-top">
                    <span className="unit-badge">Unit {unit.unitNo}</span>
                  </div>
                  <h3>{st.name[language]}</h3>
                  <p className="topic-desc">
                    {st.description[language].length > 100
                      ? st.description[language].slice(0, 100) + "..."
                      : st.description[language]}
                  </p>
                  <div className="topic-keywords">
                    {st.keywords[language].slice(0, 3).map((kw) => (
                      <span key={kw} className="keyword-tag">
                        {kw}
                      </span>
                    ))}
                  </div>
                  <div className="topic-card-footer">
                    <span>Learn with Music</span>
                    <ChevronRight size={16} />
                  </div>
                </Link>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
