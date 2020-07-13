

Pwavesets : FilterPattern {

	embedInStream { |inevent|
		var stream = pattern.asStream;
		var event, wavesets;
		while {
			event = stream.next(inevent);
			event.notNil
		} {
			wavesets = AbstractWavesetsEvent.asEvent(event);
			inevent = wavesets.yield
		}
	}

	// these return streams that can be used in event patterns and streams

	*currentWavesets {
		^FuncStream { |inevent|
			var wavesets = inevent[\wavesets] ?? { AbstractWavesetsEvent.all.at(inevent[\name]) };
			if(wavesets.isNil) {
				Error("no wavesets found, neither in inevent[\wavesets], nor for name: '%', in inevent[\name]").throw
			};
			wavesets
		}
	}

	*currentSize {
		^FuncStream { |inevent|
			var wavesets = inevent[\wavesets] ?? { AbstractWavesetsEvent.all.at(inevent[\name]) };
			if(wavesets.isNil) {
				Error("no wavesets found, neither in inevent[\wavesets], nor for name: '%', in inevent[\name]").throw
			};
			wavesets.size
		}
	}

	*currentDuration {
		^FuncStream { |inevent|
			var wavesets = inevent[\wavesets] ?? { AbstractWavesetsEvent.all.at(inevent[\name]) };
			if(wavesets.isNil) {
				Error("no wavesets found, neither in inevent[\wavesets], nor for name: '%', in inevent[\name]").throw
			};
			wavesets.duration
		}
	}
}
