

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
}
