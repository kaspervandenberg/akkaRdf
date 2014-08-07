// © Kasper van den Berg, 2015
package net.kaspervandenberg.akkaRdf.messages.fipa

/**
 * Types of messages AkkaRdf actors can receive.  Modelled after the FIPA agent
 * communications language:
 * [[http://www.fipa.org/specs/fipa00061/index.html SC00061G FIPA ACL
 * Message Structure Specification]], and
 * [[http://www.fipa.org/specs/fipa00037/index.html SC00037J Communicative
 * Act Library Specification]].
 *
 * == Parts to omit from the FIPA message structure ==
 * Many parts of the FIPA message structure (SC00061G) are part of Akka,
 * therefore AkkaRdf messages do not repeat them:
 *
 *  - `sender`:	[[akka.actor.Actor Actor#self]] for the actor that will send
 *						the message, and [[akka.actor.Actor Actor#sender()]] for the
 *						actor that has received the message.
 *  - `receiver`: [[akka.actor.ActorRef]]-object on which
 *						[[akka.actor.ActorRef ActorRef#!]] or
 *						[[akka.actor.ActorRef ActorRef#tell]] for the actor that will 
 *						send the message, and [[akka.actor.Actor Actor#self]] for the 
 *						actor that receives the message.
 *  - `reply-to`:	in the FIPA standard `reply-to` refers to the agent
 *						to whom the receiver must reply, that is `sender` and `reply-to` 
 *						can differ.  Akka does not have a `reply-to` value instead the 
 *						`sender` can be faked and the receiver will answer to `sender()`:
 *						[[akka.actor.ActorRef ActorRef#forward]] forwards
 *						a message to the [[akka.actor.ActorRef]]-object retaining the 
 *						sender that sent it to this actor; and
 *						[[akka.actor.ActorRef ActorRef]] allows overriding the sender.
 *  - `language`, `encoding`: are omitted under the assumption that the
 *						receiving actor is and akka actor and that all AkkaRdf actors 
 *						send and receive RDF triples and that scala/akka serialise and 
 *						de-serialise the scala objects that contain these triples 
 *						predictably.
 *  - `ontology`: can be omitted since the RDF URIs can point to ontologies.
 *  - `reply-with` and `in-reply-to`: allow FIPA agents to distinguish threads
 *						of conversation conversation threads are handled in an akka 
 *						specific method.
 *						When a sending actor needs to distinguish between replies from 
 *						several receivers it should spawn dedicated actors for receiving 
 *						a reply from each recipient.
 *
 * What to do with the following parameters remains an open issue:
 *  - `protocol`: the FIPA standard (SC00061G) declares `protocol` as option,
 *						but warns that ACL without the framework of protocol (and thus 
 *						directly using the ACL semantics to control the agent's 
 *						generation and interpretation of ACL) is an extremely ambitious 
 *						undertaking.  The following options can be taken:
 *    1. Omit the `protocol` field, but have an implicit protocol that the
 *							actors follow and by which they are restricted;
 *    1. Omit the `protocol` and deal with the resulting ``extreme ambition``;
 *							or
 *    1. Include the `protocol` field.
 *    1. Move the protocol into the RDF content.
 *    For now the first option is chosen for AkkaRdf.
 *  - `reply-by`: specifies a time-out before which the recipient has to reply.
 *						On the sender side akka's
 *						[[akka.pattern.AskableActorRef AskableActorRef#ask]] construct 
 *						allow specifying a time-out.  With this construct the receiver is 
 *						not notified of the time-out.  Perhaps this is enough.
 *
 * == Used FIPA message structure parts ==
 *
 * With these assumptions and restrictions the FIPA message structure only 
 * requires two fields:
 *  - `performative`: performatives define what the sending agent/actor expects
 *						the receiving agent/actor to do with the message.  For example 
 *						some messages are just to inform the receiving agent while others 
 *						are questions which the sender hopes the receiver can answer.
 *  - `content`: the content of the message.  The content will be in the form
 *						of [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Triple RDF Triples]],
 *						[[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple RDF Quadruples]],
 *						or
 *						[[net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern.Pattern patterns of Quadruples]].
 *
 * Each performative is modelled as a case class with the message's content as 
 * a property.
 */
