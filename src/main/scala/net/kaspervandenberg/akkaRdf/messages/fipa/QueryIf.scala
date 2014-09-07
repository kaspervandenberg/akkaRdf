// © Kasper van den Berg, 2014
package net.kaspervandenberg.akkaRdf.messages.fipa

/**
 * The sender asks the receiver whether the receiver ''believes'' `content`.
 *
 * The [[http://www.fipa.org/specs/fipa00037/index.html SC00037J FIPA 
 * standard]] states:
 *
 * ''"[`QueryIf` indicates that] the performing agent [(a.k.a. actor)]:''
 *  - ''has no knowledge of the truth of the proposition (i.e. `content`);
 *    and''
 *  - ''believes that the other agent [(i.e. the receiver)] can inform the
 *    querying agent [(i.e. the sender)] if it knows the truth of the 
 *    proposition."''
 *
 * When the receiver ''believes'' `content`, it should respond with an 
 * [[Inform]]-message to the sender.
 *
 * == Negation ==
 *
 * What the receiver should do when it has no information about `content` is 
 * more difficult.  The three approaches of FIPA, Akka, and RDF are 
 * contradicting:
 *  - FIPA defines replying with: `Inform( ¬ 𝜑 )`
 *  - In Akka an actor does not reply when it has no information and the
 *    sender can use a [[akka.pattern.AskableActorRef AskableActorRef#?]] with 
 *    a time-out.
 *  - RDF operates on the open world assumption: not knowing 𝜑 does not imply
 *    ¬ 𝜑; indeed, negation does not exist in RDF.
 * 
 * A solution is to distinguish between `QueryIf` and [[QueryRef]]:
 *  - When receiving a `QueryRef`-message, an actor replies if and only if
 *    it ``beliefs`` RDF-statements unifiable with the contents of `QueryRef`, 
 *    otherwise the actor remains silent.
 *  - When receiving a `QueryIf`-message, an actor always replies.  The actor
 *    replies with either [[Inform]] or with [[Failure]].  Note that because of 
 *    akka's distributed/network architecture it can happen that — although 
 *    after `QueryIf` a receiver should reply — a sender never receives a reply 
 *    from the receiver.
 *
 * @param content	a set of RDF-statements that the sender wishes the receiver
 *		to confirm.
 *
 * @tparam T	the class of the RDF-statement, it can be any of the types
 *		supported by [[Performative]].  However for 
 *		[[net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern.Pattern]] and 
 *		[[scala.Seq sequences]] of `Pattern` using [[QueryRef]] is advised.  
 *		Supported types:
 *		  - [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Triple]],
 *		  - [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]],
 *		  - [[net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern.Pattern]], or
 *		  - a [[scala.Seq]] of the above.
 *
 *		  - When `content` is a single
 *		    [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Triple]] or a single 
 *		    [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]] expect the 
 *		    receiver the reply with a single [[Inform]] or [[Failure]] message.
 *		  - When `content` is a [[scala.Seq]] of `Triples` or `Quadruples` the
 *		    receiver should only reply with `Inform` when it believes ''all'' 
 *		    triples in the sequence; when the receiver believes some or none of 
 *		    the triples it should reply with `Failure`.
 *		  - When `content` is a
 *		    [[net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern.Pattern]] or a 
 *		    `Seq` of `Pattern` the receiver should reply with a series of 
 *		    `Inform` messages, one for each triple that matches the pattern(s),  
 *		    or `Failure` when no triples match the pattern.
 */
case class QueryIf[+T](override val content: T)
			extends Performative[T](content);


