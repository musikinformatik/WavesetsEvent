


// more elaborate GUI
(
~wavesets = ~wavesets ?? { a = WavesetsEvent.read(Platform.resourceDir +/+ "sounds/a11wlk01-44_1.aiff") };
Tdef(\u, {
	inf.do { |i|
		var event;
		event = (
			start: ~start ? 0,
			num: rrand(1, 10),
			rate: 1.2,
			repeats:1
		);
		event = ~wavesets.asEvent(event);
		event.play;
		event[\dur].wait;
	}
}).play;
)



(
var w = Window("adjust start", Rect(100, 100, 820, 50)).front;
var sl = Slider(w).orientation_(\horizontal);
var b = Button(w).states_([["open"]]);
b.action = {
	Dialog.openPanel({ |path|
		var ws = WavesetsEvent.new;
		ws.readChannel(path, onComplete: { ~wavesets = ws; ~path = path })
	})
};
sl.action = { |view| ~start = view.value.linlin(0, 1, 0, ~wavesets.size) };
sl.keyDownAction = { |view, char|
	switch(char,
		$f, { ~path.postcs },
		$ , { Tdef(\u).play },
		{ ~start.post; ",".postln }
	)
};
w.layout = HLayout(sl, b);
)



