/*
doesn't work yet
*/


WavesetsMultiEvent : AbstractWavesetsEvent {


	var <bufferArray, <wavesetsArray;

	*read { |path, channels = 0, startFrame = 0, numFrames = -1, onComplete, server, minLength|
		^this.new.readAllChannels(path, channels, startFrame, numFrames, onComplete, server, minLength)
	}

	readAllChannels { |path, channels = 0, startFrame = 0, numFrames = -1, onComplete, server, minLength|
		var finish, buffers, count;
		server = server ? Server.default;
		if(server.serverRunning.not) {
			"Reading WavesetsBuffer failed. Server % not running".format(server).warn;
			^this
		};
		channels = channels.asArray;
		count = channels.size;
		finish = { if(count > 1) { count = count - 1 } { this.setBufferArray(buffers, onComplete, minLength) } };

		buffers = channels.asArray.collect { |each|
			Buffer.readChannel(server ? Server.default, path, startFrame, numFrames, channels: each, action: finish)
		};
	}

	setBufferArray { |buffers, onComplete, minLength|
		var count = buffers.size;
		var finish = { if(count > 0, { count = count - 1 }, onComplete) };
		wavesetsArray = buffers.collect { |each| Wavesets2.new.fromBuffer(each, onComplete, minLength) };
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

