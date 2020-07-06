/*

experimental.

*/

// record a stereo soundfile (wait 10 sec)
(
{
	var mod =  SinOsc.ar(XLine.kr([700, 730], 0.01, 10)) * SinOsc.ar(0.4).range(0, 2pi);
	SinOsc.ar(300, mod) * 0.1
}.asBuffer(10, fadeTime:0.1, action: { |buffer|
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

b.asEvent((start: 800, num: 2000)).play;
b.asEvent((start: 810, num: 2000, rate: 2)).play;



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

