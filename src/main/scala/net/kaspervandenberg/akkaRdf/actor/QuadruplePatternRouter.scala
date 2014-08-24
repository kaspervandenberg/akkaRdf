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

/* vim:set shiftwidth=4 tabstop=4 autoindent fo=cqtwa2 : */ 

