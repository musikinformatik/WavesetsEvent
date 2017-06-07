
s.boot;
w = WavesetsBuffer.new.readChannel("/Volumes/data/archiv/ton/count.aiff");


w.plot(6, 100);

s.boot;
WavesetsBuffer.read("/Volumes/data/archiv/ton/count.aiff").add(\x);
WavesetsBuffer.read("/Volumes/data/archiv/ton/Rasierer/Rasierer Aufnahmen Athen 2016/STE-002.wav").add(\y);

WavesetsBuffer.prepareSynthDefs;

WavesetsBuffer.all.at(\y).buffer


readChannel

(
Pdef(\x,
	Pws(
		Pbind(
			\start, Pseries(0, 30, 100),
			\num, 200,
			\legato, 1.5,
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

