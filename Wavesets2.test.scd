



Wavesets.intoEvent((name: 'afguide-first.wav'))


Pws(Pbind(\name, 'afguide-first.wav', \start, Pseries(0, 1, 8000))).play;



Pdef(\x, Pws(Pbind(\name, 'afguide-first.wav', \start, Pbrown(0, 8000, 2) + 1000), \num, 118, \rate, 1)).play;

Pdef(\x, Pws(Pbind(\name, '000_d_maj_sitar_chorda.wav', \start, Pbrown(0, 8000, 2) + 1000), \num, 118, \rate, 1)).play;




