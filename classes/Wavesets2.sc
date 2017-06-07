

Wavesets2 {

	var <signal;

	var	<xings, <lengths, <fracXings, <fracLengths;
	var <amps, <maxima, <minima;
	var <minSet, <maxSet, <avgLength, <sqrAvgLength;
	var <minAmp, <maxAmp, <avgAmp, <sqrAvgAmp;


	signal_ { |sig|
		signal = sig;
		this.analyse;
	}

	numFrames { ^signal.size }
	numXings { ^xings.size }

	fromBuffer { |buffer, onComplete|
		buffer.loadToFloatArray(0, -1, { |array|
			this.signal_(array);
			onComplete.value(this);
		})
	}

	toBuffer { |buffer, onComplete|
		buffer.loadCollection(signal.asArray, action: onComplete);
	}

	asBuffer { |server, onComplete|
		^Buffer.loadCollection(server, signal.asArray, numChannels: 1, action: onComplete)
	}

	frameFor { arg startWs, numWs = 1, useFrac = true;

		var theseXings = if (useFrac) { fracXings } { xings };
		var startFrame = theseXings.clipAt(startWs);
		var endFrame = theseXings.clipAt(startWs + numWs);
		var length = absdif(endFrame, startFrame);

		^[startFrame, length]
	}

	maximumAmp { |index, length=1|
		var maxItem = 0;
		length.do { |i| maxItem = max(maxItem, amps[index + i] ? 0) };
		^maxItem
	}

	sampleRate {
		^Server.default.options.sampleRate
	}

	plot { |index = 0, length = 1|
		var data = this.frameFor(index, length, false).postln;
		var segment = signal.copyRange(data[0], data[0] + data[1] - 1);
		var peak = max(segment.maxItem, segment.minItem.abs);
		var sustain = (data[1] - data[0] / this.sampleRate).round(0.000001);
		segment.plot(
			"Waveset: index %, length %, sustain %".format(index, length, sustain),
			minval: peak.neg,
			maxval: peak
		)
	}



	// the interesting bit

	analyse {
		//	var chunkSize = 400, pause = 0.01;	// not used yet
		xings = Array.new;
		amps = Array.new;
		lengths = Array.new;
		fracXings = Array.new;
		maxima = Array.new; 	// indices of possible turnaround points
		minima = Array.new; 	//
		"%: Analysing ...".format(this.class).inform;

		this.analyseFromTo;
		this.calcAverages;
		"\t ... done. (% xings)".format(xings.size).inform;
	}

	analyseFromTo { |startFrame = 0, endFrame, minLength = 10| 	// minLength reasonable? => 4.4 kHz maxFreq.

		var lengthCount = 0, prevSample = 0.0;
		var maxSamp = 0.0, minSamp = 0.0;
		var maxAmpIndex, minAmpIndex, wavesetAmp, frac;

		// find xings, store indices, lengths, and amps.

		startFrame = max(0, startFrame);
		endFrame = (endFrame ? signal.size - 1).min(signal.size - 1);

		(startFrame to: endFrame).do { |i|
			var thisSample;
			thisSample = signal.at(i);

			// if Xing from non-positive to positive:
			if(
				(prevSample <= 0.0) and:
				{ thisSample  > 0.0 } and:
				{ lengthCount >= minLength }
			) {

				if(xings.notEmpty) {
					// if we already have a first waveset,
					// keep results from analysis:
					wavesetAmp = max(maxSamp, minSamp.abs);
					amps = amps.add(wavesetAmp);
					lengths = lengths.add(lengthCount);
					maxima = maxima.add(maxAmpIndex);
					minima = minima.add(minAmpIndex);
				};

				xings = xings.add(i);

				// lin interpol for fractional crossings
				frac = prevSample / (prevSample - thisSample);
				fracXings = fracXings.add( i - 1 + frac );

				// reset vars for next waveset
				maxSamp = 0.0;
				minSamp = 0.0;
				lengthCount = 0;
			};

			lengthCount = lengthCount + 1;
			if(thisSample > maxSamp) { maxSamp = thisSample; maxAmpIndex = i };
			if(thisSample < minSamp) { minSamp = thisSample; minAmpIndex = i };
			prevSample = thisSample;
		};
	}

	// basic statistics
	calcAverages {
		// calculate maxAmp, minAmp, avgAmp, sqAvgAmp;
		// and maxSet, minSet, avgLength, sqAvgLength;

		var numXings = xings.size;

		minSet = lengths.minItem;
		maxSet = lengths.maxItem;
		minAmp = amps.minItem;
		maxAmp = amps.maxItem;

		if(numXings > 0) {
			fracLengths = fracXings.drop(1) - fracXings.drop(-1);

			avgLength = xings.last - xings.first / numXings;
			sqrAvgLength = (lengths.squared.sum / ( numXings - 1)).sqrt;

			avgAmp = amps.sum / numXings;
			sqrAvgAmp = (amps.squared.sum / numXings).sqrt;
		};
	}

	// peak detection on amplitudes
	// http://www.tcs-trddc.com/trddc_website/pdf/SRL/Palshikar_SAPDTS_2009.pdf

	peakAmpIdxs { |windowSize = 4, h = 3, s1|
		// 1 <= h <= 3

		var mean, sDev, aAbs, peaksSize;
		var rWindowSize = windowSize.reciprocal;
		var peakIdxs; //return value
		var idxs = List[];
		var a;

		// peak estimation function
		if(s1.isNil) {
			s1 = {|left, item, right, rContextSize|
				((maxItem(item - left) ? 0) + (maxItem(item - right) ? 0)) * 0.5
			};
		};


		a = amps.collect { |val, i|
			// compute peak function value for each of the N points in T
			s1.value(
				amps.copyRange(i-windowSize, i-1),
				val,
				amps.copyRange(i+1, i + windowSize),
				val
			);
		};
		// Compute mean, standard deviation of all positive values
		aAbs = a.abs;
		mean = aAbs.mean;
		sDev = aAbs.stdDev;

		a.do { |val, i| // collect all indices that are concidered big
			if ((a[i] > 0) and: { (a[i]-mean) > (h*sDev) }) { idxs.add(i) }
		};
		peaksSize = idxs.size.postln;

		// retain only one peak of any set of peaks within windowSize of each other
		peakIdxs = idxs.inject([0], { |last, now, i|
			if((now - last.last) <= windowSize) {
				if(amps[now] > amps[last.last]) {
					last.pop;
					last ++ now
				} {
					last
				}
			} {
				last ++ now
			}
		});

		^peakIdxs
	}


	== { |that|
		^this.compareObject(that, #[\signal])
	}

	hash {
		^this.instVarHash(#[\signal])
	}


}

/*

todo: global cache like in the original Wavesets

*/


WavesetsBuffer : Wavesets2 {
	classvar <all;

	var <buffer;

	*initClass {
		all = IdentityDictionary.new
	}

	add { |name|
		var old = all.at(name);
		old.free;
		all.put(name, this)
	}

	*read { |path, channel = 0, startFrame = 0, numFrames = -1, onComplete, server|
		^this.new.readChannel(path, channel, startFrame, numFrames, onComplete, server)
	}

	readChannel { |path, channel = 0, startFrame = 0, numFrames = -1, onComplete, server|
		var signal, finish, buffer;
		server = server ? Server.default;
		if(server.serverRunning.not) {
			"Reading WavesetsBuffer failed. Server % not running".format(server).warn;
			^this
		};
		finish = { this.setBuffer(buffer, onComplete) };
		buffer = Buffer.readChannel(server ? Server.default, path, startFrame, numFrames, channels: channel.asArray.keep(1), action: finish);
	}

	fromBuffer { |buffer, onComplete|
		this.setBuffer(buffer, onComplete)
	}

	setBuffer { |argBuffer, onComplete|
		super.fromBuffer(argBuffer, onComplete);
		buffer = argBuffer;
	}

	free {
		buffer.free;
		signal = nil;
	}

	sampleRate { ^buffer.sampleRate }


	*asEvent { |inevent|
		var wavesets = all.at(inevent.at(\name));
		if(wavesets.isNil) { "no wavesets with this name: %".format(wavesets).warn; ^nil };
		^wavesets.asEvent(inevent)
	}

	asEvent { |inevent|
		inevent = inevent ?? { () };
		inevent = inevent.copy;
		inevent.use {

			var startFrame, numFrames, sustain1;
			var startWs = ~start ? 0;
			var numWs = if(~end.notNil) { ~end - startWs } { ~num ? 1 };

			numWs = max(numWs.asInteger, 1);
			startWs = startWs.asInteger;

			#startFrame, numFrames = this.frameFor(startWs, numWs, ~useFrac ? true);
			sustain1 = numFrames / buffer.sampleRate;

			~startFrame = startFrame;
			~numFrames = numFrames;
			~amp = if(~wsamp.isNil) { 1.0 } { ~amp =  ~wsamp / this.maximumAmp(startWs, numWs) };
			~rate = ~rate ? 1.0;

			~sustain = sustain1 / ~rate * (~repeats ? 1);
			~legato !? {
				~dur = ~sustain / ~legato;
				if(~dur < 0.0001) { ~type = \rest };
			};
			~buf = buffer;
			~instrument = if(~rate2.notNil) { \wvst1gl } { \wvst0 };
		};
		^inevent
	}

	// backwards compatibility
	eventFor { |startWs=0, numWs=5, repeats=3, playRate=1, useFrac = true|
		^this.asEvent((start: startWs, length: numWs, repeats: repeats, rate: playRate, useFrac: useFrac))
	}

	== { |that|
		^this.compareObject(that, #[\signal, \buffer])
	}

	hash {
		^this.instVarHash(#[\signal, \buffer])
	}


	*prepareSynthDefs {

		SynthDef(\wvst0, { | out = 0, buf = 0, startFrame = 0, numFrames = 441, rate = 1, sustain = 1, amp = 0.1, pan, interpolation = 2 |
			var phasor = Phasor.ar(0, BufRateScale.ir(buf) * rate, 0, numFrames) + startFrame;
			var env = EnvGen.ar(Env([amp, amp, 0], [sustain, 0]), doneAction: 2);
			var snd = BufRd.ar(1, buf, phasor, 1, interpolation) * env;

			OffsetOut.ar(out, Pan2.ar(snd, pan));
		}, \ir.dup(8)).add;

		SynthDef(\wvst1gl, { | out = 0, buf = 0, startFrame = 0, numFrames = 441, rate = 1, rate2 = 1, sustain = 1,
			amp = 0.1, pan, interpolation = 2 |
			var rateEnv = Line.ar(rate, rate2, sustain);
			var phasor = Phasor.ar(0, BufRateScale.ir(buf) * rateEnv, 0, numFrames) + startFrame;
			var env = EnvGen.ar(Env([amp, amp, 0], [sustain, 0]), doneAction: 2);
			var snd = BufRd.ar(1, buf, phasor, 1, interpolation) * env;

			OffsetOut.ar(out, Pan2.ar(snd, pan));
		}, \ir.dup(8)).add;

	}



}





