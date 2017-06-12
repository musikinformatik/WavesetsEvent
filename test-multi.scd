
s.boot;

(
WavesetsBuffer.prepareSynthDefs;

~ws = [0, 1].collect { |channel|
	WavesetsBuffer.read("/Volumes/data/archiv/ton/count basie, bill's mill.aiff", channel)
}
);



(
x = WavesetsBuffer.asMultiEvent(~ws, Event.make {
	~start = 1562;
	~num = 1280;
})
)

x.do(_.play);
x[0].cs


(
Tdef(\x, {
	inf.do { |i|
		var event = Event.make {
			~start = i + 400.rand;
			~num = 8;
			~guide = 0;
			~wsamp = 0.4;
		};
		var events = WavesetsBuffer.asMultiEvent(~ws, event);
		var dur = events[0][\sustain];
		events.do(_.play);
		dur.wait;
	}
}).play
)
