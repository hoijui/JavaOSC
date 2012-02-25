## Overview

JavaOSC is a library for communicating through the OSC protocol in Java.
It is not, in itself, a usable program.
Rather, it is a library designed for building programs that need to communicate
over OSC (e.g., SuperCollider, Max/MSP, Reaktor, etc.).

The latest version of javaosc is available at
[the project homepage](http://www.illposed.com/software/javaosc.html)
or
[on github](https://github.com/hoijui/JavaOSC).


## Folder structure

* `modules/core/src/main/java/`                     JavaOSC core sources
* `modules/ui/src/main/java/`                       JavaOSC UI sources
* `modules/core/src/main/resources/puredata/`       PureData file for the PD example
* `modules/core/src/main/resources/supercollider/`  SuperCollider files for the examples
* `modules/*/target/`                               where build files end up


## How to run

### SuperCollider

JavaOSC is not a standalone application, but designed to be used in other applications.
Though, there is a very basic app, created by John Thompson, for demonstration purposes.

To _run the demo app_, make sure you have all parts packaged and installed:

	mvn install

Then start the UI:

	cd modules/ui
	mvn exec:java

Next, launch SuperCollider, open the file located in the
`modules/core/src/main/resources/supercollider/` directory,
and load the synthdef into SuperCollider.
Start the SC local server. 
Click the "All On" button and start moving the sliders.
You should hear the sounds change.
To see what messages the UI is sending, run either the CNMAT dumpOSC,
or turn on dumpOSC in SuperCollider.

### PD

There is also a PureData patch created by Alexandre Quessy,
available [here](http://www.sourcelibre.com/puredata/).

To try the demo app with PureData, launch PureData and open the file 
`modules/core/src/main/resources/puredata/javaosc.pd`.
Turn down the volume a bit at first, as it might be very loud.
Click the "All On" button, and start moving the sliders.
You should hear the sounds change.
To see what messages the UI is sending, just look in the Pd window or 
in the terminal.


## Orientation

Open Sound Control (OSC) is an UDP-based protocol for transmission of musical control data over an IP network. Applications like SuperCollider, Max/MSP, and Reaktor (among others) use OSC for network communication.

JavaOSC is a class library that gives Java programs the capability of sending and receiving OSC. 

The classes that deal with sending OSC data are located in the `com.illposed.osc` package. The core classes are `com.illposed.osc.OSCPort{In,  Out}`, `com.illposed.osc.OSCMessage` and `com.illposed.osc.OSCBundle`.

There are some associated JUnit tests for the OSC classes. They can be run with `mvn test`.


## Use

The way to use the library is to instantiate an `OSCPort`
connected to the receiving machine and then call the `send()` method
on the port with the packet to send as the argument.

To see examples, look at the tests or the simple UI located in
`com.illposed.osc.ui.OscUI`.


## Release a SNAPSHOT (devs only)

To release a development version to the Sonatype snapshot repository only:

		mvn clean deploy


## Release (devs only)

### Prepare "target/" for the release process

	mvn release:clean

### Prepare the release
* asks for the release and new snapshot versions to use (for all modules)
* packages
* signs with GPG
* commits
* tags
* pushes to origin

		mvn release:prepare

### Perform the release (main part)
* checks-out the release tag
* builds
* deploy into sonatype staging repository

		mvn release:perform

### Release the site
* generates the site, and pushes it to the github gh-pages branch,
  visible under http://hoijui.github.com/JavaOSC/

		git checkout <release-tag>
		mvn clean site
		git checkout master

### Promote it on Maven
Moves it from the sonatype staging to the main sonatype repo

1. using the Nexus staging plugin:

		mvn nexus:staging-close
		mvn nexus:staging-release

2. ... alternatively, using the web-interface:
	* firefox https://oss.sonatype.org
	* login
	* got to the "Staging Repositories" tab
	* select "com.illposed..."
	* "Close" it
	* select "com.illposed..." again
	* "Release" it


## Thanks

Thanks to John Thompson for writing the Java demo app,
Alexandre Quessy for the PD demo,
and to Martin Kaltenbrunner and Alex Potsides for their contributions.

