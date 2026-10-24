"""Experimental melody transcription; mixed audio requires musical review before use.

Run with the prototype Python environment (numpy/scipy/parselmouth/pretty_midi/soundfile).
This extracts notes only. It does not create singing or guarantee correct transcription.
"""
from pathlib import Path
import argparse
import json
import numpy as np
import parselmouth
import pretty_midi
import soundfile as sf
from scipy.ndimage import median_filter

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('source',type=Path)
parser.add_argument('output',type=Path)
parser.add_argument('--seconds',type=float,default=20)
args=parser.parse_args()
if not 1 <= args.seconds <= 90:parser.error('Use a 1–90 second excerpt.')
source=args.source
args.output.mkdir(parents=True,exist_ok=True)
audio,sr=sf.read(source,dtype='float64')
if audio.ndim>1:audio=audio.mean(axis=1)
# First chorus excerpt only. This is an automatic transcription draft for review.
audio=audio[:int(sr*args.seconds)]
snd=parselmouth.Sound(audio,sampling_frequency=sr)
pitch=snd.to_pitch_ac(time_step=0.01,pitch_floor=80,pitch_ceiling=450,voicing_threshold=0.65)
f0=pitch.selected_array['frequency'];times=pitch.xs()
voiced=f0>0
midi=np.zeros(len(f0));midi[voiced]=69+12*np.log2(f0[voiced]/440)
smoothed=median_filter(midi,size=5)
notes=np.where(voiced,np.rint(smoothed),0).astype(int)
events=[];start=0
for i in range(1,len(notes)+1):
 if i==len(notes) or notes[i]!=notes[start]:
  if notes[start]>0 and (i-start)*0.01>=0.08:
   events.append({'midi':int(notes[start]),'start':round(max(0,float(times[start])-.005),3),'end':round(float(times[i-1])+.005,3)})
  start=i
out=args.output
sequence=pretty_midi.PrettyMIDI(initial_tempo=84)
voice=pretty_midi.Instrument(program=0)
for event in events:voice.notes.append(pretty_midi.Note(90,event['midi'],event['start'],event['end']))
sequence.instruments.append(voice);sequence.write(str(out/'raghupati-chorus-draft.mid'))
y=np.zeros(int(args.seconds*22050))
for event in events:
 a=int(event['start']*22050);b=min(len(y),int(event['end']*22050));t=np.arange(b-a)/22050
 if len(t)<1:continue
 tone=np.sin(2*np.pi*pretty_midi.note_number_to_hz(event['midi'])*t)
 ramp=min(int(.015*22050),len(t)//2);envelope=np.ones(len(t))
 if ramp:envelope[:ramp]=np.linspace(0,1,ramp);envelope[-ramp:]=np.linspace(1,0,ramp)
 y[a:b]+=.3*tone*envelope
sf.write(out/'raghupati-chorus-draft.wav',y,22050,subtype='PCM_16')
(out/'raghupati-chorus-draft.json').write_text(json.dumps({'status':'automatic_transcription_not_verified','source':'https://www.mkgandhi.org/g_audio.php','duration':args.seconds,'notes':events,'warning':'Mixed-recording pitch extraction can confuse instruments with the lead melody. Verify before lyric assignment.'},indent=2))
print('Automatic score draft:',len(events),'note events; voiced-frame fraction:',round(float(voiced.mean()),2))
if events:print('MIDI pitch range:',min(x['midi'] for x in events),max(x['midi'] for x in events))
