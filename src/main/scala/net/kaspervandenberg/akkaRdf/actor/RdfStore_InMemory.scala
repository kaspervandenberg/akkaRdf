// © Kasper van den Berg, 2014
package net.kaspervandenberg.akkaRdf.actor

import net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple
import net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern._
import net.kaspervandenberg.akkaRdf.messages.fipa._

import akka.actor.ActorDSL
import akka.actor.ActorDSL._
import scala.collection.mutable
import scala.collection.Set

/**
 * Actor that stores all
 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple Quadruples]]^^(that it is 
 * [[net.kaspervandenberg.akkaRdf.messages.fipa.Performatives.Inform Informed]] 
 * about)^^ and replies with a series of `Inform`-messages on 
 * [[net.kaspervandenberg.akkaRdf.messages.fipa.Performatives.QueryRef QueryRef]]
 * and
 * [[net.kaspervandenberg.akkaRdf.messages.fipa.Performatives.QueryIf QueryIf]]
 * containing a
 * [[net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern QuadruplePattern]].
 *
 * <img src="../../../../../classes/icons/photographicMemory.png" alt="This 
 * Actor's Memomic icon: a photo camera and a brain, i.e. an actor that 
 * remembers everything it sees." /> This actor has eidetic memory: it 
 * remembers everything you tell it.
 *
 * = Example interactions =
 * == Starting Akka ==
 * To play with the `RdfStore_InMemory`-actor, we need the following objects in 
 * Akka:
 *  1. the [[akka.actor.ActorSystem AkkaSystem]], which contains the actor(s);
 *  1. an [[akka.actor.Inbox Inbox]], with which we can receive messages from
 *     the actors; and
 *  1. the `RdfStore_InMemory`-actor.
 *
 * We will use the [[akka.actor.ActorDSL ActorDSL]] to simplify using akka; 
 * this leads to the following REPL code:
 * {{{
 * scala> import akka.actor.ActorSystem
 * scala> import akka.actor.ActorDSL._
 * scala> import net.kaspervandenberg.akkaRdf.actor.RdfStore_InMemory
 * scala> import net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern._
 *
 * scala> implicit val system = ActorSystem("demoSystem")
 * scala> implicit val mailbox = inbox()
 * scala> val rdfStorer = actor("rdfStorer"){
 *      |                 new RdfStore_InMemory(PatternGS__.apply) }
 * }}}
 *
 * ==Stop Akka==
 * To stop your REPL session use:
 * {{{
 * scala> system.shutdown
 * }}}
 *
 * ==Load triples==
 * Use the example from [[net.kaspervandenberg.akkaRdf.rdf.DSL]] to define some 
 * triples.  The example below is slightly altered so that it contains
 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.NamedResource Rdf.NamedResource]]s 
 * for the graph and the subject 'bob':
 * {{{
 * scala> import net.kaspervandenberg.akkaRdf.rdf.DSL
 * scala> import net.kaspervandenberg.akkaRdf.rdf.Rdf
 * scala> object dslDemo extends DSL {
 *      | definePrefix('ex) := "http://example.org/"
 *      | definePrefix('rdf) := "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
 *      | definePrefix('foaf) := "http://xml.ns.com/foaf/0.1/"
 *      | definePrefix('xsd) := "http://www.w3.org/2001/XMLSchema#"
 *      | definePrefix('wikiData) := "http://www.wikidata.org/wiki/"
 *      | definePrefix('auxRdf) := "http://example.org/rdf/"
 *      |
 *      | val work = 'wikiData :: "Property%3AP800"
 *      | val isBornOn = 'wikiData :: "Property%3AP569"
 *      | val monaLisa = 'wikiData :: "Q12418"
 *      | val leonardoDaVinci = 'wikiData :: "Q762"
 *      |
 *      | val graphBobInfo = 'ex :: "bobInfo"
 *      | val subjBob = 'ex :: "bob"
 *      |
 *      | object graph1 extends Graph(graphBobInfo) {
 *      |   addTriple(subjBob ==> 'foaf :: "knows" ==> 'ex :: "alice")
 *      |   addTriple(subjBob ==> 'rdf :: "type" ==> 'foaf :: "Person")
 *      |   addTriple(subjBob ==> isBornOn ==>
 *      |             Rdf.Literal("1990-07-24", 'xsd ::"date"))
 *      |   addTriple(subjBob ==> 'auxRdf :: "isIntrestedIn" ==> monaLisa)
 *      | }
 *      |
 *      | object graph2 extends Graph('wikiData :: "Special%3AEntityData/Q12418") {
 *      |   addTriple(leonardoDaVinci ==> work ==> monaLisa)
 *      |   addTriple('ex :: "videoLaJoconde&#x00C0;Washington" ==>
 *      |             'auxRdf :: "about" ==> monaLisa)
 *      | }
 *      | }
 * }}}
 *
 * Having defined the graph, we can use the following to retrieve some triples:
 * {{{
 * scala> val singleTriple = dslDemo.graph1().head
 * scala> val setOfTriples = dslDemo.graph1() ++ dslDemo.graph2()
 * }}}
 *
 * ==Interact with `rdfStorer`==
 * Define a pattern to query for any triple in the graph `"bobInfo"` having the 
 * subject `"bob"`:
 * {{{
 * scala> import net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern._
 * scala> import net.kaspervandenberg.akkaRdf.messages.fipa.Performatives._
 * scala>
 * scala> val bobQueryPattern = PatternGS__(dslDemo.graphBobInfo, dslDemo.subjBob)
 * }}}
 *
 * ===Querying empty storer===
 * Upon creation `rdfStorer` does not 'know' any triples. Sending a
 * [[net.kaspervandenberg.akkaRdf.messages.fipa.Performatives.QueryRef QueryRef]]
 * results in no reply:
 * {{{
 * scala> rdfStorer ! QueryRef(bobQueryPattern)
 * scala> mailbox.receive()
 * }}}
 * Causes a `java.util.concurrent.TimeoutException`.
 *
 * Sending a
 * [[net.kaspervandenberg.akkaRdf.messages.fipa.Performatives.QueryIf QueryIf]]
 * results in a reply with
 * [[net.kaspervandenberg.akkaRdf.messages.fipa.Performatives.Failure Failure]]:
 * {{{
 * scala> rdfStorer ! QueryIf(bobQueryPattern)
 * scala> mailbox.receive()
 * }}}
 * Results in:
 * {{{
 * res6: Any = Failure(PatternGS__(NamedResource(http://example.org/bobInfo),NamedResource(http://example.org/bob)))
 * }}}
 *
 * ===Storing a single triple===
 * [[net.kaspervandenberg.akkaRdf.messages.fipa.Performatives.Inform Informing]]
 * the `rdfStorer` about the `singleTriple` and then querying for it:
 * {{{
 * scala> rdfStorer ! Inform(singleTriple)
 * scala> rdfStorer ! QueryIf(bobQueryPattern)
 * }}}
 * Results in a single reply with an `Inform` in the mailbox:
 * {{{
 * scala> mailbox.receive()
 * res6: Any = Inform(Quadruple(NamedResource(http://example.org/bobInfo),NamedResource(http://example.org/bob),NamedResource(http://www.wikidata.org/wiki/Property%3AP569),Literal(1990-07-24,NamedResource(http://www.w3.org/2001/date))))
 * }}}
 * Issueing an other`mailbox.receive()` after it results in a 
 * `TimeoutException`.
 *
 * Informing the `RdfStore_InMemory actor multiple time with the same triple 
 * results and then querying it results only in a single reply.
 *
 * ===Storing and querying a set of triples===
 * Informing the `rdfStorer` with all triples from `setOfTriples` and then 
 * querying:
 * {{{
 * scala> setOfTriples.foreach { rdfStorer ! Inform(_) }
 * scala> rdfStorer ! QueryRef(bobQueryPattern)
 * }}}
 * results in 4 `Inform` replies one for each `quadruple` matching `( bobInfo, 
 * bob, _, _):
 * {{{
 * scala> mailbox.receive()
 * res9: Any = Inform(Quadruple(NamedResource(http://example.org/bobInfo),NamedResource(http://example.org/bob),NamedResource(http://www.wikidata.org/wiki/Property%3AP569),Literal(1990-07-24,NamedResource(http://www.w3.org/2001/date))))
 * 
 * scala> mailbox.receive()
 * res10: Any = Inform(Quadruple(NamedResource(http://example.org/bobInfo),NamedResource(http://example.org/bob),NamedResource(http://www.w3.org/1999/02/type),NamedResource(http://xml.ns.com/foaf/0.1/Person)))
 * 
 * scala> mailbox.receive()
 * res11: Any = Inform(Quadruple(NamedResource(http://example.org/bobInfo),NamedResource(http://example.org/bob),NamedResource(http://example.org/rdf/isIntrestedIn),NamedResource(http://www.wikidata.org/wiki/Q12418)))
 * 
 * scala> mailbox.receive()
 * res12: Any = Inform(Quadruple(NamedResource(http://example.org/bobInfo),NamedResource(http://example.org/bob),NamedResource(http://xml.ns.com/foaf/0.1/knows),NamedResource(http://example.org/alice)))
 * 
 * scala> mailbox.receive()
 * java.util.concurrent.TimeoutException: deadline passed
 * }}}
 *
 *
 * ===Querying for all stored Patterns===
 * Querying for the `classOf` of the stored pattern type results in replies for 
 * each key in the `RdfStorer`:
 * {{{
 * scala> setOfTriples.foreach { rdfStorer ! Inform(_) } // just to make sure, if you skipped ahead
 * scala> rdfStorer ! QueryRef(classOf[PatternGS__)
 * }}}
 * results in 3 `Inform` messages:
 * {{{
 * scala> mailbox.receive()
 * res10: Any = Inform(PatternGS__(NamedResource(http://www.wikidata.org/wiki/Special%3AEntityData/Q12418),NamedResource(http://example.org/videoLaJoconde&#x00C0;Washington)))
 *
 * scala> mailbox.receive()
 * res11: Any = Inform(PatternGS__(NamedResource(http://example.org/bobInfo),NamedResource(http://example.org/bob)))
 *
 * scala> mailbox.receive()
 * res12: Any = Inform(PatternGS__(NamedResource(http://www.wikidata.org/wiki/Special%3AEntityData/Q12418),NamedResource(http://www.wikidata.org/wiki/Q762)))
 * }}}
 *
 * ===Breaking Rdf storer===
 * Querying for a pattern of a different type causes an class cast exception.
 *
 * 
 * @tparam	A	the type of
 * 			[[net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern.Pattern Pattern]]
 *			that this Actor uses to store
 *			[[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple Quadruples]].
 * @param patternCreator	function that transforms `Quadruple`s into the
 *			`Pattern` of type `A`.  This can be the `apply()`-function from the 
 *			target pattern (`A`), for eample
 *			[[net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern.PatternGS__ PatternGS__.apply]].
 *
 */
