// SPDX-FileCopyrightText: 2012 C. Ramakrishnan / Auracle
//
// SPDX-License-Identifier: BSD-3-Clause

(
	SynthDef("javaosc-example",{ arg freq = 440;
		Out.ar(0, SinOsc.ar(freq, 0, 0.2)
	)
}).writeDefFile;
	s.sendMsg("/d_load", "synthdefs/javaosc-example.scsyndef");
)

