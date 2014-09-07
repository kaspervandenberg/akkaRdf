
// © Kasper van den Berg, 2014
package net.kaspervandenberg.akkaRdf.messages.fipa

/**
 * The sender, 𝛼, informs the receiver, 𝛽, that 𝛼 does not understand the 
 * message that 𝛽 sent to 𝛼.
 *
 * @param content		The message that 𝛽 sent to 𝛼.
 *
 * @tparam T	the class of the RDF-statement, it can be any of the types
 *		supported by [[Performative]].
 */
case class NotUnderstood[+T](override val content: T)
		extends Performative[T](content);


case class NotUnderstoodException(
	val receiver: akka.actor.ActorRef,
	val performative: Any)
extends UnsupportedOperationException;
