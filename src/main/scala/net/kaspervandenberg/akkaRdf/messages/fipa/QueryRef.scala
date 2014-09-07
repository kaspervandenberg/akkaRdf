// © Kasper van den Berg, 2014
package net.kaspervandenberg.akkaRdf.messages.fipa

/**
 * The sender asks the receiver for triples matching the `content`.
 *
 * This performative is similar to [[QueryIf]]; compared to `QueryIf`, 
 * `QueryRef` is more suited to query for    
 * [[net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern.Pattern]]s and expecting 
 * all valid ''bindings'' (that is 
 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]]s matching the `Pattern`) 
 * as replies.
 * `QueryRef` and `QueryIf` differ in how the receiving actor should react when 
 * the queried triple(s) are not among the actor's believes: when receiving a 
 * `QueryRef` message the receiving actor does not need to reply when it does 
 * not know about the triples.  See [[QueryIf QueryIf §Negation]] for more 
 * information.
 *
 * The
 * [[http://www.fipa.org/specs/fipa00037/index.html SC00037J FIPA standard]] 
 * states:
 *
 * ''"`QueryRef` is the act of asking another agent to inform the requester of 
 * the object identified by a descriptor. The sending agent is requesting the 
 * receiver to perform an inform act, containing the object that corresponds to 
 * the descriptor.''
 *
 * ''The agent performing the `QueryRef` act:''
 *  - ''does not know which object or set of objects corresponds to the
 *    descriptor [(that is `content`)], and,''
 *  - ''believes that the other agent can inform the querying agent the
 *    object or set of objects [(i.e.  
 *    [[net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern.Pattern]])] that 
 *    correspond to the descriptor."''
 *
 * @param content	a set of patterns of RDF-statements that the sender wishes
 *		the receiver to complete.
 *
 * @tparam T	the class of the RDF-statement, it can be any of the types
 *		supported by [[Performative]].
 *		Supported types:
 *		  - [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Triple]],
 *		  - [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]],
 *		  - [[net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern.Pattern]],
 *		  - a [[scala.Seq]] of the above, or
 *		  - a `Class` of `QuadruplePattern`.
 *
 *		  - The primarily intended use of `QueryRef` is `content` of type 
 *		  [[net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern.Pattern]]; i.e.  
 *		  querying for all triples that match the `content` pattern and the 
 *		  receiver believes.  The receiver will reply with a series of 
 *		  [[Inform]]-messages: one for each matching triples.
 *      - When `content` is a [[scala.Seq]] of `QuadruplePatterns` sets of
 *		    matching set of patterns; see the discussion of patterns versus 
 *		    [[net.kaspervandenberg.akkaRdf.rdf.Rdf.BNode BNodes]] in
 *		  - When `content` is a [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Triple]]
 *		    or a [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]], the 
 *		    receiver will reply with a single [[Inform]]-message if and only if 
 *		    it believes the triple or the receiver will remainn silent if it 
 *		    does not believe the triple.
 *      - When `content` is a [[scala.Seq]] of `Triples` or `Quadruples`, the 
 *        receiver will reply with an `Inform`-message if it can match all 
 *        triples in the sequence.
 */
case class QueryRef[+T](override val content: T)
			extends Performative[T](content);