object Performatives {

	/**
	 * All performatives derive from Message.
	 *
	 * @constructor		create a Message with the given content.
	 *
	 * @param content		The content of the message.
	 *		Form [[http://www.thefreedictionary.com/content The Free Dictionary]]
	 *		''"The individual items or topics that are dealt with in a	 
	 *		publication or document."'' and ''"the substantive or meaningful 
	 *		part"'' (not knowing a better definition for this value).
	 *
	 * @tparam T the type of content of the message, it should be one of:
	 *		  - [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Triple]],
	 *		  - [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]],
	 *		  - [[net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern.Pattern]], or
	 *		  - a [[scala.Seq]] of the above.
	 *
	 *		The type of content can be further restricted by subclasses of 
	 *		`Message` and actors can support only a subset of types.
	 */
	abstract sealed class Message[+T](val content: T); 

	/**
	 * The sender informs the receiver that `content` is true (that is the sender 
	 * believes `content` to be true).
	 *
	 * From the
	 * [[http://www.fipa.org/specs/fipa00037/index.html SC00037J FIPA standard]]:
	 *
	 * ''"`Inform` indicates that the sending agent:''
	 *  - ''holds `content` for true;''
	 *  - ''intents that the receiving agent also comes to believe that `content`
	 *    is true; and''
	 *  - ''does not already believe that the receiver has any knowledge of the
	 *    truth of `content`."''
	 *
	 * @param content		The RDF statement (or graph) that is believed and to
	 *		inform the receiver about.
	 *
	 * @tparam T	the class of the RDF statement; the possible types are a subset
	 *		of the types of [[Message]]:
	 *		  - [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Triple]],
	 *		  - [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]],
	 *		  - a [[scala.Seq]] of the above.
	 *
	 *		`Inform` message containing a 
	 *		[[net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern.Pattern]] are 
	 *		''not'' supported.  Use `Triples` containing 
	 *		[[net.kaspervandenberg.akkaRdf.rdf.Rdf.BNode]] to inform about graphs 
	 *		with unidentified parts.
	 */
	case class Inform[+T](override val content: T) extends Message[T](content);

