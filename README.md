akkaRdf
=======

Reactive RDF implementation for Akka.

## Disclaimer:
* akkaRdf is experimental code.
* It's (currently) a single person hobby project
* The main goal of developping akkaRdf is to learn Scala, reactive programming, and the Akka framework (see https://typesafe.com/platform/runtime/akka).
	
W3C's banana-rdf(see https://github.com/w3c/banana-rdf) is probably a better choice if you are looking for a scala RDF library.


## What akkaRdf should do:
With akkaRdf you can connect RDF-actors into an ActorSystem.

*(Future work)* This would allow you for example:
* to build a reactive (i.e. scalable, resilient, …) triple store and/or SPARQL-endpoint;
* extend your akka application with RDF.
