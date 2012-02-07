(
	SynthDef("javaosc-example",{ arg freq = 440;
		Out.ar(0, SinOsc.ar(freq, 0, 0.2)
	)
}).writeDefFile;
	s.sendMsg("/d_load", "synthdefs/javaosc-example.scsyndef");
)

