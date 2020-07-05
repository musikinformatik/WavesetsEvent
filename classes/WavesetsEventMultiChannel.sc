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

	// temp.
	addToEvent {
		this.addBuffersToEvent;
		this.addWavesetsToEvent;
		this.finalizeEvent;
	}


	addBuffersToEvent {
		~buf = bufferArray.collect { |x| x.bufnum };
		~sampleRate = bufferArray[0].sampleRate;
	}

	// this can be refactored eventually by passing the wavesets as an argument
	addWavesetsToEvent {
		var startWs, theseXings;
		var lastIndex = wavesetsArray.size - 1;
		var guideWavesets;

		~guide = (~guide ? 0).clip(0, lastIndex);
		guideWavesets = wavesetsArray[~guide];
		~busOffset = (0..lastIndex);


		// from here on it is the same as in the mono version, just using guideWavesets
		~useFrac = ~useFrac ? true;
		theseXings = if (~useFrac) { guideWavesets.fracXings } { guideWavesets.xings };


		~startTime !? { ~start = guideWavesets.nextCrossingIndex(~startTime * ~sampleRate, ~useFrac) };
		~endTime !? { ~end = guideWavesets.nextCrossingIndex(~endTime * ~sampleRate, ~useFrac) };
		startWs = ~start ? 0;

		~num = if(~end.notNil) { ~end - startWs } { ~num ? 1 };
		~startFrame = theseXings.clipAt(startWs);
		~endFrame = theseXings.clipAt(startWs + ~num);
		~numFrames = absdif(~endFrame, ~startFrame);
		if(~wsamp.notNil) { ~amp =  ~wsamp / guideWavesets.maximumAmp(~start, ~num) };

	}


	finalizeEvent {
		var averagePlaybackRate, reverse;

		currentEnvironment.useWithoutParents {
			//if(~numFrames <= 0) { // this is an array here
			if(false) {
				this.embedNothingToEvent
			} {
				// this is almost the same as in superclass
				~rate = ~rate ? 1.0;

				if(~rate2.notNil) {
					averagePlaybackRate = ~rate + ~rate2 * 0.5;
					~instrument = ~instrument ? \wvst1glmulti;
				} {
					averagePlaybackRate = ~rate;
					~instrument = ~instrument ? \wvst0multi;
				};
				~rate2 = ~rate2 ? 1.0;


				// here the difference starts


				~allStarts = wavesetsArray.collect { |each, i|
					if(~guide == i) {
						~startFrame
					} {
						each.nextCrossing(~startFrame, ~useFrac)
					};
				};

				~allEnds = wavesetsArray.collect { |each, i|
					if(~guide == i) {
						~endFrame
					} {
						// this may lead to reversals, one would think,
						// but it works better than looking for the next crossing.
						//each.prevCrossing(~endFrame, ~useFrac)
						each.nextCrossing(~endFrame, ~useFrac)
					};
				};

				~secondsPerFrame = reciprocal(~sampleRate * averagePlaybackRate);

				// lag is built into the standard note event
				// it is a delay of the onset in seconds
				// we delay the onsets so the channels match up
				~lag = (~allStarts - ~startFrame) * ~secondsPerFrame;

				// these are now multichannel
				~startFrame = ~allStarts;
				~endFrame = ~allEnds;
				~numFrames = ~endFrame - ~startFrame;


				~sustain = ~sustain ?? {
					~numFrames * (~repeats ? 1) * ~secondsPerFrame
				};

				~dur ?? {
					~dur = if(~legato.isNil) { ~sustain } { ~sustain / ~legato };
					if(~dur.isArray) { ~dur = ~dur[~guide ? 0] };
					if(~dur < 0.0001) { ~type = \rest }; // this is ad hoc
				};


			}
		};

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

