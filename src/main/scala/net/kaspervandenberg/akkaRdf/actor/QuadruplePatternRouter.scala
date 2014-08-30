// © Kasper van den Berg, 2014
package net.kaspervandenberg.akkaRdf.actor

import net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple
import net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern._
import net.kaspervandenberg.akkaRdf.messages.fipa.Performatives._

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorDSL
import akka.actor.ActorDSL._

import scala.collection.Map

/**
 * Routes
 * [[net.kaspervandenberg.akkaRdf.messages.fipa.Performatives.QueryIf QueryIf]]
 * and
 * [[net.kaspervandenberg.akkaRdf.messages.fipa.Performatives.QueryRef QueryRef]]
 * to the child actor for the given
 * [[net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern]].
 *
 * The QuadruplePatternRouter` is like a river delta: the messages flow in at 
 * one end and then branch through the delta until they end up at the correct 
 * actor downstream.
 *
 * =Examples=
 * ==Start Akka==
 * Start Akka and create a `QuadruplePatternRouter` with routes to 
 * [[RdfStore_InMemory]] actors:
 * {{{
 * scala> import akka.actor.ActorSystem
 * scala> import akka.actor.ActorDSL._
 * scala> import net.kaspervandenberg.akkaRdf.actor.RdfStore_InMemory
 * scala> import net.kaspervandenberg.akkaRdf.actor.QuadruplePatternRouter
 * scala> import net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern._
 *
 * scala> implicit val system = ActorSystem("demoSystem")
 * scala> val routes = QuadruplePatternRouter.createRoutesForEachPattern {
 *      |     case  (pat, f) => actor(
 *      |         s"storer_${pat.getSimpleName}") {
 *      |         new RdfStore_InMemory(f) }
 *      | }
 * scala> val patternRouter = actor ("patternRouter") { new QuadruplePatternRouter(routes) }
 * }}}
 *
 * ==Stop Akka==
 * When finished playing with the `QuadruplePatternRouter` stop Akka as 
 * follows:
 * {{{
 * scala> system.shutdown
 * }}}
 * ''Note: if you do not call `system.shutdown` Akka will continue running and 
 * the scala REPL will not stop.''
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
 * ==Interact with `patternRouter`==
 * To demonstrate the `QuadruplePatternRouter` define an actor that will 
 * interact with `patternRouter`:
 * {{{
 * scala> import akka.actor.ActorDSL._
 * scala> import akka.actor.Actor
 * scala> import akka.actor.ActorRef
 * scala> import net.kaspervandenberg.akkaRdf.rdf.Rdf._
 * scala> import net.kaspervandenberg.akkaRdf.messages.fipa.Performatives._
 *
 * scala> case class AskTo(
 *      |   target: ActorRef,
 *      |   question: Message[_])
 *
 * scala> val demoActor = actor(new Actor{
 *      |   override def receive = {
 *      |     case AskTo(target, question) => {
 *      |       println(s"Asking ${question} to actor ${target}")
 *      |       target.tell(question, self)
 *      |     }
 *      |     case Inform(content) =>
 *      |       println(s"Received Inform-message containing ${content} from ${sender()}")
 *      |     case x: Any =>
 *      |       println(s"Received unknown message ${x} of type ${x.getClass}.")
 *      |   }
 *      | })
 * }}}
 *
 * Define a pattern to query for any triple in the graph `"bobInfo"` having the 
 * subject `"bob"`:
 * {{{
 * scala> import net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern._
 * scala> import net.kaspervandenberg.akkaRdf.messages.fipa.Performatives._
 * scala>
 * scala> val bobQueryPattern = PatternGS__(dslDemo.graphBobInfo, dslDemo.subjBob)
 * }}}
 *
 * The `QuadruplePatternRouter` forwards the [[Inform]] messages to all actors 
 * on its routes:
 * {{{
 * scala> setOfTriples.foreach { patternRouter ! Inform(_) }
 * scala> demoActor ! AskTo(patternRouter, QueryRef(bobQueryPattern))
 * }}}
 * results in 4 `Inform` replies one for each `quadruple` matching `( bobInfo, 
 * bob, _, _); note that the inform messages come from `storer_PatternGS__`:
 * {{{
 * Received Inform-message containing Quadruple(NamedResource(http://example.org/bobInfo),NamedResource(http://example.org/bob),NamedResource(http://www.wikidata.org/wiki/Property%3AP569),Literal(1990-07-24,NamedResource(http://www.w3.org/2001/date))) from Actor[akka://demoSystem/user/storer_PatternGS__#1651964514]
 * Received Inform-message containing Quadruple(NamedResource(http://example.org/bobInfo),NamedResource(http://example.org/bob),NamedResource(http://www.w3.org/1999/02/type),NamedResource(http://xml.ns.com/foaf/0.1/Person)) from Actor[akka://demoSystem/user/storer_PatternGS__#1651964514]
 * Received Inform-message containing Quadruple(NamedResource(http://example.org/bobInfo),NamedResource(http://example.org/bob),NamedResource(http://example.org/rdf/isIntrestedIn),NamedResource(http://www.wikidata.org/wiki/Q12418)) from Actor[akka://demoSystem/user/storer_PatternGS__#1651964514]
 * Received Inform-message containing Quadruple(NamedResource(http://example.org/bobInfo),NamedResource(http://example.org/bob),NamedResource(http://xml.ns.com/foaf/0.1/knows),NamedResource(http://example.org/alice)) from Actor[akka://demoSystem/user/storer_PatternGS__#1651964514]
 * }}}
 *
 * @param routes	the [[Pattern]]-subclasses mapped to the [[Actor]]s to
 * 			forward messages to.
 *
 *			`routes` containing an actor for every type of pattern can be 
 *			created as follows.  Assuming that the actor is generic and that 
 *			actor's constructor requies a function `(Quadruple) => Pattern` as 
 *			parameter such as [[RdfStore_InMemory]].
 * 			{{{
 *			scala> import net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern.Pattern
 *			scala> import net.kaspervandenberg.akkaRdf.actor.QuadruplePatternRouter
 *			scala> import net.kaspervandenberg.akkaRdf.actor.RdfStore_InMemory
 *			scala> import akka.actor.ActorDSL._
 *			scala> import akka.actor.ActorSystem
 *			scala>
 *			scala> // Initialise ActorSystem
 *			scala> implicit val system = ActorSystem("demoSys")
 *			scala>
 *			scala> val routes = QuadruplePatternRouter.createRoutesForEachPattern {
 * 			     |     case  (pat, f) => actor(
 * 			     |         s"storer_${pat.getSimpleName}") {
 * 			     |         new RdfStore_InMemory(f) }
 * 			     | }
 *			scala> val routes = Pattern.creationFunctions.mapValues (
 *			     | (f) => actor { new RdfStore_InMemory(f) } )
 *			scala> val patternRouter = actor ("patternRouter") { new QuadruplePatternRouter(routes) }
 *			}}}
 */
