import { useState, useMemo } from "react";
import { Music, Search } from "lucide-react";
import { songs } from "../data/songs";

export default function SongLibrary() {
  const [langFilter, setLangFilter] = useState("all");
  const [genreFilter, setGenreFilter] = useState("all");
  const [search, setSearch] = useState("");

  const genres = useMemo(() => [...new Set(songs.map((s) => s.genre))], []);
  const languages = useMemo(() => [...new Set(songs.map((s) => s.language))], []);

  const filtered = useMemo(() => {
    return songs.filter((s) => {
      if (langFilter !== "all" && s.language !== langFilter) return false;
      if (genreFilter !== "all" && s.genre !== genreFilter) return false;
      if (search) {
        const q = search.toLowerCase();
        return (
          s.title.toLowerCase().includes(q) || s.artist.toLowerCase().includes(q)
        );
      }
      return true;
    });
  }, [langFilter, genreFilter, search]);

  return (
    <div className="songs-page">
      <div className="songs-header">
        <h1>Song Library</h1>
        <p>Browse our curated collection of {songs.length} songs used for learning</p>
      </div>

      <div className="songs-toolbar">
        <div className="songs-search">
          <Search size={18} />
          <input
            placeholder="Search songs or artists..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <div className="songs-filters">
          <select value={langFilter} onChange={(e) => setLangFilter(e.target.value)}>
            <option value="all">All Languages</option>
            {languages.map((l) => (
              <option key={l} value={l}>{l}</option>
            ))}
          </select>
          <select value={genreFilter} onChange={(e) => setGenreFilter(e.target.value)}>
            <option value="all">All Genres</option>
            {genres.map((g) => (
              <option key={g} value={g}>{g}</option>
            ))}
          </select>
        </div>
      </div>

      <p className="results-count">{filtered.length} songs found</p>

      <div className="songs-grid">
        {filtered.map((song, i) => (
          <div key={i} className="song-lib-card">
            <div className="song-lib-icon">
              <Music size={24} />
            </div>
            <div className="song-lib-info">
              <h3>{song.title}</h3>
              <p className="song-lib-artist">{song.artist}</p>
              <div className="song-lib-meta">
                <span className="meta-tag">{song.language}</span>
                <span className="meta-tag">{song.genre}</span>
                <span className="meta-tag">{song.year}</span>
                {song.movie && <span className="meta-tag movie">{song.movie}</span>}
              </div>
            </div>
            <span className="song-rank">#{song.rank}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
