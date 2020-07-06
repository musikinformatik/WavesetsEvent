/*

experimental.

*/


// record a stereo soundfile (wait 10 sec)
(
var f = {
	var mod = Line.kr(0, 1, 10);
	var fmod =  SinOsc.ar(mod.linexp(0, 1, [700, 730], 0.01));
	fmod = fmod * Decay2.ar(Impulse.ar(mod.linexp(0, 1, 1, 16) * [2, 3]), 0.01, 0.08, 2pi, 0.3pi);
	SinOsc.ar(300, fmod) * 0.1
};

f.asBuffer(10, fadeTime:0.1, action: { |buffer|
	buffer.write("~/sc-wavesets-test.aiff".standardizePath);
	"... finished writing ....".postln;
	buffer.free;
}
);
)


s.boot;

b = WavesetsMultiEvent.read("~/sc-wavesets-test.aiff".standardizePath, [0, 1], minLength: 10);
b.add(\some);
WavesetsMultiEvent.prepareSynthDefs;


b.asEvent((start: 0, num: 2000)).play;
b.asEvent((start: 0, num: 2000, rate: 2)).play;


(
Pdef(\x,
	Pwavesets(
		Pbind(
			\start, Pn(Pseries(0, 1, 10000)),
			\rate, 1,
			\num, 1,
			\legato, 1.0,
			\name, \some,
			\amp, 0.1
		)
	)
).play;
)

// same with the other channel as a guide
(
Pdef(\x,
	Pwavesets(
		Pbind(
			\start, Pn(Pseries(0, 1, 10000)),
			\num, 1,
			\guide, 1,
			\legato, 1.0,
			\name, \some,
			\amp, 0.2
		)
	)
).play;
)


s.latency = 0.05;

(
Pdef(\x,
	Pwavesets(
		Pbind(
			\start, Pn(Pseries(0, 1, 3000)) + Pbrown(0, 1, 130, inf),
			\num, 1,
			\useFrac, false,
			\guide, 0, //Pstutter(400, Prand([0, 1], inf)),
			\rate, Pbrown(0.8, 1.3, 0.01, inf),
			\legato, 1, // Pbrown(0.8, 1.2, 0.1),
			\repeats, 1, //Pbrown(1, 14, 0.1),
			\name, \some,
			\amp, 0.2
		)
	)
).play;
)



(
Pdef(\x,
	Pwavesets(
		Pbind(
			\start, Pn(Pseries(0, 2, 1000)),
			\num, 7,
			\legato, 1.0,
			\name, \some
		)
	)
).play;
)


(
Pdef(\x,
	Pwavesets(
		Pbind(
			\start, Pbrown(0, 8001, 30),
			\num, Pbrown(5, 140, 1),
			\legato, 1.0,
			\name, \some
		)
	)
).play;
)


(
Pdef(\x,
	Pwavesets(
		Pbind(
			\start, Pseries(0, 30, 100),
			\num, 80,
			\rate, 0.5,
			\rate2, 1.2,
			\legato, 1.5,
			\name, \some
		)
	)
).play;
)

