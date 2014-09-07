// © Kasper van den Berg, 2014
package net.kaspervandenberg.akkaRdf.messages.fipa

/**
 * The sender notifies the receiver that the sender could not perform a 
 * previous request of the receiver.  For example when actor 𝛼 sent a
 * [[QueryIf QueryIf(𝜑)]] to actor 𝛽 and actor 𝛽 does not believe 𝜑, actor 𝛽 
 * should send `Failure(𝜑)` to actor 𝛼 (see [[QueryIf QueryIf § Negation]]).
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
 * constant true. Often it is the case that there is little either agent can do 
 * to further the attempt to perform the action."''
 *
 * Using `Failure` to indicate that a triple does not exist within the agent's 
 * believe base differs some what from the
 * [[http://www.fipa.org/specs/fipa00037/index.html SC00037J FIPA standard]].
 *  - the action of "looking for 𝜑" '''was''' done, but the triple was not
 *    found.
 *  - the content of `Failure` does not contain a tuple (action, reason);
 *    instead it contains only content of action.
 *
 * = Failure vs. [[NotUnderstood]] =
 * When an actor receives a message that it doesn't support it should send a 
 * [[NotUnderstood]]-message and not a `Failure`.  The meaning of `Failure` is 
 * limited to:
 *  1. knowing what a request means but not being able to fulfill it; or
 *  1. requested triples that are missing.
 *
 * = Consideration: representing actions in content =
 * Currently `content` is restricted to contain RDF tipples, having `Failure` 
 * comply with the FIPA standard requires content to be an action.  There are 
 * three possibilities:
 *
 *  1. Is being able to represent actions necessary within the context of
 *     AkkaRdf?  If actions are not needed, using RDF tuples suffices.
 *  1. It is possible to represent FIPA actions in RDF.
 *  1. The requirement on `T`, the type of `content`, being RDF triples can
 *     be loosened so that actions and tupples are supported; but this will 
 *     make all receivers of [[Performative]]s more complex.
 *
 * For now the first option is chosen.
 *
 * @param content		The RDF statement (or graph) that is was queried by
 *		the other actor and 'not' believed this actor.  That is the `content` 
 *		of the `QueryIf`-message.
 *
 * @tparam T	the class of the RDF-statement, it can be any of the types
 *		supported by [[Performative]].
 */
case class Failure[+T](override val content: T) extends Performative[T](content);

