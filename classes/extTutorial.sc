+ WavesetsEvent {

	*tutorial {
		Document.open(PathName(this.filenameSymbol.asString).navigateUp(1) +/+ "intro.scd")
	}
}

+ PathName {

	navigateUp { |levels = 1|
		^this.asAbsolutePath[..this.colonIndices.drop(levels.neg).last]
	}

}

