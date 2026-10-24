import { useState, useEffect, useRef } from "react";
import { Play, Pause } from "lucide-react";
import audioEngine from "../utils/audioEngine";

export default function AudioPlayer() {
  const [playing, setPlaying] = useState(false);
  const [progress, setProgress] = useState(0);
  const [currentTime, setCurrentTime] = useState(0);
  const animRef = useRef(null);

  const tick = () => {
    if (audioEngine.isPlaying) {
      setProgress(audioEngine.getProgress());
      setCurrentTime(audioEngine.getCurrentTime());
      animRef.current = requestAnimationFrame(tick);
    } else {
      setPlaying(false);
      setProgress(0);
      setCurrentTime(0);
    }
  };

  const handlePlay = () => {
    if (playing) {
      audioEngine.stop();
      setPlaying(false);
      setProgress(0);
      cancelAnimationFrame(animRef.current);
    } else {
      audioEngine.play();
      setPlaying(true);
      animRef.current = requestAnimationFrame(tick);
    }
  };

  useEffect(() => {
    return () => {
      cancelAnimationFrame(animRef.current);
      audioEngine.stop();
    };
  }, []);

  const formatTime = (s) => {
    const m = Math.floor(s / 60);
    const sec = Math.floor(s % 60);
    return `${m}:${sec.toString().padStart(2, "0")}`;
  };

  return (
    <div className="audio-player">
      <div className="audio-player-top">
        <button className="audio-play-btn" onClick={handlePlay}>
          {playing ? <Pause size={20} /> : <Play size={20} />}
        </button>

        <div className="audio-progress-wrap">
          <div className="audio-progress-bar">
            <div className="audio-progress-fill" style={{ width: `${progress * 100}%` }} />
          </div>
          <div className="audio-times">
            <span>{formatTime(currentTime)}</span>
            <span>{formatTime(audioEngine.getDuration())}</span>
          </div>
        </div>
      </div>

      {playing && (
        <div className="audio-waveform">
          {Array.from({ length: 24 }).map((_, i) => (
            <div key={i} className="wave-bar" style={{ animationDelay: `${i * 0.07}s` }} />
          ))}
        </div>
      )}

      <span className="prototype-badge">Prototype audio — Web Audio API</span>
    </div>
  );
}
