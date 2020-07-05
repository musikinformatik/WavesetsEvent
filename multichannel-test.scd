/*

experimental.

*/


s.boot;
WavesetsMultiEvent.prepareSynthDefs;
b = WavesetsMultiEvent.read("/Volumes/data/archiv/ton/count basie, bill's mill.aiff", [0, 1]);
b.add(\x);

b.asEvent((start: 800, num: 2000, wsamp: 1)).play;
b.asEvent((start: 8100, num: 200, wsamp: 1, rate: 2)).play;




(
Pdef(\x,
	Pwavesets(
		Pbind(
			\start, Pn(Pseries(0, 10, 10000)),
			\num, 10,
			\legato, 1.0,
			\name, \x,
			\amp, 0.2
		)
	)
).play;
)

(
Pdef(\x,
	Pwavesets(
		Pbind(
			\start, Pn(Pseries(0, 10, 10000)) + Pbrown(0, 1, 130, inf) + 14000,
			\num, 10,
			\rate, Pbrown(0.8, 1.3, 0.01, inf),
			\legato, Pbrown(0.8, 1.2, 0.1),
			\repeats, Pbrown(1, 4, 0.1),
			\name, \x,
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
			\wsamp, 1,
			\name, \x
		)
	)
).play;
)


(
Pdef(\x,
	Pwavesets(
		Pbind(
			\start, Pbrown(0, 88001, 30),
			\num, Pbrown(5, 140, 1),
			\legato, 1.0,
			\wsamp, 1,
			\name, \x
		)
	)
).play;
)


(
Pdef(\x,
	Pwavesets(
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

