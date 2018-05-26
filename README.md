Verify Control
==============

![Photo of Control from "The Spy Who Came In From The Cold"](images/control.jpg)

> Thus we do disagreeable things, but we are *defensive*. That, I think, is still fair.

Usage
-----

1. `mvn clean install`
1. `java -jar target/hub.control-1.0-SNAPSHOT.jar server config.yml`
1. Visit `http://localhost:8080/healthcheck`

What is this?
-------------

A reimplementation of [alphagov/verify-hub#policy](https://github.com/alphagov/verify-hub/blob/master/hub/policy).

### In scope initially:

* All API endpoints should respond correctly
* Legal / Illegal state transitions should be handled
* High availability

### Out of scope initially:

* Reporting events to event sink (to be implemented later)
* Exact replication of Policy's states (although the user-facing behaviour should be preserved)