	/**
	 * The sender notifies the receiver that the sender could not perform a 
	 * previous request of the receiver.  For example when actor 𝛼 sent a 
	 * [[QueryIf QueryIf(𝜑)]] to actor 𝛽 and actor 𝛽 does not believe 𝜑, actor 
	 * 𝛽 should send `Failure(𝜑)` to actor 𝛼 (see
	 * [[QueryIf QueryIf § Negation]]).
	 *
	 * The FIPA standard states:
	 *
	 * ''"`Failure` is an abbreviation for informing that an act was considered 
	 * feasible by the sender, but was not completed for some given reason.
	 *
	 * The agent receiving a failure act is entitled to believe that:''
	 *  - ''the action has not been done, and,''
	 *  - ''the action is (or, at the time the agent attempted to perform the
	 *      action, was) feasible.''
	 *
	 * ''The (causal) reason for the failure is represented by the proposition, 
	 * which is the second element of the message content tuple. It may be the 
	 * constant true. Often it is the case that there is little either agent 
	 * can do to further the attempt to perform the action."''
	 *
	 * Using `Failure` to indicate that a triple does not exist within the 
	 * agent's believe base differs some what from the
	 * [[http://www.fipa.org/specs/fipa00037/index.html SC00037J FIPA standard]].
	 *  - the action of "looking for 𝜑" '''was''' done, but the triple was not
	 *    found.
	 *  - the content of `Failure` does not contain a tuple (action, reason);
	 *    instead it contains only content of action.
	 *
	 * == Consideration: representing actions in content ==
	 * Currently `content` is restricted to contain RDF tipples, having 
	 * `Failure` comply with the FIPA standard requires content to be an 
	 * action.  There are three possibilities:
	 *
	 *  1. Is being able to represent actions necessary within the context of
	 *     AkkaRdf?  If actions are not needed, using RDF tuples suffices.
	 *  1. It is possible to represent FIPA actions in RDF.
	 *  1. The requirement on `T`, the type of `content`, being RDF triples can
	 *     be loosened so that actions and tupples are supported; but this will 
	 *     make all receivers of [[Message]]s more complex.
	 *
	 * For now the first option is chosen.
	 *
	 * @param content		The RDF statement (or graph) that is was queried by
	 *		the other actor and 'not' believed this actor.  That is the 
	 *		`content` of the `QueryIf`-message.
	 *
	 * @tparam T	the class of the RDF-statement, it can be any of the types
	 *		supported by [[Message]].	 */
	case class Failure[+T](override val content: T) extends Message[T](content);

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
	 *    sender can use a [[akka.pattern.AskableActorRef AskableActorRef#?]] 
	 *    with a time-out.
	 *  - RDF operates on the open world assumption: not knowing 𝜑 does not imply
	 *    ¬ 𝜑; indeed, negation does not exist in RDF.
	 * 
	 * A solution is to distinguish between `QueryIf` and [[QueryRef]]:
	 *  - When receiving a `QueryRef`-message, an actor replies if and only if
	 *    it ``beliefs`` RDF-statements unifiable with the contents of 
	 *    `QueryRef`, otherwise the actor remains silent.
	 *  - When receiving a `QueryIf`-message, an actor always replies.  The actor
	 *    replies with either [[Inform]] or with [[Failure]].  Note that because 
	 *    of akka's distributed/network architecture it can happen that — 
	 *    although after `QueryIf` a receiver should reply — a sender never 
	 *    receives a reply from the receiver.
	 *
	 * @param content	a set of RDF-statements that the sender wishes the receiver
	 *		to confirm.
	 *
	 * @tparam T	the class of the RDF-statement, it can be any of the types
	 *		supported by [[Message]].  However for 
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
	case class QueryIf[+T](override val content: T) extends Message[T](content);

	/**
	 * The sender asks the receiver for triples matching the `content`.
	 *
	 * This message is similar to [[QueryIf]]; compared to `QueryIf`, `QueryRef` 
	 * is more suited to query for    
	 * [[net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern.Pattern]]s and 
	 * expecting all valid ''bindings'' (that is 
	 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]]s matching the 
	 * `Pattern`) as replies.
	 * `QueryRef` and `QueryIf` differ in how the receiving actor should react 
	 * when the queried triple(s) are not among the actor's believes: when 
	 * receiving a `QueryRef` message the receiving actor does not need to reply 
	 * when it does not know about the triples.  See [[QueryIf QueryIf 
	 * §Negation]] for more information.
	 *
	 * The [[http://www.fipa.org/specs/fipa00037/index.html SC00037J FIPA 
	 * standard]] states:
	 *
	 * ''"`QueryRef` is the act of asking another agent to inform the requester 
	 * of the object identified by a descriptor. The sending agent is requesting 
	 * the receiver to perform an inform act, containing the object that 
	 * corresponds to the descriptor.''
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
	 *		supported by [[Message]].
	 *		Supported types:
	 *		  - [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Triple]],
	 *		  - [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]],
	 *		  - [[net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern.Pattern]], or
	 *		  - a [[scala.Seq]] of the above.
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
	 *      - When `content` is a [[scala.Seq]] of `Triples` or `Quadruples`, 
	 *        the receiver will reply with an `Inform`-message if it can match 
	 *        all triples in the sequence.
	 */
	case class QueryRef[+T](override val content: T) extends Message[T](content);
}
