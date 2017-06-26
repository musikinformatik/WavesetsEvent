
/*

todo: global cache like in the original Wavesets

*/


AbstractWavesetsEvent {
	classvar <all;

	*initClass {
		all = IdentityDictionary.new
	}

	add { |name|
		var old = all.at(name);
		old.free;
		all.put(name, this)
	}

	isReady {
		^this.subclassResponsibility(thisMethod)
	}

	asEvent { |inevent|
		inevent = inevent ?? { () };
		inevent = inevent.copy;
		inevent.use { this.addToEvent };
		^inevent
	}

	addToEvent {
		this.addBuffersToEvent;
		this.addWavesetsToEvent;
		this.finalizeEvent;
	}

	*asEvent { |inevent|
		var wavesets, name;
		wavesets = inevent.at(\wavesets);
		wavesets !? { ^wavesets.asEvent(inevent) };
		wavesets = all.at(inevent.at(\name));
		if(wavesets.isNil) { "no wavesets with this name: %".format(inevent.at(\name)).warn; ^nil };
		if(wavesets.isReady.not) { "wavesets not initialised: %".format(inevent.at(\name)).warn; ^nil };
		^wavesets.asEvent(inevent)
	}

	makeEvent { |start=0, num, end, rate=1, rate2, legato, wsamp, useFrac|
		var event = (
			start: start,
			end: end, num: num,
			rate: rate,
			rate2: rate2,
			legato: legato,
			wsamp: wsamp,
			useFrac:useFrac
		);
		^this.asEvent(event);
	}


	// backwards compatibility
	eventFor { |startWs=0, numWs=5, repeats=3, playRate=1, useFrac = true|
		^this.asEvent((start: startWs, length: numWs, repeats: repeats, rate: playRate, useFrac: useFrac))
	}

	toBuffer { |buffer, onComplete|
		^this.shouldNotImplement(thisMethod)
	}



}



