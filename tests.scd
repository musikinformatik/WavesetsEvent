
s.boot;
w = WavesetsEvent.new.readChannel(Platform.resourceDir +/+ "sounds/a11wlk01.wav");


w.plot(6, 100);


//////////////
s.boot;
WavesetsEvent.read(Platform.resourceDir +/+ "sounds/a11wlk01-44_1.aiff").add(\x);
WavesetsEvent.read(Platform.resourceDir +/+ "sounds/a11wlk01.wav").add(\y);
WavesetsEvent.prepareSynthDefs;
/////////////



WavesetsEvent.all.at(\y).buffer

WavesetsEvent.all.at(\x).makeEvent;
WavesetsEvent.all.at(\x).makeEvent(10, 800).play;
WavesetsEvent.all.at(\y).makeEvent(10, 800).play;


w = WavesetsEvent.all.at(\y);


////////////

(
Pdef(\x,
	Pwavesets(
		Pbind(
			\start, Pn(Pseries(0, 2, 1000)),
			\end, Pn(Pseries(0, 2.6, 1000)),
			\repeats, 3,
			\legato, 1.0,
			\amp, 0.5,
			\name, \x,
			\pan, Pbrown(-0.5, 0.5, 0.001)
		)
	)
).play;
)

(
Pdef(\x,
	Pwavesets(
		Pbind(
			\startTime, Pn(Pseries(0, 0.002, 10)),
			\endTime, Pn(Pseries(0.1, 0.002, 10)),
			\repeats, 3,
			\legato, 1,
			\amp, 0.5,
			\dur, 0.1,
			\name, \x,
			\pan, Pbrown(-0.5, 0.5, 0.001)
		)
	)
).play;
)

(
Pdef(\x,
	Pwavesets(
		Pbind(
			\start, Pn(Pseries(0, 20, 100)),
			\num, 127,
			\repeats, 2,
			\legato, 1,
			\wsamp, 0.1,
			\name, Prand([\y], inf)
		)
	)
).play;
)






(
Pdef(\x,
	Pwavesets(
		Pbind(
			\start, Pbrown(0, WavesetsEvent.all.at(\y).wavesets.numXings, 30),
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


(
Pdef(\x,
	Pwavesets(
		Pbind(
			\start, Pseries(0, 50, 300).loop,
			\num, Pseries(200, -5, 20).loop,
			\rate, 0.9,
			\rate2, 1.1,
			\wsamp, 1,
			\legato, 1.5,
			\name, \x
		)
	)
).play;
)



