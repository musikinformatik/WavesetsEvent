/*

experimental.

*/


// record a stereo soundfile (wait 10 sec)
(
var time = 8;
var f = {
	var mod = Line.kr(0, 1, time);
	var imp = Impulse.ar(mod.linexp(0, 1, 1, 16) * [2, 3]);
	var fmod =  SinOsc.ar(mod.linexp(0, 1, [700, 730] * rrand(0.4, 1.4), 0.01));
	fmod = fmod * Decay2.ar(imp, 0.02, 0.08, 2pi, 0.3pi);
	SinOsc.ar(300 + (mod * 145), fmod) * 0.1
};

f.asBuffer(time, fadeTime:0.1, action: { |buffer|
	buffer.write(Platform.resourceDir +/+ "sc-wavesets-test.aiff");
	"... finished writing ....".postln;
	buffer.free;
}
);
)


s.boot;
b = WavesetsMultiEvent.read(Platform.resourceDir +/+ "sc-wavesets-test.aiff", [0, 1], minLength: 10).add(\some);
WavesetsMultiEvent.prepareSynthDefs;

// get some information
b.wavesetsArray[0].size;
b.wavesetsArray[1].size;
b.bufferArray[0].duration
b.size

b.asEvent((start: 0, num: 2000)).play;
b.asEvent((start: 0, num: 2000, rate: 2)).play;


WavesetsEvent.all.at(\some).xings

// signal reconstruction
(
Pdef(\x,
	Pwavesets(
		Pbind(
			\name, \some,
			\start, Pn(Pseries(0, 1, b.size - 1)),
			\rate, 1,
			\num, 1,
			\legato, 1.0,
			//\amp, 0.1
		)
	)
).play;
)

// signal reconstruction with the other channel as a guide
(
Pdef(\x,
	Pwavesets(
		Pbind(
			\start, Pn(Pseries(0, 1, 3270)),
			\num, 1,
			\guide, 1,
			\legato, 1.0,
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
			\start, Pn(Pseries(0, 1, 3000)) + Pbrown(0, 270, 12, inf),
			\num, 2,
			\rate, Pbrown(0.8, 1.3, 0.01, inf),
			\legato, Pbrown(0.8, 1.2, 0.1),
			\repeats, Pbrown(0, 1, 0.1).linexp(0, 1, 1, 14),
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
			\startTime, Ptime() % 7,
			\num, 1,
			\fixsustain, 1/400,
			\legato, 1.0,
			\name, \some,
		)
	)
).play;
)


(
Pdef(\x,
	Pwavesets(
		Pbind(
			\start, Pn(Pseries(0, 2, 3270 - 27)),
			\num, 27,
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
			\startTime, Ptime.new.wrap(0, 9.9),
			\num, 2,
			\legato, 0.4,
			\rate, 1.3,
			\name, \some
		)
	)
).play;
)


(
Pdef(\x,
	Pwavesets(
		Pbind(
			\start, Pbrown(0, 3270 - 140, 30),
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
			\rate, 1,
			\rate2, 2, // here still some glitches
			\legato, 0.5,
			\name, \some
		)
	)
).play;
)

