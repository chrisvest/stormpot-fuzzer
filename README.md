stormpot-fuzzer
===============

Fuzz- and stress-testing program for Stormpot.

Run the fuzzer by simply typing the command `mvn -Pfuzz` on a command line
in this directory, with some additional parameters for specifying for how
long the fuzzer should run:

* `-Dhours=0` – run the fuzzer for this many hours, default 0.
* `-Dminutes=0` – run the fuzzer for this many minutes, default 0.
* `-Dseconds=0` – run the fuzzer for this many seconds, default 0.

The times are all added up, and that will be for how long the fuzzer will
run.

Example:

    mvn -Dminutes=5 -Dseconds=30

Note: the fuzzer requires Java7, so make sure that you have that installed.
Also, if you don't use OS X, then you might have to edit the `java7` script,
such that it runs the correct version of Java.
