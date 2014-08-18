// © Kasper van den Berg, 2014
package net.kaspervandenberg.akkaRdf.actor

import net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple
import net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern._
import net.kaspervandenberg.akkaRdf.messages.fipa.Performatives._

import akka.actor.Actor
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
 * remembers everything it sees." />
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
 * scala> import akka.actor.ActorDSL._
 * scala> import akka.actor.ActorSystem
 * scala> import net.kaspervandenberg.akkaRdf.actor.RdfStore_InMemory
 * scala> import net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern._
 * scala> implicit val system = ActorSystem("demoActorSystem")
 * scala> implicit val mailbox = inbox()
 * scala> val rdfStorerBySubject = actor("rdfStorer"){
 *      |                          new RdfStore_InMemory(PatternGS__.apply)}
 * }}}
 *
 * To stop your REPL session use:
 * {{{
 * scala> system.shutdown
 * }}}
 *
 * @tparam A	the type of
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
extends Actor
{
	private val quadrupleMap: mutable.Map[A, mutable.Set[Quadruple]] =
				mutable.Map.empty;

	override def receive =
	{
		case Inform(quadruple: Quadruple) => store(quadruple)
		case QueryRef(pattern: A)	=> retrieve(pattern)
		case QueryIf(pattern: A)	=> retrieve(pattern, reportUnfoundPattern)

		case Failure(_)				=> ()
		case _unknown				=> sender ! Failure(_unknown)
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

	private def ignoreUnfoundPattern(pattern: A): Unit = ()

	private def reportUnfoundPattern(pattern: A): Unit =
			sender ! Failure(pattern)
}
