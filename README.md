[![Build Status](https://github.com/centic9/commons-test/actions/workflows/gradle-build.yml/badge.svg)](https://github.com/centic9/commons-test/actions)
[![Gradle Status](https://gradleupdate.appspot.com/centic9/commons-test/status.svg?branch=master)](https://gradleupdate.appspot.com/centic9/commons-test/status)
[![Release](https://img.shields.io/github/release/centic9/commons-test.svg)](https://github.com/centic9/commons-test/releases)
[![GitHub release](https://img.shields.io/github/release/centic9/commons-test.svg?label=changelog)](https://github.com/centic9/commons-test/releases/latest)
[![Tag](https://img.shields.io/github/tag/centic9/commons-test.svg)](https://github.com/centic9/commons-test/tags)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.dstadler/commons-test/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.dstadler/commons-test) 
[![Maven Central](https://img.shields.io/maven-central/v/org.dstadler/commons-test.svg)](https://maven-badges.herokuapp.com/maven-central/org.dstadler/commons-test)

This is a small library of code-pieces that I find useful when writing tests.

It covers areas that I miss in JUnit itself e.g. for verifying compare()/hashCode()/equals() implementations and for multi-threaded tests.

## Contents
 
* MockSMTPServer - simulate an SMTP Server for testing code which sends emails
* MockRESTServer - simulate a HTTP Server for testing code which accesses other systems, e.g. to mock REST interfaces in tests
* TestHelpers - small utilities for testing things like equals(), hashCode(), toString(), compare() and implementations of Comparator, they ensure some things that the Java spec mandates
* ThreadTestHelpers - easily run unit tests multiple times in parallel to ensure the code does not contain hidden race conditions
* MemoryLeakVerifier - a simple way of adding memory leak assertions to unit tests
* TestEnvironment - handling temporary files/directories in a clean way, ensure that files are not locked any more at the end of the test
* PrivateConstructorCoverage - use this if you would like to get 100% coverage of classes with private constructors
* HeapDump - trigger a writing a .hprof file

## Use it

### Gradle

    compile 'org.dstadler:commons-test:1.+'

## Change it

### Grab it

    git clone https://github.com/centic9/commons-test.git

### Build it and run tests

    cd commons-test
    ./gradlew check jacocoTestReport

### Release it

* Check version in `gradle.properties`
* Publish the binaries to Maven Central

    ./gradlew --console=plain publishToMavenCentral

* This should automatically release the new version on MavenCentral
* Apply tag in Github (`git tag` && `git push --tags`)
* Increase version in `gradle.properties` afterwards
* Afterwards go to the [Github releases page](https://github.com/centic9/commons-test/releases) and add release-notes

## Support this project

If you find this library useful and would like to support it, you can [Sponsor the author](https://github.com/sponsors/centic9)

## Licensing

* commons-test is licensed under the [BSD 2-Clause License].
* A few pieces are imported from other sources, the source-files contain the necessary license pieces/references.

[BSD 2-Clause License]: https://www.opensource.org/licenses/bsd-license.php
