

/*

WavesetsEvent

*/

s.boot; // boot the server

// load two files for wavesets
(
a = WavesetsEvent.read(Platform.resourceDir +/+ "sounds/a11wlk01-44_1.aiff");
b = WavesetsEvent.read(Platform.resourceDir +/+ "sounds/a11wlk01.wav");
c = WavesetsEvent.read("/Volumes/data/archiv/ton/yodel1.aif");
WavesetsEvent.prepareSynthDefs;
)

// how many wavesets?
a.size;
b.size;

// play some.
// you pass an event with parameters and get back an event with parameters
a.asEvent((start: 76, end: 899)).play;
a.asEvent((start: 136, num: 99)).play;
a.asEvent((start: 136, num: 9911, rate: -1)).play; // backwards



// this sounds better in a repetition:
(
Tdef(\u, {
	inf.do { |i|
		a.asEvent((start: i * 10 % 1850, num: 39)).play;
		0.05.wait;
	}
}).play
)

// what parameters can you pass?

start: from which waveset to start
num: how many wavesets to play
end: up to which waveset to play (if given, overrides num)
repeats: how many times to repeat the selected wavesets
startTime: where to start in the file - will be rounded to the next waveset (if given, overrides start)
endTime: where to end in the file - will be rounded to the next waveset (if given, overrides start)
amp: scale the amplitude of the original sound
wsamp: normalize the amplitude to that value
rate: playback speed of the audio file
rate2: end playback speed of the audio file (will create a linear glisson sweep)
pan: stereo panorama position


// dur and legato

the waveset event also includes a \dur value (usually, dur is the inter-onset time between events). it is calculated from the actual duration of the selected wavesets. if you provide \legato, it will scale the duration, so that wavesets will overlap or have gaps between them.


// reproduce original
(
Tdef(\u, {
	inf.do { |i|
		var event = a.asEvent((start: i % a.size, num: 1, legato: 1));
		event.play;
		event[\dur].wait;
	}
}).play
)


// rate and legato
(
Tdef(\u, {
	inf.do { |i|
		var event = a.asEvent((start: i % a.size, num: 1, legato: 3/2, rate: 2/3));
		event.play;
		event[\dur].wait;
	}
}).play
)

// rate and legato
(
Tdef(\u, {
	inf.do { |i|
		var event = a.asEvent((start: i * 18 % a.size, num: 213, legato: 1, rate: 1.2, rate2: 0.8));
		event.play;
		event[\dur].wait;
	}
}).play
)

// repeats
(
Tdef(\u, {
	inf.do { |i|
		var event = a.asEvent((start: i % a.size, num: 1, legato: 1, repeats: 80, legato: 0.9));
		event.play;
		event[\dur].wait;
	}
}).play
)


// patterns
// using Pwavesets, we can use wavesets in event patterns directly

Pwavesets((start: 56, num: 78, wavesets: a)).play;

// we can add a waveset to a global dictionary
a.add(\one);
b.add(\two);


// then you can access by name:
Pwavesets((start: 56, num: 78, name: \one)).play;


(
Pwavesets(
	Pseq([
		(start: 56, num: 78, name: \one),
		(start: 123, num: 78, name: \one),
		(start: 562, num: 118, name: \one, legato: 0.5),
	], inf)
).play;
)

(
Pwavesets(
	Pbind(
		\name, \one,
		\start, Pn(Pseries(0, 2, 1000)),
		\end, Pn(Pseries(0, 2.6, 1000)),
		\repeats, 3,
		\legato, 1.0,
		\amp, 0.1,
		\pan, Pbrown(-0.5, 0.5, 0.001)
	)
).play
)


// for easy experimentation: use a Pdef
(
Pdef(\x,
	Pwavesets(
		Pbind(
			\name, \one,
			\start, Pn(Pseries(0, 2, c.size - 1)),
			\end, Pn(Pseries(0, 2.6, c.size - 1)),
			\repeats, 1,
			\legato, 1.0,
			\amp, 0.1,
			\pan, Pbrown(-0.5, 0.5, 0.001)
		)
	)
).play
)

(
Pdef(\x,
	Pwavesets(
		Pbind(
			\name, \one,
			\start, Pn(Pseries(0, 2, c.size - 1)) + 900,
			\num, Pn(Pseries(10, 0.6, 80)),
			\repeats, 9,
			\legato, 1,
			\rate, Pbrown(0.5, 2, 0.01, inf),
			\amp, 0.1,
			\pan, Pbrown(-0.5, 0.5, 0.001)
		)
	)
).play
)

