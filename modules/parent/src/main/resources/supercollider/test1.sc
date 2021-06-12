// SPDX-FileCopyrightText: 2012 C. Ramakrishnan / Auracle
//
// SPDX-License-Identifier: BSD-3-Clause

s.boot;
b = "hola";
b.postln;

a = {
        Out.ar(0, SinOsc.ar(800, 0, 0.1))
};


a.play;
