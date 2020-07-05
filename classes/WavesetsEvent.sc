
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

	*findWavesets { |inevent|
		^inevent.at(\wavesets) ?? { all.at(inevent.at(\name)) }
	}

	*asEvent { |inevent|
		var wavesets;
		wavesets = inevent.at(\wavesets);
		wavesets !? {
			^wavesets.asEvent(inevent)
		};
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

	*read { |path, channel = 0, startFrame = 0, numFrames = -1, onComplete, server, minLength|
		^this.new.readChannel(path, channel, startFrame, numFrames, onComplete, server, minLength)
	}

	read { |path, channel = 0, startFrame = 0, numFrames = -1, onComplete, server, minLength|
		^this.readChannel(path, channel, startFrame, numFrames, onComplete, server, minLength)
	}

	readChannel { |path, channel = 0, startFrame = 0, numFrames = -1, onComplete, server, minLength|
		var finish, buffer;
		server = server ? Server.default;
		if(server.serverRunning.not) {
			"Reading WavesetsBuffer failed. Server % not running".format(server).warn;
			^this
		};
		finish = { this.setBuffer(buffer, onComplete, minLength) };
		buffer = Buffer.readChannel(server ? Server.default, path, startFrame, numFrames, channels: channel.asArray, action: finish);
	}

	setBuffer { |argBuffer, onComplete, minLength|
		wavesets = Wavesets2.fromBuffer(argBuffer, onComplete, minLength);
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
		~num = if(~end.notNil) { max(~end - startWs, 1) } { ~num ? 1 };
		~startFrame = theseXings.clipAt(startWs);
		~endFrame = theseXings.clipAt(startWs + ~num);
		~numFrames = ~endFrame - ~startFrame;
		if(~wsamp.notNil) { ~amp =  ~wsamp / wavesets.maximumAmp(~start, ~num) };
	}

	embedNothingToEvent {
		~type = \rest;
		~dur = 1.0;
		~sustain = 0;
		"start or end of wavesets out of bounds:\n%\n".postf(currentEnvironment);
	}

	finalizeEvent {
		var averagePlaybackRate, reverse;
		currentEnvironment.useWithoutParents {
			if(~numFrames <= 0) {
				this.embedNothingToEvent
			} {
				~rate = ~rate ? 1.0;
				if(~rate2.notNil) {
					averagePlaybackRate = ~rate + ~rate2 * 0.5;
					~instrument = ~instrument ? \wvst1gl;
				} {
					averagePlaybackRate = ~rate;
					~instrument = ~instrument ? \wvst0;
				};
				~rate2 = ~rate2 ? 1.0;
				~sustain = ~sustain ?? {
					abs(~numFrames * (~repeats ? 1).floor.max(1) / (~sampleRate * averagePlaybackRate))
				};  // todo: if sustain is given, find next crossings

				~dur ?? {
					~dur = if(~legato.isNil) { ~sustain } { ~sustain / ~legato };
					if(~dur.isArray) { ~dur = ~dur[0] };
					if(~dur < 0.0001) { ~type = \rest }; // this is ad hoc
				}
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

	// fft
	// note that if you send too many requests, the corresponding action may not be called
	// this is because of the getn implementation, which has no id

	getFFT { |index, num, size, action|

		var real, imag, cosTable, complex;
		var start, numFrames, func;
		#start, numFrames = this.wavesets.frameFor(index, num, false);
		size = size ?? { numFrames.nextPowerOfTwo };
		func = { |arr|

			if(arr.size != size) { arr = arr.resamp1(size) };
			real = arr.as(Signal);
			imag = Signal.newClear(size);
			cosTable = Signal.fftCosTable(size);


			complex = fft(real, imag, cosTable);

			action.value(complex, real, imag);
		};

		if(buffer.server.isLocal) {
			this.buffer.loadToFloatArray(start, numFrames, func)
		} {
			this.buffer.getToFloatArray(start, numFrames, 0.01, 3, func)
		};
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