class RdfStore_InMemory[A <: Pattern](
		patternCreator: (Quadruple) => A)
extends ActorDSL.Act
{
	private val quadrupleMap: mutable.Map[A, mutable.Set[Quadruple]] =
				mutable.Map.empty;

	override def receive =
	{
		case Inform(quadruple: Quadruple)
			=> store(quadruple)
		case QueryRef(cls: Class[A])
			=> retrieveKeys
		case QueryRef(pattern) if pattern.isInstanceOf[Pattern]
			=> retrieve(pattern.asInstanceOf[A])
		case QueryIf(pattern)
			=> 	retrieve(
						pattern.asInstanceOf[A],
						reportUnfoundPattern)

		case NotUnderstood(performative)
			=> throw NotUnderstoodException(
					sender(),
					performative)
		case _unknown
			=> sender !  NotUnderstood(_unknown)
	}

	def store(quadruple: Quadruple) =
	{
		val pattern = patternCreator(quadruple);
		quadrupleMap.getOrElseUpdate(pattern, mutable.Set.empty)
					.add(quadruple)
	}

	def retrieve(
			pattern: A,
			onFailure: (A) => Unit = ignoreUnfoundPattern) =
	{
		def informSingle(quadruple: Quadruple): Unit =
				sender ! Inform(quadruple)

		def informAll(quadSet: Set[Quadruple]): Unit =
				quadSet.foreach(informSingle)

		quadrupleMap.andThen(informAll).applyOrElse(
				pattern,
				onFailure.andThen(Set.empty))
	}

	def retrieveKeys =
	{
		def informSingle(pattern: A): Unit =
			sender ! Inform(pattern)

		quadrupleMap.keys.foreach(informSingle)
	}

	private def ignoreUnfoundPattern(pattern: A): Unit = ()

	private def reportUnfoundPattern(pattern: A): Unit =
			sender ! Failure(pattern)
}

/* vim:set shiftwidth=4 tabstop=4 autoindent fo=cqtwa2 : */ 

