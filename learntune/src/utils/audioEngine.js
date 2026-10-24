class AudioEngine {
  constructor() {
    this.ctx = null;
    this.isPlaying = false;
    this.gainNode = null;
    this.oscillators = [];
    this.startTime = 0;
    this.duration = 32;
  }

  init() {
    if (!this.ctx) {
      this.ctx = new (window.AudioContext || window.webkitAudioContext)();
    }
    if (this.ctx.state === "suspended") {
      this.ctx.resume();
    }
  }

  play() {
    this.init();
    this.stop();

    const now = this.ctx.currentTime;
    this.startTime = now;
    this.isPlaying = true;

    this.gainNode = this.ctx.createGain();
    this.gainNode.gain.value = 0.25;
    this.gainNode.connect(this.ctx.destination);

    const notes = [261.63, 293.66, 329.63, 392.0, 440.0, 523.25, 587.33, 659.25];
    const bpm = 110;
    const beat = 60 / bpm;

    this._playPad(now, notes, beat);
    this._playMelody(now, notes, beat);

    this.gainNode.gain.setValueAtTime(0.25, now + this.duration - 3);
    this.gainNode.gain.linearRampToValueAtTime(0, now + this.duration);

    this._stopTimeout = setTimeout(() => this.stop(), this.duration * 1000);
  }

  _playPad(start, notes, beat) {
    const chords = [
      [notes[0] / 2, notes[2] / 2, notes[4] / 2],
      [notes[5] / 4, notes[0] / 2, notes[2] / 2],
      [notes[3] / 2, notes[5] / 2, notes[0]],
      [notes[4] / 2, notes[6] / 2, notes[1]],
    ];

    const padGain = this.ctx.createGain();
    padGain.gain.value = 0.06;
    padGain.connect(this.gainNode);

    for (let r = 0; r < Math.ceil(this.duration / (16 * beat)); r++) {
      chords.forEach((chord, i) => {
        const t = start + (r * 16 + i * 4) * beat;
        if (t > start + this.duration) return;
        chord.forEach((freq) => {
          const osc = this.ctx.createOscillator();
          osc.type = "sine";
          osc.frequency.value = freq;
          const g = this.ctx.createGain();
          g.gain.setValueAtTime(0, t);
          g.gain.linearRampToValueAtTime(0.06, t + 0.2);
          g.gain.setValueAtTime(0.06, t + 4 * beat - 0.2);
          g.gain.linearRampToValueAtTime(0, t + 4 * beat);
          osc.connect(g);
          g.connect(padGain);
          osc.start(t);
          osc.stop(t + 4 * beat + 0.1);
          this.oscillators.push(osc, g);
        });
      });
    }
    this.oscillators.push(padGain);
  }

  _playMelody(start, notes, beat) {
    const melodyGain = this.ctx.createGain();
    melodyGain.gain.value = 0.12;
    melodyGain.connect(this.gainNode);

    const pattern = [
      { n: 0, d: 1 }, { n: 2, d: 0.5 }, { n: 4, d: 0.5 }, { n: 5, d: 1 },
      { n: 4, d: 1 }, { n: 2, d: 0.5 }, { n: 3, d: 0.5 }, { n: 2, d: 1 },
      { n: 0, d: 0.5 }, { n: 2, d: 0.5 }, { n: 3, d: 1 }, { n: 4, d: 1 },
      { n: 5, d: 1.5 }, { n: 4, d: 0.5 }, { n: 2, d: 1 }, { n: 0, d: 2 },
    ];

    let t = start + 0.5;
    for (let r = 0; r < 4; r++) {
      for (const { n, d } of pattern) {
        if (t > start + this.duration - 1) break;
        const freq = notes[n];
        const dur = d * beat;
        const osc = this.ctx.createOscillator();
        osc.type = "triangle";
        osc.frequency.value = freq;
        const g = this.ctx.createGain();
        g.gain.setValueAtTime(0, t);
        g.gain.linearRampToValueAtTime(0.12, t + 0.03);
        g.gain.setValueAtTime(0.1, t + dur * 0.7);
        g.gain.linearRampToValueAtTime(0, t + dur);
        osc.connect(g);
        g.connect(melodyGain);
        osc.start(t);
        osc.stop(t + dur + 0.05);
        this.oscillators.push(osc, g);
        t += dur;
      }
      t += beat;
    }
    this.oscillators.push(melodyGain);
  }

  stop() {
    this.isPlaying = false;
    if (this._stopTimeout) {
      clearTimeout(this._stopTimeout);
      this._stopTimeout = null;
    }
    this.oscillators.forEach((node) => {
      try { node.disconnect(); } catch (e) { /* already disconnected */ }
    });
    this.oscillators = [];
    if (this.gainNode) {
      try { this.gainNode.disconnect(); } catch (e) { /* already disconnected */ }
      this.gainNode = null;
    }
  }

  getProgress() {
    if (!this.isPlaying || !this.ctx) return 0;
    return Math.min((this.ctx.currentTime - this.startTime) / this.duration, 1);
  }

  getCurrentTime() {
    if (!this.ctx || !this.isPlaying) return 0;
    return Math.min(this.ctx.currentTime - this.startTime, this.duration);
  }

  getDuration() {
    return this.duration;
  }
}

const audioEngine = new AudioEngine();
export default audioEngine;
