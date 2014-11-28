// © Kasper van den Berg, 2014
package net.kaspervandenberg.akkaRdf.messages.fipa

/**
 * The sender informs the receiver that the content is (no longer) true.
 */
case class Disconfirm[+T](override val content: T)
			extends Performative[T](content);
