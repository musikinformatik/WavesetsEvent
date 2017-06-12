WavesetsEvent.prepareSynthDefs;

a = WavesetsEvent.read("/Volumes/data/archiv/ton/count basie, bill's mill.aiff");

a.asEvent((start: 800, num: 1000, wsamp: 1)).play;


s.boot;
WavesetsMultiEvent.prepareSynthDefs;
b = WavesetsMultiEvent.read("/Volumes/data/archiv/ton/count basie, bill's mill.aiff", [0, 1]);

b.asEvent((start: 800, num: 1000, wsamp: 1)).play;

b.add(\x);

(
Pdef(\x,
	Pws(
		Pbind(
			\start, Pn(Pseries(0, 2, 1000)),
			\num, 7,
			\legato, 1.0,
			\wsamp, 1,
			\name, \x
		)
	)
).play;
)


(
Pdef(\x,
	Pws(
		Pbind(
			\start, Pbrown(0, WavesetsBuffer.all.at(\y).wavesets.numXings, 30),
			\num, Pbrown(5, 40, 1),
			\legato, 1.0,
			\wsamp, 1,
			\name, \y
		)
	)
).play;
)


(
Pdef(\x,
	Pws(
		Pbind(
			\start, Pseries(0, 30, 100),
			\num, 800,
			\rate, 0.5,
			\rate2, 2,
			\wsamp, 1,
			\legato, 8.5,
			\name, \x
		)
	)
).play;
)

