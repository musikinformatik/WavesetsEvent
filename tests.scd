
s.boot;
w = WavesetsBuffer.new.readChannel(Platform.resourceDir +/+ "sounds/a11wlk01.wav");


w.plot(6, 100);

s.boot;
WavesetsBuffer.read(Platform.resourceDir +/+ "sounds/a11wlk01-44_1.aiff").add(\x);
WavesetsBuffer.read(Platform.resourceDir +/+ "sounds/a11wlk01.wav").add(\y);

WavesetsBuffer.prepareSynthDefs;

WavesetsBuffer.all.at(\y).buffer

WavesetsBuffer.all.at(\x).makeEvent;
WavesetsBuffer.all.at(\x).makeEvent(10, 800).play;
WavesetsBuffer.all.at(\y).makeEvent(10, 800).play;





w = WavesetsBuffer.all.at(\y);

w.nextCrossing(891)
w.nearestCrossing(891)

(
Pdef(\x,
	Pws(
		Pbind(
			\start, Pn(Pseries(0, 2, 1000)),
			\num, 7,
			\legato, 1.0,
			\wsamp, 1,
			\name, Prand([\x, \y], inf)
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

