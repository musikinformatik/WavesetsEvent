
/*

this for now just fulfills exactly the interface WavesetBuffer needs
(no more, no less)

*/



MultichannelWavesetsBuffer {
	var each;

	*new { |each|
		^super.newCopyArgs(each)
	}

	/*
	addWavesetsToEvent {
		var theseXings, startWs, endWs, numWs;
		var guidingWavesetIndex = (~guide ? 0).clip(0, each.size - 1);


		theseXings = if (~useFrac ? true) { each.collect(_.fracXings) } { each.collect(_.xings) };
		startWs = ~start ? 0;
		numWs = if(~end.notNil) { ~end - startWs } { ~num ? 1 };
		endWs = startWs + numWs;
		~startFrame = theseXings.collect(_.clipAt(startWs));
		~endFrame = theseXings.collect(_.clipAt(endWs));
		~numFrames = absdif(~endFrame, ~startFrame);
		~rate = ~rate ? 1.0;
		~sustain = ~numFrames / (buffer.sampleRate * ~rate) * (~repeats ? 1);
		~amp = if(~wsamp.isNil) { 1.0 } { ~amp =  ~wsamp / each.collect(_.maximumAmp(startWs, numWs)) };
	}
	*/



}