
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



(
SynthDef(\ws1848, { | out = 0, buf = 0, startFrame = 0, numFrames = 441, rate = 1, sustain = 1, amp = 0.1, pan, interpolation = 2 |
	var phasor, env, snd;
	phasor = Phasor.ar(0, BufRateScale.ir(buf) * rate * sign(numFrames), 0, abs(numFrames)) + startFrame;
	env = EnvGen.ar(Env([amp, amp, 0], [sustain, Rand(0.05, 0.1)]), doneAction: 2);
	snd = BufRd.ar(1, buf, phasor, 1, interpolation);
	snd = HPF.ar(snd, 1000) * 5;
	OffsetOut.ar(out, Pan2.ar(snd, pan, env));
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
			\rate, [0, 4, 7, 11].midiratio,
			\legato, 2.0,
			\amp, 0.1,
			\pan, Pbrown(-0.5, 0.5, 0.001)
		)
	)
).play
)
