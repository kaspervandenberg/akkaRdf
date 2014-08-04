// © Kasper van den Berg, 2014
package net.kaspervandenberg.akkaRdf.rdf

import java.net.URI;
import java.util.UUID;

/**
	* An RDF-graph can be stored as a set of {@code subject}–{@code 
	* predicate}–{@code object}-[[Rdf.Triple triples]].  Adding a fourth item, 
	* named {@code graph}, allows storing multiple graphs in a single collection 
	* (see [[Rdf.Quadruple]]).
  *
	* The following RdfQuadruples represent the two combined example graphs from 
	* [[http://www.w3.org/TR/rdf11-primer/ RDF 1.1 Primer]] §3.5.  As shown in 
	* the image <img 
	* src="http://www.w3.org/TR/2014/NOTE-rdf11-primer-20140225/example-graph.jpg" 
	* />
	* {{{
	* scala> import java.net.URI
	* scala> import net.kaspervandenberg.akkaRdf.rdf.Rdf._
	*
	* scala> val graph1 = NamedResource(new URI("http://example.org/bobInfo"))
	* scala> val bob = NamedResource(new URI("http://example.org/bob"))
	* scala> val alice = NamedResource(new URI("http://example.org/alice"))
	* scala> val isA = NamedResource(
	*		new URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
	* scala> val person = NamedResource(
	*		new URI("http://xmlns.com/foaf/0.1/Person"))
	* scala> val isFriend = NamedResource(
	*		new URI("http://xmlns.com/foaf/0.1/knows"))
	*	scala> val isBornOn = NamedResource(
	*		new URI("https://www.wikidata.org/wiki/Property:P569"))
	* scala> val isIntrestedIn = NamedResource(
	*		new URI("http://example.org/rdf/isIntrestedIn"))
	* scala> val july_14th_1990 = Literal("1990-07-24",
	*		NamedResource(new URI("http://www.w3.org/2001/XMLSchema#date")))
	* scala> val graph2 = NamedResource(
	*		new URI("https://www.wikidata.org/wiki/Special:EntityData/Q12418"))
	* scala> val monaLisa = NamedResource(
	*		new URI("https://www.wikidata.org/wiki/Q12418"))
	* scala> val leonardoDaVinci = NamedResource(
	*		new URI("https://www.wikidata.org/wiki/Q762"))
	* scala> val work = NamedResource(
	*		new URI("https://www.wikidata.org/wiki/Property:P800"))
	* scala> val videoLaJocondeAWashington = NamedResource(
	*		new URI("http://example.org/videoLaJoconde&#x00C0;Washington"))
	* scala val isAbout = NamedResource(new URI("http://example.org/rdf/about"))
	*
	*	scala> Seq(
	*		Quadruple(graph1, bob, isFriend, alice),
	*		Quadruple(graph1, bob, isA, person),
	*		Quadruple(graph1, bob, isBornOn, july_14th_1990),
	*		Quadruple(graph1, bob, isIntrestedIn, monaLisa),
	*		Quadruple(graph2, leonardoDaVinci, work, monaLisa),
	*		Quadruple(graph2, videoLaJocondeAWashington, isAbout, monaLisa)
	*	)}}}
	*
	* Most [[Rdf.Value]]s in the example are [[Rdf.NamedResource]]s, one is a 
	* [[Rdf.Literal]]; we can alter the example to include [[Rdf.BNode]]:
	*			''Someone is a friend of Alice. That someone is interested in *			
	something.  The video "La Joconde à Washington" is about that *			
	something.''
	* This would be represented as:
	* {{{
	* scala> val graph3 = NamedResource(new URI("http:example.org/graph3"))
	* scala> val blank1 = BNode()
	* scala> val blank2 = BNode()
	*
	* scala> Seq(
	*		Quadruple(graph3, blank1, isFriend, alice),
	*		Quadruple(graph3, blank1, isIntrestedIn, blank2),
	*		Quadruple(graph3, videoLaJocondeAWashington, isAbout, blank2)
	* )}}}
  */
object Rdf {
	case class Triple(
		subject: Resource,
		predicate: NamedResource,
		rdfObject: Value)

	case class Quadruple (
		graph: NamedResource,
		subject: Resource,
		predicate: NamedResource,
		rdfObject: Value)

	sealed abstract class Value

	sealed abstract class Resource extends Value

	/**
		* A [[Resource]] identified with an {@link java.net.URI}.
		*
		* When two resources `a` and `b` have the same `uri`, the resources are the 
		* same.
		*/
	case class NamedResource(uri: URI) extends Resource 

	/**
		* An unidentified resource.
		*
		* BNodes are unique within a limited scope: when crossing these scope 
		* boundaries the same BNode can represent different resources.
		*/
	case class BNode(nodeId: UUID) extends Resource
	{
		/**
			* Creating a BNode without supplying an id means that the BNode will have a 
			* fresh and unique generated id.
			*/
		def this() = this(UUID.randomUUID())
	}

	/**
		* Factory extending the case class BNode with random generated ids.
		*/
	object BNode
	{
		def apply() = new BNode()
	}

	case class Literal(value: String, valueType: NamedResource) extends Value
}
