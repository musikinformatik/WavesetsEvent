
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
		// should we copy buffer?
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

	server { ^buffer.server }

	toBuffer { |buffer, onComplete|
		^this.shouldNotImplement(thisMethod)
	}

	asBuffer { |server, onComplete|
		if(server != buffer.server) { Error("can't copy waveset to another server").throw };
		^buffer
	}


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

	makeEvent { |start=0, num, end, rate=1, rate2, legato, wsamp, useFrac|
		^this.asEvent((start: start, end: end, num: num, rate: rate, rate2: rate2, legato: legato, wsamp: wsamp, useFrac:useFrac))
	}

	// backwards compatibility
	eventFor { |startWs=0, numWs=5, repeats=3, playRate=1, useFrac = true|
		^this.asEvent((start: startWs, length: numWs, repeats: repeats, rate: playRate, useFrac: useFrac))
	}

	// identity

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


