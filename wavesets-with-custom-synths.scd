
// using wavesets with your own synth


a = WavesetsEvent.read(Platform.resourceDir +/+ "sounds/a11wlk01-44_1.aiff").add(\a11);

(
SynthDef(\ws1848, { | out = 0, buf = 0, startFrame = 0, numFrames = 441, rate = 1, sustain = 1, amp = 0.1, pan, interpolation = 2 |
	var phasor = Phasor.ar(0, BufRateScale.ir(buf) * rate * sign(numFrames), 0, abs(numFrames)) + startFrame;
	var env = EnvGen.ar(Env([amp, amp, 0], [sustain, 0]), doneAction: 2);
	var snd = BufRd.ar(1, buf, phasor, 1, interpolation) * env;
	snd = snd * SinOsc.ar(rate * 200 + (phasor / BufFrames.ir(buf))) + snd;

	OffsetOut.ar(out, Pan2.ar(snd, pan));
}, \ir.dup(9)).add;
)


(
Pdef(\x,
	Pwavesets(
		Pbind(
			\name, \a11,
			\instrument, \ws1848,
			\start, Pn(Pseries(0, 1, 1848)),
			\num, 2,
			\repeats, 10,
			\rate, 1,
			\legato, 2.0,
			\amp, 0.1,
			\pan, Pbrown(-0.5, 0.5, 0.001)
		)
	)
).play
)


// a synthdef that fills the gaps (legato < 1) with a resonant filter

(
SynthDef(\ws1848, { | out = 0, freq = 440, buf = 0, impulse = 0.1, resonance = 4,
	startFrame = 0, numFrames = 441, rate = 1, sustain = 1, legato = 1, amp = 0.1, pan, interpolation = 2 |

	var phasor, env, snd, longEnv, decay;
	decay = max(0, 1 - legato) * sustain;
	phasor = Phasor.ar(0, BufRateScale.ir(buf) * rate * sign(numFrames), 0, abs(numFrames)) + startFrame;
	env = EnvGen.ar(Env([amp, amp, 0], [sustain, 0]));
	longEnv = EnvGen.ar(Env([1, 1, 0], [sustain, decay], -4), doneAction: 2);

	snd = BufRd.ar(1, buf, phasor, 1, interpolation) * env;
	snd = snd + Ringz.ar(snd + Impulse.ar(0, 0, impulse), freq, decay * resonance);

	OffsetOut.ar(out, Pan2.ar(snd, pan, longEnv));
}, \ir.dup(9)).add;
)



(
Pdef(\x,
	Pwavesets(
		Pbind(
			\name, \a11,
			\instrument, \ws1848,
			\note, Pstutter(8, Pn(Pseries(0, 1, 40))),
			\octave, 5,
			\start, Pn(Pseries(0, 1, 1848)),
			\num, 1,
			\repeats, 1,
			\resonance, 4,
			\rate, 1,
			\impulse, 0,
			\legato, Pbrown(0.1, 1.0, 0.01),
			//\dur, 0.003,
			\amp, 0.1,
			\pan, Pbrown(-0.5, 0.5, 0.001)
		)
	)
).play
)