class QuadruplePatternRouter(
		routes: Map[Class[_ <: Pattern], ActorRef])
extends ActorDSL.Act {
	override def receive = {
		case Inform(content: Quadruple) =>
			routes.values.foreach { _  forward Inform(content) }
		case QueryIf(msg: Pattern) if routes.isDefinedAt(msg.getClass) =>
			routes(msg.getClass) forward QueryIf(msg)
		case QueryRef(msg: Pattern) if routes.isDefinedAt(msg.getClass) =>
			routes(msg.getClass) forward QueryRef(msg)

		case Failure(_)				=> ()
		case _unknown				=> sender ! Failure(_unknown)
	}
}

object QuadruplePatternRouter {
	type PatternSubclass = Class[_ <: Pattern]
	type ToPatternFunction = (Quadruple) => Pattern
	type ActorConstructionFunction =
			(PatternSubclass, ToPatternFunction) => ActorRef

	def createRoutesForEachPattern(
			actorCreator: ActorConstructionFunction):
	Map[PatternSubclass, ActorRef] =
	{
		val builder = scala.collection.immutable.Map.newBuilder[
				Class[_ <: Pattern],
				ActorRef]
		Pattern.creationFunctions.foreach {
			case (k: PatternSubclass, v: ToPatternFunction) =>
				builder += (k -> actorCreator(k,v))
		}
		builder.result
	}
}

/* vim:set shiftwidth=4 tabstop=4 autoindent fo=cqtwa2 : */ 

