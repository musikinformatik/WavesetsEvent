
(
var guide = 0;
var event;
var lags, allStarts, allEnds;

event = Event.make {
~start = 1562;
~num = 1280;
};
~allEvents = ~ws.collect { |each| each.asEvent(event) };

~guideEvent = ~allEvents[guide].asEvent(event);
~startFrame = ~guideEvent[\startFrame];
~endFrame = ~guideEvent[\endFrame];

allStarts = ~ws.collect { |each, i|
	if(guide == i) {
		~startFrame
	} {
		each.wavesets.nextCrossing(~startFrame)
	};
};

allEnds = ~ws.collect { |each, i|
	if(guide == i) {
		~endFrame
	} {
		each.wavesets.prevCrossing(~endFrame)
	};
};

lags = allStarts - ~startFrame;

~allEvents.do { |x, i|
	if(guide != i) {
		x.use {
			~lag = lags[i];
			~startFrame = allStarts[i];
			~endFrame = allEnds[i];
			~sustain = ~endFrame - ~endFrame / ~buf.sampleRate;
			~busOffset = i;
			~pan = -1;
		}
	};
};
)

~allEvents.do(_.play);
