## Overview

_Open Sound Control_ (OSC) is a _content format_,
though it is often though of as a protocol for the transmission of data over a network.
Its main use and origin is that of a _replacement for MIDI_
as a network-protocol for the exchange of musical control data between soft- and hardware over a UDP-IP network.
Applications like SuperCollider, Max/MSP, and Reaktor (among others) use OSC for network communication.
Nowadays it is also used in other fields, for example in robotics.

__JavaOSC__ is a library that gives Java programs the capability of sending and receiving OSC.
It is not, in itself, a usable program.

The latest release version of the library is available on
[Maven central](http://mvnrepository.com/artifact/com.illposed.osc/javaosc-core)
or
[the project homepage](http://www.illposed.com/software/javaosc.html).

Latest development sources can be found
[on github](https://github.com/hoijui/JavaOSC).


## Folder structure

* `modules/core/src/main/java/`                     JavaOSC core sources
* `modules/ui/src/main/java/`                       JavaOSC UI sources
* `modules/core/src/main/resources/puredata/`       PureData file for the PD example
* `modules/core/src/main/resources/supercollider/`  SuperCollider files for the examples
* `modules/*/target/`                               where build files end up


## How to

### Run the Demo UI

#### ... with SuperCollider

JavaOSC is not a standalone application, but designed to be used in other applications.
Though, there is a very basic application, created by John Thompson, for demonstration purposes.

To _run the demo app_, make sure you have all parts packaged and installed:

	mvn install

Then start the UI:

	cd modules/ui
	mvn exec:java

Next, launch SuperCollider, open the file located in the
`modules/core/src/main/resources/supercollider/` directory,
and load the synthdef into SuperCollider.
Start the SC local server. 
In the JavaOSC Demo UI, click the "All On" button and start moving the sliders.
You should hear the sounds change.
To see what messages the UI is sending, run either the CNMAT dumpOSC,
or turn on dumpOSC in SuperCollider.

#### ... with PD

There is also a PureData patch created by Alexandre Quessy,
available [here](http://www.sourcelibre.com/puredata/).

To try the demo UI with PureData,
launch (this is important!) _pd-extended_ and open the file
`modules/core/src/main/resources/puredata/javaosc.pd`.
Turn down the volume a bit at first, as it might be very loud.
Click the "All On" button, and start moving the sliders.
You should hear the sounds change.
To see what messages the UI is sending, just look in the PD window or
in the terminal.

### Use the library

The classes that deal with sending OSC data are located in the `com.illposed.osc` package.
The core classes are `com.illposed.osc.OSCPort{In,  Out}`,
`com.illposed.osc.OSCMessage` and `com.illposed.osc.OSCBundle`.

The common way to use the library is to instantiate an `OSCPort`
connected to the receiving machine and then call the `send()` method
on the port with the packet to send as the argument.

There are some associated JUnit tests, which also contain code that may illustrate
how to use the library.
They can be run with `mvn test`.


## Release a SNAPSHOT (devs only)

	mvn clean deploy

To release a development version to the Sonatype snapshot repository only.


## Release (devs only)

### Prepare "target/" for the release process

	mvn release:clean

### Prepare the release

	JAVA_HOME="${JAVA_6_HOME}" \
		MAVEN_HOME="${MAVEN_3_2_5_HOME}" \
		PATH="${MAVEN_HOME}/bin/:${PATH}" \
		mvn -DdryRun=true release:prepare
	JAVA_HOME="${JAVA_6_HOME}" \
		MAVEN_HOME="${MAVEN_3_2_5_HOME}" \
		PATH="${MAVEN_HOME}/bin/:${PATH}" \
		mvn -DdryRun=false release:prepare

This does the following:

* _Important for backwards compatibility_:
use the oldest possible JDK version to compile (currently 1.6)
* asks for the release and new snapshot versions to use (for all modules)
* packages
* signs with GPG
* commits
* tags

### Perform the release (main part)

	git push origin master <release-tag>
	JAVA_HOME="${JAVA_6_HOME}" \
		MAVEN_HOME="${MAVEN_3_2_5_HOME}" \
		PATH="${MAVEN_HOME}/bin/:${PATH}" \
		mvn release:perform

This does the following:

* pushes to origin
* checks-out the release tag
* builds
* deploy into Sonatype staging repository

### Release the site

	git checkout <release-tag>
	mvn clean site
	git checkout master

This does the following:

* generates the site
* pushes the site to the GitHub `gh-pages` branch,
  which is visible under `http://hoijui.github.com/JavaOSC/`

### Promote it on Maven

Use one of these methods:

* _default_: using the Nexus staging plugin

		mvn nexus:staging-close
		mvn nexus:staging-release

* _alternative_: using the web-interface
	1. firefox https://oss.sonatype.org
	2. login
	3. got to the "Staging Repositories" tab
	4. select "com.illposed..."
	5. "Close" it
	6. select "com.illposed..." again
	7. "Release" it

This moves the artifact from the Sonatype staging to the main Sonatype repository.
From there, it will automatically be copied to Maven Central,
which happens at least every four hours.


## Thanks

Thanks to John Thompson for writing the UI (demo application),
Alexandre Quessy for the PD demo,
and to Martin Kaltenbrunner and Alex Potsides for their contributions.

