/*

The Trevor Markoff Ensemble presents:
 ====== MrKov goes Wavesets =======

Adaptation of Mr.Kov, an FFT-based acoustic markov-chain
https://swiki.hfbk-hamburg.de/MusicTechnology/773
systemCmd("open" + "Mr.Kov.pdf".resolveRelative);

*/

s.boot;

(
~fftSize = 128;
a = WavesetsEvent.read(Platform.resourceDir +/+ "sounds/a11wlk01.wav", minLength: 1024);
WavesetsEvent.prepareSynthDefs;
)



// this makes an efficient lookup
// the following may take a while

(
var refcount = 0;
~fftdict = ();
~maxIndex = a.size;
fork {
	"--- bitte warten ...".postln;
	(a.size-1).do { |i|
		refcount = refcount + 1;
		a.getFFT(i, 1, ~fftSize, { |c|
			~fftdict[i] = c.magnitude;
			i.postln;
		});
	};
	"-----ready------ (% entries)".format(~fftdict.size).postln;
};
);


// some functions to call
(
~askey = { |array, resolution = 0.01, size|
	~roundValues.(array, resolution, size).collect { |x| (x * 255).asInteger }.join(" ").asSymbol
};

~roundValues = { |array, resolution = 0.01, size|
	if(size.notNil) { array = array.resamp1(size) };
	array.round(resolution).normalize(0, 1)
};

~roundValuesToBinary = { |array, numDigits = 16|
	array.collect { |x|
		(x * (10 ** log2(numDigits))).asInteger.asBinaryDigits(numDigits).join("")
	}
};

// optimize a little. Experience shows, we are not using much more than 64 values.
// magnitudes are mirror images in FFT, we drop one redundant half

~fftValues = ~fftdict.asPairs[1,3..].collect { |x| x.keep(~fftSize div: 2) }.collect(~roundValues.(_, 0, 64));
//~fftValues = ~fftdict.values.collect { |x| x.keep(~fftSize div: 2) }.collect(~roundValues.(_, 0, 64));

~fftBinaryValues = ~fftValues.collect { |x| ~roundValuesToBinary.(x, 20)  };


~makeKeys = { |resolution, size|
	//~fftValues.collect(~askey.(_, resolution, size))
	var n = resolution.reciprocal.asInteger;
	var array = ~fftBinaryValues;
	if(size.notNil) { array = array.collect(_.keep(size)) }; // well, not so good, this means we take bin order
	array.collect { |x| x.collect(_.keep(n)).collect { |y| y.join }.join.asSymbol }
};

~keyList = ~makeKeys.(1/8);

~find = { |obj, list, offset|
	var i = offset;
	block { |return|
		list.size.do { |x, j|
			var index = j + i;
			if(list.wrapAt(index) === obj) { return.(index) }
		};
		return.(nil)
	}
};
~findNext = { |index|
	var obj = ~keyList.at(index);
	var randomOffset = ~keyList.size.rand;
	~find.(obj, ~keyList, randomOffset) ? index
};
);








// a few tests

a.getFFT(56, 1, ~fftSize, { |c| defer { c.magnitude.plot } })
~fftdict.at(56).plot


// test asKey
~askey.({ rrand(0, 1.0) } ! 8, 0.5)
~askey.({ rrand(0, 1.0) } ! 8, 1/4, 4)
~askey.([1, 1], 1/4, 4);

~findNext.(12);
~findNext.(12);
~findNext.(12);

~fftdict.keys.cs;
~fftdict.size;



// play back

(
Tdef(\mrkov, {
	var n = ~keyList.size;
	var i = n.rand;
	inf.do { |j|
		var e;
		var out = 2.rand;// change out for multichannel settings
		i = ~findNext.(i + 1) % n;
		e = a.asEvent(
			(
				start: i,
				num: 3,
				repeats:10,
				rate: 0.7,
				rate2: 0.8,
				latency: 0.08,
				amp: 0.81,
				wsamp: 0.5,
				out: out
			)
		).play;
		e[\dur].wait;
	}
}).play
)


(
Tdef(\mrkov, {
	var n = ~keyList.size;
	var i = n.rand;
	inf.do { |j|
		var e;
		var out = 2.rand;// change out for multichannel settings
		i = ~findNext.(i + 1) % n;
		e = a.asEvent(
			(
				start: i,
				num: 1,
				repeats:1,
				rate: 1,
				rate2: 1,
				latency: 0.08,
				amp: 0.81,
				wsamp: 0.5,
				out: out
			)
		).play;
		e[\dur].wait;
	}
}).play
)

// try different pecision levels

// original
~keyList = ~makeKeys.(1/32, 64); // resolution, size

~keyList = ~makeKeys.(1/16, 64);

~keyList = ~makeKeys.(1/16, 32);

~keyList = ~makeKeys.(1/16, 16);

~keyList = ~makeKeys.(1/16, 16);

~keyList = ~makeKeys.(1/8, 16);

~keyList = ~makeKeys.(1/8, 12);

~keyList = ~makeKeys.(1/8, 8);

~keyList = ~makeKeys.(1/8, 4);

~keyList = ~makeKeys.(1/4, 4);

~keyList = ~makeKeys.(1/4, 2);

~keyList = ~makeKeys.(1/1, 1); // ID

~keyList.size



// GUI for finding good parameter settings


(
var f1, f2, f3, p1, p2, b1, b2;
var res = 1/32, size = 32;
f1 = { |sl| res = sl.value.linlin(0, 1, 1/128, 1).round(0.001); b1.value = res; };
f2 = { |sl| size = sl.value.linlin(0, 1, 1, ~fftSize ? 128).asInteger;  b2.value = size; };
f3 = { ~keyList = ~fftValues.collect { |x| ~askey.(x, res, size)  } };
p1 = { Tdef(\mrkov).stop.play };
p2 = { "~keyList = ~makeKeys.(%, %));".format(res, size).postln };

w = Window("MrKov", Rect(300, 300, 500, 200)).front;
w.layout = VLayout(
	HLayout(Slider(w).action_(f1).mouseUpAction_(f3).orientation_(\horizontal), b1 = NumberBox().value_(res)),
	HLayout(Slider(w).action_(f2).mouseUpAction_(f3).orientation_(\horizontal), b2 = NumberBox().value_(size)),
	HLayout(
		Button(w).states_([["refresh"]]).action_(p1),
		Button(w).states_([["post"]]).action_(p2))

);
)