WavesetsEvent : AbstractWavesetsEvent {


	var <buffer, <wavesets;

	*read { |path, channel = 0, startFrame = 0, numFrames = -1, onComplete, server|
		^this.new.readChannel(path, channel, startFrame, numFrames, onComplete, server)
	}

	readChannel { |path, channel = 0, startFrame = 0, numFrames = -1, onComplete, server|
		var finish, buffer;
		server = server ? Server.default;
		if(server.serverRunning.not) {
			"Reading WavesetsBuffer failed. Server % not running".format(server).warn;
			^this
		};
		finish = { this.setBuffer(buffer, onComplete) };
		buffer = Buffer.readChannel(server ? Server.default, path, startFrame, numFrames, channels: channel.asArray, action: finish);
	}

	setBuffer { |argBuffer, onComplete|
		wavesets = Wavesets2.fromBuffer(argBuffer, onComplete);
		buffer = argBuffer;
	}

	free {
		buffer.free;
		wavesets = nil;
	}

	size { ^wavesets.size }

	server { ^buffer.server }

	isReady { ^wavesets.notNil and: { buffer.notNil } }

	asBuffer { |server, onComplete|
		if(server != buffer.server) { Error("can't copy waveset to another server").throw };
		^buffer
	}

	addBuffersToEvent {
		~buf = buffer.bufnum;
		~sampleRate = buffer.sampleRate;
	}

	addWavesetsToEvent {
		var theseXings, startWs, useFrac;
		useFrac = ~useFrac ? true;
		theseXings = if (useFrac) { wavesets.fracXings } { wavesets.xings };
		~startTime !? { ~start = wavesets.nextCrossingIndex(~startTime * ~sampleRate, useFrac) };
		~endTime !? { ~end = wavesets.nextCrossingIndex(~endTime * ~sampleRate, useFrac) };
		startWs = ~start ? 0;
		~num = if(~end.notNil) { ~end - startWs } { ~num ? 1 };
		~startFrame = theseXings.clipAt(startWs);
		~endFrame = theseXings.clipAt(startWs + ~num);
		~numFrames = ~endFrame - ~startFrame;
		if(~wsamp.notNil) { ~amp =  ~wsamp / wavesets.maximumAmp(~start, ~num) };
	}

	finalizeEvent {
		var timeScale, reverse;
		//reverse = ~end < ~start;
		currentEnvironment.useWithoutParents {
			~rate = ~rate ? 1.0;
			if(~rate2.notNil) {
				timeScale = ~rate + ~rate2 * 0.5;
				~instrument = ~instrument ? \wvst1gl;
			} {
				timeScale = ~rate;
				~instrument = ~instrument ? \wvst0;
			};
			~rate2 = ~rate2 ? 1.0;

			~sustain = ~sustain ?? {
				abs(~numFrames * (~repeats ? 1) / (~sampleRate * timeScale))
			};

			~dur ?? {
				~dur = if(~legato.isNil) { ~sustain } { ~sustain / ~legato };
				if(~dur.isArray) { ~dur = ~dur[0] };
				if(~dur < 0.0001) { ~type = \rest }; // this is ad hoc
			}
		};

	}




	plot { |index = 0, length = 1|
		^wavesets.plot(index, length, buffer.sampleRate)
	}

	// equality

	== { |that|
		^this.compareObject(that, #[\wavesets, \buffer])
	}

	hash {
		^this.instVarHash(#[\wavesets, \buffer])
	}


	*prepareSynthDefs {

		SynthDef(\wvst0, { | out = 0, buf = 0, startFrame = 0, numFrames = 441, rate = 1, sustain = 1, amp = 0.1, pan, interpolation = 2 |
			var phasor = Phasor.ar(0, BufRateScale.ir(buf) * rate * sign(numFrames), 0, abs(numFrames)) + startFrame;
			var env = EnvGen.ar(Env([amp, amp, 0], [sustain, 0]), doneAction: 2);
			var snd = BufRd.ar(1, buf, phasor, 1, interpolation) * env;

			OffsetOut.ar(out, Pan2.ar(snd, pan));
		}, \ir.dup(9)).add;

		SynthDef(\wvst1gl, { | out = 0, buf = 0, startFrame = 0, numFrames = 441, rate = 1, rate2 = 1, sustain = 1,
			amp = 0.1, pan, interpolation = 2 |
			var rateEnv = Line.ar(rate, rate2, sustain) * sign(numFrames);
			var phasor = Phasor.ar(0, BufRateScale.ir(buf) * rateEnv, 0, abs(numFrames)) + startFrame;
			var env = EnvGen.ar(Env([amp, amp, 0], [sustain, 0]), doneAction: 2);
			var snd = BufRd.ar(1, buf, phasor, 1, interpolation) * env;

			OffsetOut.ar(out, Pan2.ar(snd, pan));
		}, \ir.dup(10)).add;

	}



}

/*
doesn't work yet
*/


WavesetsMultiEvent : AbstractWavesetsEvent {


	var <bufferArray, <wavesetsArray;

	*read { |path, channels = 0, startFrame = 0, numFrames = -1, onComplete, server|
		^this.new.readAllChannels(path, channels, startFrame, numFrames, onComplete, server)
	}

	readAllChannels { |path, channels = 0, startFrame = 0, numFrames = -1, onComplete, server|
		var finish, buffers, count;
		server = server ? Server.default;
		if(server.serverRunning.not) {
			"Reading WavesetsBuffer failed. Server % not running".format(server).warn;
			^this
		};
		channels = channels.asArray;
		count = channels.size;
		finish = { if(count > 1) { count = count - 1 } { this.setBufferArray(buffers, onComplete) } };

		buffers = channels.asArray.collect { |each|
			Buffer.readChannel(server ? Server.default, path, startFrame, numFrames, channels: each, action: finish)
		};
	}

	setBufferArray { |buffers, onComplete|
		var count = buffers.size;
		var finish = { if(count > 0, { count = count - 1 }, onComplete) };
		wavesetsArray = buffers.collect { |each| Wavesets2.new.fromBuffer(each, onComplete) };
		bufferArray = buffers;
	}

	isReady { ^wavesetsArray.notNil and: { bufferArray.notNil } }

	addToEvent {
		var startWs, theseXings;
		var lastIndex = wavesetsArray.size - 1;
		var guide = (~guide? 0).clip(0, lastIndex);
		var guideWavesets = wavesetsArray[guide];
		var useFrac = ~useFrac ? true;

		~buf = bufferArray;
		~sampleRate = bufferArray[0].sampleRate;


		theseXings = if (useFrac) { guideWavesets.fracXings } { guideWavesets.xings };
		~startTime !? { ~start = guideWavesets.nextCrossingIndex(~startTime * ~sampleRate, useFrac) };
		~endTime !? { ~end = guideWavesets.nextCrossingIndex(~endTime * ~sampleRate, useFrac) };

		startWs = ~start ? 0;

		~num = if(~end.notNil) { ~end - startWs } { ~num ? 1 };
		~startFrame = theseXings.clipAt(startWs);
		~endFrame = theseXings.clipAt(startWs + ~num);
		~numFrames = absdif(~endFrame, ~startFrame);
		~amp = if(~wsamp.isNil) { 1.0 } { ~amp =  ~wsamp / guideWavesets.maximumAmp(~start, ~num) };


		~allStarts = wavesetsArray.collect { |each, i|
			if(guide == i) {
				~startFrame
			} {
				each.nextCrossing(~startFrame, useFrac)
			};
		};

		~allEnds = wavesetsArray.collect { |each, i|
			if(guide == i) {
				~endFrame
			} {
				each.prevCrossing(~endFrame, useFrac)
			};
		};

		~sampleDur = bufferArray[guide].sampleRate.reciprocal;
		~lag = ~allStarts - ~startFrame * ~sampleDur;
		~rate = ~rate ? 1.0;

		~startFrame = ~allStarts;
		~endFrame = ~allEnds;
		~sustain = (~endFrame - ~startFrame) * ~sampleDur * (~repeats ? 1);

		currentEnvironment.useWithoutParents {
			~legato !? {
				~dur = ~sustain[guide] / ~legato;
				if(~dur < 0.0001) { ~type = \rest }; // this is ad hoc
			};
		};

		~busOffset = (0..lastIndex);
		~instrument = if(~rate2.notNil) { \wvst1glmulti } { \wvst0multi }

	}


	// equality

	== { |that|
		^this.compareObject(that, #[\wavesetArray, \bufferArray])
	}

	hash {
		^this.instVarHash(#[\wavesetArray, \bufferArray])
	}


	*prepareSynthDefs {

		SynthDef(\wvst0multi, { | out = 0, buf = 0, startFrame = 0, numFrames = 441, rate = 1, sustain = 1, amp = 0.1, interpolation = 2, busOffset = 0 |
			var phasor = Phasor.ar(0, BufRateScale.ir(buf) * rate, 0, numFrames) + startFrame;
			var env = EnvGen.ar(Env([amp, amp, 0], [sustain, 0]), doneAction: 2);
			var snd = BufRd.ar(1, buf, phasor, 1, interpolation) * env;

			OffsetOut.ar(out + busOffset, snd);
		}, \ir.dup(9)).add;

		SynthDef(\wvst1glmulti, { | out = 0, buf = 0, startFrame = 0, numFrames = 441, rate = 1, rate2 = 1, sustain = 1,
			amp = 0.1, interpolation = 2, busOffset = 0 |
			var rateEnv = Line.ar(rate, rate2, sustain);
			var phasor = Phasor.ar(0, BufRateScale.ir(buf) * rateEnv, 0, numFrames) + startFrame;
			var env = EnvGen.ar(Env([amp, amp, 0], [sustain, 0]), doneAction: 2);
			var snd = BufRd.ar(1, buf, phasor, 1, interpolation) * env;

			OffsetOut.ar(out + busOffset, snd);
		}, \ir.dup(10)).add;

	}
}


