// © Kasper van den Berg, 2014
package net.kaspervandenberg.akkaRdf.messages.fipa
/**
 * All fipa performatives derive from Performative.
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
 *						[[akka.pattern.AskableActorRef AskableActorRef#ask]] 
 *						construct allow specifying a time-out.  With this 
 *						construct the receiver is not notified of the time-out.  
 *						Perhaps this is enough.
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
 *
 * @constructor		create a performative with the given content.
 *
 * @param content		The content of the performative.
 *		Form [[http://www.thefreedictionary.com/content The Free Dictionary]]
 *		''"The individual items or topics that are dealt with in a	 
 *		publication or document."'' and ''"the substantive or meaningful 
 *		part"'' (not knowing a better definition for this value).
 *
 * @tparam T the type of content of the performative, it should be one of:
 *		  - [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Triple]],
 *		  - [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]],
 *		  - [[net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern.Pattern]], or
 *		  - a [[scala.Seq]] of the above.
 *
 *		The type of content can be further restricted by subclasses of 
 *		`Performative` and actors can support only a subset of types.
 */
abstract class Performative[+T](val content: T) {
}
