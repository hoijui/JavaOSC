	      _                       ____    _____   _____
	     | |                     / __ \  / ____| / ____|
	     | |  __ _ __   __ __ _ | |  | || (___  | |
	 _   | | / _` |\ \ / // _` || |  | | \___ \ | |
	| |__| || (_| | \ V /| (_| || |__| | ____) || |____
	 \____/  \__,_|  \_/  \__,_| \____/ |_____/  \_____|

<!-- The title was created with: `figlet -k -f big JavaOSC` -->

<!--
SPDX-FileCopyrightText: 2012 C. Ramakrishnan / Illposed Software
SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>

SPDX-License-Identifier: CC0-1.0
-->

## Status

[![License](
https://img.shields.io/github/license/hoijui/JavaOSC.svg?color=orange)](
https://github.com/hoijui/JavaOSC/blob/master/LICENSE)
[![Latest version at Maven Central](
https://maven-badges.herokuapp.com/maven-central/com.illposed.osc/javaosc-core/badge.svg?color=blue)](
https://maven-badges.herokuapp.com/maven-central/com.illposed.osc/javaosc-core/)
[![Issues](
https://img.shields.io/badge/issues-GitHub-red.svg)](
https://github.com/hoijui/JavaOSC/issues)
[![Chat](
https://img.shields.io/badge/chat-IRC-darkgreen.svg)](
irc://irc.freenode.net/javaosc)

[![REUSE status](
https://api.reuse.software/badge/github.com/hoijui/JavaOSC)](
https://api.reuse.software/info/github.com/hoijui/JavaOSC)
[![GitHub last commit](
https://img.shields.io/github/last-commit/hoijui/JavaOSC.svg)](
https://github.com/hoijui/JavaOSC)
[![Build Status](
https://travis-ci.org/hoijui/JavaOSC.svg?branch=master)](
https://travis-ci.org/hoijui/JavaOSC?branch=master)

[![Code Quality (LGTM)](
https://img.shields.io/lgtm/grade/java/g/hoijui/JavaOSC.svg?logo=lgtm&logoWidth=18)](
https://lgtm.com/projects/g/hoijui/JavaOSC/context:java)
[![Total alerts (LGTM)](
https://img.shields.io/lgtm/alerts/g/hoijui/JavaOSC.svg?logo=lgtm&logoWidth=18)](
https://lgtm.com/projects/g/hoijui/JavaOSC/alerts/)
[![Status (SonarCloud)](
https://sonarcloud.io/api/project_badges/measure?project=com.illposed.osc:javaosc&metric=alert_status)](
https://sonarcloud.io/dashboard?id=com.illposed.osc:javaosc)
[![Coverage (SonarCloud)](
https://sonarcloud.io/api/project_badges/measure?project=com.illposed.osc:javaosc&metric=coverage)](
https://sonarcloud.io/component_measures/metric/coverage/list?id=com.illposed.osc:javaosc)
[![Bugs (SonarCloud)](
https://sonarcloud.io/api/project_badges/measure?project=com.illposed.osc:javaosc&metric=bugs)](
https://sonarcloud.io/component_measures/metric/reliability_rating/list?id=com.illposed.osc:javaosc)
[![Vulnerabilities (SonarCloud)](
https://sonarcloud.io/api/project_badges/measure?project=com.illposed.osc:javaosc&metric=vulnerabilities)](
https://sonarcloud.io/component_measures/metric/security_rating/list?id=com.illposed.osc:javaosc)

## What is this?

### JavaOSC

JavaOSC is primarily a library that gives JVM language programs the ability
of serializing, parsing, sending and receiving data in the OSC content format.
It is written in Java 8, and its only runtime dependency is a logging facade (_SLF4J_).
It is also an [OSGi](https://www.osgi.org/developer/what-is-osgi/)
(a dynamic module system for Java) bundle.

In a separate package,
it also contains a sample GUI that allows to send OSC messages to SuperCollider.

### Open Sound Control (OSC)

is a simple _content format_,
although it is often though of as a _protocol_ for the transmission of data over a network.

#### Application domains

* as a **replacement for MIDI** -
  a network-protocol for the exchange of musical control data between soft- and hardware,
  usually over a UDP/IP network
* in **robotics** -
  for communication between the parts of a robot,
  or multiple robots amongst each other

Its main strength is its simplicity.

## How to

### Get it

The latest release version of the library is available on
[GitHub for manual download](https://github.com/hoijui/JavaOSC/releases)
or on
[Maven central](https://search.maven.org/search?q=a:javaosc-core)

```xml
<dependency>
	<groupId>com.illposed.osc</groupId>
	<artifactId>javaosc-core</artifactId>
	<version>0.7</version>
</dependency>
```

The latest development sources can be found
[on GithHub](https://github.com/hoijui/JavaOSC).

### Run the Demo UI

#### ... with SuperCollider

JavaOSC is not a standalone application, but designed to be used in other applications.
Though there is a very basic application for demonstration purposes.

To _run the demo app_, make sure you have all parts packaged and installed:

```bash
mvn install
```

Then start the UI:

```bash
cd modules/ui
mvn exec:java
```

Next, launch SuperCollider (SC), open the file located in the
`modules/core/src/main/resources/supercollider/` directory,
and load the synthdef into SuperCollider.
Start the SC local server. 
In the JavaOSC Demo UI, click the "All On" button and start moving the sliders.
You should hear the sounds change.
To see what messages the UI is sending, run either the CNMAT `dumpOSC`,
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
The core classes are `com.illposed.osc.transport.OSCPort{In,  Out}`,
`com.illposed.osc.OSCMessage` and `com.illposed.osc.OSCBundle`.

The common way to use the library is to instantiate an `OSCPort{In,  Out}`
connected to the receiving machine, and then call `port.send(myPacket)`.

There are some associated JUnit tests, which also contain code that may illustrate
how to use the library.
They can be run with `mvn test`.


## Release a SNAPSHOT (devs only)

To release a development version to the Sonatype snapshot repository only:

```bash
# open a "private" shell, to not spill the changes in env vars
bash
# set env vars
export JAVA_HOME="${JAVA_8_HOME}"
# do the release
mvn clean deploy
# leave our "private" shell instance again
exit
```

In the project you want to use the _SNAPSHOT_ release,
you need to add this to your _pom.xml_:

```
	<repositories>
		<repository>
			<id>ossrh</id>
			<url>https://oss.sonatype.org/content/repositories/snapshots</url>
		</repository>
	</repositories>
```


## Release (devs only)

### Prepare "target/" for the release process

```bash
mvn release:clean
```

### Setup for signing the release

To be able to sign the release artifacts,
make sure you have a section in your `~/.m2/settings.xml` that looks like this:

	<profiles>
		<profile>
			<id>ossrh</id>
			<activation>
				<activeByDefault>true</activeByDefault>
			</activation>
			<properties>
				<gpg.executable>gpg2</gpg.executable>
				<!--
					This needs to match the `uid` as displayed by `gpg2 --list-keys`,
					and needs to be XML escaped.
				-->
				<gpg.keyname>Firstname Lastname (Comment) &lt;user@email.org&gt;</gpg.keyname>
			</properties>
		</profile>
	</profiles>

If you have not yet done so, generate and publish a key-pair.
See [the Sonatype guide](http://central.sonatype.org/pages/working-with-pgp-signatures.html)
for further details about how to work with GPG keys.

### Prepare the release

```bash
# open a "private" shell, to not spill the changes in env vars
bash
# set env vars
export JAVA_HOME="${JAVA_8_HOME}"
# check if everything is in order
mvn \
	clean \
	package \
	verify \
	gpg:sign \
	site
mvn release:clean
mvn \
	-DdryRun=true \
	release:prepare
# run the prepare phase for real
mvn release:clean
mvn \
	-DdryRun=false \
	release:prepare
# leave our "private" shell instance again
exit
```

This does the following:

* _Important for backwards compatibility_:
use the oldest possible JDK version to compile (currently 1.8)
* asks for the release and new snapshot versions to use (for all modules)
* packages
* signs with GPG
* commits
* tags

### Perform the release (main part)

```bash
# open a "private" shell, to not spill the changes in env vars
bash
# set env vars
export JAVA_HOME="${JAVA_8_HOME}"
# perform the release
git push origin master <release-tag>
mvn release:perform
mvn deploy -P release
# leave our "private" shell instance again
exit
```

This does the following:

* pushes to origin
* checks-out the release tag
* builds
* deploy into Sonatype staging repository
* promote it on Maven Central repository (may have a delay of up to 4h)


## Thanks

Thanks to John Thompson for writing the UI (demo application),
Alexandre Quessy for the PD demo,
and to Martin Kaltenbrunner and Alex Potsides for their contributions.

