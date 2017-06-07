*read { |path, server|
		var signal, buffer;
		signal = SoundFileToSignal.read(path);
		signal !? { this.signal = signal; };
	}

	readChannel { |path, server, channel = 0, startFrame = 0, numFrames = -1|
		var signal, buffer;
		if(server.serverRunning.not) {
			"Reading Wavesets failed. Server % not running".format(server).postln;
			^this
		};
		signal = SoundFileToSignal.read(path);
		signal !? { this.signal = signal; };
	}

	*read { |path|
		^this.new.read(path)
	}

SoundFileToSignal {

	*int8 { ^Int16Array }
	*int16 { ^Int16Array }
	*mulaw { ^Int16Array }
	*alaw { ^Int16Array }

	*int24 { ^Int32Array }
	*int32 { ^Int32Array }
	*float { ^Signal }

	*readData { |soundfile|
		var signal = this.perform(soundfile.sampleFormat.asSymbol).newClear(soundfile.numFrames);
		soundfile.readData(signal);
		^signal
	}

	*read { |path|
		var soundfile, signal, ok;
		soundfile = SoundFile.new;
		ok = soundfile.openRead(path);
		if (ok.not) { ("SoundFile:" + path + "not found.").warn; ^nil };
		protect { signal = this.readData(soundfile) } { soundfile.close };
		^signal
	}
}

